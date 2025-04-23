# Rule Discussions

This file contains interesting discussions about rule ambiguities and how the rules should be
interpreted. All of these are not implemented yet. This is just a list of things to keep an eye
out for during development.

1. Chainsaw vs. Mighty Blow: https://fumbbl.com/index.php?name=PNphpBB2&file=viewtopic&t=33049
   You cannot use MB _against_ the player that has Chainsaw

2. When resolving Both Dow during a Block. Who is resolved first: Attacker or Defender?
   a. Does it matter? Yes, because if a knocked down player leaves the field, it can affect
      how a dropped ball bounces.

3. When selecting skill usage. Who goes first when it involves both teams? 
   Examples being: Dodge/Tackle and Break Tackle/Diving Tackle. 
      a Does it matter? Yes, for break tackle / diving tackle, which are both "use once".
      b. Is it "Active then InActive", "InActive then Active", "Active Coach Decides" or "InActive Coach Decides"
      c. For Both Down the order is: Defender then Attacker

4. It is a bit unclear if Sweltering Heat or Deal with Secret Weapons happens first, but Sweltering heat 
   does mention "once a drive ends", which matches the "The Drive Ends" step of the End of Drive sequence.

5. It isn't well-defined what happens in which order things happen at the end of the turn
   but currently the only thing that reset/expire is skills that uses rerolls. None of these
   should interfer with Throw A Rock. So the order here doesn't matter

6. (Page 64) Stalling is worded slightly weird, ie. the steps mention "at any point during your team
   turn", but the last section talk about activating players marked as "Stalling", indicating
   it is something checked before activation. Designers Commentary does clarify a bit, but some things are
   still unclear. 

   I interpret it as the following:

   a. If a player starts with the ball and can reach the end zone, without rolling _any_
      dice, they are stalling if they do not.

   b. If a player receives the ball during the turn. Check for stalling there, ie. if they
      haven't activated yet, they are stalling

   Example: Player A starts with the ball and can reach the end zone (A is Stalling). A hand-
   off to B, which hasn't activated yet and can also reach the end zone. B is now also Stalling.
   If B had activated, they are not marked as Stalling since it would violate check 3.

7. Can Throw a Rock hit people in the Reserves? From how Stalling is defined, it is hard to be stalling
   and in the Dogout (you will need to start with the ball, hand-off, then some effect put you in the 
   Dogout (knock down ))

8. Sweltering Heat and Throw a Rock. Throw a Rock, happens at the end of a team turn, while Sweltering
   Heat happens in the "End of Drive" sequence. So people hit by a rock are not on the pitch and cannot
   faint from the heat. 

9. It is unclear what happens if you put a player on a trapdoor during setup. Does that count 
    as "enter for any reason"? For we do nothing

10. The timing of Trapdoor vs. Dodge/Rush/Jump is unclear when reading the rule. 
    I am using the following interpretation:
    a. Announce move type: Standard/Dodge, Rush, Jump
    b. Move to target
    d. Roll Rush(es)
    e. Roll Dodge
    e. Roll for Jump. On (1) move player back to starting square
    c. Roll for Trapdoor. If you leave the field, ball stays there.
    f. If standing and moving voluntarily: Pickup Ball
    g. If Falling Over/Knocked Down/Moved involuntarily: Bounce ball (We assume this has the same timing as picking up)

    The reason being that Jump can move a player back, which would create weird situations if 
    if you start bouncing balls before being moved back. I see moving the player first
    as a "Tabletop" thing just to indicate the action, but in reality the player is not yet
    "fully" in the square, which means that all effects using the wording "enter square" doesn't
    trigger until Dodge/Rush/Jump has finished rolling. The result of these rolls determine
    "how" the player enters the square, either falling, knocked down or never entered it in the 
    first place. This interpretation also solves the timing with Tentacles.


11. How does Chain-pushes and Trapdoors interact? When does the player count as being "moved"?
    E.g. if Player A is pushed into Player B who is standing on a Trapdoor, 

13. Can a player that already have AV 11 receive Iron Man from Prayers of Nuffle? It wil not work
    for sure, but can they still get it? And what happens if they cannot?

15. For High Kick: Following the strict ordering of the rules, the Kick-Off Event is resolved 
    before "What Goes Up, Must Come Down". This means that the touchback rule cannot
    yet be applied when High Kick is resolved. Also, no-where is it stated that
    the high kick player cannot enter the opponents field. So if the ball goes out of bounds
    on the other side. In theory it would be allowed to move a player into the opponents field, 
    resolve the touchback (ie. the ball doesn't land) and give it to the player that moved into the 
    other side.

16. If you have two Special Play Cards with End of Opponent Turn and End of Turn, who plays first?

17. Rerolls and Regeneration: https://fumbbl.com/index.php?name=PNphpBB2&file=viewtopic&t=32212
    Also Pro is mentioned in Designer's Commentary. 
    If a player rolls Both-Down and is injured and rolls for regeneration
       - Pro does not work, because apparently the activation ended. Does that mean the activation ends as soon as you are knocked down?
       - Team reroll does work bcause the team turn still hasn't ended.

18. A player that that falls through a Trapdoor does not cause a turnover unless they where holding the
    ball. (at least reading from the rules)

19. Pro and rerolls https://fumbbl.com/index.php?name=PNphpBB2&file=viewtopic&t=32167&postdays=0&postorder=asc&start=0

20. Rules for activation are interpreted like this (this list is not defined anywhere)
    a. Select Player
    b. Start Activation (this regains lost Tackle Zones).
    c. Select Action. Some actions require a target as part of this:
       a. Blitz, Foul, 
    d. Start of Activation
       d1. Rodney's Catch of the Day
    d. Roll for Bone Head etc. Pro is allowed here
    e. If Bone Head fails, action is "used"
    f. Roll for Foul Appearance, Dump-off (Pr. Designer's Commentary)

21. Multiple Block / Frenzy / Special Actions: https://fumbbl.com/p/blog?c=Candlejack&id=25322
    My interpretation:
     - Each block is its "own" action, i.e. either block can be replaced by a special action
     - Some special actions have a "once pr. turn" limit, these can only be used on one of the
       blocks.

22. A Teams "Active Turn" ends as soon as there is a Turn-over. This was specified in Designer's Commentary
    from May 2022 regarding Regenerration where it was stated "No, as the active teams turn has ended by the time the Regeneration
    roll is made". This clarification was removed in later versions of the FAQ though. Unclear why.

23. When rolling on the Prayers To Nuffle table during the Kick-off Event, we treat "available
    to play during this drive" as players on both the pitch and in the Reserves box. It is unclear
    if players in the Dogout are covered by that phrase after you placed players on the field.

24. Can you reroll a roll with a team reroll if the pro roll failed? 
    - FUMBBL just fixed this so you can, but unclear where that interpretation came from.
    - "Once this player has attempted to use this skill, they may not use a re-roll from any other source to re-roll this one die"
      seems pretty clear to me? :thinking:
    - https://www.reddit.com/r/bloodbowl/comments/193k4g9/pro_question_bb_2020/

25. For Falling Over and Knocked Down, does the turnover happen before those are resolved,
    which would end the team turn, making rerolls unavailable. It looks like yes.

26. Tentacles and Dodge/Rushing. From the rules it is a bit unclear what happens first,
    but dodge is described as "The agility test is made after the dodging player has been moved".
    And since the tentacles prevent the move, the dodge roll never happens. Tentacles say
    "...Marking voluntarily moves out of an square". So the interpretation depends if there
    is a short point in time, where the player did actually move. But since. Tentacles don't
    say anything about "moving back". The interpretation is that Tentacles trigger on the "intent"
    of moving. See https://www.reddit.com/r/bloodbowl/comments/11w0p1b/comment/jdbzakx/?utm_source=share&utm_medium=web3x&utm_name=web3xcss&utm_term=1&utm_content=share_button
    This also impact Jumping, i.e. you roll for Tentacles before rolling any Rush/Agility.

27. Jumping and Rushing twice: https://www.reddit.com/r/bloodbowl/comments/nht634/bb2020_rules_query_involving_rush_and_jumping_a/
    Right now it looks like the interpretation is that if you fail the rush, you end up
    in the jumping square.

28. For Giants, it isn't well-defined how to interpret its location. E.g., during setup, is it considered
    to be in the wide zone if part of it is in there, or does all of it need to be inside?

# Rule Ambiguities and differences compared to the rules as written

This section describes how rules with an element of ambiguity are implemented as well as the rationale behind 
doing it. The page number reveres to the page in the rulebook unless otherwise specified.

1. (Page 74) The rules make it optional to use a skill before or after a roll. In Jervis, if possible, 
   this choice always comes after the roll. There is no valid reason for asking it before, and asking
   both before and after would create a lot of noise. The exception being Diving Tackle, where 
   there could be reasons for choosing it both before and after.

2. (Page 37) Rolling for the weather should be done by each coach rolling one die. This doesn't
   matter in this case, so both dice are just rolled as a single step by the Home coach.

3. (Page 38) The Prayers to Nuffle Table
   It is unclear what happens if you roll a result that is not a duplicate, but cannot be
   applied, e.g., because the entire team has Loner. For now, Jervis just treats the roll as 
   wasted. Mostly because it is easier to implement, and the chance of this happening 
   is virtually zero.

4. (Page 42) Order of events in End-of-Turn is not well-defined, e.g., it is unclear if Special Play 
    Cards like "Assassination Attempt" trigger before or after "Throw a Rock" and when temporary skills
    or abilities are removed.

    For now, we choose the (somewhat arbitrary) order:
    - Prayers Of Nuffle (Throw a Rock)
    - Special Play Cards
    - Temporary Skills/Characteristics are removed
    - Stunned Players are turned to Prone

5. (Page 23 + Random Event: Ball Clone)
    Ball clone says "one ball will immediately bounce", making it unclear which ball is
    bouncing: 
     - Is it random? 
     - The ball already there? 
     - Or the ball coming into the field?
   
    This is a problem when combined with a failed pass that could end up in a turnover.
    E.g., what happens if you throw a ball at a player, it misses and hits a square with
    another ball, a ball bounce from that square and is caught by the receiver. 
    
    This results in three questions:
     a. Does it matter which ball the receiver catches?
     b. If yes, which ball is bouncing?
     c. Does the coach know which ball is bouncing? (Relevant for choosing to reroll 
       the catch)

    Since all of these aspects are undefined, the implementation always lets the last ball
    bounce. This solves all the questions above as well as increases the "awesome"-factor if
    it actually succeeds since it prevents the turn-over.
   
6. (Page 80) When using "Multiple Block", it is unclear what "Both Block actions are performed simultaneously"
   means exactly. I.e., does this also apply to injury rolls? 

   I am mostly leaning towards "No". The reason being that "Risking Injury" (page 60) is described as 
   "as a result of a Block Action" and other skills, like Pro, are not working on Regeneration 
   (Designer's FAQ). This indicates that the action ends with a player being Knocked Down.

   For that reason, while Multiple Block injuries are collected in an "Injury Pool". We fully
   resolve each injury from there, letting the attacking player choose the order (this is also easier
   to implement).

7. (Page 90 - Death Zone) The area between lines of scrimmage is not named by the rulebook. In Jervis,
   this area is named "No Man's Land".

8. (Page 94 - Death Zone) Exactly where you are allowed to place the ball for a kick is unclear.
   The rulebook doesn't describe it at all, leaving it to the original rulebook. The problem is that
   it uses the term "team's half", which has an ambiguous meaning in Blood Bowl Sevens.

   Fortunately, the Designer's Commentary (May 2024) clarifies it a bit, allowing the ball to be 
   placed "Center Field", but again, it is unclear exactly what that means. Since (technically)
   "Center Field" runs the entire length of the field (and doesn't include the wide zone). Even if 
   you assume it just talks about the area between the two lines of scrimmage, it still doesn't 
   clarify if you can place it across the entire No Man's Land zone or only on the receiving teams half.

   This means RAW, you can only place it on the receiving teams half. While RAI is probably the entire
   No Man's Land zone. However, since the errata in the same FAQ specify that touchbacks only
   occur if the ball goes over the kicking teams line of scrimmage, we use that as the strongest
   argument that RAI is the correct interpretation.

   This was also discussed here: https://www.reddit.com/r/bloodbowl/comments/18giy10/kickoff_and_touchback_in_sevens/
