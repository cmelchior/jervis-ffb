# Jervis Architecture FAQ

This document describes some of the architectural decisions that have been made in the Jervis codebase,
and what the central concepts are.


## What are the design goals for the project?

This project was started with the following design goals in mind.

1. The implementation should be a faithful representation of the rules "as written". This includes things 
   like always choosing to use a skill or not, or rerolling successful rolls. 

2. The rules engine should not be tightly coupled to the UI. The engine should not care what generated an 
   action. It could be the player through the UI, or some automated process.

3. The rule implementation should be flexible and extendable. Blood Bowl is a game of exceptions,
   and it should be possible to capture all these exceptions in the code.

4. The code should be A.I. friendly. I.e., it should be easy for an AI training process or model to 
   interact with the rules and the state of the game. 

5. It should be easy to move the state back and forth, e.g., to support Undo or an AI algorithm exploring 
   different branches.


## What are the future goals?

I am still thinking about what the end goal is for this project, it is not set in stone, 
so this is just some random thoughts:

* Be able to replay all FUMBBL Games inside the Jervis Client.

* Be an AI client you can hook up to any FUMBBL game.

* Have its own server implementation, so you can run a games across the network without a 
  client having to act as the server.


## What is this project not?

* This project is just aiming to be a client for playing the actual game, it will not 
  be a league manager or advanced team editor.


## Why is it implemented using Kotlin Multiplatform?

The most honest answer is: Because I am familiar with it.

But there are other more compelling reasons:

1. Kotlin Multiplatform has 100% compatibility with the JVM and can use any Java library when running there.
   This is relevant because the FUMBBL Client/Server is written in Java, so it would be easier to 
   copy (with credit) code from that codebase.
   
2. Kotlin Multiplatform allows for one codebase to compile to many targets: Android, iOS, JVM, Web through Wasm 
   and C. Granted, Web Wasm support is very much experimental and C will probably require its own wrapper on 
   top (since Kotlin Multiplatforms C-API is very esoteric), but it keeps options open.

3. Speed is important when training an A.I. agent, so having all the rules in C++/C/Rust would be helpful.
   Unfortunately, the trade-off would be that all UI implementations would need to write an FFI layer to 
   interact with it. That didn't seem like a lot of fun.

4. The JVM is pretty fast these days also compared to e.g., C++.

5. I considered Python, but I really don't like the language for larger projects, and on top, it has poor 
   compatibility with other languages. It would be more challenging to have the rule engine in Python and then 
   write an IPad or Android UI on top. Also, Python can interop with pure C libraries as well as JVM code 
   (through JPype). And it seemed easier to write a Python friendly API from Kotlin once, than writing FFI 
   wrappers for all UI platforms.

6. If Kotlin Multiplatform ends up being a dead end. The code can still run as-is on both Android and the
   JVM.

   
## How are the rules implemented?

The rules are implemented in [`modules/jervis-engine`](../modules/jervis-engine). See a full description
of the architecture in the [architecture-rules-engine.md](architecture-rules-engine.md) document.


## How is the UI implemented?

See more in the [architecture-ui.md](architecture-ui.md) document.


## What is the network architecture?

Right now, this project only supports a "Light-weight" server variant running on 
one of the clients. Expanding this to a full-blown independent server shouldn't 
be difficult, since a full server will "just" run multiple independent games, but it 
is not an immediate goal.  

The overall architecture for networked games are described in more details in 
[network.md](network.md).


## What does a Jervis save file look like?

Currently 3 different kinds of files are created: Team files (.jrt), Roster files (.jrr) and
Game files (.jrg). All are just JSON content, while not the most compact format, it does make 
make them easy to read and modify. 

Team and Roster files are just that. Game files are slightly different.

The main idea behind a Jervis game file is that it isn't just a _snapshot_ of the 
current game state, but it also encapsulates all events leading up to that state.

This means that when you load a game file, it is possible to either pick up a
game from where it ended. As well as undoing some steps and continue playing
from there.

<TODO Expand description once save format is more formalized>
