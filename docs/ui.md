# UI Todo list and Bugs

This document is used to track various todo lists for the UI as well as 
bugs encountered that are not fixed.

The TODO list are by no means exhaustive, but is just a place to dump
things as they come up.


## Todo List
- [ ] Add numbers where the player has moved already
- [ ] Passes going out of bounds currently land in exitAt, make the animation move the
      ball out of bounds.
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
- [x] Better ball animation using Projectile Motion equations.
    - https://www.youtube.com/watch?v=qtsxHx1MpUI&ab_channel=PhysicsAlmanac
    - Size-Distance Scaling Equation: https://sites.millersville.edu/sgallagh/Publications/top2013.pdf
    - https://en.wikipedia.org/wiki/Visual_angle
    - https://www.quora.com/Do-objects-appear-exponentially-smaller-as-you-move-away-from-them
    - https://www.youtube.com/watch?v=_KLfj84SOh8&ab_channel=ErikRosolowsky
    - https://www.omnicalculator.com/physics/time-of-flight-projectile-motion

## Design ideas

- [ ] Experiment with an action system similar to BB3 (round action circle around player)
- [ ] Think about an UI that can work across Desktop: 16:9 and iPad 4:3.
- [ ] Is it worth exploring an isometric view (to make it more immersive)? 


## Known bugs

- Pass animation triggers multiple times when rerolling catch during kick-off.