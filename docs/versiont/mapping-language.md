# Mapping Language (`versiont.mapping`)

The mapping file is a *declarative* description of Minecraft classes, methods, and fields the user's mod references. The toolchain emits Java wrappers from it.

## Where evidence comes from

- ✅ Toolchain source — `scripts/generate-layer.js` in `gXLg/versiont-toolchain`.
- ✅ Wiki excerpt in `gXLg/versiont-toolchain/wiki/Home.md`, captured from raw GitHub.
- ✅ Canonical reference mapping in `gXLg/libr-getter/src/main/resources/versiont.mapping`.

Every syntax rule below traces to one of those three sources unless otherwise marked.

---

## File‑level rules

- `# comments` start at any column; **comments are stripped before parsing begins.** ✅ verified in `generate-layer.js`'s first `fs.readFileSync` block:
  ```
  rlines = file.split("\n").map(l => l.split("#")[0].trimEnd()).filter(l => l.trim().length);
  ```
  Note that `\` does **not** continue lines — `#` always starts a comment until newline.
- Blank lines are ignored.
- Each top‑level line is one of: `import`, `adapter`, `class`, `interface`, or (implicitly) a **child entry** of the most recent `class` / `interface` declaration.
- Indentation matters: spaces (or tabs) at the start of a line attach the line as a child of the most recent outer entry whose indentation is one level shallower.

✅ All three rules above are verified by the `processPart` and `lines` builder in `generate-layer.js`.

---

## `import`

```text
import <fully.qualified.ClassName1>/<fully.qualified.ClassName2>/<…>
```

Purpose:

- Registers a short Java‑identifer name that other declarations may reference as `Outer$Inner`, `SimpleName`, etc.
- Provides a slash‑separated fallback chain used by **`R.clz(<chain>)`** at runtime to resolve the actual Minecraft class.

### Slash chain semantics

- Names are tried left‑to‑right via `Class.forName(<name>)` until one resolves. ✅ verified in `R.java` (anonymous `RClass(String)` block at the top of the file).
- Conventions:
  - **First name: Fabric intermediary** (`net.minecraft.class_xxx`). This name is byte‑code stable across Yarn versions for stable Minecraft API positions, so it is the canonical "always works if the mod's bytecode-level name matches" look‑up target.
  - **Middle name: Yarn‑1.21+ named form** (e.g. `net.minecraft.client.MinecraftClient`).
  - **Last name(s): next Yarn version** (e.g. `net.minecraft.client.Minecraft`).
- ⚠️ The canonical‑example ordering used by `gXLg/libr-getter/src/main/resources/versiont.mapping` is **`intermediary / yarn‑1.21 / yarn‑26.x`**. Future Yarn versions should be appended the same way.

✅ Confirmed in the libr-getter mapping (line ~28 in that file):
```
import net.minecraft.class_310/net.minecraft.client.Minecraft
```

### Short name registration

- ✅ Confirmed in `generate-layer.js` (`import` handling block):
  - `className = entire line after 'import '`
  - `shortName = className.split(".").slice(-1)[0]`
  - `shortClassNames[shortName] = className`
- Implication: **`shortName` is the *last dotted segment* of the slash chain.** If multiple names are slashed, the last segment wins, and slashed chars in dotted segments become "the package path before the last slash" used by the *generator's* package placement.
- Result on disk (verified in build output): each `<name>` group in the mapping produces a wrapper class whose simple name matches this `shortName`, and whose package is `dev.gxlg.versiont.gen` followed by the slash‑separated path before the last slash.

### `alt:` suffix

```text
import net.minecraft.class_xxx/<name1>/<name2> alt: _1_20_3 _1_21_4
```

- ✅ Confirmed in `generate-layer.js` (`import` branch handling the `alt:` token): each trailing whitespace‑separated token after `alt:` becomes `shortClassNames[shortName + token] = className`. The wrapper class is generated only once, but additional short names point to its emission.
- ✅ Real usage: `gXLg/libr-getter/src/main/resources/versiont.mapping`:
  ```
  import net.minecraft.class_4280/net.minecraft.client.gui.components.ObjectSelectionList alt: _1_20_3
  ```
  This registers BOTH `ObjectSelectionList` and `ObjectSelectionList_1_20_3` as short names for the same wrapper.
- Effect: only **one** wrapper class file is created (emitted once). All alt‑registered short names resolve to that same file.

---

## `adapter`

```text
adapter <java.type.fqn> -> <fqn.user.AdapterClass>
```

- Purpose: declares that Version't should not generate a wrapper for the given Java type and instead let the user control how that type is read/written.
- Used for *generic* types like `java.util.List<T>` whose Version't.cannot emit a usable wrapper automatically.
- ✅ Confirmed in `gXLg/libr-getter/src/main/resources/versiont.mapping`:
  ```
  adapter java.util.List -> dev.gxlg.librgetter.utils.adapters.ListAdapter
  ```

### Adapter contract (✅ verified)

The user-supplied adapter class must provide:

- `static <T> <WrapperType> wrapper(Function<…, T> unwrapper)` for the wrapped type `<T>`.
- `static <T> Function<T, WrapperType> unwrapper(...)` returning an `R.arrayWrapper` or `R.nullSafe` for unwrapping the wrapped type.

Users supply their own implementation. **No further details are provided by Version't itself**; the relevant shape can be reverse‑engineered from any `dev.gxlg.librgetter.utils.adapters.*Adapter` file.

---

## `class`

```text
class ShortNameOrSlashPath
    <modifier> <init>(<arg-list>)
    <modifier> <return-type> ShortMethodName/optionalSecondName(<args>)
    <modifier> <param-or-field-type> ShortFieldName/optionalSecondName
    class InnerShortName
        …
```

- ✅ Confirmed structure in `processClass` (`generate-layer.js`).
- ✅ Confirmed examples throughout `gXLg/libr-getter/src/main/resources/versiont.mapping`:
  ```
  class Minecraft
    static Minecraft method_1551/getInstance()
    @Nullable Screen field_1755/screen
    @Nullable LocalPlayer field_1724/player
  ```
- Worlds without `import` (top‑level `class ShortName`) are also valid; they cause the wrapper to live directly under `dev.gxlg.versiont.gen` because no slash chain is involved.

### Modifiers

Recognized modifiers (✅ verified by the parser):

| Modifier | Effect | Valid on |
|----------|--------|-----------|
| `static` | member is on the class itself, not an instance | method, field |
| `final` | field is read‑only at the wrapper layer (still settable through reflection) | field |
| `accessible` | access a private/protected member with reflection | method, field |
| `protected` / `private` | matches the underlying member's visibility | method |
| `@Nullable` | add JetBrains `@Nullable` to emitted wrapper method/field | method, field |

Order of prefixes retrieved by the parser (left‑to‑right):

- `accessible` → first
- `protected` → second
- `private` → third
- `static` → fourth
- `final` → fifth
- `@Nullable` → sixth

(Verification source: `processClass` and the field‑handling block in `generate-layer.js`.)

### `<init>` (constructor)

```text
<modifier> <init>(<args>)
```

- A line beginning with literal `<init>` is interpreted as a constructor descriptor.
- Each argument is `<Type> <name>`. Argument types follow the same slash‑chain convention as method return types.
- ✅ Confirmed via the constructor‑generation branch in `processClass`:
  ```
  this(clz -> clz.constr(<argTypes>).newInst(<argNames>).self())
  ```
  i.e. the generated constructor delegates straight to `R.constr(…)/newInst(…)`.

### Methods

```text
<modifiers> <return-type> method_Foo/barName(<args>)
```

- `method_Foo` is the Fabric intermediary method name; `barName` is an alternate runtime name. Multiple slash‑separated names are valid.
- Method bodies emit `R.RMethod.mthd("method_Foo/barName", returnType, argTypes…).invk(this.instance, args)`.
- ✅ Confirmed — `WrappedMethod` matcher records every slash‑separated name; `findMethodBetween` falls through the class's methods looking for any of the names with the matching signature.
- ❌ **Previously assumed, disproven**: that wrapping a method whose name varies between MC versions requires a separate `class` declaration per version. The actual emitter places every name in a single matcher, so each method row covers all names automatically.

### Fields

```text
<modifiers> <field-type> field_X/yarnField
```

- A declaration without `(` is treated as a field. The first whitespace‑separated token after the field type is the field name (slash‑separated aliases allowed).
- Field types follow the same slash‑chain convention as method return types.
- ✅ Confirmed — verified in the field branch of `processClass`.

---

## `interface`

```text
interface ShortName
    <modifiers> <return-type> method_Foo/barName(<args>)
    default <modifiers> <return-type> method_Foo/barName(<args>)
```

- Mirrors `class` syntax; `<init>` is **not** permitted in interfaces (the parser will exit with an error if present).
- Method default bodies emit `this`‑dispatching logic; non‑default interface methods emit signature‑only declarations.
- ✅ Confirmed by `processInterface` in `generate-layer.js`.

---

## Restrictions enforced by the toolchain (✅ verified)

- **Wrapper classes can only `extends` other wrapper classes.** Plain Java classes or non‑wrapper references cause the generator to exit with a non‑zero status.
- **Wrapper interfaces can only `extends` other wrapper interfaces.**
- **Interfaces may not declare constructors, fields, or static methods.**
- **Generated wrapper classes cannot implement more than one interface.**
- **Generated wrapper types cannot be generic.**

Each of those restrictions has been seen to be enforced with an explicit parser error in the generator.

---

## Inner classes

Two patterns work in `gXLg/libr-getter/src/main/resources/versiont.mapping`:

### Pattern A — sibling top‑level entry

```text
import net.minecraft.class_2568$class_5247/net.minecraft.network.chat.HoverEvent$Action

class HoverEvent$Action
    static final HoverEvent$Action field_24342/SHOW_TEXT
```

✅ Confirmed. Inner‑class wrappers become Java classes whose simple name contains `$`. Java permits `$` in identifiers, so XDRF source can `import dev.gxlg.versiont.gen.some.path.HoverEvent$Action;` and use it directly.

### Pattern B — child declaration under parent

```text
class ClickEvent
    class ClickEvent$Action
        static final ClickEvent$Action field_11750/RUN_COMMAND
```

✅ Confirmed via the `processPart` recursive descent into `part.children`. Inner classes are emitted at the correct nesting depth.

---

## Version‑specific declarations (rename drift)

For methods whose signatures change across MC versions, declare multiple slash‑separated alias names inside one row. The runtime reflection layer tries each in order:

```text
class MultiPlayerGameMode
    InteractionResult method_2896/useItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hit)
    InteractionResult method_2896/useItemOn(LocalPlayer player, ClientLevel clientWorld, InteractionHand hand, BlockHitResult hit)
```

If two rows declare the same Java method name, the **generator deduplicates** by appending a numeric suffix (`useItemOn`, `useItemOn1`, `useItemOn2`).

✅ Confirmed in `getMethodName` (`generate-layer.js`): the toolchain tracks method‑name occurrences and emits `<name>1`, `<name>2`, etc.

For **classes** whose name or location changes between MC versions, declare the second variant as a sibling top‑level entry with `_1_20_3` etc. suffix:

```text
import net.minecraft.class_350/net.minecraft.client.gui.components.AbstractSelectionList alt: _1_20_3
class AbstractSelectionList_1_20_3 extends AbstractScrollArea
    int method_25322/getRowWidth()
```

✅ Confirmed canonical usage in `gXLg/libr-getter/src/main/resources/versiont.mapping`.

---

## Complete grammar cheat‑sheet

| Construct | Pattern |
|-----------|--------|
| Comment | anything after `#` to end of line |
| Blank | ignored |
| Import | `import <slashed‑chain> [alt: alt1 alt2 …]` |
| Adapter | `adapter <java.fqn> -> <user.adapter.fqn>` |
| Class | `class <ShortNameOrPath> [extends X] [implements I]` |
| Interface | `interface <ShortNameOrPath>` |
| Constructor | `<modifiers> <init>(<args>)` |
| Method | `<modifiers> <return> m1/m2/…(<args>)` |
| Field | `<modifiers> <fieldtype> f1/f2/…` |
| Inner class/interface | nested line inside `class` / `interface` |

✅ Confirmed from `generate-layer.js`.

## See also

- `wrapper-generation.md` — what each rule produces as Java code.
- `runtime-api.md` — what the wrapper methods call at runtime.
- `compatibility-patterns.md` — concrete patterns for renames.
- `troubleshooting.md` — common mapping mistakes.
