<h1>
   Jervis — A Fantasy Football Engine
   <img src="logo/logo.svg" width="40" align="left" alt="Jervis Logo">
</h1>

<p align="center">
     <a href="https://www.ilios.dk/jervis/screenshot1.png"><img src="https://www.ilios.dk/jervis/screenshot1_preview.png" width="30%" /></a>
     <a href="https://www.ilios.dk/jervis/screenshot2.png"><img src="https://www.ilios.dk/jervis/screenshot2_preview.png" width="30%" /></a>
     <a href="https://www.ilios.dk/jervis/screenshot3.png"><img src="https://www.ilios.dk/jervis/screenshot3_preview.png" width="30%" /></a>
</p>

This repository contains the source code for the Jervis project.

Jervis is a collection of tools and libraries for creating and running a game of 
[Blood Bowl](https://start-warhammer.com/blood-bowl/), a board game owned by Games 
Workshop.

This includes:
 
- A standalone rules engine for the 2025 version of Blood Bowl. The engine 
  is UI-agnostic and can be used with any UI framework.

- A Game Client UI that can run either as a website, iPad, or Desktop app.

- An API that is AI-friendly, making it possible to replace a player with a 
  custom AI agent or use in AI training.

- A replay adapter that can convert any FUMBBL replay into an equivalent Jervis
  game. This makes it possible to use already existing replays to train AI 
  agents (WIP).

The project is written in Kotlin Multiplatform primarily focusing on Web (using 
WASM) and Desktop (using a packaged JVM). The target is to have a faithful 
adaption of  the core Blood Bowl rules as well as any expansions.

Public documentation can be found here: https://cmelchior.github.io/jervis-ffb/

**The project is a work-in-progress, so things might differ from what is 
described here.**


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

* Desktop client installers for Windows, macOS, and Linux can be found here:
  https://cmelchior.github.io/jervis-ffb/download.html. They only need to be 
  installed once and can then update themselves whenever there is a new release.
  This is done using [Conveyor](https://www.hydraulic.dev/).

  Installers for a single version (without auto-update) can be found 
  here: https://jervis.ilios.dk/download/. 

* No test build for iPad is currently being created. Instead, this must be
  built from source. 

No stability guarantees are given for developer releases.

## Building the project
See [CONTRIBUTING.md](./CONTRIBUTING.md) for details on how to build, test and
run this project.

## Repository Structure
This repository is structured in the following way:

- `modules/`: The main entry point for all code. See the section below.
- `docs/`: Contains more fine-grained docs about the progress on various aspects
  of the project.
- `tools/`: A JVM Gradle module containing helper tools and, during the gradual
  migration, the project's Kotlin Notebooks.
- `mavenRepo/`: contains some packages that are not available on Maven Central.
  Artifacts here are used to build parts of the project.
- `logo/`: Contains project logos used by various platforms.
- `Debug-FantasyFootballClient/`: It is empty by default, but when using
  [`fumbble-cli`](./fumbbl-cli) it can contain a modified version of the FUMBBL
  Client that can be used to inspect the FUMMBL network traffic. See
  [the documentation](modules/fumbbl-cli/README.md) for more details. Note, this has not been tested for 
  a while, and since the FFB Client is now open source, it is also less 
  relevant, as you can just run the real client instead. See
  [How to run FFB locally](./docs/working-with-ffb.md).
- `website`: Contains the source code for the public non-coder website. It is 
  built using [Zensical](https://zensical.org/).


### Modules Structure
The `modules/` subfolder is the main entry point for the project and consists 
of the following modules:

- `fumbbl-cli`: Small commandline tool for downloading the FUMBBL Client 
  and modifying it, so all websocket traffic is sent to the console or 
  download a replay for further analysis. Note, the last functionality should 
  only be used sparingly as it taxes the FUMBBL server too much if used in bulk. 

- `fumbbl-net`: Network code and classes for communicating with the FUMBBL 
   server as well as adapters for converting a FUMBBL game into a Jervis
   equivalent.

- `fuzzer-cli`: Commandline tool for fuzz-testing the rules engine by running
   a large number of randomized games against different rulesets.

- `tourplay-net`: Wrapper for the TourPlay REST API that can download TourPlay
   teams and make them available to Jervis.

- `jervis-engine`: The full Blood Bowl game and rules model as well as logic
  for running the game. It contains submodules split by responsibility: 
  - `core`: Infrastructure for for setting up and running games. The module is
    rules agnostic.
  - `rules-common`: Contains all rules classes and logic that is shared 
    among all supported rulesets.
  - `rules-bb2020`: Contains all BB2020 specific rules and classes. It also
    contains all variants that are based on BB2020, like Sevens.
  - `rules-bb2025`: Similar to `rules-bb2020`, but contains all the logic and
    classes for BB2025.
  - `package`: This module is what downstream modules should use. It is
    responsible for combing the `core` module with the set of currently 
    supported rules. It also contains the code needed to serialize and 
    deserialize game, team and roster files.

- `jervis-net`: The infrastructure to create and communicate with a light-weight 
  Game Server that is only used to play a single game.

- `jervis-test-utils`: Contains test setups used across multiple modules.
  This is a work-around for https://youtrack.jetbrains.com/issue/KT-35073.

- `jervis-ui`: An UI for driving a game of Blood Bowl. It has in large parts
   been inspired by the FUMBBL Client UI. It contains submodules for each
   platform supported: `desktopApp`, `iosApp`, and `webApp`. `shared` contains
   the UI implementation shared between all platforms, which is most of it.

- `platform-utils`: All helper methods that require platform-specific
  APIs are placed here. This includes things like filesystem access, setting up 
  networking, and reflection.

- `replay-analyzer`: A helper for processing and converting the JSON content of 
   a FUMBBL replay file into something that Jervis can process.

- `tools`: Contains helper tools used during development of the project.
 
### UI Resources
The Jervis Client is heavily inspired by [FUMBBL](https://github.com/christerk/ffb)
and borrows many of its assets from there. They are used with permission. All 
rights and credits go to [their respective authors](https://fumbbl.com/p/attribution), and 
they cannot be re-distributed without permission. 

If you are the author of any icons or other resources and don't want them used 
in this project, please contact christianmelchior at gmail dot com, and they 
will be removed immediately.


## Why Jervis?
As homage to the original creator of Blood Bowl: [Jervis Johnson](https://en.wikipedia.org/wiki/Jervis_Johnson). 


## Other resources
List of other Blood Bowl resources that inspired this project.

- [Fantasy Football (FUMBBL)](https://github.com/christerk/ffb)
- [FUMBBL Datasets](https://github.com/gsverhoeven/fumbbl_datasets)
- [BotBowl](https://njustesen.github.io/botbowl/)
- [Blood Bowl Challenges](https://bbtactics.com/strategy/articles/challenges/)
