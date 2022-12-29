@file:Suppress("UNREACHABLE_CODE", "unused", "UNUSED_PARAMETER")

package io.github.landgrafhomyak.chatwars.ny2023_map

import kotlin.jvm.JvmStatic

/**
 * Сериализация и сжатие карты представленной в виде одномерного массива.
 *
 * В результате некоторого времени выяснилось что некоторые игроки уходят далеко от центра,
 * поэтому появляется очень много пустого места, которое кодируется длинной последовательностью их одинаковых символов.
 *
 * todo
 */
@Deprecated("Not implemented", level = DeprecationLevel.ERROR)
public object CharLineCompressing {
    /**
     * Сериализация и сжатие. Обратная функция: [CharLineCompressing.decompress].
     *
     * @see CharLineCompressing
     * @param from Карта.
     * @param to Буфер в который сохраняется сжатая карта, должен быть как минимум в два раза больше чем [from].
     * @return Длина сжатой карты.
     */
    @JvmStatic
    public fun compress(from: Array<TileType?>, to: ByteArray): Int {
        TODO()
        var o = 0
        var n = 0
        var c: TileType?
        global@ while (o < from.size) {
            c = from[o++]
            var L = 0
            while (o < from.size && from[o] == c && L < 127) {
                o++
                L++
            }
            if (L == 1 && c != null) {
                to[n++] = TileType.serial(c).code.toByte()
            } else {
                to[n++] = (TileType.serial(c).code and 0b00111111).toByte()
                to[n++] = L.toByte()
            }
        }
        return n
    }

    /**
     * Разжатие и десериализация. Обратная функция: [CharLineCompressing.compress].
     *
     * @see CharLineCompressing
     * @param from Содержимое `xhr.responseText` с данными карты.
     * @return Карта в одномерном списке.
     */
    @JvmStatic
    public fun decompress(from: String): List<TileType?> {
        TODO()
        val buffer = ArrayList<TileType?>()
        var o = 0
        var c: Char
        var L: Int
        var t: TileType?
        while (o < from.length) {
            c = from[o++]
            if ((c.code and 0b01000000) == 0) {
                L = from[o++].code
                if (c != '\u0000')
                    c = Char(c.code or 0b01000000)
            } else {
                L = 1
            }
            t = TileType.fromSerial(c)
            for (unused in 0 until L) {
                buffer.add(t)
            }
        }
        return buffer
    }
}
