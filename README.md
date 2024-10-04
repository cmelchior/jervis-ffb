# Jervis — A Fantasy Football Engine

This repository contains the source code for the Jervis project.

Jervis is a collection of tools and libraries for creating and running a game of 
[Blood Bowl](https://start-warhammer.com/blood-bowl/), a board game created by 
Games Workshop.

This includes:
 
- A standalone, UI agnostic rules engine for the 2020 version of Blood Bowl.
  This engine can be hooked into any UI framework or AI agents.

- A Game Client UI that can run either as a website or a desktop client.

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
used without intent to infringe, or in opposition to their copyright. This site
is in no way official and is not endorsed by Games Workshop Limited.


## Requirements

Development requirements are:
- Java 21

Note, this has only been tested on Mac, so things on Windows might be broken.

## How to Use

A local desktop game client can be started using:

```shell
./gradlew :modules:jervis-ui:jvmRun -DmainClass=dk.ilios.jervis.MainKt
```

A local WASM client can be started using:

```shell
./gradlew :modules:jervis-ui:wasmJsBrowserDevelopmentRun
```


## Repository Structure

This repository is structured in the following way:

- `moduls/`: The main entry point for all code. See the section below
- `docs/`: Contains more fine-grained docs about various aspects of the project
- `tools/`: Contains commandline tools used by the project
- `Debug-FantasyFootballClient/`: Contains the modified FUMBBL Client that can
  be used to introspect FUMMBL network traffic. See 
  [the documentation](modules/fumbbl-cli/README.md) for more details.

### Modules Structure

The `modules/` sub folder is the main entry point for the project and consists 
of the following modules:

- `fumbbl-cli`: Small commandline tool for either downloading the FUMBBL Client 
  and modifying it so all websocket traffic is outputted to the console or 
  download a replay for further analysis. Note, the last functionality should only 
  be used sparingly and for testing as it taxes too much if used in bulk. 

- `fumbbl-net`: Network code and classes for communicating with the FUMBBL 
   server.

- `jervis-model`: This contains a full model of a Blood Bowl game and can be 
  used to model both the game state and rules. 

- `jervis-ui`: An UI for driving a game of Blood Bowl. It has been largely 
  copied from the FUMBBL Client UI.

- `replay-analyzer`: This module is intended for processing and analyzing the
   JSON content of a FUMBBL replay file.


## Why Jervis?

As a homage to the original creator of Blood Bowl: Jervis Johnson. Who would be 
better at playing Blood Bowl, than the man who invented it.

Also, it sounds similar to J.A.R.V.I.S, the A.I. from the Marvel Universe, so it 
also a funny play of words.


## Other resources

List of other Blood Bowl resources that inspired this project.

- [Fantasy Football (FUMBBL)](https://github.com/christerk/ffb)
- [FUMBBL Datasets](https://github.com/gsverhoeven/fumbbl_datasets)
- [BotBowl](https://njustesen.github.io/botbowl/)