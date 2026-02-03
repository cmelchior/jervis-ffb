package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.bb2025.procedures.actions.pass.InterceptionStep
import com.jervisffb.engine.rules.bb2025.procedures.skills.UseShadowingStep
import com.jervisffb.engine.rules.common.procedures.actions.move.DodgeRoll
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.ManualActionProvider

/**
 * Some "cancel" actions we want to display in inside the timer button
 */
object CancelDecorator : FieldActionDecorator<CancelWhenReady> {

    private val nodesToDecorate = setOf(
        UseShadowingStep.CheckIfShadowingIsAvailable,
        InterceptionStep.SelectPlayerForInterception,
        DodgeRoll.ChooseToUseTackle,
        DodgeRoll.ChooseToUsePrehensileTail
    )

    override fun isApplicable(state: Game, request: ActionRequest): Boolean {
        val currentNode = state.stack.currentNode()
        return nodesToDecorate.contains(currentNode)
    }

    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: CancelWhenReady,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
        val title = when (state.stack.currentNode()) {
            UseShadowingStep.CheckIfShadowingIsAvailable -> "Do not use Shadowing"
            InterceptionStep.SelectPlayerForInterception -> "Do not intercept"
            DodgeRoll.ChooseToUseTackle -> "Do not use Tackle"
            DodgeRoll.ChooseToUsePrehensileTail -> "Do not use Prehensile Tail"
            else -> error("Unsupported node: ${state.stack.currentNode()}")
        }
        acc.updateGameStatus {
            it.copy(
                centerBadgeText = title,
                centerBadgeAction = { actionProvider.userActionSelected(Cancel) },
                centerBadgeEnabled = true
            )
        }
    }
}
