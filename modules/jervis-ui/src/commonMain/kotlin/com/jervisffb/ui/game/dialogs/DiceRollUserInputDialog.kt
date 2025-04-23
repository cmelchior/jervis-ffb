package com.jervisffb.ui.game.dialogs

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
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.OnFieldLocation
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.foul.FoulContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.PassContext

/**
 * Class wrapping the intent to show a dialog for a dice roll involving multiple dice.
 * Each die gets its own line (since we assume this is only being used up to D8)
 * And the confirm button will show the final result
 */
class DiceRollUserInputDialog(
    val icon: Any? = null, // TODO Replacement for Icon?
    val title: String,
    val message: String,
    val dice: List<Pair<Dice, List<DieResult>>>,
    val result: (DiceRollResults) -> String?,
    override var owner: Team? = null,
) : UserInputDialog {

    companion object {
        fun createFanFactorDialog(team: Team): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "Fan Factor Roll",
                message = "Roll D3 for ${team.name}",
                dice = listOf(Pair(Dice.D3, D3Result.allOptions())),
                result = { rolls: DiceRollResults -> null },
            )
        }

        fun createWeatherRollDialog(rules: Rules): UserInputDialog {
            return DiceRollUserInputDialog(
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
            )
        }

        fun createDeviateDialog(rules: Rules, isKickOff: Boolean = true): UserInputDialog {
            return DiceRollUserInputDialog(
                title = if (isKickOff) "The KickOff"  else "Deviate the ball",
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
            )
        }

        fun createKickOffEventDialog(rules: Rules): UserInputDialog {
            return DiceRollUserInputDialog(
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
            )
        }

        fun createBlockRollDialog(diceCount: Int, isBlitz: Boolean): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "${ if (isBlitz) "Blitz" else "Block"} roll",
                message = "Roll ${diceCount}D6",
                dice = (1..diceCount).map { Pair(Dice.BLOCK, DBlockResult.allOptions()) },
                result = { rolls: DiceRollResults -> null },
            )
        }

        fun createSelectBlockDie(result: SelectDicePoolResult): UserInputDialog {
            return DicePoolUserInputDialog(
                dialogTitle = "Select Block Result",
                message = "Select die to apply",
                poolTitles = emptyList(),
                dice = result.pools.map { Pair(Dice.BLOCK, it) },
            )
        }

        fun createArmourRollDialog(player: Player): UserInputDialog {
            return DiceRollUserInputDialog(
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
            )
        }

        fun createInjuryRollDialog(rules: Rules, player: Player): UserInputDialog {
            return DiceRollUserInputDialog(
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
            )
        }

        fun createCasualtyRollDialog(rules: Rules, player: Player): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "Casualty roll",
                message = "Roll D16 for a casualty on ${player.name}",
                dice =
                    listOf(
                        Pair(Dice.D16, D16Result.allOptions()),
                    ),
                result = { rolls: DiceRollResults ->
                    val result = rules.casualtyTable.roll(rolls.first() as D16Result)
                    "${result.title} (${rolls.sum()})"
                },
            )
        }

        fun createLastingInjuryRollDialog(rules: Rules, player: Player): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "Lasting Injury roll",
                message = "Roll D6 for a Lasting Injury on ${player.name}",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { rolls: DiceRollResults ->
                    val result = rules.lastingInjuryTable.roll(rolls.first() as D6Result)
                    "${result.description} (${rolls.sum()})"
                },
            )
        }

        fun createArgueTheCallRollDialog(context: FoulContext, rules: Rules): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "Argue The Call Roll",
                message = "Roll D6 to Argue The Call on behalf of ${context.fouler.name}",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { rolls: DiceRollResults ->
                    val result = rules.argueTheCallTable.roll(rolls.first() as D6Result)
                    "${result.title} (${rolls.sum()})"
                },
            )
        }

        fun createAccuracyRollDialog(passContext: PassContext, rules: Rules): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "Test for Accuracy",
                message = "${passContext.thrower.name} rolls a D6 to test for accuracy when making a pass",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { _: DiceRollResults -> null }
            )
        }

        fun createScatterRollDialog(rules: Rules): UserInputDialog {
            return DiceRollUserInputDialog(
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
                            }.toString()
                        } else {
                            "null"
                        }
                    }
                }
            )
        }

        fun createDodgeRollDialog(player: Player, target: FieldCoordinate): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "Dodge Roll",
                message = "${player.name} rolls D6 to dodge to ${target.toLogString()}.",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { rolls: DiceRollResults -> null }
            )
        }

        fun createRushRollDialog(player: Player, target: OnFieldLocation): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "Rush Roll",
                message = "${player.name} rolls D6 to rush to ${target.toLogString()}",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { rolls: DiceRollResults -> null }
            )
        }

        fun createSwelteringHeatRollDialog(): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "Sweltering Heat Roll",
                message = "Roll D3 to find number of affected players.",
                dice = listOf(Pair(Dice.D3, D3Result.allOptions())),
                result = { rolls: DiceRollResults -> null }
            )
        }

        fun createPrayersToNuffleRollDialog(rules: Rules, rollsRemaining: Int): UserInputDialog {
            val diceOptions = when (rules.prayersToNuffleTable.die) {
                Dice.D8 -> Pair(Dice.D8, D8Result.allOptions())
                Dice.D16 -> Pair(Dice.D16, D16Result.allOptions())
                else -> error("Dice: ${rules.prayersToNuffleTable.die} not supported for Prayers to Nuffle")
            }
            return DiceRollUserInputDialog(
                title = "Prayer to Nuffle Roll ($rollsRemaining rolls)",
                message = "Roll ${rules.prayersToNuffleTable.die.name} to choose a prayer",
                dice = listOf(diceOptions),
                result = { rolls: DiceRollResults ->
                    rules.prayersToNuffleTable.roll(rolls.first()).description
                }
            )
        }

        fun createBadHabitsRoll(): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "Bad Habits Roll",
                message = "Roll D3 to find number of affected players",
                dice = listOf(Pair(Dice.D3, D3Result.allOptions())),
                result = { _: DiceRollResults -> null }
            )
        }

        fun createCheeringFansRollDialog(team: Team): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "Cheering Fans Roll",
                message = "${team.name} rolls a D6 for Cheering Fans",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { _: DiceRollResults -> null }
            )
        }

        fun createBrilliantCoachingRolLDialog(team: Team): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "Brilliant Coaching Roll",
                message = "${team.name} rolls a D6 for Brilliant Coaching",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { _: DiceRollResults -> null }
            )
        }

        fun createOfficiousRefRollDialog(team: Team): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "Officious Ref Roll",
                message = "${team.name} rolls a D6 for Officious Ref",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { _: DiceRollResults -> null }
            )
        }

        fun createOfficiousRefPlayerRollDialog(player: Player): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "Officious Ref Player Roll",
                message = "${player.name} rolls a D6 while arguing with the Ref",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { _: DiceRollResults -> null }
            )
        }

        fun createStandingUpRollDialog(player: Player): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "Standing up Roll",
                message = "Roll D6 for ${player.name} to stand up.",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { _: DiceRollResults -> null }
            )
        }

        fun createApothecaryInjuryRollDialog(player: Player): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "Patching-up Casualty",
                message = "Roll D6 to see if the apothecary can patch up ${player.name}",
                dice = listOf(Pair(Dice.D6, D6Result.allOptions())),
                result = { _: DiceRollResults -> null }
            )
        }

        fun createUnknownDiceRoll(dicePool: RollDice): UserInputDialog {
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

            return DiceRollUserInputDialog(
                title = "Unknown Dice Roll",
                message = "Unmapped die roll (see logs for details)",
                dice = dice,
                result = { _: DiceRollResults -> null }
            )
        }
    }
}
