# JERVIS UI

This module contains the UI code for the Jervis Game Client.

## Architecture Notes

* Multiple ClientNetworkMessageHandlers can be registered. They are called in the 
  order they are registered.
* The top-level controller is responsible for state transitions and enforcing rules
  and state. ScreenModel message handlers will always be called after updating the
  state and should thus only use this to correctly update the UI.

* We use the following terms:
  * Host: The Client responsible for the Server lifetime
  * Server: The component responsible for running the game logic. It is the source of truth
  * Client: A process that connects to the server.

In particular, this means that a Host is not special from the Client connecting,
the will receive the same set of websocket messages.
