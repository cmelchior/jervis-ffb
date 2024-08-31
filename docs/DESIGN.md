

# How to design skills and modifiers?


FFB: Skills has Modifiers
    - i.e. 



Jervis:

    - Rules are encoded in Procedures

- 25 different rolls (probably more)
- Each roll type has a set of modifiers with a given value


Implementation A:
- Procedure checks for skill and then applies the modifier
- This means that the value is encoded in CatchModifier, not inaisw
- Application is encoded in the procedure

Skill: Name, Identifier, When is it valid to use
<X>Modifier: Encodes change for a given skill (why couldn't this be rolled into Skill?)
Procedure: Usage is still encoded here.
        
Having a CatchModifier enum makes it easier to reason about all the possible modifiers to a roll
Having it on Skill makes it easier to define the whole skill behavior
Having a CatchModifier class duplicates entries that are shared across many types, e.g. Marked, Weather
but at the same time, often these types have exceptions.

Can I do `Game.getRollModifier(DiceRollType.PICKUP)`

In procedure: Annoying if having many different versions of a procedure
In Effect: 

Lets use Pickup as an example.
 - Weather
 - Big Hands (ignore weather and marked by)
 - Marked by
 - Sure Hands (reroll)
 
Pickup shows that there are often interactions that requires rules to be encoded in the procedure, e.g.
skills that cancel each other.







Scenario:
    1. Dodge now adds +1 to roll
        Add new entry to DodgeRollModifier
        Add new check in DodgeRollProcedure
    
    2. Titsy now adds +2
        Update value in DodgeRollModifier


