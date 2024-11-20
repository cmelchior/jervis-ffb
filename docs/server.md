# Server Architecture 

This document describes the network topology and the protocol being used when
clients need to communicate.

Jervis supports 3 modes of playing the game:

1. Hotseat: Everything is running on the same client. No server is used.
2. Standalone Mode (P2P): One client is acting as the "server", the other as the "client".
3. Hosted: Two clients play against each other through a central server. This server can 
   either be controlled through FUMBBL or as a separate server.


## Standalone Server

The standalone server is a light-weight server that is only used to run a single
game. This means that all state is in-memory and disappears when the Client acting
as server is closed.

Due to limitations in the browser, it is not possible for the WASM Client to 
act as a standalone server. Only Desktop and iPad Clients can do that. 


## Hosted Server

<TODO>


## Protocol

<TODO>






   