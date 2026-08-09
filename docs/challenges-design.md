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
- Should not require extensive changes to the UI. It should still feel like 
  you are playing a normal game of blood bowl (minor changes would be fine)

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

### Terminology

- Action Path: The sequence of game actions that make up a solution to a 
  challenge. Any setup actions (for bringing the game to the starting state)
  are not counted in this.
- Chance Event/Observation: A game action that contains an element of chance. 
  E.g., dice roll or coin flip. 
- Structured data/event/obeservation: A Chance Observation that contains 
  information so we can reason about its success or not.
- Unstructured data/event/observation: A Chance Observation, where the only 
  thing we know is the value rolled.
- Finalized Roll: A roll are finalized when the outcome is accepted, either
  directly or after using a reroll.
- Unfinalized Roll: A roll that are not "done" yet, e.g. the outcome still 
  needs to be selected or dice rerolled.
- Physical die: A die that has been rolled by the coach (or the value chosen)
- Logical die: A die that has not been rolled but its result determined by a 
  probability policy.  
- Demonstrated Path: The game actions in a sequence that describes a certain 
  path through the game state.

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

In an ideal world, we should not have to change the UI much when playing 
puzzles. However, depending on the exact implementation. Some changes might 
be required.

#### UI for Calculating Probabilities

In the case, we want to calculate the exact probability for a sequence there 
is no way around UI changes as we need to know the "intent" of every action 
as we must be able to determine "success". This is especially problematic 
for Block rolls, where the sides map to outcomes in unpredictable ways 
depending on which skills are being used. It probably also means that we 
cannot show actual dice rolls anywhere for the same reason. Since these 
changes would be very invasive, this approach is probably not suitable to 
pursue.

See "Blood Bowl Difficulty Rating" below for the way around this. That design
is still probability-based and needs the same per-roll inputs, but it sidesteps
the intent problem for Blocks by scoring the face the coach actually used
rather than asking what they were aiming for. That needs no UI change.

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

A more generalized goal api consist of a Goal + Modifiers:


<Action> against <TargetPlayer>
    - By X
    - (for block) Dice required 1/2/3
    - (offensive assists required) for block/foul
    - (defensive assists required) for block/foul

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

This is the option we are pursuing. See "Blood Bowl Difficulty Rating" below,
which answers the block dice and reroll questions raised here.

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

This requires a server to be the final authority.

6. Date-of-completion

Just list people in the order they completed a puzzle.

Against: Only relevant if no better scoring.
Against: Does not facilitate thingin 

## Sources

- https://fumbbl.com/help:OTTTestingInClient
- https://bbtactics.com/strategy/articles/challenges/
- https://www.reddit.com/r/bloodbowl/comments/kl3fe4/blood_bowl_puzzles_can_you_find_the_solutions/
- https://github.com/BloodBowlDave/BloodBowlActionCalculator

# Challenge system: Postgres schema (Supabase)

First draft: Needs more work

## Context

The challenge system exists in the engine but has no persistence: `Challenge` is built locally
from `SampleChallenges`, and per-user state lives in `multiplatform-settings` key-value prefs via
`ChallengeStore`. `SupabaseChallengesRepository` is a stub. We need real entities before challenges
can be shared, voted on, or ranked.

The proposed split was three entities — `Challenge`, `ChallengeUserData` (vote / favourite /
highscore), `ChallengeAttempt` (scoreboard data). The open question was how to store high scores
when scoring mechanisms will keep changing.

That question is already half-answered by the code:

- `ChallengeScoring` (`core/.../challenge/ChallengeScoring.kt`) is **deliberately not sealed** — an
  extension point, so `rules-bb2025` can add scoring types.
- `ChallengeScore` (`core/.../challenge/ScoringType.kt`) is a polymorphic hierarchy where each
  variant carries a *different payload shape* and its own `compareTo`.
- `ProbabilityScore` exists precisely because "ratings produced by different versions
  are not comparable".
- `docs/challenges-design.md:494` — *"Store the ledger, not just the score… This is cheap now and
  very expensive to retrofit."*

So the schema must not try to model scores relationally. It should **index the one scalar it ranks
on and treat everything else as an opaque Kotlin-owned payload**.

Decisions taken: **Supabase (PostgREST + RLS)**, **revisioned challenges**, **client submits the
full replay**.

---

## Verdict on the three proposed entities

| Proposed | Verdict |
|---|---|
| `Challenge` | Split into **three**: identity (`challenge`), immutable versioned definition (`challenge_revision`), hot counters (`challenge_stats`). Definition is multi-KB and TOASTed; counters change on every play. One row means every play rewrites the blob. |
| `ChallengeUserData` | Keep for vote + favourite. **Remove `highscore`.** Its natural key is `(challenge, revision, scoring_type, algorithm_version, user)` — five parts, of which a `(challenge, user)` row has two. It cannot be a column. Also remove `solved`: `SolvedState.BEST_IN_CLASS` is a *global* fact (nobody beat you), not user data — it is `position = 1` on the leaderboard. |
| `ChallengeAttempt` | Split into **the fact** (`challenge_attempt`) and **the interpretation** (`challenge_score`). An attempt is immutable history. A score is one algorithm version's opinion of it, and there will be several over time. Columns on the attempt would make re-scoring a destructive `UPDATE` over the whole table. |

`ChallengeUserState` in `ChallengeStore.kt` is a good **DTO** and a bad **table**. It should come
back as a view (`my_challenge_state` below).

---

## The core idea: the rank-key contract

The database cannot run Kotlin, so it cannot call `ChallengeScore.compareTo`. Every score therefore
projects itself onto one number:

```kotlin
@Serializable
sealed interface ChallengeScore : Comparable<ChallengeScore> {
    val date: Instant
    val description: String

    /** Matches `scoring_algorithm.scoring_type`. */
    val scoringType: String

    /** Matches `scoring_algorithm.algorithm_version`. */
    val algorithmVersion: Int

    /**
     * The one number the database ranks on. Ascending is always better, for
     * every scoring type, forever — regardless of which direction the metric
     * reads to a human.
     *
     * Invariant: for two scores of the same [scoringType] and
     * [algorithmVersion], `a.compareTo(b).sign == a.rankKey.compareTo(b.rankKey).sign`.
     */
    val rankKey: Double

    /** Applied when [rankKey] ties. Ascending. `docs/challenges-design.md:488`. */
    val tiebreak: Double
}
```

- `CompletionOnly` → `rankKey = date.toEpochMilliseconds().toDouble()`, `tiebreak = 0.0`
- `ProbabilityScore` → `rankKey = score.effectiveRisk`, `tiebreak = rolls.size.toDouble()`

Everything else about the score — `benefitBySource`, the roll ledger, whatever a future mechanism
needs — goes into a `jsonb` payload the DB never reads. **Adding a scoring mechanism is a new
`ChallengeScore` subtype plus a row in `scoring_algorithm`. It is never a migration.**

Drop the `ChallengeScore<T : ChallengeScore<T>>` self-type parameter while doing this. It is used
raw in five places and does not compile (`ChallengeStore.kt:31`, `userScore`,
`ChallengeTracker.score`, `ChallengeSessionViewModel.score`, `ChallengeDetailScreen.formatUserScore`),
and cross-type comparison is meaningless anyway — the DB filters on `scoring_type` before it ever
orders.

---

## Schema

### Identity and definition

```sql
create table coach_profile (
    user_id      uuid primary key references auth.users on delete cascade,
    coach_id     text not null unique,          -- CoachId.value used inside the engine
    display_name text not null,                 -- shown on the scoreboard
    created_at   timestamptz not null default now()
);

-- One row per challenge, ever. Votes, favourites and stats hang off this,
-- not off a revision: you favourite a challenge, not a version of it.
create table challenge (
    id         text primary key,                -- ChallengeId.value
    author_id  uuid not null references coach_profile(user_id),
    visibility text not null default 'draft'
               check (visibility in ('draft','unlisted','public')),
    created_at timestamptz not null default now()
);

-- The immutable definition. A new revision is cut whenever a published
-- challenge is edited, so old leaderboards stay meaningful.
create table challenge_revision (
    challenge_id text not null references challenge(id) on delete cascade,
    revision     int  not null,
    is_current   boolean not null default false,

    -- lifted out of the blob only because the list screen filters/sorts on them
    name         text not null,
    description  text not null,
    category     text not null,     -- ChallengeCategory.name
    scoring_type text not null,     -- @SerialName of the ChallengeScoring impl
    game_version text not null,     -- BB2020 / BB2025 -> selects the serializer module
    file_format  int  not null,     -- JervisMetaData.fileFormat

    definition     text  not null,  -- goal, rules, teams, gameRules, setup: engine JSON, verbatim
    definition_sha bytea not null,  -- sha256(definition); what an attempt pins to

    published_at timestamptz,
    created_at   timestamptz not null default now(),
    primary key (challenge_id, revision)
);
create unique index challenge_revision_current
    on challenge_revision (challenge_id) where is_current;
```

`definition` is `text`, not `jsonb`, on purpose: `jsonb` reorders keys and drops duplicates, so it is
not byte-stable and `definition_sha` would not survive a round trip. We never query inside it. The
cost is that PostgREST returns it as an escaped string the client must parse — acceptable, and the
score payloads below are `jsonb` where poking inside genuinely helps.

### Hot counters, split off the wide row

```sql
create table challenge_stats (
    challenge_id     text primary key references challenge(id) on delete cascade,
    plays            bigint not null default 0,
    solves           bigint not null default 0,
    distinct_solvers bigint not null default 0,
    votes            bigint not null default 0,   -- vote is a Boolean upvote today
    favourites       bigint not null default 0,
    updated_at       timestamptz not null default now()
);
```

Maintained by `security definer` triggers on `challenge_user_state` and `challenge_score`.

### Per-user preferences

```sql
create table challenge_user_state (
    challenge_id text not null references challenge(id) on delete cascade,
    user_id      uuid not null references coach_profile(user_id) default auth.uid(),
    favourite    boolean not null default false,
    voted        boolean not null default false,
    updated_at   timestamptz not null default now(),
    primary key (challenge_id, user_id)
);
create index on challenge_user_state (user_id) where favourite;
```

No `score`, no `solved`. Both are derived.

### The fact

```sql
create table challenge_attempt (
    id             bigint generated always as identity primary key,
    challenge_id   text not null,
    revision       int  not null,
    definition_sha bytea not null,     -- what the client actually played against
    user_id        uuid not null references coach_profile(user_id) default auth.uid(),
    outcome        text not null check (outcome in ('COMPLETED','FAILED')),
    submitted_at   timestamptz not null default now(),
    engine_version text not null,      -- BuildConfig.releaseVersion + git hash
    file_format    int  not null,
    actions        text  not null,     -- List<GameAction> played after setup
    claimed_score  jsonb,              -- what the client said; stored, never ranked
    foreign key (challenge_id, revision)
        references challenge_revision (challenge_id, revision)
);
create index on challenge_attempt (challenge_id, revision, outcome);
create index on challenge_attempt (user_id, submitted_at desc);
```

Append-only. `claimed_score` makes the trust boundary visible in the schema: it is kept for
telemetry and never feeds a leaderboard. The client uploads `COMPLETED` attempts only; the `FAILED`
value is allowed so failures can be collected later without DDL.

`actions` reuses exactly what `JervisSerialization.serializeGameStateToJson` already does — flatten
`controller.history` into a `List<GameAction>`, re-packing multi-step `GameDelta`s as
`CompositeGameAction`. Replaying it through `GameEngineController(initialActions = …)` reconstructs
everything, which is what makes any future metric computable from stored data.

### The interpretation

```sql
create table scoring_algorithm (
    scoring_type      text not null,   -- 'CompletionOnly', 'JervisRiskScore', …
    algorithm_version int  not null,   -- DifficultyRating.ALGORITHM_VERSION
    is_current        boolean not null default false,
    label             text not null,   -- ChallengeScoring.description
    higher_is_better  boolean not null default false,  -- display only; rank_key is always ASC
    primary key (scoring_type, algorithm_version)
);
create unique index on scoring_algorithm (scoring_type) where is_current;

create table challenge_score (
    attempt_id        bigint not null references challenge_attempt(id) on delete cascade,
    scoring_type      text   not null,
    algorithm_version int    not null,

    -- denormalised from the attempt; every one of these is immutable there,
    -- so it can never drift, and the leaderboard becomes a single-table scan
    challenge_id text not null,
    revision     int  not null,
    user_id      uuid not null,

    rank_key    double precision not null,
    tiebreak    double precision not null,
    achieved_at timestamptz      not null,
    payload     jsonb            not null,   -- the serialized ChallengeScore, incl. the roll ledger

    verified_at  timestamptz not null default now(),
    scorer_build text not null,

    primary key (attempt_id, scoring_type, algorithm_version),
    foreign key (scoring_type, algorithm_version)
        references scoring_algorithm (scoring_type, algorithm_version),
    -- Postgres sorts NaN last in ASC rather than erroring, so a bad rank key
    -- would silently sink instead of failing loudly.
    constraint rank_key_finite check (
        rank_key = rank_key
        and rank_key <> 'infinity'::float8
        and rank_key <> '-infinity'::float8
    )
);

create index challenge_score_board on challenge_score
    (challenge_id, revision, scoring_type, algorithm_version,
     user_id, rank_key, tiebreak, achieved_at);
```

One attempt can hold several score rows — one per algorithm version — so a v2 rollout is pure
`INSERT` next to live v1 traffic, and rollback is flipping `is_current` back.

---

## Trust model (RLS)

Supabase has no Kotlin, so the engine cannot run inside the database. The client computes a score
and cannot be trusted with it. The schema makes that structural rather than procedural:

```sql
alter table challenge_score enable row level security;
create policy read_scores on challenge_score for select using (true);
-- no insert/update/delete policy at all: unreachable from any client key

alter table challenge_attempt enable row level security;
create policy insert_own_attempt on challenge_attempt for insert
    with check (user_id = auth.uid());
create policy read_attempts on challenge_attempt for select using (true);
-- no update, no delete: attempts are facts

alter table challenge_user_state enable row level security;
create policy own_state on challenge_user_state for all
    using (user_id = auth.uid()) with check (user_id = auth.uid());
```

Only the scorer worker, holding the service-role key, writes `challenge_score`. A trigger on
`challenge_revision` rejects `UPDATE` once `published_at is not null`.

---

## Views (the client's read model)

```sql
-- Best row per coach under the current algorithm, ranked.
create view challenge_leaderboard as
select s.*,
       rank() over (
           partition by s.challenge_id, s.revision, s.scoring_type, s.algorithm_version
           order by s.rank_key, s.tiebreak, s.achieved_at
       ) as position
from (
    select distinct on (c.challenge_id, c.revision, c.scoring_type, c.algorithm_version, c.user_id) c.*
    from challenge_score c
    join scoring_algorithm a using (scoring_type, algorithm_version)
    where a.is_current
    order by c.challenge_id, c.revision, c.scoring_type, c.algorithm_version, c.user_id,
             c.rank_key, c.tiebreak, c.achieved_at
) s;
```

Filtering on `challenge_id` / `revision` pushes through the window function because both are
`PARTITION BY` columns — keep it that way, or the plan degrades to a full scan. If it ever does,
replace with a PostgREST RPC function or a `challenge_personal_best` table maintained on insert.

```sql
-- ChallengeUserState, reassembled at read time.
create view my_challenge_state as
select c.id as challenge_id,
       coalesce(u.favourite, false) as favourite,
       coalesce(u.voted, false)     as voted,
       b.attempt_id, b.rank_key, b.payload, b.achieved_at,
       b.position = 1 as best_in_class
from challenge c
left join challenge_user_state u
       on u.challenge_id = c.id and u.user_id = auth.uid()
left join challenge_revision r
       on r.challenge_id = c.id and r.is_current
left join challenge_leaderboard b
       on b.challenge_id = c.id and b.revision = r.revision and b.user_id = auth.uid();
```

---

## Kotlin work

**`core/.../challenge/ScoringType.kt`** — add `scoringType`, `algorithmVersion`, `rankKey`,
`tiebreak` to `ChallengeScore`; drop the self-type parameter; give each subtype a frozen
`@SerialName`. Extend `ProbabilityScore` to carry the ledger
(`rolls: List<RollRisk>`, `rerolls: List<RerollResource>`) per `docs/challenges-design.md:494` —
that supplies the `tiebreak` and the UI breakdown, and makes the separate ledger column unnecessary.

While there: `CompletionOnly.compareTo` has a `when (scoreCompare == 0)` whose branches are
identical. Since `rank_key` must mirror `compareTo` exactly, collapse it to `date.compareTo(other.date)`.

**New test** `core/src/commonTest/.../probability/RankKeyTest.kt` — the invariant that makes the whole
schema safe:

```kotlin
assertEquals(a.compareTo(b).sign, a.rankKey.compareTo(b.rankKey).sign)
```

over generated pairs per subtype, plus `rankKey.isFinite()`.

**New module `modules/challenge-scorer`** (JVM, Clikt, same shape as `modules/fumbbl-cli`). This is
the only thing that may write `challenge_score`:

1. Find attempts with no score row for the current `(scoring_type, algorithm_version)`.
2. Load the revision, verify `definition_sha`, decode `Challenge` and the replay.
3. `challenge.createGame()`, feed the replay, run `ChallengeTracker` alongside, require
   `outcome == COMPLETED` — this is the anti-cheat pass, not just a scoring pass.
4. Score, insert.

**Client** — replace `ChallengeStore`'s settings keys with `SupabaseChallengesRepository` reading
`my_challenge_state` and `challenge_leaderboard`. `ChallengeStore.setSolved` becomes "insert an
attempt"; it currently is not called from anywhere even though success detection already exists in
`ChallengeSessionViewModel`.

---

## Blocking prerequisites

1. **`docs/improve-serialization-format.md` must land first.** Polymorphic tokens are fully-qualified
   class names today (no `@SerialName` anywhere in the engine). A `.jrg` file on disk that breaks is
   an annoyance; a `definition` or `actions` column that breaks is unrecoverable data loss across
   every user. Renaming one package after go-live bricks every stored challenge and replay. This is
   the single hardest thing here to retrofit.
2. **`Challenge` and its extension points are not `@Serializable` at all** — only `ChallengeScore` is.
   `Challenge`, `ChallengeGoal`, `ChallengeRule`, `GoalModifier`, `ChallengeScoring` and the
   `rules-bb2025` implementations all need registering in the serializer module (already flagged in
   `docs/challenge-builder.md`). Nothing can be stored until this exists.
3. The working tree does not compile — `entities` vs `ScoreboardEntry` (~20 call sites),
   `ChallengeRepository.kt` is not valid Kotlin, both challenge test files target a deleted API, and
   `SampleChallenges` hard-codes `/Users/christian.melchior/first-turn.jrg`.

---

## Adding a new scoring mechanism, end to end

The design is only worth it if this is cheap. It is:

1. New `ChallengeScore` subtype with a frozen `@SerialName`, `rankKey`, `tiebreak`; register it.
2. `insert into scoring_algorithm values ('FewestActions', 1, false, 'Fewest actions', false);`
3. Run the scorer's backfill — it re-simulates stored replays and inserts score rows.
4. `update scoring_algorithm set is_current = true where …` — leaderboards switch atomically.

Same for bumping `DifficultyRating.ALGORITHM_VERSION` to the v2 exact DP. **No DDL, no downtime, and
v1 rows stay queryable for comparison.**

---

## Verification

- `./gradlew :modules:jervis-engine:core:jvmTest` — the `rankKey`/`compareTo` invariant test.
- Golden-token test from `improve-serialization-format.md` step 5, extended to the challenge types:
  dump the serializer module, assert no token contains `.`, assert exact match against a committed
  registry. This is the guardrail that keeps stored rows readable.
- Round-trip test: build a `Challenge`, serialize, insert into a local Supabase
  (`supabase start`), read back, `createGame()`, replay a recorded solution, assert
  `ChallengeOutcome.COMPLETED` and a stable `ProbabilityScore`.
- Trust test: with an `anon` key, attempt `insert into challenge_score` and assert it is rejected by
  RLS; attempt `insert into challenge_attempt` with a forged `user_id` and assert rejection.
- Re-score test: insert a second `scoring_algorithm` row, run the backfill, assert both versions
  coexist on the same attempt and that flipping `is_current` changes `challenge_leaderboard` with no
  writes to `challenge_attempt`.
- `./gradlew ktlintFormat && ./gradlew jvmTest` — the `CONTRIBUTING.md` checklist.
