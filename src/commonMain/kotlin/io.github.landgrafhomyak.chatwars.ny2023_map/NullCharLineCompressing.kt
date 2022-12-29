package io.github.landgrafhomyak.chatwars.ny2023_map

import kotlin.jvm.JvmStatic

/**
 * Сериализация и сжатие карты представленной в виде одномерного массива.
 *
 * В результате некоторого времени выяснилось что некоторые игроки уходят далеко от центра,
 * поэтому появляется очень много пустого места, которое кодируется длинной последовательностью из одинаковых символов.
 *
 * Последовательность из максимум 130 символов `'\0'` заменяется на пару, в которой первый символ - `'@'`,
 * а у второго код равен длине последовательности, увеличенной на 3. Сжимать последовательности из 1 и 2 символов
 * не имеет смысла, поэтому максимальную длину последовательности можно увеличить гарантируя что она содержит минимум
 * 3 символа. Полученный символ имеет код в диапазоне от 0 до 127, что удовлетворяет условиям ASCII и помещается в тип
 * `signed char`.
 * todo
 */
public object NullCharLineCompressing {
    /**
     * Сериализация и сжатие. Обратная функция: [NullCharLineCompressing.decompress].
     *
     * @see NullCharLineCompressing
     * @param from Карта.
     * @param to Буфер в который сохраняется сжатая карта, должен быть как минимум в два раза больше чем [from].
     * @return Длина сжатой карты.
     */
    @JvmStatic
    public fun compress(from: Array<TileType?>, to: ByteArray): Int {
        var o = 0
        var n = 0
        var c: TileType?
        global@ while (o < from.size) {
            c = from[o++]
            if (c != null) {
                to[n++] = c.serial.code.toByte()
                continue
            }

            @Suppress("LocalVariableName")
            var L = 1
            while (o < from.size && from[o] == null && L < 130) {
                o++
                L++
            }
            if (L == 1) {
                to[n++] = 0
                continue
            } else if (L == 2) {
                to[n++] = 0
                to[n++] = 0
                continue
            }
            to[n++] = '@'.code.toByte()
            to[n++] = (L - 3).toByte()
        }
        return n
    }

    /**
     * Разжатие и десериализация. Обратная функция: [NullCharLineCompressing.compress].
     *
     * @see NullCharLineCompressing
     * @param from Содержимое `xhr.responseText` с данными карты.
     * @return Карта в одномерном списке.
     */
    @JvmStatic
    public fun decompress(from: String): List<TileType?> {
        val buffer = ArrayList<TileType?>()
        var o = 0
        var c: Char
        while (o < from.length) {
            c = from[o++]
            if (c == '@') {
                for (unusedVar in 0 until (from[o++].code + 3))
                    buffer.add(null)
            } else {
                buffer.add(TileType.fromSerial(c))
            }
        }
        return buffer
    }
}
