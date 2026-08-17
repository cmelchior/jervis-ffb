package com.jervisffb.engine.common.inducements

import com.jervisffb.engine.model.inducements.settings.InducementGroup
import com.jervisffb.engine.model.inducements.settings.InducementGroupBuilder
import com.jervisffb.engine.model.inducements.settings.SingleInducement
import com.jervisffb.engine.model.inducements.settings.SingleInducementBuilder
import com.jervisffb.engine.model.inducements.settings.TeamPlayerInducementBuilder

// Top-level interface for all common inducement groups
sealed interface CommonInducementGroup<GB: InducementGroupBuilder, IB: SingleInducementBuilder, I: SingleInducement<IB>>
    : InducementGroup<GB, IB, I>

// Top-level builder interface for all common inducement groups.
sealed interface CommonInducementGroupBuilder: InducementGroupBuilder

// Top-level builder interface for all common single inducements.
sealed interface CommonSingleInducementBuilder: SingleInducementBuilder

// Top-level interface for common inducements that are "normal" team players.
sealed interface CommonTeamPlayerInducementBuilder: TeamPlayerInducementBuilder
