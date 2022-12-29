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
 */
public object NullCharLineCompressing {
    /**
     * Сериализация и сжатие. Обратная функция: [NullCharLineCompressing.decompress].
     *
     * @param from Карта.
     * @param to Буфер в который сохраняется сжатая карта, должен быть как минимум в два раза больше чем [from].
     * @return Длина сжатой карты.
     * @see NullCharLineCompressing
     */
    @JvmStatic
    public fun compress(from: Array<TileType?>, to: ByteArray): Int {
        var fromIndex = 0
        var toIndex = 0
        var currentTile: TileType?
        var sequenceLength:Int

        global@ while (fromIndex < from.size) {
            currentTile = from[fromIndex++]
            if (currentTile != null) {
                to[toIndex++] = currentTile.serial.code.toByte()
                continue
            }

            @Suppress("LocalVariableName")
            sequenceLength = 1
            while (fromIndex < from.size && from[fromIndex] == null && sequenceLength < 130) {
                fromIndex++
                sequenceLength++
            }
            if (sequenceLength == 1) {
                to[toIndex++] = 0
                continue
            } else if (sequenceLength == 2) {
                to[toIndex++] = 0
                to[toIndex++] = 0
                continue
            }
            to[toIndex++] = '@'.code.toByte()
            to[toIndex++] = (sequenceLength - 3).toByte()
        }
        return toIndex
    }

    /**
     * Разжатие и десериализация. Обратная функция: [NullCharLineCompressing.compress].
     *
     * @param from Содержимое `xhr.responseText` с данными карты.
     * @param to Одномерный массив размера которого должно хватать для представления карты в несжатом виде
     * @see NullCharLineCompressing
     */
    @JvmStatic
    public fun decompress(from: String, to: Array<TileType?>) {
        var fromIndex = 0
        var toIndex = 0
        var currentChar: Char
        while (fromIndex < from.length) {
            currentChar = from[fromIndex++]
            if (currentChar == '@') {
                for (sequenceIndex in 0 until (from[fromIndex++].code + 3))
                    to[toIndex++] = null
            } else {
                to[toIndex++] = TileType.fromSerial(currentChar)
            }
        }
    }
}
