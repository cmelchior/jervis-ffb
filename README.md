# Jervis Blood Bowl AI

This repository contains the building blocks for creating an AI or Bot that 
can interact with [FUMBBL](https://fumbbl.com/p/news) and [BotBowl](https://njustesen.github.io/botbowl/).

It consists of the following modules:

- `fumbbl-cli`: Small commandline tool for either downloading the FUMBBL Client and modifying it so all
  websocket traffic is outputted to the console or download a replay for futher analysis. Note, the last
  functionality should only be used sparingly and for testing as it taxes too much if used in bulk. 

- `fumbbl-net`: Network code and classes for communicating with the FUMBBL server.

- `game-model`: This is full model of a Blood Bowl game and can be used to model both the game state
  and rules. This module is not supposed to be used stand-alone but is building block for the other 
  modules.

- `game-ui`: An UI for driving a game of Blood Bowl. It has been largely inspired and copied from the
  FUMBBL Client UI.

- `replay-analyzer`: This module can process a full replay file from FUMBBL and analyze the game
  as well as mirroring the game state in the internal `game-model`.

## Tips for using the FUMBBL API

- See match details: https://fumbbl.com/p/match?id=<match_id>

- REST API: An OAuth id can be created at https://fumbbl.com/p/oauth. 
  It requires you to login at FUMBBL first. With The credentials
  there, you can call the API described in https://fumbbl.com/apidoc/

## Other resources

- https://github.com/gsverhoeven/fumbbl_datasets
- 