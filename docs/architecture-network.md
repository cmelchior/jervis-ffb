# Jervis Network Architecture

This document describes various aspects of how networked games are set up and run as well as how the
underlying network protocol behaves.

Right now we only support one type of networked game, Peer-to-Peer (P2P). But the code and architecture should
be created in a way that generalizing it to support a full-blown game server in the future shouldn't
be too difficult.


## Peer-to-Peer Game Setup

In Jervis, a P2P game is working in the following way:

1. A "P2P Host" creates a game. This will spawn a `LightServer` that is running in its own thread on the 
   same machine. The idea being that eventually this `LightServer` can be replaced with a proper game server.

2. The "P2P Host" connects to the `LightServer` as though it was a remote server.

3. Another machine called "P2P Client" connects to the URL exposed by the server.

4. Both Host and Client now run the game as if running against a proper server.


## Running a Peer-to-Peer Game

Once a game is started, each party in the setup runs their own local copy of the Rules Engine. This means 
the "Host", "Server" and "Client" all have their own copy of the game state (and rules). In case of 
conflicts, the "Server" is considered the authority on what the correct state is.

At a high level, you can think of this as a distributed system that needs to keep a view consistent with 
all parties.

Since the Rules Engine change state by processing a new `GameAction`, we just need to define how these 
actions flow through the system.

Illustrated by an example with three parties (Host, Client and Server):

1. The game is started across all parties. All parties agree on the starting state: `State.0`.

2. Host selects `Action.1` that is applied to the host state moving it to `State.1`.

3. Once applied on the Host, `Action.1` is sent to the Server.

4. Server runs `Action.1` on its own state, moving the Server state to `State.1`.

5. Once accepted on the server, the server sends `Action.1` to all other connected parties, in this case
   Client.

6. Client receives `Action.1` and applies it to its own state, moving the Client state to `State.1`.

7. Now all parties again agree on the current state, including who should produce the next `Action.2`.

8. This loop continues until the game is over.

This approach has a number of advantages:

- Network traffic is kept to a minimum, i.e. only actions are sent, rather than all model changes.
- The server is still the source-of-truth as it will reject all invalid commands.
- The server can decide where it accepts actions from, e.g., dice rolls could be done on the server
  and sent to all clients, rather than a single client producing them.
- Clients can run all their own actions "offline", i.e. they strictly only need to wait for the server
  when waiting for other parties.

The disadvantages are:
- Server and Client need to 100% agree on the rules being used. So the exact same rules engine needs to
  run both places. 
- Loading older save games will be made more complicated unless we can also restore the same version
  of the rule engine. Breaking changes to rules that impact actions are more difficult to handle.

Some additional rules required for this to work:

1. All clients must agree on numbering of game actions. We achieve this by using a simple global numbering
   system. This can be thought of as the Lamport timestamp for the game state.

2. A game action number is never allowed to be re-used (unless in very specific circumstances, see the
   next section).


### Reverting State

In a game where we always know who produces the next action, there should never be a need to revert state, but
in a realistic setting with things like timeouts, sometimes the producer of an action will change. E.g., in the case
a client runs out of time, the server might produce the next action instead. In this case, there is a risk that 
server and clients do not agree on the game state (if the client produced an action at the same time a timeout
was triggered on the server). Now it needs to be corrected.

This is done through a special "Revert" action. Illustrated with an example with three parties (Host, Client and Server)

1. The game is running across all parties. All parties agree on the current state: `State.42`. It is Client's turn,
   but it has been slow choosing the action. Now the next two steps happen at the "same time".

2. Server triggers a timeout and creates `Action.43(s)` and applies it to its own state, producing Server `State.43(s)`.
   This state is sent to Host and Client.

3. At the same time Client produces `Action.43(c)` applies it to its own state, producing `State.43(c)`. It sends
   `Action.43(c)` to the Server.

Now different things will happen. Depending on where you look. Starting on the server.

1. Server receives `Action.43(c)` and detects that it is already at `State.43(s)`. It responds with an OUT_OF_ORDER
   error to Client and otherwise leaves its own state alone (since it is the authority).

On the Client his happens:

1. First, Client receives `Action.43(s)`, but since the client is already at `State.43(c)`, it detects
   that something is wrong and stores `Action.43(s)` in a special list of actions that needs to be applied
   later.

2. Now, Client receives the OUT_OF_ORDER error; it reacts to this by evoking "Revert" on its local game state.
   This reverts the Client State to a point just before `Action.43` was applied, i.e. `State.42`. Note, this 
   is a very destructive change that remove all traces of the previous `Action.43` and is the only place
   where it is allowed to reuse game actions ids.

3. Now, Client drains the special list of actions from the server, applying `Action.43(s)` to its local state.

4. Client and Server now both agrees on the current state: `State.43`, and the game can continue.

Note, "Revert" is different from "Undo". Where "Undo" is a visible user action shared between clients, "Revert" 
is a private event used by the game controller to bring a single client back to a correct state. If a "Revert" action
is sent to the Server, it will always be rejected.


## Protocol

Communication during a game happens through a Websocket connection. On top of this, we have built a custom
protocol expressed through JSON objects. See `NetMessage`, `ClientNetMessage` and `ServerNetMessage` as entry 
points into the classes defining the protocol.

<TODO Ad a more detailed description of the protocol>

### Starting a game (Host)

--> JoinGameAsHost
<-- GameStateSync
<-- CoachJoined (Host)
<-- TeamJoined (Host Team)
<-- UpdateHostState
<-- CoachJoined (Client)
<-- TeamJoined (Client Team)
<-- ConfirmGameStart
<-- UpdateHostState
--> StartGame
<-- GameReady
<-- UpdateHostState
--> GameStarted


## Limitations

The standalone server is a light-weight server only used to run a single game. This means 
that all state is in-memory and disappears when the Host application is closed (for 
whatever reason).

Due to limitations in the browser, it is not possible for the WASM Client to
act as a Host. Only Desktop and iPad Clients can do that.
