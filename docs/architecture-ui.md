# Jervis UI Architecture

This document describes the architecture and design decisions that went into 
creating the Jervis Client UI.

The UI is implemented using Compose Multiplatform, a declarative UI framework
similar to React on web.

The primary reason for choosing this approach over a more normal game engine
approach is that it is available across all platforms, making it easier to
write the UI.

This also allows us to lean into the possibility of easily moving the UI state
back and forward together with the game state. This is especially relevant
when Undo is a first-class citizen.

The downside is that the rendering performance is lower than a traditional Game Engine, but 
as the UI isn't that demanding, the UI performance, so far, seems to hold up.

We want to have an independent Rules Engine, so we try to have a strict separation
between "Rules" and "UI". The idea being that two different UI's should be able to
play the same game. This means we do not want to track things in the model layer
that are only UI-related.

In practise this is hard to achieve without introducing a lot of complexity, so for
now, the model does store a reference to sprites.

The main entry classes for the UI are:

- `UiGameController`: This class runs the main game loop and is responsible
  for communicating with GameController that runs the rules. The main
  responsibility of this loop is to create a `UiGameSnapshot`, which is a
  datastructure that represents the full state of the UI at a given point
  in time, like a high-level "frame". This is then sent to Compose for rendering.

- `UiGameSnapshot`: Contains all the data to render the current game step.

- `UiGameDecorations`: Is responsible for tracking "model" state that is only
  relevant to the UI. This is e.g. things like bloodspots or move used indicators.

- `UiActionProvider`: This interface is responsible for making sure a game event
  makes it back to the Rules Engine. Either by enhancing the UI, so the user can
  provider it (See `ManualActionProvider`) or through some other mechanism like
  a game event from the network (because it is created by the other player).

- `FieldActionDecorator`: This interface is responsible for configuring the UI so it
  can produce actions of a given type, e.g. it will setup on-click listeners on players,
  if they can be selected.

<TODO Describe how animations and sounds work>

