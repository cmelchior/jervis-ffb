<h1>
   Jervis — A Fantasy Football Engine
   <img src="logo/logo.svg" width="40" align="left" alt="Jervis Logo">
</h1>

<p align="center">
  <img src="screenshots/screenshot1.png" width="30%" />
  <img src="screenshots/screenshot2.png" width="30%" />
  <img src="screenshots/screenshot5.png" width="30%" />
</p>

This repository contains the source code for the Jervis project.

Jervis is a collection of tools and libraries for creating and running a game of 
[Blood Bowl](https://start-warhammer.com/blood-bowl/), a board game owned by Games 
Workshop.

This includes:
 
- A standalone rules engine for the 2020 version of Blood Bowl. The engine 
  is UI agnostic and can be hooked up into any UI Framework or AI Agent.

- A Game Client UI that can run either as a website, iPad or Desktop app.

- An API that is AI friendly, making it possible to replace a player with a 
  custom AI agent.

- A replay adapter that can convert any FUMBBL replay into an equivalent Jervis
  game. This makes it possible to use already existing replays to train AI 
  agents.

The project is written in Kotlin Multiplatform primarily focusing on WASM (Web) 
and Desktop (JVM). The target is to have a faithful adaption of the core Blood 
Bowl rules as well as any expansions.

**The project is work-in-progress, so things might differ from what is described here.**


## Disclaimer

Blood Bowl is a trademark of Games Workshop Limited, used without permission, 
used without intent to infringe, or in opposition to their copyright. This 
project is in no way official and is not endorsed by Games Workshop Limited.


## Developer Releases

Test builds are created on every successful push to the `main` branch.

* The HTML version can be found here: https://jervis.ilios.dk. It is built
  using WASM, so will only work on the latest browsers. Specifically, it only
  works on the latest versions of Safari. Read here for more info: 
  https://kotlinlang.org/docs/wasm-troubleshooting.html#browser-versions

* Desktop client installers for Windows, macOS and Linux can be found here:
  https://jervis.ilios.dk/download/.

* No test build for iPad is currently being created. Instead, this must be
  built from source. 

No stability guarantees are given for developer releases.


## How To Build

Development requirements are:
- Java 21
- Git on the commandline
- Xcode 16.2 (if building for iPad)

Note, this has only been tested on Mac and Windows, so things on Linux might be broken.

A local desktop game client can be started using:

```shell
./gradlew :modules:jervis-ui:run
```

A local WASM client can be started using:

```shell
./gradlew :modules:jervis-ui:wasmJsBrowserDevelopmentRun
```

The iPad version can be built and installed using Xcode. Use the project file
found here [`modules/iosApp/iosApp.xcodeproj`](modules/iosApp/iosApp.xcodeproj). 
You will need to supply your own signature under "Signing & Capabilities".


## Repository Structure

This repository is structured in the following way:

- `modules/`: The main entry point for all code. See the section below.
- `docs/`: Contains more fine-grained docs about the progress on various aspects
  of the project.
- `tools/`: Contains helper tools for the project that are either commandline or
  Kotlin Notebooks.
- `mavenRepo/`: contains some packages that are not available on Maven Central.
  Artifacts here are used to build parts of the project.
- `screenshots/`: Contains screenshots from the game UI.
- `logo/`: Contains project logos used by various platforms.
- `Debug-FantasyFootballClient/`: It is empty as a default, but when using
  [`fumbble-cli`](./fumbbl-cli) it can contain a modified version of the FUMBBL
  Client that can be used to inspect the FUMMBL network traffic. See
  [the documentation](modules/fumbbl-cli/README.md) for more details. Note, this
  has not been tested for a while and since the FFB Client is now open source,
  it is also less relevant, as you can just run the real client instead. See
  [How to run FFB locally](./docs/working-with-ffb.md).

### Modules Structure

The `modules/` subfolder is the main entry point for the project and consists 
of the following modules:

- `fumbbl-cli`: Small commandline tool for downloading the FUMBBL Client 
  and modifying it, so all websocket traffic is sent to the console or 
  download a replay for further analysis. Note, the last functionality should only 
  be used sparingly as it taxes the FUMBBL server too much if used in bulk. 

- `fumbbl-net`: Network code and classes for communicating with the FUMBBL 
   server as well as adapters for converting a FUMBBL game into a Jervis
   equivalent.

 - `iosApp`: The XCode project needed to build the Jervis iPad
   app. This needs to be opened and run from XCode. It will automatically
   build all required dependencies from the project.

- `jervis-engine`: The full Blood Bowl game and rules model as well as logic
  for running the game.

- `jervis-net`: The infrastructure to create and communicate with a light-weight 
  Game Server that is only used to play a single game.

- `jervis-test-utils`: Contains test setups used across multiple modules.
  This is a work-around for https://youtrack.jetbrains.com/issue/KT-35073.

- `jervis-ui`: An UI for driving a game of Blood Bowl. It has in large parts
   been inspired by the FUMBBL Client UI.

- `platform-utils`: All helper methods that require platform-specific
  APIs are placed here. This includes things like filesystem access, setting up 
  networking and reflection.

- `replay-analyzer`: A helper for processing and converting the JSON content of 
   a FUMBBL replay file into something that Jervis can process.

### UI Resources

The Jervis Client is heavily inspired by [FUMBBL](https://github.com/christerk/ffb)
and borrows many of its assets from there. They are used with permission. All rights 
and credits go to [their respective authors](https://fumbbl.com/p/attribution), and 
they cannot be re-distributed without permission. 

If you are the author of any icons or other resources and don't want them used in this
project, please contact christianmelchior at gmail dot com, and they will be removed
immediately.


## Why Jervis?

As homage to the original creator of Blood Bowl: Jervis Johnson. 

Also, it sounds similar to J.A.R.V.I.S, the A.I. from the Marvel Universe, so it 
also a fun play on that pronunciation.


## Other resources

List of other Blood Bowl resources that inspired this project.

- [Fantasy Football (FUMBBL)](https://github.com/christerk/ffb)
- [FUMBBL Datasets](https://github.com/gsverhoeven/fumbbl_datasets)
- [BotBowl](https://njustesen.github.io/botbowl/)
