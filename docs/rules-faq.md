# Rules FAQ

This file describes how rules with an element of ambiguity are implemented as 
well as the rationale behind doing it the given way.  

Due to copyright restrictions, this FAQ will not directly copy sections of text
from the rulebooks, but only reference them by page numbers.

See [rules-discussions.md](rules-discussions.md) for rules that are not implemented yet.

## Rulebook

### Page 23 - Random Event: Ball Clone
Ball clone says "one ball will immediately bounce", making it unclear which ball
is bouncing:

- Is it random?
- The ball already there?
- Or the ball coming into the square?

This is a problem when combined with a failed pass that could end up in a
turnover. E.g., what happens if you throw a ball at a player, it misses and hits
a square with another ball, a ball bounces from that square and is caught by the
receiver?

This results in three questions:

a. Does it matter which ball the receiver catches?
b. If yes, which ball is bouncing?
c. Does the coach know which ball is bouncing? (Relevant for choosing to reroll
the catch)

Since all of these aspects are undefined, the implementation always lets the
last ball bounce. This solves all the questions above as well as increases the
"awesome"-factor if it actually succeeds since it prevents the turn-over.

### Page 74 - When to choose skill usage
The rules make it optional to use a skill before or after a roll. In Jervis, if 
possible, this choice always comes after the roll. There is no valid reason for 
asking it before, and asking both before and after would create a lot of noise. 
The exception being Diving Tackle, where there could be reasons for choosing it 
both before and after.

### Page 37 - Rolling for Weather
Rolling for the weather should be done by each coach rolling one die. This 
doesn't matter in this case, so both dice are just rolled as a single step by 
the Home coach.

### Page 38 - The Prayers to Nuffle Table
It is unclear what happens if you roll a result that is not a duplicate, but 
cannot be applied, e.g., because the entire team has Loner. For now, Jervis just 
treats the roll as wasted. Mostly because it is easier to implement, and the 
chance of this happening is virtually zero.

### Page 40 - Nominate Kicking Player
Nominating the Kicking Player does not cover the case where you have more than 
3 players all on the Line of Scrimmage and none elsewhere. It is assumed this is 
an oversight, and in that case, nominating a player on the Line of Scrimmage is 
also allowed, but it is still only the center Line of Scrimmage.

As a side-note, it is never allowed to select a player to kick from the 
wide-zone as we are guaranteed to always have options available in the center 
Line of Scrimmage. This is slightly weird when coupled with the above, but is 
what brings us closest to the rules as written.

### Page 42 - End-of-Turn Sequence
Order of events in the End-of-Turn sequence is not well-defined, e.g., it is 
unclear if Special Play Cards like "Assassination Attempt" trigger before or 
after "Throw a Rock" and when temporary skills or abilities are removed.

For now, we choose the (somewhat arbitrary) order:
  - Prayers Of Nuffle (Throw a Rock)
  - Special Play Cards
  - Temporary Skills/Characteristics are removed
  - Stunned Players are turned to Prone

### Page 80 - Multiple Block
When using "Multiple Block", it is unclear what "Both Block actions are 
performed simultaneously" means exactly. I.e., does this also apply to injury 
rolls?

I am mostly leaning towards "No". The reason being that "Risking Injury" 
(page 60) is described as "as a result of a Block Action" and other skills, 
like Pro, are not working on Regeneration (Designer's FAQ). This indicates that
the action ends with a player being Knocked Down.

For that reason, while Multiple Block injuries are collected in an "Injury 
Pool". We fully resolve each injury from there, letting the attacking player 
choose the order (this is also easier to implement).


## Death Zone

### Page 90 - No Man's Land
The area between lines of scrimmage is not named by the Death Zone rulebook. In 
Jervis, this area is named "No Man's Land".

### Page 94 - Place Kick for Kickoff
Exactly where you are allowed to place the ball for a kick is unclear.
Death Zone doesn't describe it at all, leaving it to the original rulebook. The 
problem is that it uses the term "team's half" (page 40), which has an ambiguous 
meaning in Blood Bowl Sevens.

Fortunately, the Designer's Commentary (May 2024) clarifies it a bit, allowing 
the ball to be placed "Center Field", but again, it is unclear exactly what that 
means. Since (technically) "Center Field" runs the entire length of the field 
(and doesn't include the wide zones). Even if you assume it just talks about the 
area between the two lines of scrimmage, it still doesn't clarify if you can 
place it across the entire No Man's Land zone or only on the receiving teams 
half (if you split the No Mans's Land in two).

This means RAW, you can only place it on the receiving teams half. While RAI is 
probably the entire No Man's Land zone. However, since the errata in the same 
FAQ specify that touchbacks only occur if the ball goes over the kicking teams
line of scrimmage, we use that as the strongest argument that RAI is the correct 
interpretation and this is the implementation used in Jervis.

This was also discussed here: https://www.reddit.com/r/bloodbowl/comments/18giy10/kickoff_and_touchback_in_sevens/
