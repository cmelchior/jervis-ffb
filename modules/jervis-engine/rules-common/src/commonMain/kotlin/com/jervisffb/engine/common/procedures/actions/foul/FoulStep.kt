package com.jervisffb.engine.common.procedures.actions.foul

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayersSelected
import com.jervisffb.engine.actions.SelectPlayers
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.common.commands.SetCurrentBall
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
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.common.context.FoulContext
import com.jervisffb.engine.common.modifiers.DefensiveAssistsArmourModifier
import com.jervisffb.engine.common.modifiers.OffensiveAssistArmourModifier
import com.jervisffb.engine.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.common.procedures.tables.injury.RiskingInjuryRoll
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.common.reports.ReportSkillUsed
import com.jervisffb.engine.common.reports.ReportSpottedByRef
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import kotlinx.collections.immutable.toPersistentList
import kotlin.collections.get

/**
 * Procedure for handling the Foul part of a Foul Action.
 *
 * See [com.jervisffb.engine.rules.bb2020.procedures.actions.foul.FoulAction].
 * See [com.jervisffb.engine.rules.bb2025.procedures.actions.foul.FoulAction].
 */
object FoulStep: Procedure() {
    override val initialNode: Node = SelectOffensiveAssists
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<FoulContext>()
        // If the player that was banned had the Leader skill, we need to check
        // if anyone else has Leader, otherwise the reroll is removed.
        val leaderCommand = when (context.fouler.hasSkill(SkillType.LEADER)) {
            true -> rules.calculateLeaderRerollStatusChange(context.fouler.team)
            false -> null
        }
        return compositeCommandOf(
            leaderCommand,
            SetCurrentBall(null)
        )
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<FoulContext>()

    object SelectOffensiveAssists: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            val context = state.getContext<FoulContext>()
            return context.fouler.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<FoulContext>()
            val fouler = context.fouler
            val victim = context.victim!!
            val offensiveAssists = victim.coordinates.getSurroundingCoordinates(rules)
                .mapNotNull { state.pitch[it].player }
                .filter { it != fouler && it.team == fouler.team }
                .filter { player ->
                    rules.canOfferAssist(player, victim)
                }
            return when (offensiveAssists.isEmpty()) {
                true -> listOf(ContinueWhenReady)
                false -> listOf(CancelWhenReady, SelectPlayers(offensiveAssists))
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val assists = when (action) {
                Cancel, Continue -> 0
                is PlayersSelected -> action.players.count()
                else -> INVALID_ACTION(action)
            }
            val context = state.getContext<FoulContext>()
            return compositeCommandOf(
                UpdateContext(context.copy(offensiveAssists = assists)),
                GotoNode(CalculateDefensiveAssists)
            )
        }
    }

    // According to Designer's Commentary May 2026, defensive foul assists are mandatory,
    // so we just calculate them here.
    object CalculateDefensiveAssists: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<FoulContext>()
            val fouler = context.fouler
            val victim = context.victim!!
            val defensiveAssists = rules.calculateDefensiveAssists(victim, fouler)
            return compositeCommandOf(
                UpdateContext(context.copy(defensiveAssists = defensiveAssists)),
                GotoNode(CalculatePutTheBootInAssists)
            )
        }
    }

    // While only relevant for BB2025, we keep it in the common code as long as it can
    // stay as a computation node
    object CalculatePutTheBootInAssists: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            if (rules.baseVersion != GameVersion.BB2025) {
                return GotoNode(RollForFoul)
            }
            val context = state.getContext<FoulContext>()

            // We always apply "Put the Boot In" and Defensive modifiers.
            // They are technically optional skills, but there should be no reason (not
            // even a bad one) to not apply them.
            val bootAssists = context.victim!!.coordinates.getSurroundingCoordinates(rules)
                .mapNotNull { state.pitch[it].player }
                .filter { it != context.fouler }
                .filter { player ->
                    val hasPutTheBootIn = player.isSkillAvailable(SkillType.PUT_THE_BOOT_IN)
                    val ignorePutTheBootIn = rules.getMarkingPlayers(
                        game = state,
                        markedTeam = player.team,
                        square = player.coordinates
                    ).any {
                        it.isSkillAvailable(SkillType.DEFENSIVE)
                    }
                    hasPutTheBootIn && !ignorePutTheBootIn
                }
                .filter { rules.isStanding(it) && rules.isMarked(it) }

            return buildCompositeCommand {
                bootAssists.forEach { player ->
                    add(ReportSkillUsed(player, SkillType.PUT_THE_BOOT_IN))
                }
                addAll(
                    UpdateContext(
                        context.copy(
                            putTheBootInAssists = bootAssists.size
                        )
                    ),
                    GotoNode(RollForFoul)
                )
            }
        }
    }

    object RollForFoul: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val foulContext = state.getContext<FoulContext>()
            val injuryContext = RiskingInjuryContext(
                player = foulContext.victim!!,
                causedBy = foulContext.fouler,
                mode = RiskingInjuryMode.FOUL,
                armourModifiers = listOfNotNull(
                    when (foulContext.offensiveAssists > 0 || foulContext.putTheBootInAssists > 0) {
                        true -> OffensiveAssistArmourModifier(foulContext.offensiveAssists + foulContext.putTheBootInAssists,)
                        false -> null
                    },
                    if (foulContext.defensiveAssists > 0) DefensiveAssistsArmourModifier(foulContext.defensiveAssists) else null
                ).toPersistentList()
            )
            return AddContext(injuryContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val foulContext = state.getContext<FoulContext>()
            val injuryContext = state.getContext<RiskingInjuryContext>()
            val spottedByRefArmour: Boolean = (injuryContext.armourRoll[0].result == injuryContext.armourRoll[1].result)
            val spottedByRefInjury: Boolean = (injuryContext.injuryRoll.isNotEmpty() && (injuryContext.injuryRoll[0] == injuryContext.injuryRoll[1]))
            val spottedByRef = spottedByRefArmour || spottedByRefInjury

            // Check pre-conditions for Sneaky Git
            val isArmourBroken = injuryContext.armourBroken
            val isSneakyGitApplicable = !isArmourBroken && spottedByRefArmour

            return buildCompositeCommand {
                add(RemoveContext<RiskingInjuryContext>())
                add(
                    UpdateContext(foulContext.copy(
                        injuryRoll = injuryContext,
                        spottedByTheRef = spottedByRef,
                        hasFouled = true
                    ))
                )
                when {
                    isSneakyGitApplicable -> add(GotoNode(ChooseToUseSneakyGit))
                    spottedByRef -> add(GotoNode(HandleBeingSentOff))
                    else -> add(ExitProcedure())
                }
            }
        }
    }

    object ChooseToUseSneakyGit: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<FoulContext>().fouler.team
        }

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<FoulContext>()
            val hasSneakyGit = (context.fouler.isSkillAvailable(SkillType.SNEAKY_GIT))
            return if (hasSneakyGit) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<FoulContext>()
            val useSkill = (action == Confirm)
            return when (useSkill) {
                true -> compositeCommandOf(
                    ReportSkillUsed(context.fouler, SkillType.SNEAKY_GIT),
                    UpdateContext(context.copy(spottedByTheRef = false)),
                    ExitProcedure()
                )
                false -> GotoNode(HandleBeingSentOff)
            }
        }
    }

    object HandleBeingSentOff: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val foulContext = state.getContext<FoulContext>()
            val fouler = foulContext.fouler
            val hasBribes = fouler.team.bribes.any { !it.used }
            val sentOffContext = BeingSentOffContext(fouler, isBribeAvailable = hasBribes)
            return compositeCommandOf(
                ReportSpottedByRef(sentOffContext, usingSecretWeapon = false),
                AddContext(sentOffContext)
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BeingSentOff
        override fun onExitNode(state: Game, rules: Rules): Command {
            // All consequences have been handled in BeingSentOff, so just cleanup here
            return compositeCommandOf(
                RemoveContext<BeingSentOffContext>(),
                ExitProcedure()
            )
        }
    }
}
