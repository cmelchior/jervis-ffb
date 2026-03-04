package com.jervisffb.engine.rules

import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.OnFieldLocation
import com.jervisffb.engine.rules.bb2025.BB2025SkillSettings
import com.jervisffb.engine.rules.bb2025.BB2025TeamActions
import com.jervisffb.engine.rules.bb2025.tables.BB2025ArgueTheCallTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025CasualtyTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025LastingInjuryTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025RangeRuler
import com.jervisffb.engine.rules.bb2025.tables.BB2025StandardInjuryTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StandardKickOffEventTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StandardPrayersToNuffleTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StandardWeatherTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StuntyInjuryTable
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.common.actions.PlayerAction
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.skills.SpecialActionProvider
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * Top-level class for all variants of the 2025 Blood Bowl rules.
 */
@Serializable
abstract class BB2025Rules(
    private val bb2025RuleParameters: RulesParametersHolder
) : Rules(bb2025RuleParameters) {

    override fun getAvailableActions(state: Game, player: Player): List<PlayerAction> {
        if (state.activePlayer != player) INVALID_GAME_STATE("$player is not the active player")
        if (player.location !is OnFieldLocation) return emptyList()
        return buildList {
            // Add any team actions that are available
            state.activeTeamOrThrow().turnData.let { turnData ->
                if (turnData.moveActions > 0) add(teamActions.move)
                if (turnData.passActions > 0 && !player.isSkillAvailable(SkillType.MY_BALL)) {
                    add(teamActions.pass)
                }
                if (turnData.handOffActions > 0 && !player.isSkillAvailable(SkillType.MY_BALL)) {
                    add(teamActions.handOff)
                }
                if (turnData.blockActions > 0) {
                    val isStanding = (player.state == PlayerState.STANDING)
                    val hasEligibleTargets = (player.location as OnFieldLocation)
                        .getSurroundingCoordinates(this@BB2025Rules, 1)
                        .mapNotNull { state.field[it].player }
                        .filter { otherPlayer -> otherPlayer.team != player.team }
                        .filter { otherPlayer -> isStanding(otherPlayer)}
                        .any { otherPlayer -> isMarking(player, otherPlayer)}

                    // TODO Also check for Jump Up
                    if (isStanding && hasEligibleTargets) {
                        add(teamActions.block)
                    }
                }
                if (turnData.blitzActions > 0) {
                    val hasEligibleBlitzTargets = player.team.otherTeam()
                        .filter { targetPlayer ->  targetPlayer.location.isOnField(this@BB2025Rules) }
                        .any {  targetPlayer -> isStanding(targetPlayer) }

                    if (hasEligibleBlitzTargets) {
                        add(teamActions.blitz)
                    }
                }
                if (turnData.foulActions > 0) {
                    val hasEligibleFoulTargets = player.team.otherTeam()
                        .filter { targetPlayer ->  targetPlayer.location.isOnField(this@BB2025Rules) }
                        .any {  targetPlayer -> targetPlayer.state == PlayerState.PRONE || targetPlayer.state == PlayerState.STUNNED }
                    if (hasEligibleFoulTargets) {
                        add(teamActions.foul)
                    }
                }
                if (
                    turnData.throwTeamMateActions > 0
                    && player.hasSkill(SkillType.THROW_TEAMMATE)
                ) {
                    add(teamActions.throwTeamMate)
                }
                // Even though Secure The Ball is only in the 2025 ruleset, we have the check here
                // since it makes maintaining the logic easier. The action is disabled by setting the
                // count to 0 in the TeamActions setup.
                val hasUnsteady = player.isSkillAvailable(SkillType.UNSTEADY)
                val isBigGuy = player.keywords.contains(PlayerKeyword.BIG_GUY)
                if (turnData.secureTheBallActions > 0 && !hasUnsteady && !isBigGuy) {
                    // Securing the Ball is only available if no standing players wit TZ's are within 2 of the ball.
                    // In the case of multiple balls, only one ball has to satisfy the criteria for the action to be
                    // available. The ball has to be on the floor at the start of the activation.
                    val eligibleBallExists = state.balls.any { ball ->
                        val onTheGround = (ball.state == BallState.ON_GROUND)
                        val enemiesInRange = ball.location.getSurroundingCoordinates(
                            rules = this@BB2025Rules,
                            distance = 2,
                            includeOutOfBounds = false
                        ).any { coordinate ->
                            state.field[coordinate].player?.let { p->
                                (p.team != player.team) && this@BB2025Rules.canMarkPlayers(p)
                            } ?: false
                        }
                        onTheGround && !enemiesInRange
                    }
                    if (eligibleBallExists) {
                        add(teamActions.secureTheBall)
                    }
                }
            }

            // Add any special actions that are provided by skills
            player.skills.filterIsInstance<SpecialActionProvider>().forEach {
                if (it.isActionAvailable(state, this@BB2025Rules)) {
                    val type = it.specialAction
                    add(teamActions[type])
                }
            }
        }
    }

    companion object {
        val DEFAULTS = RulesParametersHolder(
            name = "Blood Bowl 2025 Rules",
            baseVersion = GameVersion.BB2025,
            gameType = GameType.STANDARD,
            teamActions = BB2025TeamActions(),
            skillSettings = BB2025SkillSettings(),
            kickOffEventTable = BB2025StandardKickOffEventTable,
            prayersToNuffleTable = BB2025StandardPrayersToNuffleTable,
            weatherTable = BB2025StandardWeatherTable,
            injuryTable = BB2025StandardInjuryTable,
            stuntyInjuryTable = BB2025StuntyInjuryTable,
            casualtyTable = BB2025CasualtyTable,
            lastingInjuryTable = BB2025LastingInjuryTable,
            argueTheCallTable = BB2025ArgueTheCallTable,
            rangeRuler = BB2025RangeRuler,)
    }
}


@Serializable
class StandardBB2025Rules(
    private val standardBB2025RuleParameters: RulesParametersHolder = DEFAULTS
) : BB2025Rules(standardBB2025RuleParameters) {

    companion object {
        val DEFAULTS = BB2025Rules.DEFAULTS.copy(
            name = "Blood Bowl 2025 Rules (Strict)",
        )
    }

    // Builder API infrastructure
    override fun toBuilder() = StandardBB2025RulesBuilder(standardBB2025RuleParameters)
    class StandardBB2025RulesBuilder(parameters: RulesParameters): RulesParameterBuilder(parameters) {
        override fun build(): StandardBB2025Rules = StandardBB2025Rules(buildParameters())
    }
}
