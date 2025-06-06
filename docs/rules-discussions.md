# Rules Discussions

This file contains interesting discussions about rule ambiguities and how the rules should be
interpreted. All of these are not implemented yet. This is just a list of things to keep an eye
out for during development. See [rules-faq.md](rules-faq.md) for rules that are already implemented.

## Chainsaw vs. Mighty Blow
https://fumbbl.com/index.php?name=PNphpBB2&file=viewtopic&t=33049
You cannot use MB _against_ the player that has Chainsaw

## Both Down during Block
When resolving Both Dow during a Block. Who is resolved first: Attacker or 
Defender?
    a. Does it matter? Yes, because if a knocked down player leaves the field, 
       it can affect how a dropped ball bounces.

## Selecting Skill Usage
When selecting skill usage. Who goes first when it involves both teams? 
Examples are: Dodge/Tackle and Break Tackle/Diving Tackle. 
    a. Does it matter? Yes, for break tackle / diving tackle, which are both 
       "use once".
    b. Is it "Active then InActive", "InActive then Active", "Active Coach 
       Decides" or "InActive Coach Decides"
    c. For Both Down the order is: Defender then Attacker (link to source?)

## End of Drive Sequence: Sweltering Heat and Secret Weapons
It is a bit unclear if Sweltering Heat or Deal with Secret Weapons happens 
first, but Sweltering heat does mention "once a drive ends", which matches the 
"The Drive Ends" step of the End of Drive sequence.

## End of Turn sequence
It isn't well-defined in which order things happen at the end of the turn, but 
currently the only thing that reset/expire is skills that uses rerolls. None of 
these should interfere with Throw A Rock. So the order here doesn't matter

## Stalling
(Page 64) Stalling is worded slightly weird, ie. the steps mention "at any point 
during your team turn", but the last section talk about activating players 
marked as "Stalling", indicating it is something checked before activation. 
Designers Commentary does clarify a bit, but some things are still unclear. 

I interpret it as the following:
    a. If a player starts with the ball and can reach the end zone, without 
       rolling _any_ dice, they are stalling if they do not.
    b. If a player receives the ball during the turn. Check for stalling there, 
       ie. if they haven't activated yet, they are stalling

Example: Player A starts with the ball and can reach the end zone (A is 
Stalling). A hand-off to B, which hasn't activated yet and can also reach the 
end zone. B is now also Stalling. If B had activated, they are not marked as 
Stalling since it would violate check 3.

## Throw a Rock and Reserves
Can Throw a Rock hit people in the Reserves? From how Stalling is defined, it is 
hard to be stalling and in the Dogout (you will need to start with the ball, 
hand-off, then some effect put you in the Dogout (like being knocked out))

## Throw a Rock and Sweltering Heat
Sweltering Heat and Throw a Rock. Throw a Rock, happens at the end of a team 
turn, while Sweltering Heat happens in the "End of Drive" sequence. So people 
hit by a rock are not on the pitch and cannot faint from the heat. 

## Trapdoor and Setup
It is unclear what happens if you put a player on a trapdoor during setup. Does 
that count as "enter for any reason"?

## Trapdoor and timings
The timing of Trapdoor vs. Dodge/Rush/Jump is unclear when reading the rule. 
I am using the following interpretation:
    a. Announce move type: Standard/Dodge, Rush, Jump
    b. Move to target
    d. Roll Rush(es)
    e. Roll Dodge
    e. Roll for Jump. On (1) move player back to starting square
    c. Roll for Trapdoor. If you leave the field, ball stays there.
    f. If standing and moving voluntarily: Pickup Ball
    g. If Falling Over/Knocked Down/Moved involuntarily: Bounce ball (We assume 
       this has the same timing as picking up)

The reason being that Jump can move a player back, which would create weird 
situations if if you start bouncing balls before being moved back. I see moving 
the player first as a "Tabletop" thing just to indicate the action, but in 
reality the player is not yet "fully" in the square, which means that all 
effects using the wording "enter square" doesn't trigger until Dodge/Rush/Jump 
has finished rolling. The result of these rolls determine "how" the player 
enters the square, either falling, knocked down or never entered it in the first
place. This interpretation also solves the timing with Tentacles.

##  Trapdoor and chain pushes
How does Chain-pushes and Trapdoors interact? When does the player count as 
being "moved"? E.g. if Player A is pushed into Player B who is standing on a 
Trapdoor, 

## Trapdoor and turnovers
A player that falls through a Trapdoor does not cause a turnover unless they
were holding the ball. (at least reading from the rules).

## Iron Man and AV 11
Can a player that already have AV 11 receive Iron Man from Prayers of Nuffle? 
It wil not work for sure (since limit is 11), but can they still get it? And 
what happens if they cannot?

## Order of Special Play Cards at End of Turn
If you have two Special Play Cards with End of Opponent Turn and End of Turn, 
who plays first?

## Rerolls and Regeneration
https://fumbbl.com/index.php?name=PNphpBB2&file=viewtopic&t=32212
Also Pro is mentioned in Designer's Commentary. 
If a player rolls Both-Down and is injured and rolls for regeneration
   - Pro does not work, because apparently the activation ended. Does that mean 
     the activation ends as soon as you are 
     knocked down?
   - Team reroll does work bcause the team turn still hasn't ended.

## Pro and rerolls
https://fumbbl.com/index.php?name=PNphpBB2&file=viewtopic&t=32167&postdays=0&postorder=asc&start=0

## Activation of Players
Rules for activation are interpreted like this (this list is not defined 
anywhere)
    a. Select Player
    b. Start Activation (this regains lost Tackle Zones).
    c. Select Action. Some actions require a target as part of this:
       a. Blitz, Foul, 
    d. Start of Activation
       d1. Rodney's Catch of the Day
    d. Roll for Bone Head etc. Pro is allowed here
    e. If Bone Head fails, action is "used"
    f. Roll for Foul Appearance, Dump-off (Pr. Designer's Commentary)

## Multiple Block / Frenzy / Special Actions
https://fumbbl.com/p/blog?c=Candlejack&id=25322
My interpretation:
 - Each block is its "own" action, i.e. either block can be replaced by a 
   special action
 - Some special actions have a "once pr. turn" limit, these can only be used on
   one of the blocks.

## Regeneration an end of turn
A Teams "Active Turn" ends as soon as there is a Turn-over. This was specified 
in Designer's Commentary from May 2022 regarding Regeneration where it was 
stated "No, as the active teams turn has ended by the time the Regeneration roll 
is made". This clarification was removed in later versions of the FAQ though. 
Unclear why.

## Prayers to Nuffle and available to play during drive
When rolling on the Prayers To Nuffle table during the Kick-off Event, we treat 
"available to play during this drive" as players on both the pitch and in the 
Reserves box. It is unclear if players in the Dogout are covered by that phrase 
after you placed players on the field.

## Can you reroll a roll with a team reroll if the pro roll failed? 
FUMBBL just fixed this so you can, but unclear where that interpretation came 
from.
- "Once this player has attempted to use this skill, they may not use a re-roll 
  from any other source to re-roll this one die" seems pretty clear to me? 
- https://www.reddit.com/r/bloodbowl/comments/193k4g9/pro_question_bb_2020/
- It seems this was reverted, so a die is considered being rerolled regardless 
  of Pro working or not Note, you can still reroll the pro die

## Falling Over/Knocked Down and rerolls
For Falling Over and Knocked Down, does the turnover happen before those are 
resolved, which would end the team turn, making rerolls unavailable. It looks 
like yes.

## Tentacles and Dodge/Rushing
From the rules it is a bit unclear what happens first,
but dodge is described as "The agility test is made after the dodging player 
has been moved". And since the tentacles prevent the move, the dodge roll never 
happens. Tentacles say "...Marking voluntarily moves out of an square". So the 
interpretation depends if there is a short point in time, where the player did 
actually move. But since. Tentacles don't say anything about "moving back". The 
interpretation is that Tentacles trigger on the "intent" of moving. See 
https://www.reddit.com/r/bloodbowl/comments/11w0p1b/comment/jdbzakx/?utm_source=share&utm_medium=web3x&utm_name=web3xcss&utm_term=1&utm_content=share_button
This also impact Jumping, i.e. you roll for Tentacles before rolling any 
Rush/Agility.

This was clarified in May 2025 FAQ - Tentacles are rolled first.

## Giants
For Giants, it isn't well-defined how to interpret its location. E.g., during 
setup, is it considered to be in the wide zone if part of it is in there, or 
does all of it need to be inside?

## Page 57 / 63 - Offensive/Defensive Assists
According to the rules, it is optional to provide offensive and defensive
assists to blocks and fouls. However, since there seems no reason (even bad) for
not always having the maximum number of assists, Jervis just calculates this
automatically.

For Blocks:
- Attacker: Only increases the number of dice being rolled. If the attacker
  wants a particular result, they can still select it.
- Defender: Reduces the number of dice being rolled. If the defenders want the
  attacker to have more choice, they are still not guaranteed that that choice
  is selected.

For Fouls:
- Attacker: Increase the likelihood of an armour break. If they didn't want to
  break armour, they wouldn't foul in the first place.
- Defender: If they wanted to stay 

If a use case shows up where there could be a use case (even a bad one), this
will be re-evaluated.

UPDATE:
Use case found: You want to get rid of your own players so you can 
concede without a penalty. So choosing assists should be optional (at least in
the rules engine)

### Titchy
The rules only mention +1, but not that they ignore marked modifiers. Does that
mean they get both +1 AND all marked modifiers (effectively ignoring only the 
first one).

### Interception
If a player deflects a ball, fails to catch it, it scatters, lands on the same
player again who then catches it. Is it a successful interception that will
award SPP?

