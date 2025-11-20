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


## Spike 19
None


## Spike 20
None
