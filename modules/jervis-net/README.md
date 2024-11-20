# JERVIS NET

This module contains the infrastructure to create and connect to a Jervis
Light-weight Game Server.

This is not a full-blow game server, but is only used to control a single
game being played in Standalone Mode.


## State Machine

// Starting session
-> JoinGame
<- TeamList (Not there yet)
<- ClientJoined (to all connections)

// Accept game
<- ConfirmGame
-> StartGame / LeaveGame

// Ending session / game
<- ClientLeft
<- GameClosed

// Action sync
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
- thread safe.


