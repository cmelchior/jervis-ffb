package com.jervisffb.engine.rules

import com.jervisffb.engine.InducementSettings
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.OnPitchLocation
import com.jervisffb.engine.rules.bb2020.DEFAULT_INDUCEMENTS_BB2020
import com.jervisffb.engine.rules.bb2020.tables.BB7KickOffEventTable
import com.jervisffb.engine.rules.bb2020.tables.BB7PrayersToNuffleTable
import com.jervisffb.engine.rules.bb2020.tables.BB7StandardInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.BB7StuntyInjuryTable
import com.jervisffb.engine.rules.builder.FoulActionBehavior
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.builder.KickingPlayerBehavior
import com.jervisffb.engine.rules.builder.UseApothecaryBehavior
import com.jervisffb.engine.rules.common.actions.PlayerAction
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.skills.SpecialActionProvider
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * This file contains the standard rules for BB2020 games (and its variants).
 * - Standard (Strict)
 * - Standard (FUMBBL-Compatible)
 * - BB7
 */
@Serializable
abstract class BB2020Rules(
    private val bb2020RuleParameters: RulesParametersHolder
) : Rules(bb2020RuleParameters) {

    override fun getAvailableActions(state: Game, player: Player): List<PlayerAction> {
        if (state.activePlayer != player) INVALID_GAME_STATE("$player is not the active player")
        if (player.location !is OnPitchLocation) return emptyList()
        return buildList {
            // Add any team actions that are available
            state.activeTeamOrThrow().turnData.let { turnData ->
                if (turnData.moveActions > 0) add(teamActions.move)
                if (turnData.passActions > 0 && turnData.throwTeamMateActions == teamActions.throwTeamMate.availablePrTurn) {
                    // Pass and Throw Team-mate are mutually exclusive
                    add(teamActions.pass)
                }
                if (turnData.handOffActions > 0) add(teamActions.handOff)
                if (turnData.blockActions > 0) {
                    val isStanding = (player.state == PlayerState.STANDING)
                    val hasEligibleTargets = (player.location as OnPitchLocation)
                        .getSurroundingCoordinates(this@BB2020Rules, 1)
                        .mapNotNull { state.pitch[it].player }
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
                        .filter { targetPlayer ->  targetPlayer.location.isOnPitch(this@BB2020Rules) }
                        .any {  targetPlayer -> isStanding(targetPlayer) }

                    if (hasEligibleBlitzTargets) {
                        add(teamActions.blitz)
                    }
                }
                if (turnData.foulActions > 0) {
                    val hasEligibleFoulTargets = player.team.otherTeam()
                        .filter { targetPlayer ->  targetPlayer.location.isOnPitch(this@BB2020Rules) }
                        .any {  targetPlayer -> targetPlayer.state == PlayerState.PRONE || targetPlayer.state == PlayerState.STUNNED }
                    if (hasEligibleFoulTargets) {
                        add(teamActions.foul)
                    }
                }
                if (
                    turnData.throwTeamMateActions > 0
                    && turnData.usedStandardActions[PlayerStandardActionType.PASS] == 0
                    && player.hasSkill(SkillType.THROW_TEAMMATE)
                ) {
                    // Throw Team-mate and Pass are mutually exclusive
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
                        val enemiesInRange = ball.coordinates.getSurroundingCoordinates(
                            rules = this@BB2020Rules,
                            distance = 2,
                            includeOutOfBounds = false
                        ).any { coordinate ->
                            state.pitch[coordinate].player?.let { p->
                                (p.team != player.team) && this@BB2020Rules.canMarkPlayers(p)
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
                val skill = it as? Skill<*>
                val isSkillAvailable = (skill != null && player.isSkillAvailable(skill.type))
                val type = it.specialAction
                val isSkillActionUsed = it.isSpecialActionUsed
                val isActionAvailable = state.activeTeamOrThrow().turnData.availableSpecialActions[type]!! > 0
                if (isSkillAvailable && !isSkillActionUsed && isActionAvailable) {
                    add(teamActions[type])
                }
            }
        }
    }

    companion object {
        val DEFAULTS = RulesParametersHolder(
            name = "Blood Bowl 2020 Rules",
            baseVersion = GameVersion.BB2020,
            gameType = GameType.STANDARD,
            foulActionBehavior = FoulActionBehavior.BB2020,
            inducements = InducementSettings(DEFAULT_INDUCEMENTS_BB2020),
        )
    }

    // In BB2020, Open status does not consider if the player is standing or not.
    // This is probably and oversight that has been fixed in BB20205. To avoid
    // too many weird edge cases in BB2020, we also assume the same semantics
    // here.
    @Suppress("RedundantOverride")
    override fun isOpen(player: Player): Boolean {
        return super.isOpen(player)
    }
}

@Serializable
class StandardBB2020Rules(
    private val standardBB2020RuleParameters: RulesParametersHolder = DEFAULTS
) : BB2020Rules(standardBB2020RuleParameters) {

    companion object {
        val DEFAULTS = BB2020Rules.DEFAULTS.copy(
            name = "Blood Bowl 2020 Rules (Strict)",
        )
    }

    /**
     * Returns an updated copy of the current ruleset.
     * The original ruleset is not modified.
     */
    fun update(block: StandardBB2020RulesBuilder.() -> Unit): StandardBB2020Rules {
        return toBuilder().apply(block).build()
    }

    // Builder API infrastructure
    override fun toBuilder() = StandardBB2020RulesBuilder(standardBB2020RuleParameters)
    class StandardBB2020RulesBuilder(parameters: RulesParameters): RulesParameterBuilder(parameters) {
        override fun build(): StandardBB2020Rules = StandardBB2020Rules(buildParameters())
    }
}

/**
 * Ruleset that is compatible with the way FUMBBL organizes its rules.
 * While it generally follows the rules as written, there are minor differences.
 *
 * - KickOff: No need to select the kicking player. This is done automatically.
 *   Priority will be given to a legal player with "Kick".
 * - Foul: Player is not selected when starting the action.
 * - A more lenient timing system, so the opponents must time out each other.
 */
@Serializable
class FumbblBB2020Rules(
    private val fumbblBB2020RuleParameters: RulesParametersHolder = DEFAULTS
) : BB2020Rules(fumbblBB2020RuleParameters) {
    companion object {
        val DEFAULTS = BB2020Rules.DEFAULTS.copy(
            name = "Blood Bowl 2020 Rules (FUMBBL Compatible)",
            kickingPlayerBehavior = KickingPlayerBehavior.FUMBBL,
            foulActionBehavior = FoulActionBehavior.BB2025,
        )
    }

    // Builder API infrastructure
    override fun toBuilder() = FumbblBB2020RulesBuilder(fumbblBB2020RuleParameters)
    class FumbblBB2020RulesBuilder(parameters: RulesParameters): RulesParameterBuilder(parameters) {
        override fun build() = FumbblBB2020Rules(buildParameters())
    }
}

/**
 * Ruleset for the 2020 Blood Bowl Sevens game.
 * See Dungeon Bowl rulebook page 90 for more information.
 */
@Serializable
class BB72020Rules(
    private val bb72020RuleParameters: RulesParametersHolder = DEFAULTS
) : BB2020Rules(bb72020RuleParameters) {

    companion object {
        val DEFAULTS = BB2020Rules.DEFAULTS.copy(
            name = "Blood Bowl Sevens 2020 Rules",
            gameType = GameType.BB7,
            pitchWidth = 20,
            pitchHeight = 11,
            wideZone = 2,
            endZone = 1,
            lineOfScrimmageHome = 6,
            lineOfScrimmageAway = 13,
            playersRequiredOnLineOfScrimmage = 3,
            maxPlayersInWideZone = 1,
            maxPlayersOnPitch = 7,
            turnsPrHalf = 6,
            kickOffEventTable = BB7KickOffEventTable,
            injuryTable = BB7StandardInjuryTable,
            stuntyInjuryTable = BB7StuntyInjuryTable,
            prayersToNuffleTable = BB7PrayersToNuffleTable,
            useApothecaryBehavior = UseApothecaryBehavior.BB7,
            inducements = InducementSettings(DEFAULT_INDUCEMENTS_BB2020).toBuilder().run {
                InducementType.entries.forEach { type ->
                    when (type) {
                        InducementType.TEMP_AGENCY_CHEERLEADER -> {
                            get(type)!!.let {
                                it.price = 30_000
                                it.max = 2
                            }
                        }

                        InducementType.PART_TIME_ASSISTANT_COACH -> {
                            get(type)!!.let {
                                it.price = 30_000
                                it.max = 1
                            }
                        }

                        InducementType.WEATHER_MAGE -> get(type)!!.enabled = false
                        InducementType.BLOODWEISER_KEG -> { /* Do nothing */
                        }

                        InducementType.SPECIAL_PLAY -> { /* Do nothing */
                        }

                        InducementType.EXTRA_TEAM_TRAINING -> get(type)!!.price = 150_000
                        InducementType.BRIBE -> { /* Do nothing */
                        }

                        InducementType.WANDERING_APOTHECARY -> { /* Do nothing */
                        }

                        InducementType.MORTUARY_ASSISTANT -> { /* Do nothing */
                        }

                        InducementType.PLAGUE_DOCTOR -> { /* Do nothing */
                        }

                        InducementType.RIOTOUS_ROOKIE -> get(type)!!.enabled = false
                        InducementType.HALFLING_MASTER_CHEF -> { /* Do nothing */
                        }

                        InducementType.STANDARD_MERCENARY_PLAYERS -> { /* Do nothing */
                        }

                        InducementType.STAR_PLAYERS -> get(type)!!.enabled = false
                        InducementType.INFAMOUS_COACHING_STAFF -> get(type)!!.enabled = false
                        InducementType.WIZARD -> get(type)!!.enabled = false
                        InducementType.BIASED_REFEREE -> get(type)!!.enabled = false
                        InducementType.WAAAGH_DRUMMER -> get(type)!!.enabled = false
                        InducementType.CAVORTING_NURGLINGS -> get(type)!!.enabled = false
                        InducementType.DWARFEN_RUNESMITH -> get(type)!!.enabled = false
                        InducementType.HALFLING_HOTPOT -> get(type)!!.enabled = false
                        InducementType.MASTER_OF_BALLISTICS -> get(type)!!.enabled = false
                        InducementType.EXPANDED_MERCENARY_PLAYERS -> { /* Do nothing */
                        }

                        InducementType.GIANT -> get(type)!!.enabled = false
                        InducementType.DESPERATE_MEASURES -> {
                            get(type)!!.let {
                                it.enabled = true
                                it.price = 50_000
                                it.max = 5
                            }
                        }

                        InducementType.PRAYERS_TO_NUFFLE,
                        InducementType.TEAM_MASCOT,
                        InducementType.BLITZERS_BEST_KEGS,
                        InducementType.BRETONNIAN_PASTRIES,
                        InducementType.BRETONNIAN_DAMSEL,
                        InducementType.CANOPIC_JAR -> { /* Ignore */
                        }
                    }
                }
                build()
            }
        )


    }

    // Builder API infrastructure
    override fun toBuilder() = BB72020RulesBuilder(bb72020RuleParameters)
    class BB72020RulesBuilder(parameters: RulesParameters): RulesParameterBuilder(parameters) {
        override fun build() = BB72020Rules(buildParameters())
    }
}
