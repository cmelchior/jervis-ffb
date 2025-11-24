# Rules FAQ - BB2025

This file describes how BB2025 rules with an element of ambiguity are 
implemented as well as the rationale behind doing it the given way.

Due to copyright restrictions, this FAQ will not directly copy sections of text
from the rulebooks but only reference them by page numbers.

See [rules-discussions-bb2025.md](rules-discussions-bb2025.md) for rules that
are not implemented yet.

* TODO: Review BB2020 version of this file and move over any relevant items.


## Rulebook

### Page 23 - The Blood Bowl Pitch
The definition of Wide Zone is unclear (bullet 2). The rules describe it with 
"from End Zone to End Zone", but does not clarify if that includes or excludes 
the End Zone.

Jervis uses the interpretation that it _does_ include the End Zone as the 
Sidelines (bullet 3) include the same wording, and those clearly run the entire
length of the pitch.

### Page 38 - Player Status!
The rulebook doesn't explicitly state that the statuses: Standing, 
Distracted, Prone, Stunned, Tackle Zones, Marked, Being Marked and Open Players
are only present when a player is on the pitch, but it is pretty clear from 
context that this is assumed.

If not, it leads to weird consequences, like being able to move a player unto 
the field when rolling High Kick.

So when interpreting the rules, Jervis will always assume that these statuses
also imply that the player is on the field.

### Page 48 - High Kick (No available players)
It is unspecified what happens if you roll High Kick and no Open players are 
available. In this case, Jervis just ignores the High Kick and nothing happens. 

### Page 48 - High Kick (Move into opponent's half)
The following scenario is not covered by the rules and thus seems legal:

Following the strict ordering of the rules, the Kick-Off Event is resolved
before "What Goes Up...". This means that the touchback rule cannot yet be 
applied when High Kick is resolved.

No-where is it stated that the high kick player cannot enter the opponent's
side. This means it is allowed to move a player into the opponent's field and
then resolve the ball coming down.

This would result in a touchback, and the ball could then be given to the
player that moved into the opponent's half.

### Page 50 - End Turn and Forgo Activation
The wording around turns is a bit unclear about whether you _must_ activate all 
players (or forgo their activation), but the wording "until they have all 
activated or a Turnover is caused" indicates this.

For this reason, Jervis does allow you to end a turn before manually activating
all players. But in that case, Jervis will automatically Forgo Activation for
all remaining players as this might trigger Stalling.

### Page 52 - Forego Activation
This section is written in a way that indicates that forgoing activating 
doesn't clear Distracted. This is based on this wording "...and so will not be
subjected to any rules that take place at the start or during their activation".

This means that if a player is Distracted and chooses to forgo their activation, 
the Distracted status will _not_ be cleared.

### Page 58 - Rushing
The wording in the rulebook does not strictly specify that you can only Rush 
after using all normal Movement, which means that you can argue that a player 
can roll for Rush at the beginning of the turn, which could be an advantage in 
terms of positioning if it fails.

The opposite argument is that the rulebook uses the wording "..attempt to
push themselves and move a little bit further than they normally could..."; this
phrasing indicates that the roll happens after using normal movement.

The one example in the rulebook also demonstrates it happening after using the 
normal movement.

Since the BB2020 rulebook was also clear about this happening after using normal 
movement, Jervis will adopt the same interpretation for BB2025.

### Page 59 - Secure the Ball
It is unclear if modifiers like "Pouring Rain" apply to securing the ball.

Arguments for:
* Secure the Ball counts as "Picking up the ball" as described on page 57. This 
  is true as the rule references pickup through "must attempt to pickup the 
  ball" and "A player attempting to pick up the ball".
* This means that things like Pouring Rain and Marks are taken into account.
  So even though it isn't an agility test, it is the same as rolling for someone
  with AG 2+.

Arguments against:
* The wording "they will automatically pick up the ball", indicates that this is 
  not a "pickup"-roll, but a "secure-the-ball"-roll with the successful pickup 
  being a side effect.
* This implies that modifiers applying to Pickup do not also apply to Secure the 
  Ball. Since no modifiers explicitly mention Secure the Ball, it is always a
  unmodified 2+ roll

Jervis has adopted the 2nd interpretation, as otherwise, the wording "they will 
automatically pick up the ball" will not make any sense. This interpretation
is consistent with how FUMBBL interprets this rule.

### Page 69 - Foul Action and Lone Fouler
It is ambiguous what happens if you reroll a natural double in a Foul Action
with Lone Fouler.

Does the fouler still get sent off or not?

The Re-rolls section on page 33 is no help here either as it doesn't mention
what happens to the original role.

Arguments for:
* Being Sent-off uses the wording "...if during a Foul Action a natural double 
  is rolled..", with no indication that rerolling removes consequences of the 
  first roll.
* The wording is "rolled" not "outcome of the roll" (or similar).

Arguments against:
* In all other cases, e.g., Rush, you do not fall over if you re-roll a 1.
* The word "re-reroll" implies that the first roll is not counted.

Jervis has adopted the 2nd interpretation, as that is most in line with what 
people would assume a re-roll does (entirely replacing the first roll). This
would also explain why it isn't described better in the rulebook. 

## Spike 19
None


## Spike 20
None
