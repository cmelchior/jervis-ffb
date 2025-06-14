# Rules Engine Architecture

This document describes the architecture and design decisions that went into 
creating the rules engine.

The rules engine is designed so it can run without any kind of UI. This also 
means we try to keep a strong separation between logic and "UI" (which almost
succeeds). For that reason, this document only describes behavior related to the
rules and not the UI.


## Main Architecture

The engine's main responsibility is to implement a game of Blood Bowl. At 
its core, Blood Bowl is a board game consisting of players taking turns 
performing an "action", i.e. nothing happens simultaneously. It is on this 
premise the engine is built.

At a high level the engine consists of the following 3 parts. All of these are 
described in more detail later:

1. A game "driver", that is responsible for controlling the lifecyle of the
   game. It accepts actions from coaches and exposes the result of those 
   actions. This class is called the `GameEngineController`.

2. A "rules layer" that is responsible for determining the flow of the game 
   based on incoming actions. This layer does not care about where actions come 
   from, it just responds to them. This is a Finite State Machine with only pure
   classes and functions. This layer has no state. `Procedure` and `Node` are 
   the main interfaces for this layer.

3. A "game state" holder, that is responsible for storing the full state of the
   game. At any given point in the game, this layer holds a complete snapshot. 
   The "rules layer" will store any state it needs in here. `Game` is the main
   entry point for this layer.

## GameEngineController

The `GameEngineController` is the main entry point for the game engine. On the 
surface, this class is really simple and has two primary methods:

- `getAvailableActions()`: Returns an `ActionsRequest` that describe which 
  actions are valid in the current state and who is responsible for generating 
  them, but it leaves it up to upper layers to actually generate the actions.
- `handleAction(action: GameAction)`: Method that accept an action for the 
   current state, processes it moves the state forward.

On important thing to note is that the `GameEngineController` does not care 
where the action comes from, nor does it impose timing restrictions. All of 
this is left up to the upper layers. This means that actions can be created by a
server, and AI or clients as we see fit, and this can be changed without 
touching the core engine.

A basic game loop thus looks like this: 

```kotlin
val controller: GameEngineController = createGameEngine()
controller.startManualMode()
while (!controller.stack.isEmpty()) {
    val availableActions = controller.getAvailableActions()
    val action = createAction(request) 
    controller.handleAction(action)
}
```

### GameActionDescriptor and GameAction

TODO Description of these two interfaces
TODO Description of Undo/Revert

### ActionsRequest

TODO Description of this class and how it is used

## Rules Layer

The rules layer is the heart of the engine and is responsible for determining 
the flow of the game as well as what are valid actions for any current state.

It has two main parts:

1. A `Rules` subclass: This class contains all the static rules for the game and
   defines things like the size of the field, which lookup tables to use, etc. 
   It should contain no runtime state and all functions should be pure.

2. A set of `Procedure` and `Node` classes. These classes define the flow of 
   the game.

Procedures are not allowed to contain state themselves, all state is stored 
in the `Game` class, where a Procedure or Node can look it up. Procedures can
only modify the `Game` class through `Command` objects. Commands are created
by `Procedure`'s as a response to `GameAction`'s.

This is required so state can correctly be unrolled when handling `Undo` or 
`Revert` actions.

### Procedures

TODO Procedures

### Node Types 

TODO ActionNode
TODO ParentNode
TODO ComputationNode


## Game State

The entire game state, including the current stack of procedures are stored 
in the `Game` class.

### ProcedureContext

TODO ProcedureContext


### Disadvantages
TODO Sounds / Animations / Multiple Actions in One Go
