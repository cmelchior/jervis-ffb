# Jervis — A Fantasy Football Engine

This repository contains the source code for the Jervis project.

Jervis is a collection of tools and libraries for creating and running a game of 
[Blood Bowl](https://start-warhammer.com/blood-bowl/), a board game owned by Games Workshop.

This includes:
 
- A standalone rules engine for the 2020 version of Blood Bowl. This engine 
  can be hooked into any UI framework or AI agents.

- A Game Client UI that can run either as a website, desktop client or iPad app.

- An AI Agent Framework that makes it possible to replace a player with a custom
  AI agent.

- A replay adapter that can convert any FUMBBL Replay into an equivalent Jervis
  game. This makes it possible to use already existing replays to train AI 
  agents.

The project is still very much work-in-progress. 

The project is primarily written in Kotlin (Multiplatform) targeting WASM and 
Desktop targets.


## Disclaimer

Blood Bowl is a trademark of Games Workshop Limited, used without permission, 
used without intent to infringe, or in opposition to their copyright. This 
project is in no way official and is not endorsed by Games Workshop Limited.


## Developer Releases

Test builds are created on every successful push to `main`.

* The HTML version can be found here: https://jervis.ilios.dk. It is built
  using WASM, so will only work on the latest browsers. Specifically, it does
  not work on Safari. Read here for more info: 
  https://kotlinlang.org/docs/wasm-troubleshooting.html#browser-versions

* Desktop client installers for Windows, MacOS and Linux can be found here:
  https://jervis.ilios.dk/download/.

* No test builds for iPad is currently being created. Instead these must be
  built from source.

No guarantees are given for them.


## How To Build

Development requirements are:
- Java 21
- Git on the commandline
- Xcode (if building for iPad)

Note, this has only been tested on Mac, so things on Windows might be broken.

A local desktop game client can be started using:

```shell
./gradlew :modules:jervis-ui:jvmRun -DmainClass=com.jervisffb.MainKt
```

A local WASM client can be started using:

```shell
./gradlew :modules:jervis-ui:wasmJsBrowserDevelopmentRun
```

The iPad version can be built and installed by using Xcode. Use the project file
found here `modules/iosApp/iosApp.xcodeproj`. You will need to supply your own 
signature under "Signing & Capabilities".


## Repository Structure

This repository is structured in the following way:

- `modules/`: The main entry point for all code. See the section below.
- `docs/`: Contains more fine-grained docs about various aspects of the project.
- `tools/`: Contains commandline tools used by the project.
- `Debug-FantasyFootballClient/`: Contains a modified FUMBBL Client that can
  be used to introspect FUMMBL network traffic. See 
  [the documentation](modules/fumbbl-cli/README.md) for more details.

### Modules Structure

The `modules/` subfolder is the main entry point for the project and consists 
of the following modules:

- `fumbbl-cli`: Small commandline tool for either downloading the FUMBBL Client 
  and modifying it, so all websocket traffic is sent to the console or 
  download a replay for further analysis. Note, the last functionality should only 
  be used sparingly and for testing as it taxes the server too much if used in 
  bulk. 

- `fumbbl-net`: Network code and classes for communicating with the FUMBBL 
   server as well as adapters for converting a FUMBBL game into a Jervis
   equivalent.

- `jervis-engine`: This contains a full model of the Blood Bowl rules and 
  contains the logic for running a game.

- `jervis-net`: This contains the infrastructure to create and communicate with
   a light-weight Game Server that is only used to play a single game.

- `jervis-ui`: An UI for driving a game of Blood Bowl. It has been largely 
  copied from the FUMBBL Client UI. 

- `replay-analyzer`: This module is intended for processing and analyzing the
   JSON content of a FUMBBL replay file.

- `platform-utils`: All helper methods that might require platform-specific
  APIs are placed here. E.g., Filesystem access, setting up networking and 
  reflection.

### UI Resources

The Jervis Client is heavily inspired by FUMBBL and borrows all (almost) of
its assets from there. All rights and credits go to their respective authors.

It is possible to sync the latest FUMBBL resource with the Jervis project
by using this gradle command:

```
./gradlew updateFFBResources
```

This task will clone the FFB repository, take the resource folder and 
flatten it so it is usable by Compose Multiplatform and then finally moving it
into place in the project so it is ready for immediate use.

Team Roster logos have been copied manually from https://fumbbl.com/p/createteam


## Why Jervis?

As a homage to the original creator of Blood Bowl: Jervis Johnson. 

Also, it sounds similar to J.A.R.V.I.S, the A.I. from the Marvel Universe, so it 
also a fun play on that pronunciation.


## Other resources

List of other Blood Bowl resources that inspired this project.

- [Fantasy Football (FUMBBL)](https://github.com/christerk/ffb)
- [FUMBBL Datasets](https://github.com/gsverhoeven/fumbbl_datasets)
- [BotBowl](https://njustesen.github.io/botbowl/)
