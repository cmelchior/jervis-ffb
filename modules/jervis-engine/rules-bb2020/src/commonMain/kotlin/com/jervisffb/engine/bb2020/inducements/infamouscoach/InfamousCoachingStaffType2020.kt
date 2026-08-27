package com.jervisffb.engine.bb2020.inducements.infamouscoach

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.infamouscoach.InfamousCoachingStaff
import com.jervisffb.engine.model.inducements.infamouscoach.InfamousCoachingStaffType
import kotlinx.serialization.Serializable

@Serializable
enum class InfamousCoachingStaffType2020(
    override val label: String
): InfamousCoachingStaffType {
    JOSEF_BUGMAN("Josef Bugman") {
        override fun create(team: Team): InfamousCoachingStaff = JosefBugman(team)
    },
    KARI_COLDSTEEL("Kari Coldsteel") {
        override fun create(team: Team): InfamousCoachingStaff = KariColdsteel(team)
    },
    PAPA_SKULLBONES("Papa Skullbones") {
        override fun create(team: Team): InfamousCoachingStaff = PapaSkullbones(team)
    }
}
