# How to create Distribution Packages

This document contains information related to creating and maintaining releases for the various platforms


## MacOS 

Follow https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Signing_and_notarization_on_macOS/README.md
to setup keys correctly. This is done on Github Actions. Signed releases from there will happen automatically.

Iconsets (.icns) are created from [logo.svg](../logo.svg) using [Image2Icon](https://apps.apple.com/us/app/image2icon-make-your-icons/id992115977?mt=12&ls=1).

Build release package using:
```
./gradlew notarizeDmg
```

The installer is found at. We are not allowed to release anything below 1.0.0, so this will be the minimum release
number.

```
modules/jervis-ui/build/compose/binaries/main/dmg/Jervis Fantasy Football-X.Y.Z.dmg
```

## Windows

Iconsets (.ico) are created from [logo.svg](../logo.svg) using [Image2Icon](https://apps.apple.com/us/app/image2icon-make-your-icons/id992115977?mt=12&ls=1).
