# Rules FAQ

This file describes how rules with an element of ambiguity are implemented as 
well as the rationale behind doing it the given way.  

Due to copyright restrictions, this FAQ will not directly copy sections of text
from the rulebooks, but only reference them by page numbers.

See [rules-discussions.md](rules-discussions.md) for rules that are not implemented yet.

## Rulebook

### Page 39 - Treacherous Trapdoors
Several interactions between trapdoors and other rules are not clearly defined.
These are described in more detail in the following sections.

#### Page 57 - Trapdoors and Knocked Down Results
The trapdoor says that as soon as a player enters the square, we should roll
for the trapdoor. However, the interaction with a POW/Stumble is not
clarified. There are two options:

1. We roll for the trapdoor when pushed back, but before being Knocked Down, 
   i.e. similar to being pushed into the crowd where only the crowd injury is
   rolled.
2. The Block is considered a single "step" that needs to resolve fully before
   rolling for the trapdoor. This will potentially result in two rolls on the
   injury table.

The exact wording in the rulebook says "enter a trapdoor square for any reason",
but does not use the word "immediately" similar to Pushed into the Crowd. For
that reason Jervis adopts the second option, similar to FUMBBL.

#### Page 58 - Trapdoors and Chain-pushes
The interaction between trapdoors and chain-pushes is not described in the
rulebook. Trapdoors just mention "player enters the square", but it isn't clear
when that happens during a chain push.

This leaves two interpretations:

1. The player can fall into the trapdoor before chain-pushing the other
   player out. If they fall through, the chain just stops there. The original
   player stays on the trapdoor.
2. The check for trapdoor isn't done until after the full chain is resolved.
   Potentially leaving a "hole" in the chain.

Since the rules specify that you must choose to follow up before rolling any
dice, it seems that the likely interpretation is 2). Since this is also easier
to implement, this is the approach Jervis takes, similar to FUMBBL.

### Page 41 - High Kick and Touchbacks
Following the strict ordering of the rules, the Kick-Off Event is resolved
before "What Goes Up, Must Come Down". This means that the touchback rule cannot
yet be applied before High Kick is resolved. 

No-where is it stated that the High Kick player cannot enter the opponent's 
field (unlike for the "On the Ball" skill). So if the ball deviates back to the 
kicking teams side, Jervis will allow a player on the receiving team to move 
under the ball on the kicking teams side. 

When the ball then lands during "What goes up, must come down", a touchback is 
immediately triggered before the player moved can roll for catch. However, 
because a touchback was triggered, the player can just be awarded the ball with 
no rolls needed since the Touchback rule does not specify that the player has to
be on the receiving side.

### Page 42 - Player Activations and Actions

From the rules, it is unclear if "Activations" and "Actions" are the same
or two different things. It gets especially confusing when you mix nega-traits 
into the picture, as it becomes unclear when you regain tackle zones after
failing a nega-trait.

The first question to answer is if the activation lifecycle looks like this:

```
-> Start Activation
   -> Start Action
   ...
   -> End Action
-> End Activation
```

or this: 

```
-> Select Action (which activates the player)
...
-> End Activation (which ends the activation)
```

The rulebook uses the phrase "when you activate a player, you must declare
the action". Which can be read both ways. 

But Bone Head says "When this player is activated, ..., immediately
after declaring the action". This indicate that the first interpretation is
the correct one. This reading is also the one that allows players with Bone
Head to use Pro to reroll Bone Head after having failed in the previous 
turn, which seems to be the accepted convention. See 
https://fumbbl.com/index.php?name=PNphpBB2&file=viewtopic&t=32167&postdays=0&postorder=asc&start=0
for a discussion on this.

So Jervis has adopted the first interpretation. This also has the advantage
that it creates specific lifecycles for (new) special rules to hook into.

Note, this interpretation means that activating players go through the following
steps:

1. Select Player (mostly a UI concept).
2. Mark them as active. If standing, they now regain their tackle zones.
3. Declare an action. For some actions like Blitz or Foul, this requires
   selecting a target.
4. Roll for all Nega-traits in order, stop at the first failure (no player
   normally has multiple of these). If failed, go directly to "End Action".
   a. Bone Head: If failed, loose tackle zones, mark action as used and move to 
      "End Action" immediately.
   b. Really Stupid: If failed, loose tackle zones, mark action as used and move
      to "End Action" immediately.
   b. Unchannelled Fury: If failed, mark action as used and move to "End Action" 
      immediately.
   c. Animal Savagery: Either hit player OR loose tackle zones and move to "End
      Action" immediately.
   d. Blood Lust: If failed, they might change the declared action to Move.
5. If action has a target, roll for all opponent skills like Foul Appearance and 
   Dump Off.
6. Perform the action.
7. End Action.
8. End Activation.

### Page 49 - Resolve Pass Action in Empty Square
The full consequences of doing an accurate throw to an empty square are not 
defined, leaving it up to interpretation if the ball bounces from there or 
not.

The rules say "the ball will land in the target square" and nothing more, but 
the fluff text for bounce indicates that a ball hitting the ground will bounce. 

Both "Scatter" and "Deviate" (on page 25) also explicitly state that a bounce
will happen if the ball ends up in an empty square.

It therefore seems most likely that a bounce is intended, which is what Jervis 
does.

Also, this case is explicitly spelled out in the 2016 rulebook (page 19 - 
Bouncing Balls), where it is defined that a bounce will happen.

### Page 50 - Passing Interference and Ball Clone
The interaction between multiple balls and Passing Interference is not 
well-defined. 

For Deflection, it is only mentioned that the player must be standing with a 
tackle zone.

For Interception, it is said the player will attempt to catch the ball. Since
Ball Clone explicitly forbids this, Interception is not allowed, but it does
raise the question if Deflection is possible.

"No Hands*" explicitly says that the player cannot interfere with the ball. The
same is not the case for Ball Clone.

For that reason, Jervis allows a player with a ball to deflect a pass, but will 
automatically fail converting the deflection to an interception.

### Page 51 - Failed Catch 
The consequences of a failed catch are not defined, unlike picking up the ball
(page 46) where it is explicitly stated that the ball will bounce if the pickup
fails. 

The "fluff" description of Bounce on page 25 hints at it, but this is just the 
definition of what happens once a bounce is triggered, not what actually 
triggers the bounce.

There is probably little doubt that a bounce will happen since _something_
needs to happen and everything else makes less sense. So this is what Jervis
does.

Also, this case is explicitly spelled out in the 2016 rulebook (page 19 - 
Catching the Football), where it is defined that a bounce will happen.

See also https://www.reddit.com/r/bloodbowl/comments/1gmscfm/rule_question_if_a_player_passes_the_ball_on_an/

### Page 58 - Pushed Players and Chain-pushes
Pushbacks and the timing of other events are not well-defined in the rulebook. 
This impacts a number of things, like Trapdoors (see next section), but also 
chain pushes and the order in which we roll for bounces, throw-ins, injuries 
and check for touchdowns. It gets further complicated when you consider 
Ball Clone.

This section attempts to surface some of these problems and will define
the order implemented in Jervis.

#### Leaving the Square
The rulebook does not specify _exactly_ when a player leaves their square and
enters the next one during a chain push. 

This is a problem for e.g. Trapdoors, since Trapdoors mention "player enters 
the square". 

This leaves a number of interpretations:

1. A player leaves their square as soon as they are pushed and enters the next
   one immediately. This will e.g. trigger events like Trapdoor, which 
   contradicts the rule that you cannot roll dice before choosing to follow up.

2. A player does not leave their starting square until the full push chain is 
   determined but only registers the "intent" to move. 

3. A player declares their intent to push into another square, is considered as
   having left their starting square, but not yet "entered" the target square.
   I.e. they are in some kind of intermediate "pushed" state.

We can probably rule out option 1, immediately, which leaves option 2 and 3.

The choice has implications on circular chain pushes, because with 24 
players, chain pushes can circle back and move either the attacker or defender 
so they are no longer adjacent. With 28 players (less in Dungeon Bowl), a chain 
push can itself cause a chain push on a square that was already part of the 
push chain, causing an infinite circle.

If we treat the pushing player as not having left their square yet, then we
end up in a situation where a chain-push can push the original attacker away
from the person they are pushing themselves, which seems paradoxical.

So for that reason, we will choose option 3 as the interpretation. It is also
easier to implement.

Note, this choice does not affect the problem that a chain-push can affect a 
square that was already chain-pushed. In this case, there are two 
interpretations for player A (pusher) and player B (pushee):

1. Resolve the push chain from the front, meaning the player A moved into
   B's position and when the chain pushes reaches the square again, A is now 
   pushed in a new direction, potentially no longer adjacent to B.

2. Resolve the push chain from the back, meaning that player A might be pushed
   away from player B, and once we get to A, they will push some new player X
   rather than B. 

As we can see, using interpretation 2, raises some really weird questions as you
need to figure out if Player A was pushed away before doing their own push. 
The implications on what happens when it is their turn become really messy, 
as their push is suddenly happening in a different location from them (if it 
stays the same). And if it follows their new location, an entire new push
chain might be triggered.

For those reasons, Jervis is choosing to move players in a push chain from 
the front.

After players have moved, we will then resolve Trapdoors as the players "fully" 
entered the square.

#### Follow-up
Given the above interpretations, we can end up in a situation, after all pushes, 
where the attacker is now more than 1 square away from the defender, making a 
follow-up impossible.

The rulebook does not strictly mention this situation, but it is vaguely 
covered. On page 59, it is stated that something might prevent a follow-up, so 
in this case, we treat the chain-push as the thing that prevents it.

This seems more consistent than allowing the attacker to "teleport", which would
be further complicated as the original square might be filled by another player.

#### Ball bouncing and Throw-in
After moving players in the push chain and the attacker following up, balls are 
now ready to resolve their final location. There are 3 kinds of situations 
involving balls:

1. Defender holding the ball is knocked down.
2. A player holding the ball was pushed into a trapdoor and was swallowed.
3. A player holding the ball was pushed into a crowd.

The order of resolving this is not defined in the rulebook, and only matters
if there are multiple balls in play. But an order still needs to be defined.

The solution that would make most "sense" would be just resolve events in order
of the push chain. But would e.g. an attacker being swallowed by a trap, bounce 
their ball before the ball being thrown in from the crowd?

In this case Jervis has opted to resolve things in the order players are moved.
This means:

1. Resolve all locations in the push chain, starting from the defenders position
   This also means that bounces from normallyresolved before throwing in.
2. Then resolve the attackers location, as their "follow-up" was the last move.
   This will always be a bounce.

#### Touchdowns
When resolving the push chain, there are multiple places where a touchdown
can be detected, and the rulebook does not specify if any situation takes
precedence over another. This is probably because it only really matters
when Ball Clone is in play. But some of the questions that arise:

1. During a Push sequence, do we trigger a touchdown as soon as it is detected?
   E.g. bounces happen before we check for throw-in. So two players might be
   able to catch a ball, but it would only be the bounce that scored a 
   touchdown.

2. Do we delay checking for Touchdowns until all balls are "at rest", and if 
   yes, which order do we then check them in?

3. When checking the push-chain, do we go from the front or the back of the 
   chain?

4. If the attacker is trapped by a trapdoor, but managed to push another
   player into a scoring position. Is the touchdown scored?

For now Jervis, uses the same logic as for how chain-pushes are resolved, i.e.
we resolve the chain in the order players are moved. The player that first gets 
hold of the ball successfully will score a touchdown (and all other touchdowns 
are ignored). In the case the attacker caused a turn-over, this will be 
overridden if a touchdown is scored by another player.

This means the following:

1. After moving all pushes and choosing to follow-up and rolling for trapdoors,
   we first check the push chain, starting from the defender and ending with 
   the attacker. If any-one is holding the ball, they score a touchdown.

2. We then resolve all locations in the push chain, starting from the 
   defender's. This will all be ball bounces (if anything), since only
   the last player could end up in the crowd. A throw-in will happen if the last
   player was pushed into the crowd.

3. Last, if the attacker was trapped, their ball will finally bounce.

#### Implementation
As has been demonstrated above, the exact interpretation of timings can affect 
quite a lot of things, and none of them are clearly defined.

So for completeness, this is the order implemented in Jervis for single blocks
(Multiple Block does this slightly differently, see its section further below):

1. Roll block dice and select either Push, Stumble or POW.

2. Determine the push chain, including all relevant skills at each step, like 
   Sidestep, Stand Firm or Grab. For chain-pushes, players are considered as 
   having left their square, but does not yet count as having entered the 
   target square. No dice are rolled yet.

3. Once the push chain is determined, moves are resolved from the front, 
   starting with the defender. This might result in some players being moved 
   twice. No dice is rolled yet.

4. If a player is pushed into the crowd, this is resolved, i.e., rolling for 
   injury and choosing to use the apothecary. This is due the use of 
   "immediately" in the Pushed Into the Crowd section.

5. Choose to follow up or not. If yes, the attacker is moved.

The order of the remaining sections is not well-defined in the rulebook, so the
order is somewhat arbitrary. The only thing mentioned in the rulebook is that
you bounce the ball _after_ rolling for armour/injury.

The order does have an impact on exactly which players would be able to catch 
balls bouncing or being thrown in, and in case of multiple balls, it might 
affect who we first to check for scoring. 

The order for resolving these are mostly chosen to be comparable to how FUMBBL 
does it as it would be the least surprising to people.

6. If the defender was knocked down, roll for armour and injury and use the 
   apothecary. If they had the ball it is knocked loose, but does not bounce 
   yet. Note, in the case of Ball Clone, there will now be two balls bouncing
   from this square. In that case, the ball on the ground when the player was
   pushed there is bounced first.

7. Check for Skills affecting the ball, like Strip Ball. If triggered, the ball
   is knocked loose, but no dice is rolled yet.

8. Go through the push chain starting with the defender ending with the
   attacker. If a player is standing on a trapdoor, roll to see if they are
   removed. If yes, any ball they are holding is knocked loose. Do not roll for
   bounce yet.

9. Check if any players in the push chain is in a scoring position holding the
   ball. Going through the push chain starting from the defender and ending with
   the attacker. If a touchdown is scored, the remaining balls still need to
   be resolved (due to the possibility of exploding balls). But no other
   touchdowns are scored.

10. Now go through the push chain starting from the defender but do not include
    the attacker. For any location still on the field, bounce the ball until it
    is either caught or lands in an empty square. The first player to trigger a
    touchdown will score it (if one wasn't triggered in step 9). 

11. If the push-chain includes a player pushed into the crowd holding the ball,
    that ball is now thrown in. If a player catches it in a scoring position,
    they score a touchdown unless a touchdown was already triggered in step 9
    or 10.

12. Finally, we check if the ball in attackers location needs to bounce. If it 
    ends up being caught by a player in a scoring position a touchdown is scored
    if one is not already triggered in step 9-11.

### Page 58 - Pushed Into the Crowd and Follow-up
When pushed into the crowd, the rules specify that a player is immediately
removed and placed into the dogout. This requires rolling directly on the Injury
table. However, this conflicts with the Follow-up rule that specifies that you
need to follow up before rolling any dice.

This leads to two interpretations:

1. The player is removed from play, but the injury roll is not made until after
   the follow-up choice is made.
2. The pushed-into-crowd rule takes precedence, and you roll for crowd injury
   immediately before choosing to follow up.

There are no strong arguments for either interpretation, so the choice seems
arbitrary. However, since we chose the 2nd for chain-pushes and sinceFUMBBL 
also has chosen the second interpretation, Jervis has done the same.

### Page 74 - When to choose skill usage
The rules make it optional to use a skill before or after a roll. In Jervis, if 
possible, this choice always comes after the roll. There is no valid reason for 
asking it before, and asking both before and after would create a lot of noise. 
The exception being Diving Tackle, where there could be reasons for choosing it 
both before and after.

### Page 37 - Rolling for Weather
Rolling for the weather should be done by each coach rolling one die. This 
doesn't matter in this case, so in Jervis both dice are just rolled as a single 
step by the Home coach.

### Page 38 - The Prayers to Nuffle Table
It is unclear what happens if you roll a result that is not a duplicate, but 
cannot be applied, e.g., because the entire team has Loner. For now, Jervis just 
treats the roll as wasted. Mostly because it is easier to implement, and the 
chance of this happening is virtually zero.

### Page 40 - Nominate Kicking Player
Nominating the Kicking Player does not cover the case where you have more than 
3 players all on the Line of Scrimmage and none elsewhere. It is assumed this is 
an oversight, and in that case, nominating a player on the Line of Scrimmage is 
also allowed, but it is still only the center Line of Scrimmage.

As a side-note, it is never allowed to select a player to kick from the 
wide-zone as we are guaranteed to always have options available in the center 
Line of Scrimmage. This is slightly weird when coupled with the above, but is 
what brings us closest to the rules as written.

### Page 42 - End-of-Turn Sequence
Order of events in the End-of-Turn sequence is not well-defined, e.g., it is 
unclear if Special Play Cards like "Assassination Attempt" trigger before or 
after "Throw a Rock" and when temporary skills or abilities are removed.

For now, we choose the (somewhat arbitrary) order:
  - Prayers Of Nuffle (Throw a Rock)
  - Special Play Cards
  - Temporary Skills/Characteristics are removed
  - Stunned Players are turned to Prone

### Page 64 - Scoring During Your Opponent's Turn
The rules say that if this happens, you end their turn immediately, start your
own turn and the trigger the scoring. This is effectively the same as just
incrementing score and turn counter in the opponent's turn, except for one case.

When both turn counters are at 8, the scoring teams turn will never happen, so
they would not be able to score during their next turn.

This seems like an oversight, so Jervis is choosing an implementation where
the score counter is incremented immediately and if not at the end of the half,
the next turn is skipped.

### Page 80 - Multiple Block
When using Multiple Block, it is unclear what "Both Block actions are 
performed simultaneously" means exactly. Does it apply to all steps of both
blocks or only some of them? And what actually consititutes a an atomic 
"step"?

All of this is undefined in the rulebook, and is very much left up to
interpretation. 

Jervis has taken the following approach:

- 1. The order of checks should match the single block case.
- 2. When in doubt lean towards the FUMBBL implementation (if possible).

These two restrictions has a number of implications

#### Resolving Injuries
Resolving injuries are split into two steps:

1. We roll for injury when knocking an attacker down. But we defer
   finalizing the injury (apothecary / regen) until later. Injuries
   are either put in an Attacker or Defender Injury Pool

2. Once both attacks are done, we resolve the injury pool. First 
   defenders, then attacker. Each injury is fully resolved before
   moving to the next. 

This choice is partly backed by the rulebook. The reason being that 
"Risking Injury" (page 60) is described as "as a result of a Block 
Action" and other skills, like Pro, are not working on Regeneration 
(Designer's FAQ). This indicates that the action ends with a player 
being Knocked Down.

For that reason, while Multiple Block injuries are collected in an "Injury 
Pool", that is handled by itself at a later stage in the sequence.


#### Chain-pushes
See `Page 58 - Pushed Players and Chain-pushes` for a discussion on chain pushes
and the semantics chosen for normal chain pushes. These choices also impact 
Multiple Block, but unfortunately in quite complicated ways and none of the 
interactions are clarified by the rulebook.

Some examples:

- With 34(!!) players, a multiple block on the first target can chain-push the
  second target away from the attacker. How does this impact the second block?

- For normal blocks, you bounce the ball after choosing to follow up, but it 
  is unclear when this step happens for multiple block. After each block
  or after both blocks are resolved, and all unjuried players have left the 
  field?

Since no exact description of what "simultaneously" means exists, the
implementation has to take quite a few, somewhat arbitrary, choices. Among
others, this includes:

- If one of the targets is pushed away before the second block is resolved. The
  block is allowed to be executed at a distance. The logic being that, is that
  the block already happened. Pushback options are chosen from the targets 
  current position, similar to the Vicious Vine special ability that also allows 
  blocking at a distance.

- Similar to single blocks we have 3 phases after resolving the block result.
  These are resolved in lockstep, i.e. first Push Chain A, then Push Chain B
  1. Check for Trapdoors and handle injuries immediately.
  2. Check for touchdowns. We do not need to check the attacker, since they did
     not move as they cannot follow up.
  3. Resolve ball events and check for touchdowns. Start with all locations in 
     Push Chain A, then Push Chain B. Attacker cannot have a loose ball at 
     this stage.

#### Implementation
With the above decisions in mind, we should be as close to simultaneous as 
possible, while still allowing for some reasonable trade-offs. The full sequence
for Multiple Block then looks like this:

1. Select the Multiple Block Special Action
2. Select target 1 and type of block: Normal or special.
3. Select target 2 and type of block: Normal or special.
4. Roll block dice (or special attack dice) for target 1.
5. Roll block dice (or special attack dice) for target 2.
6. Choose rerolls or accept results for all dice against target 1 and target 2.
   This is done at the same time for both targets. 
7. Select the final dice results for both target 1 and target 2.
   This is done at the same time for both targets.
8. Choose which target to resolve block for first, now named A and B. 
9. Determine push chain for target A. This is now Push Chain A. Similar to how 
   it is done for single blocks.
10. Move all players in Push Chain A.
11. If the last player is pushed into the crowd, roll for injury and use the
    apothecary. But do not throw in the ball.
12. Determine push chain for target B. This is now Push Chain B. Taking into 
    account the adjusted positions for players moved in Push Chain A. This means 
    a player might be moved multiple times. If Grab is used, the original 
    position for the defender in Block 1, is considered occupied (as per 
    Designer's Commentary). 
13. If the last player is pushed into the crowd, roll for injury and use the
    apothecary. But do not throw in the ball.
14. Resolve the result of Block A, i.e. roll for armour/injury if needed. If an 
    injury occurs, place the player in either the Attacker or Defender Injury 
    Pool. But do not finalize the injury yet. If the player is knocked down 
    while holding a ball, it is knocked loose, but does not bounce yet.
15. Resolve the result of Block B, i.e. roll for armour/injury if needed. If an
    injury occurs, place the player in either the Attacker or Defender Injury
    Pool. The attacker can suffer two different injuries. But do not finalize 
    the injury yet. If the player is knocked down while holding a ball, it is 
    knocked loose, but does not bounce yet.
16. Resolve the Defender Injury Pool. Players must be handled in order until
    the injury is fully resolved, i.e., using apothecary, regeneration, etc.
    Once resolved, the injury is removed. Continue until the pool is empty. 
17. Resolve the Attacker Injury Pool. Each injury is fully resolved before 
    going to the next, i.e., using apothecary, regeneration, etc.
    Once resolved, the injury is removed. Continue until the pool is empty.
18. Check for Trapdoors in Push Chain A, starting from defender A. Go 
    through all squares players are pushed into. Roll for any player standing on
    a trapdoor. If they are injured because of it, resolve the injury 
    immediately.
19. Check for Trapdoors in Push Chain B, starting from the defender B. Go
    through all squares players are pushed into. Roll for any player standing on
    a trapdoor. If they are injured because of it, resolve the injury
    immediately.
20. Check for Touchdowns. Attacker cannot have scored, since they cannot have
    moved. Check all players, starting from the defender in Push Chain A, then
    in Push Chain B. If a player has scored, no other players can score as well. 
21. Resolve ball events (bounce / throw-in). Start with defender in Push Chain
    A. Then Push Chain B. If a ball lands on a player after a bounce or throw-in
    that triggers a touchdown. The remaining ball handling sequence still need
    complete, but no further touchdowns are scored.


## Death Zone

### Page 90 - No Man's Land
The area between lines of scrimmage is not named by the Death Zone rulebook. In 
Jervis, this area is named "No Man's Land".

### Page 94 - Place Kick for Kickoff
Exactly where you are allowed to place the ball for a kick is unclear.
Death Zone doesn't describe it at all, leaving it to the original rulebook. The 
problem is that it uses the term "team's half" (page 40), which has an ambiguous 
meaning in Blood Bowl Sevens.

Fortunately, the Designer's Commentary (May 2024) clarifies it a bit, allowing 
the ball to be placed "Center Field", but again, it is unclear exactly what that 
means. Since (technically) "Center Field" runs the entire length of the field 
(and doesn't include the wide zones). Even if you assume it just talks about the 
area between the two lines of scrimmage, it still doesn't clarify if you can 
place it across the entire No Man's Land zone or only on the receiving teams 
half (if you split the No Mans's Land in two).

This means RAW, you can only place it on the receiving teams half. While RAI is 
probably the entire No Man's Land zone. However, since the errata in the same 
FAQ specify that touchbacks only occur if the ball goes over the kicking teams
line of scrimmage, we use that as the strongest argument that RAI is the correct 
interpretation and this is the implementation used in Jervis.

This was also discussed here: https://www.reddit.com/r/bloodbowl/comments/18giy10/kickoff_and_touchback_in_sevens/


## Special Play Cards

### Random Event: Ball Clone
During Ball Clone, if one ball enters a square with another ball, it says that
"one ball will immediately bounce", making it unclear which ball
is bouncing:

- Is it random?
- The ball already there?
- Or the ball coming into the square?

This is a problem when combined with a failed pass that could end up in a
turnover. E.g., what happens if you throw a ball at a player, it misses and hits
a square with another ball, a ball bounces from that square and is caught by the
receiver?

This results in three questions:

a. Does it matter which ball the receiver catches?
b. If yes, which ball is bouncing?
c. Does the coach know which ball is bouncing? (Relevant for choosing to reroll
the catch)

Since all of these aspects are undefined, the implementation always lets the
last ball bounce. This solves all the questions above as well as increases the
"awesome"-factor if it actually succeeds since it prevents the turn-over.


## Almanac - 2024

###  Page 9 - Blood Lust

#### Touchdowns (Page 64 in Core Rulebook)

A touchdown is scored when a player with the ball enters the end zone, but 
Blood Lust both says that:

- "at the end of their activation, they must bite an adjacent thrall".
- "If a player who failed this roll wants to make a ..., or score, then they 
  must bite a Thrall before they ... score."

If "scoring" and "end of activation" as two separate events (which it is since a
goal only causes a turn-over, it doesn't immediately end the activation). One 
interpretation would be that you must bite two thralls in case of a scoring 
event. One to score and one when the activation ends due to the goal.

No-one will argue that this is the intended reading of the rules, which leads to 
the next question. Do we roll when scoring or when the activation ends?

At a glance, the distinction doesn't make a difference, but since some special 
rules affect scoring and end of activation, it might.

Since the Skill explicitly state that they must roll _before_ they score, we 
adopt the interpretation that in the case of a goal, Blood Lust is rolled 
immediately after the goal is detected and not when the action ends. For all 
other cases is Blood Lust still rolled at the end of activation.


## Star Players

### Maple Highgrove - Vicious Vine
Vicious Vine does not specify how pushback works for the player being pushed.

Since the block can be at a distance, it is possible to do a block that
are between a diagonal and straight ahead. And the question then is:

1. Is it treated as a corner block?
2. Is it treated as a straight ahead block?

Both choices seem equally fine, but FUMBBL has selected the first 
interpretation, i.e., anything but a straight line is a corner block. So Jervis 
does the same.
