package com.jervisffb.ui

enum class SoundEffect(val fileName: String, val lengthMs: Int) {
    BLOCK("block.ogg", 382),
    BLUNDER("blunder.ogg", 448),
    BOUNCE("bounce44.wav", 140),
    CATCH("catch44.wav", 147),
    CHAINSAW("chainsaw.ogg", 1402),
    CLICK("click44.wav", 116),
    DING("ding.ogg", 482),
    DODGE("dodge.ogg", 425),
    DUH("duh.ogg", 903),
    EW("ew.ogg", 1080),
    EXPLODE("explode.ogg", 721),
    FALL("fall.ogg", 381),
    FIREBALL("fireball.ogg", 730),
    FOUL("foul.ogg", 576),
    HYPNO("hypno.ogg", 1000),
    INJURY("injury.ogg", 715),
    KICK("kick.ogg", 509),
    KO("ko.ogg", 393),
    ZAP("zap.ogg", 1013),
    METAL("metal.ogg", 301),
    NOMNOM("nomnom.ogg", 1031),
    ORGAN("organ.ogg", 3404),
    PICKUP("pickup.ogg", 296),
    QUESTION("question.ogg", 391),
    RIP("rip.ogg", 621),
    ROAR("roar.ogg", 991),
    ROOT("root.ogg", 966),
    SLURP("slurp.ogg", 553),
    STAB("stab.ogg", 280),
    STEP("step44.wav", 118),
    SWOOP("swoop.ogg", 501),
    TOUCHDOWN("td.ogg", 4524),
    THROW("throw44.wav", 160),
    WHISTLE("whistle.ogg", 455),
    WOOOAAAH("woooaaah.ogg", 1929),
    PUMPCROWD("pumpcrowd.ogg", 1000),
    TRAPDOOR("trapdoor.ogg", 1000),
    VOMIT("vomit.ogg", 1000),
    YOINK("yoink.ogg", 500),
    ERROR("bounce44.wav", 140),
}

/**
 * Global object that is responsible for playing game sounds.
 *
 * **Developer's Commentary: **
 * For now, sounds are played in the background, which means they are decoupled
 * from the UI flow once started. It isn't clear if this is wise, or we need
 * a way to interrupt current sounds. Something to keep an eye out for.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object SoundManager {
    /**
     * Preload all sound effects. Must be called before any sounds are used.
     */
    suspend fun initialize()

    /**
     * Play a designated sound effect.
     */
    fun play(sound: SoundEffect)
}
