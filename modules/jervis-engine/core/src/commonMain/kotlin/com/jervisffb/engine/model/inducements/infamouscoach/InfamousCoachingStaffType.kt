package com.jervisffb.engine.model.inducements.infamouscoach

import com.jervisffb.engine.model.Team

/**
 * Interface describing the type of coaching staff.
 * This is used to more easily identify the coaching staff when configuring
 * inducements available for the game.
 */
interface InfamousCoachingStaffType {
    val label: String
    fun create(team: Team): InfamousCoachingStaff
}
