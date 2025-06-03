# Game Flow Implementation

This document is used during development as a list tracker for things that need 
to be implemented. Things should only be marked as completed when they have unit 
tests covering the particular feature. This document only covers the core game
rules, skills are tracked separately in [skills.md](skills.md). 

## Pregame
- [x] The Fans
- [x] The Weather
  - [x] Sweltering Heat
  - [x] Very Sunny
  - [x] Perfect Conditions
  - [x] Pouring Rain
  - [x] Blizzard
- [ ] Take on Journeymen
- [ ] Inducements (Select and Buy)
  - [ ] Temp Agency Cheerleaders
  - [ ] Part-time Assistant Coaches
  - [ ] Weather Mage
  - [ ] Bloodweiser Kegs
  - [ ] Special Plays (soooo many cards)
  - [ ] Extra Team Training
  - [ ] Bribes
  - [ ] Wandering Apothecaries
  - [ ] Mortuary Assistant
  - [ ] Plague Doctor
  - [ ] Riteous Rockies
  - [ ] Halfing Master Chef
  - [ ] Unlimited Mercenary Players
  - [ ] Star Players
  - [ ] (In)famous Coaching Staff
    - [ ] Josef Bugman 
    - [ ] How many others(???)
  - [ ] Wizard
    - [ ] Hireling Sports-Wizard
    - [ ] How many others(???)
  - [ ] Biased Referee 
    - [ ] Biased Referee
    - [ ] How many others(???)
- [ ] Roll on Prayers to Nuffle Table. Duplicates are only checked within the same team
  - [ ] Treacherous Trapdoor
  - [x] Friends with the Ref
  - [x] Stiletto
  - [x] Iron Man
  - [x] Knuckle Dusters
  - [x] Bad Habits
  - [x] Greasy Cleats
  - [x] Blessed Statue of Nuffle
  - [x] Moles under the Pitch
  - [ ] Perfect Passing
  - [ ] Fan Interaction
  - [ ] Necessary Violence
  - [ ] Fouling Frenzy
  - [ ] Throw a Rock
    - [ ] Choice is optional
  - [ ] Under Scrutiny
  - [ ] Intensive Training

## Start of Drive

- [x] Setup
  - [x] Too many players on the field
  - [x] Too few players on the field
  - [x] Too many players in Wide Zones
  - [x] 0 players available
  - [x] Too few players on the LoS
  - [x] If less than 3 players, all must be on LoS
- [x] Kick-off (Kicking player and target)
  - [x] Default: Players not on LoS
  - [x] 3 or less
  - [x] All players on LoS
  - [x] Place Kick: Only locations on the opponent side
- [x] The Kick-off Event
  - [x] Get the Ref
  - [x] Time-out
  - [x] Solid Defense
  - [x] High Kick
    - [x] Allow moving player to kicking side
  - [x] Cheering Fans
  - [x] Brilliant Coaching
  - [x] Changing Weather
  - [x] Quick Snap
  - [ ] Blitz
    - [ ] Some skills are not available
  - [x] Officious Ref
  - [x] Pitch Invasion
- [x] What goes up, must come down
  - [x] Land on Field
  - [x] Lands on Player
  - [x] Team rerolls not available for catch
- [x] Touchbacks
  - [x] Give to player
  - [x] Give to prone player is all players are fallen over
  - [x] End sequence as soon as ball goes over middle. I.e., not catch rolls etc.
  - [x] Going out of bounds
  - [x] Go out of bounds / award to player that is then knocked down and ball bounce (previous bug)

## Game

- [x] Moving the turn marker
- [x] Halfs
- [x] Extra Time
- [x] Sudden Death
- [ ] Drive counter
- [ ] Scoring
  - [x] Moving into the End Zone with the ball using a standard move
  - [ ] Follow-up into the End Zone
  - [ ] Jumping into the End Zone with the ball
  - [ ] Leaping into the End Zone with the ball
  - [x] Picking the ball up in the Endzone
  - [x] Catching the ball after a pass in the end zone
  - [x] Catching the ball after a hand-off in the end zone
  - [x] Catching the ball after it bouncing into the end zone
  - [x] Catching the ball in the end zone after a throw-in 
  - [ ] Push player to scoring position during block, but blocker is knocked down.
  - [ ] Throw Team-mate. Player lands in the end zone
  - [ ] Chain push: Player with the ball is pushed into the end zone
  - [ ] Chain push: Player is pushed into the ball and picks it up.
  - [ ] Using Ball & Chain to move into the End Zone (not really supported, but just in case)
  - [ ] Pushed into end zone after a block
- [ ] Turnover ends turn as quickly as possible
  - [ ] Falling Over
  - [ ] Knocked Down
  - [ ] Placed Prone with the Ball
  - [ ] Player with ball goes into the crowd
  - [ ] Fails to pickup ball
  - [ ] Fumble Pass, even if caught
  - [ ] No catch after Pass/Hand-off
  - [ ] Deflection/Interception and ball not caught by throwing team
  - [ ] Throw Team-mate (with ball) fails to land safely, lands in crowd, is eaten
  - [ ] Sent-off for commiting a Foul
  - [ ] Touchdown
- [ ] Conceding
  - [ ] Free if 3 or less players at beginning of turn
- [ ] Team Rerolls
  - [ ] Reset at half time
  - [ ] Carry over into Extra Time
  - [ ] Leader
  - [ ] Can be used to reroll types of rolls: <Figure out list of rolls>
- [ ] Dodging (basic, no skills)
- [x] Deviating Ball
  - [x] D6 in random direction
  - [x] Player in landing square must attempt to catch it
  - [x] Will bounce if player in landing square has no tackle zones or is prone/stunned
  - [x] Will bounce if landing square is empty
- [x] Scatter Ball
  - [x] 3xD8 in random direction, doesn't fully land before last roll
  - [x] Player in landing square must attempt to catch it
  - [x] Will bounce if player in landing square has no tackle zones or is prone/stunned
  - [x] Will bounce if landing square is empty
- [x] Bouncing Ball
  - [x] Move 1 square in random direction
  - [x] Bounce into another player must catch if possible
  - [x] Bounce into prone/missing tz/stunned player will bounce again
  - [x] Keep bouncing until coming to rest
- [ ] Throw-in (basic no skills)
  - [ ] After bounce
  - [ ] After scatter
  - [ ] After Deviate
  - [ ] Repeat process
  - [ ] Catch or Bounce on landing
- [ ] Catch (basic no skills)
  - [ ] Accurate pass
  - [ ] Deflection into Interception
  - [ ] Bouncing ball
  - [ ] Thrown-in
  - [ ] Scattered
  - [ ] Deviated
  - [ ] TackleZone modifiers
  - [ ] Must roll for catch
  - [ ] Players can only catch if they have tackle zones
  - [ ] Failing to catch will bounce from square
- [ ] Track Star Player Points
- [ ] Track more advanced stats
- [ ] Track Star Player Points and adjustments to it.
- [ ] Touchdown
- [ ] Detect Stalling
- [ ] Trapdoors
- [ ] Maximum Stats
- [ ] Knocked Down
- [ ] Falling Over
- [ ] Roll over if stunned last turn
- [ ] Moving into a square
  - [ ] Moving voluntarily
  - [ ] Moving involentary
  - [ ] Being pushed
  - [ ] Need Dodge/Rush/Jump to go there

## Actions

- [x] Move Action
  - [x] Select player and end action before moving doesn't doesn't mark the player as used. 
  - [x] Starting a move action while prone and aborting it again doesn't mark player as used
  - [x] Moving any square mark the action as "used".
  - [x] Action doesn't end when no more "normal move" is left, only when all rushes are also used.
- [x] Stand Up
  - [x] Must stand up doing any action
  - [x] Standing up uses 3 move
  - [x] Standup with less than 3 strength requires a roll and use all move.
  - [x] Failing stand-up roll ends action and uses player action
    - [x] Move
    - [x] Block
    - [x] Blitz
    - [x] Pass
    - [x] HandOff
    - [x] Foul
    - [ ] Throw Teammate
    - [ ] Special actions
- [x] Dodge
  - [x] Moving away from marking player requires a dodge roll.
  - [x] Moving away from non-marking player is free.
  - [x] Need agility roll to dodge.
  - [x] Failure: Knocked over in target square
- [x] Rush
  - [x] Rush before Dodge.
  - [x] A player can normally rush twice pr. action
  - [x] Rush is 2+
  - [x] Failing a Rush puts player in target square.
  - [x] Move action is over after rushing twice.
- [x] Jump Sub-action
  - [x] Can only jump over prone/stunned player
  - [x] Jump over stunned/prone player from both teams
  - [x] Must use two move to reach target square. If not enough move left (including rush), no jump is allowed.
  - [x] If Rushing twice and fail, ends up in target square
  - [x] Can only jump to opposite squares. Similar to pushes
  - [ ] Cannot jump over a Giant since the rules specify a "single square"
  - [x] Modifiers on leaving and entering
- [ ] Pass Action
- [ ] Hand-off Action
- [ ] Throw Team-mate Action
- [x] Block Action
  - [x] Cannot block while prone
  - [x] Can only block if marking the player.
  - [x] Unlimited blocks
  - [x] 1 - 3 dice rolls depending on strength
  - [x] Turnover if knocked down
  - [x] Assists
    - [x] Assists from open players
    - [x] Players being marked themselves cannot assist
    - [x] Prone players cannot assist
  - [x] Player Down!
  - [x] Both Down!
  - [ ] Push Back!
    - [ ] Push direction
    - [ ] Can only push into empty squares
    - [ ] Follow up
    - [ ] Chain Push into free space
    - [ ] Push into the crowd if no free space. Crowd take predence
  - [x] Stumble
  - [x] Pow!
- [x] Blitz Action
  - [x] Must select target before starting blitz 
  - [x] Rush To Blitz
  - [x] Cannot blitz if no more move
  - [x] Cannot blitz if no valid targets
  - [x] Move after blitz
  - [x] Blitz uses 1 move
  - [x] Fail rush just before blitz
- [ ] Foul Action
  - [ ] Must select player when starting action
  - [ ] Can only foul selected player
  - [ ] Assists
  - [ ] Caught by the Ref
- Hand-off Action
  - [ ] Can start action without having the ball
  - [ ] Action ends after handover (no more moves allowed)
  - [ ] Can hand-over after all moves are used
  - [ ] Players with PA - can still hand-off
  - [ ] Test against agility to catch it
- Pass Action
- Throw Team-mate Action

## BB7

- [ ] Place Kick (BB7): All locations not on kicking teams side.
- [ ] Touchback ((BB7): Only happens when crossing back to kicker's side, not in No Man's Land.

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












