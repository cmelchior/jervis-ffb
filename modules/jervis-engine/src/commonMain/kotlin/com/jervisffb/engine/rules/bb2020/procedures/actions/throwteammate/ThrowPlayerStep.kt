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
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.fsm.checkTypeAndValue
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.LandingRollContext
import com.jervisffb.engine.model.context.MovePlayerIntoSquareContext
import com.jervisffb.engine.model.context.PickupRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.LandingModifier
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportStartingThrowTeamMate
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.Bounce
import com.jervisffb.engine.rules.bb2020.procedures.DeviateRoll
import com.jervisffb.engine.rules.bb2020.procedures.DeviateRollContext
import com.jervisffb.engine.rules.bb2020.procedures.Pickup
import com.jervisffb.engine.rules.bb2020.procedures.ScatterRoll
import com.jervisffb.engine.rules.bb2020.procedures.ScatterRollContext
import com.jervisffb.engine.rules.bb2020.procedures.ThrowIn
import com.jervisffb.engine.rules.bb2020.procedures.ThrowInContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MovePlayerIntoSquare
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.ScoringATouchDownContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.ScoringATouchdown
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.FallingOver
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.KnockedDown
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryRoll
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure for handling the throwing the player part of a [ThrowTeamMateAction].
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
                .map { TargetSquare.throwTarget(it) }
                .let { SelectFieldLocation(it) }
            return listOf(targetSquares, CancelWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Cancel -> ExitProcedure() // Abort the throw
                else -> {
                    checkTypeAndValue<FieldSquareSelected>(state, action) {
                        val context = state.getContext<ThrowTeamMateContext>()
                        val distance = rules.rangeRuler.measure(context.thrower, it.coordinate)
                        val newLocation = it.coordinate
                        val automaticFumble = context.thrower.passing == null
                        buildCompositeCommand {
                            add(ReportStartingThrowTeamMate(context))
                            if (automaticFumble) {
                                // Having a PA of - is an automatic fumble, no dice is rolled
                                addAll(
                                    SetContext(
                                        context.copy(
                                            target = newLocation,
                                            range = distance,
                                            qualityRollResult = ThrowPlayerResult.FUMBLED_THROW
                                        )),
                                    GotoNode(ResolveFumbledThrow)
                                )
                            } else {
                                // Otherwise, the player needs to roll for the throw
                                addAll(
                                    SetContext(
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
            return when (context.qualityRollResult) {
                ThrowPlayerResult.SUCCESSFUL_THROW,
                ThrowPlayerResult.SUPERB_THROW -> GotoNode(ResolveThrowOnTarget)
                ThrowPlayerResult.TERRIBLE_THROW -> GotoNode(ResolveDeviateThrow)
                ThrowPlayerResult.FUMBLED_THROW -> GotoNode(ResolveFumbledThrow)
                null -> INVALID_GAME_STATE("Missing passing result value")
            }
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
            return SetContext(deviateContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = DeviateRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val deviateContext = state.getContext<DeviateRollContext>()
            val throwContext = state.getContext<ThrowTeamMateContext>()
            return if (deviateContext.outOfBoundsAt != null) {
                compositeCommandOf(
                    SetPlayerLocation(
                        player = throwContext.thrownPlayer!!,
                        location = FieldCoordinate.OUT_OF_BOUNDS,
                        isThrown = false,
                    ),
                    SetContext(throwContext.copy(target = FieldCoordinate.OUT_OF_BOUNDS, outOfBoundsAt = deviateContext.outOfBoundsAt)),
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
                    SetContext(throwContext.copy(target = deviateContext.landsAt)),
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
            return SetContext(scatterContext)
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
                        location = FieldCoordinate.OUT_OF_BOUNDS,
                        isThrown = false,
                    ),
                    SetContext(throwContext.copy(target = FieldCoordinate.OUT_OF_BOUNDS, outOfBoundsAt = scatterContext.outOfBoundsAt)),
                    RemoveContext<ScatterRollContext>(),
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
                    SetContext(throwContext.copy(target = landsAt)),
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
            return checkType<D8Result>(action) { bounceRoll ->
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
                val adjustedTarget = if (target.isOutOfBounds(rules)) FieldCoordinate.OUT_OF_BOUNDS else target
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.BOUNCE, bounceRoll),
                    SetPlayerLocation(
                        player = throwContext.thrownPlayer,
                        location = adjustedTarget,
                        isThrown = true,
                    ),
                    SetContext(throwContext.copy(
                        target = adjustedTarget,
                        outOfBoundsAt = if (landingNode == ResolveLandingInTheCrowd) throwContext.thrownPlayer.coordinates else null
                    )),
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
                SetPlayerState(playerInSquare, PlayerState.KNOCKED_DOWN),
                SetContext(injuryContext),
                SetContext(throwContext.copy(knockedDownWhenLanding = true))
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = KnockedDown
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
                    add(SetBallState.outOfBounds(thrownPlayer.ball!!, context.outOfBoundsAt!!))
                    add(SetTurnOver(TurnOver.STANDARD))
                }
                add(SetContext(injuryContext))
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val throwContext = state.getContext<ThrowTeamMateContext>()
            return compositeCommandOf(
                RemoveContext<ThrowInContext>(),
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
                SetContext(context)
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
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
            return if (context.knockedDownWhenLanding) {
                return ResolveLandingPlayerKnockedDown
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
            val qualityModifier = when (context.qualityRollResult) {
                ThrowPlayerResult.SUPERB_THROW -> LandingModifier.SUPERB_THROW
                ThrowPlayerResult.SUCCESSFUL_THROW -> LandingModifier.SUCCESSFUL_THROW
                ThrowPlayerResult.TERRIBLE_THROW -> LandingModifier.TERRIBLE_THROW
                ThrowPlayerResult.FUMBLED_THROW -> LandingModifier.FUMBLED_THROW
                null -> INVALID_GAME_STATE("Missing quality roll value: $context")
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
            return SetContext(LandingRollContext(thrownPlayer, diceRollTarget, modifiers))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = LandingRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val rollContext = state.getContext<LandingRollContext>()
            val successfulLanding = rollContext.isSuccess
            // If the player successfully landed. One of 3 things will happen:
            // 1. They landed successfully on an empty square. The action is over.
            // 2. They landed successfully on a square with a ball. They must attempt to pick it up.
            // 3. They landed unsuccessfully on the square. They will Fall Over and the ball will bounce.
            //    If there are multiple balls in play, the ball held by the player is bounced first.
            return if (successfulLanding) {
                GotoNode(LandSuccessfullyInSquare)
            } else {
                GotoNode(ResolveLandingPlayerFallingOver)
            }
        }
    }

    object LandSuccessfullyInSquare: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ThrowTeamMateContext>()
            val player = context.thrownPlayer ?: INVALID_GAME_STATE("Could not find thrown player: $context")
            val target = context.target ?: INVALID_GAME_STATE("Could not find target location: $context")
            return SetContext(MovePlayerIntoSquareContext(player, target))
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
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val throwContext = state.getContext<ThrowTeamMateContext>()
            val pickupContext = PickupRollContext(throwContext.thrownPlayer!!)
            return SetContext(pickupContext)
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
                    SetBallState.bouncing(it)
                },
                SetPlayerState(thrownPlayer, PlayerState.FALLEN_OVER),
                SetContext(RiskingInjuryContext(thrownPlayer, mode = RiskingInjuryMode.BAD_LANDING))
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = FallingOver
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
                SetPlayerState(thrownPlayer, PlayerState.KNOCKED_DOWN),
                SetContext(RiskingInjuryContext(thrownPlayer, mode = RiskingInjuryMode.BAD_LANDING))
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return exitPlayingGoingDownNode(state)
        }
    }

    object CheckForScoring : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ThrowTeamMateContext>()
            return SetContext(ScoringATouchDownContext(context.thrownPlayer!!))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
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
                    SetBallState.bouncing(ball),
                    GotoNode(BounceBallOnLandingSquare)
                )
            } else  {
                add(ExitProcedure())
            }
        }
    }
}
