package dk.ilios.jervis.actions

abstract class DieResult(val result: Int, val min: Short, val max: Short): Number(), Action {
    init {
        if (result < min || result > max) {
            throw IllegalArgumentException("Result outside range: $min <= $result <= $max")
        }
    }
    override fun toByte(): Byte = result.toByte()
    override fun toDouble(): Double = result.toDouble()
    override fun toFloat(): Float = result.toFloat()
    override fun toInt(): Int = result.toInt()
    override fun toLong(): Long = result.toLong()
    override fun toShort(): Short = result.toShort()
    override fun toString(): String {
        return "${this::class.simpleName}[$result]"
    }
}