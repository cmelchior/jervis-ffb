package com.jervisffb.engine.rules.common.planner

import com.jervisffb.engine.GameRulesContext
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.common.pathfinder.PathFinder

/**
 * Sometimes you want to know possible valid future actions to plan the current
 * one. The UI mostly uses this to present a nicer UX.
 *
 * This interface is responsible for doing that, taking both static rules and
 * game policies into account. No component should try to do this on its own,
 * they should always go through this interface.
 *
 * It is accessed through [GameRulesContext.actionPlanner].
 *
 * Developer's Commentary:
 * This interface is just a short-gap to avoid the overhead of running two
 * full games in parallel. Right now, we mostly use it to work around issues
 * with standard movement, which always consists of "select move type + square".
 *
 * If this interface starts getting too much responsibility, we should
 * reconsider if just running two engines in parallel might be easier.
 */
interface ActionPlanner {

    // Can be used to calculate paths between squares on the pitch.
    val pathFinder: PathFinder

    /**
     * Creates a movement plan, optionally limited to [maxSteps] movement steps.
     * A `null` limit includes all movement steps currently available.
     */
    fun createMovePlan(
        state: Game, // Current state of the game, from which to create the movement plan.
        player: Player, // Player to create the movement plan for.
        maxSteps: Int? = null, // Limit the number of movement steps to consider.
    ): MovePlan

}
