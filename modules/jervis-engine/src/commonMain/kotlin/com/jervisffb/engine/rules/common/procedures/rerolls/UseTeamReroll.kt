package com.jervisffb.engine.rules.common.procedures.rerolls

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetTeamRerollUsed
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.reports.ReportTeamCaptainResult
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.rerolls.TeamMascotReroll
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.assert

/**
 * Procedure controlling how to use a Team Reroll. This procedure doesn't
 * actually re-roll the dice. It is only responsible for marking the reroll as
 * used and trigger any side-effects from it.
 *
 * The actual result is stored in [UseRerollContext.selectedRerollOption].
 * If the re-roll is not
 *
 * Developer's Commentary:
 * The exact order of checks for re-rolls is unspecified in the rules, and
 * a lot is left up for interpretation. Jervis uses the following order:
 *
 * 0. Set `(rerollUsed=true, rerollAllowed=true)`.
 * 1. Check for Loner. If failed, set `rerollAllowe=false`. Skip Mascot roll.
 * 2. Check for Mascot. Roll to see if the reroll can be used. If failed,
 *    set `rerollAllowed=false`.
 * 3. Check for Team Captain. Regardless of Loner or Mascot preventing the use
 *    of the re-roll. Roll to see if Team Captain returns the re-roll to the
 *    available pool. If success, set `rerollUse=false`.
 *
 * Note that rerolling all these rolls is allowed, resulting in a recursive
 * loop. The following rules apply:
 *
 * 1. A re-roll is marked as used while being resolved, preventing it from
 *    being used in a recursive step.
 * 2. Loner can be re-rolled using both Pro and Team Rerolls.
 * 3. Mascot can be re-rolled using Team Rerolls, but not Pro.
 * 4. Team Captain can be re-rolled using Team Rerolls, but not Pro.
 *
 * See [BB2025 Base Rules](../website/bb2025/bb2025-base-rules.md) for a
 * discussion on this.
 */
object UseTeamReroll : Procedure() {
    override val initialNode: Node = CheckForLoner
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        // We mark the re-roll used before running any checks. This is an easy
        // way to ensure that re-rolls are not used twice in case we run into
        // recursive reroll usage (like using team rerolls to roll failed loner)
        // If this turns out to be false, we reset the flag later.
        val context = state.getRerollContext()
        val reroll = context.source ?: INVALID_GAME_STATE("Cannot use team reroll as no reroll source: $context")
        return compositeCommandOf(
            SetTeamRerollUsed(state.activeTeamOrThrow(), reroll),
            UpdateContext(context.copy(rerollAllowed = true))
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        val context = state.getRerollContext()
        assert(context.source != null) {
            "Cannot use re-roll as no re-roll source is selected: $context"
        }
    }

    object CheckForLoner: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getRerollContext()
            return when (context.player?.isSkillAvailable(SkillType.LONER)) {
                false,
                null -> CheckForMascot
                true -> null
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val rerollContext = state.getRerollContext()
            val player = rerollContext.player ?: INVALID_GAME_STATE("Mising player: $rerollContext")
            return AddContext(LonerRollContext(player))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = LonerRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val rerollContext = state.getRerollContext()
            val lonerContext = state.getContext<LonerRollContext>()
            val canReroll = lonerContext.isSuccess
            return compositeCommandOf(
                RemoveContext(lonerContext),
                UpdateContext(rerollContext.copy(
                    rerollAllowed = canReroll
                )),
                GotoNode(CheckForMascot)
            )
        }
    }

    object CheckForMascot: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getRerollContext()
            val isRerollAllowed = context.rerollAllowed
            val isMascotReroll = context.source is TeamMascotReroll
            return when (isMascotReroll && isRerollAllowed) {
                true -> null
                else -> CheckForTeamCaptain
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val rerollContext = state.getRerollContext()
            val reroll = rerollContext.source as? TeamMascotReroll ?: INVALID_GAME_STATE("Reroll is not a mascot: $rerollContext")
            val mascotContext = MascotContext(
                team = rerollContext.team,
                reroll = reroll,
            )
            return AddContext(mascotContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = TeamMascotStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            val rerollContext = state.getRerollContext()
            val context = state.getContext<MascotContext>()
            return compositeCommandOf(
                UpdateContext(rerollContext.copy(
                    rerollAllowed = context.isRerollAllowed
                )),
                GotoNode(CheckForTeamCaptain)
            )
        }
    }

    object CheckForTeamCaptain: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getRerollContext()
            val hasCaptain = (getTeamCaptain(context) != null)
            return when (hasCaptain) {
                true -> null
                false -> ExitProcedureNode
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val rerollContext = state.getRerollContext()
            val captain = getTeamCaptain(rerollContext) ?: INVALID_GAME_STATE("Cannot find a Team Captain: $rerollContext")
            val context = TeamCaptainRollContext(captain)
            return AddContext(context)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = TeamCaptainRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val rerollContext = state.getRerollContext()
            val rollContext = state.getContext<TeamCaptainRollContext>()
            val reroll = rerollContext.source ?: INVALID_GAME_STATE("Missing reroll source: $rerollContext")
            return buildCompositeCommand {
                add(RemoveContext(rollContext))
                if (rollContext.isSuccess) {
                    addAll(
                        SetTeamRerollUsed(state.activeTeamOrThrow(), reroll, markUsed = false),
                        ReportTeamCaptainResult(rollContext, reroll)
                    )
                }
                add(ExitProcedure())
            }
        }
    }

    private fun getTeamCaptain(context: UseRerollContext): Player? {
        val rules = context.team.game.rules
        return context.team.firstOrNull { player ->
            player.location.isOnField(rules) && player.specialRules.contains(PlayerSpecialRule.TEAM_CAPTAIN)
        }
    }
}
