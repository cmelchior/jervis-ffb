package com.jervisffb.resources

import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.resources.bb2020.BB2020StandaloneRosters
import com.jervisffb.resources.bb2025.BB2025StandaloneRosters
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val args = arrayOf("/Users/christian.melchior/.jervis")
    if (args.isEmpty() || args.size > 1) {
        println("Usage: ./gradlew jvmRun -DmainClass=com.jervisffb.resources.GenerateRosterFilesKt <fullPathToCacheDir>")
        exitProcess(1)
    } else {
        GenerateRosterFiles().generate(args.single())
    }
}

class GenerateRosterFiles {
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
        val rosterCache = File(root, "rosters")
        rosterCache.mkdirs()

        BB2020StandaloneRosters.defaultRosters.forEach { (fileName, roster) ->
            val json = json.encodeToString(roster)
            File(rosterCache, fileName).writeText(json, charset = Charsets.UTF_8)
        }

        BB2025StandaloneRosters.defaultRosters.forEach { (fileName, roster) ->
            val json = json.encodeToString(roster)
            File(rosterCache, fileName).writeText(json, charset = Charsets.UTF_8)
        }
    }
}
