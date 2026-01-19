# Rules Discussions - BB2025

This file contains discussions about rule ambiguities in BB2025 and how they 
could be implemented in Jervis. This means that if an item is in this file, it 
has not been implemented yet.

Once an item is implemented, any discussion here is moved to 
[rules-faq-bb2025.md](rules-faq-bb2025.md) including justification for choosing
a certain interpretation.

* TODO: Review BB2020 version of this file and move over any relevant items.

## Rulebook

### Page 48 - Charge
The exact functionality of Charge is still a bit unclear in a number of areas:

Performing vs. Declaring actions:
The wording only mentions "performing" actions, not "declaring" them, but
normally you cannot perform actions without declaring _something_ first.

This opens up a lot of weird edge cases, like all negatraits only triggering
when you declare actions.

Special Actions:
By listing the number of actions you can perform, it opens up a question if
Special Actions are allowed. Right now the assumption is _no_ as Kick
Team-mate is a special action and is the only one explicitly mentioned.

Turn or not:
The wording is "may then be activated, one at a time, exactly as if it was
their teams turn", isn't exactly clear and leads to a number of
interpretations:

1) The entire Charge is treated as a normal team turn. This enables things
   like team rerolls, but also Wizards (that trigger end-of-turn)
2) Charge is _not_ a team turn, and the above sentence only applies to the
   activation and not when performing the action itself. This means that
   skills like Dodge and Break Tackle cannot be used.
3) Charge is _not_ a team turn, but the above sentence applies to the entire
   action itself. This means that skills like Dodge can be used.

All of this probably needs a FAQ to be clarified, so for now we are going
with an educated guess that requires the least amount of weirdness. These
proposed semantics:

- All players can declare a Move action
- One player can declare a Blitz action
- One player can declare a Throw Team-mate action
- One player can declare a Kick Team-mate action
- Special Actions that replace the block part of a Blitz action will work.
- All skills/traits granting standalone special actions will not work. This
  includes:
  - Ball & Chain
  - Bombardier
  - Breathe Fire
  - Chainsaw
  - Monstrous Mouth
  - Projectile Vomit
  - Punt
  - Stab (as block)
- Charge is not a Team Turn, so the team is not "active". This means that
  team rerolls do not work and effects that trigger end-of-turn effects cannot 
  be applied (like Wizards). This is similar to BB2020.
- You are not forced to activate or forego activation on all players. This is 
  entirely optional.
- All skills that only works during a teams turn will work for the activated
  players as the "exactly as a team turn" are assumed to extend to performing
  the action as well. This avoids the weirdness BB2020 had with a lot of
  skills not working during a Blitz.

### Page 54 - Standing up 
It is unclear what "regardless of any modifiers" means? Is Dodgy Snack a 
modifier? Is a Lasting Injury? What about a stat increase bought with SPP?

Example: Mummy with MA 3 receives a Dodgy Snack, making it MA 2 for the drive.
Do they need to roll to stand up or not?

### Page 73 - Throw-ins
Does not specify what happens if the ball lands in an unoccupied square making
it undefined. In BB2020, it would bounce, so most likely this section was 
just accidentally deleted. Passing to an empty specifically mentions that it
will bounce as well.

### Page 143 - Treacherous Trapdoor
It is unclear what "entering for any reason" means and when it triggers. This
impacts a lot of things from Jump and High Kick to Chain Push.

## Spike 19
None

## Spike 20
None


## Notes of things to follow up on

- Prayers to Nuffle are now moved into the Inducement section instead of being its own thing.
- Only one reroll is allowed per dice pool. Regardless of how many dice it affects.
- Turn over: Failing to catch ball. Is it a turnover if 2nd player catches the ball
- Bounces are now a Scatter(1) roll - Does it matter?
