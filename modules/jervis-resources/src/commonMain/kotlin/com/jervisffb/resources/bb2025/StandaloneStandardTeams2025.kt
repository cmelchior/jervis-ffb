package com.jervisffb.resources.bb2025

import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.serialization.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialization.JervisMetaData
import com.jervisffb.engine.serialization.buildTeamFile
import com.jervisffb.engine.teamBuilder

// The list of default starter team rosters.
// This issued by Standalone Mode.
//
// Builds follow the recommended BB2025 starter rosters from FUMBBL:
// https://fumbbl.com/help:BB25RaceStrategy
object StandaloneStandardTeams2025 {
    private val rules = StandardBB2025Rules()
    val defaultTeams = mapOf(
        "amazon-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = AMAZON_TEAM_BB2025
            team = teamBuilder(rules, AMAZON_TEAM_BB2025) {
                id = TeamId("jervis-amazon-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Am1".playerId, "Xochitl", 1.playerNo, AMAZON_BLOCKER)
                addPlayer("Am2".playerId, "Itzel", 2.playerNo, AMAZON_BLOCKER)
                addPlayer("Am3".playerId, "Citlali", 3.playerNo, AMAZON_BLITZER)
                addPlayer("Am4".playerId, "Yaotl", 4.playerNo, AMAZON_BLITZER)
                addPlayer("Am5".playerId, "Teyolia", 5.playerNo, AMAZON_LINEMAN)
                addPlayer("Am6".playerId, "Metztli", 6.playerNo, AMAZON_LINEMAN)
                addPlayer("Am7".playerId, "Tonalli", 7.playerNo, AMAZON_LINEMAN)
                addPlayer("Am8".playerId, "Cozamalotl", 8.playerNo, AMAZON_LINEMAN)
                addPlayer("Am9".playerId, "Yaretzi", 9.playerNo, AMAZON_LINEMAN)
                addPlayer("Am10".playerId, "Chalchi", 10.playerNo, AMAZON_LINEMAN)
                addPlayer("Am11".playerId, "Nenetl", 11.playerNo, AMAZON_LINEMAN)
                rerolls = 3
                apothecaries = 1
                dedicatedFans = 2
                teamValue = 980_000
            }
            history = null
        },

        "human-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = HUMAN_TEAM_BB2025
            team = teamBuilder(rules, HUMAN_TEAM_BB2025) {
                id = TeamId("jervis-human-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Hu1".playerId, "Gustav the Great", 1.playerNo, OGRE)
                addPlayer("Hu2".playerId, "Heinrich von Toth", 2.playerNo, HUMAN_BLITZER)
                addPlayer("Hu3".playerId, "Klaus Gruber", 3.playerNo, HUMAN_BLITZER)
                addPlayer("Hu4".playerId, "Matthias Falk", 4.playerNo, HUMAN_CATCHER)
                addPlayer("Hu5".playerId, "Elsa Schmidt", 5.playerNo, HUMAN_CATCHER)
                addPlayer("Hu6".playerId, "Pip Underbough", 6.playerNo, HALFLING_HOPEFUL)
                addPlayer("Hu7".playerId, "Hans Weber", 7.playerNo, HUMAN_LINEMAN)
                addPlayer("Hu8".playerId, "Ulrich Bauer", 8.playerNo, HUMAN_LINEMAN)
                addPlayer("Hu9".playerId, "Friedrich Kohl", 9.playerNo, HUMAN_LINEMAN)
                addPlayer("Hu10".playerId, "Otto Kruger", 10.playerNo, HUMAN_LINEMAN)
                addPlayer("Hu11".playerId, "Wilhelm Brandt", 11.playerNo, HUMAN_LINEMAN)
                addPlayer("Hu12".playerId, "Lotte Weiss", 12.playerNo, HUMAN_LINEMAN)
                rerolls = 3
                apothecaries = 1
                dedicatedFans = 2
                teamValue = 990_000
            }
            history = null
        },

        "high-elf-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = HIGH_ELF_TEAM_BB2025
            team = teamBuilder(rules, HIGH_ELF_TEAM_BB2025) {
                id = TeamId("jervis-high-elf-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("He1".playerId, "Aethyrion Swiftflame", 1.playerNo, DRAGON_PRINCE)
                addPlayer("He2".playerId, "Caledor Starcrest", 2.playerNo, DRAGON_PRINCE)
                addPlayer("He3".playerId, "Korhil Whiteclaw", 3.playerNo, WHITE_LION)
                addPlayer("He4".playerId, "Caradryan Brightmane", 4.playerNo, WHITE_LION)
                addPlayer("He5".playerId, "Elarion Dawnrunner", 5.playerNo, HIGH_ELF_LINEMAN)
                addPlayer("He6".playerId, "Finubar Goldleaf", 6.playerNo, HIGH_ELF_LINEMAN)
                addPlayer("He7".playerId, "Talandor Silverspear", 7.playerNo, HIGH_ELF_LINEMAN)
                addPlayer("He8".playerId, "Aerandir Moonblade", 8.playerNo, HIGH_ELF_LINEMAN)
                addPlayer("He9".playerId, "Letharion Swiftwind", 9.playerNo, HIGH_ELF_LINEMAN)
                addPlayer("He10".playerId, "Vaelith Starbrow", 10.playerNo, HIGH_ELF_LINEMAN)
                addPlayer("He11".playerId, "Thalion Brightshield", 11.playerNo, HIGH_ELF_LINEMAN)
                rerolls = 2
                apothecaries = 0
                dedicatedFans = 1
                teamValue = 995_000
            }
            history = null
        },

        "lizardmen-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = LIZARDMEN_TEAM_BB2025
            team = teamBuilder(rules, LIZARDMEN_TEAM_BB2025) {
                id = TeamId("jervis-lizardmen-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Li7".playerId, "Kroxigar", 1.playerNo, KROXIGOR)
                addPlayer("Li1".playerId, "Tzunki", 2.playerNo, SAURUS_BLOCKERS)
                addPlayer("Li2".playerId, "Quetzl", 3.playerNo, SAURUS_BLOCKERS)
                addPlayer("Li3".playerId, "Huanchi", 4.playerNo, SAURUS_BLOCKERS)
                addPlayer("Li4".playerId, "Tepok", 5.playerNo, SAURUS_BLOCKERS)
                addPlayer("Li5".playerId, "Chotec", 6.playerNo, SAURUS_BLOCKERS)
                addPlayer("Li6".playerId, "Tlazcotl", 7.playerNo, SAURUS_BLOCKERS)
                addPlayer("Li8".playerId, "Skik", 8.playerNo, SKINK_RUNNER_LINEMEN)
                addPlayer("Li9".playerId, "Tiktaq", 9.playerNo, SKINK_RUNNER_LINEMEN)
                addPlayer("Li10".playerId, "Chakax", 10.playerNo, SKINK_RUNNER_LINEMEN)
                addPlayer("Li11".playerId, "Oxyotl", 11.playerNo, CHAMELEON_SKINKS)
                rerolls = 1
                apothecaries = 0
                dedicatedFans = 1
                teamValue = 1_000_000
            }
            history = null
        },

        "nurgle-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = NURGLE_TEAM_BB2025
            team = teamBuilder(rules, NURGLE_TEAM_BB2025) {
                id = TeamId("jervis-nurgle-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Nu1".playerId, "The Green Reaper", 1.playerNo, ROTSPAWN)
                addPlayer("Nu2".playerId, "Filthius", 2.playerNo, ROTTER_LINEMEN)
                addPlayer("Nu3".playerId, "Poxlicker", 3.playerNo, ROTTER_LINEMEN)
                addPlayer("Nu4".playerId, "Mouldy Joe", 4.playerNo, ROTTER_LINEMEN)
                addPlayer("Nu5".playerId, "Wormtongue", 5.playerNo, ROTTER_LINEMEN)
                addPlayer("Nu6".playerId, "Gristlegrin", 6.playerNo, BLOATERS)
                addPlayer("Nu7".playerId, "Blightgut", 7.playerNo, BLOATERS)
                addPlayer("Nu8".playerId, "Festerface", 8.playerNo, BLOATERS)
                addPlayer("Nu9".playerId, "Slimebeard", 9.playerNo, BLOATERS)
                addPlayer("Nu10".playerId, "Maggotclaw", 10.playerNo, PESTIGORS)
                addPlayer("Nu11".playerId, "Rotbelly", 11.playerNo, PESTIGORS)
                rerolls = 2
                apothecaries = 0
                teamValue = 1_000_000
            }
            history = null
        },

        "skaven-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = SKAVEN_TEAM_BB2025
            team = teamBuilder(rules, SKAVEN_TEAM_BB2025) {
                id = TeamId("jervis-skaven-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Sk1".playerId, "Gnawdoom", 1.playerNo, RAT_OGRE)
                addPlayer("Sk2".playerId, "Slink Sharpclaw", 2.playerNo, SKAVEN_BLITZER)
                addPlayer("Sk3".playerId, "Verminkin", 3.playerNo, SKAVEN_BLITZER)
                addPlayer("Sk4".playerId, "Quickpaw", 4.playerNo, GUTTER_RUNNER)
                addPlayer("Sk5".playerId, "Snitch", 5.playerNo, GUTTER_RUNNER)
                addPlayer("Sk6".playerId, "Throwmaster Skab", 6.playerNo, SKAVEN_THROWER)
                addPlayer("Sk7".playerId, "Nip Nip", 7.playerNo, SKAVEN_LINEMAN)
                addPlayer("Sk8".playerId, "Scratch", 8.playerNo, SKAVEN_LINEMAN)
                addPlayer("Sk9".playerId, "Scurry", 9.playerNo, SKAVEN_LINEMAN)
                addPlayer("Sk10".playerId, "Warpfang", 10.playerNo, SKAVEN_LINEMAN)
                addPlayer("Sk11".playerId, "Stinkeye", 11.playerNo, SKAVEN_LINEMAN)
                rerolls = 3
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 980_000
            }
            history = null
        },

        "khorne-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = KHORNE_TEAM_BB2025
            team = teamBuilder(rules, KHORNE_TEAM_BB2025) {
                id = TeamId("jervis-khorne-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Kh1".playerId, "Gorefang", 1.playerNo, BLOODSPAWN)
                addPlayer("Kh2".playerId, "Karnak", 2.playerNo, BLOODSEEKERS)
                addPlayer("Kh3".playerId, "Skulltaker", 3.playerNo, BLOODSEEKERS)
                addPlayer("Kh4".playerId, "Redmaw", 4.playerNo, BLOODSEEKERS)
                addPlayer("Kh5".playerId, "Vorgaroth", 5.playerNo, BLOODSEEKERS)
                addPlayer("Kh6".playerId, "Khorzak", 6.playerNo, KHORNGORS)
                addPlayer("Kh7".playerId, "Bloodhorn", 7.playerNo, KHORNGORS)
                addPlayer("Kh8".playerId, "Mordrek", 8.playerNo, BLOODBORN_MARAUDER_LINEMEN)
                addPlayer("Kh9".playerId, "Gorehand", 9.playerNo, BLOODBORN_MARAUDER_LINEMEN)
                addPlayer("Kh10".playerId, "Akharn", 10.playerNo, BLOODBORN_MARAUDER_LINEMEN)
                addPlayer("Kh11".playerId, "The Butcher", 11.playerNo, BLOODBORN_MARAUDER_LINEMEN)
                rerolls = 1
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 980_000
            }
            history = null
        },

        "dwarf-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = DWARF_TEAM_BB2025
            team = teamBuilder(rules, DWARF_TEAM_BB2025) {
                id = TeamId("jervis-dwarf-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Dw1".playerId, "Borin Stonefist", 1.playerNo, DWARF_BLITZER)
                addPlayer("Dw2".playerId, "Grimnirsson", 2.playerNo, DWARF_BLITZER)
                addPlayer("Dw3".playerId, "Thorek Quickstep", 3.playerNo, DWARF_RUNNER)
                addPlayer("Dw4".playerId, "Grombrindal", 4.playerNo, TROLL_SLAYER)
                addPlayer("Dw5".playerId, "Durgan Ironbeard", 5.playerNo, DWARF_LINEMAN)
                addPlayer("Dw6".playerId, "Kazrik", 6.playerNo, DWARF_LINEMAN)
                addPlayer("Dw7".playerId, "Ulfar", 7.playerNo, DWARF_LINEMAN)
                addPlayer("Dw8".playerId, "Brokki", 8.playerNo, DWARF_LINEMAN)
                addPlayer("Dw9".playerId, "Hargrim", 9.playerNo, DWARF_LINEMAN)
                addPlayer("Dw10".playerId, "Rorek", 10.playerNo, DWARF_LINEMAN)
                addPlayer("Dw11".playerId, "Grundin", 11.playerNo, DWARF_LINEMAN)
                rerolls = 2
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 985_000
            }
            history = null
        },

        "tomb-kings-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = TOMB_KINGS_TEAM_BB2025
            team = teamBuilder(rules, TOMB_KINGS_TEAM_BB2025) {
                id = TeamId("jervis-tomb-kings-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Tk1".playerId, "Khalida's Guard", 1.playerNo, TOMB_GUARDIANS)
                addPlayer("Tk2".playerId, "Settra's Guard", 2.playerNo, TOMB_GUARDIANS)
                addPlayer("Tk3".playerId, "Nekhesh", 3.playerNo, TOMB_GUARDIANS)
                addPlayer("Tk4".playerId, "Usirian", 4.playerNo, TOMB_GUARDIANS)
                addPlayer("Tk5".playerId, "Ramhotep", 5.playerNo, TOMB_KINGS_BLITZERS)
                addPlayer("Tk6".playerId, "Ramhoptep", 6.playerNo, TOMB_KINGS_BLITZERS)
                addPlayer("Tk7".playerId, "Nasir the Passer", 7.playerNo, TOMB_KINGS_THROWERS)
                addPlayer("Tk8".playerId, "Khepper", 8.playerNo, SKELETON_LINEMEN)
                addPlayer("Tk9".playerId, "Meskhen", 9.playerNo, SKELETON_LINEMEN)
                addPlayer("Tk10".playerId, "Ankhara", 10.playerNo, SKELETON_LINEMEN)
                addPlayer("Tk11".playerId, "Neferkara", 11.playerNo, SKELETON_LINEMEN)
                rerolls = 2
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 975_000
            }
            history = null
        },
    )

}
