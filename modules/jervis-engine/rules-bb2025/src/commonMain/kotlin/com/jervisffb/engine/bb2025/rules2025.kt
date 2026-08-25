package com.jervisffb.engine.bb2025

import com.jervisffb.engine.InducementSettings
import com.jervisffb.engine.TimerSettings
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.bb2025.commands.ResetShadowingSkill
import com.jervisffb.engine.bb2025.commands.SetWasOnPitchDuringDrive
import com.jervisffb.engine.bb2025.inducements.InducementType2025
import com.jervisffb.engine.bb2025.modifiers.PlayerStatusEffectType2025
import com.jervisffb.engine.bb2025.procedures.AnimalSavageryStep
import com.jervisffb.engine.bb2025.procedures.BB7KickOffDeviateRoll
import com.jervisffb.engine.bb2025.procedures.BoneHeadRoll
import com.jervisffb.engine.bb2025.procedures.GameDrive
import com.jervisffb.engine.bb2025.procedures.ReallyStupidRoll
import com.jervisffb.engine.bb2025.procedures.TakeRootRoll
import com.jervisffb.engine.bb2025.procedures.TheKickOffEvent2025
import com.jervisffb.engine.bb2025.procedures.UnchannelledFuryRoll
import com.jervisffb.engine.bb2025.procedures.actions.block.HitAndRunStep
import com.jervisffb.engine.bb2025.procedures.actions.block.singleblock.SingleStandardBlockStep
import com.jervisffb.engine.bb2025.procedures.actions.foul.ArgueTheCallRoll
import com.jervisffb.engine.bb2025.procedures.actions.foul.BeingSentOff
import com.jervisffb.engine.bb2025.procedures.actions.foul.ChainsawFoulStep
import com.jervisffb.engine.bb2025.procedures.actions.move.DodgeRoll
import com.jervisffb.engine.bb2025.procedures.actions.move.JumpStep
import com.jervisffb.engine.bb2025.procedures.actions.move.LeapStep
import com.jervisffb.engine.bb2025.procedures.actions.move.MovePlayerIntoSquare
import com.jervisffb.engine.bb2025.procedures.actions.move.PogoStep
import com.jervisffb.engine.bb2025.procedures.actions.move.RushRoll
import com.jervisffb.engine.bb2025.procedures.actions.pass.HailMaryPassStep
import com.jervisffb.engine.bb2025.procedures.actions.securetheball.SecureTheBallStep
import com.jervisffb.engine.bb2025.procedures.injury.FallingOver2025
import com.jervisffb.engine.bb2025.procedures.injury.KnockedDown2025
import com.jervisffb.engine.bb2025.procedures.injury.PatchUpPlayer
import com.jervisffb.engine.bb2025.procedures.injury.RiskingInjuryRoll
import com.jervisffb.engine.bb2025.procedures.rerolls.BrilliantCoachingReroll
import com.jervisffb.engine.bb2025.procedures.rerolls.LeaderTeamReroll
import com.jervisffb.engine.bb2025.procedures.rerolls.StandardTeamReroll
import com.jervisffb.engine.bb2025.skills.Leader
import com.jervisffb.engine.bb2025.skills.LoneFouler
import com.jervisffb.engine.bb2025.skills.SecretWeapon
import com.jervisffb.engine.bb2025.skills.ShadowingStep
import com.jervisffb.engine.bb2025.skills.TentaclesStep
import com.jervisffb.engine.bb2025.tables.ArgueTheCallTable2025
import com.jervisffb.engine.bb2025.tables.BB7ArgueTheCallTable
import com.jervisffb.engine.bb2025.tables.BB7DesperateMeasuresTable
import com.jervisffb.engine.bb2025.tables.BB7KickOffEventTable
import com.jervisffb.engine.bb2025.tables.BB7PrayersToNuffleTable
import com.jervisffb.engine.bb2025.tables.BB7StandardInjuryTable
import com.jervisffb.engine.bb2025.tables.BB7StuntyInjuryTable
import com.jervisffb.engine.bb2025.tables.CasualtyTable2025
import com.jervisffb.engine.bb2025.tables.LastingInjuryTable2025
import com.jervisffb.engine.bb2025.tables.RangeRuler2025
import com.jervisffb.engine.bb2025.tables.StandardInjuryTable2025
import com.jervisffb.engine.bb2025.tables.StandardKickOffEventTable2025
import com.jervisffb.engine.bb2025.tables.StandardPrayersToNuffleTable
import com.jervisffb.engine.bb2025.tables.StandardWeatherTable2025
import com.jervisffb.engine.bb2025.tables.StuntyInjuryTable2025
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.common.AbstractRules
import com.jervisffb.engine.common.inducements.InducementTypeCommon
import com.jervisffb.engine.common.modifiers.PlayerStatusEffectTypeCommon
import com.jervisffb.engine.common.planner.ActionPlannerCommon
import com.jervisffb.engine.common.procedures.DeviateRoll
import com.jervisffb.engine.common.tables.DisabledCasualtyTable
import com.jervisffb.engine.common.tables.DisabledLastingInjuryTable
import com.jervisffb.engine.common.utils.endActionImmediately
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
import com.jervisffb.engine.rules.JUMP_DISTANCE
import com.jervisffb.engine.rules.RulesParameterBuilder
import com.jervisffb.engine.rules.RulesParameters
import com.jervisffb.engine.rules.RulesParametersHolder
import com.jervisffb.engine.rules.SPRINT_EXTRA_RUSHES
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
abstract class Rules2025(
    val parameters: RulesParametersHolder
) : AbstractRules(parameters) {

    override fun isDistracted(player: Player): Boolean {
        // In BB2025, Distracted is modeled as a condition on a player.
        // Note that the status effect and lack of tackle zones are modeled
        // independently.
        return player.statusEffects.any { it.type == PlayerStatusEffectType2025.DISTRACTED }
    }

    override fun isSkillAvailable(player: Player, type: SkillType): Boolean {
        return player.getSkillOrNull(type)?.let { skill ->

            // Check for Distracted
            val notWorkingWhenDistracted = player.statusEffects.any { it.type == PlayerStatusEffectType2025.DISTRACTED } && !skill.workWithoutTackleZones
            if (notWorkingWhenDistracted) {
                return false
            }

            // Check for missing tackle zones (not sure this is possible without being distracted in BB2025)
            val notWorkingWithNoTackleZones = player.intermediateState == null
                && player.state == PlayerPitchState.STANDING
                && !player.hasTackleZones
                && !skill.workWithoutTackleZones
            if (notWorkingWithNoTackleZones) {
                return false
            }

            // Check for Prone state
            // TODO Is a Stunned player considered Prone or are they completely separate?
            val state = player.state
            val isProne = (state == PlayerPitchState.PRONE || state == PlayerPitchState.STUNNED || state == PlayerPitchState.STUNNED_OWN_TURN)
            if (isProne && !skill.workWhenProne) {
                return@let false
            }
            return !skill.used
        } ?: false
    }

    override fun calculateMoveTypesAvailable(state: Game, player: Player): SelectMoveType? {
        if (state.endActionImmediately()) {
            return null
        }

        // A dev-mode edit can move the active player to the dugout while the
        // activation procedure is still on the move-selection node. There are no
        // movement options for a player that is not on the pitch, and checking this
        // here avoids accessing Player.coordinates for a dugout player.
        if (!player.location.isOnPitch(this)) {
            return null
        }
        val options = mutableListOf<MoveType>()

        // Stand up
        if (player.state == PlayerPitchState.PRONE) {
            options.add(MoveType.STAND_UP)
        }

        // Rooted or Chomped players cannot leave their current square, so exit early.
        val isRooted = player.statusEffects.any { it.type == PlayerStatusEffectTypeCommon.ROOTED }
        val isChomped = player.statusEffects.any { it.type == PlayerStatusEffectType2025.CHOMPED }
        if (isRooted || isChomped) {
            return when (options.isNotEmpty()) {
                true -> SelectMoveType(options)
                false -> null
            }
        }

        // Normal move (with a potential rush)
        // Sprint is still optional, but here we assume it will be used if needed
        val extraSprintRush = if (player.isSkillAvailable(SkillType.SPRINT)) SPRINT_EXTRA_RUSHES else 0
        if (player.movesLeft + player.rushesLeft + extraSprintRush >= 1 && isStanding(player)) {
            options.add(MoveType.STANDARD)
        }

        // Jump, if next to a prone player and space on the opposite side
        val hasMoveLeft = player.movesLeft + player.rushesLeft + extraSprintRush >= JUMP_DISTANCE && isStanding(player)
        val legalJumpSquares = player.coordinates.getSurroundingCoordinates(this, distance = 1)
            .mapNotNull { state.pitch[it].player }
            .filter { !isStanding(it) }
            .any {
                // A jumping player can only jump to the same squares you would normally push the player
                // to. See page 56 in the BB2025 rulebook.
                getPushOptions(player, it).any { coords ->
                    coords.isOnPitch(this) && state.pitch[coords].isUnoccupied()
                }
            }

        if (hasMoveLeft && legalJumpSquares) {
            options.add(MoveType.JUMP)
        }

        // Leap and Pogo
        val allSquares = player.coordinates.getSurroundingCoordinates(this, distance = JUMP_DISTANCE)
        val adjacentSquares = player.coordinates.getSurroundingCoordinates(this, distance = 1)
        val legalLeapSquares = (allSquares - adjacentSquares.toSet()).any { state.pitch[it].isUnoccupied() }
        if (hasMoveLeft && legalLeapSquares && player.isSkillAvailable(SkillType.LEAP)) {
            options.add(MoveType.LEAP)
        }
        if (hasMoveLeft && legalLeapSquares && player.isSkillAvailable(SkillType.POGO_STICK)) {
            options.add(MoveType.POGO)
        }

        return if (options.isNotEmpty()) SelectMoveType(options) else null
    }

    override fun canOfferAssist(assister: Player, target: Player): Boolean {
        if (assister.statusEffects.any { it.type == PlayerStatusEffectType2025.EYE_GOUGE }) return false
        return super.canOfferAssist(assister, target)
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
                        .getSurroundingCoordinates(this@Rules2025, 1)
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
                        .filter { targetPlayer ->  targetPlayer.location.isOnPitch(this@Rules2025) }
                        .any {  targetPlayer -> isStanding(targetPlayer) }

                    if (hasEligibleBlitzTargets) {
                        add(teamActions.blitz)
                    }
                }
                if (turnData.foulActions > 0) {
                    val hasEligibleFoulTargets = player.team.otherTeam()
                        .filter { targetPlayer ->  targetPlayer.location.isOnPitch(this@Rules2025) }
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
                            rules = this@Rules2025,
                            distance = 2,
                            includeOutOfBounds = false
                        ).any { coordinate ->
                            state.pitch[coordinate].player?.let { p->
                                (p.team != player.team) && this@Rules2025.canMarkPlayers(p)
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
                if (it.isActionAvailable(state, this@Rules2025)) {
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
    @Transient override val fallingOverStep: Procedure = FallingOver2025
    @Transient override val knockedDownStep: Procedure = KnockedDown2025
    @Transient override val kickOffTouchBackNode: Node = TheKickOffEvent2025.TouchBack
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
    @Transient override val chainsawFoulStep: Procedure = ChainsawFoulStep
    @Transient override val kickOffDeviateRollStep: Procedure = DeviateRoll
    @Transient override val rushRoll: Procedure = RushRoll
    @Transient override val gameDrive: Procedure = GameDrive
    @Transient override val beingSentOff: Procedure = BeingSentOff
    @Transient override val movePlayerIntoSquare: Procedure = MovePlayerIntoSquare
    @Transient override val patchUpPlayer: Procedure = PatchUpPlayer
    @Transient override val riskingInjuryRoll: Procedure = RiskingInjuryRoll
    @Transient override val dodgeRoll: Procedure = DodgeRoll
    @Transient override val takeRootRoll: Procedure = TakeRootRoll
    @Transient override val boneHeadRoll: Procedure = BoneHeadRoll
    @Transient override val reallyStupidRoll: Procedure = ReallyStupidRoll
    @Transient override val unchannelledFuryRoll: Procedure = UnchannelledFuryRoll
    @Transient override val animalSavageryStep: Procedure = AnimalSavageryStep
    @Transient override val argueTheCallRoll: Procedure = ArgueTheCallRoll

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
//        state.getContext<MultipleBlockContext2025>().addInjuryReferenceForPlayer(player, injuryContext)

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
            kickOffEventTable = StandardKickOffEventTable2025,
            prayersToNufflePriceForUnderdog = 50_000,
            prayersToNuffleEnabledForUnderdogDuringPregame = false,
            prayersToNuffleTable = StandardPrayersToNuffleTable,
            desperateMeasuresTable = BB7DesperateMeasuresTable,
            weatherTable = StandardWeatherTable2025,
            injuryTable = StandardInjuryTable2025,
            stuntyInjuryTable = StuntyInjuryTable2025,
            casualtyTable = CasualtyTable2025,
            lastingInjuryTable = LastingInjuryTable2025,
            argueTheCallTable = ArgueTheCallTable2025,
            randomDirectionTemplate = RandomDirectionTemplate,
            rangeRuler = RangeRuler2025,
            teamActions = TeamActions2025(),
            rushesPrAction = 2,
            allowMultipleTeamRerollsPrTurn = true,
            standingUpTarget = 4,
            moveRequiredForStandingUp = 3,
            secureTheBallTarget = 2,
            actionPlanner = ActionPlannerCommon,
            undoActionBehavior = UndoActionBehavior.ONLY_NON_RANDOM_ACTIONS,
            diceRollsOwner = DiceRollOwner.ROLL_ON_SERVER,
            foulActionBehavior = FoulActionBehavior.BB2025,
            kickingPlayerBehavior = KickingPlayerBehavior.STRICT,
            useApothecaryBehavior = UseApothecaryBehavior.STANDARD,
            skillSettings = SkillSettings2025(),
            allowPlayerEditsDuringGame = false,
            canUseMultipleRerollsOnDicePools = false,
        )
    }
}

@Serializable(with = StandardBB2025RulesSerializer::class)
class StandardBB2025Rules(
    parameters: RulesParametersHolder = DEFAULTS
) : Rules2025(parameters) {

    companion object {
        val DEFAULTS = Rules2025.DEFAULTS.copy(
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

/**
 * Ruleset for the 2020 Blood Bowl Sevens game.
 * See Dungeon Bowl rulebook page 90 for more information.
 */
@Serializable(with = BB7RulesSerializer2025::class)
class BB7Rules2025(
    parameters: RulesParametersHolder = DEFAULTS
) : Rules2025(parameters) {

    companion object {
        val DEFAULTS = Rules2025.DEFAULTS.copy(
            name = "Blood Bowl Sevens 2025 Rules",
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
            casualtyTable = DisabledCasualtyTable,
            lastingInjuryTable = DisabledLastingInjuryTable,
            prayersToNuffleTable = BB7PrayersToNuffleTable,
            argueTheCallTable = BB7ArgueTheCallTable,
            useApothecaryBehavior = UseApothecaryBehavior.BB7,
            inducements = InducementSettings(
                topDogTopUpLimitFromTreasury = Int.MAX_VALUE,
                underdogTopUpLimitFromTreasury = Int.MAX_VALUE,
                inducements = DEFAULT_INDUCEMENTS_BB2025
            ).toBuilder().run {
                InducementTypeCommon.entries.forEach { type ->
                    when (type) {
                        InducementTypeCommon.TEMP_AGENCY_CHEERLEADER -> {
                            getSingle(type).let {
                                it.price = 15_000
                                it.max = 2
                            }
                        }
                        InducementTypeCommon.PART_TIME_ASSISTANT_COACH -> {
                            getSingle(type).let {
                                it.price = 15_000
                                it.max = 2
                            }
                        }
                        InducementTypeCommon.WEATHER_MAGE -> getSingle(type).enabled = false
                        InducementTypeCommon.EXTRA_TEAM_TRAINING -> {
                            getSingle(type).let {
                                it.price = 125_000
                                it.max = 6
                            }
                        }
                        InducementTypeCommon.BRIBE -> {
                            getSingle(type).let {
                                it.price = 100_000
                                it.max = 2
                                // TODO Reduce price for Bribery and Corruption teams
                            }
                        }
                        InducementTypeCommon.WANDERING_APOTHECARY -> {
                            getSingle(type).let {
                                it.price = 100_000
                                it.max = 1
                            }
                        }
                        InducementTypeCommon.MORTUARY_ASSISTANT -> {
                            getSingle(type).let {
                                it.price = 100_000
                                it.max = 1
                            }
                        }
                        InducementTypeCommon.PLAGUE_DOCTOR -> {
                            getSingle(type).let {
                                it.price = 100_000
                                it.max = 1
                            }
                        }
                        InducementTypeCommon.RIOTOUS_ROOKIE -> getSingle(type).enabled = false
                        InducementTypeCommon.HALFLING_MASTER_CHEF -> {
                            getSingle(type).let {
                                it.price = 300_000
                                it.max = 1
                                // TODO Reduce price for Hafling teams
                            }
                        }
                        InducementTypeCommon.STANDARD_MERCENARY_PLAYERS -> getInducement(type).enabled = false
                        InducementTypeCommon.STAR_PLAYERS -> getGroup(type).enabled = false
                        InducementTypeCommon.INFAMOUS_COACHING_STAFF -> getGroup(type).enabled = false
                        InducementTypeCommon.WIZARD -> getGroup(type).enabled = false
                        InducementTypeCommon.BIASED_REFEREE -> getGroup(type).enabled = false
                        InducementTypeCommon.DESPERATE_MEASURES -> {
                            getSingle(type).let {
                                it.enabled = true
                                it.price = 50_000
                                it.max = 5
                            }
                        }
                    }

                }
                InducementType2025.entries.forEach { type ->
                    when (type) {
                        InducementType2025.PRAYERS_TO_NUFFLE -> {
                            getSingle(type).let {
                                it.enabled = true
                                it.price = 5_000
                                it.max = 2
                            }
                        }
                        InducementType2025.TEAM_MASCOT -> getSingle(type).enabled = false
                        InducementType2025.BLITZERS_BEST_KEGS -> {
                            getSingle(type).let {
                                it.enabled = true
                                it.price = 50_000
                                it.max = 2
                            }
                        }
                    }
                }
                build()
            }
        )


    }

    @Transient
    override val kickOffDeviateRollStep: Procedure = BB7KickOffDeviateRoll

    /**
     * Returns an updated copy of the current ruleset.
     * The original ruleset is not modified.
     */
    fun update(block: BB7RulesBuilder2025.() -> Unit): BB7Rules2025 {
        return toBuilder().apply(block).build()
    }

    // Builder API infrastructure
    override fun toBuilder() = BB7RulesBuilder2025(parameters)
    class BB7RulesBuilder2025(parameters: RulesParameters): RulesParameterBuilder(parameters) {
        override fun build() = BB7Rules2025(buildParameters())
    }
}

// -----------------------------------------------------------------------
// Custom serializers for the concrete BB2025 Rules subclasses.
//
// Each Rules instance is fully described by its [RulesParametersHolder],
// so the serializer delegates to that data class's auto-generated
// serializer. This avoids putting `@Serializable` on the cross-module
// abstract [Rules2025] class, which trips a Kotlin 2.4 codegen bug in
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

object BB7RulesSerializer2025 : KSerializer<BB7Rules2025> {
    private val delegate = RulesParametersHolder.serializer()
    override val descriptor: SerialDescriptor =
        SerialDescriptor("com.jervisffb.engine.rules.BB7Rules2025", delegate.descriptor)
    override fun serialize(encoder: Encoder, value: BB7Rules2025) =
        delegate.serialize(encoder, value.parameters)
    override fun deserialize(decoder: Decoder): BB7Rules2025 =
        BB7Rules2025(delegate.deserialize(decoder))
}
