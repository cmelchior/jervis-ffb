package com.jervisffb.ui.game.dialogs

import androidx.compose.ui.unit.Dp
import com.jervisffb.engine.actions.D12Result
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D20Result
import com.jervisffb.engine.actions.D2Result
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D4Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.common.context.FoulContext
import com.jervisffb.engine.common.context.PassContext
import com.jervisffb.engine.common.modifiers.CasualtyModifier
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.locations.OnPitchLocation
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Class wrapping the intent to show a dialog for a dice roll involving 1 - many dice.
 * A Confirm button will show the final result.
 */
class MultipleChoiceUserInputDialog(
    val icon: Any? = null, // TODO Replacement for Icon?
    val title: String,
    val message: String,
    val dice: List<Pair<Dice, List<DieResult>>>,
    val result: (DiceRollResults) -> String?,
    val nextActionId: GameActionId,
    override var owner: Team? = null,
    val width: Dp = DialogSize.MEDIUM,
    val movable: Boolean = true,
) : UserInputDialog {

    companion object {

        fun createWeatherRollDialog(id: GameActionId, rules: Rules): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Weather roll",
                message = "Roll 2D6 for the weather",
                dice =
                    listOf(
                        Pair(Dice.D6, D6Result.allOptions()),
                        Pair(Dice.D6, D6Result.allOptions()),
                    ),
                result = { rolls: DiceRollResults ->
                    val description =
                        rules.weatherTable.roll(
                            rolls.rolls.first() as D6Result,
                            rolls.rolls.last() as D6Result,
                        ).title
                    "$description (${rolls.sumOf { it.value }})"
                },
                nextActionId = id,
                movable = false,
                width = DialogSize.D6_SELECTOR
            )
        }

        fun createDeviateDialog(id: GameActionId, rules: Rules, isKickOff: Boolean = true): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = if (isKickOff) "The KickOff" else "Deviate the ball",
                message = "Roll Roll 1D8 + 1D6 to deviate the ball.",
                dice =
                    listOf(
                        Pair(Dice.D8, D8Result.allOptions()),
                        Pair(Dice.D6, D6Result.allOptions()),
                    ),
                result = { rolls: DiceRollResults ->
                    val d8 = rolls.first() as? D8Result ?: rolls.last() as D8Result
                    val d6 = rolls.last() as? D6Result ?: rolls.first() as D6Result
                    val description =
                        when (val direction = rules.direction(d8)) {
                            Direction(-1, -1) -> "Up-Left"
                            Direction(0, -1) -> "Up"
                            Direction(1, -1) -> "Up-Right"
                            Direction(-1, 0) -> "Left"
                            Direction(1, 0) -> "Right"
                            Direction(-1, 1) -> "Down-Left"
                            Direction(0, 1) -> "Down"
                            Direction(1, 1) -> "Down-Right"
                            else -> TODO("Not supported: $direction")
                        }
                    "$description(${d6.value})"
                },
                nextActionId = id,
                width = DialogSize.D8_SELECTOR
            )
        }

        fun createKickOffEventDialog(id: GameActionId, rules: Rules): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "KickOff Event",
                message = "Roll 2D6 for the KickOff event.",
                dice =
                    listOf(
                        Pair(Dice.D6, D6Result.allOptions()),
                        Pair(Dice.D6, D6Result.allOptions()),
                    ),
                result = { rolls: DiceRollResults ->
                    val description: String =
                        rules.kickOffEventTable.roll(
                            rolls.first() as D6Result,
                            rolls.last() as D6Result,
                        ).description
                    "$description (${rolls.sumOf { it.value }})"
                },
                nextActionId = id,
            )
        }

        fun createBlockRollDialog(id: GameActionId, diceCount: Int, isBlitz: Boolean): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "${ if (isBlitz) "Blitz" else "Block"} roll",
                message = "Roll ${diceCount}D6",
                dice = (1..diceCount).map { Pair(Dice.BLOCK, DBlockResult.allOptions()) },
                result = { _: DiceRollResults -> null },
                nextActionId = id,
            )
        }

        fun createSelectBlockDie(id: GameActionId, result: SelectDicePoolResult): UserInputDialog {
            return DicePoolUserInputDialog(
                dialogTitle = "Select Block Result",
                message = "Select die to apply",
                poolTitles = emptyList(),
                dice = result.pools.map { Pair(Dice.BLOCK, it) },
                nextActionId = id,
            )
        }

        fun createArmourRollDialog(id: GameActionId, player: Player): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Armour roll",
                message = "Roll 2D6 to break armour for ${player.name}",
                dice =
                    listOf(
                        Pair(Dice.D6, D6Result.allOptions()),
                        Pair(Dice.D6, D6Result.allOptions()),
                    ),
                result = { rolls: DiceRollResults ->
                    rolls.sum().toString()
                },
                nextActionId = id,
            )
        }

        fun createInjuryRollDialog(id: GameActionId, rules: Rules, player: Player): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Injury roll",
                message = "Roll 2D6 for an injury on ${player.name}",
                dice =
                    listOf(
                        Pair(Dice.D6, D6Result.allOptions()),
                        Pair(Dice.D6, D6Result.allOptions()),
                    ),
                result = { rolls: DiceRollResults ->
                    val result = rules.injuryTable.roll(rolls.first() as D6Result, rolls.last() as D6Result)
                    "${result.title} (${rolls.sum()})"
                },
                nextActionId = id,
            )
        }

        fun createCasualtyRollDialog(id: GameActionId, rules: Rules, player: Player): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Casualty roll",
                message = "Roll D16 for a casualty on ${player.name}",
                dice =
                    listOf(
                        Pair(Dice.D16, D16Result.allOptions()),
                    ),
                result = { rolls: DiceRollResults ->
                    // Check for Decay (ideally we should get this information from the Model layer somehow)
                    // but since this dialog will be gone soon, just hack it for now.
                    val modifiers = if (player.hasSkill(SkillType.DECAY)) {
                        listOf(CasualtyModifier.DECAY)
                    } else {
                        emptyList()
                    }
                    val result = rules.casualtyTable.roll(rolls.first() as D16Result, modifiers)
                    "${result.title} (${rolls.sum()})"
                },
                nextActionId = id,
            )
        }

        fun createLastingInjuryRollDialog(id: GameActionId, rules: Rules, player: Player): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Lasting Injury roll",
                message = "Roll D6 for a Lasting Injury on ${player.name}",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { rolls: DiceRollResults ->
                    val result = rules.lastingInjuryTable.roll(rolls.first() as D6Result)
                    "${result.description} (${rolls.sum()})"
                },
                nextActionId = id,
            )
        }

        fun createArgueTheCallRollDialog(id: GameActionId, context: FoulContext, rules: Rules): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Argue The Call Roll",
                message = "Roll D6 to Argue The Call on behalf of ${context.fouler.name}",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { rolls: DiceRollResults ->
                    val result = rules.argueTheCallTable.roll(rolls.first() as D6Result)
                    "${result.title} (${rolls.sum()})"
                },
                nextActionId = id,
            )
        }

        fun createAccuracyRollDialog(id: GameActionId, passContext: PassContext, rules: Rules): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Test for Accuracy",
                message = "${passContext.thrower.name} rolls a D6 to test for accuracy when making a pass",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { _: DiceRollResults -> null },
                nextActionId = id,
            )
        }

        fun createScatterRollDialog(id: GameActionId, rules: Rules): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Scatter Roll",
                message = "Roll 3D8 to scatter the ball",
                dice = listOf(Pair(Dice.D8, D8Result.allOptions()), Pair(Dice.D8, D8Result.allOptions()), Pair(Dice.D8, D8Result.allOptions())),
                result = { dice: DiceRollResults ->
                    dice.joinToString(prefix = "[", postfix = "]") { result: DieResult ->
                        if (result is D8Result) {
                            when (val direction = rules.direction(result)) {
                                Direction(-1, -1) -> "Up-Left"
                                Direction(0, -1) -> "Up"
                                Direction(1, -1) -> "Up-Right"
                                Direction(-1, 0) -> "Left"
                                Direction(1, 0) -> "Right"
                                Direction(-1, 1) -> "Down-Left"
                                Direction(0, 1) -> "Down"
                                Direction(1, 1) -> "Down-Right"
                                else -> TODO("Not supported: $direction")
                            }
                        } else {
                            "null"
                        }
                    }
                },
                nextActionId = id,
            )
        }

        fun createDodgeRollDialog(id: GameActionId, player: Player, target: PitchCoordinate): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Dodge Roll",
                message = "${player.name} rolls D6 to dodge to ${target.toLogString()}.",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { _: DiceRollResults -> null },
                nextActionId = id,
            )
        }

        fun createRushRollDialog(id: GameActionId, player: Player, target: OnPitchLocation): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Rush Roll",
                message = "${player.name} rolls D6 to rush to ${target.toLogString()}",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { _: DiceRollResults -> null },
                nextActionId = id,
            )
        }

        fun createSwelteringHeatRollDialog(id: GameActionId): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Sweltering Heat Roll",
                message = "Roll D3 to find number of affected players.",
                dice = listOf(Pair(Dice.D3, D3Result.allOptions())),
                result = { _: DiceRollResults -> null },
                nextActionId = id,
            )
        }

        fun createPrayersToNuffleRollDialog(id: GameActionId, rules: Rules, rollsRemaining: Int): UserInputDialog {
            val diceOptions = when (rules.prayersToNuffleTable.die) {
                Dice.D8 -> Pair(Dice.D8, D8Result.allOptions())
                Dice.D16 -> Pair(Dice.D16, D16Result.allOptions())
                else -> error("Dice: ${rules.prayersToNuffleTable.die} not supported for Prayers to Nuffle")
            }
            return MultipleChoiceUserInputDialog(
                title = "Prayer to Nuffle Roll ($rollsRemaining rolls)",
                message = "Roll ${rules.prayersToNuffleTable.die.name} to choose a prayer",
                dice = listOf(diceOptions),
                result = { rolls: DiceRollResults ->
                    rules.prayersToNuffleTable.roll(rolls.first()).description
                },
                nextActionId = id,
            )
        }

        fun createBadHabitsRoll(id: GameActionId): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Bad Habits Roll",
                message = "Roll D3 to find number of affected players",
                dice = listOf(Pair(Dice.D3, D3Result.allOptions())),
                result = { _: DiceRollResults -> null },
                nextActionId = id,
            )
        }

        fun createCheeringFansRollDialog(id: GameActionId, team: Team): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Cheering Fans Roll",
                message = "${team.name} rolls a D6 for Cheering Fans",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { _: DiceRollResults -> null },
                nextActionId = id,
            )
        }

        fun createBrilliantCoachingRolLDialog(id: GameActionId, team: Team): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Brilliant Coaching Roll",
                message = "${team.name} rolls a D6 for Brilliant Coaching",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { _: DiceRollResults -> null },
                nextActionId = id,
            )
        }

        fun createOfficiousRefRollDialog(id: GameActionId, team: Team): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Officious Ref Roll",
                message = "${team.name} rolls a D6 for Officious Ref",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { _: DiceRollResults -> null },
                nextActionId = id,
            )
        }

        fun createOfficiousRefPlayerRollDialog(id: GameActionId, player: Player): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Officious Ref Player Roll",
                message = "${player.name} rolls a D6 while arguing with the Ref",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { _: DiceRollResults -> null },
                nextActionId = id,
            )
        }

        fun createStandingUpRollDialog(id: GameActionId, player: Player): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Standing up Roll",
                message = "Roll D6 for ${player.name} to stand up.",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { _: DiceRollResults -> null },
                nextActionId = id,
            )
        }

        fun createApothecaryInjuryRollDialog(id: GameActionId, player: Player): UserInputDialog {
            return MultipleChoiceUserInputDialog(
                title = "Patching-up Casualty",
                message = "Roll D6 to see if the apothecary can patch up ${player.name}",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { _: DiceRollResults -> null },
                nextActionId = id,
            )
        }

        fun createUnknownDiceRoll(id: GameActionId, dicePool: RollDice): UserInputDialog {
            val dice= dicePool.dice.map {
                when (it) {
                    Dice.D2 -> Pair(it, D2Result.allOptions())
                    Dice.D3 -> Pair(it, D3Result.allOptions())
                    Dice.D4 -> Pair(it, D4Result.allOptions())
                    Dice.D6 -> Pair(it, D6Result.allOptions())
                    Dice.D8 -> Pair(it, D8Result.allOptions())
                    Dice.D12 -> Pair(it, D12Result.allOptions())
                    Dice.D16 -> Pair(it, D16Result.allOptions())
                    Dice.D20 -> Pair(it, D20Result.allOptions())
                    Dice.BLOCK -> Pair(it, DBlockResult.allOptions())
                }
            }

            return MultipleChoiceUserInputDialog(
                title = "Unknown Dice Roll",
                message = "Unmapped die roll (see logs for details)",
                dice = dice,
                result = { _: DiceRollResults -> null },
                nextActionId = id,
            )
        }
    }
}
