# Rules Engine Architecture

This document describes the architecture and design decisions that went into creating the rules engine.

The rule engine is designed so it can run without any kind of UI. This also means we try to keep a
strong separation between logic and "UI". For that reason, this document only describes behavior
related to the rules and not the UI.


## Main Architecture

The Rules Engines' main responsibility is to implement a game of Blood Bowl. At its core Blood Bowl
is a board game consisting of players taking turns performing an "action". It is on this premise 
the Rules Engine is built.

The Engine consists of three main parts:

    1. A `Rules` subclass: This class contains all the static rules for the game and defines things
       like the size of the field, which tables to use for the game, etc. It should contain no 
       runtime state.

    2. A set of `Procedure` classes. These classes define the "flow" of the game, i.e. they should
       not contain any runtime state. These classes can use the `Rules` class to get answers they
       need to determine the flow of the game, like how many turns are left, which players are
       still not activated, etc. These classes also expose which legal actions can be taken at any
       point in the game.

    3. A `Game` class. This is the where all the runtime state is stored. It should only be modified
       through `Command` objects created by `Procedure`s. Commands are created as a response to 
       actions taken by players.

TODO Expand this description 

## Rules Logic

TODO Description of FSM/Procedures
TODO Node Types
TODO Contexts

## Game Actions

A game is driven by an instance of a `GameEngineController`. This has a very basic loop:

```kotlin
val gameEngineController = createGameEngine()
gameEngineController.startManualMode()
while (!controller.stack.isEmpty()) {
    val availableActions = controller.getAvailableActions()
    val action = createAction(request)
    controller.handleAction(action)
}
```

Thus, the engine itself doesn't care what creates an action, which makes it straightforward to 
create a layered approach to generating actions.

Example: If a server needs a player to be selected, this can either be provided directly by the server 
or by the client that again creates it automatically through some logic or instead exposes the choice
in the UI.

TODO ActionDescriptor vs. GameAction vs. Command
TODO Undo and Revert
TODO CompositeAction

### Disadvantages
TODO Sounds / Animations / Multiple Actions in One Go
