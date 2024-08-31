package dk.ilios.jervis.reports

import dk.ilios.jervis.model.context.DodgeRollContext
import dk.ilios.jervis.utils.sum

class ReportDodgeResult(private val context: DodgeRollContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String
        get() {
            return buildString {
                when(context.isSuccess) {
                    true -> appendLine("${context.player.name} successfully dodged on a D6 roll of ${context.roll!!.result.value}. ")
                    false -> appendLine("${context.player.name} failed to dodge on a D6 roll of ${context.roll!!.result.value}.")
                }
                appendLine("Target: ${context.player.agility}+")
                append("Modifiers: ${context.rollModifiers.sum()}")
            }
        }
}

//FooBar starts a Move Action
//    FooBar attempts to dodge
//        Dodge Roll [6]
//            Foo uses Break Tackle
//            Bar uses Prehensile Tail
//            Baz uses Diving Tackle
//            Foo trips during the dodge ([6] - 4 < 3+)
//            Roll 6+ to succeed (d6 - 3 Marked + 1 Break Tackle >= 3+)
//            Foo uses a Pro Reroll
//                Pro Roll [4]
//                    Foo succesfully uses Pro
//        Dodge Roll [6]
//            Bal uses Diving Tackle
//            Foo succesfully dodges




//
//FooBar attempts do Dodge (+)
//    Dodge Roll [6]
//    FooBar failed to dodge
//    FooBar crashes to the ground
//
//FooBar attempts do Dodge (3+)
//    Dodge Roll [6] + []
//    R
//
//    FooBar failed to dodge
//    Foobar uses a Team reroll
//    Dodge Roll [8]
