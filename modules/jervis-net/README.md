# JERVIS NET

This module contains the infrastructure to create and connect to a Jervis
Light-weight Game Server.

For now, this is not a full-blow game server, but is only used to control a 
single game being played in P2P Mode.

## Handle connection / disconnect

-> Connection Start
<- Status Code != 200 (error happened, connection not established)
<- Status Code == 200 (connection established)
-> JoinGameAsX (verify that )
<- Server State Sync (send latest state of the server relevant to the client)

## State Machine - Host

HostState updates should always follow after the messages needed to actually
get to that state.


* Setup Game
* Select Team
-> Start Server (with rules + host team)
-> JoinAsHost 
<- GameStateSyncMessage (hostState = JoinServer)
<- CoachJoined ("Host")
<- TeamJoined ("HostTeam")
<- HostStateUpdate ("WaitForClient")
<- TeamJoined ("ClientTeam")
<- ConfirmStartingGame
<- HostStateUpdate ("AcceptGame")
-> StartGame(true)

## State Machine - Client

-> Connection Start (with GameId)
-> JoinAsClient
<- ServerStateSync (state = SELECT_TEAM)
-> TeamSelected
<- TeamJoined
<- Update State (Accept Game)
-> ConfirmGame
<- AcceptGame(yes/no)
<- Update State (Run Game)



// Setup Game 

-> Connection Start
<- ServerStateSync (state = SELECT_TEAMS)
-> JoinGameAsPlayer (ignore gameid for now, there is ever only one)
<- ClientJoined (to all connections)
-> TeamSelected
<- TeamJoined
<- Update State (Accept Game)
-> ConfirmGame
<- AcceptGame(yes/no)
<- Update State (Run Game)



// Game Loop
-> GameAction
<- SyncGameAction (other clients)

// Errors that are not killing the connection
<- ServerError

// Future enhancements
-> ChatMessage
<- SyncChat
-> ChatCommand
<- CommandModification

## Thread safety

- All messages for a single game session are being handled through a single channel
- This ensures that all state is only modified from a single source, making it 
  thread safe.


