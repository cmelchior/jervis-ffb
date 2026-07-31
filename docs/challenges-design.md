# Jervis Challenges

This document describes how "Challenges" or "Puzzles" could be made available
inside Jervis FFB.

## Design Goals

- Teach people to recognize optimal patterns of play
- Teach people to be creative in solving on-pitch problems
- Give advanced players a chance to explore advanced play.
- Introduce newer players to more advanced concepts.
- Aspirational content for single-player play.
- Something that can bring the community together.

## Technical Foundation

The rules engine is already decoupled from the UI. All input actions, including
dice rolls can be created programmatically. This allows for a lot of 
flexibity with regard to how to treat dice rolls. Undo is also supported, which
can improve the UX inside a challenge (undo an action rather than having to 
restart the entire thing).

We might want to surface success probabilities as part of a "score" or just
as extra information to the user. Currently, these values are not exposed 
from the engine and will need to be added somehow. This will require some 
experimentation to figure out the best way to do this. This is not critical
to shipping an MVP of this feature.

## UI 

At a high level it should behave like this:

- There is a "Challenges" or "Puzzles" button on the front page. This button 
  should lead to a list of available puzzles.
- At the top of the challenges page should be checkboxes for each category we 
  want to have, including a "Hide solved" checkbox.
- Below is the list of challenges with the following information:
  - Star (to remember it)
  - Name (of the puzzle)
  - Author (who created it)
  - Category (label)
  - Trophy icon (faded in when solved, faded out when not solved)
  - Rating (Users can rate each challenge +1 or -1) 
- When clicking a challenge, it should go to a "details" page with the following
  information:
  - Same information as the list view
  - Description: Describe what the goal is
  - Rules: Any rules being applied, like number of turns, how dice rolls are 
    handled etc.
  - A upvote/downvote button
  - Also a scoreboard for people that already solved it. It should be scored
    by the chance of success. An extension would be clicking on one scoreboard
    to replay that solution.

### Game UI

For the Game UI, if we want to calculate probabilities for each roll we most 
likely cannot reuse the existing dice wheels, instead we need to show 
"outcome"-buttons. E.g. for Armour it should be ["Break", "Not Break"], for 
blocks it becomes ["Attacker Down", "Opponent Down", "Opponent Pushed", 
"Opponent Pushed + Down"]. This needs to be investigated further.

### Editor UI

In an ideal world, puzzles should be created by the community. For this to 
work we need a Puzzle Editor. 

- Dev Mode can be re-used for this as it gives people the option of changing 
  the board state independently of what is actually allowed.
- Dev Mode is not fleshed out, so a lot of functionality is probably missing 
  to fully support this.  
- The Serialization format is still not stable. A public editor doesn't make 
  a lot of sense until it is.
- Some options to control dice rolls will also need to be added, e.g. either 
  a predefined list of rolls or some options for controlling it. This can 
  probably turn rather complicated and will need further design.
- Configuring a "Goal" might also be tricky. Some easy ones are "score", more
  complicated ones are "Let player X block Player Y using 2 Block Dice"

  
## Design

### Categories

Each challenge should be given a category for easier filtering. We should start 
with only one, more can be added later

Potential Categories:
    -- One-Turn-Touchdowns: Score when starting from Line-of-Scrimmage
    -- Scoring: Score a touchddown from the current board position
    -- Blocking: Block a given opponent with a set number of dice
    -- Break the Cage: Get a number of blocks against an opponent in a cage 
       or somehow free the ball
    -- Crowd Surfing: Surf a specific player

### Restricting options:

How to approach making the challenge interesting:

1. Unfold over multiple turns

This is probably infeasible. Even with a good AI, it would be impossible for
puzzle authors to design a consistent and comparable experience.

2. Restrict dice rolls

   a. Provide a predetermined list of dice values. Blood Bowl 2 does this.

      Against: While it might create an interesting puzzle, it doesn't translate
      well into playing real games.


   b. Restrict dice to certain values, like all 1D Block skull, all 
      2D-blocks roll POW. This can vary from puzzle to puzzle. Some examples:

      - Assume that all 1d and 2d (your favor) blocks will result in pushes. 
        All 2d blocks in opponent’s favor will result as double skulls.
      - Ensure every 1d non-block action attempt is a 2+ (i.e. passes, catches, 
        hand-offs, dodges, all should be 2+ for success). Assume successful 
        rolls.
      - Ensure every 1d non-block action attempt is at most 3+ risk (i.e. 
        passes, catches, hand-offs, dodges, all should be 2+ or 3+ for success).
        Assume successful rolls.
      - Assume tentacle rolls are passed and that you can dodge out.
      - Assume Really Stupid (2+) succeeds, (4+) fails. Assume all other 1d6 
        action rolls fail.
      - Don’t sacrifice ball security! You cannot have an easy blitz to your 
        ball carrier by the High Elf team at the end of your turn. If any 
        blitz is possible, a high elf player who chooses to blitz your ball 
        carrier must require at the least multiple 3+ dodges to reach the ball 
        carrier, preferably a 2d block your favor.
      - Assume the kickoff lands off the field, so you can give directly to 
        your ball carrier.
      - Assume you win a reroll during the kickoff.


      Against: Will be a tricky to create an editor for this. 
    
      For: Does force you to think about "failure"-cases and guide towards 
      optimal play.

      For: Might be a way to avoid having to do full-scope propabilit 
      calculations.


3. Track Probability

   Each roll gets assigned a probability. The higher it gets at the end the 
   better.

   For: I asked about on the FUMBBL discord. Most people seem to prefer 
   something like this. It would also guide people towards optimal solutions.

   Against: This only works if we can assign probabilities to each roll, 
   especially for Blocks this is hard to do automatically and will most 
   likely require a new kind of UI.


### Setting the Goal

Each challenge should have a specific goal, and it should be possible to 
define this goal in the Challenge Editor. Currently, exactly how much 
customization is needed here is unclear, so for now this is just a list of 
known use cases. More might appear. Until they have been fleshed out in a 
bit more detail, we should keep challenges defined by code, rather than a UI

- Score a goal: The active team must score at the end of the turn

- Block player with X Dice: A target player must be blocked by X dice (or 
  any number of dice)
  - a) Block can be done by any player
  - b) Block must be done by a specific player

- Push X players off the pitch.
  - a) Push specific player(s) off the pitch

### Puzzle Types

- Maximize Probability: Roll values are selected. The aim is to reach the 
  goal with as high a probability as possible.
- Dice List: The Coach is given a list of dice rolls. The aim is to use them in
  order, while still reaching the goal.
- Dice Pool: The Coach is given a pool of rolls. They can be used in any order
  to reach the goal, but the lower the dice used, the better.

### Scoring the Puzzle

How to approach "scoring" a puzzle:

1. Done / Not-Done

The simplest is just checking if a solution was found.

2. Probability-of-success

Combine all dice rolls into a final probability. The higher it is, the better.
While this would be a nice way to measure it, there are problems with it. E.g.
how do you assign probabilities across block dice, rerolls and skills that 
modify the dice roll. Most likely it will require changes to the UI as just
selecting dice values (as during normal games), might not provide enough 
information to calculate "success"-state. This needs further investigation.

3. Number of dice rolls

Counting the number of dice rolls, lower is better.

For: This can be useful when fixing the dice values, e.g. all pushes.

Against: Sometimes rolling more dice can be better if the target
number is lower or there are rerolls for them.

4. Number of actions

Counting the number of game actions, lower is better.

Counter-argument: This is mostly an optimization that will have little effect
on actual gameplay.

5. Time-to-completion


6. Date-of-completion

Just list people in the order they completed a puzzle.

Against: Only relevant if no better scoring.
Against: Does not facilitate thingin 

## Sources

- https://fumbbl.com/help:OTTTestingInClient
- https://bbtactics.com/strategy/articles/challenges/


# Design Suggestion 1

Lets rephrase the problem a little bit.


What I want is the probability of success given the _current_ chain of events.

The chain of events are a dice roll type + the wanted outcome (e.g. success/failure).

Modifiers exists for all of these rolls. Some modifiers can reroll all dice types, some modifers can only reroll a single type, others can reroll all rolls of a given type and finally some modifers can either add +1 or -1 to the roll affecting the chance of success.

What would the best approach be to show "chance of success" for the sequence


Worked for 54s

The best approach is to model the sequence as a dynamic program over rolls and remaining modifier resources.

You cannot generally calculate each roll independently and multiply the results, because shared modifiers couple the rolls: using a reroll now changes the probability of later rolls.

1. Represent each event

Each event describes the roll and what outcome the sequence requires:

data class RollEvent(
val type: RollType,
val outcomes: List<RollOutcome>,
val wanted: (RollOutcome) -> Boolean,
)

For a d6 target roll:

val dodge = RollEvent(
type = RollType.DODGE,
outcomes = (1..6).map { RollOutcome(it, 1.0 / 6.0) },
wanted = { it.value >= 3 },
)

A wanted failure simply reverses the predicate:

wanted = { it.value < 3 }
2. Represent modifiers uniformly

Each modifier needs:

Which roll types it applies to.
Its effect.
When it can be used.
Its usage scope.
sealed interface ModifierEffect {
data object Reroll : ModifierEffect
data class Adjust(val amount: Int) : ModifierEffect
}

enum class UsageScope {
ONCE_PER_SEQUENCE,
ONCE_PER_ROLL,
UNLIMITED,
}

data class Modifier(
val id: ModifierId,
val applicableTypes: Set<RollType>?,
val effect: ModifierEffect,
val scope: UsageScope,
)

Examples:

Modifier(
id = ModifierId.TEAM_REROLL,
applicableTypes = null, // All types
effect = ModifierEffect.Reroll,
scope = UsageScope.ONCE_PER_SEQUENCE,
)

Modifier(
id = ModifierId.DODGE_REROLL,
applicableTypes = setOf(RollType.DODGE),
effect = ModifierEffect.Reroll,
scope = UsageScope.ONCE_PER_ROLL,
)

Modifier(
id = ModifierId.BREAK_TACKLE,
applicableTypes = setOf(RollType.DODGE),
effect = ModifierEffect.Adjust(+1),
scope = UsageScope.ONCE_PER_SEQUENCE,
)

Here, ONCE_PER_ROLL represents something available on every applicable roll but usable only once on that individual roll.

3. Track the calculation state
   data class State(
   val eventIndex: Int,
   val remainingUses: Map<ModifierId, Int>,
   val usedOnCurrentRoll: Set<ModifierId>,
   )

When moving to the next event:

fun State.nextEvent() = copy(
eventIndex = eventIndex + 1,
usedOnCurrentRoll = emptySet(),
)

Consumable modifiers are stored in remainingUses. Reusable per-roll modifiers only need to be tracked in usedOnCurrentRoll.

4. Evaluate before and after the roll

You need two functions:

beforeRoll: enumerate possible die results.
afterRoll: choose the best legal response to a result.

Conceptually:

V(i,R)=
d
∑
​

P
i
​

(d)D(i,d,R)

Where D evaluates the available decisions after rolling d.

For a given result:

D(i,d,R)=max
⎩
⎨
⎧
​

accept the result
apply a modifier
reroll
​

Accepting
accept(i,d,R)={
V(i+1,R),
0,
​

if d is wanted
otherwise
​

Rerolling

A reroll returns to the same event with the modifier consumed or marked used:

reroll(i,R,m)=V(i,consume(R,m))
Applying +1 or -1

The modifier changes the result and reevaluates it:

adjust(i,d,R,m)=D(i,d+Δ
m
​

,consume(R,m))
5. Kotlin-shaped implementation
   fun evaluateSequence(
   events: List<RollEvent>,
   modifiers: List<Modifier>,
   initialResources: Resources,
   ): Double {
   val cache = mutableMapOf<EvaluationState, Double>()

   fun beforeRoll(
   eventIndex: Int,
   resources: Resources,
   usedThisRoll: Set<ModifierId>,
   ): Double {
   if (eventIndex == events.size) return 1.0

        val state = EvaluationState.BeforeRoll(
            eventIndex,
            resources,
            usedThisRoll,
        )

        return cache.getOrPut(state) {
            val event = events[eventIndex]

            event.outcomes.sumOf { outcome ->
                outcome.probability * afterRoll(
                    eventIndex = eventIndex,
                    outcome = outcome,
                    resources = resources,
                    usedThisRoll = usedThisRoll,
                )
            }
        }
   }

   fun afterRoll(
   eventIndex: Int,
   outcome: RollOutcome,
   resources: Resources,
   usedThisRoll: Set<ModifierId>,
   ): Double {
   val state = EvaluationState.AfterRoll(
   eventIndex,
   outcome,
   resources,
   usedThisRoll,
   )

        return cache.getOrPut(state) {
            val event = events[eventIndex]

            var best = if (event.wanted(outcome)) {
                beforeRoll(
                    eventIndex = eventIndex + 1,
                    resources = resources,
                    usedThisRoll = emptySet(),
                )
            } else {
                0.0
            }

            for (modifier in legalModifiers(
                event,
                outcome,
                modifiers,
                resources,
                usedThisRoll,
            )) {
                val nextResources = resources.consumeIfNeeded(modifier)
                val nextUsed = usedThisRoll + modifier.id

                val candidate = when (val effect = modifier.effect) {
                    ModifierEffect.Reroll ->
                        beforeRoll(
                            eventIndex = eventIndex,
                            resources = nextResources,
                            usedThisRoll = nextUsed,
                        )

                    is ModifierEffect.Adjust ->
                        afterRoll(
                            eventIndex = eventIndex,
                            outcome = outcome.adjust(effect.amount),
                            resources = nextResources,
                            usedThisRoll = nextUsed,
                        )
                }

                best = maxOf(best, candidate)
            }

            best
        }
   }

   return beforeRoll(
   eventIndex = 0,
   resources = initialResources,
   usedThisRoll = emptySet(),
   )
   }

In real code, beforeRoll and afterRoll would usually be local mutually recursive functions or methods on an evaluator.

What the displayed percentage means

Using maxOf(...) means the displayed result is:

The maximum probability of completing the sequence, assuming modifiers are used optimally after every observed result.

That is probably the most useful “chance of success” value.

You may also want to display:

Base chance: no optional modifiers.
Optimal chance: modifiers used optimally.
Recommended action: for the current result, which modifier produces the highest continuation probability.
Important distinction

Modifiers that always apply and require no decision can be folded directly into the roll:

effectiveResult = rawResult + passiveModifiers.sum()

Modifiers that are optional, consumable, or shared must remain in the DP state.

Only when every roll has completely independent, dedicated modifiers can you reduce the calculation to:

P(sequence)=
i
∏
​

P(wanted outcome for roll i)

With shared rerolls or once-per-turn modifiers, use the state-based calculation.

How expensive is the DP algorithm?

Let:

N: number of events in the sequence.
F: maximum number of distinct outcomes per roll, typically 6.
M: number of modifier types.
u
j
​

: remaining uses of consumable modifier j.
K: number of reusable modifiers that may each be used once on the current roll.

The number of resource configurations is:

R=
j
∏
​

(u
j
​

+1)

If every consumable modifier is simply available/unavailable, this becomes:

R=2
C

where C is the number of distinct consumable modifiers.

Tracking which reusable modifiers have already been used on the current roll adds up to:

2
K

configurations.

Therefore, the approximate number of memoized states is:

O(N⋅F⋅
j
∏
​

(u
j
​

+1)⋅2
K
)
​


Each state may inspect up to M legal modifier actions, giving time complexity:

O(N⋅F⋅M⋅
j
∏
​

(u
j
​

+1)⋅2
K
)
​


Memory complexity is approximately:

O(N⋅F⋅
j
∏
​

(u
j
​

+1)⋅2
K
)
​

Typical case

Suppose you have:

10 rolls.
6 outcomes per roll.
3 single-use shared modifiers.
2 reusable once-per-roll modifiers.
5 possible modifier actions.

Then:

10⋅6⋅2
3
⋅2
2
=1,920

memoized states, with at most five actions checked per state. That is very small.

Even:

20 rolls.
10 single-use modifier flags.
4 per-roll modifier flags.

produces approximately:

20⋅6⋅2
10
⋅2
4
≈2000000

states. That is more significant, especially if recalculated continuously in the UI.

The main source of cost

The sequence length is not normally the problem. The exponential factor comes from the number of independently tracked modifier states:

2
C+K

For example, ten independent one-use skills create 1,024 possible availability combinations.

Important optimization: group equivalent resources

If you have three identical team rerolls, do not track three booleans:

teamReroll1Available
teamReroll2Available
teamReroll3Available

Track one count:

teamRerollsRemaining: Int // 0..3

This produces four states instead of eight:

3+1=4

rather than:

2
3
=8

In general, identical resources with u uses contribute u+1, not 2
u
.

Other optimizations

Only include a modifier in the state if its availability can affect a future decision. Passive modifiers do not belong in the resource state.

Canonicalize resources so that equivalent modifier configurations produce identical cache keys. Also filter modifiers by roll type before evaluating actions, and cache the result for the entire current chain until the chain or available resources change.

Because rerolls consume or mark a resource before returning to the same roll, the state continually moves toward fewer available actions. This prevents infinite recursion and makes the calculation effectively a directed acyclic state graph.

For a Blood Bowl-style sequence with perhaps 5–20 rolls and a handful of shared rerolls and once-per-turn skills, exact DP should normally be practical. The difficult cases arise when many independent modifiers can be combined in arbitrary orders.