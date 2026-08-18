package com.jervisffb.resources.bb2025

import com.jervisffb.engine.bb2025.BB72025Rules
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.serialization.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialization.JervisMetaData
import com.jervisffb.engine.serialization.buildTeamFile
import com.jervisffb.engine.teamBuilder

// The List of default starter team rosters for BB7.
// This is primarily used by Standalone Mode
object BB2025StandaloneBB7Teams {
    private val rules = BB72025Rules()
    val defaultTeams = mapOf(
        "amazon-bb7-boneheader-starter1-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = AMAZON_TEAM_BB2025
            team = teamBuilder(rules, AMAZON_TEAM_BB2025) {
                name = "Bonehead Starter #1"
                type = GameType.BB7
                addPlayer("Am1-bb7-1".playerId, "Citlali", 1.playerNo, AMAZON_BLITZER)
                addPlayer("Am1-bb7-2".playerId, "Yaotl", 2.playerNo, AMAZON_BLITZER)
                addPlayer("Am1-bb7-3".playerId, "Xochitl", 3.playerNo, AMAZON_BLOCKER)
                addPlayer("Am1-bb7-4".playerId, "Itzel", 4.playerNo, AMAZON_BLOCKER)
                addPlayer("Am1-bb7-5".playerId, "Teyolia", 5.playerNo, AMAZON_LINEMAN)
                addPlayer("Am1-bb7-6".playerId, "Metztli", 6.playerNo, AMAZON_LINEMAN)
                addPlayer("Am1-bb7-7".playerId, "Tonalli", 7.playerNo, AMAZON_LINEMAN)
                addPlayer("Am1-bb7-8".playerId, "Cozamalotl", 8.playerNo, AMAZON_LINEMAN)
                rerolls = 0
                apothecaries = 0
                dedicatedFans = 0
                teamValue = 600_000
            }
            history = null
        },

        "amazon-bb7-boneheader-starter2-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = AMAZON_TEAM_BB2025
            team = teamBuilder(rules, AMAZON_TEAM_BB2025) {
                name = "Bonehead Starter #2"
                type = GameType.BB7
                addPlayer("Am2-bb7-1".playerId, "Citlali", 1.playerNo, AMAZON_THROWER)
                addPlayer("Am2-bb7-2".playerId, "Xochitl", 2.playerNo, AMAZON_BLOCKER)
                addPlayer("Am2-bb7-3".playerId, "Itzel", 3.playerNo, AMAZON_BLOCKER)
                addPlayer("Am2-bb7-4".playerId, "Teyolia", 4.playerNo, AMAZON_LINEMAN)
                addPlayer("Am2-bb7-5".playerId, "Metztli", 5.playerNo, AMAZON_LINEMAN)
                addPlayer("Am2-bb7-6".playerId, "Tonalli", 6.playerNo, AMAZON_LINEMAN)
                addPlayer("Am2-bb7-7".playerId, "Cozamalotl", 7.playerNo, AMAZON_LINEMAN)
                rerolls = 1
                apothecaries = 0
                dedicatedFans = 0
                teamValue = 585_000
            }
            history = null
        },

        "elven-union-bb7-boneheader-starter1-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = ELVEN_UNION_TEAM_BB2025
            team = teamBuilder(rules, ELVEN_UNION_TEAM_BB2025) {
                name = "Bonehead Starter #1"
                type = GameType.BB7
                addPlayer("E11-bb7-1".playerId, "Elandril Surehand", 1.playerNo, ELVEN_THROWER)
                addPlayer("El1-bb7-2".playerId, "Caelir Quickblade", 2.playerNo, ELVEN_BLITZER)
                addPlayer("El1-bb7-3".playerId, "Vaelith Dawnstep", 3.playerNo, ELVEN_BLITZER)
                addPlayer("El1-bb7-4".playerId, "Lirael Starwind", 4.playerNo, ELVEN_CATCHER)
                addPlayer("El1-bb7-5".playerId, "Thalion Brightleaf", 5.playerNo, ELVEN_LINEMAN)
                addPlayer("El1-bb7-6".playerId, "Eryndor Silverbrook", 6.playerNo, ELVEN_LINEMAN)
                addPlayer("El1-bb7-7".playerId, "Sylvaris Mooncrest", 7.playerNo, ELVEN_LINEMAN)
                rerolls = 0
                apothecaries = 0
                dedicatedFans = 0
                teamValue = 600_000
            }
            history = null
        },

        "elven-union-bb7-boneheader-starter2-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = ELVEN_UNION_TEAM_BB2025
            team = teamBuilder(rules, ELVEN_UNION_TEAM_BB2025) {
                name = "Bonehead Starter #2"
                type = GameType.BB7
                addPlayer("El2-bb7-1".playerId, "Aerendyl Swiftblade", 1.playerNo, ELVEN_BLITZER)
                addPlayer("El2-bb7-2".playerId, "Finrael Goldbough", 2.playerNo, ELVEN_LINEMAN)
                addPlayer("El2-bb7-3".playerId, "Caladrel Evenstar", 3.playerNo, ELVEN_LINEMAN)
                addPlayer("El2-bb7-4".playerId, "Ilyrana Sunweaver", 4.playerNo, ELVEN_LINEMAN)
                addPlayer("El2-bb7-5".playerId, "Maethor Greenmantle", 5.playerNo, ELVEN_LINEMAN)
                addPlayer("El2-bb7-6".playerId, "Nimriel Lightfoot", 6.playerNo, ELVEN_LINEMAN)
                addPlayer("El2-bb7-7".playerId, "Talandris Silverglade", 7.playerNo, ELVEN_LINEMAN)
                rerolls = 1
                apothecaries = 0
                dedicatedFans = 0
                teamValue = 590_000
            }
            history = null
        },

        "chaos-dwarf-bb7-boneheader-starter1-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = CHAOS_DWARF_TEAM_BB2025
            team = teamBuilder(rules, CHAOS_DWARF_TEAM_BB2025) {
                name = "Bonehead Starter #1"
                type = GameType.BB7
                addPlayer("Cd1-bb7-1".playerId, "Zhorak Brasshoof", 1.playerNo, BULL_CENTAUR_BLITZERS)
                addPlayer("Cd1-bb7-2".playerId, "Drazhak Ashhorn", 2.playerNo, BULL_CENTAUR_BLITZERS)
                addPlayer("Cd1-bb7-3".playerId, "Skabnash", 3.playerNo, HOBGOBLIN_LINEMEN)
                addPlayer("Cd1-bb7-4".playerId, "Gitzik", 4.playerNo, HOBGOBLIN_LINEMEN)
                addPlayer("Cd1-bb7-5".playerId, "Krulg", 5.playerNo, HOBGOBLIN_LINEMEN)
                addPlayer("Cd1-bb7-6".playerId, "Murgit", 6.playerNo, HOBGOBLIN_LINEMEN)
                addPlayer("Cd1-bb7-7".playerId, "Sneek", 7.playerNo, HOBGOBLIN_LINEMEN)
                addPlayer("Cd1-bb7-8".playerId, "Bogrot", 8.playerNo, HOBGOBLIN_LINEMEN)
                rerolls = 1
                apothecaries = 0
                dedicatedFans = 0
                teamValue = 600_000
            }
            history = null
        },

        "chaos-dwarf-bb7-boneheader-starter2-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = CHAOS_DWARF_TEAM_BB2025
            team = teamBuilder(rules, CHAOS_DWARF_TEAM_BB2025) {
                name = "Bonehead Starter #2"
                type = GameType.BB7
                addPlayer("Cd2-bb7-1".playerId, "Rakhul the Chained", 1.playerNo, ENSLAVED_MINOTAUR)
                addPlayer("Cd2-bb7-2".playerId, "Azgorth Emberhoof", 2.playerNo, BULL_CENTAUR_BLITZERS)
                addPlayer("Cd2-bb7-3".playerId, "Snivlak", 3.playerNo, HOBGOBLIN_LINEMEN)
                addPlayer("Cd2-bb7-4".playerId, "Grubnak", 4.playerNo, HOBGOBLIN_LINEMEN)
                addPlayer("Cd2-bb7-5".playerId, "Krazgit", 5.playerNo, HOBGOBLIN_LINEMEN)
                addPlayer("Cd2-bb7-6".playerId, "Morkish", 6.playerNo, HOBGOBLIN_LINEMEN)
                addPlayer("Cd2-bb7-7".playerId, "Zaggit", 7.playerNo, HOBGOBLIN_LINEMEN)
                rerolls = 1
                apothecaries = 0
                dedicatedFans = 0
                teamValue = 580_000
            }
            history = null
        },

        "dwarf-bb7-boneheader-starter1-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = DWARF_TEAM_BB2025
            team = teamBuilder(rules, DWARF_TEAM_BB2025) {
                name = "Bonehead Starter #1"
                type = GameType.BB7
                addPlayer("Dw1-bb7-1".playerId, "Skalf Doomseeker", 1.playerNo, TROLL_SLAYER)
                addPlayer("Dw1-bb7-2".playerId, "Bardin Ironfist", 2.playerNo, DWARF_BLITZER)
                addPlayer("Dw1-bb7-3".playerId, "Kragni Stonehelm", 3.playerNo, DWARF_BLITZER)
                addPlayer("Dw1-bb7-4".playerId, "Dori Swiftfoot", 4.playerNo, DWARF_RUNNER)
                addPlayer("Dw1-bb7-5".playerId, "Brokk Granitebeard", 5.playerNo, DWARF_LINEMAN)
                addPlayer("Dw1-bb7-6".playerId, "Hargin Deepdelver", 6.playerNo, DWARF_LINEMAN)
                addPlayer("Dw1-bb7-7".playerId, "Varrik Steelbrow", 7.playerNo, DWARF_LINEMAN)
                rerolls = 0
                apothecaries = 0
                dedicatedFans = 0
                teamValue = 565_000
            }
            history = null
        },

        "dwarf-bb7-boneheader-starter2-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = DWARF_TEAM_BB2025
            team = teamBuilder(rules, DWARF_TEAM_BB2025) {
                name = "Bonehead Starter #2"
                type = GameType.BB7
                addPlayer("Dw2-bb7-1".playerId, "Skorri Fleetfoot", 1.playerNo, DWARF_RUNNER)
                addPlayer("Dw2-bb7-2".playerId, "Durak Ironhand", 2.playerNo, DWARF_LINEMAN)
                addPlayer("Dw2-bb7-3".playerId, "Hroki Stoneback", 3.playerNo, DWARF_LINEMAN)
                addPlayer("Dw2-bb7-4".playerId, "Beldin Deepforge", 4.playerNo, DWARF_LINEMAN)
                addPlayer("Dw2-bb7-5".playerId, "Torgrim Oathkeeper", 5.playerNo, DWARF_LINEMAN)
                addPlayer("Dw2-bb7-6".playerId, "Korgan Goldvein", 6.playerNo, DWARF_LINEMAN)
                addPlayer("Dw2-bb7-7".playerId, "Fargrim Flintbeard", 7.playerNo, DWARF_LINEMAN)
                rerolls = 1
                apothecaries = 0
                dedicatedFans = 0
                teamValue = 600_000
            }
            history = null
        },

//        "orc-bb7-bonehead-starter1-bb2025.jrt" to buildTeamFile {
//            metadata = JervisMetaData(FILE_FORMAT_VERSION)
//            roster = ORC_TEAM_BB2025
//            team = teamBuilder(rules, ORC_TEAM_BB2025) {
//                name = "Bonehead Starter #1"
//                type = GameType.BB7
//                addPlayer("Orc-bb7-1".playerId, "Grukk da Flash", 1.playerNo, ORC_BLITZER)
//                addPlayer("Orc-bb7-2".playerId, "Basha Ironjaw", 2.playerNo, ORC_BLITZER)
//                addPlayer("Orc-bb7-3".playerId, "Morglug da Wall", 3.playerNo, BIG_UN_BLOCKERS)
//                addPlayer("Orc-bb7-4".playerId, "Zogwort Longarm", 4.playerNo, ORC_THROWER)
//                addPlayer("Orc-bb7-5".playerId, "Snagga", 5.playerNo, ORC_LINEMEN)
//                addPlayer("Orc-bb7-6".playerId, "Rukfang", 6.playerNo, ORC_LINEMEN)
//                addPlayer("Orc-bb7-7".playerId, "Gorbad", 7.playerNo, ORC_LINEMEN)
//                rerolls = 1
//                apothecaries = 0
//                dedicatedFans = 0
//                teamValue = 585_000
//            }
//            history = null
//        },
    )
}
