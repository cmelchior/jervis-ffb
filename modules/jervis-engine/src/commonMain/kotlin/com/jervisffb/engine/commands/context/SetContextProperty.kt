package com.jervisffb.engine.commands.context

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushContext
import kotlin.reflect.KMutableProperty1

/**
 * Helper command, making it easier to modify single properties in a complex
 * [com.jervisffb.engine.model.context.ProcedureContext] object, rather than
 * having to `copy()` it.
 *
 * Developer's Commentary:
 * Currently, the pattern we use is to have ProcedureContext objects being
 * immutable data classes, and modifications are happening through the `copy()`
 * method.
 *
 * The original idea was that having context objects being immutable made it
 * easier to reason about them in steams. And using the `copy()` method
 * was also fine for smaller objects and changes. However, due to how the
 * event stream with the UI is done, having these objects being immutable
 * doesn't really affect concurrency issues.
 *
 * Also, some context objects are pretty complex with nested data structures,
 * where this approach falls a bit apart. So we have already introduced custom
 * commands for manipulating lists inside context objects, and this class is
 * just a continuation of that.
 *
 * However, it does feel a bit weird to have this generic command object, but
 * forcing specific command objects for other properties in the model. For some
 * commands, we cannot use a generic wrapper though, since setting the property
 * requires calling functions or changing multiple places.
 *
 * Originally, we also wanted to avoid using KProperties as it made it
 * impossible to serialize the command objects, which was why we moved away
 * from it. But with the current architecture, we only serialize
 * GameActionDescriptor and GameAction objects. So Command objects are only
 * used locally in each client.
 *
 * Long story short, currently this class is an experiment, so try to avoid
 * using it everywhere until we have better signals that it is the way to go.
 *
 * Some piece of feedback. Creating these are a bit annoying and makes it hard
 * scanning nodes. So it feels like hiding these behind helper functions
 * makes it a bit more readable.
 *
 * @see PushContext
 * @see com.jervisffb.engine.model.context.DodgeRollContext
 */
class SetContextProperty<T, V>(
    private val property: KMutableProperty1<T, V>,
    private val obj: T,
    private val value: V
): Command {
    private var originalValue: V = value // Work-around for initializing the value

    override fun execute(state: Game) {
        originalValue = property.get(obj)
        property.set(obj, value)
    }

    override fun undo(state: Game) {
        property.set(obj, originalValue)
    }
}


