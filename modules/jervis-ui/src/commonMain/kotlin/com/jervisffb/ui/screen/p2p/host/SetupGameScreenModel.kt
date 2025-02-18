package com.jervisffb.ui.screen.p2p.host

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.model.BallType
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.PitchType
import com.jervisffb.engine.model.StadiumType
import com.jervisffb.engine.rules.bb2020.tables.SpringWeatherTable
import com.jervisffb.engine.rules.bb2020.tables.StandardKickOffEventTable
import com.jervisffb.engine.rules.bb2020.tables.StandardWeatherTable
import com.jervisffb.engine.rules.bb2020.tables.SummerWeatherTable
import com.jervisffb.engine.rules.bb2020.tables.WinterWeatherTable
import com.jervisffb.ui.PROPERTIES_MANAGER
import com.jervisffb.ui.viewmodel.MenuViewModel
import com.jervisffb.utils.PROP_DEFAULT_COACH_NAME
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SetupGameScreenModel(private val menuViewModel: MenuViewModel, private val parentModel: P2PHostScreenModel) : ScreenModel {

    val coachName = MutableStateFlow("")
    val gameName = MutableStateFlow("Game-${Random.nextInt(10_000)}")
    val port = MutableStateFlow<Int?>(8080)
    val isSetupValid = MutableStateFlow<Boolean>(false)

    val selectedWeatherTable = MutableStateFlow<WeatherTableEntry?>(null)
    val selectedKickOffTable = MutableStateFlow<KickOffTableEntry?>(null)
    val selectedUnusualBall = MutableStateFlow<UnusualBallEntry?>(null)
    val selectedPitch = MutableStateFlow<PitchEntry?>(null)

    val weatherTables = listOf(
        "Rulebook" to listOf(
            WeatherTableEntry("Standard", StandardWeatherTable, true),
        ),
        "Death Zone" to listOf(
            WeatherTableEntry("Spring", SpringWeatherTable, false),
            WeatherTableEntry("Summer", SummerWeatherTable, false),
            WeatherTableEntry("Autumn", SummerWeatherTable, false),
            WeatherTableEntry("Winter", WinterWeatherTable, false),
            WeatherTableEntry("Subterranean", StandardWeatherTable, false),
            WeatherTableEntry("Primordial", StandardWeatherTable, false),
            WeatherTableEntry("Graveyard", StandardWeatherTable, false),
            WeatherTableEntry("Desolate Wasteland", StandardWeatherTable, false),
            WeatherTableEntry("Mountainous", StandardWeatherTable, false),
            WeatherTableEntry("Coastal", StandardWeatherTable, false),
            WeatherTableEntry("Desert", StandardWeatherTable, false),
        )
    )

    val kickOffTables = listOf(
        "Rulebook" to listOf(
            KickOffTableEntry("Standard", StandardKickOffEventTable, true),
        ),
        "Spike Magazine 15 (Amazons)" to listOf(
            KickOffTableEntry("Temple-City", StandardKickOffEventTable, false),
        )
    )

    val unusualBallList = listOf(
        "Rulebook" to listOf(
            UnusualBallEntry("Normal Ball", NoUnusualBall, true)
        ),
        "Death Zone" to listOf(
            UnusualBallEntry("Roll On Unusual Balls Table", RollOnUnusualBallTable, false),
            UnusualBallEntry("Explodin'", SpecificUnusualBall(BallType.EXPLODIN), false),
            UnusualBallEntry("Deamonic", SpecificUnusualBall(BallType.DEAMONIC), false),
            UnusualBallEntry("Stacked Lunch", SpecificUnusualBall(BallType.STACKED_LUNCH), false),
            UnusualBallEntry("Draconic", SpecificUnusualBall(BallType.DRACONIC), false),
            UnusualBallEntry("Spiteful Sprite", SpecificUnusualBall(BallType.SPITEFUL_SPRITE), false),
            UnusualBallEntry("Master-hewn", SpecificUnusualBall(BallType.MASTER_HEWN), false),
            UnusualBallEntry("Extra Spiky", SpecificUnusualBall(BallType.EXTRA_SPIKY), false),
            UnusualBallEntry("Greedy Nurgling", SpecificUnusualBall(BallType.GREEDY_NURGLING), false),
            UnusualBallEntry("Dark Majesty", SpecificUnusualBall(BallType.DARK_MAJESTY), false),
            UnusualBallEntry("Shady Special", SpecificUnusualBall(BallType.SHADY_SPECIAL), false),
            UnusualBallEntry("Soulstone", SpecificUnusualBall(BallType.SOULSTONE), false),
            UnusualBallEntry("Frozen", SpecificUnusualBall(BallType.FROZEN_BALL), false),
            UnusualBallEntry("Sacred Egg", SpecificUnusualBall(BallType.SACRED_EGG), false),
            UnusualBallEntry("Snotling Ball-suite", SpecificUnusualBall(BallType.SNOTLING_BALL_SUIT), false),
            UnusualBallEntry("Limpin' Squig", SpecificUnusualBall(BallType.LIMPIN_SQUIG), false),
            UnusualBallEntry("Warpstone Brazier", SpecificUnusualBall(BallType.WARPSTONE_BRAZIER), false),
        ),
        "Spike Magazine 14 (Norse)" to listOf(
            UnusualBallEntry("Hammer of Legend", SpecificUnusualBall(BallType.HAMMER_OF_LEGEND), false),
            UnusualBallEntry("The Runestone", SpecificUnusualBall(BallType.THE_RUNESTONE), false),
        ),
        "Spike Magazine 15 (Amazons)" to listOf(
            UnusualBallEntry("Crystal Skull", SpecificUnusualBall(BallType.CRYSTAL_SKULL), false),
            UnusualBallEntry("Snake-swallowed", SpecificUnusualBall(BallType.SNAKE_SWALLOWED), false),
        ),
    )

    val pitches = listOf(
        "Rulebook" to listOf(
            PitchEntry("Standard", PitchType.STANDARD, true),
        ),
        "Spike Magazine 14 (Norse)" to listOf(
            PitchEntry("Frozen Lake", PitchType.FROZEN_LAKE, false),
        ),
        "Spike Magazine 15 (Amazons)" to listOf(
            PitchEntry("Overgrown Jungle", PitchType.OVERGROWN_JUNGLE, false),
        )
    )

    val stadia = listOf(
        "Death Zone" to listOf(
            StadiumEntry("Disabled", NoStadium, true),
            StadiumEntry("Enabled", RollForStadiumUsed, false),
        ),
        "Unusual Playing Surface" to listOf(
            StadiumEntry("Ankle-Deep Water", SpecificStadium(StadiumType.ANKLE_DEEP_WATER), false),
            StadiumEntry("Sloping Pitch", SpecificStadium(StadiumType.SLOPING_PITCH), false),
            StadiumEntry("Ice", SpecificStadium(StadiumType.ICE), false),
            StadiumEntry("Astrogranite", SpecificStadium(StadiumType.ASTROGRANITE), false),
            StadiumEntry("Uneven Footing", SpecificStadium(StadiumType.UNEVEN_FOOTING), false),
            StadiumEntry("Solid Stone", SpecificStadium(StadiumType.SOLID_STONE), false),
        ),
    )

    init {
        setGameName("Game-${Random.nextInt(10_000)}")
        setPort(8080.toString())
        menuViewModel.navigatorContext.launch {
            PROPERTIES_MANAGER.getString(PROP_DEFAULT_COACH_NAME)?.let {
                updateCoachName(it)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun getCoach(): Coach? {
        val name = coachName.value
        return if (name.isNotBlank()) {
            Coach(CoachId(Uuid.random().toString()), name)
        } else {
            null
        }
    }

    fun setPort(port: String) {
        val newPort = port.toIntOrNull()
        this.port.value = newPort
        checkValidSetup()
    }

    private fun getLocalIp(): String {
        return "127.0.0.1"
    }

    private fun getPublicIp(): String {
        TODO()
    }

    fun updateCoachName(name: String) {
        coachName.value = name
        checkValidSetup()
    }

    private fun checkValidSetup() {
        var isValid = true
        isValid = isValid && gameName.value.isNotBlank()
        isValid = isValid && coachName.value.isNotBlank()
        isValid = isValid && (port.value.let {it != null && it in 1..65535 })
        isSetupValid.value = isValid
    }

    fun setGameName(gameName: String) {
        this.gameName.value = gameName
        checkValidSetup()
    }

    fun setWeatherTable(entry: WeatherTableEntry) {
        selectedWeatherTable.value = entry
        checkValidSetup()
    }

    fun setKickOffTable(entry: KickOffTableEntry) {
        selectedKickOffTable.value = entry
        checkValidSetup()
    }

    fun setUnusualBall(entry: UnusualBallEntry) {
        selectedUnusualBall.value = entry
        checkValidSetup()
    }

    fun setPitch(entry: PitchEntry) {
        selectedPitch.value = entry
        checkValidSetup()
    }

    fun gameSetupDone() {
        menuViewModel.navigatorContext.launch {
            PROPERTIES_MANAGER.setProperty(PROP_DEFAULT_COACH_NAME, coachName.value)
        }
        parentModel.gameSetupDone()
    }
}
