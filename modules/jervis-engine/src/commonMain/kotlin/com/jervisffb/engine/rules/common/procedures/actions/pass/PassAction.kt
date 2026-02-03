package com.jervisffb.engine.rules.common.procedures.actions.pass

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.SelectPassType
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetCurrentBall
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
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.PassingInterferenceContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.pass.HailMaryPassStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.pass.InterceptionContext
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.ResolveMoveTypeStep
import com.jervisffb.engine.rules.common.procedures.calculateMoveTypesAvailable
import com.jervisffb.engine.rules.common.procedures.getResetTemporaryModifiersCommands
import com.jervisffb.engine.rules.common.procedures.getSetPlayerRushesCommand
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.addIfNotNull
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

enum class PassingType {
    ACCURATE,
    INACCURATE,
    WILDLY_INACCURATE,
    FUMBLED
}

data class PassContext(
    val thrower: Player,
    val type: PassType = PassType.STANDARD,
    val hasMoved: Boolean = false,
    // Target of the pass in the current step. This means it will be updated when the ball scatters, deviates etc.
    val target: FieldCoordinate? = null,
    val range: Range? = null,
    val useNervesOfSteel: Boolean = false,
    val passingRoll: D6DieRoll? = null,
    val passingModifiers: PersistentList<DiceModifier> = persistentListOf(),
    val passingResult: PassingType? = null,
    val useSafePass: Boolean = false,
    val runInterference: Player? = null,
    // Used in BB2020
    val passingInterference: PassingInterferenceContext? = null,
    // Used in BB2025
    val intercept: InterceptionContext? = null,
) : ProcedureContext {
    fun copyAndAdd(passingModifier: DiceModifier): PassContext = this.copy(
        passingModifiers = passingModifiers.add(passingModifier)
    )
    val hasThrown: Boolean
        get() = (range != null)
}

/**
 * Procedure for controlling a player's Pass action.
 *
 * See page 48 in the BB2020 rulebook.
 * See page 70 in the BB2025 rulebook.
 */
object PassAction : Procedure() {
    override val initialNode: Node = MoveOrPassOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer!!
        return compositeCommandOf(
            getSetPlayerRushesCommand(rules, player),
            SetContext(PassContext(thrower = player))
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<PassContext>()
        val activePlayerContext = state.getContext<ActivatePlayerContext>()
        return compositeCommandOf(
            RemoveContext<PassContext>(),
            SetContext(
                activePlayerContext.copy(
                    markActionAsUsed = (context.hasMoved || context.passingRoll != null)
                )
            ),
            *getResetTemporaryModifiersCommands(state, rules, Duration.END_OF_ACTION),
        )
    }
    override fun isValid(state: Game, rules: Rules) {
        state.activePlayer ?: INVALID_GAME_STATE("No active player")
    }

    object MoveOrPassOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayer!!.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            if (state.endActionImmediately()) {
                return listOf(ContinueWhenReady)
            }

            val context = state.getContext<PassContext>()
            val options = mutableListOf<GameActionDescriptor>()

            // Find possible move types
            options.addIfNotNull(calculateMoveTypesAvailable(state, state.activePlayer!!))

            // If holding the ball, the player can start the "Pass" section of the Pass action
            if (context.thrower.hasBall()) {
                val passTypes = buildList {
                    add(PassType.STANDARD)
                    if (context.thrower.isSkillAvailable(SkillType.HAIL_MARY_PASS)) {
                        add(PassType.HAIL_MARY_PASS)
                    }
                }
                options.add(SelectPassType(passTypes))
            }

            // End the pass action before trying to throw the ball
            options.add(EndActionWhenReady)

            return options
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<PassContext>()
            return when (action) {
                is PassTypeSelected -> {
                    when (action.type) {
                        PassType.STANDARD -> GotoNode(ResolveStandardThrow)
                        PassType.HAIL_MARY_PASS -> compositeCommandOf(
                            ReportSkillUsed(context.thrower, SkillType.HAIL_MARY_PASS),
                            GotoNode(ResolveHailMaryThrow)
                        )
                    }
                }
                Continue, EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(context.thrower, action.moveType)
                    compositeCommandOf(
                        SetContext(moveContext),
                        GotoNode(ResolveMove)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ResolveMove : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ResolveMoveTypeStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If player is not standing on the field after the move, it is a turn over,
            // otherwise they are free to continue their pass action.
            val moveContext = state.getContext<MoveContext>()
            val context = state.getContext<PassContext>()
            return buildCompositeCommand {
                if (moveContext.hasMoved) {
                    add(SetContext(context.copy(hasMoved = true)))
                }
                if (state.endActionImmediately()) {
                    add(ExitProcedure())
                } else if (!rules.isStanding(context.thrower)) {
                    add(SetTurnOver(TurnOver.STANDARD))
                    add(ExitProcedure())
                } else {
                    if (context.hasThrown) {
                        add(GotoNode(MoveOrEndAction))
                    } else {
                        add(GotoNode(MoveOrPassOrEndAction))
                    }
                }
            }
        }
    }

    object ResolveStandardThrow : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PassContext>()
            return SetCurrentBall(context.thrower.ball)
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(HandlePostThrowStep)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return when (rules.baseVersion) {
                GameVersion.BB2020 -> com.jervisffb.engine.rules.bb2020.procedures.actions.pass.PassStep
                GameVersion.BB2025 -> com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassStep
            }
        }
    }

    object ResolveHailMaryThrow : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PassContext>()
            return compositeCommandOf(
                SetContext(context.copy(type = PassType.HAIL_MARY_PASS)),
                SetCurrentBall(context.thrower.ball)
            )
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(HandlePostThrowStep)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return when (rules.baseVersion) {
                GameVersion.BB2020 -> TODO("Not supported yet")
                GameVersion.BB2025 -> HailMaryPassStep
            }
        }
    }

    object HandlePostThrowStep: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<PassContext>()
            val abortUsingSafePass = context.useSafePass
            return buildCompositeCommand {
                add(SetCurrentBall(null))
                when {
                    context.target == null -> {
                        // No target was selected, so no pass was attempted, continue the pass.
                        add(GotoNode(MoveOrPassOrEndAction))
                    }
                    abortUsingSafePass -> {
                        // If Safe Pass was used, activation ends immediately, but no turnover occurs
                        val activateContext = state.getContext<ActivatePlayerContext>()
                        add(SetContext(activateContext.copy(activationEndsImmediately = true)))
                        add(ExitProcedure())
                    }
                    else -> {
                        // Check if the conditions for using Give and Go are present
                        // While this skill is only available in BB205, it should be safe to do the checks in the common action
                        val hasGiveAndGo = context.thrower.isSkillAvailable(SkillType.GIVE_AND_GO)
                        val isQuickPass = (context.range == Range.QUICK_PASS)
                        val isTurnover = state.isTurnOver()
                        val canUseGiveAndGo = hasGiveAndGo && isQuickPass && !isTurnover
                        if (canUseGiveAndGo) {
                            addAll(
                                ReportSkillUsed(context.thrower, SkillType.GIVE_AND_GO),
                                GotoNode(MoveOrEndAction)
                            )
                        } else {
                            add(ExitProcedure())
                        }
                    }
                }
            }
        }
    }

    // If thrower has Give and Go, after the throw, they can continue to move.
    object MoveOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PassContext>().thrower.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            if (state.endActionImmediately()) {
                return listOf(ContinueWhenReady)
            }
            val options = mutableListOf<GameActionDescriptor>()
            // Find possible move types
            options.addIfNotNull(calculateMoveTypesAvailable(state, state.activePlayer!!))
            // End the action
            options.add(EndActionWhenReady)
            return options
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<PassContext>()
            return when (action) {
                Continue, EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(context.thrower, action.moveType)
                    compositeCommandOf(
                        SetContext(moveContext),
                        GotoNode(ResolveMove)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }
}
