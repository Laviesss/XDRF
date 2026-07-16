# Runtime API

The runtime API is delivered by the `versiont-library` artifact (Maven coords `dev.gxlg:versiont-library`) inside the package `dev.gxlg.versiont.api`. Generated wrappers reference these classes directly; user code may call them too.

This file lists **only** the Ver‑verified API surface I read in `R.java`/`V.java` and confirmed against generated wrappers.

---

## `dev.gxlg.versiont.api.R` — the reflection entry point

A single static class. Every wrapper method's body uses `R.…`; nothing else does the lookups.

### Class lookup

| Method | Returns | Notes |
|--------|---------|-------|
| `R.clz(String names)` | `R.RClass` | `names` is `slash‑separated`, tried left‑to‑right via `Class.forName`. **Lazily** cached and reused. ✅ verified in `R.java` constructor block. |
| `R.clz(Class<?> clz)` | `R.RClass` | Direct wrap of a compile‑time `Class` reference; e.g. `R.clz(KeyBinding.class)` if KeyBinding is on the user's compile classpath. ✅ verified. |

### Reflection handles

| Method | Returns | Notes |
|--------|---------|-------|
| `R.clz(...).inst(Object instance)` | `R.RInstance` | Binds to an instance so `.mthd(...)` and `.fld(...)` resolve *instance* members. |
| `R.clz(...).constr(Class<?>... paramTypes)` | `R.RConstructor` | Searches by *parameter types only* (ignores names); instance `constr` is fixed‑arity. |
| `R.clz(...).mthd(String names, Class<?>... types)` | `R.RMethod` | First‑slot type is the return; remaining types are parameter types. Names are `slash‑separated` fallback. ✅ |
| `R.clz(...).fld(String names, Class<?> fieldType)` | `R.RField` | Same fallback‑names semantics for the field's identifier. ✅ |

### Invocation

| Method | Returns | Notes |
|--------|---------|-------|
| `R.RConstructor.newInst(Object... args)` | `RInstance` | Constructs and binds an instance. Wraps in `RInstance` for further reflection. |
| `R.RMethod.invk(Object... args)` | `Object` | Returns whatever the underlying MC method returns. |
| `R.RField.get()` | `Object` | Read‑value. |
| `R.RField.set(Object value)` | `void` | Write‑value. |

Each handle performs caching by `(class, name, types)` so a second call returns immediately. ✅ verified by the `methodsCache`/`fieldsCache` static maps.

### Resolution policies

- **`R.methodMatches(Method, Class<?>... types)`** — first `Class<?>` is the expected return type; remaining are parameter types. Allows `Method`‑based matching in the `WrappedMethod` matcher. ✅ verified in `R.java`.
- **`R.fieldMatches(Field, Class<?>)`** — checks field's declared type is assignable from the supplied type. ✅
- **`R.findMethodBetween(...)`** — internal; not a public API.

### Wrapping helpers

| Method | Returns | Notes |
|--------|---------|-------|
| `R.wrapperInst(Class<T extends Wrapper<?>> wrapperClass, Object instance)` | `T` | Wraps an already‑constructed MC instance inside a wrapper. Handles subclass dispatch via `subClazzes`. ✅ |
| `R.unwrapWrapper(Wrapper<?>)` (and `R.unwrapWrapper(Wrapper<?>, Class<T>)`) | `Object` / `T` | Reverse of `wrapperInst`. ✅ |
| `R.interfaceInstance(...)` | proxy | Creates a JDK proxy implementing a wrapper interface. Used in default interface methods. ✅ |
| `R.extendWrapper(superClass, extendingWrapperCls, interfaces...)` | `RClass` | Allows **runtime class extension** — generate a `ByteBuddy` subclass of a hard‑to‑construct Minecraft class so a user's wrapper can extend it. ⚠️ Lightweight introspection only; performance‑sensitive; `R.preload(...)` lets you asynchronously resolve. ✅ verified by signature. |

### Array / null helpers

| Method | Notes |
|--------|-------|
| `R.arrayWrapper(Function<Object, T> wrapperT)` | Build an array‑wrapping function. |
| `R.arrayUnwrapper(Function<T, Object> unwrapT)` | Reserve. |
| `R.nullSafe(Function<T, R> function)` | Composable null guard. |

⚠️ Verified by signature; we didn't hit them in the XDRF mapping.

### Class‑identity helpers

| Method | Notes |
|--------|-------|
| `R.isUserClass(Class<?>)` | True if the class was added by `R.extendWrapper`. ✅ |
| `R.isActualUserClass(Class<?>)` | True after the corresponding `extendWrapper` proxy has resolved. ✅ |
| `R.preload(RClass... classes)` | Async resolve (used to hide first‑use latency in player join). ✅ |

---

## `dev.gxlg.versiont.api.R.RClass`

Inner class returned by `R.clz(...)`. Methods:

| Method | Notes |
|--------|-------|
| `.inst(Object instance)` | Bind to an already‑constructed instance. |
| `.constr(Class<?>... types)` | Resolve a *constructor* on this class. |
| `.mthd(String names, Class<?>... types)` | Resolve a *method* by name and parameter types. Returns `RMethod`. First `types` slot is the return type. |
| `.fld(String names, Class<?> type)` | Resolve a *field* by name and declared type. Returns `RField`. |
| `.arrayType()` | Next `RClass` for the array element type. |
| `.self()` | `Class<?>` — triggers lazy resolution and returns it. |

✅ All verified by signatures.

---

## `dev.gxlg.versiont.api.R.RInstance`

Inner class returned by `R.clz(...).inst(...)`. Methods:

| Method | Notes |
|--------|-------|
| `.fld(String names, Class<?> type)` | Field access through instance. |
| `.mthd(String names, Class<?>... types)` | Method invocation. |
| `.self()` | `Object` — returns the underlying Minecraft instance. |

---

## `dev.gxlg.versiont.api.R.RMethod`

| Method | Notes |
|--------|-------|
| `.invk(Object... args)` | Invoke. |
| `.self()` | Returns the cached `StoredMethod`. |

The `StoredMethod` returned is a `MethodHandle` wrapper that pre‑arranged the method's signature so invocation is fast.

---

## `dev.gxlg.versiont.api.R.RField`

| Method | Notes |
|--------|-------|
| `.get()` / `.set(Object)` | Against the underlying Java `Field` (or static if `inst` was null). |
| `.self()` | Returns the cached `StoredField`. |

---

## `dev.gxlg.versiont.api.R.RConstructor`

| Method | Notes |
|--------|-------|
| `.newInst(Object... args)` | Construct. Calls `Class.getDeclaredConstructor(types)` exactly once and caches it. |
| `.self()` | `MethodHandle`. |

---

## `dev.gxlg.versiont.api.V` — version helpers

Methods:

| Method | Returns | Notes |
|--------|---------|-------|
| `V.getVersion()` | `V.MinecraftVersion` | Lazy‑parsed from `FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion().getFriendlyString()`. ✅ |
| `V.higher(String other)` | `boolean` | ✅ |
| `V.equal(String other)` | `boolean` | ✅ |
| `V.lower(String other)` | `boolean` | ✅ |
| `V.isObfuscated()` | `boolean` | True on a non‑dev environment AND `lower("26.1")` == true. Used to decide whether intermediary names are valid at runtime. ✅ |

### SemVer extraction note (✅ verified)

- `V.MinecraftVersion(String version)` splits the version on `[^0-9.]` (any non‑digit, non‑dot) and uses the first 0‑2‑3 numeric slots. So `1.21.10`, `26.1`, and `26.1.1-pre3` are all parsed.

### Intended use pattern (verified)

```java
if (V.higher("1.21.10")) {
    // Behaviour available only on MC ≥ 1.21.11
}
```

This is the **only** runtime version check Version't supports. No `V.between(a, b)` helper exists.

---

## `dev.gxlg.versiont.api.types.Wrapper` — base class for every generated wrapper

The `Wrapper<SELF extends Wrapper>` abstract class:

| Member | Notes |
|--------|-------|
| `protected final Object instance` | Captured once via `delayedConstructor`. ✅ |
| `protected Wrapper(Wrapper.DelayedConstructor dc)` | Subclasses must be constructed via `R.wrapperInst(...)`; this constructor is what the user's `super(dc)` calls. ✅ |
| `Wrapper.DelayedConstructor` lambda | `Object construct(R.RClass actualClass)` returns and binds the underlying instance. ✅ |
| `public Object unwrap()` / `<T> T unwrap(Class<T>)` | Pulls back to the raw MC instance. |
| `equals(Object)` | Wrapper identity = identity of the wrapped instance. |
| `__preInitWrapper` | Static field used to plumb thread‑local `__wrapper` references into wrapper‑instance methods. ⚠️ |

✅ Every claim above is verified by reading `Wrapper.java`.

---

## `dev.gxlg.versiont.api.types.WrapperInterface`

| Method | Notes |
|--------|-------|
| `Object unwrap()` | All wrapper interfaces expose `unwrap()`. |
| `<S> S unwrap(Class<S>)` | Unwrap with cast. |

`WrapperInterface` is the parallel abstract class for generated *interface* wrappers, used so the same `R.wrapperInst(...)` and `R.interfaceInstance(...)` plumbing apply.

✅ Verified by `WrapperInterface.java`.

---

## `dev.gxlg.versiont.api.types.WrappedMethod`

| Field | Type | Source of value |
|-------|------|------------------|
| matcher | `Function<Method, Boolean>` | Used by `WrappedMethod.matches(Method)`. Compares method name against each slash‑split alias and signature against `R.methodMatches`. |
| invoker | `Invoker` | `Object invoke(Wrapper, Object[])` — calls the wrapper and returns the result. |

Used by every generated wrapper to enable bytecode‑level dispatch by clients like ByteBuddy‑dynloaded subclasses.

✅ Verified by `WrappedMethod.java`.

---

## `dev.gxlg.versiont.api.types.StoredMethod` / `StoredField`

Internal cache results for `MethodHandle`‑backed `Method`/`Field` invocations. Public lookups are through `R.RMethod` / `R.RField`. ✅ verified by file structure.

---

## `dev.gxlg.versiont.api.types.RedirectedCall`

Record `(boolean isRedirected, Object result)` for interface call dispatch. Internal to the interface method wrapping path. ✅

---

## Composition patterns

### Wrap an existing MC instance

```java
KeyBinding kb = R.wrapperInst(KeyBinding.class, existingKeyBindingInstance);
```

`R.wrapperInst` looks at `KeyBinding.subClazzes` to find a more specific subclass wrapper when present, else constructs a fresh wrapper. ✅ verified by reading `WrapperInst` resolution chain.

### Reach a class on a different MC major

```java
Class<?> rawMc = R.clz("net.minecraft.class_310/net.minecraft.client.MinecraftClient/net.minecraft.client.Minecraft")
                  .self();
```

First slash name (`class_310`) is the Fabric intermediary and works on obfuscated MC. Second name works on Yarn‑1.21 mapping. Third name works on `26.x`. The first one that resolves is used.

---

## See also

- `wrapper-generation.md` — what every wrapper actually calls into.
- `compatibility-patterns.md` — how these calls compose for a real Fabric‑mod Minecraft API.
- `troubleshooting.md` — common runtime reflection failures.
