package com.jervisffb.ui.game

import com.jervis.generated.SettingsKeys
import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.AdminGameAction
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.bb2025.procedures.actions.move.LeapStep
import com.jervisffb.engine.bb2025.procedures.actions.move.PogoStep
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.common.procedures.ActivatePlayer
import com.jervisffb.engine.common.procedures.StartOfDriveSequence
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rng.DiceRollGenerator
import com.jervisffb.engine.rng.UnsafeRandomDiceGenerator
import com.jervisffb.engine.rules.JUMP_DISTANCE
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.utils.InvalidActionException
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.game.animations.AnimationFactory
import com.jervisffb.ui.game.animations.JervisAnimation
import com.jervisffb.ui.game.model.UiPitchPlayer
import com.jervisffb.ui.game.model.UiPitchSquare
import com.jervisffb.ui.game.state.UiActionProviderGroup
import com.jervisffb.ui.game.state.actionwheel.AccuracyBB2020WheelController
import com.jervisffb.ui.game.state.actionwheel.AccuracyBB2025PassWheelController
import com.jervisffb.ui.game.state.actionwheel.AccuracyBB2025ThrowTeamMateWheelController
import com.jervisffb.ui.game.state.actionwheel.AlwaysHungrySquirmFreeWheelController
import com.jervisffb.ui.game.state.actionwheel.AlwaysHungryWheelController
import com.jervisffb.ui.game.state.actionwheel.AnimalSavageryWheelController
import com.jervisffb.ui.game.state.actionwheel.ArgueTheCallRollWheelController
import com.jervisffb.ui.game.state.actionwheel.ArgueTheCallWheelController
import com.jervisffb.ui.game.state.actionwheel.AwayTeamFanFactorRoll
import com.jervisffb.ui.game.state.actionwheel.BoneHeadWheelController
import com.jervisffb.ui.game.state.actionwheel.BounceBallRollWheelController
import com.jervisffb.ui.game.state.actionwheel.BouncePlayerRollWheelController
import com.jervisffb.ui.game.state.actionwheel.BreatheFireWheelController
import com.jervisffb.ui.game.state.actionwheel.BribeRollWheelController
import com.jervisffb.ui.game.state.actionwheel.BrilliantCoachingKickingTeamRollWheelController
import com.jervisffb.ui.game.state.actionwheel.BrilliantCoachingReceivingTeamRollWheelController
import com.jervisffb.ui.game.state.actionwheel.CatchWheelController
import com.jervisffb.ui.game.state.actionwheel.ChainsawWheelController
import com.jervisffb.ui.game.state.actionwheel.ChargePlayersRollWheelController
import com.jervisffb.ui.game.state.actionwheel.CheeringFansKickingTeamRollWheelController
import com.jervisffb.ui.game.state.actionwheel.CheeringFansReceivingTeamRollWheelController
import com.jervisffb.ui.game.state.actionwheel.ChompWheelController
import com.jervisffb.ui.game.state.actionwheel.ChooseAlternativeToMascotWheelController
import com.jervisffb.ui.game.state.actionwheel.ChooseKickingTeamWheelController
import com.jervisffb.ui.game.state.actionwheel.CoinTossWheelController
import com.jervisffb.ui.game.state.actionwheel.DauntlessWheelController
import com.jervisffb.ui.game.state.actionwheel.DeviateRollWheelController
import com.jervisffb.ui.game.state.actionwheel.DodgeWheelController
import com.jervisffb.ui.game.state.actionwheel.DodgySnackEffectOnKickingTeamRollWheelController
import com.jervisffb.ui.game.state.actionwheel.DodgySnackEffectOnReceivingTeamRollWheelController
import com.jervisffb.ui.game.state.actionwheel.DodgySnackKickingTeamRollWheelController
import com.jervisffb.ui.game.state.actionwheel.DodgySnackReceivingTeamRollWheelController
import com.jervisffb.ui.game.state.actionwheel.FollowUpWheelController
import com.jervisffb.ui.game.state.actionwheel.FoulAppearanceWheelController
import com.jervisffb.ui.game.state.actionwheel.HomeTeamFanFactorRoll
import com.jervisffb.ui.game.state.actionwheel.HypnoticGazeWheelController
import com.jervisffb.ui.game.state.actionwheel.InterceptionWheelController
import com.jervisffb.ui.game.state.actionwheel.JumpUpWheelController
import com.jervisffb.ui.game.state.actionwheel.JumpWheelController
import com.jervisffb.ui.game.state.actionwheel.KickoffEventWheelController
import com.jervisffb.ui.game.state.actionwheel.LandingWheelController
import com.jervisffb.ui.game.state.actionwheel.LeapWheelController
import com.jervisffb.ui.game.state.actionwheel.LonerWheelController
import com.jervisffb.ui.game.state.actionwheel.PickupWheelController
import com.jervisffb.ui.game.state.actionwheel.PitchInvasionKickingTeamPlayersAffectedRollWheelController
import com.jervisffb.ui.game.state.actionwheel.PitchInvasionKickingTeamRollWheelController
import com.jervisffb.ui.game.state.actionwheel.PitchInvasionReceivingTeamPlayersAffectedRollWheelController
import com.jervisffb.ui.game.state.actionwheel.PitchInvasionReceivingTeamRollWheelController
import com.jervisffb.ui.game.state.actionwheel.PogoWheelController
import com.jervisffb.ui.game.state.actionwheel.ProWheelController
import com.jervisffb.ui.game.state.actionwheel.ProjectileVomitWheelController
import com.jervisffb.ui.game.state.actionwheel.PuntDirectionWheelController
import com.jervisffb.ui.game.state.actionwheel.PuntDistanceWheelController
import com.jervisffb.ui.game.state.actionwheel.QuickSnapRollWheelController
import com.jervisffb.ui.game.state.actionwheel.ReallyStupidWheelController
import com.jervisffb.ui.game.state.actionwheel.RecoverPlayerRollWheelController
import com.jervisffb.ui.game.state.actionwheel.RegenerationInducementReRollWheelController
import com.jervisffb.ui.game.state.actionwheel.RegenerationWheelController
import com.jervisffb.ui.game.state.actionwheel.RushWheelController
import com.jervisffb.ui.game.state.actionwheel.ScatterRollWheelController
import com.jervisffb.ui.game.state.actionwheel.SecureTheBallWheelController
import com.jervisffb.ui.game.state.actionwheel.SelectBlockTypeWheelController
import com.jervisffb.ui.game.state.actionwheel.SelectBrawlerDieWheelController
import com.jervisffb.ui.game.state.actionwheel.SelectCoinSideWheelController
import com.jervisffb.ui.game.state.actionwheel.SelectPlayerActionWheelController
import com.jervisffb.ui.game.state.actionwheel.SelectProDieWheelController
import com.jervisffb.ui.game.state.actionwheel.ShadowingWheelController
import com.jervisffb.ui.game.state.actionwheel.SolidDefenseWheelController
import com.jervisffb.ui.game.state.actionwheel.StandardBlockChooseResultOrRerollWheelController
import com.jervisffb.ui.game.state.actionwheel.StandardBlockRollWheelController
import com.jervisffb.ui.game.state.actionwheel.SteadyFootingWheelController
import com.jervisffb.ui.game.state.actionwheel.SwoopDirectionWheelController
import com.jervisffb.ui.game.state.actionwheel.SwoopDistanceWheelController
import com.jervisffb.ui.game.state.actionwheel.TakeRootWheelController
import com.jervisffb.ui.game.state.actionwheel.TeamCaptainWheelController
import com.jervisffb.ui.game.state.actionwheel.TeamMascotWheelController
import com.jervisffb.ui.game.state.actionwheel.TentaclesWheelController
import com.jervisffb.ui.game.state.actionwheel.ThrowInWheelController
import com.jervisffb.ui.game.state.actionwheel.UnchannelledFuryWheelController
import com.jervisffb.ui.game.state.actionwheel.UseApothecaryWheelController
import com.jervisffb.ui.game.state.actionwheel.UseBigHandWheelController
import com.jervisffb.ui.game.state.actionwheel.UseBlockWheelController
import com.jervisffb.ui.game.state.actionwheel.UseBribeWheelController
import com.jervisffb.ui.game.state.actionwheel.UseBullseyeWheelController
import com.jervisffb.ui.game.state.actionwheel.UseChainsawWheelController
import com.jervisffb.ui.game.state.actionwheel.UseClawsWheelController
import com.jervisffb.ui.game.state.actionwheel.UseDirtyPlayerWheelController
import com.jervisffb.ui.game.state.actionwheel.UseDivingCatchWheelController
import com.jervisffb.ui.game.state.actionwheel.UseDodgeWheelController
import com.jervisffb.ui.game.state.actionwheel.UseEyeGougeWheelController
import com.jervisffb.ui.game.state.actionwheel.UseFendWheelController
import com.jervisffb.ui.game.state.actionwheel.UseFumblerooskiWheelController
import com.jervisffb.ui.game.state.actionwheel.UseGrabWheelController
import com.jervisffb.ui.game.state.actionwheel.UseHitAndRunWheelController
import com.jervisffb.ui.game.state.actionwheel.UseIronHardSkinWheelController
import com.jervisffb.ui.game.state.actionwheel.UseKickWheelController
import com.jervisffb.ui.game.state.actionwheel.UseLeapWheelController
import com.jervisffb.ui.game.state.actionwheel.UseLethalFlightWheelController
import com.jervisffb.ui.game.state.actionwheel.UseLoneFoulerWheelController
import com.jervisffb.ui.game.state.actionwheel.UseMightyBlowController
import com.jervisffb.ui.game.state.actionwheel.UseMortuaryAssistantWheelController
import com.jervisffb.ui.game.state.actionwheel.UsePileDriverWheelController
import com.jervisffb.ui.game.state.actionwheel.UsePlagueDoctorWheelController
import com.jervisffb.ui.game.state.actionwheel.UseSafePairOfHandsWheelController
import com.jervisffb.ui.game.state.actionwheel.UseSafePassWheelController
import com.jervisffb.ui.game.state.actionwheel.UseSidestepWheelController
import com.jervisffb.ui.game.state.actionwheel.UseSneakyGitWheelController
import com.jervisffb.ui.game.state.actionwheel.UseSprintWheelController
import com.jervisffb.ui.game.state.actionwheel.UseStandFirmWheelController
import com.jervisffb.ui.game.state.actionwheel.UseSteadyFootingWheelController
import com.jervisffb.ui.game.state.actionwheel.UseStripBallWheelController
import com.jervisffb.ui.game.state.actionwheel.UseStrongArmWheelController
import com.jervisffb.ui.game.state.actionwheel.UseSureHandsWheelController
import com.jervisffb.ui.game.state.actionwheel.UseSwoopWheelController
import com.jervisffb.ui.game.state.actionwheel.UseTackleWheelController
import com.jervisffb.ui.game.state.actionwheel.UseTauntWheelController
import com.jervisffb.ui.game.state.actionwheel.UseThickSkullWheelController
import com.jervisffb.ui.game.state.actionwheel.UseTwoHeadsWheelController
import com.jervisffb.ui.game.state.actionwheel.UseVeryLongLegsWheelController
import com.jervisffb.ui.game.state.actionwheel.UseWrestleWheelController
import com.jervisffb.ui.game.state.actionwheel.WeatherRollWheelController
import com.jervisffb.ui.game.state.indicators.BallCarriedStatusIndicator
import com.jervisffb.ui.game.state.indicators.BallExitStatusIndicator
import com.jervisffb.ui.game.state.indicators.BallOnGroundStatusIndicator
import com.jervisffb.ui.game.state.indicators.BlockStatusIndicator
import com.jervisffb.ui.game.state.indicators.MoveUsedStatusIndicator
import com.jervisffb.ui.game.state.indicators.PitchStatusIndicator
import com.jervisffb.ui.game.state.indicators.PreGamePlayerAndRefereeStatusIndicator
import com.jervisffb.ui.game.state.indicators.PushDirectionArrowStatusIndicator
import com.jervisffb.ui.game.state.indicators.RecoverPlayerStatusIndicator
import com.jervisffb.ui.game.state.indicators.SwoopDirectionArrowIndicator
import com.jervisffb.ui.game.state.indicators.TeamFeatureStatusIndicator
import com.jervisffb.ui.game.state.indicators.TeamRerollStatusIndicator
import com.jervisffb.ui.game.state.indicators.TeamSetupsAvailableStatusIndicator
import com.jervisffb.ui.game.view.ActionWheelUiState
import com.jervisffb.ui.game.view.ContextWheelUiState
import com.jervisffb.ui.game.view.GameStatusMessageFactory
import com.jervisffb.ui.game.view.HideActionWheel
import com.jervisffb.ui.game.view.NoContextMenu
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.ui.utils.FrameRateAverager
import com.jervisffb.utils.closeIfPossible
import com.jervisffb.utils.jervisLogger
import com.jervisffb.utils.singleThreadDispatcher
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.jervisffb.engine.bb2020.procedures.actions.move.JumpStep as BB2020JumpStep
import com.jervisffb.engine.bb2025.procedures.actions.move.JumpStep as BB2025JumpStep


/**
 * This enums describes what kind of high-level "client" a game is. This can
 * be used to determine what kind of UI to show.
 */
enum class UiGameClientType {
    REPLAY,
    HOTSEAT,
    P2P_CLIENT,
    P2P_HOST,
    HOSTED_GAME,
}

/**
 * This class is the main entry point for holding the UI game state. It acts
 * as the main ViewModel in MVVM.
 *
 * It is responsible for acting as a bridge towards [GameEngineController],
 * which means it should consume all events from there as well as being the only one
 * to send UI actions back to it.
 *
 * This way, we can intercept events and states in both directions and convert them,
 * so they are suitable for being consumed by the UI.
 */
class UiGameController(
    // Which type of client should we create. This affects what kind of UI to show.
    val clientType: UiGameClientType,
    // Which Teams are controlled through this UI controller.
    // This mostly affects UNDO.
    val uiMode: TeamActionMode,
    val gameController: GameEngineController,
    val actionProvider: UiActionProviderGroup,
    val menuViewModel: MenuViewModel,
    private val preloadedActions: List<GameAction>,
    private val focusProvider: UiFocusProvider? = null,
) {

    // Callback triggered after handling a non-admin action
    // We use this to exit certain admin modes. Like moving players freely.
    var onNonAdminAction: () -> Unit = { }

    companion object {
        private val LOG = jervisLogger()
    }

    // Reference to the current rules engine state of the game
    // DO NOT modify the state on this end.
    val state: Game = gameController.state
    val rules: Rules = gameController.rules
    val diceGenerator: DiceRollGenerator = UnsafeRandomDiceGenerator() // Used by UI to create random results. Should this be somewhere else?

    // Persistent UI decorations that need to be stored across actions
    val uiDecorations = UiPersistentGameIndicators()
    private val pitchStatusIndicators: List<PitchStatusIndicator> = listOf(
        BallCarriedStatusIndicator,
        BallExitStatusIndicator,
        BallOnGroundStatusIndicator,
        BlockStatusIndicator,
        MoveUsedStatusIndicator,
        PreGamePlayerAndRefereeStatusIndicator,
        PushDirectionArrowStatusIndicator,
        RecoverPlayerStatusIndicator,
        SwoopDirectionArrowIndicator,
        TeamFeatureStatusIndicator,
        TeamRerollStatusIndicator,
        TeamSetupsAvailableStatusIndicator,
    )
    val actionWheelControllers = setOf(
        AccuracyBB2020WheelController,
        AccuracyBB2025PassWheelController,
        AccuracyBB2025ThrowTeamMateWheelController,
        AlwaysHungryWheelController,
        AlwaysHungrySquirmFreeWheelController,
        AnimalSavageryWheelController,
        BounceBallRollWheelController,
        BouncePlayerRollWheelController,
        BoneHeadWheelController,
        BreatheFireWheelController,
        CatchWheelController,
        ChainsawWheelController,
        ChompWheelController,
        DauntlessWheelController,
        DeviateRollWheelController,
        DodgeWheelController,
        FoulAppearanceWheelController,
        HypnoticGazeWheelController,
        InterceptionWheelController,
        JumpWheelController,
        JumpUpWheelController,
        LandingWheelController,
        LeapWheelController,
        LonerWheelController,
        PickupWheelController,
        PogoWheelController,
        ProWheelController,
        ProjectileVomitWheelController,
        PuntDirectionWheelController,
        PuntDistanceWheelController,
        ReallyStupidWheelController,
        RegenerationWheelController,
        RegenerationInducementReRollWheelController,
        UnchannelledFuryWheelController,
        RushWheelController,
        SecureTheBallWheelController,
        ShadowingWheelController,
        SteadyFootingWheelController,
        TakeRootWheelController,
        SelectPlayerActionWheelController,
        SelectBlockTypeWheelController,
        ScatterRollWheelController,
        SwoopDirectionWheelController,
        SwoopDistanceWheelController,
        TeamCaptainWheelController,
        TeamMascotWheelController,
        TentaclesWheelController,

        StandardBlockRollWheelController,
        StandardBlockChooseResultOrRerollWheelController,
        SelectProDieWheelController,
        SelectBrawlerDieWheelController,

        FollowUpWheelController,
        UseBigHandWheelController,
        UseBlockWheelController,
        UseBullseyeWheelController,
        UseChainsawWheelController,
        UseClawsWheelController,
        UseDirtyPlayerWheelController,
        UseDivingCatchWheelController,
        UseDodgeWheelController,
        UseTwoHeadsWheelController,
        UseEyeGougeWheelController,
        UseFendWheelController,
        UseFumblerooskiWheelController,
        UseGrabWheelController,
        UseHitAndRunWheelController,
        UseIronHardSkinWheelController,
        UseKickWheelController,
        UseLeapWheelController,
        UseLethalFlightWheelController,
        UseLoneFoulerWheelController,
        UsePileDriverWheelController,
        UseMightyBlowController,
        UseSafePairOfHandsWheelController,
        UseSafePassWheelController,
        UseSidestepWheelController,
        UseSneakyGitWheelController,
        UseSprintWheelController,
        UseStandFirmWheelController,
        UseSteadyFootingWheelController,
        UseStripBallWheelController,
        UseStrongArmWheelController,
        UseSureHandsWheelController,
        UseSwoopWheelController,
        UseTackleWheelController,
        UseTauntWheelController,
        UseThickSkullWheelController,
        UseVeryLongLegsWheelController,
        UseWrestleWheelController,

        UseApothecaryWheelController,
        UseMortuaryAssistantWheelController,
        UsePlagueDoctorWheelController,
        ArgueTheCallWheelController,
        UseBribeWheelController,
        ChooseAlternativeToMascotWheelController,
        ArgueTheCallRollWheelController,
        BribeRollWheelController,
        RecoverPlayerRollWheelController,

        // Pre-game / Kick-off Rolls
        HomeTeamFanFactorRoll,
        AwayTeamFanFactorRoll,
        WeatherRollWheelController,
        KickoffEventWheelController,
        ChargePlayersRollWheelController,
        QuickSnapRollWheelController,
        SolidDefenseWheelController,
        CheeringFansKickingTeamRollWheelController,
        CheeringFansReceivingTeamRollWheelController,
        BrilliantCoachingKickingTeamRollWheelController,
        BrilliantCoachingReceivingTeamRollWheelController,
        DodgySnackKickingTeamRollWheelController,
        DodgySnackReceivingTeamRollWheelController,
        DodgySnackEffectOnKickingTeamRollWheelController,
        DodgySnackEffectOnReceivingTeamRollWheelController,
        PitchInvasionKickingTeamRollWheelController,
        PitchInvasionReceivingTeamRollWheelController,
        PitchInvasionKickingTeamPlayersAffectedRollWheelController,
        PitchInvasionReceivingTeamPlayersAffectedRollWheelController,
        ThrowInWheelController,

        // Coin
        SelectCoinSideWheelController,
        CoinTossWheelController,
        ChooseKickingTeamWheelController
    )

    // Dispatcher is held separately from the scope because cancelling a scope does not release the
    // thread behind its dispatcher. It must be closed manually in `stopGameEventLoop()`
    private val animationDispatcher = singleThreadDispatcher("AnimationDispatcher")
    private val animationScope = CoroutineScope(CoroutineName("AnimationScope") + animationDispatcher)

    val gameScope = CoroutineScope(
        Job()
            + CoroutineName("GameLoopScope")
            // + singleThreadDispatcher("GameLoopScope")
            // TODO We cannot share mutableStateOf properties across threads. Moving the entire Game Loop to the Main Thread
            //  fixes it for now. But performance might be a problem. We need to find a performant way to run the game loop
            //  in the background and then offload it all to the Main Thread for rendering
            + Dispatchers.Main
    )

    // Once set, the game loop stops consuming actions. One-way on purpose: an
    // attempt that is over stays over, and "Try Again" builds a whole new game
    // with a new controller, so nothing ever needs to unfreeze this one.
    private var actionsFrozen = false

    // Storing a reference to a UiGameSnap is generally a bad idea as it becomes invalid when the game loop
    // rolls over, but we only use the replay during setting up the UI. After that, we should have all consumers
    // set up correctly and the `replay` is not used.
    val uiStateFlow: Flow<UiGameSnapshot>
        field = MutableSharedFlow<UiGameSnapshot>(replay = 1, onBufferOverflow = BufferOverflow.SUSPEND)

    // While the Action Wheel is part of the UiState, its lifecycle is slightly different, so it  has
    // `replay` is only used to allow the UI to register itself after the game controller has started
    val uiActionWheelFlow: Flow<List<ActionWheelUiState>>
        field = MutableSharedFlow<List<ActionWheelUiState>>(replay = 1, extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.SUSPEND)
    val uiContextWheelFlow: Flow<ContextWheelUiState>
        field = MutableSharedFlow<ContextWheelUiState>(extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.SUSPEND)
    val gameStatusMessageFactory = GameStatusMessageFactory(menuViewModel, state)

    // `replay` is only used to allow the UI to register itself after the game controller has started
    val animationFlow: Flow<JervisAnimation?>
        field = MutableSharedFlow<JervisAnimation?>(replay = 1, onBufferOverflow = BufferOverflow.SUSPEND)

    // Channel used by the UI to indicate when the animation is done
    val animationDone = Channel<Boolean>(capacity = Channel.RENDEZVOUS, onBufferOverflow = BufferOverflow.SUSPEND)

    // Multiplier applied to all animation durations.
    // 0.0 = Animations are disabled.
    // 0.5 = Animations run twice as fast.
    // 1.0 = Animations run at normal speed.
    var animationSpeedFactor: Float = 1f

    // When `true`, the game loop suppresses all UI updates so engine can be advanced silently.
    // Used by Replay Jump-to-Start / Jump-to-End to rewind/forward without the UI flickering.
    var suppressUiUpdates: Boolean = false

    init {
        actionProvider.init(this)
    }

    // Report an invalid action to the user, it should not crash the app
    private fun reportInvalidAction(ex: InvalidActionException) {
        menuViewModel.showReportIssueDialog(
            title = "Invalid action created",
            body = """
                The UI created an action that was rejected by the rules engine.
                State: ${state.stack.stateToPrettyString()}
                ${ex.message}
            """.trimIndent(),
            error = ex,
            gameState = gameController
        )
        LOG.e { "Invalid action selected: ${ex.message}" }
        menuViewModel.lastActionException = ex
    }

    /**
     * Stops the game loop from processing any further actions.
     * This can e.g., be used when displaying End-of-Game or End-of-Challenge
     * dialogs.
     */
    fun freezeActions(freeze: Boolean = true) {
        actionsFrozen = freeze
    }

    /**
     * Start the main game loop.
     *
     * This will start executing the game by setting up receiving updates from
     * [GameEngineController], process them to set up the UI as well as sending back
     * actions.
     *
     * Each execution of the loop can thus be seen as the controller of a single
     * logical "step" of the game. It will run until the game is over.
     *
     * TODO How to handle interruptions, i.e. players accidentally leaving and
     *  rejoining.
     */
    fun startGameEventLoop() {
        val controller = gameController

        // We need to start the Rules Engine first.
        // Do this outside the coroutine to ensure that `startHandler` is called correctly
        // when setting up everything.
        controller.startManualMode()
        actionProvider.startHandler()
        val fpsCounter = FrameRateAverager()

        gameScope.launch {

            // Pre-loaded actions are used to fast-forward to an initial state.
            // We do this before starting the main loop so the UI start from
            // that state.
            // TODO Error handling here?
            preloadedActions.forEach { preloadedAction ->
                gameController.handleAction(preloadedAction)
                actionProvider.actionHandled(null, preloadedAction)
            }

            // Run main game loop
            var lastUiState = UiGameSnapshot(
                actionOwner = null,
                delta = null,
                game = controller.state,
                squares = persistentMapOf(),
                players = persistentMapOf(),
                freeBalls = emptyMap(),
                gameStatusText = null,
                status = UiGameStatusUpdate.INITIAL,
                unknownActions = persistentListOf(),
                homeDogoutOnClickAction = null,
                awayDogoutOnClickAction = null,
                dialogInput = null,
                movesUsed = persistentListOf(),
                weather = Weather.PERFECT_CONDITIONS,
                homeTeamInfo = UiTeamInfoUpdate.INITIAL,
                awayTeamInfo = UiTeamInfoUpdate.INITIAL,
                movePlan = null,
                showReferee = false,
                refereeCoordinates = null,
            )

            while (!controller.stack.isEmpty()) {

                // Read new model state
                val state = controller.state
                val delta = controller.getDelta()
                val actions = controller.getAvailableActions()
                val (previousNode, currentNode) = controller.previousNode() to controller.currentNode()
                val acc = UiSnapshotAccumulator(
                    uiStateFlow = uiStateFlow,
                    uiActionWheelFlow = uiActionWheelFlow,
                    uiContextWheelFlow = uiContextWheelFlow,
                    previousSnapshot = lastUiState,
                    uiController = this@UiGameController
                )

                runPreUpdateAnimations(acc, previousNode, currentNode)

                // Log entries from last action should be added after the animation,
                // so we don't accidentally reveal the result too soon.

                // TODO Run Sound Decorators

                // Update UI State based on latest model state
                actionProvider.prepareForNextAction(controller, actions)
                addBaseGameStateChanges(state, actions, delta, acc)
                applyUiIndicators(actions, controller, acc)
                acc.emitAllUpdates()

                // Detect animations and run them after updating the UI, but before making it ready
                // for creating the user actions
                runPostUpdateStateAnimations(state, acc)

                // TODO Just changing the existing uiState might not trigger recomposition correctly
                //  We need an efficient way to copy the old one.
                actionProvider.decorateAvailableActions(actions, acc)
                acc.let {
                    menuViewModel.updateUiState(it.build())
                    it.emitAllUpdates()
                }

                // Wait for the system to produce the next action, this can either be
                // automatically generated or come from the UI. Here we do not care where
                // it comes from.
                val userAction = run {
                    tailrec suspend fun getNextAcceptedAction(id: GameActionId): GameAction {
                        val action = actionProvider.getAction(id)
                        return when {
                            // When actions are frozen, we still accept them from the UI
                            // but silently drop them, leaving the UI and board state untouched.
                            actionsFrozen -> getNextAcceptedAction(id)
                            else -> action
                        }
                    }
                    getNextAcceptedAction(controller.nextActionIndex())
                }

                // After an action was selected, run all decorators that modify
                // the UI while the action is being processed.
                updatePersistentUiDecorationsBeforeActionUpdate(state, userAction)
                actionProvider.decorateSelectedAction(userAction, acc)
                acc.emitAllUpdates()

                // Then run any animations triggered by the action (but before the state is updated)
                runPostActionSelectedAnimations(controller, userAction, acc)

                // Last, send action to the Rules Engine for processing.
                // This will start the next iteration of the game loop.
                // TODO Add error handling here. What to do for invalid actions?
                lastUiState = acc.build()
                try {
                    val actionWheelLocation = actionWheelControllers.firstOrNull { it.nodes.contains(currentNode) }?.getActionWheelCenter(state)
                    gameController.handleAction(userAction)
                    if (userAction !is AdminGameAction) {
                        onNonAdminAction()
                    }
                    actionProvider.actionHandled(actions.team, userAction)
                    // Now that we know the next node, we can also determine if the Action Wheel
                    // is visible next step, if it isn't, we can hide it immediately.

                    // If Undo'ing actions, this might happen through short-cuts and not the UI.
                    // If this happens while a context menu is open, its state will be left hanging.
                    // In particular `LocalPitchDataWrapper.isContentMenuVisible`. For that reason, we always
                    // reset that state here when the action is Undo or Revert. It also means we do not have
                    // to deal with "back"-animations.
                    val shouldHidePrimaryActionWheel = checkHideActionWheelImmediately(gameController, actionWheelLocation)
                    if (shouldHidePrimaryActionWheel) {
                        val isRevertingState = (userAction is Undo || userAction is Revert)
                        acc.addActionWheelEvent(HideActionWheel(hideImmediately = isRevertingState))
                    }
                    // Always hide any Context Menu still visible
                    acc.addContextWheelEvent(NoContextMenu)
                    acc.emitActionWheelState()

                } catch (ex: InvalidActionException) {
                    reportInvalidAction(ex)
                }
            }
        }.invokeOnCompletion {
            if (it != null && it !is CancellationException) {
                throw it
            }
        }
    }

    /**
     * Counterpart to [startGameEventLoop]. It must be called when a game is
     * stopped to release all resources.
     */
    fun stopGameEventLoop() {
        gameScope.cancel()
        animationScope.cancel()
        animationDispatcher.closeIfPossible()
        actionProvider.stopHandler()
    }

    fun checkHideActionWheelImmediately(gameController: GameEngineController, lastWheelLocation: PitchCoordinate?): Boolean {
        // If both current and previous node had a visible wheel in the same location, we can keep it around
        // Otherwise it should be hidden
        val currentNode = gameController.currentNode()
        val nextController = actionWheelControllers.firstOrNull { it.nodes.contains(currentNode) }
        val nextActionWheelPosition = nextController?.getActionWheelCenter(gameController.state)
        return nextController == null || (lastWheelLocation != null && lastWheelLocation != nextActionWheelPosition)
    }

    private fun applyUiIndicators(actionRequest: ActionRequest, controller: GameEngineController, acc: UiSnapshotAccumulator) {
        val state = controller.state
        val currentNode = controller.currentNode() as ActionNode
        pitchStatusIndicators.forEach { indicator ->
            indicator.decorate(currentNode, state, actionRequest, acc)
        }

        // Set the game status for the current step. For Hotseat games it will always show the message
        // for the active coach. For P2P games, each client will see the message relevant for the respective coach.
        gameStatusMessageFactory.applyMessage(actionProvider, acc)
    }

    // Run animations before we update the UI to represent the state we moved to after applying the last GameAction
    private suspend fun runPreUpdateAnimations(acc: UiSnapshotAccumulator, previousNode: Node?, currentNode: Node?) {

        val currentWheelHandler = actionWheelControllers.firstOrNull { controller ->
            controller.nodes.contains(currentNode)
        }
        if (currentWheelHandler != null && !suppressUiUpdates) {
            if (currentWheelHandler.onApplyCurrentState(
                    acc,
                    gameController.lastAction,
                    previousNode,
                    currentNode!!
                )
            ) {
                acc.emitActionWheelState()
                awaitAnimationCompletion()
            }
        }

        if (isAnimationsEnabled()) {
            val animation = AnimationFactory.getPreUpdateAnimation(state)
            if (animation != null) {
                animationFlow.emit(animation)
                awaitAnimationCompletion()
            }
        }
    }

    private suspend fun runPostUpdateStateAnimations(state: Game, acc: UiSnapshotAccumulator) {
        if (isAnimationsEnabled()) {
            val animation = AnimationFactory.getFrameAnimation(this, state, rules)
            if (animation != null) {
                acc.addActionWheelEvent(HideActionWheel(hideImmediately = true))
                acc.emitActionWheelState()
                animationFlow.emit(animation)
                awaitAnimationCompletion()
                // Enable this to have a promotional logo show up on touchdowns
                //    if (animation is ConfettiAnimation) {
                //        animationFlow.emit(LogoAnimation())
                //        animationDone.receive()
                //    }
            }
        }
    }

    private suspend fun runPostActionSelectedAnimations(
        engineController: GameEngineController,
        action: GameAction,
        acc: UiSnapshotAccumulator
    ) {
        if (isAnimationsEnabled(action)) {
            // Run any animations on Wheel Controllers first, before triggering more custom animations.
            // This is because we want to "finish" rolling dice, before showing the actual result of those dice rolls
            val currentWheelHandler = actionWheelControllers.firstOrNull { controller ->
                controller.nodes.contains(engineController.currentNode())
            }
            if (currentWheelHandler?.onPostActionAnimation(acc, action) == true) {
                acc.emitActionWheelState()
                awaitAnimationCompletion()
            }
            val animation = AnimationFactory.getPostActionAnimation(this, state, action)
            if (animation != null) {
                // We do not want animations to run on top of action wheels, so hide them
                // before running the animation.
                acc.addActionWheelEvent(HideActionWheel(hideImmediately = true))
                acc.emitActionWheelState()
                animationFlow.emit(animation)
                awaitAnimationCompletion()
            }
        }
    }

    /**
     * Method responsible for updating the UI state based on recent changes in the [Game] model.
     */
    private fun addBaseGameStateChanges(state: Game, actions: ActionRequest, delta: GameDelta, acc: UiSnapshotAccumulator) {
        val focus = focusProvider?.getFocus(state) ?: UiFocus.NONE

        // Update the persistent UI decorations before starting
        updatePersistentUiDecorations(state, delta, uiDecorations, acc)

        // Re-render the entire pitch. This feels a bit like overkill, but making it more granular
        // is going to be challenging, and it doesn't look like there is a performance problem doing it.
        (0 until rules.pitchWidth).forEach { x ->
            (0 until rules.pitchHeight).forEach { y ->
                val coordinate = PitchCoordinate(x, y)
                val square = renderSquare(coordinate, state, focus.squares[coordinate])
                acc.addOrUpdateSquare(coordinate, square)
            }
        }

        // This will reset the player state and the data class should ensure equality is
        // checked correctly using the auto-generated `equals()`
        state.homeTeam.forEach { player ->
            acc.addOrUpdatePlayer(
                player.id,
                UiPitchPlayer(player, focusStyle = focus.players[player.id]),
            )
        }
        state.awayTeam.forEach { player ->
            acc.addOrUpdatePlayer(
                player.id,
                UiPitchPlayer(player, focusStyle = focus.players[player.id]),
            )
        }
    }

    private fun updatePersistentUiDecorationsBeforeActionUpdate(state: Game, action: GameAction) {
        // Register intent to move
        if (action is CompositeGameAction && action.actionList.size == 2) {
            val moveType = action.actionList.firstOrNull().let { (it as? MoveTypeSelected)?.moveType }
            if (moveType == MoveType.STANDARD) {
                val player = state.activePlayer ?: error("Missing active player")
                uiDecorations.registerStartingMoveStep(player, player.coordinates)
            }
        }
    }

    private fun updatePersistentUiDecorations(state: Game, delta: GameDelta, uiIndicators: UiPersistentGameIndicators, acc: UiSnapshotAccumulator) {
        if (delta.reversed) {
            uiIndicators.undo(delta.id)
            acc.setMovesUsed(uiIndicators.movesUsed)
            return
        }

        // Clear move markers when an action ends
        if (delta.containsCommand { it is ExitProcedure && it.procedure == ActivatePlayer }) {
            uiIndicators.resetMovesUsed()
            acc.setMovesUsed(uiIndicators.movesUsed)
            return
        }

        // Clear move markers when starting a drive. This also handles after a touchdown
        if (state.currentProcedureState()?.procedure == StartOfDriveSequence) {
            uiIndicators.resetMovesUsed()
            acc.setMovesUsed(uiIndicators.movesUsed)
            return
        }

        // Track standing up so we can adjust "Move used" correctly.
        if (delta.containsAction(MoveTypeSelected(MoveType.STAND_UP))) {
            val activePlayer = state.activePlayer!!
            if (activePlayer.move >= rules.moveRequiredForStandingUp && !activePlayer.hasSkill(SkillType.JUMP_UP)) {
                uiIndicators.addMoveUsedToStandUp(rules.moveRequiredForStandingUp)
            } else {
                uiIndicators.addMoveUsedToStandUp(0)
            }
        }

        // Add decoration when moving player
        val normalMoveStep = if (uiIndicators.playerIsMoving()) {
            val moveData = uiIndicators.getMovingPlayerInfo()
            delta.steps.any { step ->
                step.commands.any { command ->
                    command is SetPlayerLocation && command.player == moveData.first && command.originalPlayerLocation.isAdjacent(state.rules, command.location)
                }
            }
        } else {
            false
        }

        val jumpMoveStep = delta.steps.lastOrNull()?.let {
            (it.procedure == BB2020JumpStep || it.procedure == BB2025JumpStep ||
                it.procedure == LeapStep || it.procedure == PogoStep) &&
                it.action is PitchSquareSelected
        } ?: false

        if (normalMoveStep) {
            val startingCoordinates = uiDecorations.getMovingPlayerInfo().second
            uiIndicators.addMoveUsed(startingCoordinates)
            uiIndicators.registerUndo(
                deltaId = delta.id,
                action = { uiIndicators.removeLastMoveUsed() }
            )
            uiIndicators.finishMoveStep()
        } else if (jumpMoveStep) {
            val start = state.getContextOrNull<MoveContext>()?.startingSquare
            if (start != null) {
                val extraCost = JUMP_DISTANCE - 1
                uiIndicators.addMoveUsed(start, extraMoveCost = extraCost)
                uiIndicators.registerUndo(
                    deltaId = delta.id,
                    action = { uiIndicators.removeLastMoveUsed(extraMoveCost = extraCost) }
                )
            }
        }
        acc.setMovesUsed(uiIndicators.movesUsed)
    }

    private fun renderSquare(
        coordinate: PitchCoordinate,
        game: Game,
        focusStyle: UiFocusStyle? = null,
    ): UiPitchSquare {
        val square = game.pitch[coordinate]
        return UiPitchSquare(
            coordinates = coordinate,
            player = square.player?.id,
            focusStyle = focusStyle,
        )
    }

    private fun isAnimationsEnabled(currentAction: GameAction? = null): Boolean {
        if (currentAction == null && gameController.lastActionWasUndo()) return false
        if (currentAction != null && currentAction is Undo) return false
        val isAnimationsEnabled = SETTINGS_MANAGER.getBoolean(SettingsKeys.JERVIS_UI_ENABLE_ANIMATIONS_VALUE, true)
        return isAnimationsEnabled && animationSpeedFactor > 0f
    }

    fun userSelectedAction(id: GameActionId, action: GameAction) {
        actionProvider.userActionSelected(id, action)
    }

    fun notifyAnimationDone() {
        animationScope.launch {
            animationDone.send(true)
            animationFlow.emit(null)
        }.invokeOnCompletion {
            if (it != null && it !is CancellationException) {
                throw it
            }
        }
    }

    /**
     * Discard any pending animation-completion signals sitting on [animationDone].
     *
     * The game loop pairs each `animationDone.receive()` with a `notifyAnimationDone()` purely by
     * arrival order. A replay jump or an interrupted animation can leave a stale completion
     * parked on the rendezvous channel; if not cleared it would satisfy the next animation's wait
     * prematurely and cut it short. The replay controller calls this when resuming from pause,
     * where no legitimate completion can be in flight. It is a no-op when the channel is idle,
     * so normal games are unaffected.
     */
    fun drainAnimationSignals() {
        while (animationDone.tryReceive().isSuccess) { /* discard stale completion(s) */ }
    }

    /**
     * Await the completion of the animation the loop just emitted.
     *
     * First, discarding any STALE completion left parked on [animationDone] by
     * an animation that fired more than once - e.g. a cancelled full-screen
     * animation whose `finally` still calls [notifyAnimationDone], or a
     * straggler from a replay jump.
     *
     * The loop consumes exactly one completion per animation and always before
     * emitting the next, so anything already parked when we start waiting for
     * a NEW animation is stale by construction. The just-emitted animation
     * cannot have completed yet (the UI processes the emit on a later frame,
     * while this drain runs synchronously microseconds after the emit), so this
     * never drops the genuine signal.
     */
    private suspend fun awaitAnimationCompletion() {
        while (animationDone.tryReceive().isSuccess) { /* discard stale completion(s) */ }
        animationDone.receive()
    }

    /**
     * Scale a base animation duration (in milliseconds) by the current
     * [com.jervisffb.ui.game.UiGameController.animationSpeedFactor].
     *
     * The result is clamped to at least 1ms so Compose `tween` and
     * `delay` calls stay valid even when the factor is very small.
     *
     * Note: full-screen animations are skipped entirely when the
     * factor is `<= 0`, so this helper is only reached with a positive factor.
     */
    fun scaledAnimationMs(baseMillis: Int): Int =
        (baseMillis * animationSpeedFactor).roundToInt().coerceAtLeast(1)

}
