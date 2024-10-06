plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "Jervis-Fantasy-Football"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

include(":modules:fumbbl-cli")
include(":modules:fumbbl-net")
include(":modules:jervis-engine")
include(":modules:jervis-ui")
include(":Debug-FantasyFootballClient")
include(":modules:replay-analyzer")
include("modules:utils")
