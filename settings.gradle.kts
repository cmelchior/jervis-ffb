plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "Jervis-Fantasy-Football"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.hq.hydraulic.software")
        maven {
            url = uri("${rootProject.projectDir}/mavenRepo")
        }
        mavenLocal()
    }
}

include(":Debug-FantasyFootballClient")
include(":modules:fumbbl-cli")
include(":modules:fumbbl-net")
include(":modules:fuzzer-cli")
include(":modules:jervis-engine:core")
include(":modules:jervis-engine:package")
include(":modules:jervis-engine:rules-common")
include(":modules:jervis-engine:rules-bb2020")
include(":modules:jervis-engine:rules-bb2025")
include(":modules:jervis-net")
include(":modules:jervis-resources")
include(":modules:jervis-test-utils")
include(":modules:jervis-ui:desktopApp")
include(":modules:jervis-ui:shared")
include(":modules:jervis-ui:webApp")
include(":modules:platform-utils")
include(":modules:replay-analyzer")
include(":modules:tourplay-net")
include(":tools")

include("modules:jervis-engine:package")
