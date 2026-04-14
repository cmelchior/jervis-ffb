# 2025 Base Rules

This page documents any ambiguities and decisions made by Jervis when 
implementing the base rules for the 2025 ruleset.

## Page 23 - The Blood Bowl Pitch
The definition of Wide Zone is unclear (bullet 2). The rules describe it with
"from End Zone to End Zone", but does not clarify if that includes or excludes
the End Zone.

Jervis uses the interpretation that it _does_ include the End Zone as the
Sidelines (bullet 3) include the same wording, and those clearly run the entire
length of the pitch.

## Page 38 - Player Status!
The rulebook doesn't explicitly state that the statuses: Standing,
Distracted, Prone, Stunned, Tackle Zones, Marked, Being Marked and Open Players
are only present when a player is on the pitch, but it is pretty clear from the
context that this is assumed.

If not, it leads to weird consequences, like being able to move a player unto
the pitch when rolling High Kick.

So when interpreting the rules, Jervis will always assume that these statuses
also imply that the player is on the pitch.

## Page 48 - High Kick

## No available players
It is unspecified what happens if you roll High Kick and no Open players are
available. 

In this case, Jervis just ignores the High Kick result and continues with the 
kick-off sequence.

## Move into opponent's half
The following scenario is not covered by the rules and thus seems legal:

Following the strict ordering of the rules, the Kick-Off Event is resolved
before "What Goes Up...". This means that the touchback rule cannot yet be
applied when High Kick is resolved.

No-where is it stated that the High Kick player cannot enter the opponent's
side. This means if the ball deviates into the opponents half, it is allowed to
move a player into the opponent's side before resolving the ball coming down.

When the ball comes down, it would result in a touchback, and the ball could 
then be given to the player that moved into the opponent's half.

## Page 50 - End Turn and Forgo Activation
The wording around turns is a bit unclear about whether you _must_ activate all
players (or forgo their activation), but the wording "until they have all
activated or a Turnover is caused" indicates this.

For this reason, Jervis does allow you to end a turn before manually activating
all players. But in that case, Jervis will automatically Forgo Activation for
all remaining players as this might trigger Stalling.

## Page 52 - Forego Activation
This section is written in a way that indicates that forgoing activating
doesn't clear Distracted. This is based on this wording "...and so will not be
subjected to any rules that take place at the start or during their activation".

This means that if a player is Distracted and chooses to forgo their activation,
the Distracted status will _not_ be cleared.

## Page 54 - Move Action
There are a number of skills that can interact with a Move Action, but the order
in which skills are checked and used is not defined in the rulebook.

For now, Jervis is using the following order as that seems the most
reasonable (for a suitable definition of "reasonable"):

1. Declare intent to move player.
2. Tentacles: If they succeed, the Move Action stops before the player is moved.
3. Move the player into the target square.
4. Fumblerooski
5. Rush if needed
   a. Sprint
   b. Sure Feet
6. Dodge if needed
   a. Stunty* / Titchy*
   b. Two Heads
   c. Break Tackle
   d. Prehensile Tail
   e. Diving Tackle
7. Shadowing

## Page 54 - Standing Up
The rules for standing up state that: "If a player with a Move Allowance
Characteristic of 2 or less (regardless of any modifiers) wishes to stand up..."

But it is unclear what modifiers actually apply? Is e.g., a Dodgy Snack a
modifier?

For now, when checking if MA < 3, Jervis will only take into account base MA 
plus any adjustments with aduration of PERMANENT (e.g. stat increases). 

This means that a Mummie (MA 3) with Dodgy Snack, does not roll to stand up.

## Page 56 - Jumping over Players
Since players leave their square in a slightly unconventional way, the order
of checks needs to be defined. This isn't 100% clear from the rulebook, but
the following order seems correct:

1. Tentacles
2. Rush(es)
3. Jump/Leap/Pogo
4. Diving Tackle

This order is not strictly defined in the rules, and there could be an argument
for needing to roll to Rush before Tentacles as you do not strictly move until
the rushes succeeded. However, Tentacles uses the wording "attempt" which could
be read as declaring the intent to Jump.

This is also the order used by FUMBBL, so Jervis has adopted this order as well.

!!! fumbbl "FUMBBL"

    The Game Client checks for Tentacles before rolling for Rush.

## Page 58 - Rushing
The wording in the rulebook does not strictly specify that you can only Rush
after using all normal Movement, which means that you can argue that a player
can roll for Rush at the beginning of the turn, which could be an advantage in
terms of positioning if the roll fails.

The opposite argument is that the rulebook uses the wording "..attempt to
push themselves and move a little bit further than they normally could..."; this
phrasing indicates that the roll happens after using normal movement.

The one example in the rulebook also demonstrates it happening after using the
normal movement.

Since the BB2020 rulebook was also clear about this happening after using normal
movement, Jervis will adopt the same interpretation for BB2025.

!!! naf "NAF"

    You may Rush only after regular movement.

!!! bloodbowl "2020 Rules"

    Page 44 says "And the end of the player's movement, declare that they will
    Rush..."


## Page 59 - Secure the Ball
It is unclear if modifiers like "Pouring Rain" apply to securing the ball.

Arguments for:
* Secure the Ball counts as "Picking up the ball" as described on page 57. This
  is true as the rule references pickup through "must attempt to pickup the
  ball" and "A player attempting to pick up the ball".
* This means that things like Pouring Rain and Marks are taken into account.
  So even though it isn't an agility test, it is the same as rolling for someone
  with AG 2+.

Arguments against:
* The wording "they will automatically pick up the ball", indicates that this is
  not a "pickup"-roll, but a "secure-the-ball"-roll with the successful pickup
  being a side effect.
* This implies that modifiers applying to Pickup do not also apply to Secure the
  Ball. Since no modifiers explicitly mention Secure the Ball, it is always a
  unmodified 2+ roll

Both NAF and FUMBBL are using the first interpretation, so Jervis has adopted
the same.

!!! naf "NAF"

    The -1 modifier from Pouring Rain applies to Secure the Ball.

## Page 62 - Both Down
If both players are Knocked Down, the rules do not specify the sequence of
events. E.g., do we first roll for Steady Footing for each player and then
roll Armour/Injury for each, or do we fully resolve each Knocked Down event in
full?

There is a slight chance that choosing to use certain skills or the apothecary
on the injury depends on knowing this, but the likelihood seems very low.

For that reason, and since it is easier to implement, Jervis will fully resolve
each Knocked Down event in full. First the Defender, then the Attacker.

## Page 62/63 - Pushed Players and Chain Pushes
Pushbacks and the timing of other events are not well-defined in the rulebook.
This impacts a number of things, like trapdoors, bouncing balls, throw-ins and
scoring. It gets further complicated when you consider Ball Clone (which is not
yet available in BB2025, but it might be in the future) or environmental hazards
like those found in Dungeon Bowl.

The only rules we have from the rulebook that can act as guidelines are these:

- Follow-up: Choose Follow-up before rolling any dice.
- Pushed Players: If pushed into the ball, it will bounce.
- Pushed into the Crowd: Roll for Injury, then Throw-in the ball.
- Trapdoor: When entering a square for any reason, remove from play similar to
  being pushed into the crowd. The ball will be dropped on the ground and
  bounce.
- POW: After the Push Back result has been applied, the target player is
  immediately Knocked Down.

This section will attempt to surface some of these gray areas and the rationale
Jervis has taken to resolve them.

### Leaving the Square
The rulebook does not specify _exactly_ when a player leaves their square and
enters the next one during a chain push.

This is a problem for e.g. Trapdoors, since Trapdoors mention "player enters
the square".

This leaves a number of interpretations:

1. A player leaves their square as soon as they are pushed and enters the next
   one immediately. This will trigger events like Trapdoor, but will also
   contradict the rule that you cannot roll dice before choosing to follow up.

2. A player does not leave their starting square until the full push chain is
   determined but only registers the _intent_ to move. They occupy their
   starting square until the end of the push chain.

3. A player declares their intent to push into another square, is considered as
   having left their starting square but not yet "Entered" the target square.
   I.e. they are in some kind of intermediate "Pushed" state.

We can probably rule out option 1 immediately, which leaves options 2 and 3.

The choice has implications on circular chain pushes, because with 22
players, chain pushes can circle back and affect the attackers starting square
again. With 28 players (less in Dungeon Bowl), a chain push can result in a
chain push on another square that was already part of the push chain, causing an
infinite circle.

If we treat the pushing player as not having left their square yet, then we
end up in a situation where a chain-push can push the original attacker away
from the person they are pushing themselves, which seems paradoxical.

So for that reason, Jervis will choose option 3 as the interpretation. It is
also easier to implement.

Note; this choice does not fix the problem that a chain-push can affect a
square that was already chain-pushed. In this case, there are two
interpretations for player A (pusher) and player B (pushee):

1. Resolve the push chain from the front, meaning the player A moved into
   B's position and when the chain pushes reaches the square again, A is now
   pushed in a new direction, potentially no longer adjacent to B.

2. Resolve the push chain from the back, meaning that player A might be pushed
   away from player B, and once we get to A, they will push some new player X
   rather than B.

As we can see, using interpretation 2, raises some really weird questions as you
need to figure out if Player A was pushed away before resolving their block.
The implications for what happens then become really messy, as their push is
suddenly happening in a different location from them (if it stays the same).
And if it follows their new location, an entire new push chain might be
triggered.

For those reasons, Jervis is choosing to move players in a push chain from
the front. This also seems more "logical".

After players have moved, Jervis will then resolve Trapdoors as the players
fully entered their target square.

Note, the rules also make a distinction between "Pushed Back" and
"Knocked Down", these are two different things. A logical consequence of this
is that if a player leaves the pitch during "Pushed Back" (like when falling
through a Trapdoor), they are not around to suffer the consequences of being
Knocked Down afterward. Similar to what happens when being pushed into the
Crowd. This is a different interpretation than the one used in FUMBBL.

!!! fumbbl "FUMBBL"
    
    In the case of Trapdoors, the Game Client will roll for two injuries, one
    for the trapdoor and one for being Knocked Down.

### Follow-up
Given the above interpretations, we can end up in a situation, after all pushes,
where the attacker is now more than 1 square away from the defender, making a
follow-up impossible.

The rulebook does not mention this situation, it just says that the attacker
gets a free move into the square vacated by the defender (see page 63).

However, since this might be occupied by another player, that was chain-pushed
into it. Jervis will disallow follow-ups in this case. This is also consistent
with the behavior in BB2020.

!!! bloodbowl "2020 Rules"
    
    On page 59, it is stated "At other times, a player may be prevented from
    following-up, even if they wanted to...".

### Ball bouncing and Throw-in
After moving players in the push chain and the attacker follows up, balls are
now ready to resolve their final location. There are four kinds of situations
involving balls:

1. A player holding the ball is knocked down.
2. A player holding the ball was pushed into a trapdoor and was swallowed.
3. A player holding the ball was pushed into a crowd.
4. A player is pushed into a ball on the ground.

The order of resolving this is not defined in the rulebook and only matters
if there are multiple balls in play. But an order still needs to be defined.

The solution that would makes "most sense" would be to just resolve events in
order of the push chain, but it is a somewhat arbitrary choice. Another solution
could have been by "event type", for which an order would when need to be
created.

This means:

1. Jervis resolves squares in the push chain, one at a time, starting from 
   the defender position. For each square in the chain we resolve evens in this 
   order: Bounce, Trapdoor, Crowd Injury, Throw-in, Check for Touchdown.

2. Then resolve the attacker location, as their "follow-up" was the last move.

Note that Strip Ball is a special case. It will bounce right after following up,
but before we start resolving the push chains and before the defender is placed
prone. This means that it is technically possible for the defender to catch it
again, and then be knocked down, for it to bounce again. In the UI, we can
streamline this by only showing Strip Ball as an option for Pushbacks and not
for POW's.

### Touchdowns
When resolving the push chain, there are multiple places where a touchdown
can be detected, and the rulebook does not specify if any situation takes
precedence over another. This is probably because it only really matters
when Ball Clone is in play. But some of the questions that arise:

1. During a Push sequence, do we trigger a touchdown as soon as it is detected?
   E.g. bounces happen before we check for throw-in. So two players (bounce and
   throw-in) might be able to catch a ball. Does the playing catching the bounce
   then immediately score a touchdown?

2. Do we delay checking for Touchdowns until all balls are "at rest", and if
   yes, which order do we then check them in?

3. When checking the push-chain, do we go from the front or the back of the
   chain?

4. If the attacker is trapped by a trapdoor, but managed to push another team
   player into a scoring position. Is the touchdown scored or is it a Turnover?

For now, Jervis uses the same logic as for how chain-pushes are resolved, i.e.,
we check for Touchdowns in the order the push chain is resolved. The player that
first gets hold of the ball successfully will score a touchdown (and all other
touchdowns are ignored). In the case the attacker caused a turn-over, this will
be overridden if a touchdown is scored by another player.

These choices are somewhat arbitrary, but seem to make thematically "sense".

This means the following:

1. We resolve all ball-events at each step in the push chain, starting from the
   defender's position and ending with the attacker's.

2. The player during this that catches the ball in a scoring position will
   score a touchdown. Even though we still resolve the rest of the chain, which
   might also include a normal turnover being triggered.

### Implementation
As has been demonstrated above, the exact interpretation of timings can affect
quite a lot of things, and none of them are clearly defined.

So for completeness, this is the order implemented in Jervis for single blocks
(Multiple Block does this slightly differently, see its section for more
details):

1. Roll block dice and determine the result.

2. Determine the push chain, including all relevant skills at each step, like
   Sidestep, Stand Firm or Grab. For chain-pushes, players are considered as
   having left their square, but does not yet count as having fully entered the
   target square. No dice are rolled yet.

3. Once the push chain is determined, moves are resolved from the front,
   starting with the defender. This might result in some players being moved
   twice, including the attacker which can end up no longer adjacent to the
   defender. No dice is rolled yet.

4. Choose to follow up or not. If yes, the attacker is moved.

5. Strip Ball trigger (if applicable). That ball will bounce now, while the
   defender is still Standing.

6. Resolve all events in the push chain (we treat them as part of "applying
   the Push Back"), starting from the Defender's position and ending with the
   Attacker's. For each square we check the following:
   a. If there is a ball on the ground, it will bounce.
   b. If there is a Trapdoor, roll for it. If failed, the player will drop their
      ball (if any), be removed from play, roll injury. The ball will bounce.
      If a player leaves the pitch this way, they will not suffer the
      consequences of being Knocked Down.
   c. If pushed into the crowd, roll for Injury and then Throw-in the ball.
   d. if the player is holding a ball, check for touchdown. If yes, we will
   resolve the rest of the chain, but this player is considered the scoring
   player. Further turnovers will be ignored.

7. Knock defender down (if applicable). Roll for Armour and Injury. If they had
   the ball, it will now bounce.

8. Knock attacker down (if applicable). Roll fo Armour and Injury. If they
   had the ball, it will now bounce.

9. If the Attacker has the ball, check if a touchdown is scored.

## Page 64 - Blitz Action
Technically, selecting the target of the Blitz is part of declaring the action.
This has an impact when paired with nega-traits as the correct ordering would
be:

1. Activate Player
2. Declare Blitz
3. Select Target
4. Roll for Negatraits, like Bone Head

However, this sequence complicates an otherwise nice split between Activating 
Players and Performing the action. So since it has no practical impact, Jervis 
has changed the order to be:

1. Activate Player
2. Declare Blitz
3. Roll for Negatraits, like Bone Head
4. Resolve Blitz, including selecting the target as the first step.

## Page 69 - Foul Action and Lone Fouler
It is ambiguous what happens if you reroll a natural double in a Foul Action
with Lone Fouler.

Does the fouler still get sent off or not?

The Re-rolls section on page 33 is no help here either as it doesn't mention
what happens to the original role.

Arguments for:
* Being Sent-off uses the wording "...if during a Foul Action a natural double
  is rolled..", with no indication that rerolling removes consequences of the
  first roll.
* The wording is "rolled" not "outcome of the roll" (or similar).

Arguments against:
* In all other cases, e.g., Rush, you do not fall over if you re-roll a 1.
* The word "re-reroll" implies that the first roll is not counted.

Jervis has adopted the 2nd interpretation, as that is most in line with what
people would assume a re-roll does (entirely replacing the first roll). This
would also explain why it isn't described better in the rulebook. NAF
has a similar interpretation,

!!! naf "NAF"

    The player is not Sent-off for a double that is then rerolled.


## Page 71 - Test for Accuracy
Rules as written make PA 1+ identical to PA 2+, which is probably not intended.

Both NAF and FUMBBL have adopted the interpretation instead:

If the Passing Ability Test is a 1 or lower after modifiers, or a natural 1,
the Pass is Fumbled. PA1+ players pass the test on a modified 1.

So Jervis has done the same.

!!! naf "NAF"

    If the Passing Ability Test is a 1 or lower after modifiers, or a natural 1, 
    the Pass is Fumbled. PA1+ players pass the test on a modified 1.

!!! fumbbl "FUMBBL"

    This is clearly game-breaking, and FUMBBL will ignore the limits on the modifiers. Rolls
    can be modified outside the 1-6 range.
    
    Modified pass rolls of 1 or lower will be fumbles unless the player has PA1+ in which
    case a modified 0 or lower will be a fumble


## Page 71 - Fumbled Pass
The BB2025 rulebook does not specify what happens if you try to pass with a
PA stat of "-". In BB2020, this was defined as you would always fumble the ball

Since BB2025 lacks any guidance on this, Jervis uses the BB2020 rule.

!!! bloodbowl "2020 Rules"
    
    Page 29 says "However, if the player has a PA of '-', or if the roll is a 
    natural 1, the test is 'Fumbled'."

## Page 78 - Landing

### Picking up the Ball
In BB2020, a player landing successfully on the ball was allowed to pick it up.

This wording has been removed from the rulebook in BB2025, and Pickup (page 57)
specifies that a player moving outside their activation cannot pick up the ball.

!!! bloodbowl "2020 Rules"

    Page 46 says "If a player voluntarily moves into a square in which the ball
    is placed, they must attempt to pick it up."

    Page 54 says "If the Agility test is passed, ..., the thrown player will 
    land safely and is considered to have moved voluntarily."

!!! bloodbowl "2025 Rules"
    
    Page 57 says "If a player is ever involuntarily moved into a square 
    containing the ball, ..., they may not attempt to pick up the ball and it 
    will bounce."

### Throwing a Stunned Player
Stunned players can be thrown but will automatically fail their Agility Test
and Fall Over.

However, it is unclear what happens if the Armour Roll doesn't break Armour. Does
the player stay Stunned or reverts to a Prone state?

Falls Over (page 40) says: "...When a player Falls Over, place a Prone Token 
next them and then make an Armour Roll..."". A RAW interpretation could indicate
that the player then becomes Prone.

Stunned (page 39) says: "At the end of of each team's turn, any players on 
their team that started that turn Stunned will automatically roll over...". This
could indicate that it isn't possible to remove a Stunned effect during the
turn, although it isn't spelled out.

As it is both unintuitive, unclear and mechanically clunky to allow a Stunned
player to become Prone this way, Jervis has adopted the interpretation that if a 
Stunned Player is either Knocked Down or Falls Over during a turn, and Armour
isn't broken, they remain Stunned rather than reverting to Prone.

## Page 97 - New Skills
When gaining new skills through advancements, it is illegal to get the same
skill twice.

However, it isn't specified if this can happen in other cases.

In BB2020, there were a few cases where this could happen using Special
Play Cards, but the interaction wasn't specified and mostly involved skills with
modifiers like "Loner (4+)" or "Mighty Blow(+2)". In those cases, it was assumed
that for negative effects, the lower number was chosen (e.g., Loner (3+) over 
Loner (4+)) and for positive effects, the higher number was chosen (e.g., 
Mighty Blow(+2) over Mighty Blow(+1)). However, you were not allowed to use the 
skill twice.

Right now, this is a hypothetical scenario in BB2025, as Special Play Cards are
not available, and Prayers to Nuffle has wording that prevents this scenario.

So for now, Jervis will throw an error if Players end up having the same skill
twice.

## Page 144 - Team Mascot

### Loner
Loner uses the wording "Whenever this player wishes to use a Team Re-roll...".
Mascot uses the wording "...whenever the team wishes to use this Team
re-roll ".

Which makes it unclear which order you check, and how they impact each other.

For now, Jervis uses the (somewhat arbitrary) order:

1. First we check for Loner. If failed, the Mascot re-roll is marked as used,
   without rolling for it as we treat the roll as par of the reroll.
2. If Loner is passed, we then roll for Mascot.

### Reroll Mascot Roll
Technically, it would be allowed to use a Team Reroll to re-roll the Mascot Roll,
but since the rules specify that failing the mascot role will allow you to do
that anyway. There doesn't seem to be any use case for this, except complicating
the re-roll logic.

For this reason, Jervis does not allow using team rerolls on the Mascot Roll,
but any applicable skill (not Pro) can be used.

### Team Captain
It is slightly unclear how the Team Mascot reroll should be treated in 
combination with Team Captain.

Jervis treats the "4+ to work" as part of the re-roll, so if Team Captain
keeps the Mascot reroll. The Coach must still roll the next time it is used.

This is consistent with the NAF interpretation.

!!! naf "NAF"

    If a Mascot reroll is free (independent of success) due to Team Captain, 
    the 4+ roll has to be repeated next time the Mascot reroll is used.

## Page 155 - Team Captain

A few edge cases are unclear about how to handle Team Captain.

### Loner
Loner describes "...the Team Re-roll is lost just as if have been used". Jervis
interprets this as meaning that Team Captain can roll to prevent rerolls lost
this way.

### Pro
The Pro skill only works on rolls that are taken on behalf of the Player. Team 
Captain rolling is not considered such a roll, as it is taken for the reroll 
itself. 

For this reason, Pro cannot be used to reroll Team Captain rolls. 

!!! naf "NAF"

    The Team Captain cannot use Pro to reroll the Team Captain roll.

### Promotion
It is unclear what happens if a player with Pro already gets promoted to
Team Captain.

1. Do they gain Pro twice?
2. If Pro was gained through an advancement roll, is the price reduced?
3. Do they just get the "Team Captain", designation, but otherwise keep
   their normal Pro?

Of these, the only thing Jervis prevents is someone having Pro twice. Otherwise,
none of the questions are answered in the rulebook, but since all of them
are related to "team building", and cannot happen in-game, Jervis does not have
an opinion. This means that Jervis will leave it up to the Team Roster provider
to handle this.

This also means that Jervis supports a Team Captain not having Pro at all.
