package com.jervisffb.resources.bb2025

import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
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

        "black-orc-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = BLACK_ORC_TEAM_BB2025
            team = teamBuilder(rules, BLACK_ORC_TEAM_BB2025) {
                id = TeamId("jervis-black-orc-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Bo1".playerId, "Grubjaw", 1.playerNo, BLACK_ORC_TRAINED_TROLL)
                addPlayer("Bo2".playerId, "Gorblud", 2.playerNo, BLACK_ORC_BLACK_ORC)
                addPlayer("Bo3".playerId, "Ironhide", 3.playerNo, BLACK_ORC_BLACK_ORC)
                addPlayer("Bo4".playerId, "Kragga", 4.playerNo, BLACK_ORC_BLACK_ORC)
                addPlayer("Bo5".playerId, "Morgsnarl", 5.playerNo, BLACK_ORC_BLACK_ORC)
                addPlayer("Bo6".playerId, "Bruzga", 6.playerNo, BLACK_ORC_BLACK_ORC)
                addPlayer("Bo7".playerId, "Skullkrakka", 7.playerNo, BLACK_ORC_BLACK_ORC)
                addPlayer("Bo8".playerId, "Nikkit", 8.playerNo, BLACK_ORC_GOBLIN_BRUISER)
                addPlayer("Bo9".playerId, "Skrag", 9.playerNo, BLACK_ORC_GOBLIN_BRUISER)
                addPlayer("Bo10".playerId, "Gitz", 10.playerNo, BLACK_ORC_GOBLIN_BRUISER)
                addPlayer("Bo11".playerId, "Runtz", 11.playerNo, BLACK_ORC_GOBLIN_BRUISER)
                rerolls = 2
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 955_000
            }
            history = null
        },

        "bretonnian-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = BRETONNIAN_TEAM_BB2025
            team = teamBuilder(rules, BRETONNIAN_TEAM_BB2025) {
                id = TeamId("jervis-bretonnian-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Br1".playerId, "Sir Roland", 1.playerNo, BRETONNIAN_GRAIL_KNIGHT)
                addPlayer("Br2".playerId, "Sir Percival", 2.playerNo, BRETONNIAN_GRAIL_KNIGHT)
                addPlayer("Br3".playerId, "Alaric", 3.playerNo, BRETONNIAN_BRETONNIAN_SQUIRE)
                addPlayer("Br4".playerId, "Bastien", 4.playerNo, BRETONNIAN_BRETONNIAN_SQUIRE)
                addPlayer("Br5".playerId, "Gawain", 5.playerNo, BRETONNIAN_BRETONNIAN_SQUIRE)
                addPlayer("Br6".playerId, "Hector", 6.playerNo, BRETONNIAN_BRETONNIAN_SQUIRE)
                addPlayer("Br7".playerId, "Jean", 7.playerNo, BRETONNIAN_BRETONNIAN_SQUIRE)
                addPlayer("Br8".playerId, "Luc", 8.playerNo, BRETONNIAN_BRETONNIAN_SQUIRE)
                addPlayer("Br9".playerId, "Olivier", 9.playerNo, BRETONNIAN_BRETONNIAN_SQUIRE)
                addPlayer("Br10".playerId, "Remy", 10.playerNo, BRETONNIAN_BRETONNIAN_SQUIRE)
                addPlayer("Br11".playerId, "Thibault", 11.playerNo, BRETONNIAN_BRETONNIAN_SQUIRE)
                rerolls = 2
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 760_000
            }
            history = null
        },

        "chaos-chosen-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = CHAOS_CHOSEN_TEAM_BB2025
            team = teamBuilder(rules, CHAOS_CHOSEN_TEAM_BB2025) {
                id = TeamId("jervis-chaos-chosen-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Cc1".playerId, "Murkfang", 1.playerNo, CHAOS_CHOSEN_CHAOS_TROLL)
                addPlayer("Cc2".playerId, "Khaargos", 2.playerNo, CHAOS_CHOSEN_CHAOS_CHOSEN)
                addPlayer("Cc3".playerId, "Vorgath", 3.playerNo, CHAOS_CHOSEN_CHAOS_CHOSEN)
                addPlayer("Cc4".playerId, "Skarr", 4.playerNo, CHAOS_CHOSEN_CHAOS_CHOSEN)
                addPlayer("Cc5".playerId, "Rhazgor", 5.playerNo, CHAOS_CHOSEN_CHAOS_CHOSEN)
                addPlayer("Cc6".playerId, "Gorvax", 6.playerNo, CHAOS_CHOSEN_BEASTMAN_RUNNER_LINEMAN)
                addPlayer("Cc7".playerId, "Maalok", 7.playerNo, CHAOS_CHOSEN_BEASTMAN_RUNNER_LINEMAN)
                addPlayer("Cc8".playerId, "Brakk", 8.playerNo, CHAOS_CHOSEN_BEASTMAN_RUNNER_LINEMAN)
                addPlayer("Cc9".playerId, "Tharg", 9.playerNo, CHAOS_CHOSEN_BEASTMAN_RUNNER_LINEMAN)
                addPlayer("Cc10".playerId, "Urzak", 10.playerNo, CHAOS_CHOSEN_BEASTMAN_RUNNER_LINEMAN)
                addPlayer("Cc11".playerId, "Vorg", 11.playerNo, CHAOS_CHOSEN_BEASTMAN_RUNNER_LINEMAN)
                rerolls = 3
                apothecaries = 0
                dedicatedFans = 1
                teamValue = 995_000
            }
            history = null
        },

        "chaos-renegade-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = CHAOS_RENEGADE_TEAM_BB2025
            team = teamBuilder(rules, CHAOS_RENEGADE_TEAM_BB2025) {
                id = TeamId("jervis-chaos-renegade-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Cr1".playerId, "Grimjaw", 1.playerNo, CHAOS_RENEGADE_TROLL)
                addPlayer("Cr2".playerId, "Brutok", 2.playerNo, CHAOS_RENEGADE_OGRE)
                addPlayer("Cr3".playerId, "Shadeclaw", 3.playerNo, CHAOS_RENEGADE_RENEGADE_DARK_ELF)
                addPlayer("Cr4".playerId, "Gorgrub", 4.playerNo, CHAOS_RENEGADE_RENEGADE_ORC)
                addPlayer("Cr5".playerId, "Harrik", 5.playerNo, CHAOS_RENEGADE_RENEGADE_HUMAN)
                addPlayer("Cr6".playerId, "Mordek", 6.playerNo, CHAOS_RENEGADE_RENEGADE_HUMAN)
                addPlayer("Cr7".playerId, "Rask", 7.playerNo, CHAOS_RENEGADE_RENEGADE_HUMAN)
                addPlayer("Cr8".playerId, "Tavik", 8.playerNo, CHAOS_RENEGADE_RENEGADE_HUMAN)
                addPlayer("Cr9".playerId, "Ulric", 9.playerNo, CHAOS_RENEGADE_RENEGADE_HUMAN)
                addPlayer("Cr10".playerId, "Varek", 10.playerNo, CHAOS_RENEGADE_RENEGADE_HUMAN)
                addPlayer("Cr11".playerId, "Zarek", 11.playerNo, CHAOS_RENEGADE_RENEGADE_HUMAN)
                addPlayer("Cr12".playerId, "Sneek", 12.playerNo, CHAOS_RENEGADE_RENEGADE_GOBLIN)
                rerolls = 2
                apothecaries = 1
                dedicatedFans = 2
                teamValue = 950_000
            }
            history = null
        },

        "dark-elf-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = DARK_ELF_TEAM_BB2025
            team = teamBuilder(rules, DARK_ELF_TEAM_BB2025) {
                id = TeamId("jervis-dark-elf-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("De1".playerId, "Maleneth", 1.playerNo, DARK_ELF_DARK_ELF_BLITZER)
                addPlayer("De2".playerId, "Khaine's Fang", 2.playerNo, DARK_ELF_DARK_ELF_BLITZER)
                addPlayer("De3".playerId, "Morathi's Kiss", 3.playerNo, DARK_ELF_WITCH_ELF)
                addPlayer("De4".playerId, "Shadowblade", 4.playerNo, DARK_ELF_WITCH_ELF)
                addPlayer("De5".playerId, "Drazhar", 5.playerNo, DARK_ELF_DARK_ELF_LINEMAN)
                addPlayer("De6".playerId, "Veyl", 6.playerNo, DARK_ELF_DARK_ELF_LINEMAN)
                addPlayer("De7".playerId, "Nythra", 7.playerNo, DARK_ELF_DARK_ELF_LINEMAN)
                addPlayer("De8".playerId, "Syth", 8.playerNo, DARK_ELF_DARK_ELF_LINEMAN)
                addPlayer("De9".playerId, "Kheris", 9.playerNo, DARK_ELF_DARK_ELF_LINEMAN)
                addPlayer("De10".playerId, "Velkyn", 10.playerNo, DARK_ELF_DARK_ELF_LINEMAN)
                addPlayer("De11".playerId, "Z'ress", 11.playerNo, DARK_ELF_DARK_ELF_LINEMAN)
                rerolls = 2
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 985_000
            }
            history = null
        },

        "gnome-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = GNOME_TEAM_BB2025
            team = teamBuilder(rules, GNOME_TEAM_BB2025) {
                id = TeamId("jervis-gnome-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                league = RegionalSpecialRule.HAFLING_THIMBLE_CUP
                addPlayer("Gn1".playerId, "Oakheart", 1.playerNo, GNOME_ALTERN_FOREST_TREEMAN)
                addPlayer("Gn2".playerId, "Bramblebeard", 2.playerNo, GNOME_ALTERN_FOREST_TREEMAN)
                addPlayer("Gn3".playerId, "Quicktail", 3.playerNo, GNOME_WOODLAND_FOX)
                addPlayer("Gn4".playerId, "Nimblepaw", 4.playerNo, GNOME_WOODLAND_FOX)
                addPlayer("Gn5".playerId, "Mirrormoss", 5.playerNo, GNOME_GNOME_ILLUSIONIST)
                addPlayer("Gn6".playerId, "Puck", 6.playerNo, GNOME_GNOME_ILLUSIONIST)
                addPlayer("Gn7".playerId, "Tumbletwig", 7.playerNo, GNOME_GNOME_BEASTMASTER)
                addPlayer("Gn8".playerId, "Fennel", 8.playerNo, GNOME_GNOME_BEASTMASTER)
                addPlayer("Gn9".playerId, "Boggle", 9.playerNo, GNOME_GNOME_LINEMAN)
                addPlayer("Gn10".playerId, "Dindle", 10.playerNo, GNOME_GNOME_LINEMAN)
                addPlayer("Gn11".playerId, "Fizzle", 11.playerNo, GNOME_GNOME_LINEMAN)
                addPlayer("Gn12".playerId, "Midge", 12.playerNo, GNOME_GNOME_LINEMAN)
                addPlayer("Gn13".playerId, "Pipkin", 13.playerNo, GNOME_GNOME_LINEMAN)
                addPlayer("Gn14".playerId, "Wizzle", 14.playerNo, GNOME_GNOME_LINEMAN)
                rerolls = 3
                apothecaries = 1
                dedicatedFans = 2
                teamValue = 990_000
            }
            history = null
        },

        "goblin-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = GOBLIN_TEAM_BB2025
            team = teamBuilder(rules, GOBLIN_TEAM_BB2025) {
                id = TeamId("jervis-goblin-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                league = RegionalSpecialRule.BADLANDS_BRAWL
                addPlayer("Go1".playerId, "Mucka", 1.playerNo, GOBLIN_TRAINED_TROLL)
                addPlayer("Go2".playerId, "Rippa", 2.playerNo, GOBLIN_TRAINED_TROLL)
                addPlayer("Go3".playerId, "Bitz", 3.playerNo, GOBLIN_GOBLIN_LINEMAN)
                addPlayer("Go4".playerId, "Gubbin", 4.playerNo, GOBLIN_GOBLIN_LINEMAN)
                addPlayer("Go5".playerId, "Snagga", 5.playerNo, GOBLIN_GOBLIN_LINEMAN)
                addPlayer("Go6".playerId, "Skrik", 6.playerNo, GOBLIN_GOBLIN_LINEMAN)
                addPlayer("Go7".playerId, "Grub", 7.playerNo, GOBLIN_GOBLIN_LINEMAN)
                addPlayer("Go8".playerId, "Nobbin", 8.playerNo, GOBLIN_GOBLIN_LINEMAN)
                addPlayer("Go9".playerId, "Wort", 9.playerNo, GOBLIN_GOBLIN_LINEMAN)
                addPlayer("Go10".playerId, "Zog", 10.playerNo, GOBLIN_GOBLIN_LINEMAN)
                addPlayer("Go11".playerId, "Fungus", 11.playerNo, GOBLIN_GOBLIN_LINEMAN)
                addPlayer("Go12".playerId, "Krank", 12.playerNo, GOBLIN_OOLIGAN)
                addPlayer("Go13".playerId, "Spinna", 13.playerNo, GOBLIN_FANATIC)
                addPlayer("Go14".playerId, "Dooma", 14.playerNo, GOBLIN_DOOM_DIVER)
                addPlayer("Go15".playerId, "Buzzsaw", 15.playerNo, GOBLIN_LOONEY)
                rerolls = 2
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 945_000
            }
            history = null
        },

        "halfling-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = HALFLING_TEAM_BB2025
            team = teamBuilder(rules, HALFLING_TEAM_BB2025) {
                id = TeamId("jervis-halfling-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                league = RegionalSpecialRule.HAFLING_THIMBLE_CUP
                addPlayer("Ha1".playerId, "Elder Oak", 1.playerNo, HALFLING_ALTERN_FOREST_TREEMAN)
                addPlayer("Ha2".playerId, "Old Root", 2.playerNo, HALFLING_ALTERN_FOREST_TREEMAN)
                addPlayer("Ha3".playerId, "Bumble", 3.playerNo, HALFLING_HALFLING_HOPEFUL)
                addPlayer("Ha4".playerId, "Crumpet", 4.playerNo, HALFLING_HALFLING_HOPEFUL)
                addPlayer("Ha5".playerId, "Dimple", 5.playerNo, HALFLING_HALFLING_HOPEFUL)
                addPlayer("Ha6".playerId, "Pip", 6.playerNo, HALFLING_HALFLING_HOPEFUL)
                addPlayer("Ha7".playerId, "Merry", 7.playerNo, HALFLING_HALFLING_HOPEFUL)
                addPlayer("Ha8".playerId, "Tuck", 8.playerNo, HALFLING_HALFLING_HOPEFUL)
                addPlayer("Ha9".playerId, "Nibs", 9.playerNo, HALFLING_HALFLING_HOPEFUL)
                addPlayer("Ha10".playerId, "Wally", 10.playerNo, HALFLING_HALFLING_HOPEFUL)
                addPlayer("Ha11".playerId, "Boffo", 11.playerNo, HALFLING_HALFLING_HOPEFUL)
                addPlayer("Ha12".playerId, "Minto", 12.playerNo, HALFLING_HALFLING_HOPEFUL)
                rerolls = 1
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 600_000
            }
            history = null
        },

        "imperial-nobility-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = IMPERIAL_NOBILITY_TEAM_BB2025
            team = teamBuilder(rules, IMPERIAL_NOBILITY_TEAM_BB2025) {
                id = TeamId("jervis-imperial-nobility-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("In1".playerId, "Balthasar von Draken", 1.playerNo, IMPERIAL_NOBILITY_OGRE)
                addPlayer("In2".playerId, "Lord Adelbert", 2.playerNo, IMPERIAL_NOBILITY_NOBLE_BLITZER)
                addPlayer("In3".playerId, "Lady Elspeth", 3.playerNo, IMPERIAL_NOBILITY_NOBLE_BLITZER)
                addPlayer("In4".playerId, "Sir Conrad", 4.playerNo, IMPERIAL_NOBILITY_BODYGUARD)
                addPlayer("In5".playerId, "Sir Roderick", 5.playerNo, IMPERIAL_NOBILITY_BODYGUARD)
                addPlayer("In6".playerId, "Dame Beatrix", 6.playerNo, IMPERIAL_NOBILITY_BODYGUARD)
                addPlayer("In7".playerId, "Dame Margarethe", 7.playerNo, IMPERIAL_NOBILITY_BODYGUARD)
                addPlayer("In8".playerId, "Edmund", 8.playerNo, IMPERIAL_NOBILITY_IMPERIAL_RETAINER)
                addPlayer("In9".playerId, "Falk", 9.playerNo, IMPERIAL_NOBILITY_IMPERIAL_RETAINER)
                addPlayer("In10".playerId, "Gustav", 10.playerNo, IMPERIAL_NOBILITY_IMPERIAL_RETAINER)
                addPlayer("In11".playerId, "Hugo", 11.playerNo, IMPERIAL_NOBILITY_IMPERIAL_RETAINER)
                rerolls = 2
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 960_000
            }
            history = null
        },

        "necromantic-horror-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = NECROMANTIC_HORROR_TEAM_BB2025
            team = teamBuilder(rules, NECROMANTIC_HORROR_TEAM_BB2025) {
                id = TeamId("jervis-necromantic-horror-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Ne1".playerId, "Lupus", 1.playerNo, NECROMANTIC_HORROR_WEREWOLF)
                addPlayer("Ne2".playerId, "Mordred", 2.playerNo, NECROMANTIC_HORROR_GHOUL_RUNNER)
                addPlayer("Ne3".playerId, "Vesper", 3.playerNo, NECROMANTIC_HORROR_GHOUL_RUNNER)
                addPlayer("Ne4".playerId, "Gristle", 4.playerNo, NECROMANTIC_HORROR_FLESH_GOLEM)
                addPlayer("Ne5".playerId, "Mortis", 5.playerNo, NECROMANTIC_HORROR_FLESH_GOLEM)
                addPlayer("Ne6".playerId, "Wraithbane", 6.playerNo, NECROMANTIC_HORROR_WRAITH)
                addPlayer("Ne7".playerId, "Shroud", 7.playerNo, NECROMANTIC_HORROR_WRAITH)
                addPlayer("Ne8".playerId, "Boris", 8.playerNo, NECROMANTIC_HORROR_ZOMBIE_LINEMAN)
                addPlayer("Ne9".playerId, "Igor", 9.playerNo, NECROMANTIC_HORROR_ZOMBIE_LINEMAN)
                addPlayer("Ne10".playerId, "Nox", 10.playerNo, NECROMANTIC_HORROR_ZOMBIE_LINEMAN)
                addPlayer("Ne11".playerId, "Rotter", 11.playerNo, NECROMANTIC_HORROR_ZOMBIE_LINEMAN)
                rerolls = 2
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 960_000
            }
            history = null
        },

        "norse-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = NORSE_TEAM_BB2025
            team = teamBuilder(rules, NORSE_TEAM_BB2025) {
                id = TeamId("jervis-norse-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                league = RegionalSpecialRule.OLD_WORLD_CLASSIC
                addPlayer("No1".playerId, "Hrolf", 1.playerNo, NORSE_ULFWERENER)
                addPlayer("No2".playerId, "Einar", 2.playerNo, NORSE_ULFWERENER)
                addPlayer("No3".playerId, "Sigrun", 3.playerNo, NORSE_VALKYRIE)
                addPlayer("No4".playerId, "Astrid", 4.playerNo, NORSE_VALKYRIE)
                addPlayer("No5".playerId, "Bjorni", 5.playerNo, NORSE_BEER_BOAR)
                addPlayer("No6".playerId, "Skjold", 6.playerNo, NORSE_NORSE_RAIDER)
                addPlayer("No7".playerId, "Ragnar", 7.playerNo, NORSE_NORSE_RAIDER)
                addPlayer("No8".playerId, "Leif", 8.playerNo, NORSE_NORSE_RAIDER)
                addPlayer("No9".playerId, "Gunnar", 9.playerNo, NORSE_NORSE_RAIDER)
                addPlayer("No10".playerId, "Ivar", 10.playerNo, NORSE_NORSE_RAIDER)
                addPlayer("No11".playerId, "Sten", 11.playerNo, NORSE_NORSE_RAIDER)
                addPlayer("No12".playerId, "Orm", 12.playerNo, NORSE_NORSE_RAIDER)
                rerolls = 3
                apothecaries = 1
                dedicatedFans = 0
                teamValue = 1_000_000
            }
            history = null
        },

        "ogre-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = OGRE_TEAM_BB2025
            team = teamBuilder(rules, OGRE_TEAM_BB2025) {
                id = TeamId("jervis-ogre-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                league = RegionalSpecialRule.BADLANDS_BRAWL
                addPlayer("Og1".playerId, "Boulder", 1.playerNo, OGRE_OGRE_BLOCKER)
                addPlayer("Og2".playerId, "Grond", 2.playerNo, OGRE_OGRE_BLOCKER)
                addPlayer("Og3".playerId, "Mawbreaker", 3.playerNo, OGRE_OGRE_BLOCKER)
                addPlayer("Og4".playerId, "Stoneskull", 4.playerNo, OGRE_OGRE_BLOCKER)
                addPlayer("Og5".playerId, "Thump", 5.playerNo, OGRE_OGRE_BLOCKER)
                addPlayer("Og6".playerId, "Puntmaster", 6.playerNo, OGRE_OGRE_RUNT_PUNTER)
                addPlayer("Og7".playerId, "Nib", 7.playerNo, OGRE_GNOBLAR_LINEMAN)
                addPlayer("Og8".playerId, "Gibble", 8.playerNo, OGRE_GNOBLAR_LINEMAN)
                addPlayer("Og9".playerId, "Skrat", 9.playerNo, OGRE_GNOBLAR_LINEMAN)
                addPlayer("Og10".playerId, "Munch", 10.playerNo, OGRE_GNOBLAR_LINEMAN)
                addPlayer("Og11".playerId, "Pipik", 11.playerNo, OGRE_GNOBLAR_LINEMAN)
                addPlayer("Og12".playerId, "Runt", 12.playerNo, OGRE_GNOBLAR_LINEMAN)
                addPlayer("Og13".playerId, "Titch", 13.playerNo, OGRE_GNOBLAR_LINEMAN)
                addPlayer("Og14".playerId, "Wobble", 14.playerNo, OGRE_GNOBLAR_LINEMAN)
                rerolls = 0
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 965_000
            }
            history = null
        },

        "old-world-alliance-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = OLD_WORLD_ALLIANCE_TEAM_BB2025
            team = teamBuilder(rules, OLD_WORLD_ALLIANCE_TEAM_BB2025) {
                id = TeamId("jervis-old-world-alliance-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Owa1".playerId, "Brutus von Krug", 1.playerNo, OLD_WORLD_ALLIANCE_OGRE)
                addPlayer("Owa2".playerId, "Dietrich Falk", 2.playerNo, OLD_WORLD_ALLIANCE_HUMAN_BLITZER)
                addPlayer("Owa3".playerId, "Lukas Pfeiffer", 3.playerNo, OLD_WORLD_ALLIANCE_HUMAN_THROWER)
                addPlayer("Owa4".playerId, "Otto Schnell", 4.playerNo, OLD_WORLD_ALLIANCE_HUMAN_CATCHER)
                addPlayer("Owa5".playerId, "Kazrik Ironmark", 5.playerNo, OLD_WORLD_ALLIANCE_DWARF_BLITZER)
                addPlayer("Owa6".playerId, "Borin", 6.playerNo, OLD_WORLD_ALLIANCE_DWARF_LINEMAN)
                addPlayer("Owa7".playerId, "Durgan", 7.playerNo, OLD_WORLD_ALLIANCE_DWARF_LINEMAN)
                addPlayer("Owa8".playerId, "Rorek", 8.playerNo, OLD_WORLD_ALLIANCE_DWARF_LINEMAN)
                addPlayer("Owa9".playerId, "Thrain Axebreaker", 9.playerNo, OLD_WORLD_ALLIANCE_TROLL_SLAYER)
                addPlayer("Owa10".playerId, "Hans Gruber", 10.playerNo, OLD_WORLD_ALLIANCE_HUMAN_LINEMAN)
                addPlayer("Owa11".playerId, "Pip Underbough", 11.playerNo, OLD_WORLD_ALLIANCE_HALFLING_HOPEFUL)
                rerolls = 2
                apothecaries = 0
                dedicatedFans = 0
                teamValue = 1_000_000
            }
            history = null
        },

        "shambling-undead-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = SHAMBLING_UNDEAD_TEAM_BB2025
            team = teamBuilder(rules, SHAMBLING_UNDEAD_TEAM_BB2025) {
                id = TeamId("jervis-shambling-undead-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Su1".playerId, "Morgul the Ancient", 1.playerNo, SHAMBLING_UNDEAD_MUMMY)
                addPlayer("Su2".playerId, "Khepra the Unbroken", 2.playerNo, SHAMBLING_UNDEAD_MUMMY)
                addPlayer("Su3".playerId, "Wightfang", 3.playerNo, SHAMBLING_UNDEAD_WIGHT_BLITZER)
                addPlayer("Su4".playerId, "Graveblade", 4.playerNo, SHAMBLING_UNDEAD_WIGHT_BLITZER)
                addPlayer("Su5".playerId, "Nekhara", 5.playerNo, SHAMBLING_UNDEAD_GHOUL_RUNNER)
                addPlayer("Su6".playerId, "Carrion", 6.playerNo, SHAMBLING_UNDEAD_GHOUL_RUNNER)
                addPlayer("Su7".playerId, "Rotgrin", 7.playerNo, SHAMBLING_UNDEAD_ZOMBIE_LINEMAN)
                addPlayer("Su8".playerId, "Mouldy", 8.playerNo, SHAMBLING_UNDEAD_ZOMBIE_LINEMAN)
                addPlayer("Su9".playerId, "Stitch", 9.playerNo, SHAMBLING_UNDEAD_ZOMBIE_LINEMAN)
                addPlayer("Su10".playerId, "Graveclaw", 10.playerNo, SHAMBLING_UNDEAD_ZOMBIE_LINEMAN)
                addPlayer("Su11".playerId, "Bones", 11.playerNo, SHAMBLING_UNDEAD_ZOMBIE_LINEMAN)
                rerolls = 3
                apothecaries = 0
                dedicatedFans = 0
                teamValue = 1_000_000
            }
            history = null
        },

        "snotling-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = SNOTLING_TEAM_BB2025
            team = teamBuilder(rules, SNOTLING_TEAM_BB2025) {
                id = TeamId("jervis-snotling-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Sn1".playerId, "Muckspore", 1.playerNo, SNOTLING_SNOTLING_LINEMAN)
                addPlayer("Sn2".playerId, "Pipstink", 2.playerNo, SNOTLING_SNOTLING_LINEMAN)
                addPlayer("Sn3".playerId, "Grotgut", 3.playerNo, SNOTLING_SNOTLING_LINEMAN)
                addPlayer("Sn4".playerId, "Nobnob", 4.playerNo, SNOTLING_SNOTLING_LINEMAN)
                addPlayer("Sn5".playerId, "Wobble", 5.playerNo, SNOTLING_SNOTLING_LINEMAN)
                addPlayer("Sn6".playerId, "Skribble", 6.playerNo, SNOTLING_SNOTLING_LINEMAN)
                addPlayer("Sn7".playerId, "Fungus", 7.playerNo, SNOTLING_SNOTLING_LINEMAN)
                addPlayer("Sn8".playerId, "Sporefling", 8.playerNo, SNOTLING_FUNGUS_FLINGA)
                addPlayer("Sn9".playerId, "Mushrump", 9.playerNo, SNOTLING_FUNGUS_FLINGA)
                addPlayer("Sn10".playerId, "Stilty", 10.playerNo, SNOTLING_STILTY_RUNNA)
                addPlayer("Sn11".playerId, "Wagonwheel", 11.playerNo, SNOTLING_PUMP_WAGON)
                addPlayer("Sn12".playerId, "Rattletrap", 12.playerNo, SNOTLING_PUMP_WAGON)
                addPlayer("Sn13".playerId, "Grubgrin", 13.playerNo, SNOTLING_TRAINED_TROLL)
                addPlayer("Sn14".playerId, "Mossmaw", 14.playerNo, SNOTLING_TRAINED_TROLL)
                rerolls = 2
                apothecaries = 1
                dedicatedFans = 2
                teamValue = 805_000
            }
            history = null
        },

        "underworld-denizens-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = UNDERWORLD_DENIZENS_TEAM_BB2025
            team = teamBuilder(rules, UNDERWORLD_DENIZENS_TEAM_BB2025) {
                id = TeamId("jervis-underworld-denizens-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Uw1".playerId, "Grubgrin", 1.playerNo, UNDERWORLD_DENIZENS_TROLL)
                addPlayer("Uw2".playerId, "Skritch", 2.playerNo, UNDERWORLD_DENIZENS_SKAVEN_BLITZER)
                addPlayer("Uw3".playerId, "Whiskerblade", 3.playerNo, UNDERWORLD_DENIZENS_GUTTER_RUNNER)
                addPlayer("Uw4".playerId, "Throwik", 4.playerNo, UNDERWORLD_DENIZENS_SKAVEN_THROWER)
                addPlayer("Uw5".playerId, "Gnawer", 5.playerNo, UNDERWORLD_DENIZENS_SKAVEN_CLANRAT)
                addPlayer("Uw6".playerId, "Scritch", 6.playerNo, UNDERWORLD_DENIZENS_SKAVEN_CLANRAT)
                addPlayer("Uw7".playerId, "Warpnose", 7.playerNo, UNDERWORLD_DENIZENS_SKAVEN_CLANRAT)
                addPlayer("Uw8".playerId, "Gitz", 8.playerNo, UNDERWORLD_DENIZENS_GOBLIN_LINEMAN)
                addPlayer("Uw9".playerId, "Skrag", 9.playerNo, UNDERWORLD_DENIZENS_GOBLIN_LINEMAN)
                addPlayer("Uw10".playerId, "Nobbin", 10.playerNo, UNDERWORLD_DENIZENS_GOBLIN_LINEMAN)
                addPlayer("Uw11".playerId, "Mucka", 11.playerNo, UNDERWORLD_DENIZENS_GOBLIN_LINEMAN)
                addPlayer("Uw12".playerId, "Pipspore", 12.playerNo, UNDERWORLD_DENIZENS_SNOTLING_LINEMAN)
                addPlayer("Uw13".playerId, "Wart", 13.playerNo, UNDERWORLD_DENIZENS_SNOTLING_LINEMAN)
                addPlayer("Uw14".playerId, "Snot", 14.playerNo, UNDERWORLD_DENIZENS_SNOTLING_LINEMAN)
                rerolls = 3
                apothecaries = 1
                dedicatedFans = 2
                teamValue = 985_000
            }
            history = null
        },

        "vampire-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = VAMPIRE_TEAM_BB2025
            team = teamBuilder(rules, VAMPIRE_TEAM_BB2025) {
                id = TeamId("jervis-vampire-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                addPlayer("Va1".playerId, "Lord Carmine", 1.playerNo, VAMPIRE_VAMPIRE_THROWER)
                addPlayer("Va2".playerId, "Countess Nocturna", 2.playerNo, VAMPIRE_VAMPIRE_THROWER)
                addPlayer("Va3".playerId, "Velkan", 3.playerNo, VAMPIRE_VAMPIRE_RUNNER)
                addPlayer("Va4".playerId, "Seraphine", 4.playerNo, VAMPIRE_VAMPIRE_RUNNER)
                addPlayer("Va5".playerId, "Vladis", 5.playerNo, VAMPIRE_VAMPIRE_BLITZER)
                addPlayer("Va6".playerId, "Harker", 6.playerNo, VAMPIRE_THRALL_LINEMAN)
                addPlayer("Va7".playerId, "Renfield", 7.playerNo, VAMPIRE_THRALL_LINEMAN)
                addPlayer("Va8".playerId, "Mina", 8.playerNo, VAMPIRE_THRALL_LINEMAN)
                addPlayer("Va9".playerId, "Lucien", 9.playerNo, VAMPIRE_THRALL_LINEMAN)
                addPlayer("Va10".playerId, "Elizabeta", 10.playerNo, VAMPIRE_THRALL_LINEMAN)
                addPlayer("Va11".playerId, "Rook", 11.playerNo, VAMPIRE_THRALL_LINEMAN)
                addPlayer("Va12".playerId, "Sanguinus", 12.playerNo, VAMPIRE_THRALL_LINEMAN)
                rerolls = 3
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 990_000
            }
            history = null
        },

        "wood-elf-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = WOOD_ELF_TEAM_BB2025
            team = teamBuilder(rules, WOOD_ELF_TEAM_BB2025) {
                id = TeamId("jervis-wood-elf-starter-team-bb2025")
                name = "FUMBBL Starter #1"
                league = RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE
                addPlayer("We1".playerId, "Orion Swiftbow", 1.playerNo, WOOD_ELF_WARDANCER)
                addPlayer("We2".playerId, "Talarion Leafblade", 2.playerNo, WOOD_ELF_WARDANCER)
                addPlayer("We3".playerId, "Ariel Windstep", 3.playerNo, WOOD_ELF_WOOD_ELF_CATCHER)
                addPlayer("We4".playerId, "Lethiel Moonrunner", 4.playerNo, WOOD_ELF_WOOD_ELF_CATCHER)
                addPlayer("We5".playerId, "Faelar", 5.playerNo, WOOD_ELF_WOOD_ELF_LINEMAN)
                addPlayer("We6".playerId, "Thalion", 6.playerNo, WOOD_ELF_WOOD_ELF_LINEMAN)
                addPlayer("We7".playerId, "Eryn", 7.playerNo, WOOD_ELF_WOOD_ELF_LINEMAN)
                addPlayer("We8".playerId, "Silvan", 8.playerNo, WOOD_ELF_WOOD_ELF_LINEMAN)
                addPlayer("We9".playerId, "Caelynn", 9.playerNo, WOOD_ELF_WOOD_ELF_LINEMAN)
                addPlayer("We10".playerId, "Fenril", 10.playerNo, WOOD_ELF_WOOD_ELF_LINEMAN)
                addPlayer("We11".playerId, "Myrddin", 11.playerNo, WOOD_ELF_WOOD_ELF_LINEMAN)
                rerolls = 2
                apothecaries = 0
                dedicatedFans = 1
                teamValue = 995_000
            }
            history = null
        },
    )

}
