package com.jervisffb.ui.game.animations

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.OnFieldLocation
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.Bounce
import com.jervisffb.engine.rules.bb2020.procedures.Catch
import com.jervisffb.engine.rules.bb2020.procedures.CatchRoll
import com.jervisffb.engine.rules.bb2020.procedures.TheKickOffEvent
import com.jervisffb.engine.rules.bb2020.procedures.WeatherRoll
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.ChangingWeather
import com.jervisffb.engine.rules.bb2020.tables.KickOffEvent
import com.jervisffb.engine.rules.bb2020.tables.TableResult
import com.jervisffb.engine.rules.bb2020.tables.Weather
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_blitz
import com.jervisffb.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_blizzard
import com.jervisffb.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_brilliant_coaching
import com.jervisffb.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_cheering_fans
import com.jervisffb.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_get_the_ref
import com.jervisffb.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_high_kick
import com.jervisffb.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_nice
import com.jervisffb.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_officious_ref
import com.jervisffb.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_pitch_invasion
import com.jervisffb.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_pouring_rain
import com.jervisffb.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_quick_snap
import com.jervisffb.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_solid_defence
import com.jervisffb.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_sweltering_heat
import com.jervisffb.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_timeout
import com.jervisffb.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_very_sunny

/**
 * Class responsible for detecting if an animation should be run, and which one.
 * There are 3 places an animation can run:
 *
 * 1. At the beginning of the loop, but before the UI is updated.
 * 2. After the UI is updated, but before action decorators are used.
 * 3. After an action has been selected, but before it is applied to the model.
 */
object AnimationFactory {

    // Gravity constant used for animations
    const val GRAVITY = 9.81f

    /**
     * Return animation being run at the beginning of a frame, before the
     * UI has updated to the latest state.
     */
    fun getPreUpdateAnimation(state: Game): JervisAnimation? {
        return null
    }

    /**
     * Return animation being run after the UI has been updated to the latest state,
     * but before action decorators are used.
     */
    fun getFrameAnimation(state: Game, rules: Rules): JervisAnimation? {
        val state = state
        val stack = state.stack

        // Animate kick-off
        // We want to animate the kick-off (ball flying) after the kick-off event has been resolved.
        // This event is a bit complicated to track as we don't fully know what happens during the KickOff Event
        val firstCatch = (
            stack.singleCurrentNode(CatchRoll.RollDie)
                && !stack.containsNode(Bounce.ResolveCatch)
                && !stack.containsNode(Catch.CatchFailed)
        )
        val firstBounce = (stack.singleCurrentNode(Bounce.RollDirection) && !stack.containsNode(Catch.CatchFailed))
        val isResolvingLanding = stack.containsNode(TheKickOffEvent.ResolveBallLanding)
        val touchBack = stack.currentNode() == TheKickOffEvent.TouchBack
        if (
            (firstCatch && !firstBounce && isResolvingLanding)
            || (!firstCatch && firstBounce && isResolvingLanding)
            || touchBack
        ) {
            // The kicking player might no longer be on the field. For example, in the case of a Pitch Invasion
            // or a Blitz. For now, we just decide to animate the ball from the center of the end-zone, but maybe
            // some other solution would be funnier/better?
            val from = when (val kickerLocation = state.kickingPlayer!!.location) {
                is OnFieldLocation -> kickerLocation
                else -> {
                    if (state.kickingPlayer!!.isOnHomeTeam()) {
                        val x = 0
                        val y = state.rules.fieldHeight / 2
                        FieldCoordinate(x, y)
                    } else {
                        val x = state.rules.fieldWidth - 1
                        val y = state.rules.fieldHeight / 2
                        FieldCoordinate(x, y)
                    }
                }
            }
            var to = state.singleBall().location
            val outOfBounds = false
            if (to.isOutOfBounds(rules)) {
                to = state.singleBall().outOfBoundsAt!!
            }
            return PassAnimation(from, to, outOfBounds)
        }

        return null
    }

    /**
     * Returns animation being run after an action has been selected, but
     * before it is being sent to the [GameEngineController].
     */
    fun getPostActionAnimation(state: Game, action: GameAction): JervisAnimation? {
        if (action == Undo) return null
        val currentNode = state.currentProcedure()?.currentNode()

        // Animate KickOff Event Result
        // Right now we just "guess" that the rules do the same table lookup.
        // This is pretty annoying, but there is no stable place we can check after
        // executing the event (and some events are just pure computation nodes).
        // We could also introduce a "Confirm"-node in the Engine, but doing that solely
        // to support animations is also annoying.
        if (currentNode == TheKickOffEvent.RollForKickOffEvent) {
            val roll = (action as DiceRollResults).rolls.map { it as D6Result }
            val result: TableResult = state.rules.kickOffEventTable.roll(roll.first(), roll.last())
            val image = when (result) {
                KickOffEvent.GET_THE_REF -> Res.drawable.icons_animation_kickoff_kick_off_get_the_ref
                KickOffEvent.TIME_OUT -> Res.drawable.icons_animation_kickoff_kick_off_timeout
                KickOffEvent.SOLID_DEFENSE -> Res.drawable.icons_animation_kickoff_kick_off_solid_defence
                KickOffEvent.HIGH_KICK -> Res.drawable.icons_animation_kickoff_kick_off_high_kick
                KickOffEvent.CHEERING_FANS -> Res.drawable.icons_animation_kickoff_kick_off_cheering_fans
                KickOffEvent.CHANGING_WEATHER -> null
                KickOffEvent.BRILLIANT_COACHING -> Res.drawable.icons_animation_kickoff_kick_off_brilliant_coaching
                KickOffEvent.QUICK_SNAP -> Res.drawable.icons_animation_kickoff_kick_off_quick_snap
                KickOffEvent.BLITZ -> Res.drawable.icons_animation_kickoff_kick_off_blitz
                KickOffEvent.OFFICIOUS_REF -> Res.drawable.icons_animation_kickoff_kick_off_officious_ref
                KickOffEvent.PITCH_INVASION -> Res.drawable.icons_animation_kickoff_kick_off_pitch_invasion
                else -> null
            }
            return if (image != null) {
                KickOffEventAnimation(image)
            } else {
                null
            }
        }

        // Weather changes due to Changing Weather kickoff event is reported as the final weather result
        if (currentNode == WeatherRoll.RollWeatherDice && state.stack.get(-1).currentNode() == ChangingWeather.ChangeWeather) {
            val roll = (action as DiceRollResults).rolls.map { it as D6Result }
            val result: Weather = state.rules.weatherTable.roll(roll.first(), roll.last())
            val weatherImage = when (result) {
                Weather.SWELTERING_HEAT -> Res.drawable.icons_animation_kickoff_kick_off_sweltering_heat
                Weather.VERY_SUNNY -> Res.drawable.icons_animation_kickoff_kick_off_very_sunny
                Weather.PERFECT_CONDITIONS -> Res.drawable.icons_animation_kickoff_kick_off_nice
                Weather.POURING_RAIN -> Res.drawable.icons_animation_kickoff_kick_off_pouring_rain
                Weather.BLIZZARD -> Res.drawable.icons_animation_kickoff_kick_off_blizzard
            }
            return KickOffEventAnimation(weatherImage)
        }

        return null
    }
}
