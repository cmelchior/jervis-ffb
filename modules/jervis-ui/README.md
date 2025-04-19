# Jervis UI

This module contains the UI code for the Jervis Game Client.

### Updating Resources

It is possible to sync the latest FUMBBL resource with the Jervis project
by using the following Gradle command:

```
./gradlew updateFFBResources
```

This task will clone the FFB repository, take the resource folder and
flatten it so it is usable by Compose Multiplatform and then finally moving it
into place in the project so it is ready for immediate use.

See https://youtrack.jetbrains.com/issue/CMP-4196 for more details on why
this is needed.

Player icons and portraits are not copied during this, they are loaded dynamically
when a roster is used and cached locally. This is done using the `fumbbl-icons.ini` file 
which is copied as part of the above process. This file defines the mapping 
between a local path and the URL on which they can be found.


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
