package dk.ilios.jervis.serialize

import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.Rules
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

@Serializable
data class JervisFile(
    val metadata: JervisMetaData,
    val configuration: JervisConfiguration,
    val game: JervisGameData
)

@Serializable
data class JervisMetaData(
    val fileFormat: Int = 1
)

// Class encapsulating all rules, teams and other game configurations that are user defined.
@Serializable
data class JervisConfiguration(
    val rules: Rules,
)

/**
 * Class encapsulating the actual game state and all actions
 */
@Serializable
data class JervisGameData(
    val homeTeam: Team,
    val awayTeam: Team,
    val actions: List<GameAction>
)
