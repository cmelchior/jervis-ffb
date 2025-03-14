# Network

This document is mostly here to track various aspects of network behavior that requires tests, and/or
doesn't work correctly yet.


1. P2P Host flow after starting the server, i.e. it should be able to handle client disconnecting
   while waiting to accept the game (it doesn't right now)

2. What happens if multiple clients connects. The last client should be rejected. Requires test.