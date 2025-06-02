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
- If a ball bounce over the center during kick-off and caught by a player there, it should still be returned to the
  the other team (I think, double check the rules)
- During kickoff, if the ball is not caught and then bounces across the the LoS, no touch-back is awarded. This happened
  on the LoS, where opponent player also failed to catch it.