# 2025 Skills

According to the rules, all Active skills are optional unless stated otherwise.

Since Jervis aims to be as close to the rules as possible, a similar policy is
in place in the client. However, having to decide if "obvious" skills should be
used all the time makes for an annoying and slow user experience.

For this reason, Jervis has implemented the concept of "Auto Actions". This
means that in many cases, it can automatically be decided that a skill should
be used, so the Coach is not asked, instead the Game just uses it.

It is possible to change this behavior on a per-skill basis in the settings as 
well as disable Auto Actions completely in the few cases where you might want
full control. This must be done before the event happens.

Skills that are optional, but where _not_ applying a skill does not have a use
case (either good or bad), the skill will always be used without involving the 
user or Auto Actions. E.g., Dauntless only affects the number of Block Dice 
rolled, not the final result, so Dauntless is always used.

These cases will be spelled out for the relevant skills below.

## Page 124 - Big Hand
The rules are unclear if Secure the Ball is a "pickup" or not. By extension,
this affects Big Hand that works on pickup.

Since both NAF and FUMMBL ruled that this was the case, Jervis allows the use
of Big Hand on Secure the Ball actions

!!! naf "NAF"

    Big Hand works with the Secure the Ball action.

!!! fumbbl "FUMBBL"

    House-ruled that "The secure the ball roll counts as a pickup roll. Any 
    modifiers that apply to pickup rolls apply to the secure the ball roll as 
    well."

## Page 127 - Cloud Burster
Technically, this skill is optional, but you would only want to disable it if 
you wanted the opponent to "accidentally" catch the ball, but in that case
the opponent can just decide to not intercept.

So to improve game flow, this skill is always used when available.

## Page 127 - Dauntless
While this skill is optional, it only affects how many dice are rolled and not
the final outcome. This combined with the fact that there is no scenario where
you might want to _not_ use Dauntless, Jervis always applies it.

This is similar to Guard and Horns.

## Page 127 - Defensive
Since both Guard and Put the Boot In are applied automatically (as there is
no use case for making it optional), the same reasoning applies to Defensive as
it only disables those two skills.

If this changes in the future for either Guard or Put the Boot In, it should
also change for Defensive.

## Page 127 - Diving Catch
The behavior of Diving Catch is not defined in the case of the ball landing near
multiple players with this skill. This leaves questions like, can multiple
players attempt to catch the ball? Which order do they do it in? Especially if
on different teams.

As these questions were answered in a FAQ for BB2020, Jervis adopts the same 
answers for BB2025:

2. All players with Diving Catch can choose to use the skill to attempt to catch
   the ball.
2. The order is defined by the active teams coach, including the order of
   players on the opposing team.

The +1 modifier from Diving Catch apply when "...attempting to Catch the ball
as part of a Pass Action if they are in the target square.". This means that
the Diving Catch modifier can apply multiple times, e.g., you fail the first
catch, the ball bounces around, and ends up in the target square again.

!!! bloodbowl "2020 Ruleset"

    A FAQ clarified that all players with Diving Catch can choose to catch 
    the ball, including opposing players, the order is determined by the active
    teams coach.

## Page 127 - Diving Tackle
In BB2025, Diving Tackle is always applied after all rerolls and modifiers
have been applied, not before. This is a change from BB2020, where it could
be applied both before and after rolling the die.

This has a few implications:

1. The UI should not accept "success" on a Dodge Roll automatically without
   also assuming that Diving Tackle is used.
2. Diving Tackle is applied after Leap. This means that Leap cannot reduce the
   modifier from Diving Tackle. See a more expanded discussion in the Leap
   section.

## Page 128 - Eye Gouge
Eye Gouge says it can trigger when an opposition player is Pushed Back. From
as strict RAW reading of the rulebook this would also apply to chain-pushes as :

"...the player occupying the square the originally Pushed player has been Pushed
Back into will be themselves Pushed Back as if they had been Pushed Back by the 
player who is now occupying their square...".

However, this makes chain-pushes quite awkward, and both NAF and FUMBBL have 
ruled that Eye Gouge does not work during chain-pushes, only the first block.

Jervis has adopted a similar interpretation.

!!! naf "NAF"

    When Chain Pushing a player B with the Eye Gouge skill into a third player 
    C, Eye Gouge is not applied to player C.

!!! fumbbl "FUMBBL"

    House-rule that "Eye Gouge can only be used by the active player".


## 128 - Fumblerooski
From the rules, it is unclear how Fumblerooski interacts with other skills, as
Fumblerooski will trigger on "they may choose to place the ball on the
ground in any square they move out of during their Move Action".

What does this mean when using Tentacles or Shadowing?

For now, Jervis uses the following order:

1. Check for Tentacles: If failed, the player isn't moved and Fumblerooski
   cannot be used.
2. Move Player. Fumblerooskie can now be used.
3. Shadowing/Diving Tackles are used afterward. If an opponent ends up in the
   square that was just vacated, the ball will bounce as only the active player
   can do a Pickup.

Similar questions arise for Jump and Leap as the rulebook isn't clear on exactly
where Fumblerooski can trigger. When rolling 1, the answer seems clear:
Fumblerooski cannot be used as you never leave the square. The same is true
when rolling a 6, Fumblerooski can be used in the starting square.

However, what happens on rolls where you fail the Jump, but still end up in the 
target square? Is it possible to use Fumblerooski between moving and Fall Over?

Since other skills like Steady Footing have a window there to trigger, we also
assume that Fumblerooski can. FUMBBL also allows this.

Finally, some have argued that Fumblerooskie can be interpreted as allowing you 
to place the ball in _any_ square that was vacated during the move, not just the
last one.

Apart from being a very generous interpretation and mechanically clunky, NAF
has also ruled that it only applies to the last square vacated.

Jervis has adopted the same interpretation.

!!! naf "NAF"

    The skill can only be used in the moment the square is vacated.

!!! fumbbl "FUMBBL"

    The FUMBBL Game Client allows Fumblerooski to trigger when a player falls 
    over in the target square.

## Page 128 - Grab
Grab is split into two sections: 1) Moving the pushed player and 2) Disable
Sidestep for pushed "players". It is unclear why the plural is used here.

It could either reference Multiple Block, chain-pushes, or both.

NAF has ruled that Grab canceling Sidestep only works on the blocked player(s)
and not on chain-pushed players. 

Jervis has adopted this interpretation as well.

!!! naf "NAF"

    Only the immediate Block target cannot use Sidestep.

## Page 129 - Guard
While this skill is optional, it only affects how many dice are rolled and not
the final outcome. This combined with the fact that there is no scenario where
you might want to _not_ use Guard, Jervis always uses it.

## Page 129 - Hit and Run
The interaction between Hit and Run and Multiple Block is a bit unclear as a
strict RAW could indicate that you could use Hit and Run on both blocks,
potentially moving the player twice.

However, that opens up a lot of questions with regard to the exact timing and
checking for Hit and Run constraints.

So since allowing Hit and Run twice would complicate the implementation on top
of being an extremely generous interpretation, for now Jervis will only check
for Hit and Run after both blocks in a Multiple Block have been fully resolved.

## Page 129 - Horns
While Horns are technically an optional skill, similar to Guard, there doesn't
seem to be any reason for _not_ using it.

So for that reason Jervis will always apply it.

Another question is whether Horns will automatically be applied to the 2nd block
when using Frenzy. The skill wording doesn't make this 100% clear, but given
that Horns are always applied, this doesn't matter (for now).

## Page 129 - Juggernaut
Juggernaut cancels Fend, Stand Firm, and Wrestle for both the blocked player
and all other opponents in the push chain.

This interpretation comes from the Juggernaut skill description: "..opposition
players cannot use..." (note plural and specifically mentioning opposition).

In particular, this means that in a chain-push scenario, a player with
Juggernaut will cancel Stand Firm for all opponent players, but not players on
their own team.

## Page 130 - Jump Up
While Jump Up is technically optional, Jervis will always apply it as the coach
can just choose to not use the extra movement.

## Page 130 - Kick
The behavior of Kick if unclear.

In the rulebook, the wording for Kick is "...then when kicking Deviates, this
player's Coach may choose...". This combined with a D3 being defined as
"Sometimes you will be asked to 'roll a D3'; to do this, roll a D6 and half
the value shown on the dice, rounding any fractions up."

This means it is possible to interpret it as either:

1. You must choose to roll a D3 or D6 before the roll. FUMBBL uses this
   interpretation.
2. You can choose to treat the D6 as a D3 after the roll. NAF uses this
   interpretation.

For now, Jervis uses the NAF interpretation, which means that you are allowed to
roll the deviate die (using a D6) and then choose to treat the roll as either a 
D6 or D3 after seeing the result.

!!! naf "NAF"

    The choice between a D3 or D6 is made after the roll.

!!! fumbbl "FUMBBL"

    The FUMBBL Game Client forces the player to choose between a D3 or
    D6 _before_ rolling any dice.

## Page 130 - Leap
Leap makes it clear that reducing the difficulty of the Leap is only allowed if
the negative modifier is `<= -2`. However, the sequence of events when combined 
with Diving Tackle is unclear as Diving Tackle is applied after
all other modifiers. Does this mean that Leap can reduce the difficulty from
Diving Tackle?

For now, Jervis assumes "no", so if the Leap modifier was chosen, it will not
apply to Diving Tackle.

This means the sequence of events is:

1. Declare Leap.
2. Roll for any Rushes needed.
2. Choose to use Very Long Legs.
3. Choose to use Leap Modifier. It will either be +1 or +0 depending on the
   number of negative modifiers.
4. Roll / Re-roll the Leap.
5. Choose to use Diving Tackle.
6. Leap Modifier does not get recalculated. It was locked in step 3.
6. Calculate the final Leap result.

This interpretation is mostly chosen because it reduces the complexity of the
rule engine and should be re-evaluated if new information becomes available.

!!! naf "NAF"

    Diving Tackle is used after the Dodge re-roll decision and all modifiers.

## Page 130 - Lone Fouler
Lone Fouler only works if there are no Offensive or Defensive assists. In 
many cases it would be mathematically better to have a reroll than having +1.

However, it is worth noting that following the NAF interpretation on assists,
adding available Offensive or Defensive assists is not optional. This means
that the active Coach does not get a chance to "disable" assists, because they
want to use Lone Fouler instead.

!!! naf "NAF"
    
    Offensive and Defensive Assists are not optional.

## Page 131 - Mighty Blow
Mighty Blow specifically says "Whenever this player Knocks Down an opposition
player during a Block Action...", but when you are pushed into the crowd, the
player is never Knocked Down. This means that Mighty Blow cannot be used on
crowd injuries.

## Page 132 - Multiple Block

!!! note "Note"

    Multiple Block is not fully implemented in Jervis yet, so this section
    just attempts to capture the implications of implementing it.

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

These two restrictions have a number of implications.

### Resolving Injuries
Resolving injuries is split into two steps:

1. We roll for injury when knocking a player down. But we defer
   finalizing the injury (apothecary / regen) until later. Injuries
   are either put in an Attacker or Defender "Injury Pool".

2. Once both attacks are done, we resolve the injury pool. First
   Defenders, then Attacker. Each injury is fully resolved before
   moving to the next.

In BB2020 this choice was partly backed by the rulebook. The reason being that
"Risking Injury" (page 60 - BB2020) is described as "as a result of a Block
Action" and other skills, like Pro, are not working on Regeneration
(Designer's FAQ). This indicates that the action ends with a player
being Knocked Down.

That wording is gone in BB2025, but we still use it as a guide here.

### Chain-pushes
See `Page 58 - Pushed Players and Chain-pushes` for a discussion on chain pushes
and the semantics chosen for normal chain pushes. These choices also impact
Multiple Block, but unfortunately in quite complicated ways, and none of the
interactions are clarified by the rulebook.

Some examples:

- With enough players, a multiple block on the first target can chain-push the
  second target away from the attacker. How does this impact the second block?

- For normal blocks, you bounce the ball after choosing to follow up, but it
  is unclear when this step happens for Multiple Block. After each block
  or after both blocks are resolved, and all unjuried players have left the
  field?

Since no exact description of what "simultaneously" means exists, Jervis has to
take quite a few, somewhat arbitrary, choices. Among others, this includes:

- If one of the targets is pushed away before the second block is resolved. The
  block is allowed to be executed at a distance. The logic being that is that
  the block already happened. Pushback options are chosen from the targets 
  current position, similar to the Vicious Vine special ability that also allows
  blocking at a distance.

### Implementation
With the above decisions in mind, we should be as close to simultaneous as
possible, while still allowing for some reasonable trade-offs. The full sequence
for Multiple Block currently looks like this:

1. Select the Multiple Block Special Action.

2. Select Target 1. Only a normal Block is allowed.

3. Select Target 2. Only a normal Block is allowed.

4. Roll block dice for Target 1.

5. Roll block dice for Target 2.

6. Choose a reroll type or accept roll for all dice against both targets. This
   is done at the same time. The following skills can also affect this:
    - Brawler (Both blocks)
    - Hatred (Both blocks)
    - Pro (One of the blocks)
    - Savage Blow (one of the blocks)
    - Team Rerolls (Both blocks)

7. Use skills that modify the final results. This is done at the same time
   for both targets:
    - Juggernaut

8. Select the final dice results for both targets. This is done at the same
   time.

9. Choose which target to resolve the Block for first, now named Block A and
   Block B.

10. Resolve Block A. What happens differs slightly depending on the block
    dice:
    - __Player Down__
        - Mark Attacker as being Knocked Down, but the player is not Knocked Down
          yet.
        - If Attacker is holding the ball, mark it as loose and bouncing, but do
          not roll for bouncing yet.

    - __Both Down__
        - Handle skills that trigger on Both Down. First Defender, then
          Attacker.
        - Mark Defender/Attacker as being Knocked Down, but they are not
          Knocked Down yet. No skills that trigger on being Knocked Down are
          applied yet.
        - If Defender is holding the ball, mark it as loose and bouncing, but do
          not roll for bouncing yet.
        - If Attacker is holding the ball, mark it as loose and bouncing, but do
          not roll for bouncing yet.

    - __Push Back__
        - Create the Push Chain, i.e., determine were all pushed players will
          be pushed to. Using skills as appropriate.
            - Sidestep
            - Grab
            - Stand Firm
        - Do not move any players yet.
        - Do not roll any dice yet.

    - __Stumble__
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

13. Move all Players in Push Chain B (if any). Note, a Push Chain is defined by
    the squares affected and not players, so by resolving Push Chain A first,
    some players might be moved twice: First as part of Push Chain A, and then
    as part of Push Chain B.

14. Follow-up is not allowed when using Multiple Block.

15. Decide if Strip Ball is used against Defender A. Resolve ball bouncing if
    needed.

16. Decide if Strip Ball is used against Defender B. Resolve ball bouncing if
    needed.

17. Resolve Push Chain A. Look at one square at a time, starting
    from Defender A and all the way through the Push Chain. We will check for
    touchdowns at every step, but the entire chain must still be resolved
    regardless of a touchdown being scored in the middle. For each square
    resolve events in the same order as for a single block:
    - Check for a loose ball. If found, it will bounce.
    - If standing on a Trapdoor. Roll to see if the player falls through.
      If Yes:
        - Roll for Injury immediately and resolve it.
        - If holding a ball, resolve bouncing from the square.
        - Will not suffer the consequences of being Knocked Down (if any).
    - If pushed into the crowd:
        - Roll for Injury immediately and resolve it.
        - Throw the ball back in.
    - Check for Touchdown from the player standing in the square.

18. Resolve Push Chain for Block B. Using the same sequence as for Block A.

19. Resolve Events in Attacker's square, if not done already as part of a Push
    Chain. This concludes the Pushed Back sequence for both blocks, and we can 
    continue with Knocked Down.

20. Resolve skills for Block A that trigger on Knocked Down.
    - Safe Pair of Hands
    - Steady Footing
    - Saboteur

21. Resolve skills for Block B that trigger on Knocked Down.

22. Put Attacker and Defenders Prone as needed.

23. Roll Armour and Injury for Defender A. If injuries happen, do not resolve
    completely, but add to a Defender Injury Pool instead.

24. Roll Armour and Injury for Defender B. If injuries happen, do not resolve
    completely, but add to a Defender Injury Pool instead.

25. Roll Armour and Injury for Attacker A, first for Block A, then for Block B.
    Injuries are added to the Attacker Injury Pool.

26. Resolve Defender Injury Pool, ie. resolve apothecary and regen for both
    players at the same time

27. Resolve Attacker Injury Pool, ie. resolve apothecary and regen for all 
    injuries on the Attacker at the same time.

28. If defenders or attackers dropped a ball as part of being Knocked Down.
    Bounce now: Defender A, Defender B, then Attacker.

29. If Attacker is still standing, holding the ball. Check for a Touchdown.

### Other Skill Interactions
Multiple Block interacts with a number of other skills. These are listed here:

1. Taunt: Since Multiple Block prevents Follow-up, Taunt cannot be used on the
   player using Multiple Block.

2. Pile Driver: It is unclear if Pile Driver can be used on both blocks, or it
   only activates after the Multiple Block Action is over. For now, Jervis
   assumes that it can only be used after both blocks are resolved (i.e., once).
   This is similar to how FUMBBL handles it.

## Page 132 - No Ball
Technically, you would be allowed to use other skills when attempting to pick
up the ball (like Extra Arms), but since it would be pointless, Jervis just
ignore these skills if a player has No Ball.

## Page 133 - Pile Driver
As Pile Driver says "perform a free Foul Action", it doesn't count against
the limit of declaring one Foul Action per turn.

Also, Pile Driver will activate after fully resolving the block, including
bouncing any balls the opponent might have had.

While, technically, Quick Foul can be used on a Pile Driver foul, it would not
do anything useful, so this interaction is ignored in Jervis. The reasoning
being:

1. Pile Driver puts a Player Prone and ends their Activation.
2. Quick Foul removes the "End Activation" flag.
3. Standing Up can only be used at the beginning of the action.
4. The player is prone and still "active", but there is nothing
   they can do at this point, except ending their Activation.

!!! naf "NAF"

    Pile Driver does not count towards the limit of fouls per turn.

## Page 134 - Put the Boot In
Technically, using the skill should be optional, but there doesn't seem to be
any use case for this (even a bad one), so until such a use case surfaces, this
skill is always used.

## Page 135 - Safe Pair of Hands
The skill requires an adjacent unoccupied square. If one isn't available, this
skill cannot be used.

## Page 135 - Safe Pass
The skill uses the wording "Passing Ability Test", which is the same wording
used for rolling for the throw in both Pass and Throw Team-mate actions. But
then the rest of the skill only describes what happens to the ball, indicating
that maybe they just mean that it works for normal Pass actions.

If we allowed it to work for Throw Team-mate actions, it would open up a lot
of unspecified scenarios with regard to how the thrown player be handled?

For this reason, Jervis assumes that Safe Pass is only applicable for normal
Pass actions (and by extension, throwing bombs).

## Page 136 - Sprint
Sprint is an optional skill, but for most cases, there is no downside to just
applying it automatically.

The only exception is during a Frenzy Blitz. In this case, the coach might not
want to use Sprint to prevent Frenzy from triggering a 2nd block. 

For those reasons Jervis will apply Sprint automatically. Except in the Blitz
case (with the player having Frenzy), then the Coach will be asked if Sprint
should be used.

## Page 136 - Stab
Stab is not available when starting from prone as it is an Active skill that
requires a tackle zone when activating the player. Doing a Blitz-Stab from Prone
will still work as the player will be standing by the time the Block is
performed.

!!! fumbbl "FUMBBL"

    Stab is an Active Trait, which can not be used when Prone.

## Page 136 - Strip Ball
Strip Ball requires that a player is "Pushed Back" (note, this is different
from the Push Back result on the Block die). Since Stand Firm prevents a player
from being Pushed Back, this also prevents the Strip Ball skill from being used.

FUMBBL has a similar interpretation.

When Strip Ball and Sure Hands interact, the order is unclear. But it shouldn't
matter, so to simplify the rules engine, Jervis is asking the pushed player to
use Sure Hands, before asking the attacker to use Strip Ball.

!!! fumbbl "FUMBBL"

    Strip Ball requires the Push Back and will therefore not trigger

## Page 137 - Take Root

### Take Root and Diving Tackle
The interaction between Take Root and Diving Tackle is unclear as Take Root
says that Rooted is removed when "Placed Prone", and Diving Tackle says
"...place this player Prone...".

This leaves it open to interpretation if you can use Diving Tackle on a player
that is Rooted. Some arguments:

1. For: FUMBBL is currently allowing Diving Tackle to be used.
2. For: The definition of Placed Prone does not specify anything about where it
   happens.
3. Against: Take Root says "... may not leave their current square for any
   reason..", and Rooted is not cleared until _after_ the player is in a
   different square.
4. For/Against: The FAQ for BB2020 made it clear that both Wrestle and Pile Driver
   could be used when Rooted. Diving Tackle was not mentioned.

While a somewhat arbitrary choice, Jervis will be using the FUMBBL
interpretation.

!!! fumbbl "FUMBBL"

    The Game Client allows Diving Tackle to be used on Rooted players.

!!! bloodbowl "2020 Rules"

    FAQ Wording: "Can a player that has been Rooted as per the Take Root
    trait use a skill such as Pile Driver or Wrestle in order to place
    themselves Prone and therefore no longer be Rooted?"

### Take Root and Declaring Actions
Take Root only says you are not allowed to "Perform" Move Actions. This
means it is still allowed to "Declare" them, but the only thing you can do in
that case is just end the action immediately.

## Page 137 - Timmm-ber!
It is unclear why Timmm-ber! wants to mention that it only works on MA 2 or
less, since that is a requirement for even rolling for Standing Up.

For now, Jervis assumes that this sentence can be ignored and doesn't put extra
constraints on the skill. I.e., any player that rolls for Standing Up can use
Timmm-ber!.

Technically, Timmm-ber! is an optional skill, but since there is no use-case for
not applying it, Jervis will apply it automatically.

## Page 137 - Tentacles
This skill explicitly mentions Dodge, Jump and Leap, but not Pogo, so it is
assumed that this skill cannot affect a player using Pogo.

## Page 138 - Trickster
The exact timing of Trickster is a bit unclear in the Block Sequence, especially
in relation to Foul Apparenace.

Both use the wording "...an opposition player attempts to perform a Block Action
against this player...", but Trickster also has an additional "...Before
determining how many dice are rolled...".

While an argument could be made that Trickster triggers before Foul Appearance,
it also feels "weird". So for this reason, Jervis will check for Foul Appearance
and only if passed, can Trickster be used. FUMBBL has a similar interpretation.

!!! fumbbl "FUMBBL"

    The Game Client rolls for Foul Appereance before allowing the use of Trickster.

## Page 138 - Very Long Legs
While this skill is optional during Interceptions, Jervis always enables it as
the coach can just choose to not intercept instead. This lowers the complexity
in the rule engine.

It is still optional for Catch, Leap, and Jump.

Note that Very Long Legs does not mention Pogo, so it is assumed that this
skill does not work if a player is using Pogo.
