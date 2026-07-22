This document contains the first draft of a Claude Agent plan for making
the file-serialization format more durable and future-proof. It is persisted 
here, so we have some time to think about edge-cases.

# Future-proofing `JervisGameFile` serialization (format v2)

## Context

Jervis save files (`.jrg` game, `.jrt` team, `.jrr` roster, `.jrs` setup) and the
network protocol use kotlinx.serialization with `useArrayPolymorphism = true`, so
every polymorphic value is written as `["<serialName>", { …payload… }]`. There is
**not a single `@SerialName`** anywhere in the engine/net modules, so `<serialName>`
defaults to the **fully-qualified class name**. Consequently, moving a class to
another package or renaming it silently changes the on-disk token and breaks every
previously-saved file. The format is currently documented as "not yet formalized",
`FILE_FORMAT_VERSION` is `1`, is never read on load, and there are no
golden/snapshot tests — so a rename+re-register passes CI while breaking old files.

**Goal:** decouple the on-disk type token from code identity (package/class name),
and add a guardrail so that decoupling cannot silently drift over ~10 years.

**Decisions (confirmed with the owner):**
1. **Clean break** — pre-release, no old files must survive. Bump `FILE_FORMAT_VERSION`
   to `2`; no FQN→token migration. But still add the version-read + migration
   *scaffolding* now (empty), because 10-year durability needs the machinery to exist.
2. **Simple-name tokens, disambiguated by `GameVersion`.** Tokens are short, frozen,
   package-independent strings (seeded from the current simple class name), and MAY be
   reused across rules versions. Disambiguation is achieved by recording `GameVersion`
   in `JervisMetaData` and selecting a per-version serializer module at (de)serialize
   time. (Today there are zero cross-version simple-name collisions — every ruleset
   class is already prefixed `BB2020…`/`BB2025…` — so this is forward-looking insurance
   that also permits cleaner tokens later.)
3. **Include net messages** — the jervis-net message hierarchies get stable tokens too.

**The core principle to preserve for a decade:** the `@SerialName` value is the wire
contract. It is seeded from the class name once, then **frozen** — never re-derived,
never "fixed up" to match a later rename. A guardrail test enforces this.

---

## Approach

1. Add a frozen `@SerialName("<simpleName>")` to every polymorphic participant.
2. Split the single serializer module into one module/`Json` **per `GameVersion`**;
   record `gameVersion` in `JervisMetaData` and select the module from it.
3. Load in two phases: read `metadata` module-free → pick the versioned `Json` → decode
   (with an empty-but-present migration ladder keyed on `fileFormat`).
4. Give the net protocol stable tokens + version-aware selection.
5. Add a guardrail test that freezes the full token/enum registry and fails on drift.
6. Update the generator notebook to enforce tokens, and formalize the docs.

Keep `useArrayPolymorphism = true` — `SpriteSource` and `InducementSelection` both
have a real `type` property, so the default `"type"` class discriminator would
collide. `@SerialName` works identically under array polymorphism.

---

## Step 1 — Stable tokens (`@SerialName`)

Seed each token from the current simple class name (e.g. `@SerialName("EndTurn")`,
`@SerialName("D6Result")`, `@SerialName("BB2020StandardInjuryTable")`). Three groups:

- **~111 module-registered concrete classes** — every `subclass(X::class)` in the four
  generated module files. The annotation lives on the *class declaration* (the module
  file cannot carry it). A class registered under multiple bases (e.g. `D6Result` under
  both `GameAction` and `DieResult`) needs the annotation only once.
- **Sealed auto-registered subtypes** (not in any module, resolved by the compiler
  sealed serializer, also currently FQN-based):
    - `SpriteSource` → `SingleSprite`, `SpriteSheet` and `PositionUiData` →
      `PositionSpriteSheetUiData` (`core/.../serialize/JervisGameFile.kt`)
    - `InducementSelection<*>` → `Simple`, `Wizard`, `BiasedReferee`, `InfamousCoach`,
      `StarPlayer`, `Mercenary` (`core/.../actions/GameAction.kt:520-574`)
- **4 custom `Rules` serializers** — change only the hardcoded descriptor string
  `SerialDescriptor("com.jervisffb.engine.rules.StandardBB2020Rules", …)` →
  `SerialDescriptor("StandardBB2020Rules", …)` in
  `rules-bb2020/.../rules/bb2020Rules.kt` (`StandardBB2020RulesSerializer`,
  `FumbblBB2020RulesSerializer`, `BB72020RulesSerializer`) and
  `rules-bb2025/.../rules/bb2025rules.kt` (`StandardBB2025RulesSerializer`). The
  abstract `BB2020Rules`/`BB2025Rules` stay non-`@Serializable` (Kotlin 2.4
  `SyntheticAccessorLowering` workaround) — only the string changes.

**Bootstrap:** add a one-off cell to `tools/GenerateSerializers.ipynb` (already uses
ClassGraph) that, for every class it would register, emits `(sourceFile, fqn,
simpleName, hasSerialName)`; a companion editor pass inserts `@SerialName("<simpleName>")`
+ the `import kotlinx.serialization.SerialName` where missing. Review the diff, then the
  Step 5 guardrail + notebook verification catch anything missed.

- `IntRangeSerializer` (`core/.../model/Player.kt`) needs **no** change — its descriptor
  is a stdlib list name and it is only used as a fixed `@Serializable(IntRangeSerializer::class)`,
  never polymorphically.
- **Do not** annotate non-`@Serializable` sealed subtypes (`CalculatedAction`, the net
  `Internal*` messages) — the sealed serializer auto-excludes them.

**Enums (do NOT blanket-annotate):** ~100 enums serialize by raw constant name. Adding
`@SerialName` to every constant is high-churn with no extra safety. Instead the guardrail
(Step 5) freezes each enum's constant-name set; a rename then fails the golden diff. Add a
per-constant `@SerialName("oldName")` only when deliberately renaming a constant while
keeping the wire value.

## Step 2 — Per-`GameVersion` modules + metadata

- `JervisGameFile.kt`: `JervisMetaData(val fileFormat: Int, val gameVersion: GameVersion)`.
  Derive the version from `controller.rules.baseVersion` (games), `team.version` (teams);
  add a `gameVersion` parameter to the roster/setup builders (a raw `Roster` has no version
  field — supply it from the `BB2020StandaloneRosters`/`BB2025StandaloneRosters` /
  `DefaultSetups` context).
- `JervisSerialization.kt`: replace the single `jsonFormat` with per-version maps and a
  `fun json(v: GameVersion): Json` accessor:
    - `BB2020 → core + common + bb2020SerializerModule`
    - `BB2025 → core + common + bb2025SerializerModule`
      Keep the old combined `jervisEngineSerializerModule` only as a clearly-renamed
      `combinedModuleForToolingOnly` for the version-agnostic net base (valid only while the
      guardrail confirms no cross-version collision).
- **Save:** stamp `JervisMetaData(FILE_FORMAT_VERSION, version)` and encode everything
  (including the two team `JsonElement` snapshots and `createTeamSnapshot`) with
  `json(version)`.

## Step 3 — Two-phase load + migration scaffold

In `loadFromFileContent` (`JervisSerialization.kt`):
1. `Json.parseToJsonElement(text)`; decode just `root["metadata"]` with a **bare** `Json`
   (no module — `JervisMetaData` is a plain class) to read `fileFormat` + `gameVersion`.
2. Migration ladder (empty at v2, but present):
   `MIGRATIONS: Map<Int, (JsonElement) -> JsonElement>`; `while (fmt < FILE_FORMAT_VERSION)
   { tree = MIGRATIONS.getValue(fmt)(tree); fmt = … }`; fail on unknown/too-new versions.
3. Decode `JervisGameFile` and the second-pass `SerializedTeam` team elements with
   `json(gameVersion)`.

Update the other consumers of the (now per-version) module:
`jervis-ui/shared/.../CacheManager.kt` (read each file's `metadata.gameVersion` first, then
decode/encode with `json(version)`), and the desktop `GenerateTeamFiles.kt` /
`GenerateRosterFiles.kt` (encode the BB2020 and BB2025 buckets with their respective `json`).
The `JervisSerialization` public method signatures are unchanged, so UI callers only recompile.

## Step 4 — Net layer (`jervis-net`)

- Add `@SerialName` to every serializable message leaf in `messages/ClientMessage.kt` and
  `messages/ServerMessage.kt` (e.g. `JoinGameAsCoachMessage`, `GameActionMessage`,
  `GameStateSyncMessage`, `ConfirmGameStartMessage`, `SyncGameActionMessage`, the `*ServerError`
  classes, `TeamInfo` leaves). Leave the non-`@Serializable` `Internal*` messages alone.
- Only `GameStateSyncMessage` and `ConfirmGameStartMessage` (server→client) carry `Rules`.
  Add a leading `val gameVersion: GameVersion` to those two, so a client can select its module
  before decoding the versioned payload.
- Refactor `JervisNetworkSerializer.kt` from one global `Json` into: an `agnostic` `Json`
  (core+common, decodes all version-agnostic messages) + `forVersion(v)` delegating to
  `JervisSerialization.json(v)`; add `readServerMessage(text)` that reads the array token,
  uses `gameVersion` from the two self-describing messages (else the connection's cached
  version, else `agnostic`), decodes, and caches the version. Thread the server's version
  (`GameSession.gameSettings.gameRules.baseVersion`) into the server encode sites
  (`WebSocketUtils.kt`, `ServerCommunication.kt`, `PlatformWebSocketServer.kt`); the client
  encodes with `agnostic` and decodes via `readServerMessage`
  (`JervisClientWebSocketConnection.kt`).

## Step 5 — Guardrail test (the crux)

New tests (prefer `commonTest`; fall back to `jvmTest` like the existing `SerializationTests`
if the experimental descriptor APIs are awkward on a target). Commit the golden registries as
**Kotlin source constants** in the test source set (portable across KMP targets, clean PR
diffs), with a `printGolden()` helper to regenerate on intentional change.

- **Module pass (`SerializersModule.dumpTo`)** — per version module, collect every
  `(baseClass, token = actualSerializer.descriptor.serialName)`. Assert: (a) no token
  contains `.`/`com.` (catches a forgotten `@SerialName`, whose default is the FQN);
  (b) uniqueness within each base; (c) the `(base → sorted tokens)` map equals the committed
  golden — any add/remove/change fails, forcing a reviewed decision + format bump. Also flag any
  *new* cross-version token collision (it would break `combinedModuleForToolingOnly`).
- **Descriptor-walk pass** — from `JervisGameFile`/`JervisTeamFile`/`JervisRosterFile`/
  `JervisSetupFile`/`SerializedTeam`/`Roster` serializers, recurse descriptors (guarding a
  `visited` set): for `PolymorphicKind.SEALED` read the element-1 union's `elementNames`
  (sealed subtype tokens — invisible to `dumpTo`); for `SerialKind.ENUM` snapshot
  `elementNames.toSet()` (freeze enum constants). Same no-FQN + golden-diff assertions. Both
  APIs are confirmed present in 1.11.0 and format-independent (require
  `@OptIn(ExperimentalSerializationApi::class)`).
- **Net guardrail** (in `jervis-net` commonTest) — descriptor-walk from `NetMessage` and the
  `Client`/`ServerMessage` roots; same assertions + one committed net golden.
- *Optional:* a small golden-JSON `.jrg`/`.jrt`/`.jrr` fixture round-trip (BB2020 + BB2025) for
  human-readable diffs — secondary to the registry, which is the authoritative drift guard.

## Step 6 — Generator notebook + docs

- `tools/GenerateSerializers.ipynb`: add a **verification cell** that fails generation if any
  class it registers lacks a non-FQN `@SerialName` (read via ClassGraph annotation info); keep
  the Step-1 bootstrap cell; optionally emit the golden constants for paste-in.
- `JervisGameFile.kt`: `FILE_FORMAT_VERSION = 2` + KDoc `- 2: stable @SerialName tokens +
  gameVersion in metadata`; note on `JervisMetaData` that `gameVersion` selects the module and
  `fileFormat` drives migrations.
- `docs/architecture-overview-faq.md:105-120`: replace the save-format TODO with the formalized
  description (envelope, replay model, token format, per-version modules, migration scaffold,
  guardrail contract).

---

## Files to modify (grouped)

- **`@SerialName` on class declarations (repeating):** `core/.../actions/GameAction.kt`,
  `core/.../serialize/JervisGameFile.kt`, and concrete classes across `core/.../model/**`,
  `core/.../rules/builder/**`, `core/.../model/inducements/**`; `rules-common/.../**`
  (`Fireball`/`Zap`/`StandardPathFinder`); every class named in `bb2020Serializers.kt` /
  `bb2025Serializers.kt` (tables, skill settings, team actions, wizards);
  `jervis-net/.../messages/ClientMessage.kt` + `ServerMessage.kt` leaves.
- **Custom serializer descriptor strings (4):** `rules-bb2020/.../rules/bb2020Rules.kt`,
  `rules-bb2025/.../rules/bb2025rules.kt`.
- **Format/selection:** `core/.../serialize/JervisGameFile.kt` (metadata, version const,
  `createTeamFile`), `core/.../serialize/*` team/roster builders,
  `package/.../serialize/JervisSerialization.kt` (per-version `Json`, save, two-phase load,
  migration scaffold, `createTeamSnapshot`).
- **Consumers:** `jervis-ui/shared/.../CacheManager.kt`, desktop `GenerateTeamFiles.kt` /
  `GenerateRosterFiles.kt`, and the roster/setup sources under `jervis-resources/.../bb2020/**`,
  `bb2025/**`, `DefaultSetups.kt`.
- **Net selection:** `jervis-net/.../serialize/JervisNetworkSerializer.kt`, `WebSocketUtils.kt`,
  `ServerCommunication.kt`, `PlatformWebSocketServer.kt`, `JervisClientWebSocketConnection.kt`,
  `messages/ServerMessage.kt` (+`GameSession` to expose the server version).
- **New tests + goldens:** `jervis-engine/package/src/commonTest/.../SerializationRegistryGuardrailTest.kt`,
  `jervis-net/src/commonTest/.../NetRegistryGuardrailTest.kt`.
- **Generator + docs:** `tools/GenerateSerializers.ipynb`, `docs/architecture-overview-faq.md`.

## Reused existing utilities

- `RulesParameters.baseVersion: GameVersion` — the version source for save + selection.
- `SerializedTeam.version: GameVersion` — team-file version source.
- `createDefaultGameStateBB2020` (`jervis-test-utils/.../test/bb2020/teams.kt`) + BB2025
  equivalent — drive the round-trip/guardrail tests.
- Existing `SerializationTests.kt` (jvmTest) + `SerializerRoundTripTests.kt` (commonTest) — extend
  rather than replace.
- Existing per-ruleset file bucketing in `GenerateSerializers.ipynb` — already emits per-version files.

## Verification

- **Tests:** `./gradlew :modules:jervis-engine:package:jvmTest` (existing + new engine guardrail),
  `./gradlew :modules:jervis-net:jvmTest` (P2P/fuzz + net guardrail), `./gradlew build` (compiles
  all KMP targets — confirms the `@SerialName`/`@OptIn` additions compile on jvm/ios/wasmJs).
- **Round-trip both versions:** build BB2020 and BB2025 default games, `startManualMode(false)`,
  `saveToFile` → `loadFromFile`, assert `isSuccess`. Open a `.jrg` and confirm `fileFormat == 2`,
  `gameVersion` present, and `grep com.jervisffb <file>` → **zero** hits. Repeat for `.jrt`/`.jrr`
  via `CacheManager`. Run `P2PNetworkTests` for a BB2020 and a BB2025 game (exercises the
  agnostic→versioned transition).
- **Durability proof via the guardrail:**
    1. Rename `EndTurn`→`EndTurnAction` but keep `@SerialName("EndTurn")` → build + guardrail pass,
       old file still loads (decoupling works).
    2. Change the token string → guardrail fails with a precise golden diff (forces a format bump).
    3. Delete an `@SerialName` → the no-FQN assertion fails (token becomes the FQN).
    4. Rename an enum constant → the enum snapshot diff fails.

## Notes / follow-ups (out of scope)

- **Latent bug to fix separately:** `StatModifier` (`core/.../model/StatModifier.kt`) is a
  non-`@Serializable` polymorphic interface used by `SerializedPlayer.statModifiers` and is not
  registered in any module. It serializes today only because that list is always empty; if it ever
  carries data it throws "not registered". Register it + freeze tokens in a follow-up (the guardrail
  will not cover it until it is `@Serializable` + registered).
- The hand-maintained `commonSerializers.kt` (`Spell`/`PathFinder`, "TODO make sure this is
  generated") could be folded into the notebook, but that is not required here.

