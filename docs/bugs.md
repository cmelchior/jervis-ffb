# Bug List

This file is just a temporary way to capture any bugs seen that I didn't have time to investigate yet.

## Known bugs

- Block against Kroxigor with 1 asssist 4 < 5 only rolls 1 dice
- Cannot select Block action from prone.
- No pass animation is being triggered when Using the Pass action.
- Player with ball blocking across middle line drops ball that bounce, crash
- Hover over player in dugout during setup doesn't show stat card.
- The UI flow for P2P Host/Client works in the happy case, but there are plenty of options
  for messing it up. Probably need to rethink the logic to make it easier to manage. Especially
  when the other party jump back in the flow.
- When selecting dice rolls on the server, it should not be possible to select "All" for UNDO.
  "All" doesn't work, since if you undo a dice roll, the server will register it is at a point where
  it needs to roll and then immediately re-apply the dice roll.

- Saw this when GFI: Seems to be a race condition in the flow because we are jumping threads. Can be
  reproduced relatively consistently using two random players in a P2P Game (it does require the UI)
  Exception in thread "AWT-EventQueue-0" java.util.ConcurrentModificationException
  at java.base/java.util.ArrayList$Itr.checkForComodification(ArrayList.java:1095)
  at java.base/java.util.ArrayList$Itr.next(ArrayList.java:1049)
  at com.jervisffb.engine.fsm.ProcedureStack.containsNode(ProcedureStack.kt:142)
  at com.jervisffb.ui.game.viewmodel.SidebarViewModel$special$$inlined$map$1$2.emit(Emitters.kt:66)

- If a ball bounce over the center during kick-off and caught by a player there, it should still be returned to the
  the other team (I think, double check the rules)

- In a HotSeat game beween 2 random players and Blitz is rolled, the game deadlocks
