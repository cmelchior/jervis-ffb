# FUMBBL AI

This repository contains the building blocks for creating an AI or Bot that 
can interact with [FUMBBL](https://fumbbl.com/p/news) and [BotBowl](https://njustesen.github.io/botbowl/).

It consists of the following modules:

- `game-downloader`: Can download a single game file from FUMBBL. Should only be used sparingly and
  for testing. Written in Kotlin (JVM).

- `game-model`: This is full model of a Blood Bowl game and can be used to model both the game state
  and rules. This module is not supposed to be used stand-alone but is building block for the other 
  modules.

- `replay-analyzer`: This module can process a full replay file from FUMBBL and analyze the game
  as well as mirroring the game state in the internal `game-model`.

## Tips for using the FUMBBL API

- See match details: https://fumbbl.com/p/match?id=<match_id>


## Other resources

- https://github.com/gsverhoeven/fumbbl_datasets
- 