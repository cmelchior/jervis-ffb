package com.jervisffb.engine.bb2025.procedures.rerolls

import com.jervisffb.engine.bb2025.reports.ReportTeamCaptainResult
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
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.rerolls.LonerRoll
import com.jervisffb.engine.rules.common.procedures.rerolls.LonerRollContext
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.assert

/**
 * Procedure controlling how to use a Team Reroll. This procedure doesn't
 * actually re-roll the dice. It is only responsible for marking the reroll as
 * used and triggering any side effects from it.
 *
 * The exact order of checks for re-rolls is unspecified in the rules. The
 * Designer's Commentary has clarified some of this, but unfortunately, it still
 * leaves some open questions.
 *
 * Designer's Commentary:
 * Q: Can I use the Team Captain special rule in conjunction with the Team
 *    Re-roll given by a Team Mascot? (pg. 144)
 *
 * A: Yes. If a 4+ is rolled, and the Team Mascot Re-roll is used as normal,
 *    you may then test to see if it is free via the Team Captain rule. If it
 *    is, you can then use the Team Mascot Re-roll again. Each time you use it,
 *    you must roll a D6 as described in the Team Mascot rule - it can be
 *    ineffective the second time.
 *
 * Developer's Commentary:
 * With the above in mind, Jervis interprets failing a Team Mascot roll as the
 * same as never having the re-roll in the first place, i.e., it isn't treated
 * as a re-roll. This has implications on Loner and Team Captain.
 *
 * For this reason, the following order of resolution is used:
 *
 * 0. Set [RerollSource.rerollUsed] to `true`.
 * 1. Set [UseRerollContext.rerollAllowed] to `true`.
 * 2. Check if the reroll is a Team Mascot re-roll. In this case, roll to see
 *    if it is available. If not, set [UseRerollContext.rerollAllowed] to
 *    `false` and abort. We do not check for neither Loner nor Team Captain.
 * 3. Check for Loner. If failed, set [UseRerollContext.rerollAllowed] to
 *    `false`.
 * 4. Check for Team Captain, regardless of Loner outcome. Roll to see if the
 *    Team Captain returns the re-roll to the available pool. If success, set
 *    [RerollSource.rerollUsed] to `false`.

 * Note that rerolling all these rolls is allowed, resulting in a recursive
 * loop. The following rules apply:
 *
 * 1. A re-roll is marked as used while being resolved, preventing it from
 *    being used in a recursive step.
 * 2. Mascot Roll can be re-rolled using Team Rerolls, but not Pro.
 * 3. Loner can be re-rolled using both Pro and Team Rerolls.
 * 4. Team Captain can be re-rolled using Team Rerolls, but not Pro.
 *
 * See [BB2025 Base Rules](../website/bb2025/bb2025-base-rules.md) for a
 * discussion on this.
 */
object UseTeamReroll : Procedure() {
    override val initialNode: Node = CheckForMascot
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        // We mark the re-roll used before running any checks. This is an easy
        // way to ensure that re-rolls are not used twice in case we run into
        // recursive reroll usage (like using team rerolls to roll failed loner)
        // If this turns out to be false, we reset the flag later.
        val context = state.getRerollContext()
        val reroll = context.source ?: INVALID_GAME_STATE("Cannot use team reroll as no reroll source: $context")
        return compositeCommandOf(
            SetTeamRerollUsed(state.activeTeamOrThrow(), reroll),
            UpdateContext(context.copy(
                rerollDice = context.originalRoll,
                rerollAllowed = true,
            ))
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        val context = state.getRerollContext()
        assert(context.source != null) {
            "Cannot use re-roll as no re-roll source is selected: $context"
        }
    }

    object CheckForMascot: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getRerollContext()
            val isRerollAllowed = context.rerollAllowed
            val isMascotReroll = context.source is TeamMascotReroll
            return when (isMascotReroll && isRerollAllowed) {
                true -> null
                else -> CheckForLoner
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
            val mascotContext = state.getContext<MascotContext>()
            return compositeCommandOf(
                UpdateContext(rerollContext.copy(
                    rerollAllowed = mascotContext.isRerollAllowed
                )),
                RemoveContext(mascotContext),
                // Only handle Loner and Team Captain if the Mascot Roll succeeded
                // If Mascot failed, these will be handled recursively inside `TeamMascotStep`
                // when the alternative reroll is selected
                when (mascotContext.alternativeRerollSelected == null && mascotContext.isSuccessful) { // TODO Skip loner
                    true -> GotoNode(CheckForLoner)
                    false -> ExitProcedure() // Failing the Mascot
                }
            )
        }
    }

    object CheckForLoner: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getRerollContext()
            return when (context.player?.isSkillAvailable(SkillType.LONER)) {
                false,
                null -> CheckForTeamCaptain
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
            player.location.isOnPitch(rules) && player.specialRules.contains(PlayerSpecialRule.TEAM_CAPTAIN)
        }
    }
}
