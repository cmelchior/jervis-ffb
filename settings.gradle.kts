plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "Jervis-Fantasy-Football"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven {
            url = uri("${rootProject.projectDir}/mavenRepo")
        }
        mavenLocal()
    }
}

include(":modules:fumbbl-cli")
include(":modules:fumbbl-net")
include(":modules:jervis-engine")
include(":modules:jervis-net")
include(":modules:jervis-ui")
include(":modules:jervis-resources")
include(":modules:jervis-test-utils")
include(":modules:replay-analyzer")
include(":modules:platform-utils")
include(":Debug-FantasyFootballClient")
