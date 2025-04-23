package com.jervisffb.fumbbl.cli.gamedownloader

internal class Data {
    var `val` = 0
    var string: String? = null
    var position = 0
    var index = 0

    companion object {
        fun getInstance(): Data {
            return Data()
        }
    }
}

// @Suppress("standard:property-naming")
@Suppress("property-naming")
internal object LZString {
    var keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="

    fun compress(uncompressed: String?): String {
        if (uncompressed == null) {
            return ""
        }
        val context_dictionary: MutableMap<String, Int> = HashMap()
        val context_dictionaryToCreate: MutableSet<String> = HashSet()
        var context_c = ""
        var context_wc = ""
        var context_w = ""
        var context_enlargeIn = 2.0
        var context_dictSize = 3
        var context_numBits = 2
        var context_data_string = ""
        var context_data_val = 0
        var context_data_position = 0
        for (ii in 0 until uncompressed.length) {
            context_c = "" + uncompressed[ii]
            if (!context_dictionary.containsKey(context_c)) {
                context_dictionary[context_c] = Integer.valueOf(context_dictSize++)
                context_dictionaryToCreate.add(context_c)
            }
            context_wc = context_w + context_c
            if (context_dictionary.containsKey(context_wc)) {
                context_w = context_wc
            } else {
                if (context_dictionaryToCreate.contains(context_w)) {
                    if (context_w[0] < 'Ā') {
                        var k: Int
                        k = 0
                        while (k < context_numBits) {
                            context_data_val = context_data_val shl 1
                            if (context_data_position == 15) {
                                context_data_position = 0
                                context_data_string = context_data_string + context_data_val.toChar()
                                context_data_val = 0
                            } else {
                                context_data_position++
                            }
                            k++
                        }
                        var j = context_w[0].code
                        k = 0
                        while (k < 8) {
                            context_data_val = context_data_val shl 1 or (j and 0x1)
                            if (context_data_position == 15) {
                                context_data_position = 0
                                context_data_string = context_data_string + context_data_val.toChar()
                                context_data_val = 0
                            } else {
                                context_data_position++
                            }
                            j = j shr 1
                            k++
                        }
                    } else {
                        var j = 1
                        var k: Int
                        k = 0
                        while (k < context_numBits) {
                            context_data_val = context_data_val shl 1 or j
                            if (context_data_position == 15) {
                                context_data_position = 0
                                context_data_string = context_data_string + context_data_val.toChar()
                                context_data_val = 0
                            } else {
                                context_data_position++
                            }
                            j = 0
                            k++
                        }
                        j = context_w[0].code
                        k = 0
                        while (k < 16) {
                            context_data_val = context_data_val shl 1 or (j and 0x1)
                            if (context_data_position == 15) {
                                context_data_position = 0
                                context_data_string = context_data_string + context_data_val.toChar()
                                context_data_val = 0
                            } else {
                                context_data_position++
                            }
                            j = j shr 1
                            k++
                        }
                    }
                    context_enlargeIn--
                    if (java.lang.Double.valueOf(context_enlargeIn).toInt() == 0) {
                        context_enlargeIn = Math.pow(2.0, context_numBits.toDouble())
                        context_numBits++
                    }
                    context_dictionaryToCreate.remove(context_w)
                } else {
                    var j = context_dictionary[context_w] as Int
                    for (k in 0 until context_numBits) {
                        context_data_val = context_data_val shl 1 or (j and 0x1)
                        if (context_data_position == 15) {
                            context_data_position = 0
                            context_data_string = context_data_string + context_data_val.toChar()
                            context_data_val = 0
                        } else {
                            context_data_position++
                        }
                        j = j shr 1
                    }
                }
                context_enlargeIn--
                if (java.lang.Double.valueOf(context_enlargeIn).toInt() == 0) {
                    context_enlargeIn = Math.pow(2.0, context_numBits.toDouble())
                    context_numBits++
                }
                context_dictionary[context_wc] = Integer.valueOf(context_dictSize++)
                context_w = context_c
            }
        }
        if ("" != context_w) {
            if (context_dictionaryToCreate.contains(context_w)) {
                if (context_w[0] < 'Ā') {
                    var k: Int
                    k = 0
                    while (k < context_numBits) {
                        context_data_val = context_data_val shl 1
                        if (context_data_position == 15) {
                            context_data_position = 0
                            context_data_string = context_data_string + context_data_val.toChar()
                            context_data_val = 0
                        } else {
                            context_data_position++
                        }
                        k++
                    }
                    var j = context_w[0].code
                    k = 0
                    while (k < 8) {
                        context_data_val = context_data_val shl 1 or (j and 0x1)
                        if (context_data_position == 15) {
                            context_data_position = 0
                            context_data_string = context_data_string + context_data_val.toChar()
                            context_data_val = 0
                        } else {
                            context_data_position++
                        }
                        j = j shr 1
                        k++
                    }
                } else {
                    var j = 1
                    var k: Int
                    k = 0
                    while (k < context_numBits) {
                        context_data_val = context_data_val shl 1 or j
                        if (context_data_position == 15) {
                            context_data_position = 0
                            context_data_string = context_data_string + context_data_val.toChar()
                            context_data_val = 0
                        } else {
                            context_data_position++
                        }
                        j = 0
                        k++
                    }
                    j = context_w[0].code
                    k = 0
                    while (k < 16) {
                        context_data_val = context_data_val shl 1 or (j and 0x1)
                        if (context_data_position == 15) {
                            context_data_position = 0
                            context_data_string = context_data_string + context_data_val.toChar()
                            context_data_val = 0
                        } else {
                            context_data_position++
                        }
                        j = j shr 1
                        k++
                    }
                }
                context_enlargeIn--
                if (java.lang.Double.valueOf(context_enlargeIn).toInt() == 0) {
                    context_enlargeIn = Math.pow(2.0, context_numBits.toDouble())
                    context_numBits++
                }
                context_dictionaryToCreate.remove(context_w)
            } else {
                var j = context_dictionary[context_w] as Int
                for (k in 0 until context_numBits) {
                    context_data_val = context_data_val shl 1 or (j and 0x1)
                    if (context_data_position == 15) {
                        context_data_position = 0
                        context_data_string = context_data_string + context_data_val.toChar()
                        context_data_val = 0
                    } else {
                        context_data_position++
                    }
                    j = j shr 1
                }
            }
            context_enlargeIn--
            if (java.lang.Double.valueOf(context_enlargeIn).toInt() == 0) {
                context_enlargeIn = Math.pow(2.0, context_numBits.toDouble())
                context_numBits++
            }
        }
        var value = 2
        for (i in 0 until context_numBits) {
            context_data_val = context_data_val shl 1 or (value and 0x1)
            if (context_data_position == 15) {
                context_data_position = 0
                context_data_string = context_data_string + context_data_val.toChar()
                context_data_val = 0
            } else {
                context_data_position++
            }
            value = value shr 1
        }
        while (true) {
            context_data_val = context_data_val shl 1
            if (context_data_position == 15) {
                context_data_string = context_data_string + context_data_val.toChar()
                break
            }
            context_data_position++
        }
        return context_data_string
    }

    fun decompressHexString(hexString: String?): String? {
        if (hexString == null) {
            return ""
        }
        if (hexString.length % 2 != 0) {
            throw RuntimeException("Input string length should be divisible by two")
        }
        val intArr = IntArray(hexString.length / 2)
        var i = 0
        var k = 0
        while (i < hexString.length) {
            intArr[k] = ("" + hexString[i] + hexString[i + 1]).toInt(16)
            i += 2
            k++
        }
        val sb = StringBuilder()
        var j = 0
        while (j < intArr.size) {
            sb.append(Character.toChars(intArr[j] or (intArr[j + 1] shl 8)))
            j += 2
        }
        return decompress(sb.toString())
    }

    fun decompress(compressed: String?): String? {
        if (compressed == null) return ""
        if ("".equals(compressed, ignoreCase = true)) {
            return null
        }
        val dictionary: MutableList<String?> = ArrayList(200)
        var enlargeIn = 4.0
        var dictSize = 4
        var numBits = 3
        var entry: String? = ""
        var c = ""
        val data: Data = Data.getInstance()
        data.string = compressed
        data.`val` = compressed[0].code
        data.position = 32768
        data.index = 1
        for (i in 0..2) {
            dictionary.add(i, "")
        }
        var bits = 0
        var maxpower = Math.pow(2.0, 2.0)
        var power = 1
        while (power != java.lang.Double.valueOf(maxpower).toInt()) {
            val resb: Int = data.`val` and data.position
            data.position = data.position shr 1
            if (data.position == 0) {
                data.position = 32768
                data.`val` = data.string!![data.index++].code
            }
            bits = bits or (if (resb > 0) 1 else 0) * power
            power = power shl 1
        }
        when (bits) {
            0 -> {
                bits = 0
                maxpower = Math.pow(2.0, 8.0)
                power = 1
                while (power != java.lang.Double.valueOf(maxpower).toInt()) {
                    val resb: Int = data.`val` and data.position
                    data.position = data.position shr 1
                    if (data.position == 0) {
                        data.position = 32768
                        data.`val` = data.string!![data.index++].code
                    }
                    bits = bits or (if (resb > 0) 1 else 0) * power
                    power = power shl 1
                }
                c = c + bits.toChar()
            }

            1 -> {
                bits = 0
                maxpower = Math.pow(2.0, 16.0)
                power = 1
                while (power != java.lang.Double.valueOf(maxpower).toInt()) {
                    val resb: Int = data.`val` and data.position
                    data.position = data.position shr 1
                    if (data.position == 0) {
                        data.position = 32768
                        data.`val` = data.string!![data.index++].code
                    }
                    bits = bits or (if (resb > 0) 1 else 0) * power
                    power = power shl 1
                }
                c = c + bits.toChar()
            }

            2 -> return ""
        }
        dictionary.add(3, c)
        var w: String? = c
        val result = StringBuilder(200)
        result.append(c)
        while (true) {
            var temp: String
            if (data.index > data.string!!.length) {
                return ""
            }
            bits = 0
            maxpower = Math.pow(2.0, numBits.toDouble())
            power = 1
            while (power != java.lang.Double.valueOf(maxpower).toInt()) {
                val resb: Int = data.`val` and data.position
                data.position = data.position shr 1
                if (data.position == 0) {
                    data.position = 32768
                    data.`val` = data.string!![data.index++].code
                }
                bits = bits or (if (resb > 0) 1 else 0) * power
                power = power shl 1
            }
            var d: Int
            when (bits.also { d = it }) {
                0 -> {
                    bits = 0
                    maxpower = Math.pow(2.0, 8.0)
                    power = 1
                    while (power != java.lang.Double.valueOf(maxpower).toInt()) {
                        val resb: Int = data.`val` and data.position
                        data.position = data.position shr 1
                        if (data.position == 0) {
                            data.position = 32768
                            data.`val` = data.string!![data.index++].code
                        }
                        bits = bits or (if (resb > 0) 1 else 0) * power
                        power = power shl 1
                    }
                    temp = ""
                    temp = temp + bits.toChar()
                    dictionary.add(dictSize++, temp)
                    d = dictSize - 1
                    enlargeIn--
                }

                1 -> {
                    bits = 0
                    maxpower = Math.pow(2.0, 16.0)
                    power = 1
                    while (power != java.lang.Double.valueOf(maxpower).toInt()) {
                        val resb: Int = data.`val` and data.position
                        data.position = data.position shr 1
                        if (data.position == 0) {
                            data.position = 32768
                            data.`val` = data.string!![data.index++].code
                        }
                        bits = bits or (if (resb > 0) 1 else 0) * power
                        power = power shl 1
                    }
                    temp = ""
                    temp = temp + bits.toChar()
                    dictionary.add(dictSize++, temp)
                    d = dictSize - 1
                    enlargeIn--
                }

                2 -> return result.toString()
            }
            if (java.lang.Double.valueOf(enlargeIn).toInt() == 0) {
                enlargeIn = Math.pow(2.0, numBits.toDouble())
                numBits++
            }
            entry =
                if (d < dictionary.size && dictionary[d] != null) {
                    dictionary[d]
                } else if (d == dictSize) {
                    w + w!![0]
                } else {
                    return null
                }
            result.append(entry)
            dictionary.add(dictSize++, w + entry!![0])
            enlargeIn--
            w = entry
            if (java.lang.Double.valueOf(enlargeIn).toInt() == 0) {
                enlargeIn = Math.pow(2.0, numBits.toDouble())
                numBits++
            }
        }
    }

    fun compressToUTF16(input: String?): String {
        var input = input ?: return ""
        var output = ""
        var current = 0
        var status = 0
        input = compress(input)
        for (i in 0 until input.length) {
            val c = input[i].code
            when (status++) {
                0 -> {
                    output = output + ((c shr 1) + 32).toChar()
                    current = c and 0x1 shl 14
                }

                1 -> {
                    output = output + (current + (c shr 2) + 32).toChar()
                    current = c and 0x3 shl 13
                }

                2 -> {
                    output = output + (current + (c shr 3) + 32).toChar()
                    current = c and 0x7 shl 12
                }

                3 -> {
                    output = output + (current + (c shr 4) + 32).toChar()
                    current = c and 0xF shl 11
                }

                4 -> {
                    output = output + (current + (c shr 5) + 32).toChar()
                    current = c and 0x1F shl 10
                }

                5 -> {
                    output = output + (current + (c shr 6) + 32).toChar()
                    current = c and 0x3F shl 9
                }

                6 -> {
                    output = output + (current + (c shr 7) + 32).toChar()
                    current = c and 0x7F shl 8
                }

                7 -> {
                    output = output + (current + (c shr 8) + 32).toChar()
                    current = c and 0xFF shl 7
                }

                8 -> {
                    output = output + (current + (c shr 9) + 32).toChar()
                    current = c and 0x1FF shl 6
                }

                9 -> {
                    output = output + (current + (c shr 10) + 32).toChar()
                    current = c and 0x3FF shl 5
                }

                10 -> {
                    output = output + (current + (c shr 11) + 32).toChar()
                    current = c and 0x7FF shl 4
                }

                11 -> {
                    output = output + (current + (c shr 12) + 32).toChar()
                    current = c and 0xFFF shl 3
                }

                12 -> {
                    output = output + (current + (c shr 13) + 32).toChar()
                    current = c and 0x1FFF shl 2
                }

                13 -> {
                    output = output + (current + (c shr 14) + 32).toChar()
                    current = c and 0x3FFF shl 1
                }

                14 -> {
                    output = output + (current + (c shr 15) + 32).toChar()
                    output = output + ((c and 0x7FFF) + 32).toChar()
                    status = 0
                }
            }
        }
        output = output + (current + 32).toChar()
        return output
    }

    fun decompressFromUTF16(input: String?): String? {
        if (input == null) return ""
        val output = StringBuilder(200)
        var current = 0
        var status = 0
        var i = 0
        while (i < input.length) {
            val c = input[i].code - 32
            when (status++) {
                0 -> current = c shl 1
                1 -> {
                    output.append((current or (c shr 14)).toChar())
                    current = c and 0x3FFF shl 2
                }

                2 -> {
                    output.append((current or (c shr 13)).toChar())
                    current = c and 0x1FFF shl 3
                }

                3 -> {
                    output.append((current or (c shr 12)).toChar())
                    current = c and 0xFFF shl 4
                }

                4 -> {
                    output.append((current or (c shr 11)).toChar())
                    current = c and 0x7FF shl 5
                }

                5 -> {
                    output.append((current or (c shr 10)).toChar())
                    current = c and 0x3FF shl 6
                }

                6 -> {
                    output.append((current or (c shr 9)).toChar())
                    current = c and 0x1FF shl 7
                }

                7 -> {
                    output.append((current or (c shr 8)).toChar())
                    current = c and 0xFF shl 8
                }

                8 -> {
                    output.append((current or (c shr 7)).toChar())
                    current = c and 0x7F shl 9
                }

                9 -> {
                    output.append((current or (c shr 6)).toChar())
                    current = c and 0x3F shl 10
                }

                10 -> {
                    output.append((current or (c shr 5)).toChar())
                    current = c and 0x1F shl 11
                }

                11 -> {
                    output.append((current or (c shr 4)).toChar())
                    current = c and 0xF shl 12
                }

                12 -> {
                    output.append((current or (c shr 3)).toChar())
                    current = c and 0x7 shl 13
                }

                13 -> {
                    output.append((current or (c shr 2)).toChar())
                    current = c and 0x3 shl 14
                }

                14 -> {
                    output.append((current or (c shr 1)).toChar())
                    current = c and 0x1 shl 15
                }

                15 -> {
                    output.append((current or c).toChar())
                    status = 0
                }
            }
            i++
        }
        return decompress(output.toString())
    }

    @Throws(Exception::class)
    fun decompressFromBase64(input: String): String? {
        return decompress(decode64(input))
    }

    fun decode64(input: String): String {
        val str = StringBuilder(200)
        var ol = 0
        var output_ = 0
        var i = 0
        while (i < input.length) {
            val enc1 = keyStr.indexOf(input[i++])
            val enc2 = keyStr.indexOf(input[i++])
            val enc3 = keyStr.indexOf(input[i++])
            val enc4 = keyStr.indexOf(input[i++])
            val chr1 = enc1 shl 2 or (enc2 shr 4)
            val chr2 = enc2 and 0xF shl 4 or (enc3 shr 2)
            val chr3 = enc3 and 0x3 shl 6 or enc4
            if (ol % 2 == 0) {
                output_ = chr1 shl 8
                if (enc3 != 64) {
                    str.append((output_ or chr2).toChar())
                }
                if (enc4 != 64) {
                    output_ = chr3 shl 8
                }
            } else {
                str.append((output_ or chr1).toChar())
                if (enc3 != 64) {
                    output_ = chr2 shl 8
                }
                if (enc4 != 64) {
                    str.append((output_ or chr3).toChar())
                }
            }
            ol += 3
        }
        return str.toString()
    }

    fun encode64(input: String): String {
        val result = StringBuilder((input.length * 8 + 1) / 3)
        var i = 0
        val max = input.length shl 1
        while (i < max) {
            val left = max - i
            if (left >= 3) {
                val ch1 = input[i shr 1].code shr 1 - (i and 0x1) shl 3 and 0xFF
                i++
                val ch2 = input[i shr 1].code shr 1 - (i and 0x1) shl 3 and 0xFF
                i++
                val ch3 = input[i shr 1].code shr 1 - (i and 0x1) shl 3 and 0xFF
                i++
                result.append(keyStr[ch1 shr 2 and 0x3F])
                result.append(keyStr[(ch1 shl 4) + (ch2 shr 4) and 0x3F])
                result.append(keyStr[(ch2 shl 2) + (ch3 shr 6) and 0x3F])
                result.append(keyStr[ch3 and 0x3F])
                continue
            }
            if (left == 2) {
                val ch1 = input[i shr 1].code shr 1 - (i and 0x1) shl 3 and 0xFF
                i++
                val ch2 = input[i shr 1].code shr 1 - (i and 0x1) shl 3 and 0xFF
                i++
                result.append(keyStr[ch1 shr 2 and 0x3F])
                result.append(keyStr[(ch1 shl 4) + (ch2 shr 4) and 0x3F])
                result.append(keyStr[ch2 shl 2 and 0x3F])
                result.append('=')
                continue
            }
            if (left == 1) {
                val ch1 = input[i shr 1].code shr 1 - (i and 0x1) shl 3 and 0xFF
                i++
                result.append(keyStr[ch1 shr 2 and 0x3F])
                result.append(keyStr[ch1 shl 4 and 0x3F])
                result.append('=')
                result.append('=')
            }
        }
        return result.toString()
    }

    fun compressToBase64(input: String?): String {
        return encode64(compress(input))
    }

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val test = "Lets see how much we can compress this string!"
        val output = compress(test)
        println("Compressed: $output")
        val decompressed = decompress(output)
        println("Decompressed: $decompressed")
        val testUTF16 = "Lets see how much we can compress this string!"
        val outputUTF16 = compressToUTF16(testUTF16)
        println("Compressed: $outputUTF16")
        val decompressedUTF16 = decompressFromUTF16(outputUTF16)
        println("Decompressed: $decompressedUTF16")
        val testBase64 = "Lets see how much we can compress this string!"
        val outputBase64 = compressToBase64(testBase64)
        println("Compressed: $outputBase64")
        val decompressedBase64 = decompressFromBase64(outputBase64)
        println("Decompressed: $decompressedBase64")
    }
}
