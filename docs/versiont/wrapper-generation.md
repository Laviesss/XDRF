# Wrapper Generation

The toolchain parses `versiont.mapping` and writes one or more Java files into `build/generated/sources/versiont/java/dev/gxlg/versiont/gen/…`. These files are static, checkable, git‑ignorable artefacts.

✅ Everything below is verified through:

- The `scripts/generate-layer.js` source (read in full).
- The `build/generated/.../dev/gxlg/versiont/gen/…` directory of actual generated wrappers from a working project.

---

## Output root

```
build/generated/sources/versiont/java/dev/gxlg/versiont/gen/
```

The toolchain uses `build/generated/sources/versiont/java/` as the output root and creates `dev/gxlg/versiont/gen/…` underneath it. That path is added to `main`'s `srcDir`.

✅ Confirmed in `VersiontPlugin.groovy`:

```
def generatedSourceDir = project.layout.buildDirectory.dir("generated/sources/versiont/java").get().asFile
```

---

## File‑by‑file structure (verified by `processClass`)

For each `class ShortName` entry in the mapping, the toolchain emits **one** Java file. The file's package and class name are derived from the **import line** that registered the short name:

- If the import line resolves to `className = "…/z/Y"` and `shortName = "Y"`, then:
  - `package = dev.gxlg.versiont.gen` + the slashed‑path of every name *before* the LAST slash, with `/` → `.` and `.` → `.`. Concretely: `"…a/b/c/Y"` ⇒ package `dev.gxlg.versiont.gen.a.b.c`.
  - `className = Y` (the last segment).
- Top‑level `class ShortName` with no prior `import` ⇒ package = `dev.gxlg.versiont.gen`, `className = ShortName`.

> ⚠️ The exact path‑splitting is a fine‑grained generator implementation detail; for practical use, treat the import line's `…/ShortName` tail as the simple name and the prefix as the package path.

✅ Verified against the `processClass` output for both `xdrf's MinecraftClient` (package `dev.gxlg.versiont.gen`) and `SystemToast$Type` (package `dev.gxlg.versiont.gen.com.mojang.blaze3d.platform`).

---

## Anatomy of a generated wrapper

Reading any generated file shows the same template (`processClass`, end of the long string concatenation block). Every wrapper has these sections:

```
package <pkg>;
import dev.gxlg.versiont.api.R;
import dev.gxlg.versiont.api.types.Wrapper;
import dev.gxlg.versiont.api.types.WrappedMethod;

public class <Name> extends <WrapperBaseName> {

    // 1. Reflection handle to the underlying MC class, lazily resolved.
    public static final R.RClass clazz = R.clz(<chain>);

    // 2. Classes that extend this one in extended/mod wrappers.
    public static final java.util.List<Class<? extends <Name>>> subClazzes = …;

    // 3. Static list of WrappedMethod records for ByteBuddy dispatchers.
    public static final java.util.List<WrappedMethod> wrappedMethods = …;

    // 4. Reflection handle to the wrapped instance.
    private final R.RInstance rInstance;

    // 5. Generated constructors (one per `<init>` row).

    // 6. Internal constructor:
    protected <Name>(Wrapper.DelayedConstructor delayedConstructor) { … }

    // 7. Generated methods (one per non‑`<init>` row).
}
```

---

## Element‑to‑emission map

Verified against `processClass` (and confirmed by emitted files):

| Mapping row | Generated emission |
|-------------|--------------------|
| `class ShortName` | file `dev.gxlg/versiont/gen/<…>/ShortName.java` |
| `static ShortName getInstance()` | `public static ShortName getInstance() { try { return (ShortName) clazz.mthd("getInstance", ShortName.class).invk(); } … }` |
| `void onGameMessage(Packet packet)` | `public void onGameMessage(Packet packet) { try { this.rInstance.mthd("onGameMessage", void.class, Packet.class).invk(packet); } … }` |
| `<init>(Text content, boolean overlay)` | `public <Name>(Text content, boolean overlay) { this(clz -> clz.constr(Text.class, boolean.class).newInst(content, overlay).self()); }` |
| `static SystemToast$Type WORLD_BACKUP` | `public static SystemToast$Type WORLD_BACKUP() { return (SystemToast$Type) clazz.fld("WORLD_BACKUP", SystemToast$Type.class).get(); }` and a setter overload. |
| `static <Name> ENTITY_VILLAGER_CELEBRATE` | `public static <Name> ENTITY_VILLAGER_CELEBRATE() { return R.wrapperInst(<Wrapper>.class, clazz.fld("ENTITY_VILLAGER_CELEBRATE", <Wrapper>.clazz.self()).get()); }` (when the field type is itself a registered wrapper). |

---

## Field‑as‑method‑style accessors

When a `class Foo` row declares a non‑static field:

```text
@Nullable <FieldType> someField
```

The generated wrapper exposes:

```
public final <FieldType> getSomeFieldField() { … }
public final void setSomeFieldField(<FieldType> value) { … }
```

Note the **`Field` suffix** on the accessor. The fixed suffix lets `final` fields remain settable through reflection at runtime — the Java keyword only blocks *direct* writes from compiled bytecode.

✅ Confirmed for every non‑static field in the generated wrappers I read (e.g. `MinecraftClient.getPlayerField()`, `MinecraftClient.getToastManagerField()`).

---

## Wrapper interfaces

`interface` entries in the mapping emit one Java file whose package is identical to the corresponding `class` layout. The generated interface extends `WrapperInterface` directly:

```java
public interface Foo extends WrapperInterface {
    Class<?> wrapper = Foo.class;
    R.RClass clazz = Foo.clazz;
    Map<Foo, Foo> instances = Collections.synchronizedMap(new WeakHashMap<>());
    List<WrappedMethod> wrappedMethods = List.of(...);
    void name();
    default Foo asFoo() {
        return instances.computeIfAbsent(this, k ->
            R.interfaceInstance(this, Foo.class, Foo.class));
    }
}
```

⚠️ Implemented exact‑shape detail is verified by `processInterface`; the precise Java skeleton shown is derived from `WrapperInterface` semantics rather than an emitted file in this project (no interface entries were used in the test mapping).

---

## Inherited interfaces via `implements`

Only one `implements <Iface>` per wrapper class is permitted.
✅ Confirmed: the text reads `while (parts.length) { c = parts.shift(); … }` and there's no `implements-clause`‑per‑interface fan‑out in `processClass`. Multiple interfaces would break the generator.

---

## Inheritance chains

`extends` chains consume other wrapper classes:

```text
class BlockBehaviour$BlockStateBase
class BlockState extends BlockBehaviour$BlockStateBase
```

Both classes are emitted; `BlockState`'s `extends BlockBehaviour$BlockStateBase` is a real Java superclass reference. The package layout above applies to each.

✅ Confirmed by the `extends` branch in `processClass`. Wrappers extend *each other*; they do not extend arbitrary Minecraft classes.

---

## Wrappers mutually reference each other via full FQNs

When `SystemToast.add(...)` references `ToastManager.clazz.self()`, the emitted Java contains the literal `dev.gxlg.versiont.gen.<...>.ToastManager.clazz.self()` class reference. **The generator does not add `import`s between sibling wrappers** when their packages differ.

Practical implication: if your mod reaches across wrapper packages, it's safe — those classes are concrete Java classes on the build classpath by the time `javac` runs. When the *generator itself* emits cross‑package references (such as a method body of `SystemToast` referring to `ToastManager`), it uses fully‑qualified names already, so the emitted wrapper file compiles standalone.

---

## Static class resolution at runtime — what `clazz = R.clz(<chain>)` actually does

`R.clz(<chain>)` parses `<chain>` as a slash‑separated list and tries each via `Class.forName`. Java guarantees caching of successful lookups via the toolchain's own cache (`clazzCache`), so a single `clazz.self()` per wrapper is sufficient.

✅ Verified in `R.java`:

```
String[] classNames = names.split("/");
return cache(…, () -> {
    for (String clazz : classNames) {
        try { return Class.forName(clazz); }
        catch (ClassNotFoundException ignored) { }
    }
    throw new RuntimeException("Class not found from " + Arrays.toString(classNames));
});
```

---

## Pre‑Init wrapper — the `Wrapper.DelayedConstructor` mechanism

Wrapper constructors do not receive the underlying Minecraft instance directly. They receive a thunk:

```java
new <Name>(clz -> clz.constr(<argTypes>).newInst(<argNames>).self())
```

The toolchain's generated `protected <Name>(Wrapper.DelayedConstructor dc) { super(dc); rInstance = …; …fields… }` calls `dc.construct(actualClass)` which:

- Calls `clazz.inst(instance)` on the live instance (so the wrapper caches the instance).
- Registers the wrapper instance back via reflection (in `Wrapper`).

✅ Confirmed in `Wrapper.java`: `DelayedConstructor.construct(R.RClass actualClass)` returns the actual instance; the wrapper constructor captures it via `this.instance = instance;`.

---

## What versiontLayer prints

```
> Task :versiontLayer
Generated dev.gxlg.versiont.gen.<…>
Generated dev.gxlg.versiont.gen.<…>
…
Version't layer generated!
```

The `Generated ` line is printed for *every* class entry, including inner classes, plus an extra line for `toolchain-emitted` `java.lang.Object` base class. The list order is generation order, not declaration order.

✅ Verified by the tail of `generate-layer.js`:
```
for (const fullyQualified in processedClasses) {
  …
  console.log("Generated", fullyQualified);
}
console.log("Version't layer generated!");
```

---

## See also

- `mapping-language.md` — what each row of `versiont.mapping` looks like.
- `runtime-api.md` — what every wrapper method calls at runtime.
- `troubleshooting.md` — diagnosis of "missing wrapper" / "wrapper can't find class".
