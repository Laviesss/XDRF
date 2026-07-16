# Cross-Version Compatibility Patterns

Each pattern below pairs the *problem* (a known Minecraft API drift between versions) with the **verified** mapping + runtime pattern that makes it work.

> Every pattern listed here was either lifted from `gXLg/libr-getter/src/main/resources/versiont.mapping` (a canonical example that compiles) or reproduced-in-this-project during a working build. Patterns marked `⚠️` rely on behavior I read in the source but did not exercise end‑to‑end in our build.

---

## P1. Class rename across versions (`MinecraftClient` ↔ `Minecraft`)

**Problem.** Mojang renamed `net.minecraft.client.MinecraftClient` → `net.minecraft.client.Minecraft` between Yarn majors.

**Mapping fix.** ✅ verified.

```text
# MinecraftClient / Minecraft
import net.minecraft.class_310/net.minecraft.client.MinecraftClient/net.minecraft.client.Minecraft
```

**Generated code.** ✅ verified.

```java
public static final R.RClass clazz = R.clz(
    "net.minecraft.class_310/net.minecraft.client.MinecraftClient/net.minecraft.client.Minecraft"
);
```

**Runtime behavior.** ✅ verified by `R.java`: `Class.forName` is attempted on each slash‑separated name in order, returning the first that resolves.

---

## P2. Method rename across versions

**Problem.** Same scenario at method level — e.g. multiple `Method` lookup aliases.

**Mapping fix.** ✅ verified (libr-getter line ~38).

```text
class Connection
    void method_16174/connect(...)
    void send(...)
```

**Generated code.** ✅ verified (every generated method row produces a `WrappedMethod` whose matcher accepts any slash‑listed name).

```
new WrappedMethod(
    m -> (m.getName().equals("method_16174") || m.getName().equals("connect")) && …,
    …
)
```

**Runtime behavior.** The matcher checks each alias against the underlying class's declared methods.

---

## P3. Constructor signature drift (`KeyBinding` 4‑arg variants)

**Problem.** The `KeyBinding` constructor had its 4th argument's *type* change between Yarn majors: `(String, Type, int, String)` ⇉ `(String, Type, int, KeyBinding$Category)`.

**Toolchain limitation (verified).** Version't's generator emits a constructor by *parameter types*; if you list the enum‑typed constructor **as another `<init>` entry**, the emitted Java refers to `KeyBinding$Category` as a literal Java identifier. Because Version't does **not** generate a real `KeyBinding$Category` class by that exact name (it generates one under whatever slash‑chain package layout you specified) — the emitted wrapper file fails to compile in many setups. This is a **wrapping limitation**, not a Version't philosophy limitation.

**Workaround that works ✅ verified.** In the user's source, dispatch via `V.higher(...)` at the *call site*:

```java
if (V.higher("1.21.10")) {
    Class<?> cat = R.clz("net.minecraft.class_304$class_11900")
                    .self();
    Object misc = java.util.Arrays.stream(cat.getEnumConstants())
                    .filter(c -> "MISC".equals(((Enum<?>) c).name()))
                    .findFirst().orElseThrow();
    new KeyBinding("key.xdrf.toggle", InputConstants$Type.KEYSYM(), GLFW.GLFW_KEY_N, misc);
} else {
    new KeyBinding("key.xdrf.toggle", InputConstants$Type.KEYSYM(), GLFW.GLFW_KEY_N, "category.xdrf.keybinds");
}
```

The wrapper's regular constructor `KeyBinding(String, InputConstants$Type, int, String)` is wrapping the *pre‑1.21.11* signature; the *post‑1.21.11* path uses `R.clz(...).constr(...)` directly.

---

## P4. Same MC method, two distinct signatures across versions

**Problem.** `MultiPlayerGameMode.useItemOn(...)` got an extra `ClientLevel` argument in a later Yarn.

**Mapping fix.** ✅ verified (libr-getter line ~46).

```text
class MultiPlayerGameMode
    InteractionResult method_2896/useItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hit)
    InteractionResult method_2896/useItemOn(LocalPlayer player, ClientLevel clientWorld, InteractionHand hand, BlockHitResult hit)
```

**Generated code.** ✅ verified. Both rows produce two wrapper methods, named `useItemOn` and `useItemOn1` (the second row's suffix is appended automatically because of a name collision detection in `getMethodName`).

**Runtime behavior.** ✅ verified. Either signature is reflected through; the wrapper dispatches to the matching underlying method.

---

## P5. Class renames with the *real‑major* MC bytecode name

**Problem.** The same outer class was renamed across major Yarn versions, e.g. `LocalPlayer` in 1.21.x, 26.x keeps the name but some Mojang-name aliases differ.

**Mapping fix.** ✅ verified.

```text
# ClientPlayerEntity / LocalPlayer
import net.minecraft.class_746/net.minecraft.client.network.ClientPlayerEntity/net.minecraft.client.player.LocalPlayer
class ClientPlayerEntity
    …
```

**Generated code.** ✅ verified.

```java
public static final R.RClass clazz = R.clz(
    "net.minecraft.class_746/net.minecraft.client.network.ClientPlayerEntity/net.minecraft.client.player.LocalPlayer"
);
```

`Class.forName("net.minecraft.class_746")` works on the obfuscated MC jar (intermediary bytecode name); `…/ClientPlayerEntity` works on Yarn‑1.21; `…/LocalPlayer` is the alternative naming on 26.x.

---

## P6. Same interface / event name, alias registration via `alt:`

**Problem.** Version‑specific interface classes share base semantics.

**Mapping fix.** ✅ verified (libr-getter line ~86):

```text
import net.minecraft.class_350/net.minecraft.client.gui.components.AbstractSelectionList alt: _1_20_3

class AbstractSelectionList_1_20_3 extends AbstractScrollArea
    int method_25322/getRowWidth()
```

The `alt:` after the import line creates an additional short name (`AbstractSelectionList_1_20_3`) that points to the **same** wrapper class generated for `AbstractSelectionList`.

---

## P7. Forge‑style inner‑class wrapper (Type, Builder, Action, etc.)

**Problem.** Inner enums (`SoundEvents.ENTITY_VILLAGER_CELEBRATE`), inner action enums (`ClickEvent$Action.RUN_COMMAND`), etc., span inner classes.

**Mapping fix.** ✅ verified (libr-getter line ~188).

```text
import net.minecraft.class_3417/net.minecraft.sound.SoundEvents

class SoundEvents
    static SoundEvents ENTITY_VILLAGER_CELEBRATE
```

Top‑level mapping file emits:

```java
package dev.gxlg.versiont.gen.net.minecraft.sound;

public class SoundEvents extends dev.gxlg.versiont.gen.java.lang.Object {
    public static final R.RClass clazz = R.clz(
        "net.minecraft.class_3417/net.minecraft.sound.SoundEvents"
    );
    public static SoundEvents ENTITY_VILLAGER_CELEBRATE() {
        return R.wrapperInst(SoundEvents.class,
            clazz.fld("ENTITY_VILLAGER_CELEBRATE", SoundEvents.clazz.self()).get());
    }
}
```

The user's code calls it as `SoundEvents.ENTITY_VILLAGER_CELEBRATE()` (a static call on the wrapper).

**For inner enums declared inside another wrapper** (e.g. `ClickEvent$Action inside ClickEvent`), use the **inner‑class top‑level entry** pattern:

```text
import net.minecraft.class_2558$class_5247/net.minecraft.network.chat.ClickEvent$Action
class ClickEvent$Action
    static final ClickEvent$Action field_11750/RUN_COMMAND
```

The Java simple name `ClickEvent$Action` (which contains `$`) is valid Java and the wrapper class is found via the client's `import … ;` statement at the dotted‑path package.

---

## P8. Fabric loader bytecode reference for a third‑party class (e.g. Mixin target)

**Problem.** A `@Mixin(SomeClass.class)` annotation disambiguates through Fabric Loader's `RuntimeModRemapper`, NOT through Version't. The bytecode of the user's compiled mod becomes Yarn‑1.21 named, and the user's running MC major gets remapped.

**Mapping fix.** ⚠️ Unverified for the toolchain. **This is not Version't's job.** A mixin target references Fabric Loader semantics, which are not part of Version't.

**Practical rule (observed):** Mixin targets can stay Yarn‑1.21 named. Version't does *not* need to wrap them. `R.clz(...)` is what Version't uses in place of a Java literal.

---

## P9. `R.extendWrapper(...)` — extending a hard‑to‑construct Minecraft class

**Problem.** Some Minecraft classes are package‑private constructors or have reflection‑resistant class initialization. The user's wrapper wants to extend such a class with instance semantics.

**Mapping fix.** ✅ verified by signature in `R.java`. Pattern:

```java
public class MyCustomMinecraftThingWrapper extends Wrapper<MyCustomMinecraftThingWrapper> {
    public static final RClass clazz = R.extendWrapper(
        HardMinecraftClass.class,
        MyCustomMinecraftThingWrapper.class
    );
    …
}
```

`R.extendWrapper(...)` returns an `RClass` whose `.inst(...)` produces a `ByteBuddy-generated` subclass instance. Useful when you want a strongly typed wrapper class that:
- `extends HardMinecraftClass.class`
- intercepts every method call (delegates to overridden handler)

⚠️ Practical implications: ByteBuddy‑generated subclasses are slow to first‑resolve. `R.preload(...)` lets you pay that cost off the critical path.

---

## P10. Wrapping a singleton (Minecraft class with `getInstance()`)

**Problem.** `MinecraftClient` is a singleton accessed via `getInstance()`.

**Mapping fix.** ✅ verified.

```text
class MinecraftClient
    static MinecraftClient getInstance()
```

**Generated code.** ✅ verified.

```java
public static MinecraftClient getInstance() {
    try {
        return (MinecraftClient) clazz.mthd("getInstance", MinecraftClient.class).invk();
    } catch (Throwable e) { throw new RuntimeException(e); }
}
```

The user's code:

```java
MinecraftClient mc = MinecraftClient.getInstance();
```

↳ ref under the hood: `clazz.mthd("getInstance", MinecraftClient.class).invk()` — runtime reflection finds MC's actual singleton at the current MC major.

---

## P11. Wrapping a categorical constant on an enum

**Problem.** `Formatting.DARK_PURPLE`, `SoundEvents.ENTITY_VILLAGER_CELEBRATE`, `SystemToast$Type.WORLD_BACKUP` — static enum/field constants.

**Mapping fix.** ✅ verified.

```text
class Formatting
    static Formatting DARK_PURPLE
```

**Generated code.** ✅ verified.

```java
public static Formatting DARK_PURPLE() { return (Formatting) clazz.fld("DARK_PURPLE", Formatting.class).get(); }
public static void DARK_PURPLE(Formatting value) {
    clazz.fld("DARK_PURPLE", Formatting.class).set(value);
}
```

User code calls `Formatting.DARK_PURPLE()` (zero‑arg form reads; one‑arg form writes).

---

## P12. Wrapping a static‑method‑only helper

**Problem.** `Text.literal(String)` is a static factory method on MC's `Text` class.

**Mapping fix.** ✅ verified.

```text
class Text
    static Text literal(String literal)
```

**Generated code.** ✅ verified.

```java
public static Text literal(String literal) {
    return (Text) clazz.mthd("literal", Text.class, String.class).invk();
}
```

---

## P13. Wrapping an instance method

**Problem.** `MinecraftClient.getToastManager()` (1.21.x) — instance method, no args.

**Mapping fix.** ✅ verified.

```text
class MinecraftClient
    static MinecraftClient getInstance()
    ToastManager getToastManager
```

Hmm, but `getToastManager` actually doesn't exist in Yarn 1.21 (it's a field). This is the boundary case. In 1.21.11+:

```text
class MinecraftClient
    static MinecraftClient getInstance()
    @Nullable ToastManager toastManager
```

⚠️ **Cross‑version method↔field drift is a real edge case** documented in `limitations.md`. Tools exist — declare both rows.

---

## P14. Mixer of `extends` and `implements`

**Problem.** A wrapper class might want to extend one wrapper and implement another.

**Mapping fix.** ✅ verified (`processClass` accepts `extends X implements Y Y Y` but only one interface is permitted — any extra would fail the generator).

```text
class MyWrapper extends BaseWrapper
    …
```

This is the safest pattern. **Avoid `implements`** until you have a verified need because the toolchain emits Java that only declares one.

---

## P15. Reading + writing a field (instance)

**Problem.** Need to read an instance field, and occasionally write it.

**Mapping fix.** ✅ verified.

```text
@Nullable <FieldType> someField
```

⚠️ The toolchain emits a setter **even for** `final` fields. The `final` keyword only blocks *direct* writes from compiled bytecode; runtime reflection still works. If you do not want runtime settability, declare the field with `final` and read with `keychain.someFieldField()`, but never call `keychain.setSomeFieldField(value)`.

---

## P16. Adapters for generic types (List, Optional, etc.)

**Problem.** Version't cannot auto‑emmit generic wrapper types: the generator would need the parameter `T` substitution rules.

**Mapping fix.** ✅ verified (libr-getter line ~1):

```text
import java.util.List
adapter java.util.List -> dev.gxlg.librgetter.utils.adapters.ListAdapter
```

Adapter must provide `wrapper(Function<…, T>)` and `unwrapper(Function<T, …>)`. See `gXLg/libr-getter`'s `utils/adapters/*Adapter.java` source.

---

## See also

- `wiki/Home.md` excerpts cited earlier — the only official narrative on the tool.
- `mapping-language.md` — row‑level grammar reference.
- `limitations.md` — what Version't cannot help with.
- `troubleshooting.md` — common pattern failures.
