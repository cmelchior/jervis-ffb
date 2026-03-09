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
- [x] Defensive
  - [x] Prevents Guard
  - [x] Prevents Put the Boot In
- [ ] Hit and Run
- [ ] Jump Up
- [ ] Leap
    - [x] Leap over any square using Jump rules
    - [x] Must use two move to reach target square. If not enough move left (including rush), no jump is allowed.
    - [x] If Rushing twice and fail, ends up in starting square
    - [x] If Rushing once and fail, ends up in starting square,
    - [ ] Cannot jump over a Giant since the rules specify a "single square"
    - [x] Modifiers on leaving and entering
    - [x] Leap modifier is optional
    - [x] Leap modifier only work if there are more than one negative modifier
- [x] Safe Pair of Hands
  - [x] Does not work against Strip Ball Pow (NAF)
  - [x] Works on Knocked Down
  - [x] Works on Falling Over
  - [x] Works on being Placed Prone
  - [x] Does not work if no unoccupied adjacent squares
- [x] Sidestep
  - [x] Sidestep is optional
  - [x] Player with sidestep choose the target square.
  - [x] Sidestep does not work when prone (During chain-pushes)
  - [x] Sidestep is not available if there are no valid target squares
  - [x] Is not available if Distracted
- [ ] Sprint
  - [ ] Is optional
  - [ ] Adds 1 extra move pr action
- [x] Sure Feet
  - [x] Reroll failed Rush
  - [x] Reroll successful Rush
  - [x] Only once pr turn

## Devious Skills

- [x] Dirty Player
  - [x] Is optional
  - [x] Works on either Armour or Injury
  - [x] Can be used on every Foul Action.
- [x] Eye Gouge
  - [x] Is Optional
  - [x] Works on Foul Assists
  - [x] Works on Block Assists
  - [x] Next activation clear the status
  - [x] Status is kept across end-of-half
  - [x] Does not work in Chain Pushes
  - [x] Apply on Pushback
  - [x] Apply on Pushback into Crowd
  - [x] Apply on POW
  - [x] Apply on Stumble
  - [x] Does not work against Stand Firm
- [ ] Fumblerooskie
- [ ] Lethal Flight
- [x] Lone Fouler
  - [x] "Failed" rolls also take into account other modifiers
  - [x] Only works if no offensive assists
  - [x] Only works if no defensive assists
  - [x] Modifiers are also optional on the reroll
  - [x] Does not work if Put the Boot In is used
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
- [x] Sneaky Git
  - [x] Works on double armour if not broken
  - [x] Does not work on double armour if broken
  - [x] Does not work on double injury
  - [x] Is optional
  - [ ] When combined with Lone Fouler, only consider last roll
- [ ] Violent Innovator

## General Skills

- [x] Block
  - [x] Using Block is optional
  - [x] Block does not work when distracted
- [x] Dauntless
  - [x] Added before all other modifiers (especially Horns)
  - [x] Skip if Stronger than opponent
  - [x] Works on Blitz
  - [x] Works on Block
  - [x] Unmodified Strength is ignoring all temporary modifiers.
- [ ] Fend
    - [x] Prevent follow up
    - [ ] Does not work against Ball & Chain
    - [ ] Does not work against Juggernaut
    - [x] Prevent follow up from Frenzy
    - [x] Works on POW
    - [x] Works on Stumble
    - [x] Works on Pushback
    - [x] Does not trigger on Both Down
    - [x] Does not work when Distracted
- [ ] Frenzy*
    - [ ] Must take two blocks if player if able
    - [ ] Works during Block and Blitz
    - [ ] Horns work on both Frenzy Blocks
    - [ ] Does not force the use of Sprint
    - [ ] 2nd Block can only be thrown if the target is Pushed Back
    - [ ] 2nd Block can be another Special Action like Stab
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
- [x] Steady Footing
  - [x] If success, prevents turn over
  - [x] Prevent Fall Over (during own turn)
    - [x] Rush
    - [x] Jump
    - [x] Being Thrown
  - [x] Prevent Knocked Down (during own turn)
    - [x] Block
  - [x] Prevent Knocked Down (during opponent turn)
  - [x] Does not work when Distracted
- [x] Strip Ball
    - [x] Is optional
    - [x] Bounce from square after follow up
    - [x] Does not work against a chain-pushed player
    - [x] Does not work against Stand Firm (Fumbbl)
    - [x] No score, if ball is stripped when pushed into end zone
- [x] Sure Hands
  - [x] Works on pickup
  - [x] Does not work on Secure the Ball
  - [x] Prevent Strip Ball from working
- [x] Tackle
  - [x] Prevent use of Dodge skill when dodging away
  - [x] Only use Tackle if other player has Dodge
  - [x] Defender does not count as having dodge during a Block
- [ ] Taunt
  - [x] Cannot use Taunt if Fend is selected
  - [ ] Does not work against Rooted
  - [x] Works on POW
  - [x] Works on Stumble
  - [x] Works on Pushback
  - [x] Does not work on Both Down
  - [x] Does not work when Distracted
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
- [x] Horns
  - [x] Add +1 to ST for block action during Blitz
  - [x] Does not work on Block
- [ ] Iron Hard Skin
- [ ] Monstrous Mouth
- [x] Prehensile Tail
  - [x] Adds -1 negative modifier
  - [x] Only one player can use it
- [ ] Tentacles
- [x] Two Heads
  - [x] Use skill on dodge
  - [x] Use skill multiple times pr. turn
  - [x] Skill use is optional
- [x] Very Long Legs
  - [x] Is optional
  - [x] Does not work with Pogo
  - [x] +1 to Leap
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
- [x] Give and Go
  - [x] Works on successful Quick Pass
  - [x] Does not work on Short Pass and above
  - [x] Works on successful Hand Off
  - [x] Does not work on Hand Off that results in a turnover
- [ ] Hail Mary Pass
  - [ ] Works on Bombs
  - [x] Select any square on the field
  - [x] Cannot intercept with Hail Mary Pass
  - [x] Always Long Bomb distance
  - [x] Always count Accurate as Inaccurate
- [ ] Leader
    - [ ] Available after setup
    - [ ] Does not reset for overtime
    - [ ] Carry over into overtime
    - [ ] After Knocked Down
    - [ ] After Falling over
    - [ ] Removed immediately if pushed into the crowd
    - [ ] Not removed if other players with Leader on the field
    - [ ] Only one, regardless of number of players
- [x] Nerves of Steel
  - [x] Ignore Marked modifiers on Catch
  - [x] Ignore Marked modifiers on Pass
- [ ] On the Ball
- [ ] Pass
  - [ ] Works for Bombs
  - [x] Works for normal passes
  - [x] Works for hail mary passes
- [ ] Punt
- [ ] Safe Pass
  - [x] Works on Pass
  - [x] Works on Hail Mary Pass
  - [ ] Works on throwing a bomb

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
- [x] Grab
  - [x] Grab is optional
  - [x] Blocking player select any adjacent square to blocked player
  - [x] Grab cannot be used if there are no valid target squares
  - [x] Prevents using Sidestep
  - [x] Grab does not prevent Sidestep in a Chain-Push (NAF)
  - [x] Only work on Declared Block Actions, i.e. not on Blitz.
- [x] Guard
  - [x] Allow offensive assists
  - [x] Allow defensive assists
- [ ] Juggernaut
- [x] Mighty Blow
  - [x] Works on Armour
  - [x] Works on Injury if not used on Armour
  - [x] Is not used if no effect
  - [x] Does not work if Distracted
  - [x] Works for both Attacker and Defender
- [ ] Multiple Block
    - [ ] During Multiple Block: Scoring Turnovers will win over End-Turn turnovers.
    - [ ] Only normal blocks are available, no special actions
    - [ ] Tackle is available for both blocks
    - [ ] Taunt does not work against Multiple Block
    - [ ] Mighty Blow works on both block
    - [ ] Wrestle works on both block
    - [ ] Most roll for Dauntless on both blocks
- [x] Stand Firm
  - [x] Prevent being pushed back in the first block
  - [x] Prevent being pushed back in a chain push
  - [x] Does not prevent Frenzy hitting twice
  - [x] Does not work if distracted
  - [x] Prevent Follow Up
  - [x] No players are moved if using Stand Firm during the Chain Push
- [x] Strong Arm
  - [x] Works on Quick Pass
  - [x] Works on Safe Pass
  - [x] Is optional
- [x] Thick Skull
  - [x] Stunty rolling 7 (incl modifiers)
  - [x] Normal player rolling 8 (incl modifiers)
  - [x] Do not use Thick Skull if it isn't useful

## Traits

- [ ] Animal Savagery*
  - [ ] Must use Mighty Blow on Armour 
  - [ ] Must use Claw
- [ ] Animosity (X)*
- [ ] Always Hungry*
- [ ] Ball & Chain*
- [ ] Bombardier
  - [ ] Accurate skill works
  - [ ] Cannoneer skill works
  - [ ] Safe Pass skills works
  - [ ] Hail Mary Pass works
- [ ] Bone Head*
  - [x] Clear after activating
  - [x] Roll after selecting action
  - [x] Does not clear if you forgo activation
  - [x] Use action if failing roll
  - [ ] Can use skills to reroll Bone Head (Pro)
- [ ] Blood Lust (X+)*
- [x] Breathe Fire
    - [x] Replace Block
    - [x] Mighty Blow does not work on Breathe Fire
    - [x] Can be used during a Blitz
    - [x] Cannot move after Breathe Fire during a Blitz
    - [x] Hit opponent (natural 6)
    - [x] Hit opponent (4+)
    - [x] Hit player (1)
    - [x] Nothing happens (2-3)
    - [x] -1 modifier to roll on 5+ ST
    - [x] Is not available when starting from Prone
    - [x] Is not available if not starting next to an opponent
- [ ] Chainsaw*
- [ ] Decay*
- [ ] Hypnotic Gaze
- [x] Insignificant*
- [ ] Kick Team-mate
- [ ] Loner (X+)*
- [ ] My Ball* 
  - [x] Cannot Pass
  - [x] Cannot Hand-off
  - [ ] Cannot Fumblerooskie
- [x] No Ball*
  - [x] Catch rolls are automatic 1's
  - [x] Do not use Extra Arms on Catch
  - [x] Pickup rolls are automatic 1's
  - [x] Do not use Big Hand / Extra Arms on Pickup
  - [x] Secure the Ball rolls are automatic 1's
  - [x] Cannot intercept
- [ ] Plague Ridden
- [ ] Pogo Stick
    - [x] Pogo over any square using Jump rules
    - [x] Must use two move to reach target square. If not enough move left (including rush), no jump is allowed.
    - [x] If Rushing twice and fail, ends up in starting square
    - [x] If Rushing once and fail, ends up in starting square,
    - [ ] Cannot Pogo over a Giant since the rules specify a "single square"
    - [x] Modifiers on leaving and entering
    - [x] No negative modifiers when using Pogo
- [x] Projectile Vomit
    - [x] Replace Block
    - [x] Mighty Blow does not work on Projectile Vomit
    - [x] Unsuccessful Projectile Vomit on Armour does nothing
    - [x] Can be used during a Blitz
    - [x] Cannot move after Projectile Vomit during a Blitz
    - [x] Hit opponent
    - [x] Hit player
    - [x] Is not available when starting from Prone
    - [x] Is not available if not starting next to an opponent
- [ ] Really Stupid*
  - [ ] Pro can be used as a reroll
  - [x] Clear after activating
  - [x] Roll after selecting action
  - [x] Does not clear if you forgo activation
  - [x] Use action if failing roll
  - [x] Adjacent player can offer help
  - [x] Adjacent player with Really Stupid cannot offer help
  - [x] Distracted adjacent player cannot offer help
- [ ] Regeneration
- [x] Right Stuff*
  - [x] Can be thrown using Throw Team-mate
  - [x] Can be thrown prone when using Throw Team-mate
- [ ] Secret Weapon*
- [x] Stab
  - [x] Replace Block
  - [x] Mighty Blow does not work on Stab
  - [x] Unsuccesfull Stab on Armour does nothing
  - [x] Can be used during a Blitz
  - [x] Cannot move after Stabbing during a Blitz
  - [x] Is not available when starting from Prone
  - [x] Is not available if not starting next to an opponent
- [x] Stunty*
  - [x] No marked negative modifiers when Dodging
  - [x] -1 to intercept
  - [x] Roll on Stunty Injury Table
- [ ] Swoop
- [ ] Take Root*
- [x] Titchy*
  - [x] +1 when making a dodge
  - [x] Does not give -1 when dodging into a square marked by this player
- [x] Throw Team-mate
- [ ] Timmm-ber!
- [ ] Unchannelled Fury*
  - [ ] Pro can be used as a reroll
  - [x] Roll after selecting action
  - [x] Use action if failing roll
  - [x] Block improves chance
  - [x] Blitz improves chance
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