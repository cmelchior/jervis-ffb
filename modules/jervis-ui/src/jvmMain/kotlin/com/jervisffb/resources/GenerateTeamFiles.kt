package com.jervisffb.resources

import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.utils.APPLICATION_DIRECTORY
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val args = arrayOf(APPLICATION_DIRECTORY)
    if (args.isEmpty() || args.size > 1) {
        println("Usage: ./gradlew jvmRun -DmainClass=com.jervisffb.resources.GenerateTeamFilesKt <fullPathToCacheDir>")
        exitProcess(1)
    } else {
        GenerateTeamFiles().generate(args.single())
    }
}

class GenerateTeamFiles {
    private val json = Json {
        prettyPrint = true
        useArrayPolymorphism = true
        serializersModule = SerializersModule {
            include(JervisSerialization.jervisEngineModule)
        }
    }

    fun generate(cacheRoot: String) {
        val root = File(cacheRoot)
        if (!root.exists()) {
            root.mkdirs()
        }
        val rosterCache = File(root, "teams")
        rosterCache.mkdirs()

        (StandaloneStandardTeams.defaultTeams + StandaloneBB7Teams.defaultTeams).forEach { (fileName, roster) ->
            val json = json.encodeToString(roster)
            val file = File(rosterCache, fileName)
            if (file.exists()) {
                file.delete()
            }
            File(rosterCache, fileName).writeText(json, charset = Charsets.UTF_8)
        }
    }
}
