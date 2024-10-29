# UI Todo list and Bugs

This document is used to track various todo lists for the UI as well as 
bugs encountered that are not fixed.

The TODO list are by no means exhaustive, but is just a place to dump
things as they come up.


## Todo List
- [ ] Better ball animation using Projectile Motion equations.
  - [ ] https://www.youtube.com/watch?v=qtsxHx1MpUI&ab_channel=PhysicsAlmanac
  - [ ] Size-Distance Scaling Equation: https://sites.millersville.edu/sgallagh/Publications/top2013.pdf
  - [ ] https://en.wikipedia.org/wiki/Visual_angle
  - [ ] https://www.quora.com/Do-objects-appear-exponentially-smaller-as-you-move-away-from-them
  - [ ] https://www.youtube.com/watch?v=_KLfj84SOh8&ab_channel=ErikRosolowsky
  - [ ] https://www.omnicalculator.com/physics/time-of-flight-projectile-motion
- [ ] Add numbers where the player has moved already
- [ ] Add square where opponent players can move to
- [ ] Move dice roll dialogs away from the pitch
- [ ] Rethink dialog design. They are currently way too big. As a minimum
      you need to be able to drag them away from the screen.


## Design ideas

- [ ] Experiment with an action system similar to BB3 (round action circle around player)
- [ ] Think about an UI that can work across Desktop: 16:9 and iPad 4:3.
- [ ] Is it worth exploring an isometric view (to make it more immersive)?


## Known bugs
- Blitz: After Selecting target, no squares for movement is shown and not 
  possible to end action. Only happens first time when loading the game. Undoing 
  back into the state is working fine.
