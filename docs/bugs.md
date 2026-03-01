# Bug List

This file is just a temporary way to capture any bugs seen that I didn't have time to investigate yet.

## Known bugs
- Team Badge Outline does not rescale when the game window size changes.
- The DevModeGameActions do not work in P2P due to too strict checking for action owners.
- In a P2P Game, both Host and Client can select the same team
- Move Counter is not updated on forced follow-up during a blitz, like when using Taunt
- Move counter is wrong when using the first GFI to Blitz and then the 2nd GFI to move in another direction. 
- Move counter on field squares does not reset when a players action ends immediately (like when using Safe Pass)
- Action Wheel animation isn't correct when animating D6 rererolls when automatically selecting the reroll type.
  It looks like two animations are running over each other or that some positions are being reused.
- Action Wheel for Catch rerolls jump back to the thrower when undoing actions
- CompositeGameActions are broken up in the save file, so if you reload a save file and start Undo'ing actions,
  it will behave differently than if you do it from the same game.
- No pass animation is being triggered when Using the Pass action.
- Player with ball blocking across middle line drops ball that bounce, crash
- Hover over player in dugout during setup doesn't show stat card.
- The UI flow for P2P Host/Client works in the happy case, but there are plenty of options
  for messing it up. Probably need to rethink the logic to make it easier to manage. Especially
  when the other party jump back in the flow.
- When selecting dice rolls on the server, it should not be possible to select "All" for UNDO.
  "All" doesn't work, since if you undo a dice roll, the server will register it is at a point where
  it needs to roll and then immediately re-apply the dice roll.
- Sidebar is not updated correctly when players are banned. The count is correct, but they do not show up
- `ActionSelectorViewModel` throws concurrent modification exception for unknownActions. Figure out a solution.
- Apothecary during Multiple Block seems to crash when used with Undo. Needs more investigation.
- Starting a Blitz from prone when next to the target, does not provide a quick action for standing up and attacking in one click.
- "Standup and End Action" is not working when player has a negatrait (Kroxigor)
- We do not catch the case where all prayers to nuffle are active and do not abort rolling, resulting in an infinite loop.