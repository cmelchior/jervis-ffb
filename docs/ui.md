# UI Todo list and Bugs

This document is used to track various todo lists for the UI as well as 
bugs encountered that are not fixed.

The TODO list are by no means exhaustive, it is just a place to dump
things as they come up.


## Todo List
- [ ] When selecting a team reroll, what to do about temporary rerolls.
      Right now the rules just select the most appropriate one, but most likely we need 
      to list all types of rerolls and let it be up to the UI to filter/show them somehow.
- [ ] Add "quick"-action when prone, where you can also select Jump if it would be 
      possible after standing up.
- [ ] Filter actions that cannot be used because they cannot select a target
  - Foul
  - Block if Prone (but yes combined with Jump Up)
  - Blitz if no players
- [ ] Custom cursors:
  - [ ] During move
  - [ ] When selecting block
  - [ ] When selecting pass target
  - [ ] When selecting foul target
- [ ] Add numbers to yellow squares to indicate the target number for rush/dodge.
- [ ] Keep player card open for the currently active player.
- [ ] Add support for Rerolls / Apothecary / etc. in the sidebars
- [ ] Create "dice"-like background for dice rolls.
- [ ] Only have 3 players pr. row in the Dugout (similar to FUMBBL).
- [ ] Add numbers where the player has moved already
- [ ] Passes going out of bounds currently land in exitAt, make the animation move the
      ball out of bounds.
- [ ] Set size of player icons and square decorators correctly. Right now they are bit off making them look blurry.
- [ ] Add UI indicator signaling limit of opponent players move (see BB3).
- [ ] Add scrollbar indicator to game log components.
- [ ] If possible, move dice roll dialogs away from the pitch.
- [ ] Rethink dialog design. They are currently way too big. As a minimum
      you need to be able to drag them away from the screen.
- [ ] Some fonts need to scale with the size of the window. Introduce the concept of 
      wsp (Window Scaled Pixels) and use them where appropriate (Player stats / Skills)
- [ ] Figure out a better way to trigger recomposition. Currently, there is a lot of
      object creation/copying going on with UiFieldSquare. It seems performant enough
      on my machine, but it also feels like it could be optimized. Currently there
      are two places changes are happening. In the rules engine and in 
      ManualActionProvider when setting up listeners.
- [x] Hover on block shows dice indicator.
- [x] Better ball animation using Projectile Motion equations.
    - https://www.youtube.com/watch?v=qtsxHx1MpUI&ab_channel=PhysicsAlmanac
    - Size-Distance Scaling Equation: https://sites.millersville.edu/sgallagh/Publications/top2013.pdf
    - https://en.wikipedia.org/wiki/Visual_angle
    - https://www.quora.com/Do-objects-appear-exponentially-smaller-as-you-move-away-from-them
    - https://www.youtube.com/watch?v=_KLfj84SOh8&ab_channel=ErikRosolowsky
    - https://www.omnicalculator.com/physics/time-of-flight-projectile-motion
- [x] Standup and end movement as a single action (UI only).


## Design ideas

- [ ] Experiment with an action system similar to BB3 (round action circle around player)
- [ ] Think about an UI that can work across Desktop: 16:9 and iPad 4:3.
- [ ] Is it worth exploring an isometric view (to make it more immersive)? 
  - How feasible is it to use the current player graphics?


## Known bugs

- Pass animation triggers multiple times when landing on player (catch) and catch fails.
- Block against Kroxigor with 1 asssist 4 < 5 only rolls 1 dice
- Can block from prone
- UI does not restore moved used indicators correctly when undo'ing across player actions.
- Player with ball blocking across middle line drops ball that bounce, crash
  java.lang.IllegalArgumentException: Cannot determine position of: FieldCoordinateImpl(x=2147483647, y=2147483647)
  at com.jervisffb.engine.rules.Rules$DefaultImpls.throwIn(Rules.kt:141)
  at com.jervisffb.engine.rules.BB2020Rules.throwIn(StandardBB2020Rules.kt:5)
