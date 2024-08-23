plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "Jervis-A-Blood-Bowl-AI"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

include(":modules:fumbbl-cli")
include(":modules:fumbbl-net")
include(":modules:game-model")
include(":Debug-FantasyFootballClient")
include(":modules:replay-analyzer")
include(":modules:game-ui")
include("modules:utils")
