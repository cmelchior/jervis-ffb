# Skills - BB2025

This document lists all skills known in the Blood Bowl 2025 Ruleset and whether
they are implemented in Jervis. If a skill was introduced after the BB2025 base
rules, a reference to its source is provided.

Skills marked with * are mandatory.
Skill keywords used in the rulebook are added in ().

As a general rule, skills are implemented faithfully, i.e., they should follow
the rules as written. This also includes choosing whether to use them or not.
The rules module will generally always ask for usage, but this might be
responded to automatically by higher layers in either the server or the client
before being shown as a dialog to players.

Skills should only be marked as completed here if they are implemented and have
a test class in `modules/jervis-engine/src/commonTest/kotlin/dk/ilios/jervis/bb2025/skills`.

## Agility Skills

- [ ] Catch
    - [x] Reroll catching the landing after kickoff
    - [x] Reroll catching a successful Pass
    - [x] Reroll catching a bouncing ball
    - [x] Reroll catching a hand-off
    - [ ] Reroll catching a deviated ball
    - [x] Reroll catching a scattered ball
    - [x] Reroll catching a bouncing ball
    - [x] Only works on failed catches
    - [x] Does not work if missing tackle zones
    - [ ] Works multiple times pr. turn
- [ ] Diving Catch
- [ ] Diving Tackle
- [ ] Dodge
- [ ] Defensive
- [ ] Hit and Run
- [ ] Jump Up
- [ ] Leap
- [ ] Safe Pair of Hands
- [ ] Sidestep
- [ ] Sprint
- [x] Sure Feet
  - [x] Reroll failed Rush
  - [x] Reroll successful Rush
  - [x] Only once pr turn

## Devious Skills

- [x] Dirty Player
  - [x] Is optional
  - [x] Works on either Armour or Injury
  - [x] Can be used on every Foul Action.
- [ ] Eye Gouge
- [ ] Fumblerooskie
- [ ] Lethal Flight
- [ ] Pile Driver
- [x] Put the Boot In
  - [x] Doesn't work when prone
  - [x] Usage is optional (for now we always use it)
  - [x] Works when inside a tackle zone
  - [x] Only works on offensive assists
- [x] Quick Foul
  - [x] Can move after foul
  - [x] Cannot foul twice
  - [x] Cannot move if foul is a turnover
- [ ] Saboteur
- [x] Shadowing
  - [x] Only works on opponent turn
  - [x] 4+ to succeed
  - [x] Only works when a player dodges, not leaps or teleports
  - [x] Only one player can select shadowing 
  - [x] Can only use up to shadowing MA
- [ ] Sneaky Git
- [ ] Violent Innovator

## General Skills

- [x] Block
  - [x] Using Block is optional
  - [ ] Block does not work when distracted
  - [ ] Defender chooses bloc
- [ ] Dauntless
- [ ] Fend
    - [ ] Prevents Frenzy from working
- [ ] Frenzy*
    - [ ] Must take two blocks if player if able
    - [ ] Works during Block and Blitz
    - [ ] Must rush if needed
    - [ ] 2nd block isn't taken in case of a touchdown
    - [ ] Cannot take 2nd block if no more move/rushes left
    - [ ] Works on Rooted player (even though they are not pushed back (FAQ)
    - [ ] Works on Stand Firm player - No FAQ, but the same reasoning as for Rooted.
    - [ ] Roll twice for Foul Appearance (FAQ)
    - [ ] Frenzy 2nd block is not taken when defender is using Fend (as they cannot follow up)
    - [ ] Works on players with the Foul Appearance trait
    - [ ] 2nd block is not required to be a normal block but can be any valid "block" special action (FAQ)
- [x] Kick
  - [x] Usage is optional
  - [x] Reduces distance from D6 to D3
- [ ] Pro
- [ ] Steady Footing
- [ ] Strip Ball
- [ ] Sure Hands
- [ ] Tackle
- [ ] Taunt
- [ ] Wrestle

## Mutations

- [x] Big Hand
  - [x] Big Hands is optional
  - [x] Works on normal pickup
  - [x] Works on Secure the Ball
  - [x] Ignores Pouring Rain
  - [x] Ignores Marks
- [ ] Claws
- [ ] Disturbing Presence*
- [x] Extra Arms
  - [x] Is optional
  - [x] Works on Catch
  - [x] Works on Pickup
  - [x] Works on Secure the Ball
  - [x] Works on Interceptions
- [ ] Foul Appearance*
- [ ] Horns
- [ ] Iron Hard Skin
- [ ] Monstrous Mouth
- [ ] Prehensile Tail
- [ ] Tentacles
- [x] Two Heads
  - [x] Use skill on dodge
  - [x] Use skill multiple times pr. turn
  - [x] Skill use is optional
- [ ] Very Long Legs
  - [x] Is optional
  - [ ] +1 to Leap
  - [x] +1 to Jump
  - [x] +2 to Intercept
  - [x] Ignore CloudBurster

## Passing Skills

- [x] Accurate
  - [x] Works on Quick Pass
  - [x] Works on Safe Pass
  - [x] Is optional
- [x] Cannoneer
  - [x] Works on Long Pass
  - [x] Works on Long Bomb
  - [x] Is optional
- [x] Cloud Burster
  - [x] Prevents interceptions
- [ ] Dump-off
- [ ] Give and Go
- [ ] Hail Mary Pass
- [ ] Leader
    - [ ] Available after setup
    - [ ] Does not reset for overtime
    - [ ] Carry over into overtime
    - [ ] After Knocked Down
    - [ ] After Falling over
    - [ ] Removed immediately if pushed into the crowd
    - [ ] Not removed if other players with Leader on the field
    - [ ] Only one, regardless of number of players
- [ ] Nerves of Steel
- [ ] On the Ball
- [ ] Pass
- [ ] Punt
- [ ] Safe Pass

## Strength Skills

- [ ] Arm Bar
- [ ] Brawler
- [x] Break Tackle
  - [x] Can only use once pr turn
  - [x] If used on the first roll, it also applies to any reroll
  - [x] +1 if ST3 or less
  - [x] +2 if ST4
  - [x] +3 if ST5 or more
- [ ] Bulls Eye
- [ ] Grab
- [ ] Guard
- [ ] Juggernaut
- [ ] Mighty Blow
- [ ] Multiple Block
    - [ ] During Multiple Block: Scoring Turnovers will win over End-Turn turnovers.
- [ ] Stand Firm
- [ ] Strong Arm
- [ ] Thick Skull

## Traits

- [ ] Animal Savagery*
- [ ] Animosity (X)*
- [ ] Always Hungry*
- [ ] Ball & Chain*
- [ ] Bombardier
  - [ ] Accurate skill works
  - [ ] Cannoneer skill works
- [ ] Bone Armor*
- [ ] Bone Fury*
- [ ] Bone Head*
- [ ] Blood Lust (X+)*
- [ ] Breathe Fire
- [ ] Chainsaw*
- [ ] Decay*
- [ ] Hypnotic Gaze
- [ ] Kick Team-mate
- [ ] Loner (X+)*
- [ ] My Ball* 
  - [x] Cannot Pass
  - [x] Cannot Hand-off
  - [ ] Cannot Fumblerooskie
- [ ] No Ball*
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
- [x] Titchy*
  - [x] +1 when making a dodge
  - [x] Does not give -1 when dodging into a square marked by this player
- [ ] Throw Team-mate
- [ ] Timmm-ber!
- [ ] Unchannelled Fury*
- [x] Unsteady
  - [x] Prevent selecting Secure the Ball if standing
  - [x] Prevent selecting Throw Team-mate prone

# Star Player Skills

## Rulebook

- [ ] Krump and Smash, Varag Ghoul-Chewer
- [ ] Savage Blow, Anqi Panqi
- [ ] Master Assassin, Skitter Stab-Stab
- [ ] Star of the Show, Count Luthor von Drakenborg
- [ ] Dwarven Grit, Josef Bugman
- [ ] Thinking Man’s Troll, Ripper Bolgrot
- [ ] Slayer, Grim Ironjaw
- [ ] Blind Rage, Akhorne the Squirrel
- [ ] Consummate Professional, Griff Oberwald
- [ ] All You Can Eat, Cindy Piewhistle
- [ ] Halfling Luck, Puggy Baconbreath
- [ ] The Ballista, Morg ‘n’ Thorg
- [ ] Catch of the Day, Rodney Roachbait
- [ ] Lord of Chaos, Lord Borak The Despoiler
- [ ] Ram, Rumbelow Sheepskin
- [ ] The Flashing Blade, Jeremiah Kool

## Extra Star Players (Warhammer Community Download)
Source: https://www.warhammer-community.com/en-gb/downloads/blood-bowl/

- [ ] Blast It!, Barik Farblast
- [ ] Putrid Regurgitation, Bilerot Vomitflesh
- [ ] Sneakiest of the Lot, The Black Gobbo
- [ ] Look Into My Eyes, Boa Kon'sstriktr
- [ ] Kaboom!, Bomber Dribblesnot
- [ ] Tasty Morsel, 'Captain' Karina von Riesz
- [ ] Reliable, Deeproot Strongbranch
- [ ] A Sneaky Pair, Dribl & Drull
- [ ] Mesmerizing Dance, Eldril Sidewinder
- [ ] Baleful Hex, Estelle La Veneaux
- [ ] Whirling Dervish, Fungus the Loon
- [ ] Frenzied Rush, Glart Smashrip
- [ ] Shot to Nothing, Gloriel Summerbloom
- [ ] Primal Savagery, Glotl Stop
- [ ] I'll Carry You, Grak & Crumbleberry
- [ ] Gored by the Bull, Grashnak Blackhoof
- [ ] Incorporeal, Gretchen Wächter
- [ ] Wisdom of the White Dwarf, Grombrindal
- [ ] Quick Bite, Guffle Pusmaw
- [ ] Treacherous, Hakflem Skuttlespike
- [ ] Old Pro, Helmut Wolf
- [ ] Unstoppable Momentum, H'thark the Unstoppable
- [ ] Dwarven Scourge, Ivan ‘the Animal’ Deathshroud
- [ ] Raiding Party, Ivar Eriksson
- [ ] Swift as the Breeze, Jordell Freshbreeze
- [ ] Indomitable, Karla Von Kill
- [ ] Black Ink, Kiroth Krakeneye
- [ ] I'll Be Back, Kreek Rustgouger
- [ ] Vicious Vines, Maple Highgrove
- [ ] Maximum Carnage, Max Spleenripper
- [ ] Crushing Blow, Mighty Zug
- [ ] Kick ’em While They’re Down!, Nobbla Blackwart
- [ ] Toxin Connoisseur, Rashnak Backstabber
- [ ] Bounding Leap, Rowana Foresetfoot
- [ ] Slashing Nails, Roxanna Darknail
- [ ] Yoink!, Scrappa Sorehead
- [ ] Fury of the Blood Good, Scyla Anfingrimm
- [ ] Pump Up the Crowd, Skrorg Snowpelt
- [ ] Working in Tandem, Lucient & Valen Swift
- [ ] Furious Outburst, Swiftwine Glimmershard
- [ ] Beer Barrel Bash, Thorsson Stoutmead
- [ ] Savage Mauling, Wilhelm Chaney
- [ ] Woodland Fury, Willow Rosebark
- [ ] Watch out, Withergrasp Doubledrool
- [ ] Blastin' Solves Everything, Zzharg Madeye
- [ ] Excuse Me, Are You a Zoat?, Zolcath the Zoat

## BB2020 Players
Figure out what happened to these players and either remove them or add them to 
the correct list.

- [ ] Brutal Block, Frank ‘n’ Stein
- [ ] Ghostly Flames, Bryce 'the Slice' Cambuel: https://bbtactics.com/ghostly-flames/
- [ ] Strong Passing Game, Skrull Halfheight
- [ ] Watch Out!, Withergrasp Doubledrool

# Special Skills

This is not really skills, but more like "Special Rules". Unsure exactly how to track these.

- None