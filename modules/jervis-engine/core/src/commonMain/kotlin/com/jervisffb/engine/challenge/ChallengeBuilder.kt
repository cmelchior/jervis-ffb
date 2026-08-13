package com.jervisffb.engine.challenge

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.ChallengeId
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.serialization.SerializedTeam

/**
 * This class controls building a [Challenge] while verifying all constraints.
 */
class ChallengeBuilder(val id: ChallengeId) {
    var name: String = ""
    var author: Coach = Coach(CoachId("coach-jervis"), "Jervis")
    var description: String = ""
    var category: ChallengeCategory? = null
    var scoring: ChallengeScoring<*> = ChallengeScoring.CompletionOnly
    var goal: ChallengeGoal? = null
    var gameRules: Rules? = null
    var homeTeam: Team? = null
    var awayTeam: Team? = null
    val setup: MutableList<GameAction> = mutableListOf()

    private val rules = mutableListOf<ChallengeRule>()

    fun addRule(rule: ChallengeRule): ChallengeBuilder {
        if (!rule.isMultipleAllowed && rules.any { it::class == rule::class }) {
            throw IllegalArgumentException("'$name' already has a ${rule::class.simpleName} rule.")
        }
        rules.add(rule)
        return this
    }

    fun addRules(vararg rules: ChallengeRule): ChallengeBuilder {
        rules.forEach {
            addRule(it)
        }
        return this
    }

    fun build(): Challenge {
        require(name.isNotBlank()) { "A challenge requires a name." }
        require(description.isNotBlank()) { "A challenge requires a description." }
        require(category != null) { "A challenge requires a category." }
        require(gameRules != null) { "No game rules were specified." }
        require(homeTeam != null) { "Home team is missing" }
        require(awayTeam != null) { "Away team is missing" }

        val scoring = scoring
        if (scoring is ChallengeScoring.ProbabilityScoring) {
            require(scoring.solvingTeamId == homeTeam!!.id || scoring.solvingTeamId == awayTeam!!.id) {
                "The Jervis Probability Score solving team is not part of '$name': ${scoring.solvingTeamId}"
            }
        }

        val goal = this.goal ?: throw IllegalArgumentException("'$name' needs a goal.")
        goal.validateModifiers()

        // Snapshot the teams the challenge starts from.
        val homeTeamState = SerializedTeam.serialize(homeTeam!!)
        val awayTeamState = SerializedTeam.serialize(awayTeam!!)

        return Challenge(
            id = id,
            version = 1,
            name = name,
            author = author,
            description = description,
            category = category!!,
            scoring = scoring,
            goal = goal,
            rules = rules,
            gameRules = gameRules!!,
            homeTeam = homeTeam!!,
            awayTeam = awayTeam!!,
            initialHomeTeamState = homeTeamState,
            initialAwayTeamState = awayTeamState,
            setup = setup,
        )
    }
}
