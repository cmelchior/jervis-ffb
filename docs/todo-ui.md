# UI Todo list and Bugs

This document is used to track various todo lists for the UI.

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
- [ ] Could we add 3d dice rolling across the board? Probably difficult in pure Compose, but maybe using Lottie?
- [ ] Import teams from TourPlay (and other online services).
- [ ] Add AFK Limit to timer settings + AFK button in the UI
- [ ] Add a "Player Editor" in Dev Mode that makes it possible to add skills, change stats, and states.
      Need to figure out exactly how this should work.
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
      - [ ] How feasible is it to use the current player graphics?
- [ ] Add support for downloading teams from TourPlay. They can be accessed using something like this.
      All headers are required.

```
curl --location 'https://tourplay.net/api/rosters/44442' \
--header 'Accept: application/json, text/plain, */*' \
--header 'Referer: https://tourplay.net/en/blood-bowl/roster/44442' \
--header 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36'
```

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









