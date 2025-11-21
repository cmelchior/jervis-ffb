package com.jervisffb.engine.rules

import com.jervisffb.engine.rules.bb2025.BB2025SkillSettings
import com.jervisffb.engine.rules.bb2025.BB2025TeamActions
import com.jervisffb.engine.rules.bb2025.tables.BB2025ArgueTheCallTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025CasualtyTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025LastingInjuryTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025RangeRuler
import com.jervisffb.engine.rules.bb2025.tables.BB2025StandardInjuryTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StandardKickOffEventTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StandardPrayersToNuffleTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StandardWeatherTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StuntyInjuryTable
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.GameVersion
import io.ktor.http.parameters
import kotlinx.serialization.Serializable

/**
 * Top-level class for all variants of the 2025 Blood Bowl rules.
 */
@Serializable
abstract class BB2025Rules(
    private val bb2025RuleParameters: RulesParametersHolder
) : Rules(bb2025RuleParameters) {
    companion object {
        val DEFAULTS = RulesParametersHolder(
            name = "Blood Bowl 2025 Rules",
            baseVersion = GameVersion.BB2025,
            gameType = GameType.STANDARD,
            teamActions = BB2025TeamActions(),
            skillSettings = BB2025SkillSettings(),
            kickOffEventTable = BB2025StandardKickOffEventTable,
            prayersToNuffleTable = BB2025StandardPrayersToNuffleTable,
            weatherTable = BB2025StandardWeatherTable,
            injuryTable = BB2025StandardInjuryTable,
            stuntyInjuryTable = BB2025StuntyInjuryTable,
            casualtyTable = BB2025CasualtyTable,
            lastingInjuryTable = BB2025LastingInjuryTable,
            argueTheCallTable = BB2025ArgueTheCallTable,
            rangeRuler = BB2025RangeRuler,)
    }
}


@Serializable
class StandardBB2025Rules(
    private val standardBB2025RuleParameters: RulesParametersHolder = DEFAULTS
) : BB2025Rules(standardBB2025RuleParameters) {

    companion object {
        val DEFAULTS = BB2025Rules.DEFAULTS.copy(
            name = "Blood Bowl 2025 Rules (Strict)",
        )
    }

    // Builder API infrastructure
    override fun toBuilder() = StandardBB2025RulesBuilder(standardBB2025RuleParameters)
    class StandardBB2025RulesBuilder(parameters: RulesParameters): RulesParameterBuilder(parameters) {
        override fun build(): StandardBB2025Rules = StandardBB2025Rules(buildParameters())
    }
}
