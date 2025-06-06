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
  - [x] Fumble Pass, even if caught
  - [ ] No catch after Pass/Hand-off
  - [x] Deflection/Interception and ball not caught by throwing team
  - [ ] Throw Team-mate (with ball) fails to land safely, lands in crowd, is eaten
  - [x] Sent-off for commiting a Foul
  - [x] Touchdown - Success
    - [x] Move into opponent end-zone
    - [x] Jump into endzone
    - [x] Leap into endzone
    - [x] Catch pass in endzone
    - [x] Catch hand-off in endzone
    - [x] Catch bounce in endzone
    - [ ] Catch scatter in endzone
    - [x] Catch throw-in in endzone
    - [ ] Pickup deviate in endzone
    - [x] Pickup ball in endzone
    - [x] Push own ball carrier into endzone
    - [x] Push opponent ball carier into endzone 
    - [x] Land after throw team mate in endzone
  - [x] No Touchdown
    - [x] Move into own end-zone
    - [x] Fail rush into endzone
    - [x] Fail dodge into endzone
    - [x] pickup own ball in endzone

- [ ] Conceding
  - [ ] Free if 3 or less players at beginning of turn
- [ ] Team Rerolls
  - [ ] Reset at half time
  - [ ] Carry over into Extra Time
  - [ ] Leader
  - [ ] Only work during teams turn
  - [ ] Can be used to reroll types of rolls: <Figure out list of rolls>
- [ ] Marking Players
  - [ ] Marking
  - [ ] Marked
  - [ ] Open
  - [ ] Defensive Assists
  - [ ] Offensive Assists
  - [ ] Bulk marked modifiers 
- [x] Dodging (basic, no skills)
  - [x] When leaving marking player
  - [x] Not when leaving player with no TZ
  - [x] Not when moving from open into marked position
  - [x] -1 pr. marking player in
  - [x] Move before Roll
  - [x] Fall over in target square
  - [x] Can reroll 
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
- [x] Throw-in (basic no skills)
  - [x] From corners
  - [x] From south/north/east/west
  - [x] Player in landing field must catch it if possible
  - [x] Will bounce if player is not able to catch a ball
  - [x] Repeat process if leaving the field again
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
- [ ] Track statistics (not in the rules)
- [ ] Touchdown
- [ ] Detect Stalling
- [ ] Trapdoors
- [ ] Maximum Stats
- [ ] Knocked Down
- [ ] Falling Over
- [ ] Roll over if stunned last turn
- [ ] Moving into a square
  - [ ] Moving voluntarily
  - [ ] Moving involuntary
  - [ ] Being pushed
  - [ ] Need Dodge/Rush/Jump to go there
- [ ] Characteristic Tests
  - [x] Agility
  - [ ] Passing
- [ ] End of turn
- [ ] End of Drive
- [ ] End of Half
- [ ] End of Game
- [ ] Range Ruler
  - [ ] Follows the chart in the rulebook
  - [ ] Interceptions are correct. See https://www.luccini.it/bloodbowl/downloads/Tabella_Intercetti.pdf

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
  - [x] Fail 1st/2nd Rush and fall over
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
- [x] Foul Action
  - [x] Must select player when starting action: Prone or Stunned
  - [x] Can only foul selected player
  - [x] Assists
  - [x] Caught by the Ref
  - [x] Sent-off is a turnover
  - [x] Action end after foul
  - [x] Fouling does not take up a move
  - [x] Argue the call tests
- [x] Hand-off Action
  - [x] Only 1 hand-off pr. turn
  - [x] Move uses action
  - [x] Can cancel action if no move/handoff is done
  - [x] Can start action without having the ball
  - [x] Action ends automatically after handover
  - [x] Can hand-over after all moves are used
  - [x] Players with PA - can still hand-off
  - [x] Test against agility to catch it
  - [x] Ball bounce from target player if not caught
  - [x] Cannot hand-off to opponent
- [x] Argue the Call Table
  - [x] You're Outta Here - Bans Coach / Cannot argue again / Effect Brilliant Coaching
  - [x] I Don't Care
  - [x] Well, When You Put It Like That...
- [x] Pass Action
  - [x] 1 pass action pr turn
  - [x] Action used without throwing
  - [x] Throw without moving
  - [x] Cannot move after throw
  - [x] Throw to any square within range
  - [x] Can cancel throw part and restart it
  - [x] Quick pass
  - [x] Short pass
  - [x] Long pass
  - [x] Long bomb
  - [x] Marked modifiers to parser
  - [x] Accurate pass
  - [x] Inaccurate pass
  - [x] Wildly inaccurate pass
  - [x] Fumbled pass
  - [x] Bounce from target if not caught
  - [x] Passing Interference (players under the ruler)
    - [x] Must be standing with a tackle zone
    - [x] Opposing coach chooses player if multiple options
    - [x] Modifiers 
    - [x] Scatter if failed catch
    - [x] Run Passing Interference before going out of bounds
    - [x] Run Passing Interference on accurate pass
    - [x] Run Passing Interference on inaccurate pass
    - [x] Run Passing Interference on Wildly inaccurate pass
    - [x] Convert deflection into interception
    - [x] Deflect, but fails to catch, ball ends up on floor
    - [x] Deflect, but fails to catch, ball ends up on thrower team
    - [x] Deflect, but fails to catch, ball ends up on interceptor team
    - [x] Deflect, but fails to catch, scatters the ball out of bounds
    - [x] Passing Interference on ball going out of bounds
- [ ] Throw Team-mate Action
  - [ ] Require Throw Team-mate and Right Stuff traits
  - [ ] Uses Pass action
  - [ ] Action used without throwing
  - [ ] Throw without moving
  - [ ] Cannot move after throw
  - [ ] Throw to any square within range
  - [ ] Superb Throw
  - [ ] Successful Throw
  - [ ] Terrible Throw
  - [ ] Fumbled Throw
  - [ ] Scatter on non-fumbled throws
  - [ ] Land Superb throw
  - [ ] Land Fumbled throw
  - [ ] Land Successful throw
  - [ ] Land Land Terrible throw
  - [ ] Marker modifiers on landing
  - [ ] Land in occupied square
  - [ ] Turnover if landing on player from own team
  - [ ] No Turnover if thrown player is knocked down and not holding the ball
  - [ ] Turnover if thrown player is knocked down and is holding the ball
  - [ ] Land in occupied square with prone player
  - [ ] Crash Landing
  - [ ] Landing in the crowd

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












