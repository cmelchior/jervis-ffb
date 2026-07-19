# Jervis Engine
This module contains the Jervis Game model. This is the full set of Blood Bowl
rules and the logic for running a game. It can run independently of the UI.

## Submodules
The engine consists of the following submodules: 

- `core`: The engine infrastructure for setting up and running games. This
   module is rules agnostic.
- `rules-common`: Module containing all rules classes and logic that is shared 
  among different rulesets.
- `rules-bb2020`: Contains all BB2020 specific rules and classes. It also
  contains all variants that are based on BB2020, like Sevens.
- `rules-bb2025`: Similar to `rules-bb2020`, but contains all the logic and 
  classes for BB2025.
- `package`: This module is what downstream consumers should use. It is 
  responsible for combing the `core` module with the set of currently supported 
  rules. It also contains the code needed to serialize and deserialize game, 
  team and roster files.

## Adding a new rule
Rules are implemented using the `Procedure` abstract class. If it applies 
to both BB2020 and BB2025, it should be placed in `rules-common`, 
otherwise in their respective rules module.

Sometimes you have a shared top-level procedure, that will delegate to different
rules-specific sub-procedures. In this case, a new property must be added
to `RulesParameters`, which will allow each ruleset to define their own 
behavior.
