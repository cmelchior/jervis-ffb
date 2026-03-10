package com.jervisffb.engine.rules.bb2020.procedures.actions.throwteammate

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.LandingRollContext
import com.jervisffb.engine.model.context.MovePlayerIntoSquareContext
import com.jervisffb.engine.model.context.PickupRollContext
import com.jervisffb.engine.model.context.ScoringATouchDownContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.LandingModifier
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportPlayerBounce
import com.jervisffb.engine.reports.ReportPlayerLandingInSquare
import com.jervisffb.engine.reports.ReportPlayerLandingOnAnotherPlayer
import com.jervisffb.engine.reports.ReportQualityOfThrow
import com.jervisffb.engine.reports.ReportStartingThrowTeamMate
import com.jervisffb.engine.reports.ReportThrownPlayerGoingOutOfBounds
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.BB2020FallingOver
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.BB2020KnockedDown
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.DeviateRoll
import com.jervisffb.engine.rules.common.procedures.DeviateRollContext
import com.jervisffb.engine.rules.common.procedures.Pickup
import com.jervisffb.engine.rules.common.procedures.ScatterRoll
import com.jervisffb.engine.rules.common.procedures.ScatterRollContext
import com.jervisffb.engine.rules.common.procedures.ThrowIn
import com.jervisffb.engine.rules.common.procedures.ThrowInContext
import com.jervisffb.engine.rules.common.procedures.actions.move.MovePlayerIntoSquare
import com.jervisffb.engine.rules.common.procedures.actions.move.ScoringATouchdown
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.LandingRoll
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowPlayerResult
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowTeamMateContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryRoll
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure for handling the throwing the player part of a [com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowTeamMateAction].
 * This procedure assumes that the player being thrown has already been selected.
 *
 * See page 53 in the rulebook.
 */
object ThrowPlayerStep: Procedure() {
    override val initialNode: Node = DeclareTargetSquare
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.getContext<ThrowTeamMateContext>().let {
            if (it.thrownPlayer == null) INVALID_GAME_STATE("Thrown player is not set: $it")
        }
    }

    object DeclareTargetSquare: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ThrowTeamMateContext>().thrower.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<ThrowTeamMateContext>()
            val targetSquares = context.thrower.coordinates.getSurroundingCoordinates(rules, rules.rangeRuler.MAX_DISTANCE)
                .filter {
                    val range = rules.rangeRuler.measure(context.thrower, it)
                    when (range) {
                        Range.PASSING_PLAYER -> false
                        Range.QUICK_PASS -> true
                        Range.SHORT_PASS -> true
                        Range.LONG_PASS,
                        Range.LONG_BOMB,
                        Range.OUT_OF_RANGE -> false
                    }
                }
                .map { TargetSquare.Companion.throwTarget(it) }
                .let { SelectFieldLocation(it) }
            return listOf(targetSquares, CancelWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Cancel -> ExitProcedure() // Abort the throw
                else -> {
                    castAction<FieldSquareSelected>(action) {
                        val context = state.getContext<ThrowTeamMateContext>()
                        val distance = rules.rangeRuler.measure(context.thrower, it.coordinate)
                        val newLocation = it.coordinate
                        val automaticFumble = context.thrower.passing == null
                        buildCompositeCommand {
                            add(ReportStartingThrowTeamMate(context, it.coordinate))
                            if (automaticFumble) {
                                // Having a PA of - is an automatic fumble, no dice is rolled
                                addAll(
                                    UpdateContext(
                                        context.copy(
                                            target = newLocation,
                                            range = distance,
                                            qualityRollResult = ThrowPlayerResult.FUMBLED
                                        )
                                    ),
                                    GotoNode(ResolveFumbledThrow)
                                )
                            } else {
                                // Otherwise, the player needs to roll for the throw
                                addAll(
                                    UpdateContext(
                                        context.copy(
                                            target = newLocation,
                                            range = distance,
                                        )
                                    ),
                                    GotoNode(TestForQuality)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    object TestForQuality: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = QualityRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ThrowTeamMateContext>()
            return compositeCommandOf(
                ReportQualityOfThrow(context),
                when (val roll = context.qualityRollResult) {
                    ThrowPlayerResult.SUCCESSFUL,
                    ThrowPlayerResult.SUPERB -> GotoNode(ResolveThrowOnTarget)
                    ThrowPlayerResult.TERRIBLE -> GotoNode(ResolveDeviateThrow)
                    ThrowPlayerResult.FUMBLED -> GotoNode(ResolveFumbledThrow)
                    ThrowPlayerResult.SUBPAR,
                    null -> INVALID_GAME_STATE("Unsupported quality roll result: $roll")
                }
            )
        }
    }

    /**
     * On a successful throw, the player makes it to its target square from where it will scatter.
     */
    object ResolveThrowOnTarget: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<ThrowTeamMateContext>()
            return compositeCommandOf(
                SetPlayerLocation(context.thrownPlayer!!, context.target!!, isThrown = true),
                GotoNode(ScatterPlayer)
            )
        }
    }

    /**
     * If the pass is a Terrible Throw, the player will deviate from the thrower position.
     */
    object ResolveDeviateThrow: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ThrowTeamMateContext>()
            val deviateContext = DeviateRollContext(context.thrower.coordinates)
            return AddContext(deviateContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = DeviateRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val deviateContext = state.getContext<DeviateRollContext>()
            val throwContext = state.getContext<ThrowTeamMateContext>()
            return if (deviateContext.outOfBoundsAt != null) {
                compositeCommandOf(
                    SetPlayerLocation(
                        player = throwContext.thrownPlayer!!,
                        location = deviateContext.landsAt!!,
                        isThrown = false,
                    ),
                    UpdateContext(
                        throwContext.copy(
                            target = deviateContext.landsAt,
                            outOfBoundsAt = deviateContext.outOfBoundsAt
                        )
                    ),
                    RemoveContext<DeviateRollContext>(),
                    GotoNode(ResolveLandingInTheCrowd)
                )
            } else {
                compositeCommandOf(
                    SetPlayerLocation(
                        throwContext.thrownPlayer!!,
                        deviateContext.landsAt!!,
                        isThrown = true,
                    ),
                    UpdateContext(throwContext.copy(target = deviateContext.landsAt)),
                    RemoveContext<DeviateRollContext>(),
                    GotoNode(ScatterPlayer)
                )
            }
        }
    }

    // For Superb, Successful and Terrible throws, the player will scatter before
    // attempting to land
    object ScatterPlayer: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ThrowTeamMateContext>()
            val scatterContext = ScatterRollContext(
                from = context.thrownPlayer!!.coordinates
            )
            return AddContext(scatterContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScatterRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val scatterContext = state.getContext<ScatterRollContext>()
            val throwContext = state.getContext<ThrowTeamMateContext>()
            val thrownPlayer = throwContext.thrownPlayer!!
            return if (scatterContext.outOfBoundsAt != null) {
                compositeCommandOf(
                    SetPlayerLocation(
                        player = thrownPlayer,
                        location = scatterContext.landsAt!!,
                        isThrown = false,
                    ),
                    UpdateContext(
                        throwContext.copy(
                            target = scatterContext.landsAt,
                            outOfBoundsAt = scatterContext.outOfBoundsAt
                        )
                    ),
                    RemoveContext<ScatterRollContext>(),
                    ReportThrownPlayerGoingOutOfBounds(throwContext, scatter = true),
                    GotoNode(ResolveLandingInTheCrowd)
                )
            } else {
                val landsAt = scatterContext.landsAt!!
                compositeCommandOf(
                    SetPlayerLocation(
                        player = thrownPlayer,
                        location = landsAt,
                        isThrown = true,
                    ),
                    UpdateContext(throwContext.copy(target = landsAt)),
                    RemoveContext<ScatterRollContext>(),
                    if (state.field[landsAt].isOccupied()) {
                        GotoNode(ResolveLandingInOccupiedSquare)
                    } else if (throwContext.willCrashLand) {
                        GotoNode(BouncePlayer)
                    } else {
                        GotoNode(ResolveLanding)
                    }
                )
            }
        }
    }

    /**
     * If the pass is fumbled, the player will bounce from the thrower's location after
     * which they must attempt to land.
     */
    object ResolveFumbledThrow: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<ThrowTeamMateContext>()
            return compositeCommandOf(
                SetPlayerLocation(
                    player = context.thrownPlayer!!,
                    location = context.thrower.coordinates,
                    isThrown = true,
                ),
                GotoNode(BouncePlayer)
            )
        }
    }

    // Resolve a thrown player bouncing from their current location.
    object BouncePlayer: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ThrowTeamMateContext>().thrower.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D8))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D8Result>(action) { bounceRoll ->
                val throwContext = state.getContext<ThrowTeamMateContext>()
                val direction = rules.direction(bounceRoll)
                val thrownPlayer = throwContext.thrownPlayer!!
                val target = thrownPlayer.coordinates.move(direction, steps = 1)
                val landingNode = when {
                    target.isOutOfBounds(rules) -> ResolveLandingInTheCrowd
                    target.isOnField(rules) && !state.field[target].isOccupied() -> ResolveLanding
                    target.isOnField(rules) && state.field[target].isOccupied() -> ResolveLandingInOccupiedSquare
                    else -> error("Could not determine landing type at: $target")
                }
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.BOUNCE, bounceRoll),
                    SetPlayerLocation(
                        player = throwContext.thrownPlayer,
                        location = target,
                        isThrown = true,
                    ),
                    UpdateContext(
                        throwContext.copy(
                            target = target,
                            outOfBoundsAt = if (landingNode == ResolveLandingInTheCrowd) throwContext.thrownPlayer.coordinates else null
                        )
                    ),
                    if (landingNode == ResolveLandingInTheCrowd) {
                        ReportThrownPlayerGoingOutOfBounds(throwContext, scatter = false)
                    } else {
                        ReportPlayerBounce(throwContext, target)

                    },
                    GotoNode(landingNode)
                )
            }
        }
    }

    // Player is attempting to land in a square already containing a player. That player is knocked
    // down. Roll for injury and then bounce the landing player. Note, if the player being knocked down
    // had the ball, it will bounce before the thrown player.
    object ResolveLandingInOccupiedSquare: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val throwContext = state.getContext<ThrowTeamMateContext>()
            val playerInSquare = state.field[throwContext.target!!].player ?: INVALID_GAME_STATE("No player found in square: ${throwContext.target}")
            val injuryContext = RiskingInjuryContext(playerInSquare)
            return compositeCommandOf(
                ReportPlayerLandingOnAnotherPlayer(throwContext, playerInSquare),
                AddContext(injuryContext),
                UpdateContext(throwContext.copy(knockedDownWhenLanding = true))
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2020KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            val throwContext = state.getContext<ThrowTeamMateContext>()
            val injuryContext = state.getContext<RiskingInjuryContext>()
            // According to FAQ May 2025, landing on a player from your own team is always a turnover.
            return compositeCommandOf(
                if (injuryContext.player.team == throwContext.thrownPlayer!!.team) SetTurnOver(TurnOver.STANDARD) else null,
                RemoveContext<RiskingInjuryContext>(),
                GotoNode(BouncePlayer)
            )
        }
    }

    // The player went out of bounds during the throw. First roll for their injury,
    // then throw the ball back in (if they had one). This is only a turnover if the
    // player had the ball.
    object ResolveLandingInTheCrowd: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ThrowTeamMateContext>()
            val thrownPlayer = context.thrownPlayer!!
            val injuryContext = RiskingInjuryContext(thrownPlayer, mode = RiskingInjuryMode.PUSHED_INTO_CROWD)
            return buildCompositeCommand {
                if (thrownPlayer.hasBall()) {
                    // Lose the ball before rolling for injury, so the ball doesn't end up in the Dogout.
                    addAll(
                        SetBallLocation(thrownPlayer.ball!!, context.target!!),
                        SetBallState.outOfBounds(thrownPlayer.ball!!, context.outOfBoundsAt!!),
                        SetTurnOver(TurnOver.STANDARD)
                    )
                }
                add(AddContext(injuryContext))
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val throwContext = state.getContext<ThrowTeamMateContext>()
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                if (throwContext.outOfBoundsAt != null && state.balls.any { it.outOfBoundsAt != null }) {
                    GotoNode(ThrowBallBackIn)
                } else {
                    ExitProcedure()
                }
            )
        }
    }

    // If the thrown player landed in the crowd holding the ball, the ball is thrown back in,
    // and the action ends with a turnover (this was already set when the player rolled for
    // injury).
    object ThrowBallBackIn: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.balls.single { it.outOfBoundsAt != null }
            val context = ThrowInContext(ball, ball.outOfBoundsAt!!)
            return compositeCommandOf(
                SetCurrentBall(ball),
                AddContext(context)
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ThrowInContext>(),
                SetCurrentBall(null),
                ExitProcedure()
            )
        }
    }

    // Player is attempting to land in an empty square on the field.
    // A player that is crash landing has already bounced when reaching this node.
    object ResolveLanding: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<ThrowTeamMateContext>()
            return if (context.fallOverWhenLanding) {
                ResolveLandingPlayerFallingOver
            } else if (context.knockedDownWhenLanding) {
                ResolveLandingPlayerKnockedDown
            } else if (context.willCrashLand) {
                ResolveLandingPlayerFallingOver
            } else {
                null
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            // Determine target and modifiers for the Landing roll
            val context = state.getContext<ThrowTeamMateContext>()
            val thrownPlayer = context.thrownPlayer ?: INVALID_GAME_STATE("Could not find the thrown player: $context")
            val diceRollTarget = thrownPlayer.agility
            val modifiers = mutableListOf<DiceModifier>()

            // Add modifier depending on the quality of the throw
            val qualityModifier = when (val roll = context.qualityRollResult) {
                ThrowPlayerResult.SUPERB -> LandingModifier.SUPERB
                ThrowPlayerResult.SUCCESSFUL -> LandingModifier.SUCCESSFUL
                ThrowPlayerResult.TERRIBLE -> LandingModifier.TERRIBLE
                ThrowPlayerResult.FUMBLED -> LandingModifier.FUMBLED
                ThrowPlayerResult.SUBPAR,
                null -> INVALID_GAME_STATE("Invalid quality roll result: $roll")
            }
            modifiers.add(qualityModifier)

            // Add marked modifiers for the field
            rules.addMarkedModifiers(
                state,
                thrownPlayer.team,
                thrownPlayer.coordinates,
                modifiers,
                LandingModifier.MARKED
            )
            return AddContext(LandingRollContext(thrownPlayer, diceRollTarget, modifiers))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = LandingRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val throwContext = state.getContext<ThrowTeamMateContext>()
            val rollContext = state.getContext<LandingRollContext>()
            val successfulLanding = rollContext.isSuccess
            // If the player successfully landed. One of 3 things will happen:
            // 1. They landed successfully on an empty square. The action is over.
            // 2. They landed successfully on a square with a ball. They must attempt to pick it up.
            // 3. They landed unsuccessfully on the square. They will Fall Over and the ball will bounce.
            //    If there are multiple balls in play, the ball held by the player is bounced first.
            return compositeCommandOf(
                ReportPlayerLandingInSquare(throwContext, rollContext),
                if (successfulLanding) {
                    GotoNode(LandSuccessfullyInSquare)
                } else {
                    GotoNode(ResolveLandingPlayerFallingOver)
                }
            )
        }
    }

    object LandSuccessfullyInSquare: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ThrowTeamMateContext>()
            val player = context.thrownPlayer ?: INVALID_GAME_STATE("Could not find thrown player: $context")
            val target = context.target ?: INVALID_GAME_STATE("Could not find target location: $context")
            return AddContext(MovePlayerIntoSquareContext(player, target))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = MovePlayerIntoSquare
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ThrowTeamMateContext>()
            val player = context.thrownPlayer ?: INVALID_GAME_STATE("Could not find thrown player: $context")
            val playerHasBall = player.hasBall()
            // Can only be true if the moving player isn't holding a ball
            val ballInSquare = state.field[player.coordinates].balls.isNotEmpty()
            val isTurnOver = state.isTurnOver()
            // At this stage, a ball might have bounced and been caught for a touchdown; in that
            // case, the landing player is not the one who gets the touchdown.
            return when {
                playerHasBall && !isTurnOver -> GotoNode(CheckForScoring)
                playerHasBall && isTurnOver -> ExitProcedure()
                !playerHasBall && isTurnOver -> ExitProcedure()
                !playerHasBall && ballInSquare -> GotoNode(PickupBallAfterLanding)
                !playerHasBall && !ballInSquare -> ExitProcedure()
                else -> INVALID_GAME_STATE("Invalid state for landing player: hasBall[$playerHasBall], ballInSquare[$ballInSquare], turnOver[$isTurnOver]")
            }
        }
    }

    object PickupBallAfterLanding: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val throwContext = state.getContext<ThrowTeamMateContext>()
            val ball = state.field[throwContext.thrownPlayer!!.coordinates].balls.single()
            val pickupContext = PickupRollContext(throwContext.thrownPlayer, ball)
            return AddContext(pickupContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Pickup
        override fun onExitNode(state: Game, rules: Rules): Command {
            // All possible results are calculated inside Pickup, so just end here
            return ExitProcedure()
        }
    }

    object BounceBallOnLandingSquare: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.balls.first { it.state == BallState.BOUNCING }
            return SetCurrentBall(ball)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            // All outcomes are determined inside Bounce, so just exit here.
            return compositeCommandOf(
                SetCurrentBall(null),
                ExitProcedure()
            )
        }
    }

    object ResolveLandingPlayerFallingOver: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val throwContext = state.getContext<ThrowTeamMateContext>()
            val thrownPlayer = throwContext.thrownPlayer ?: INVALID_GAME_STATE("Could not find thrown player: $throwContext")
            return compositeCommandOf(
                SetPlayerLocation(
                    thrownPlayer,
                    throwContext.target!!,
                    isThrown = false,
                ),
                state.balls.firstOrNull { it.state == BallState.ON_GROUND && it.location == throwContext.target }?.let {
                    SetBallState.Companion.bouncing(it)
                },
                SetPlayerState(thrownPlayer, PlayerState.FALLEN_OVER),
                AddContext(RiskingInjuryContext(thrownPlayer, mode = RiskingInjuryMode.BAD_LANDING))
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2020FallingOver
        override fun onExitNode(state: Game, rules: Rules): Command {
            return exitPlayingGoingDownNode(state)
        }
    }

    object ResolveLandingPlayerKnockedDown: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val throwContext = state.getContext<ThrowTeamMateContext>()
            val thrownPlayer = throwContext.thrownPlayer ?: INVALID_GAME_STATE("Could not find thrown player: $throwContext")
            return compositeCommandOf(
                SetPlayerLocation(
                    thrownPlayer,
                    throwContext.target!!,
                    isThrown = false,
                ),
                state.balls.firstOrNull { it.state == BallState.ON_GROUND && it.location == throwContext.target }?.let {
                    SetBallState.bouncing(it)
                },
                AddContext(RiskingInjuryContext(thrownPlayer, mode = RiskingInjuryMode.BAD_LANDING))
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2020KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return exitPlayingGoingDownNode(state)
        }
    }

    object CheckForScoring : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ThrowTeamMateContext>()
            return AddContext(ScoringATouchDownContext(context.thrownPlayer!!))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ScoringATouchDownContext>(),
                ExitProcedure()
            )
        }
    }

    private fun exitPlayingGoingDownNode(state: Game): Command {
        // If there was a ball in the square the player fell over in, it will now bounce;
        // otherwise there is nothing to do. Note, if the player was holding a ball, it
        // will already have bounced. so this only happens for a ball laying on the ground.
        val throwContext = state.getContext<ThrowTeamMateContext>()
        val ball = state.balls.firstOrNull { it.state == BallState.BOUNCING }
        return buildCompositeCommand {
            add(RemoveContext<RiskingInjuryContext>())
            if (ball != null) {
                addAll(
                    SetBallState.Companion.bouncing(ball),
                    GotoNode(BounceBallOnLandingSquare)
                )
            } else {
                add(ExitProcedure())
            }
        }
    }
}
