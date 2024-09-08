# Skills

This document list all skills implemented in Jervis and any notes around their implementation.
As a general rule, skill are implemented faithfully, i.e. they should follow the rules as written.

This also includes choosing whether to use them or not. The rules module will always ask for usage,
but this might be responded to automatically by higher layers before being shown as a dialog to 
players.

Skills marked with * are mandatory.

Skills should only be marked as completed here if they are implemented and have a test class in
`modules/game-model/src/commonTest/kotlin/dk/ilios/jervis/skills`

## Agility Skills

- [ ] Catch
- [ ] Diving Catch
- [ ] Diving Tackle
- [ ] Dodge
- [ ] Defensive
- [ ] Jump Up
- [ ] Leap
- [ ] Safe Pair of Hands
- [ ] Sidestep
- [ ] Sneaky Git
- [ ] Sprint
- [ ] Sure Feet

## General Skills

- [ ] Block
- [ ] Dauntless
- [ ] Dirty Player (+1)
- [ ] Fend
- [ ] Frenzy*
- [ ] Kick
- [ ] Pro
- [ ] Shadowing
- [ ] Strip Ball
- [ ] Sure Hands
- [ ] Tackle
- [ ] Wrestle

## Mutations

- [ ] Big Hand
- [ ] Claws
- [ ] Disturbing Presence*
- [ ] Extra Arms
- [ ] Foul Appearance*
- [ ] Horns
- [ ] Iron Hard Skin
- [ ] Monstrous Mouth
- [ ] Prehensile Tail
- [ ] Tentacles
- [ ] Two Heads
- [ ] Very Long Legs

## Passing Skills

- [ ] Accurate
- [ ] Cannoneer
- [ ] Cloud Burster
- [ ] Dump-off
- [ ] Fumblerooskie
- [ ] Hail Mary Pass
- [ ] Leader
- [ ] Nerves of Steel
- [ ] On the Ball
- [ ] Pass
- [ ] Running Pass
- [ ] Safe Pass

## Strength Skills

- [ ] Arm Bar
- [ ] Brawler
- [x] Break Tackle
- [ ] Grab
- [ ] Guard
- [ ] Juggernaut
- [ ] Might Blow (+1)
- [ ] Multiple Block
- [ ] Pile Driver
- [ ] Stand Firm
- [ ] Strong Arm
- [ ] Thick Skull

## Traits

- [ ] Animal Savagery*
- [ ] Animosity (X)*
- [ ] Always Hungry*
- [ ] Ball & Chain*
- [ ] Bombardier
- [ ] Bone Head*
- [ ] Chainsaw*
- [ ] Decay*
- [ ] Hypnotic Gaze
- [ ] Kick Team-mate
- [ ] Loner (X+)*
- [ ] No Hands*
- [ ] Plague Ridden
- [ ] Pogo Stick
- [ ] Projectile Vomit
- [ ] Really Stupid*
- [ ] Regeneration
- [ ] Right Stuff*
- [ ] Secret Weapon*
- [ ] Stab
- [ ] Stunty*
- [ ] Swarming
- [ ] Swoop
- [ ] Take Root*
- [ ] Titchy*
- [ ] Throw Team-mate
- [ ] Timmm-ber!
- [ ] Unchannelled Fury*
- [ ] Hit and Run (Spike 15)

# Star Player Skills

List of stars is pulled from https://fumbbl.com/p/stars (pr. 30/8/2024) 
and https://bbtactics.com/blood-bowl-star-players-list/ (pr. 30/8/2024)

- [ ] A Sneaky Pair, Dribl & Drull
- [ ] All You Can Eat, Cindy Piewhistle: https://bbtactics.com/all-you-can-eat/
- [ ] Baleful Hex, Estelle La Veneaux
- [ ] Beer Barrel Bash, Thorsson Stoutmead
- [ ] Black Ink, Kiroth Krakeneye
- [ ] Blast It!, Barik Farblast: https://bbtactics.com/blast-it/
- [ ] Blind Rage, Akhorne the Squirrel: https://bbtactics.com/blind-rage/
- [ ] Bounding Leap, Rowana Foresetfoot
- [ ] Brutal Block, Frank ‘n’ Stein
- [ ] Burst of Speed, Roxanna Darknail
- [ ] Catch of the Day, Rodney Roachbait: https://youtu.be/Q95AhhT8MJ4?si=FCgqzgXcPNvZ5tjb&t=235
- [ ] Consummate Professional, Griff Oberwald
- [ ] Crushing Blow, Mighty Zug
- [ ] Crushing Blow, Varag Ghoul-Chewer
- [ ] Dwarven Scourge, Ivan ‘the Animal’ Deathshroud
- [ ] Excuse Me, Are You a Zoat?, Zolcath the Zoat
- [ ] Frenzied Rush, Glart Smashrip
- [ ] Fury of the Blood Good, Scyla Anfingrimm
- [ ] Ghostly Flames, Bryce 'the Slice' Cambuel: https://bbtactics.com/ghostly-flames/
- [ ] Gored by the Bull, Grashnak Blackhoof
- [ ] Halfling Luck, Puggy Baconbreath
- [ ] I'll Be Back, Kreek Rustgouger
- [ ] Incorporeal, Gretchen Wachter “The Blood Bowl Widow”
- [ ] Indomitable, Karla Von Kill
- [ ] Indomitable, Willow Rosebark
- [ ] Kaboom!, Bomber Dribblesnot: https://bbtactics.com/kaboom/
- [ ] Kick ’em While They’re Down!, Nobbla Blackwart
- [ ] Look Into My Eyes, Boa Kon'sstriktr: https://bbtactics.com/look-into-my-eyes/
- [ ] Lord of Chaos, Lord Borak The Despoiler
- [ ] Master Assassin, Skitter Stab-Stab
- [ ] Maximum Carnage, Max Spleenripper
- [ ] Mesmerizing Gaze, Eldril Sidewinder
- [ ] Old Pro, Helmut Wolf
- [ ] Primal Savagery, Glotl Stop
- [ ] Pump Up the Crowd, Skrorg Snowpelt
- [ ] Putrid Regurgitation, Bilerot Vomitflesh: https://bbtactics.com/putrid-regurgitation/
- [ ] Raiding Party, Ivar Eriksson
- [ ] Ram, Rumbelow Sheepskin
- [ ] Reliable, Deeproot Strongbranch: https://bbtactics.com/reliable/
- [ ] Savage Mauling, Wilhelm Chaney
- [ ] Shot to Nothing, Gloriel Summerbloom
- [ ] Slayer, Grim Ironjaw
- [ ] Sneakiest of the Lot, The Black Gobbo
- [ ] Star of the Show, Count Luthor von Drakenborg: https://bbtactics.com/star-of-the-show/
- [ ] Strong Passing Game, Skrull Halfheight
- [ ] Tasty Morsel, 'Captain' Karina von Riesz: https://bbtactics.com/tasty-morsel/
- [ ] The Ballista, Morg ‘n’ Thorg
- [ ] Thinking Man’s Troll, Ripper Bolgrot
- [ ] Treacherous, Hakflem Skuttlespike
- [ ] Two for One, Grak & Crumbleberry, Lucian Swift & Valen Swift: https://bbtactics.com/two-for-one/
- [ ] Watch Out!, Withergrasp Doubledrool
- [ ] Whirling Dervish, Fungus the Loon
- [ ] Wisdom of the White Dwarf, Grombrindal, the White Dwarf
- [ ] Yoink!, Scrappa Sorehead

# Special Skills

This is not really skills, but more like "Special Rules". Unsure exactly how to track these.

- [ ] Bugman's XXXXXX
- [ ] Keen Player
