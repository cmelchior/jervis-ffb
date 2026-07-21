package com.jervisffb.engine.bb2025

import com.jervisffb.engine.InducementSettings
import com.jervisffb.engine.TimerSettings
import com.jervisffb.engine.bb2025.commands.ResetShadowingSkill
import com.jervisffb.engine.bb2025.commands.SetWasOnPitchDuringDrive
import com.jervisffb.engine.bb2025.procedures.BB2025TheKickOffEvent
import com.jervisffb.engine.bb2025.procedures.actions.block.HitAndRunStep
import com.jervisffb.engine.bb2025.procedures.actions.block.singleblock.SingleStandardBlockStep
import com.jervisffb.engine.bb2025.procedures.actions.foul.ChainsawFoulStep
import com.jervisffb.engine.bb2025.procedures.actions.move.JumpStep
import com.jervisffb.engine.bb2025.procedures.actions.move.LeapStep
import com.jervisffb.engine.bb2025.procedures.actions.move.PogoStep
import com.jervisffb.engine.bb2025.procedures.actions.pass.HailMaryPassStep
import com.jervisffb.engine.bb2025.procedures.actions.securetheball.SecureTheBallStep
import com.jervisffb.engine.bb2025.procedures.injury.BB2025FallingOver
import com.jervisffb.engine.bb2025.procedures.injury.BB2025KnockedDown
import com.jervisffb.engine.bb2025.procedures.rerolls.BrilliantCoachingReroll
import com.jervisffb.engine.bb2025.procedures.rerolls.LeaderTeamReroll
import com.jervisffb.engine.bb2025.procedures.rerolls.StandardTeamReroll
import com.jervisffb.engine.bb2025.procedures.table.kickoff.BB2025CheeringFans
import com.jervisffb.engine.bb2025.skills.Leader
import com.jervisffb.engine.bb2025.skills.LoneFouler
import com.jervisffb.engine.bb2025.skills.SecretWeapon
import com.jervisffb.engine.bb2025.skills.ShadowingStep
import com.jervisffb.engine.bb2025.skills.TentaclesStep
import com.jervisffb.engine.bb2025.tables.BB2025ArgueTheCallTable
import com.jervisffb.engine.bb2025.tables.BB2025CasualtyTable
import com.jervisffb.engine.bb2025.tables.BB2025LastingInjuryTable
import com.jervisffb.engine.bb2025.tables.BB2025RangeRuler
import com.jervisffb.engine.bb2025.tables.BB2025StandardInjuryTable
import com.jervisffb.engine.bb2025.tables.BB2025StandardKickOffEventTable
import com.jervisffb.engine.bb2025.tables.BB2025StandardPrayersToNuffleTable
import com.jervisffb.engine.bb2025.tables.BB2025StandardWeatherTable
import com.jervisffb.engine.bb2025.tables.BB2025StuntyInjuryTable
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.common.AbstractRules
import com.jervisffb.engine.common.pathfinder.StandardPathFinder
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PitchType
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.getSkillOrNull
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.OnPitchLocation
import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import com.jervisffb.engine.rules.RulesParameterBuilder
import com.jervisffb.engine.rules.RulesParameters
import com.jervisffb.engine.rules.RulesParametersHolder
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.builder.FoulActionBehavior
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.builder.KickingPlayerBehavior
import com.jervisffb.engine.rules.builder.NoStadium
import com.jervisffb.engine.rules.builder.StandardBall
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.rules.builder.UseApothecaryBehavior
import com.jervisffb.engine.rules.common.SetupRule
import com.jervisffb.engine.rules.common.TeamCaptainNotOnPitch
import com.jervisffb.engine.rules.common.actions.PlayerAction
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.rerolls.TeamReroll
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import com.jervisffb.engine.rules.common.skills.RerollSource
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
import com.jervisffb.engine.bb2025.procedures.TeamTurn as BB2025TeamTurn
import com.jervisffb.engine.bb2025.procedures.actions.pass.PassStep as BB2025PassStep
import com.jervisffb.engine.bb2025.procedures.actions.throwteammate.ThrowPlayerStep as BB2025ThrowPlayerStep
import com.jervisffb.engine.bb2025.procedures.inducements.ApplyInducements as BB2025ApplyInducements

/**
 * Top-level class for all variants of the 2025 Blood Bowl rules.
 *
 * NOTE: intentionally NOT @Serializable. Marking this cross-module abstract
 * subclass @Serializable trips a Kotlin 2.4 codegen bug in
 * SyntheticAccessorLowering (`Index out of bounds` on the cross-module
 * delegating-constructor call). Concrete subclasses below use custom
 * [KSerializer] implementations instead.
 */
abstract class BB2025Rules(
    val parameters: RulesParametersHolder
) : AbstractRules(parameters) {

    override fun isDistracted(player: Player): Boolean {
        // In BB2025, Distracted is modeled as a condition on a player.
        // Note that the status effect and lack of tackle zones are modeled
        // independently.
        return player.statusEffects.any { it.type == PlayerStatusEffectType.DISTRACTED }
    }

    override fun isRerollAllowed(dicePool: List<DieRoll<*>>): Boolean {
        // In BB2025, as soon as a single die in a dice pool has been rerolled,
        // no other rerolls are allowed.
        return dicePool.none { it.rerollSource != null }
    }

    override fun isSetupValid(state: Game, team: Team): List<SetupRule> {
        val setupErrors = super.isSetupValid(state, team).toMutableList()
        val rules = state.rules
        // If a Team has a Team Captain, he must be placed on the pitch
        // if possible
        val (inReserve, onPitch, notAvailable) = team
            .filter { it.specialRules.contains(PlayerSpecialRule.TEAM_CAPTAIN) }
            .fold(
                initial = Triple(mutableListOf<Player>(), mutableListOf<Player>(), mutableListOf<Player>())
            ) { data, player ->
                when {
                    player.location.isOnPitch(rules) -> data.second.add(player)
                    !player.location.isOnPitch(rules) && player.state == PlayerDogoutState.RESERVE -> data.first.add(player)
                    else -> data.third.add(player)
                }
                data
            }

        if (onPitch.isEmpty() && inReserve.isNotEmpty()) {
            setupErrors.add(TeamCaptainNotOnPitch(inReserve.map { it.id }))
        }

        return setupErrors
    }

    override fun getAvailableActions(state: Game, player: Player): List<PlayerAction> {
        if (state.activePlayer != player) INVALID_GAME_STATE("$player is not the active player")
        if (player.location !is OnPitchLocation) return emptyList()
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
                    val isStanding = (player.state == PlayerPitchState.STANDING)
                    // Jump Up can only be used on Block Actions, not Special Actions
                    val hasJumpUp = player.isSkillAvailable(SkillType.JUMP_UP) && player.state == PlayerPitchState.PRONE
                    val hasEligibleTargets = (player.location as OnPitchLocation)
                        .getSurroundingCoordinates(this@BB2025Rules, 1)
                        .mapNotNull { state.pitch[it].player }
                        .filter { otherPlayer -> otherPlayer.team != player.team }
                        .filter { otherPlayer -> isStanding(otherPlayer)}
                        .any { otherPlayer ->
                            isMarking(player, otherPlayer) || hasJumpUp
                        }
                    if ((isStanding || hasJumpUp) && hasEligibleTargets) {
                        add(teamActions.block)
                    }
                }
                if (turnData.blitzActions > 0) {
                    val hasEligibleBlitzTargets = player.team.otherTeam()
                        .filter { targetPlayer ->  targetPlayer.location.isOnPitch(this@BB2025Rules) }
                        .any {  targetPlayer -> isStanding(targetPlayer) }

                    if (hasEligibleBlitzTargets) {
                        add(teamActions.blitz)
                    }
                }
                if (turnData.foulActions > 0) {
                    val hasEligibleFoulTargets = player.team.otherTeam()
                        .filter { targetPlayer ->  targetPlayer.location.isOnPitch(this@BB2025Rules) }
                        .any {  targetPlayer -> targetPlayer.state == PlayerPitchState.PRONE || targetPlayer.state == PlayerPitchState.STUNNED }
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
                        val enemiesInRange = ball.coordinates.getSurroundingCoordinates(
                            rules = this@BB2025Rules,
                            distance = 2,
                            includeOutOfBounds = false
                        ).any { coordinate ->
                            state.pitch[coordinate].player?.let { p->
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

    override fun createTeamReroll(team: Team, index: Int): TeamReroll {
        return StandardTeamReroll(team.id, index)
    }

    override fun createBrilliantCoachingReroll(team: Team): TeamReroll {
        return BrilliantCoachingReroll(team.id)
    }

    override fun createLeaderTeamReroll(team: Team): TeamReroll {
        return LeaderTeamReroll(team.id)
    }

    override fun isLeaderReroll(reroll: TeamReroll): Boolean {
        return (reroll is LeaderTeamReroll)
    }

    // Ruleset-specific procedures / nodes
    @Transient override val standardBlockStep: Procedure = SingleStandardBlockStep
    @Transient override val fallingOverStep: Procedure = BB2025FallingOver
    @Transient override val knockedDownStep: Procedure = BB2025KnockedDown
    @Transient override val kickOffTouchBackNode: Node = BB2025TheKickOffEvent.TouchBack
    @Transient override val jumpStep: Procedure = JumpStep
    @Transient override val leapStep: Procedure = LeapStep
    @Transient override val pogoStep: Procedure = PogoStep
    @Transient override val secureTheBallStep: Procedure = SecureTheBallStep
    @Transient override val shadowingStep: Procedure = ShadowingStep
    @Transient override val tentaclesStep: Procedure = TentaclesStep
    @Transient override val hitAndRunStep: Procedure = HitAndRunStep
    @Transient override val hailMaryPassStep: Procedure = HailMaryPassStep
    @Transient override val teamTurn: Procedure = BB2025TeamTurn
    @Transient override val passStep: Procedure = BB2025PassStep
    @Transient override val throwPlayerStep: Procedure = BB2025ThrowPlayerStep
    @Transient override val applyInducementsStep: Procedure = BB2025ApplyInducements
    @Transient override val cheeringFansStep: Procedure = BB2025CheeringFans
    @Transient override val chainsawFoulStep: Procedure = ChainsawFoulStep

    override val rightStuffMaxStrength: Int = Int.MAX_VALUE
    override fun calculateLeaderRerollStatusChange(team: Team): Command? = Leader.calculateLeaderRerollStatusChange(team)

    override fun getEndOfTurnResetCommands(player: Player): List<Command> = buildList {
        if (player.hasSkill(SkillType.SHADOWING)) {
            add(ResetShadowingSkill(player))
        }
    }

    override fun wasSecretWeaponOnPitchDuringDrive(player: Player): Boolean {
        return player.getSkillOrNull<SecretWeapon>()?.onPitchDuringDrive == true
    }

    override fun setSecretWeaponOnPitchDuringDrive(player: Player, onPitch: Boolean): Command? {
        val skill = player.getSkillOrNull<SecretWeapon>() ?: return null
        return SetWasOnPitchDuringDrive(skill, onPitch = onPitch)
    }

    override fun getLoneFoulerRerollSource(player: Player): RerollSource? {
        return player.getSkillOrNull<LoneFouler>()
    }

//    override fun addMultipleBlockInjuryReference(
//        state: Game,
//        player: Player,
//        injuryContext: com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext,
//    ): Command =
//        state.getContext<BB2025MultipleBlockContext>().addInjuryReferenceForPlayer(player, injuryContext)

    companion object {
        val DEFAULTS = RulesParametersHolder(
            name = "Blood Bowl 2025 Rules",
            baseVersion = GameVersion.BB2025,
            gameType = GameType.STANDARD,
            timers = TimerSettings.BB_CLOCK,
            inducements = InducementSettings(
                topDogTopUpLimitFromTreasury = Int.MAX_VALUE,
                underdogTopUpLimitFromTreasury = 50_000,
                inducements = DEFAULT_INDUCEMENTS_BB2025
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
            kickOffEventTable = BB2025StandardKickOffEventTable,
            prayersToNufflePriceForUnderdog = 50_000,
            prayersToNuffleEnabledForUnderdogDuringPregame = true,
            prayersToNuffleTable = BB2025StandardPrayersToNuffleTable,
            weatherTable = BB2025StandardWeatherTable,
            injuryTable = BB2025StandardInjuryTable,
            stuntyInjuryTable = BB2025StuntyInjuryTable,
            casualtyTable = BB2025CasualtyTable,
            lastingInjuryTable = BB2025LastingInjuryTable,
            argueTheCallTable = BB2025ArgueTheCallTable,
            randomDirectionTemplate = RandomDirectionTemplate,
            rangeRuler = BB2025RangeRuler,
            teamActions = BB2025TeamActions(),
            rushesPrAction = 2,
            allowMultipleTeamRerollsPrTurn = true,
            standingUpTarget = 4,
            moveRequiredForStandingUp = 3,
            secureTheBallTarget = 2,
            pathFinder = StandardPathFinder(),
            undoActionBehavior = UndoActionBehavior.ONLY_NON_RANDOM_ACTIONS,
            diceRollsOwner = DiceRollOwner.ROLL_ON_SERVER,
            foulActionBehavior = FoulActionBehavior.BB2025,
            kickingPlayerBehavior = KickingPlayerBehavior.STRICT,
            useApothecaryBehavior = UseApothecaryBehavior.STANDARD,
            skillSettings = BB2025SkillSettings(),
            allowPlayerEditsDuringGame = false,
            canUseMultipleRerollsOnDicePools = false,
        )
    }
}

@Serializable(with = StandardBB2025RulesSerializer::class)
class StandardBB2025Rules(
    parameters: RulesParametersHolder = DEFAULTS
) : BB2025Rules(parameters) {

    companion object {
        val DEFAULTS = BB2025Rules.DEFAULTS.copy(
            name = "Blood Bowl 2025 Rules (Strict)",
            prayersToNuffleEnabledForUnderdogDuringPregame = false
        )
    }

    /**
     * Returns an updated copy of the current ruleset.
     * The original ruleset is not modified.
     */
    fun update(block: StandardBB2025RulesBuilder.() -> Unit): StandardBB2025Rules {
        return toBuilder().apply(block).build()
    }

    // Builder API infrastructure
    override fun toBuilder() = StandardBB2025RulesBuilder(parameters)
    class StandardBB2025RulesBuilder(parameters: RulesParameters): RulesParameterBuilder(parameters) {
        override fun build(): StandardBB2025Rules = StandardBB2025Rules(buildParameters())
    }
}

// -----------------------------------------------------------------------
// Custom serializers for the concrete BB2025 Rules subclasses.
//
// Each Rules instance is fully described by its [RulesParametersHolder],
// so the serializer delegates to that data class's auto-generated
// serializer. This avoids putting `@Serializable` on the cross-module
// abstract [BB2025Rules] class, which trips a Kotlin 2.4 codegen bug in
// SyntheticAccessorLowering.
// -----------------------------------------------------------------------

object StandardBB2025RulesSerializer : KSerializer<StandardBB2025Rules> {
    private val delegate = RulesParametersHolder.serializer()
    override val descriptor: SerialDescriptor =
        SerialDescriptor("com.jervisffb.engine.rules.StandardBB2025Rules", delegate.descriptor)
    override fun serialize(encoder: Encoder, value: StandardBB2025Rules) =
        delegate.serialize(encoder, value.parameters)
    override fun deserialize(decoder: Decoder): StandardBB2025Rules =
        StandardBB2025Rules(delegate.deserialize(decoder))
}
