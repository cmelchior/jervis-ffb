package dk.ilios.jervis.model

import dk.ilios.jervis.utils.safeTryEmit
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// This doesn't serialize :(
// We have 2 options for how to make the Jervis Model observable:
// 1. Anyone changing the model classes must manually trigger the notifyChange method
//      - This is basically all classes in`dk.ilios.jervis.commands`, but the pattern is easy
//      - It can be easy to forget and there is no way to detect it.
//      - It keeps the model classes very simple and easy to serialize/deserialize.
//      - Keeping notifiers manual might be helpful for AI that is potentially doing a lot of changes,
//        but triggering listeners is just wasted cycles.
//
// 2. Make all properties observable
//      - Can be encoded by delegate properties
//      - Properties look slightly different, but the pattern is simple
//      - Code is contained inside model classes, no-one using the classes need to know it.
//      - Serialization is impossible without hacks due to https://github.com/Kotlin/kotlinx.serialization/issues/1578
abstract class Observable<T> {

    private val _state = MutableSharedFlow<T>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    protected val observeState: SharedFlow<T> = _state

    init {
        notifyUpdate() // Make sure there is one state in the flow
    }

    protected fun <P> observable(initialValue: P, onChange: ((oldValue: P, newValue: P) -> Unit)? = null): ReadWriteProperty<Any?, P> {
        return object: ObservableProperty<P>(initialValue) {
            override fun afterChange(property: KProperty<*>, oldValue: P, newValue: P) {
                _state.safeTryEmit(this@Observable as T)
                onChange?.let {
                    onChange(oldValue, newValue)
                }
            }
        }
    }

    public fun notifyUpdate() {
        _state.safeTryEmit(this as T)
    }
}

object JervisObservables {



}
