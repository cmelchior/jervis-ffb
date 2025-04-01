# How to create Distribution Packages

This document contains information related to creating and maintaining releases for the various platforms.

## GitHub Actions

GitHub Actions are set up to build and upload installers for test versions across all targets. These are created
for every push to the `main` branch

Desktop installers are found here: https://jervis.ilios.dk/download/
WASM test build is found here: https://jervis.ilios.dk

Installers can also be built locally, but will require the appropriate platform as well as a number of environment
variables being set:

```
export JERVIS_WINDOWS_PACKAGE_GUID="<GUID>"

export JERVIS_MACOS_SIGNING_ID="<ID>"
export JERVIS_MACOS_KEYCHAIN="<PathToKeyChainIfNeeded>" 
export JERVIS_MACOS_NOTARIZATION_APPLE_ID="<APPLE_ID>"
export JERVIS_MACOS_NOTARIZATION_PASSWORD="<APPLE_PASSWORD>"
export JERVIS_MACOS_NOTARIZATION_TEAM_ID="<TEAM_ID>"
```

## MacOS 

Follow https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Signing_and_notarization_on_macOS/README.md
to set up keys correctly.

Iconsets (.icns) are created from [logo.svg](../logo.svg) using [Image2Icon](https://apps.apple.com/us/app/image2icon-make-your-icons/id992115977?mt=12&ls=1).

Build release package using:
```
./gradlew notarizeDmg
```

We are not allowed to release anything below 1.0.0, so this will be the minimum release
number. The installer is found at:
```
modules/jervis-ui/build/compose/binaries/main/dmg/Jervis Fantasy Football-X.Y.Z.dmg
```

## Windows

Iconsets (.ico) are created from [logo.svg](../logo.svg) using [Image2Icon](https://apps.apple.com/us/app/image2icon-make-your-icons/id992115977?mt=12&ls=1).

Requires: WiX Toolset 3.11+ (64-bit)
Download: https://wixtoolset.org/releases/

Build package using:
```
gradlew packageReleaseDistributionForCurrentOS
```

The installer is found at:
```
modules\jervis-ui\build\compose\binaries\main-release\msi\Jervis Fantasy Football-X.Y.Z.msi
```

## WASM

`index.html` and associated resources are located in [modules/jervis-ui/src/wasmJsMain/resources](../modules/jervis-ui/src/wasmJsMain/resources).

[`favicon.svg`](../modules/jervis-ui/src/wasmJsMain/resources/favicon.svg) is [logo.svg](../icons/favicon.svg) that 
has been copied.