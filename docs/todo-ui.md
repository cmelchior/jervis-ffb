# UI Todo list and Bugs

This document is used to track various todo lists for the UI.

The TODO list are by no means exhaustive, it is just a place to dump
things as they come up.


## Todo List
- [ ] Add a "dev-mode" protocol for networked game. Should be used for e.g. "Allow Player Edits". Need to figure out 
      how to refresh UI on the other client.
- [ ] Make a keyboard shortcut for showing "Player range" and "Tackle Zones"
      Unclear which keys makes sense. Probably need to make it configurable
- [ ] Add UI indicator signaling limit of opponent players move (see BB3).
- [ ] When should Leader reroll be added/removed/transparent? Right now it happens when setup completes?
      Should it follow the player during setup instead?
- [ ] I messed up the icon logic for players. I assumed that all players are 30x30...that is not correct.
      Big Guys are 38x38 and go beyond their designated square (so they appear more imposing).
      This means we need to separate Player icons from their square. I suspect fixing this would also 
      make it easier to add support for Giants.
- [ ] When selecting a team reroll, what to do about temporary rerolls.
      Right now the rules just select the most appropriate one, but most likely we need 
      to list all types of rerolls and let it be up to the UI to filter/show them somehow.
- [ ] Add "quick"-action when prone, where you can also select Jump if it would be 
      possible after standing up.
- [ ] Custom cursors:
  - [ ] During move
  - [ ] When selecting block
  - [ ] When selecting pass target
  - [ ] When selecting foul target
- [ ] Add numbers to yellow squares to indicate the target number for rush/dodge.
- [ ] Keep player card open for the currently active player.
- [ ] Passes going out of bounds currently land in exitAt, make the animation move the
      ball out of bounds.
- [ ] Set size of player icons and square decorators correctly. Right now they are bit off making them look blurry.
- [ ] Add scrollbar indicator to game log components.
- [ ] Could we add 3d dice rolling across the board? Probably difficult in pure Compose, but maybe using Lottie?
- [ ] Add AFK Limit to timer settings + AFK button in the UI
- [ ] Add a "Player Editor" in Dev Mode that makes it possible to add skills, change stats, and states.
      Need to figure out exactly how this should work.
- [ ] When Undo'ing block dice it messes up the Action Wheel logic so it shows the dice roll that is skipped when going
      forward. That isn't a bug per see, but how the design works. However it feels jarring in that case. Not 100% sure
      what the best approach is. We can either combine Nodes in the engine, or find some ways to hack the UI flow.
      Either way feel kinda hack-ish :/
- [ ] Better ball animation using Projectile Motion equations.
    - https://www.youtube.com/watch?v=qtsxHx1MpUI&ab_channel=PhysicsAlmanac
    - Size-Distance Scaling Equation: https://sites.millersville.edu/sgallagh/Publications/top2013.pdf
    - https://en.wikipedia.org/wiki/Visual_angle
    - https://www.quora.com/Do-objects-appear-exponentially-smaller-as-you-move-away-from-them
    - https://www.youtube.com/watch?v=_KLfj84SOh8&ab_channel=ErikRosolowsky
    - https://www.omnicalculator.com/physics/time-of-flight-projectile-motion
    - I think I implemented the correct algorithm, but the ball landing still looks off. Need to figure out why.


## Design ideas

- [ ] Experiment with an action system similar to BB3 (round action circle around player)
- [ ] Think about an UI that can work across Desktop: 16:9 and iPad 4:3.
- [ ] Is it worth exploring an isometric view (to make it more immersive)? 
      - [ ] How feasible is it to use the current player graphics?


## Architecture Thought Dumping Ground

After the restructuring of the UI architecture to be more clean and immutable, I think there 
is a relatively clear path forward. However, we still need to figure out a good way to benchmark it.
It seems fast enough, but I suspect there are lots of ways it can be optimized. 

- [ ] Having both `FieldViewData` and `FieldSizeData` feels a bit redundant. It
      Would be nice to combine them some way.


## Game Timer

A game timer isn't described as such in the rules, but one would probably need to
be implemented for practical reasons.

It is unclear exactly how such a timer should work in practice, so this
section just contains my current thoughts:

When `processAction` returns, the timer starts for `actionOwner`. It ends when
`processAction` is called again. This should accurately detect the time
it takes a player to select an action.

Of course this doesn't account for a lot of cases, like network latency, disconnecting,
being AFK for valid reasons. This will need to be considered.

Also there is a very open question about what happns when a timer "runs out".

The obvious answer would be an Turnover, but just setting Turnover to true
in the model, doesn't actually trigger that. It would need some GameAction input.

I guess the server could just feed automatic actions to the model. It could
automatically select EndAction, EndTurn, Cancel, NoRerollSelected when they
are available. But in some cases these are not present, e.g. during chain pushes
or during setup.

So there will be some cases where we need to make a judgment call about what
to do.

In theory, we could just force stop the turn by incrementing the turncounter,
but there would be a high chance that some state is left inconsistent. I.e.
removal of temporary stats at the end of a turn.

In the old Blood Bowl rules, it say 4 minutes pr. turn, but that doesn't take
into account all the interruptions during a turn, i.e., if all actions during
a team turn was attributed to the active team, an annoying player could run
down the other players clock by not selecting options.

Some alternatives:

1. Chess clock: You just get allocated 4x16 minutes of time for the entire game.
   If you run out of time, your turns basically turn into No-ops.

2. Allocated something like 3 minutes pr. turn + a pool to be used during the
   opponents turn.









