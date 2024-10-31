# Rule Discussions

This file contains interesting discussions about rule ambiguities and how the rules should be
interpreted.

1. Chainsaw vs. Mighty Blow: https://fumbbl.com/index.php?name=PNphpBB2&file=viewtopic&t=33049
   You cannot use MB _against_ the player that has Chainsaw

2. When resolving Both Dow during a Block. Who is resolved first: Attacker or Defender?

3. When selecting skill usage. Who goes first? E.g. choosing Dodge/Tackle f or Stumble

4. In which order is skill usage declared when both teams have to choose? E.g. Break Tackle and Diving Tackle?
   Is it "Active then InActive", "InActive then Active", "Active Coach Decides" or "InActive Coache Decides"

5. It is a bit unclear if Sweltering Heat or Deal with Secret Weapons happens first, but Sweltering heat 
   does mention "once a drive ends", which matches the "The Drive Ends" step of the End of Drive sequence.

6. It isn't well-defined what happens in which order things happen at the end of the turn
   but currently the only thing that reset/expire is skills that uses rerolls. None of these
   should interfer with Throw A Rock. So the order here doesn't matter

7. (Page 64) Stalling is worded slightly weird, ie. the steps mention "at any point during your team
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

8. Can Throw a Rock hit people in the Reserves? From how Stalling is defined, it is hard to be stalling
   and in the Dogout (you will need to start with the ball, hand-off, then some effect put you in the 
   Dogout (knock down ))

9. Sweltering Heat and Throw a Rock. Throw a Rock, happens at the end of a team turn, while Sweltering
   Heat happens in the "End of Drive" sequence. So people hit by a rock are not on the pitch and cannot
   faint from the heat. 

10. It is unclear what happens if you put a player on a trapdoor during setup. Does that count 
    as "enter for any reason"? For we do nothing

11. The timing of Trapdoor vs. Dodge/Jump is a bit unclear. Since Dodge/Jump is rolled after the player
    is moved, I read it as the following sequence:
    a. Announce Jump
    b. Roll Rush 1 (if failed, fall over in starting square)
    c. Move to target
    d. Roll for Trapdoor
    e. Roll for Jump

    Same for Dodge
    a. Announce Dodge
    b. Move Player
    c. Roll for Trapdoor
    d. Roll for Dodge
      
12. How does Chain-pushes and Trapdoors interact? When does the player count as being "moved"?
    E.g. if Player A is pushed into Player B who is standing on a Trapdoor, 

13. What happens if you cannot apply a Prayers To Nuffle result, e.g. because the entire team
    has Loner? Designers Commentary for Lasting Injury indicates you reroll until you get
    a viable result. But Prayers also says "May roll", indicating that it is optional, so not being
    able to apply the result is okay.

14. Can a player that already have AV 11 receive Iron Man from Prayers of Nuffle? It wil not work
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

# Differences compared to the rules as written

1. (Page 74) The rules make it optional to use a skill before or after a roll. In Jervis, if possible, 
   this choice always come after the roll. There is no valid reason for asking it before, and asking
   both before and after would create a lot of noise. The exception being Diving Tackle, where 
   there could b reasons for choosing it both before and after.

2. (Page 37) Rolling for the weather should be done by each coach rolling one die. This doesn't
   matter in this case, so both dice are just rolled as a single step by the Home coach.

3. (Page 38) The Prayers to Nuffle Table
   It is unclear what happens if you roll a result that is not a duplicate, but cannot be
   applied, e.g., because the entire team has Loner. For now, Jervis just treat the roll as 
   wasted. Mostly because it is easier to implement, and the chance of this happening 
   is virtually zero.

4. (Page 42) Order of events in End-of-Turn is not well-defined, e.g., it is unclear if Special Play 
    Cards like Assassination Attempt trigger before or after Throw a Rock and when temporary skills
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
     c. Does the player know which ball is bouncing? (Relevant for choosing to reroll 
       the catch)

    Since all of these aspects are undefined, the implementation always lets the last ball
    bounce. This solves all the questions above as well as increases the "awesome"-factor if
    it actually succeeds since it prevents the turn-over.
   
6. When using Multiple Block, it is unclear what "Both Block actions are performed simultaneously"
   means exactly. I.e., does this also apply to injury rolls? 

   I am mostly leaning towards "No". The reason being that "Risking Injury" (page 60) is described as 
   "as a result of a Block Action" and other skills, like Pro, are not working on Regeneration 
   (Designer's FAQ). This indicates that the action ends with a player being Knocked Down.

   For that reason, while Multiple Block injuries are collected in an "Injury Pool". We fully
   resolve each injury from there, letting the attacking player choose the order. 
   (This is also easier to implement)
