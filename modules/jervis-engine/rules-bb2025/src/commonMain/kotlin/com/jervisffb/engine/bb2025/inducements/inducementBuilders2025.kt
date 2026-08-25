package com.jervisffb.engine.bb2025.inducements

import com.jervisffb.engine.model.inducements.settings.InducementGroup
import com.jervisffb.engine.model.inducements.settings.InducementGroupBuilder
import com.jervisffb.engine.model.inducements.settings.SingleInducement
import com.jervisffb.engine.model.inducements.settings.SingleInducementBuilder
import com.jervisffb.engine.model.inducements.settings.TeamPlayerInducementBuilder

// Top-level interface for all BB20205 specific inducement groups
sealed interface InducementGroup2025<GB: InducementGroupBuilder, IB: SingleInducementBuilder, I: SingleInducement<IB>>
: InducementGroup<GB, IB, I>

// Top-level builder interface for all BB20205 specific builders for inducement groups.
sealed interface InducementGroupBuilder2025: InducementGroupBuilder

// Top-level builder interface for all BB20205 specific builders for single inducements.
sealed interface SingleInducementBuilder2025: SingleInducementBuilder

// Top-level interface for BB20205 specific inducements builders for "normal" team players.
sealed interface TeamPlayerInducementBuilder2025: TeamPlayerInducementBuilder
