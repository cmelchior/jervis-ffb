# Jervis Probability Score

The Jervis Probability Score (JPS) is a way to compare completed solutions to a
Jervis Challenge. It answers a deliberately narrow question:

> How likely is it to achieve, at least, the chosen action sequence, 
> including rerolls available along this path?

In particular, it does not measure "outcome", nor how clever the solution was, 
how difficult it was to find, or how optimal it is. It says nothing about the 
state of the pitch, how many players are in tackle zones, or whether the 
opponent can score next turn.

> Example:
> 
> Goal: Player X must catch a thrown ball this turn.
> 
> Solution: An action sequence demonstrating Player Y throwing an inaccurate 
> pass that scatters 3 times and still ends up landing on the target player 
> who catches it. 

JBS will only calculate the demonstrated sequence and not include the 
probability of an accurate pass that was caught directly. So JBS tells you 
nothing about the probability of the outcome, just the probability of reaching
the outcome using the given sequence.

The score is 0.0–100% and higher is better.

## The idea in plain language

Every chance event (dice roll) in the sequence contributes some chance of 
success.

JPBS combines those chances into one number. A solution with many risky
rolls will generally score worse than a solution that reaches the same goal
with safer rolls.

The existing dice interface is intentionally unchanged. JPS uses the die value 
selected by the coach as the evidence of how demanding that event was. It 
does not ask the coach to select a separate “this is what I wanted the
roll to do” option.

Example: If the coach selects a 5 where a 3+ is enough. JPS treats this as a 
5+ roll.

This has two consequences:

1) If a coach intentionally or accidentally selects a wrong value, the score 
   will be lower than it could be. E.g., if a 4 was selected on a dodge roll 
   that only required 3+. This will force players to learn the underlying rules
   and improve their understanding of the game to know what they need to roll.

2) It handles modifier skills cleanly as JPS expects the coach to take these 
   into account when selecting the dice value. This approach has the 
   advantage that it can re-use the existing dice UI rather than invent a new 
   one just for challenges.

### Blocks

Blocks use the face that the coach actually selected. This avoids asking which
block result the coach considered a success. A face that appears on more than
one side of a block die is naturally more likely than a face with only one
side. With several dice, the scorer uses the chance that the selected face is
available in the pool (or that every die shows it when the opponent chooses
the result).

This means that the chance for a successful block is generally 
pessimistic as it doesn't take into account e.g., Tackle overriding Dodge, which
would make Stumble equal to POW, or if not, equal to "Push Back".

### Pass / Throw Team-mate

Rolls which outcome doesn't succeed / fail, like Accuracy rolls, are still 
scored as X+ rolls. E.g. If a player wanted an inaccurate pass as the 
"success" criteria, JPS will include the "probability for in-accurate + 
probability for accurate", but this is not a valid in-game state, as 
accurate throws behave differently than in-accurate ones.

This means that trying to calculate the probability for this definition of 
success is not possible and the reported value will the higher than the 
actual probability.

This is only a problem for targets that aim to land in a bracket below the 
"best". Accurate rolls will score using the correct probability.

### Scatter, Throw-in

These are scored based on the probability of hitting the target location. 
This means:

- Scatter: The dice selected, determine the landing spot. JPS will treat all
  combinations of scatter dice hitting the same target as success. If the ball
  ends up out-of-bounds, only rolls that leave the pitch from the same square
  as the selected sequence.

- Throw-in: The dice selected, determine the landing spot. JPS will treat all
  combinations of throw-in dice hitting the same target as success. Going 
  shorter or longer is a failure.

### Armour, Casualty, and Injury Rolls

Casualty and Injury rolls are rolls that include modifiers and the result 
is a non-linear set of outcomes. This makes it hard for JPS to determine the 
probability.

Similarly to Accuracy rolls, we just treat the selected value as a minimum 
value, and higher value will also be considered a success. This also means 
that the score for Injury rolls is higher than the "real" value, since e.g
DEAD will score in the same bucket as choosing to rolling Badly Hurt.

However, the default behavior for JPS is to ignore these rolls in the 
scoring, so it only matters in scenarios where it is explicitly enabled.

### Rerolls

Rerolls are part of the probability, but they are handled by a fixed, 
predictable policy rather than by trying to search every possible variation.

The policy gives priority to:

1. A standard skill reroll.
2. A team reroll that does not need to pass a Loner roll.
3. Pro or a Loner-gated reroll, depending on which has the better chance
   of being activated. Pro wins an exact tie.

If Pro or Loner fails their activation roll, it stops there. The policy does
not attempt to recover from it by using a team reroll on the activation roll.

There are two ways (or modes) a challenge can record rerolls

The challenge’s configured scoring policy determines which of the modes is
used. Scores from different modes or policy versions cannot be compared against
each other.

The modes are:

#### Fixed-Reroll scoring

This is the simplest mode. An initial roll and a reroll used in the 
sequence are treated as one logical event, using the final selected die value. 
The probability calculator then applies the fixed reroll policy to the 
alternative branch of that event. This makes the result independent of which 
reroll button happened to be clicked during the game.

#### Actual-Reroll-Choice scoring

This hybrid mode also records the actual (physical) choices made by the coach, 
and is the default mode:

- The initial die and the final reroll are separate events.
- The exact reroll selected by the coach is consumed.
- A fresh die that was accepted without a reroll can still receive the fixed
  policy’s hypothetical recovery chance.
- A die that was actually rerolled cannot receive another hypothetical
  recovery.
- Activation rolls, such as Pro and Loner, are recorded in chronological order.

This means that a questionable reroll choice is visible in the score. For
example, choosing a 2 and then rerolling it produces two physical events. A
selected natural 1 contributes no dice risk, but it still consumes the chosen
reroll, affecting future rolls. 

Similarly, choosing a 2, where a 3 is required, is a failure in the eyes of 
the rules, but the value will be used as success when scoring the sequence.

Actual rerolls are consumed even when their activation fails. Resources needed
by later demonstrated rerolls are reserved from earlier hypothetical choices
so the recorded physical trace remains scoreable.

Some examples where this behavior might be counter-intuitive:

Consider three logical 3+ tests by the solving team, with one team reroll
available. The recorded physical results are:

1. Roll a 2, use the team reroll, and roll a 3.
2. Roll a 3 and accept it.
3. Roll a 3 and accept it.

There are four physical D6 results here, but only three logical tests. Under
Actual-Reroll-Choice scoring, the first test contributes both physical dice.
For a real 3+ test, a success is 3–6 (4/6) and a failure is 1–2 (2/6).

The three scoring interpretations are:

1. **Fixed-Reroll scoring**

   The first roll and its reroll are collapsed into one logical test using the
   final value, 3. All three logical tests are therefore treated as 3+ tests.
   With no failure, the chance is `(4/6) × (4/6) × (4/6) = 8/27`.

   One failure can be recovered. There are three possible positions for that
   failure, giving `3 × (2/6) × (4/6) × (4/6) × (4/6) = 8/27`.

   **Total: `8/27 + 8/27 = 16/27 = 59.26%`.**

2. **Hybrid-Reroll scoring with the optimistic selected-value mapping**

   This is the current hybrid convention. It scores the selected initial 2
   as if it represented a 2+ success, so that result contributes 5/6. The
   rerolled 3 and the two later 3s each contribute 4/6.

   **Total: `(5/6) × (4/6) × (4/6) × (4/6) = 20/81 = 24.69%`.**

   This is an approximation, not the real probability of the failed 3+ roll.
   The team reroll was actually used, so it is consumed, and neither later
   test can receive a hypothetical recovery.

3. **Hybrid-Reroll scoring with the real success/failure mapping**

   For the literal physical trace, the initial 2 belongs to the real failure
   range, 1–2, and therefore contributes 2/6. The rerolled 3 and the two later
   3s are successes in the 3–6 range and each contributes 4/6.

   **Total: `(2/6) × (4/6) × (4/6) × (4/6) = 8/81 = 9.88%`.**

The totals are therefore:

| Scoring interpretation | Total probability |
|---|---:|
| Fixed reroll | **16/27 = 59.26%** |
| Hybrid reroll, optimistic 2+ mapping | **20/81 = 24.69%** |
| Hybrid reroll, real 1–2 failure mapping | **8/81 = 9.88%** |

This difference is by design, as the coach can choose the same sequence as
"Fixed" for a better score. The optimistic 2+ mapping is used for UX reasons, as
using the real failure mapping would require massive UI changes compared to a 
real game, which is undesirable. Also, since the optimistic mapping will always
result in a lower value than the optimal case, it being in-accurate is 
acceptable.


## How solutions are ranked

The final Probability Score is the total probability of the entire recorded 
sequence. 

An unsupported or incomplete chance event makes the result unscored rather
than silently assigning it an optimistic estimate.

-----

## Technical Highlevel Architecture

The statistical information required to calculate JPS is gathered and processed
in multiple places:

1. `GameController.statistics` must be configured with a `GameStatistics()` 
   class. This class is responsible for keeping all statistical data gathered
   during a game.

2. When running a game, all "chance events" (dice rolls, coin toss) must create
   a `ChanceObservation`. This class contains all the metadata needed to reason
   about the roll, including success probability and available rerolls. 
   
   This information is saved using `AddChanceObservation` or 
   `UpdateChanceObservation` commands.

3. The list of observations can be accessed from 
   `GameStatistics.diceProbabilities`. 

4. Before scoring a list of actions, the observations must be normalized 
   (collapsing roll and rerolls). This is done using the `ChanceNormalizer`.
   The normalizer can be configured to use different policies with regard to 
   how it interprets available rerolls. The normalizer will turn the list of 
   observations into a list of `ActionPathEvent`.

5. This normalized list can be scored using an `ActionPathScorer`, which again
   can have different policies configured with regard to how the probabilities
   are calculated. Calling `ActionPathScorer.score()` will return the final
   calculated JPS value as a `ProbabilityScoreResult.Scored` or 
   `ProbabilityScoreResult.Unscored` if the value could not be calculated.

   Available rerolls are treated as a shared resource that is distributed 
   optimally using dynamic programming. To avoid this step being to costly, 
   the normalizer will choose which reroll to use at each step using a 
   pre-configured priority list, rather than calculating the optimal choice each
   time.

Usage Example:

```kotlin 
// Start game
val stats = GameStatistics()
val controller = GameEngineController(state = gameState, statistics = stats)
// ... Run actions through controller.handleAction()

// Score current state using default configured normalizer
val score: ProbabilityScoreResult.Scored = PhysicalActionPathScorer.score(
    rules = gameState.rules,
    observations = stats.diceProbabilities.observations,
    solvingTeam = gameState.homeTeam
)
println(score.successProbability)

// Score current state using a custom normalizer
val normalizer = ChanceNormalizer(FixedRerollUsageNormalizerPolicy)
val actionEvents = normalizer.normalize(stats.diceProbabilities.observations)
val score: ProbabilityScoreResult.Scored = PhysicalActionPathScorer.scoreNormalized(
    rules = gameState.rules,
    events = actionEvents,
    solvingTeam = gameState.homeTeam
)
println(score.successProbability)
```

-----

## Statistical reference

This section describes the implementation for readers who want the exact
definition.

### Surprisal and JBS

For a line with final assigned probability $P$, the score is its surprisal:

\[
\mathrm{JPS} = -\log_2(P)
\]

Equivalently, a score of $r$ bits corresponds to probability

\[
P = 2^{-r}.
\]

For independent events, surprisal is additive. The scorer nevertheless uses a
forward resource-state calculation, because reroll resources can be shared
by multiple events.

For a successful D6 event, the selected value is read as follows:

| Selected value | Assigned chance | Surprisal
|---:|---:|---:|
| 1 | 6/6 | 0.0 bits |
| 2 | 5/6 | 0.26 bits |
| 3 | 4/6 | 0.58 bits |
| 4 | 3/6 | 1.00 bit |
| 5 | 2/6 | 1.58 bits |
| 6 | 1/6 | 2.58 bits |


### Event probabilities

For the fixed-reroll scorer, let $v$ be the final selected D6 value and
let $s$ indicate whether the engine observed the event as a success:

\[
p(v,s) =
\begin{cases}
(7-v)/6 & \text{if } s=\text{success}\\
v/6 & \text{if } s=\text{failure}
\end{cases}
\]

For the hybrid actual-choice scorer, every physical selected D6 value uses:

\[
p(v) = (7-v)/6
\]

regardless of whether the rules engine classified that particular die as a
success or failure. This is the convention that makes the selected value the
sole D6 difficulty signal. Activation dice and physical reroll dice contribute
to the demonstrated physical probability; only primary dice contribute to the
hybrid primary probability.

For a block face with single-die probability $f$ and a pool of $n$ dice:

\[
p_{\text{block}} =
\begin{cases}
1-(1-f)^n & \text{when the rolling coach selects the result}\\
f^n & \text{when the opponent selects the result.}
\end{cases}
\]

The selected face is the result being scored, not a reconstructed tactical
intent.

### Risk breakdown

The hybrid scorer exposes three related probabilities:

- **Primary probability**: primary D6 events and block events only.
- **Demonstrated probability**: all physical D6 events and block events,
  before hypothetical recovery.
- **Final probability**: the result after actual reroll consumption and
  hypothetical recovery branches.

The displayed risk components are:

\[
R_{\text{primary}}=-\log_2(P_{\text{primary}})
\]

\[
R_{\text{actual}}=-\log_2(P_{\text{demonstrated}})-R_{\text{primary}}
\]

\[
R_{\text{hypothetical}}=-\log_2(P_{\text{final}})
 -[-\log_2(P_{\text{demonstrated}})]
\]

\[
\mathrm{JPS}=R_{\text{primary}}+R_{\text{actual}}+R_{\text{hypothetical}}
\]

In fixed-recovery mode, primary and demonstrated probability are the same,
so the actual-roll adjustment is zero. The recovery adjustment is the change
from the base line probability to the fixed-policy final probability.

### Recovery recurrence

For a solving-team event with demonstrated probability $p$, alternative
probability $1-p$, recovery activation probability $a$, and a recovery
resource that is available, the fixed policy contributes:

- $p$ without spending the resource;
- $(1-p)ap$ when the alternative branch occurs, the recovery activates, and
  the reroll returns to the demonstrated branch.

An activation failure is terminal for that recovery in version 1. There is no
fallback recovery after a failed Pro activation.

For an opponent-owned event, the fixed policy treats recovery adversarially:
the opponent may spend the selected recovery on the demonstrated branch. The
surviving factor is:

\[
p((1-a)+ap)
\]

The first term is activation failure retaining the demonstrated result; the
second is successful activation followed by another demonstrated result.

### Resource state and scope

The scorer’s dynamic-programming state tracks resources that can affect later
events. Each physical team-reroll token is distinct. Skill and other resources
are keyed by their reset scope: action, activation, turn, drive, half, or game.
Reusable resources do not add a consumed key.

The rules setting controlling multiple team rerolls per turn is applied
separately from token identity. When the setting disallows multiple team
rerolls, using one also marks that team turn as used. When it allows multiple
rerolls, separate available tokens may be used later in the same turn.

After each event, state keys that no later event can use are removed before
equal states are merged. This keeps the calculation bounded without changing
the result. If the configured state ceiling is exceeded, the challenge can
still be completed, but its score is returned as unranked.
