package com.jervisffb.engine.bb2020.inducements

import com.jervisffb.engine.model.inducements.settings.InducementGroup
import com.jervisffb.engine.model.inducements.settings.InducementGroupBuilder
import com.jervisffb.engine.model.inducements.settings.SingleInducement
import com.jervisffb.engine.model.inducements.settings.SingleInducementBuilder
import com.jervisffb.engine.model.inducements.settings.TeamPlayerInducementBuilder

// Top-level interface for all BB2020 specific inducement groups
sealed interface BB2020InducementGroup<GB: InducementGroupBuilder, IB: SingleInducementBuilder, I: SingleInducement<IB>>
: InducementGroup<GB, IB, I>

// Top-level builder interface for all BB2020 specific builders for inducement groups.
sealed interface BB2020InducementGroupBuilder: InducementGroupBuilder

// Top-level builder interface for all BB2020 specific builders for single inducements.
sealed interface BB2020SingleInducementBuilder: SingleInducementBuilder

// Top-level interface for BB2020 specific inducements builder for "normal" team players.
sealed interface BB2020TeamPlayerInducementBuilder: TeamPlayerInducementBuilder
