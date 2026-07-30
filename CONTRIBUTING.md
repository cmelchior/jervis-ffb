# Contributing to Jervis FFB
This document covers how to build, test, and run the various parts of the Jervis 
project.

For a more in-depth technical and architectural background, see the documents
under [`docs/`](docs/). 

## Requirements
Development requirements are:
- Java 21
- Git on the commandline
- Xcode 16.2 (if building for iPad)
- The project has been developed in IntelliJ IDEA, while optional, it will
  provide a better experience.

Note: this has only been tested on Mac and Windows. Linux has not been 
tested thoroughly, but _should_ work.

Some code for handling game settings is generated as part of the build process,
so unresolved references might be present when opening this project for the
first time. This should be fixed after the first build.

## How To Build

### Building Game Clients
A local desktop game client can be started using:

```shell
./gradlew :modules:jervis-ui:desktopApp:run
```

A local WASM client can be started using:

```shell
./gradlew :modules:jervis-ui:webApp:wasmJsBrowserDevelopmentRun
```

A local iPad client can be started from IntelliJ using the `iosApp` run 
configuration. This requires a new version of IntelliJ and the `Kotlin 
Multiplatform` and `Compose Multiplatform` plugins.

If this run configuration is not available, the iPad version can also be built 
and installed using Xcode. Use the project file found here
[`modules/jervis-ui/iosApp/iosApp.xcodeproj`](modules/jervis-ui/iosApp/iosApp.xcodeproj).
You might need to supply your own signature under "Signing & Capabilities".

### Building Documentation Website
The documentation website is built by installing Zensical:

```shell
pip install zensical
```

And then running the following command from the root of the repository:
```shell
# Website is visible on localhost:8080
zensical serve
```

## Running tests

While multiplatform supports running tests, for practical reasons we mostly only
run unit tests on the JVM target as this is considered the primary target. This
might change in the future.

Run all tests across all modules using 

```shell
./gradlew jvmTest
```

Useful variants when iterating:

- Single module: 
  ```
  ./gradlew :modules:jervis-engine:jvmTest
  ```
- Single test class:
  ```
  ./gradlew :modules:jervis-engine:jvmTest --tests "com.jervisffb.test.bb2025.GameProgressTests"
  ```
  
### Fuzz Tester
For non-trivial changes to the rule engine, also consider running the fuzz
tester. It lives in the `:modules:fuzzer-cli` module and runs random games
against the engine to force crashes or inconsistent state. See
[`modules/fuzzer-cli/README.md`](modules/fuzzer-cli/README.md) for the full
usage reference.

The preferred workflow is to build the packaged jar once and invoke it via
the helper script at the project root:

```shell
# Build the Fuzzer CLI
./gradlew buildTools
# See all options
./fuzzer-cli --help
# Run the fuzzer against the standard BB2025 ruleset
./fuzzer-cli bb2025
# Configure the fuzzer
./fuzzer-cli --games 100000 --batch-size 5000 --threads 8 bb2025
# Run a single game using a pre-defined seed
./fuzzer-cli --games 1 --seed "12345" bb2025
```

For quick iteration during development, you can also invoke it through Gradle. 
This skips the packaging step but takes longer per invocation:

```shell
./gradlew :modules:fuzzer-cli:run --args="<fuzzerConfigurationName>"
```

## Formatting code
The project uses [ktlint](https://github.com/pinterest/ktlint) via the Gradle
plugin. `ktlintCheck` runs as part of `./gradlew check` and CI will fail on
style violations.

Run the formatter before opening a PR:

```shell
./gradlew ktlintFormat
```

To only verify without modifying files:

```shell
./gradlew ktlintCheck
```

## Compose Compatibility

To improve the performance of Compose, types must be marked "Stable" where 
possible. In modules, like `jervis-ui`, this is done automatically by applying 
the Compose Compiler Plugin.

`jervis-engine` does not apply this by design, so there we need to manually list
these types. This is done in `modules/jervis-engine/compose-stability.conf`.

When adding a new interface or sealed hierarchy to `jervis-engine`, check if 
it can be added to this file. More information is available there.

The status of stability can be checked by running this command:

```shell
./gradlew :modules:jervis-ui:shared:compileKotlinJvm -PcomposeReports=true --rerun
```

The reports are found in `modules/jervis-ui/shared/build/compose_compiler/`
See `com_jervisffb:shared-composables.txt` and check for `unstable` properties
or classes

## Updating Gradle Wrapper
The project uses Gradle to manage dependencies and the build processes. To
update the Gradle Wrapper to the latest version, run the following command:

```shell
./gradlew wrapper --gradle-version latest --distribution-type all
```

## Updating UI resources
The Jervis client borrows a large portion of its icons and sounds from the
[FUMBBL client](https://github.com/christerk/ffb) (used with permission — see
[FUMBBL attribution](https://fumbbl.com/p/attribution)). These assets live
under [`modules/jervis-ui/shared/src/commonMain/composeResources`](modules/jervis-ui/shared/src/commonMain/composeResources)
and can be re-synced with upstream FUMBBL using:

```shell
./gradlew :modules:jervis-ui:shared:updateFFBResources
```

The task clones the FFB repository into `build/ffb-repo`, flattens its resource
directory into a layout that Compose Multiplatform can consume, and copies the
result into the project. It also refreshes `icons.ini`, which maps local paths
to their remote FUMBBL URLs. Player icons and portraits are intentionally *not*
copied — their licensing status is unclear, so they are loaded and cached at
runtime instead. See [CMP-4196](https://youtrack.jetbrains.com/issue/CMP-4196)
for a background on why the flattening step is necessary.

The `icons.ini` file can be updated manually by using this command from 
the root of the project:

```shell
curl -fL https://raw.githubusercontent.com/christerk/ffb/refs/heads/master/ffb-client/src/main/resources-live/icons.ini -o modules/jervis-ui/shared/src/commonMain/composeResources/files/fumbbl/icons.ini
```

copying the contents of
[`icons.ini`]([modules/jervis-ui/shared/src/commonMain/composeResources/files/fumbbl/icons.ini](https://github.com/christerk/ffb/blob/4d834d6a31e87d9b78997af8017aad46c9ec536c/ffb-client/src/main/resources-live/icons.ini)
into [`icons-extra.ini`](modules/jervis-ui/shared/src/commonMain/composeResources/files/fumbbl/icons-extra.ini)
using the same `url=local/path` format.

Extra icons that are not defined in FUMBBL's `icons.ini` but should still be
loaded from the network can be added to[`icons-extra.ini`](modules/jervis-ui/shared/src/commonMain/composeResources/files/fumbbl/icons-extra.ini)
using the same `url=local/path` format.

## AI Agents
AI Agents have been used during the development of this repository, and 
contributions created using these are fine as well, with one warning: It is 
expected that any PR has actually been reviewed by a human. "AI-slop" will 
be closed without warning.

A [AGENTS.md](./AGENTS.md) file is included in this repository, which provides
additional guidance on how to use AI Agents effectively, but beware that this
file is very much a work-in-progress and currently provides little guidance.

Some notes when using these:
- AI Agents do not fully grasp the often conflicting rules and will make 
  errors when implementing these. Do not expect them to one-shot any Procedures
  and associated tests. You will end up overspecifying things to a degree where
  it is faster to implement them manually.
- AI Agents are fairly capable when working with Compose, and with some 
  guidance, they can speed up building UI elements. But beware of the difference
  between Jetpack Compose and Compose Multiplatform. There are subtle 
  differences, and AI will make mistakes here, especially on more niche 
  framework features.

## PR checklist
Before creating a PR against Jervis, run through the following steps:

1. `./gradlew ktlintFormat`
2. `./gradlew jvmTest`
3. If you touched both `engine/rules/bb2020/` and `engine/rules/bb2025/`, verify
   the change is mirrored consistently across both rulesets.
4. All changes that add to or change the rules implementation should have 
   associated unit tests in `modules/jervis-engine/src/commonTest/` 
5. Do not commit generated files under `build/` or resource copies produced by
   `updateFFBResources` unless updating those resources is the point of the PR.
