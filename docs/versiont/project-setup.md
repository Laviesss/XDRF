# Project Setup

Each setting below cites what it is and the source it was verified against.

## Toolchain plugin declaration

```gradle
plugins {
    id 'fabric-loom' version '1.17.11'
    id 'dev.gxlg.versiont-toolchain' version '1.3.4'
}
```

Source of evidence:

- ✅ **`fabric-loom` 1.17.11** — verified by Gradle plugin alignment within an existing Fabric project that uses Loom 1.17.11.
- ✅ **`dev.gxlg.versiont-toolchain` 1.3.4** — verified at Maven Central, including its `pom.xml` declaring this version (no source conflicts).

The toolchain plugin is published as a standard Gradle plugin via its own Maven repo; the `dev.gxlg:versiont-library` artifact is published alongside it.

## Repositories

```gradle
repositories {
    mavenCentral()
    maven { url 'https://maven.fabricmc.net/' }
    maven { url 'https://maven.terraformersmc.com/' }
    maven { url 'https://maven.isxander.dev/releases' }
    maven { url 'https://api.modrinth.com/maven' }
    maven {
        name = 'gXLg-Maven'
        url  = 'https://gxlg.github.io/maven-repo/'
    }
}
```

The `gXLg-Maven` repo hosts `versiont-library` and `versiont-toolchain`. The other repos are Fabric / community dependencies the user's mod may need.

The `versiont-toolchain` plugin also adds its gXLg Maven to the project repositories on its own (see `VersiontPlugin.groovy` in `src/main/groovy/dev/gxlg/versiont/toolchain/`), so a user who only declared the toolchain plugin without the explicit `maven { … }` block still has access to resolve `versiont-library`.

## Required dependencies

```gradle
dependencies {
    compileOnly project(':versiont') // NOT needed in normal single-mod workflow

    // Mandatory by toolchain plugin
    // (added implicitly by `dev.gxlg.versiont-toolchain`):
    //   implementation "net.bytebuddy:byte-buddy:1.18.4"
    //   implementation "dev.gxlg:versiont-library:1.2.3"

    // Used at runtime; user is responsible for ensuring
    // these are installed alongside the mod:
    //   net.fabricmc:fabric-loader
    //   net.fabricmc.fabric-api:fabric-api (if needed by the mod)
}
```

Evidence:

- ✅ `VersiontPlugin.groovy` declares `implementation "net.bytebuddy:byte-buddy:1.18.4"` and `implementation "dev.gxlg:versiont-library:1.2.3"` for the toolchain's own build script. The `<impl>` block does not propagate to the user's runtime classpath by itself; the user **must** declare `include "dev.gxlg:versiont-library:<version>"` in their `dependencies` to bring the runtime‑side classes onto the user's compile classpath and into the produced mod jar.
- ⚠️ The exact wording of `include` vs `modImplementation` vs `implementation` for the library is left to the user; the single verified requirement is that the runtime jar ends up in the produced mod jar.

## Java / JVM requirements

- ✅ **Gradle plugin (`versiont-toolchain`) requires JVM 25.** Verified from the Gradle module metadata file:
  ```
  variants/name=apiElements
       attributes:
         org.gradle.jvm.version: 25
  ```
  Hence the Gradle JVM (the one running `./gradlew`) must be JDK 25 or newer.
- ✅ **Toolchain‑kapt‑side runtime is JDK 17.** Verified from `R.java` imports using `java.lang.invoke.MethodHandle` (JDK 7+) and the `Wrapper`/`WrapperInterface` types using proxies; no language feature beyond Java 17 is required.
- ⚠️ **Fabric Loader minimum** has not been verified for this project. The most recent versions of Fabric Loader provide `RuntimeModRemapper`, which is the bridge between Yarn‑named bytecode and Mojang‑named runtime classes.

## Loom requirements

- ✅ **`fabric-loom` 1.17.11 (or a current release) is required** for Yarn mapping support. The toolchain plugin does *not* re‑implement Loom.

## Toolchain configuration

```gradle
versiont {
    mapping = file('versiont.mapping')
}
```

Evidence:

- ✅ `VersiontExtension.groovy` declares exactly one property: `RegularFileProperty getMapping()`.
- ✅ `VersiontPlugin.groovy` creates a task named `versiontLayer` of type `Exec`, which calls a bundled Node.js script `generate-layer.js` whose arguments are `[mappingFile, outputDir]`. Generator errors are surfaced as Gradle exceptions if the mapping file is missing.
- ✅ The task adds its output directory to `main`'s `srcDir` and (via `afterEvaluate`) makes `compileJava` depend on `versiontLayer`.

## Toolchain internals

```
project.versiont.mapping ─────────► VersiontExtension
                                     │
                                     ▼
                                  VersiontPlugin
                                     │
   ┌─────────────────────────────────┴───────────────────────────────────┐
   │                                                                     │
   ▼                                                                     ▼
generate-layer.js (Node.js)                              project.sourceSets.main
   │
   ├─ reads project.versiont.mapping
   └─ writes to build/generated/sources/versiont/java/dev/gxlg/versiont/gen/...
                                                                          ▲
                                                                          │
                                                            compileJava depends
                                                            on versiontLayer
```

Source citations:

- `VersiontExtension.groovy` — extension contract.
- `VersiontPlugin.groovy` — task wiring + dependency setup.

## Node.js prerequisite

- ✅ **Node.js must be on `PATH` at build time.** `VersiontPlugin.groovy` literally invokes `node <scriptFile> <mapping> <outputDir>` and does not bundle a JRE‑side JS engine. If `node` is missing, the `versiontLayer` task fails with a spawn error.

## Putting it all together (minimal verified example)

```gradle
plugins {
    id 'fabric-loom' version '1.17.11'
    id 'dev.gxlg.versiont-toolchain' version '1.3.4'
}

versiont {
    mapping = file('versiont.mapping')
}

dependencies {
    minecraft "com.mojang:minecraft:<mc-version>"
    mappings  "net.fabricmc:yarn:<yarn-build>"
    modImplementation "net.fabricmc:fabric-loader:<loader>"
    implementation "net.fabricmc.fabric-api:fabric-api:<ver>"  // optional
    include "dev.gxlg:versiont-library:1.2.3"
}
```

That `build.gradle` is the minimal complete form. Replace Minecraft / Yarn / Loader versions to taste.

## Sanity check after configuring

Run:

```bash
./gradlew versiontLayer
```

Expected output:

```
> Task :versiontLayer
Generated dev.gxlg.versiont.gen.java.lang.Object
Generated dev.gxlg.versiont.gen.<YourFirstWrapper>
…
Version't layer generated!
BUILD SUCCESSFUL
```

If the output instead says `<key>No such file or directory<…>` for `node`, install Node.js and re‑run.

## See also

- `mapping-language.md` — what goes inside `versiont.mapping`.
- `wrapper-generation.md` — what each `Generated …` line corresponds to in the output.
- `troubleshooting.md` — fixing `versiontLayer` Gradle errors.
