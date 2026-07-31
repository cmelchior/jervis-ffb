package com.jervisffb.ui.menu.challenges

import kotlin.jvm.JvmInline

/**
 * Unique identifier for a [Challenge].
 */
@JvmInline
value class ChallengeId(val value: String)

/**
 * The categories a [Challenge] can belong to. These are used both as labels on the challenge and
 * as the filters shown at the top of the challenges list.
 */
enum class ChallengeCategory(val label: String) {
    BLOCKING("Blocking"),
    BREAK_THE_CAGE("Cage Breaking"),
    CROWD_SURFING("Crowd Surfing"),
    ONE_TURN_TOUCHDOWNS("One-Turn-Touchdowns"),
    SCORING("Scoring"),
}

/**
 * A single entry on a challenge's scoreboard. [successChance] is a value between `0f` and `1f`
 * describing how likely the submitted solution was to succeed (higher is better). The scoreboard
 * is ordered by this value.
 */
data class ScoreboardEntry(
    val coachName: String,
    val successChance: Float,
)

/**
 * A single challenge/puzzle.
 *
 * Note: This is currently only scaffolding backed by [SampleChallenges]. It holds no game state
 * yet; wiring a challenge to an actual predefined game position is future work (see the design
 * doc). Per-user state (favorite/solved/rating) is not stored here but in [ChallengeStore].
 */
data class Challenge(
    val id: ChallengeId,
    val name: String,
    val author: String,
    val category: ChallengeCategory,
    val description: String,
    val rules: List<String>,
    /** Mock aggregate community rating shown next to the up/down vote controls. */
    val communityScore: Int = 0,
    val scoreboard: List<ScoreboardEntry> = emptyList(),
    /** Seeds the solved-state the first time a challenge is seen, so the trophy/"Hide solved" are demoable. */
    val solvedByDefault: Boolean = false,
)

/**
 * Hard-coded sample challenges used to drive the UI scaffolding until real challenges exist.
 */
object SampleChallenges {

    val all: List<Challenge> = listOf(
        Challenge(
            id = ChallengeId("two-heads-are-better"),
            name = "Two Heads Are Better",
            author = "Ilios",
            category = ChallengeCategory.CROWD_SURFING,
            description = "A lone Black Orc stands between your Blitzer and a clear run at the end " +
                "zone. Knock him down and stay on your feet, without rolling a single skull.",
            rules = listOf(
                "You have 1 turn to solve it.",
                "Team re-rolls are disabled.",
                "Block dice are pre-rolled and fixed.",
            ),
            communityScore = 42,
            solvedByDefault = true,
            scoreboard = listOf(
                ScoreboardEntry("Ilios", 0.98f),
                ScoreboardEntry("GrimIronjaw", 0.91f),
                ScoreboardEntry("Nuffle", 0.72f),
            ),
        ),
        Challenge(
            id = ChallengeId("threading-the-needle"),
            name = "Threading the Needle",
            author = "Nuffle",
            category = ChallengeCategory.SCORING,
            description = "Your Thrower has the ball and a Catcher is streaking downfield. Find the " +
                "passing lane through a crowded midfield and complete the throw.",
            rules = listOf(
                "You have 2 turns to solve it.",
                "1 team re-roll is available.",
                "Weather: Blizzard (passing is harder).",
            ),
            communityScore = 18,
            scoreboard = listOf(
                ScoreboardEntry("Nuffle", 0.66f),
                ScoreboardEntry("Skittter", 0.51f),
            ),
        ),
        Challenge(
            id = ChallengeId("the-great-escape"),
            name = "The Great Escape",
            author = "Skittter",
            category = ChallengeCategory.BLOCKING,
            description = "Your Gutter Runner is surrounded. Dodge out of three tackle zones and " +
                "sprint into open field without hitting the turf.",
            rules = listOf(
                "You have 1 turn to solve it.",
                "Going for it is allowed.",
                "Team re-rolls are disabled.",
            ),
            communityScore = 27,
            solvedByDefault = true,
            scoreboard = listOf(
                ScoreboardEntry("Skittter", 0.84f),
                ScoreboardEntry("Ilios", 0.79f),
                ScoreboardEntry("WhatBall", 0.60f),
            ),
        ),
        Challenge(
            id = ChallengeId("one-turn-touchdown"),
            name = "One-Turn Touchdown",
            author = "Ilios",
            category = ChallengeCategory.BREAK_THE_CAGE,
            description = "The classic. Score a touchdown in a single turn using a hand-off and a " +
                "blitz to clear the final defender.",
            rules = listOf(
                "You have 1 turn to solve it.",
                "1 team re-roll is available.",
                "The ball starts with the Thrower.",
            ),
            communityScore = 63,
            scoreboard = listOf(
                ScoreboardEntry("Ilios", 0.44f),
                ScoreboardEntry("Garion", 0.41f),
            ),
        ),
        Challenge(
            id = ChallengeId("hold-the-line"),
            name = "Hold the Line",
            author = "GrimIronjaw",
            category = ChallengeCategory.ONE_TURN_TOUCHDOWNS,
            description = "You are up by one with the clock running down. Stall the drive, keep the " +
                "ball safe and deny the opponent any chance to equalise.",
            rules = listOf(
                "You have 3 turns to solve it.",
                "Standard team re-rolls apply.",
                "The opponent plays aggressively.",
            ),
            communityScore = 9,
            scoreboard = listOf(
                ScoreboardEntry("GrimIronjaw", 0.55f),
            ),
        ),
        Challenge(
            id = ChallengeId("crowd-surf"),
            name = "Crowd Surf",
            author = "Nuffle",
            category = ChallengeCategory.CROWD_SURFING,
            description = "The opposing Blitzer is standing right on the sideline. Push him into the " +
                "crowd for a guaranteed removal.",
            rules = listOf(
                "You have 1 turn to solve it.",
                "Team re-rolls are disabled.",
                "Block dice are pre-rolled and fixed.",
            ),
            communityScore = 51,
            scoreboard = listOf(
                ScoreboardEntry("Nuffle", 0.95f),
                ScoreboardEntry("Cowhead", 0.88f),
            ),
        ),
        Challenge(
            id = ChallengeId("the-long-bomb"),
            name = "The Long Bomb",
            author = "Skittter",
            category = ChallengeCategory.SCORING,
            description = "It is all or nothing. Launch a Hail Mary pass the length of the pitch and " +
                "bring it down for the winning score.",
            rules = listOf(
                "You have 1 turn to solve it.",
                "No re-rolls are available.",
                "Weather: Sweltering Heat.",
            ),
            communityScore = -4,
            scoreboard = emptyList(),
        ),
        Challenge(
            id = ChallengeId("pick-and-run"),
            name = "Pick and Run",
            author = "GrimIronjaw",
            category = ChallengeCategory.BLOCKING,
            description = "The ball is loose inside a tackle zone. Pick it up cleanly and carry it to " +
                "safety before the defence collapses on you.",
            rules = listOf(
                "You have 2 turns to solve it.",
                "1 team re-roll is available.",
            ),
            communityScore = 33,
            scoreboard = listOf(
                ScoreboardEntry("GrimIronjaw", 0.71f),
                ScoreboardEntry("Tussock", 0.64f),
                ScoreboardEntry("Java", 0.58f),
            ),
        ),
    )

    fun byId(id: ChallengeId): Challenge = all.first { it.id == id }
}
