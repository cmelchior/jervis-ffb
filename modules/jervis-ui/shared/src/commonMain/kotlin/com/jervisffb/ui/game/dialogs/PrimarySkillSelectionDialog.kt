package com.jervisffb.ui.game.dialogs

import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.ui.game.model.ModelRef

/** Dialog for selecting a new skill from a player's primary skill categories. */
class PrimarySkillSelectionDialog(
    val player: ModelRef<Player>,
    val primaryCategories: List<SkillCategory>,
    val skills: List<SkillId>,
    val title: String,
    val nextActionId: GameActionId,
    override var owner: Team? = null,
) : UserInputDialog
