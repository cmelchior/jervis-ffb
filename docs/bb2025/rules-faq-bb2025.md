# Rules FAQ - BB2025

This file describes how BB2025 rules with an element of ambiguity are 
implemented as well as the rationale behind doing it the given way.

Due to copyright restrictions, this FAQ will not directly copy sections of text
from the rulebooks but only reference them by page numbers.

See [rules-discussions-bb2025.md](rules-discussions-bb2025.md) for rules that
are not implemented yet.

* TODO: Review BB2020 version of this file and move over any relevant items.


## Rulebook

### Page 23 - The Blood Bowl Pitch
The definition of Wide Zone is unclear (bullet 2). The rules describe it with 
"from End Zone to End Zone", but does not clarify if that includes or excludes 
the End Zone.

Jervis uses the interpretation that it _does_ include the End Zone as the 
Sidelines (bullet 3) include the same wording, and those clearly run the entire
length of the pitch.

### Page 38 - Player Status!
The rulebook doesn't explicitly state that the statuses: Standing, 
Distracted, Prone, Stunned, Tackle Zones, Marked, Being Marked and Open Players
are only present when a player is on the pitch, but it is pretty clear from 
context that this is assumed.

If not, it leads to weird consequences, like being able to move a player unto 
the field when rolling High Kick.

So when interpreting the rules, Jervis will always assume that these statuses
also imply that the player is on the field.

### Page 48 - High Kick (No available players)
It is unspecified what happens if you roll High Kick and no Open players are 
available. In this case, Jervis just ignores the High Kick and nothing happens. 

### Page 48 - High Kick (Move into opponent's half)
The following scenario is not covered by the rules and thus seems legal:

Following the strict ordering of the rules, the Kick-Off Event is resolved
before "What Goes Up...". This means that the touchback rule cannot yet be 
applied when High Kick is resolved.

No-where is it stated that the high kick player cannot enter the opponent's
side. This means it is allowed to move a player into the opponent's field and
then resolve the ball coming down.

This would result in a touchback, and the ball could then be given to the
player that moved into the opponent's half.

### Page 50 - End Turn and Forgo Activation
The wording around turns is a bit unclear about whether you _must_ activate all 
players (or forgo their activation), but the wording "until they have all 
activated or a Turnover is caused" indicates this.

For this reason, Jervis does allow you to end a turn before manually activating
all players. But in that case, Jervis will automatically Forgo Activation for
all remaining players as this might trigger Stalling.

### Page 52 - Forego Activation
This section is written in a way that indicates that forgoing activating 
doesn't clear Distracted. This is based on this wording "...and so will not be
subjected to any rules that take place at the start or during their activation".

This means that if a player is Distracted and chooses to forgo their activation, 
the Distracted status will _not_ be cleared.

### Page 56 - Jumping over Players
Since players leave their square in a slightly unconventional way, the order
of checks needs to be defined. This isn't 100% clear from the rulebook, but
the following order seems correct:

1. Tentacles
2. Rush(es)
3. Jump/Leap/Pogo
4. Diving Tackle

This order is not strictly defined in the rules, and there could be an argument 
for needing to roll for Rush before Tentacles as you do not strictly move until 
the rushes succeeded. However, Tentacles uses the wording "attempt" which could 
be read as declaring the intent to Jump. 

This is also the order used by FUMBBL, so Jervis has adopted this order as well. 

### Page 58 - Rushing
The wording in the rulebook does not strictly specify that you can only Rush 
after using all normal Movement, which means that you can argue that a player 
can roll for Rush at the beginning of the turn, which could be an advantage in 
terms of positioning if it fails.

The opposite argument is that the rulebook uses the wording "..attempt to
push themselves and move a little bit further than they normally could..."; this
phrasing indicates that the roll happens after using normal movement.

The one example in the rulebook also demonstrates it happening after using the 
normal movement.

Since the BB2020 rulebook was also clear about this happening after using normal 
movement, Jervis will adopt the same interpretation for BB2025.

### Page 59 - Secure the Ball
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

The NAF has released a document with suggested interpretations of this rule
until we see a FAQ released. FUMBBL has adopted a similar interpretation.

For those reasons Jervis will also apply "pickup"-modifiers to the Secure the 
Ball roll.

### Page 62 - Both Down
If both players are Knocked Down, the rules do not specify the sequence of 
events. E.g., do we first roll for Steady Footing for each player and then 
roll Armour/Injury for each, or do we fully resolve each Knocked Down event in 
full?

There is a slight chance that choosing to use certain skills or the apothecary
on the injury depends on knowing this, but the likelyhood seems very low.

For that reason, and since it is easier to implement, Jervis will fully resolve
each Knocked Down event in full. First the Defender, then the Attacker.

### Page 62/63 - Pushed Players and Chain Pushes
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

#### Leaving the Square
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
potential infinite circle.

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
the front.

After players have moved, Jervis will then resolve Trapdoors as the players 
fully entered their target square.

Note, the rules also make a distinction between "Pushed Back" and 
"Knocked Down", these are two different things. A logical consequence of this
is that if a player leaves the field during "Pushed Back" (like when falling 
through a Trapdoor), they are not around to suffer the consequences of being 
Knocked Down afterward. Similar to what happens when being pushed into the 
Crowd. 

#### Follow-up
Given the above interpretations, we can end up in a situation, after all pushes,
where the attacker is now more than 1 square away from the defender, making a
follow-up impossible.

The rulebook does not mention this situation, it just says that the attacker
gets a free move into the square vacated by the defender (see page 63).

However, since this might be occupied by another player, that was chain-pushed
into it. Jervis will disallow follow-ups in this case.

#### Ball bouncing and Throw-in
After moving players in the push chain and the attacker follows up, balls are
now ready to resolve their final location. There are 4 kinds of situations
involving balls:

1. A player holding the ball is knocked down.
2. A player holding the ball was pushed into a trapdoor and was swallowed.
3. A player holding the ball was pushed into a crowd.
4. A player is pushed into a ball on the ground.

The order of resolving this is not defined in the rulebook and only matters
if there are multiple balls in play. But an order still needs to be defined.

The solution that would make most "sense" would be to just resolve events in
order of the push chain, but it is a somewhat arbitrary choice. Another solution
could have been by "event type", for which an order would when need to be
created.

This means:

1. Jervis resolve all locations in the push chain, starting from the defender 
   position. For each square in the chain we resolve evens in this order:
   Bounce, Trapdoor, Crowd Injury, Throw-in, Has the player scored.

2. Then resolve the attacker location, as their "follow-up" was the last move.

Note that Strip Ball is a special case. It will bounce right after following up,
but before we start resolving the push chains and before the defender is placed
prone. This means that it is technically possible for the defender to catch it
again, and then be knocked down, for it to bounce again. In the UI, we can
streamline this by only showing Strip Ball as an option for Pushbacks and not 
for POW's.

#### Touchdowns
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

#### Implementation
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
   the Push Back"), starting from the Defender's position, and ending with the 
   Attacker's. For each square we check the following:
   a. If there is a ball on the ground, it will bounce.
   b. If there is a Trapdoor, roll for it. If failed, the player will drop their
      ball (if any), be removed from play, roll injury. The ball will bounce.
      If a player leaves the field this way, they will not suffer the 
      consequences of being Knocked Down.
   c. If pushed into the crowd, roll for Injury and then Throw-in the ball.
   d. if the player is holding a ball, check for touchdown. If yes, we will 
      resolve the rest of the chain, but this player is considered the scoring
      player. Further turnovers will be ignored.

7. Knock defender down (if applicable). Roll for Armour and Injury. If they had
   the ball, it will now bounce.

### Page 69 - Foul Action and Lone Fouler
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
would also explain why it isn't described better in the rulebook. 

### Page 71 - Test for Accuracy
Rules as written make PA 1+ identical to PA 2+, which is probably not intended.
Both NAF and FUMBBL have adopted the interpretation instead:

If the Passing Ability Test is a 1 or lower after modifiers, or a natural 1, 
the Pass is Fumbled. PA1+ players pass the test on a modified 1.

So Jervis has done the same, at least until we have a FAQ released.

### Page 71 - Fumbled Pass
The BB2025 rulebook does not specify what happens if you try to pass with a 
PA stat of "-". In BB2020, this was defined as you would always fumble the ball

Since BB2025 lack any guidance on this, for now Jervis uses th BB2020 rule, i.e.
a player with PA -, can start a pass action, but the pass will always be a 
fumble.

### Page 78 - Landing
In BB2020, it was well-defined what happened if a player landed successfully on 
the ball (they were allowed to pick it up).

This wording has been removed from the rulebook in BB2025, and Pickup (page 57)
indicates that any player moving outside their activation is counting as 
involuntary movement, thus it is no longer allowed.

It isn't explicitly spelled out this way, but that is the interpretation used
by Jervis.

### Page 124 - Big Hand
Using the NAF interpretation of Secure the Ball as a "pickup". This also means
that Big Hand will work on Secure the Ball rolls.

### Page 127 - Cloud Burster
Technically, this is skill is optional, but you would only want to disable it
if you wanted the opponent to "accidentally" catch the ball, but in that case
the opponent can just decide to not intercept.

So for ease of use, this skill is always used when available.

### Page 127 - Defensive
Since both Guard and "Put the Boot In" are applied automatically (as there is
no use case for making it optional), the same reasoning applies to Defensive as 
it only disables those two skills.

If this changes for either Guard or "Put the Boot In" in the future, it should
also change for Defensive.

### Page 128 - Eye Gouge
Eye Gouge says it can trigger when an opposition player is Pushed Back, but it
is unclear if being chain-pushed also count as being Pushed Back.

For now, NAF has ruled that it does not. Thus, Eye Gouge will not trigger on
chain-pushes, but can only be used by the attacker when performing a Block.

Jervis has adopted a similar interpretation.

### Page 128 - Grab
Grrab is split into two sections: 1) Moving the pushed player and 2) Disable
Sidestep for pushed "players". It is unclear why the plural is used here.
It could either reference Multiple Block, chain-pushes, or both.

NAF has ruled that Grab canceling Sidestep only works on the blocked player(s)
and not on chain-pushed players. Jervis has adopted this interpretation
as well.

### Page 129 - Guard
While this skill is optional, it only affects how many dice are rolled and not
the final outcome. This combined with the fact that there are no scenario where
you might want to _not_ use Guard, we always apply it.

### Page 129 - Juggernaut
Juggernaut cancels Fend, Stand Firm and Wrestle for both the blocked player
and all other opponents in the push chain.

This interpretation comes from the Juggernaut skill description: "..opposition 
players cannot use..." (note plural and specifically mentioning opposition).

In particular, this means that in a Chain Push scenario, a player with 
Juggernaut will cancel Stand Firm for all opponent players, but not players on 
their own team.

### Page 130 - Kick
Jervis uses the NAF interpretation, which means that you are allowed to roll the
deviate die (D6) and then choose to treat the roll as either a D6 or D3.

In the rulebook, D3's are defined as Rolling a D6, taking half the value and 
round up.

### Page 130 - Leap
Leap make it clear that reducing the difficulty of the Leap is only allowed if
there is more than one negative modifier. However, the sequence of events when
combined with Diving Tackle is a bit unclear as Diving Tackle is applied after
all other modifiers. Does this mean that Leap can reduce the difficulty from
Diving Tackle?

For now, Jervis assumes "no", so if the Leap modifier was chosen, it will not
apply to Diving Tackle.

This means the sequence of events is:

1. Declare Leap
2. Roll for any Rushes needed
2. Choose to use Very Long Legs
3. Choose to use Leap Modifier. It will either be +1 or +0 depending on the 
   number of negative modifiers.
4. Roll / Re-roll the Leap
5. Choose to use Diving Tackle. 
6. Leap Modifier does not get recalculated. It was locked in step 3.
6. Calculate final Leap result.

This interpretation is mostly chosen because it reduces the complexity of the 
rule engine. It should be re-evaluated in case that NAF / FUMBBL / FAQ takes
the opposite stance.

### Page 131 - Mighty Blow
Mighty Blow specifically says "Whenever this player Knocks Down an opposition 
player during a Block Action...", but when you are pushed into the crowd, the 
player is never Knocked Down. This means that Mighty Blow cannot be used on
crowd injuries.

### Page 132 - Multiple Block
When using Multiple Block, it is unclear what "Both Block actions are performed 
simultaneously" means exactly? Does it apply to all steps of both blocks or only
some of them? And what actually constitutes an atomic step?

All of this is undefined in the rulebook and is very much left up to
interpretation. In this section we will try to describe the approach Jervis
has taken to resolve this ambiguity.

Jervis has taken the following approach:
1. The order of checks should match the single block case, whenever possible.
2. As many things as possible should be done in "lockstep" between the two 
   blocks.

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

1. Select the Multiple Block Special Action.

2. Select Target 1. Only a normal block is allowed.

3. Select Target 2. Only a normal block is allowed.

4. Roll block dice for Target 1.

5. Roll block dice for Target 2.

6. Choose a reroll type or accept roll for all dice against both targets. This
   is done at the same time.
   - Brawler
   - Hatred
   - Pro
   - Savage Blow
   - Team Reroll

7. Use skills that modify the final results. This is done at the same time
   for both targets.
    - Juggernaut

8. Select the final dice results for both targets. This is done at the same 
   time.

9. Choose which target to resolve block for first, now named Block A and
   Block B.

10. Resolve Block A. What happens, differs slightly depending on the block
    dice:
    - PlayerDown
        - Mark Attacker as being Knocked Down, but player is not Knocked Down 
          yet.
        - If Attacker is holding the ball, mark it as loose and bouncing, but do
          not roll for bouncing yet.

    - BothDown
        - Handle skills that trigger on Both Down. First Defender, then 
          Attacker.
        - Mark Defender/Attacker as being Knocked Down, but they are not
          Knocked Down yet. No skills that trigger on being Knocked Down are
          applied yet.
        - If Defender is holding the ball, mark it as loose and bouncing, but do
          not roll for bouncing yet.
        - If Attacker is holding the ball, mark it as loose and bouncing, but do
          not roll for bouncing yet.

    - Push Back
        - Create the Push Chain, i.e., determine were all pushed players will
          be pushed to. Using skills as appropriate.
            - Sidestep
            - Grab
            - Stand Firm
        - Do not move any players yet.
        - Do not roll any dice yet.

    - Stumble
        - Choose to use Tackle and Dodge.
        - Stumble is converted to either a Push Back or Pow and will use their
          resolution order.

    - Pow
        - Create Push Chain. See Push Back for details.
        - Mark Defender as being Knocked Down, but they are not Knocked Down 
          yet. No skills that trigger on being Knocked Down are applied yet.
        - If Defender is holding the ball, mark it as loose and bouncing, but do
          not roll for bouncing yet.

11. Resolve Block B, using the same steps as Block A. No dice have been rolled
    yet.

12. Move all Players in Push Chain A (if any).

12. Move all Players in Push Chain B (if any). Note, a Push Chain is defined by
    the squares affected and not players, so by resolving Push Chain A first,
    some players might be moved twice or end up in a position not intended
    when only looking at a single Push Chain.

13. Follow-up is not applicable when using Multiple Block.

14. Decide if Strip Ball is used against Defender A.

15. Decide if Strip Ball is used against Defender B.

16. Resolve Push Chain for Block A. Look at one square at a time, starting
    from Defender A and all the way through the Push Chain. We will check for
    touchdowns at every step, but the entire chain must still be resolved
    regardless of a touchdown being scored in the middle. For each square
    resolve events in the same order as for single blocks:.
    - Check for a loose ball. If found, it will bounce.
    - If standing on a Trapdoor. Roll to see if the player falls through.
      If Yes:
        - Roll for Injury immediately and resolve it
        - If holding a ball, resolve bouncing from the square.
        - Will not suffer the consequences of being Knocked Down (if any).
    - If pushed into the crowd:
        - Roll for Injury immediately and resolve it.
        - Throw the ball back in.
    - Check for Touchdown from the player standing in the square.

17. Resolve Push Chain for Block B. Using the same sequence as for Block A.

18. Resolve Events in Attacker's square, if not done already as part of a Push
    Chain. This concludes the Pushed Back sequence, and we can continue with the
    Knocked Down

19. Resolve skills for Block A that trigger on Knocked Down.
    - Safe Pair of Hands
    - Steady Footing
    - Saboteur
    
20. Resolve skills for Block B that trigger on Knocked Down.

21. Put Attacker and Defenders Prone as needed.

22. Roll Armour and Injury for Defender A. If injuries, do not resolve 
    completely, but add to a Defender Injury Pool instead.

23. Roll Armour and Injury for Defender B. If injuries, do not resolve
    completely, but add to a Defender Injury Pool instead.

24. Roll Armour and Injury for Attacker A, first for Block A, then for Block B.
    Injuries are added to the Attacker Injury Pool.

25. Resolve Defender Injury Pool, ie. resolve apothecary and regen both
    players at the same time

26. Resolve Attacker Injury Pool, ie. resolve apothecary and regen for both
    injuries at the same time.

27. If defenders or attackers dropped a ball as part of being Knocked Down.
    Bounce now: Defender A, Defender B, then Attacker.

27. If Attacker is still standing, holding the ball. Check for a Touchdown.

#### Other Skill Interactions
Multiple Block interacts with a number of other skills. These are listed here:

1. Taunt: Since Multiple Block prevents Follow-up, Taunt cannot be used on the
   player using Multiple Block.

2. Pile Driver: It is unclear if Pile Driver can be used on both blocks, or it 
   only activates after the Multiple Block Action is over. For now, Jervis 
   assumes that it can only be used after both blocks are resolved (i.e., once). 
   This is similar to how FUMBBL handles it.

### Page 132 - No Ball
Technically, you are allowed to user other skills like Extra Arms, but since 
it would be pointless, Jervis just ignore these skills.

### Page 134 - Put the Boot In
Technically, using the skill should be optional, but there doesn't seem to be 
any use case for this (even a bad one), so until such a use case surfaces, this 
skill is always used.

### Page 135 - Safe Pass
The skill uses the wording "Passing Ability Test", which is the same wording
used for rolling for the throw in both Pass and Throw Team-mate actions. But 
then the rest of the skill only describes what happens to the ball, indicating 
that maybe they just mean that it works for normal Pass actions.

If we allowed it to work for Throw Team-mate actions, it would open up a lot 
of unspecified scenarios with regard to how the thrown player be handled? 

For this reason, Jervis assumes that Safe Pass is only applicable for normal
Pass actions (and by extension, throwing bombs).

### Page 136 - Strip Ball
Strip Ball requires that a player is "Pushed Back" (note, this is different
than the Push Back result on the Block die). Since Stand Firm prevents a player
from being Pushed Back, this also prevents the Strip Ball skill from being used.

FUMBBL has a similar interpretation.

When Strip Ball and Sure Hands interact, the order is unclear. But it shouldn't 
matter, so to simplify the rules engine, Jervis is asking the pushed player to 
use Sure Hands, before asking the attacker to use Strip Ball.

### Page 137 - Tentacles
This skill explicitly mentions Dodge, Jump and Leap, but not Pogo, so it is 
assumed that this skill cannot affect a player using Pogo. 

### Page 138 - Very Long Legs
While this skill is optional during Interceptions, Jervis always enables it as 
the coach can just choose to not intercept instead. This lowers the complexity 
in the rule engine.

It is still optional for Catch, Leap and Jump. 

Note, that Very Long Legs does not mention Pogo, so it is assumed that this 
skill does not work if a player is using Pogo.

## Spike 19
None


## Spike 20
None
