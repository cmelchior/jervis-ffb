# Jervis Architecture FAQ

This document describes some of the architectural decisions that have been made in the Jervis codebase,
and what the central concepts are.

## What are the design goals for the project?

This project was started with the following design goals in mind.

1. The implementation should be a faithful representation of the rules "as written". This includes things 
   like always choosing to use a skill or not, or rerolling successful rolls. 

2. The rules engine should not care what generates an action, it could be the player through the UI,
   or some automated process.

3. The rule implementation should be flexible and extendable. Blood Bowl is a game of exceptions,
   and it should be possible to capture all these exceptions in the code.

4. The code should be A.I. friendly. I.e., it should be easy for an AI training process or model to 
   interact with the rules and the state of the game. 

5. The rules should be decoupled from the UI. It should be possible to hook many different kinds of UIs
   up to the same rules engine.

## What are the future goals?

1. Be able to replay all FUMBBL Games inside the Jervis Client.

2. Be a alternative to the current FUMBBL Client.

3. Be an AI client you can hook up to any FUMBBL game.

4. Have its own server implementation, so you can run a games across the network, without using 
   the FUMBBL server.


## Why is it implemented using Kotlin Multiplatform?

The most honest answer is: Because I am familiar with it.

But there are other more compelling reasons:

1. Kotlin Multiplatform has 100% compatibility with the JVM and can use any Java library when running there.
   This is relevant because the entire FUMBBL Client/Server is written in Java, so it would be easier to 
   copy (with credit) code from that codebase.
   
2. Kotlin Multiplatform allows for one codebase to compile to many targets: Android, iOS, JVM, Web through Wasm 
   and C. Granted, Web Wasm support is very much experimental and C will probably require its own wrapper on 
   top (since Kotlin Multiplatforms C-API is very esoteric), but it keeps options open.

3. Speed is important when training an A.I. agent, so having all the rules in C++/C/Rust would be helpful there.
   Unfortunately, the trade-off would be that all UI implementations would need to write an FFI layer to 
   interact with it. That didn't seem like a lot of fun.

4. The JVM is blazingly fast these days also compared to e.g. C++.

5. I considered Python, but I really dislike the language, and on top, it has poor compatibility with other 
   languages. It would be more challenging to have the rule engine in Python and then write an IPad or Android 
   UI on top. Also, Python can interop with pure C libraries as well as JVM code (through JPype). And it seemed
   easier to write a Python friendly API from Kotlin once, than writing FFI wrappers for all UI platforms.

6. If Kotlin Multiplatform ends up being a dead end. The code can still run as-is on both Android and the
   JVM.

## Why Jervis?

As a homage to the original creator of Blood Bowl: Jervis Johnson. Who would be better at playing 
Blood Bowl, than the man who invented it. 

Also, it sounds similar to J.A.R.V.I.S, the A.I from the Marvel Universe, so it also a funny play 
of words.

## How is the rules implemented?

The rules are implemented in `modules/game-model`.

...

<Seperation of Concerns>

<Description of FSM>
    <Node Types>
    <Contexts>

<ActionDescriptor> vs. <Action> vs. <Commands>

<GameModel>

<Undo>

<Disadvantages>
    <Sounds>
    <Animations>
    <Multiple actions in one go>

## How is the UI implemented?

<TODO>

## How does the server architecture work

It doesn't look like anything right now, since no server has been implemented yet.
But it is envisioned to look like the following:

1. Server and two game clients, all running a copy of the rules engine.

2. When GameClient.A executes GameAction.1, it is executed on the local copy and also sent 
   to the server.

3. The Server checks the validity of that action, i.e. is it a valid action and from the 
   correct player. If yes, it is executed on the servers local copy and also sent to
   GameClient.B

4. GameClient.B receives GameAction.1 and runs it on its own copy.

5. This continues until the game is over.

This approach has a number of advantages:

- Network traffic is kept to a minimum, i.e. only actions are sent, rather than all model changes.
- The server is still source-of-truth as it will reject all invalid commands.
- The server can decide where it accepts actions from, e.g. dice rolls should be done on the server,
  and not from the client.

The disadvantages are:

- Server and Client needs to 100% agree on the rules being used. So exact same rules engine needs to 
  run both places.
- Loading older save games will be made more complicated unless we can also restore the same version
  of the rules engine.

## What does a Jervis save file look like?

The main idea behind a Jervis save file is that it isn't just a _snapshot_ of the 
current game state, but it also encapsulates all events leading up to that state.

This means that when you load a save file, it is possible to roll back as long
as you would like.
