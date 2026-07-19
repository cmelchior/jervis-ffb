package com.jervisffb.engine.rules

import com.jervisffb.engine.InducementSettings
import com.jervisffb.engine.TimerSettings
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PitchType
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.OnPitchLocation
import com.jervisffb.engine.rules.bb2020.BB2020SkillSettings
import com.jervisffb.engine.rules.bb2020.BB2020TeamActions
import com.jervisffb.engine.rules.bb2020.DEFAULT_INDUCEMENTS_BB2020
import com.jervisffb.engine.rules.bb2020.procedures.BB2020TheKickOffEvent
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.StandardBlockStep
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.JumpStep
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.BB2020FallingOver
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.BB2020KnockedDown
import com.jervisffb.engine.rules.bb2020.skills.Leader
import com.jervisffb.engine.rules.bb2020.tables.BB2020ArgueTheCallTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020CasualtyTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020LastingInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020RangeRuler
import com.jervisffb.engine.rules.bb2020.tables.BB2020StandardInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020StandardKickOffEventTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020StandardPrayersToNuffleTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020StandardWeatherTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020StuntyInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.BB7KickOffEventTable
import com.jervisffb.engine.rules.bb2020.tables.BB7PrayersToNuffleTable
import com.jervisffb.engine.rules.bb2020.tables.BB7StandardInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.BB7StuntyInjuryTable
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.builder.FoulActionBehavior
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.builder.KickingPlayerBehavior
import com.jervisffb.engine.rules.builder.NoStadium
import com.jervisffb.engine.rules.builder.StandardBall
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.rules.builder.UseApothecaryBehavior
import com.jervisffb.engine.rules.common.actions.PlayerAction
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.pathfinder.StandardPathFinder
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.procedures.DummyProcedure
import com.jervisffb.engine.rules.common.procedures.rerolls.BB2020BrilliantCoachingReroll
import com.jervisffb.engine.rules.common.procedures.rerolls.BB2020StandardTeamReroll
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.BB2020CheeringFans
import com.jervisffb.engine.rules.common.rerolls.TeamReroll
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.skills.SpecialActionProvider
import com.jervisffb.engine.rules.common.tables.RandomDirectionTemplate
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import com.jervisffb.engine.rules.bb2020.procedures.TeamTurn as BB2020TeamTurn
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.PassStep as BB2020PassStep
import com.jervisffb.engine.rules.bb2020.procedures.actions.throwteammate.ThrowPlayerStep as BB2020ThrowPlayerStep

/**
 * This file contains the standard rules for BB2020 games (and its variants).
 * - Standard (Strict)
 * - Standard (FUMBBL-Compatible)
 * - BB7
 *
 * The rule classes are intentionally _NOT_ @Serializable. Marking this
 * cross-module abstract subclass @Serializable trips a Kotlin 2.4 codegen bug
 * in SyntheticAccessorLowering (`Index out of bounds` on the cross-module
 *  delegating-constructor call). Instead we use custom serializer
 *  implementations.
 *
 *  TODO Create a reproducer example for this bug and report to Kotlin.
 */
abstract class BB2020Rules(
    val parameters: RulesParametersHolder
) : AbstractRules(parameters) {

    override fun isDistracted(player: Player): Boolean {
        // Distracted is not a concept in BB2020, so we always return false.
        return false
    }

    override fun isRerollAllowed(dicePool: List<DieRoll<*>>): Boolean {
        // It is only allowed to reroll a die a single time. So if a rerollSource
        // exists, it cannot be rerolled again, but if some reroll can reroll some
        // of the other dice that is allowed.
        return !dicePool.all { it.rerollSource != null }
    }

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
                    val isStanding = (player.state == PlayerPitchState.STANDING)
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
                        .any {  targetPlayer -> targetPlayer.state == PlayerPitchState.PRONE || targetPlayer.state == PlayerPitchState.STUNNED }
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

    override fun createTeamReroll(team: Team, index: Int): TeamReroll {
        return BB2020StandardTeamReroll(team.id, index)
    }

    override fun createBrilliantCoachingReroll(team: Team): TeamReroll {
        return BB2020BrilliantCoachingReroll(team.id)
    }

    override fun createLeaderTeamReroll(team: Team): TeamReroll {
        INVALID_GAME_STATE("Leader not supported in BB2020")
    }

    override fun isLeaderReroll(reroll: TeamReroll): Boolean {
        return false // Leader is not supported in BB2020
    }



    companion object {
        val DEFAULTS = RulesParametersHolder(
            name = "Blood Bowl 2020 Rules",
            baseVersion = GameVersion.BB2020,
            gameType = GameType.STANDARD,
            timers = TimerSettings.BB_CLOCK,
            inducements = InducementSettings(
                topDogTopUpLimitFromTreasury = Int.MAX_VALUE,
                underdogTopUpLimitFromTreasury = Int.MAX_VALUE,
                inducements = DEFAULT_INDUCEMENTS_BB2020
            ),
            moveRange = 1..9,
            strengthRange = 1..8,
            agilityRange = 1..6,
            passingRange = 1..6,
            armorValueRange = 3..11,
            halfsPrGame = 2,
            turnsPrHalf = 8,
            hasExtraTime = false,
            turnsInExtraTime = 8,
            hasShootoutInExtraTime = true,
            pitchWidth = 26,
            pitchHeight = 15,
            wideZone = 4,
            endZone = 1,
            lineOfScrimmageHome = 12,
            lineOfScrimmageAway = 13,
            playersRequiredOnLineOfScrimmage = 3,
            maxPlayersInWideZone = 2,
            maxPlayersOnPitch = 11,
            stadium = NoStadium,
            ballSelectorRule = StandardBall,
            pitchType = PitchType.STANDARD,
            matchEventsEnabled = false,
            kickOffEventTable = BB2020StandardKickOffEventTable,
            prayersToNufflePriceForUnderdog = 50_000,
            prayersToNuffleEnabledForUnderdogDuringPregame = true,
            prayersToNuffleTable = BB2020StandardPrayersToNuffleTable,
            weatherTable = BB2020StandardWeatherTable,
            injuryTable = BB2020StandardInjuryTable,
            stuntyInjuryTable = BB2020StuntyInjuryTable,
            casualtyTable = BB2020CasualtyTable,
            lastingInjuryTable = BB2020LastingInjuryTable,
            argueTheCallTable = BB2020ArgueTheCallTable,
            randomDirectionTemplate = RandomDirectionTemplate,
            rangeRuler = BB2020RangeRuler,
            teamActions = BB2020TeamActions(),
            rushesPrAction = 2,
            allowMultipleTeamRerollsPrTurn = true,
            standingUpTarget = 4,
            moveRequiredForStandingUp = 3,
            secureTheBallTarget = 2,
            pathFinder = StandardPathFinder(),
            undoActionBehavior = UndoActionBehavior.ONLY_NON_RANDOM_ACTIONS,
            diceRollsOwner = DiceRollOwner.ROLL_ON_SERVER,
            foulActionBehavior = FoulActionBehavior.BB2020,
            kickingPlayerBehavior = KickingPlayerBehavior.STRICT,
            useApothecaryBehavior = UseApothecaryBehavior.STANDARD,
            skillSettings = BB2020SkillSettings(),
            allowPlayerEditsDuringGame = false,
            canUseMultipleRerollsOnDicePools = true,
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

    // Ruleset-specific procedures / nodes
    @Transient override val standardBlockStep: Procedure = StandardBlockStep
    @Transient override val fallingOverStep: Procedure = BB2020FallingOver
    @Transient override val knockedDownStep: Procedure = BB2020KnockedDown
    @Transient override val kickOffTouchBackNode: Node = BB2020TheKickOffEvent.TouchBack
    @Transient override val jumpStep: Procedure = JumpStep
    @Transient override val teamTurn: Procedure = BB2020TeamTurn
    @Transient override val passStep: Procedure = BB2020PassStep
    @Transient override val throwPlayerStep: Procedure = BB2020ThrowPlayerStep
    @Transient override val cheeringFansStep: Procedure = BB2020CheeringFans

    // Not supported in BB2020 right now, so just ignore them
    // We should probably refactor the rules, so we do not need them here.
    @Transient override val leapStep: Procedure = DummyProcedure
    @Transient override val pogoStep: Procedure = DummyProcedure
    @Transient override val secureTheBallStep: Procedure = DummyProcedure
    @Transient override val shadowingStep: Procedure = DummyProcedure
    @Transient override val tentaclesStep: Procedure = DummyProcedure
    @Transient override val hitAndRunStep: Procedure = DummyProcedure
    @Transient override val hailMaryPassStep: Procedure = DummyProcedure
    @Transient override val applyInducementsStep: Procedure = DummyProcedure
    @Transient override val chainsawFoulStep: Procedure = DummyProcedure

    override val rightStuffMaxStrength: Int = 3
    override fun calculateLeaderRerollStatusChange(team: Team): Command? =
        Leader.removeLeaderRerollIfNotAvailable(team)

//    override fun addMultipleBlockInjuryReference(
//        state: Game,
//        player: Player,
//        injuryContext: com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext,
//    ): Command =
//        state.getContext<BB2020MultipleBlockContext>().addInjuryReferenceForPlayer(player, injuryContext)
//    // PlacedProne / Leap / Pogo / Secure the Ball / Shadowing / Tentacles
//    // are BB2025-only; the DummyProcedure defaults from `Rules` apply here.
}

@Serializable(with = StandardBB2020RulesSerializer::class)
class StandardBB2020Rules(
    parameters: RulesParametersHolder = DEFAULTS
) : BB2020Rules(parameters) {

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
    override fun toBuilder() = StandardBB2020RulesBuilder(parameters)
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
@Serializable(with = FumbblBB2020RulesSerializer::class)
class FumbblBB2020Rules(
    parameters: RulesParametersHolder = DEFAULTS
) : BB2020Rules(parameters) {
    companion object {
        val DEFAULTS = BB2020Rules.DEFAULTS.copy(
            name = "Blood Bowl 2020 Rules (FUMBBL Compatible)",
            kickingPlayerBehavior = KickingPlayerBehavior.FUMBBL,
            foulActionBehavior = FoulActionBehavior.BB2025,
        )
    }

    // Builder API infrastructure
    override fun toBuilder() = FumbblBB2020RulesBuilder(parameters)
    class FumbblBB2020RulesBuilder(parameters: RulesParameters): RulesParameterBuilder(parameters) {
        override fun build() = FumbblBB2020Rules(buildParameters())
    }
}

/**
 * Ruleset for the 2020 Blood Bowl Sevens game.
 * See Dungeon Bowl rulebook page 90 for more information.
 */
@Serializable(with = BB72020RulesSerializer::class)
class BB72020Rules(
    parameters: RulesParametersHolder = DEFAULTS
) : BB2020Rules(parameters) {

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
            inducements = InducementSettings(
                topDogTopUpLimitFromTreasury = Int.MAX_VALUE,
                underdogTopUpLimitFromTreasury = Int.MAX_VALUE,
                inducements = DEFAULT_INDUCEMENTS_BB2020
            ).toBuilder().run {
                InducementType.entries.forEach { type ->
                    when (type) {
                        InducementType.TEMP_AGENCY_CHEERLEADER -> {
                            getSingle(type).let {
                                it.price = 30_000
                                it.max = 2
                            }
                        }
                        InducementType.PART_TIME_ASSISTANT_COACH -> {
                            getSingle(type).let {
                                it.price = 30_000
                                it.max = 1
                            }
                        }
                        InducementType.WEATHER_MAGE -> getSingle(type).enabled = false
                        InducementType.BLOODWEISER_KEG -> { /* Do nothing */ }
                        InducementType.SPECIAL_PLAY -> { /* Do nothing */ }
                        InducementType.EXTRA_TEAM_TRAINING -> getSingle(type).price = 150_000
                        InducementType.BRIBE -> { /* Do nothing */ }
                        InducementType.WANDERING_APOTHECARY -> { /* Do nothing */ }
                        InducementType.MORTUARY_ASSISTANT -> { /* Do nothing */ }
                        InducementType.PLAGUE_DOCTOR -> { /* Do nothing */ }
                        InducementType.RIOTOUS_ROOKIE -> getSingle(type).enabled = false
                        InducementType.HALFLING_MASTER_CHEF -> { /* Do nothing */ }
                        InducementType.STANDARD_MERCENARY_PLAYERS -> { /* Do nothing */ }
                        InducementType.STAR_PLAYERS -> getInducement(type).enabled = false
                        InducementType.INFAMOUS_COACHING_STAFF -> getInducement(type).enabled = false
                        InducementType.WIZARD -> getInducement(type).enabled = false
                        InducementType.BIASED_REFEREE -> getInducement(type).enabled = false
                        InducementType.WAAAGH_DRUMMER -> getInducement(type).enabled = false
                        InducementType.CAVORTING_NURGLINGS -> getInducement(type).enabled = false
                        InducementType.DWARFEN_RUNESMITH -> getInducement(type).enabled = false
                        InducementType.HALFLING_HOTPOT -> getInducement(type).enabled = false
                        InducementType.MASTER_OF_BALLISTICS -> getInducement(type).enabled = false
                        InducementType.EXPANDED_MERCENARY_PLAYERS -> { /* Do nothing */ }
                        InducementType.GIANT -> getSingle(type).enabled = false
                        InducementType.DESPERATE_MEASURES -> {
                            getSingle(type).let {
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
                        InducementType.CANOPIC_JAR -> { /* Ignore */ }
                    }
                }
                build()
            }
        )


    }

    // Builder API infrastructure
    override fun toBuilder() = BB72020RulesBuilder(parameters)
    class BB72020RulesBuilder(parameters: RulesParameters): RulesParameterBuilder(parameters) {
        override fun build() = BB72020Rules(buildParameters())
    }
}

// -----------------------------------------------------------------------
// Custom serializers for the concrete BB2020 Rules subclasses.
//
// Each Rules instance is fully described by its [RulesParametersHolder],
// so the serializer delegates to that data class's auto-generated
// serializer. This avoids putting `@Serializable` on the cross-module
// abstract [BB2020Rules] class, which trips a Kotlin 2.4 codegen bug in
// SyntheticAccessorLowering.
// -----------------------------------------------------------------------
object StandardBB2020RulesSerializer : KSerializer<StandardBB2020Rules> {
    private val delegate = RulesParametersHolder.serializer()
    override val descriptor: SerialDescriptor =
        SerialDescriptor("com.jervisffb.engine.rules.StandardBB2020Rules", delegate.descriptor)
    override fun serialize(encoder: Encoder, value: StandardBB2020Rules) =
        delegate.serialize(encoder, value.parameters)
    override fun deserialize(decoder: Decoder): StandardBB2020Rules =
        StandardBB2020Rules(delegate.deserialize(decoder))
}

object FumbblBB2020RulesSerializer : KSerializer<FumbblBB2020Rules> {
    private val delegate = RulesParametersHolder.serializer()
    override val descriptor: SerialDescriptor =
        SerialDescriptor("com.jervisffb.engine.rules.FumbblBB2020Rules", delegate.descriptor)
    override fun serialize(encoder: Encoder, value: FumbblBB2020Rules) =
        delegate.serialize(encoder, value.parameters)
    override fun deserialize(decoder: Decoder): FumbblBB2020Rules =
        FumbblBB2020Rules(delegate.deserialize(decoder))
}

object BB72020RulesSerializer : KSerializer<BB72020Rules> {
    private val delegate = RulesParametersHolder.serializer()
    override val descriptor: SerialDescriptor =
        SerialDescriptor("com.jervisffb.engine.rules.BB72020Rules", delegate.descriptor)
    override fun serialize(encoder: Encoder, value: BB72020Rules) =
        delegate.serialize(encoder, value.parameters)
    override fun deserialize(decoder: Decoder): BB72020Rules =
        BB72020Rules(delegate.deserialize(decoder))
}
