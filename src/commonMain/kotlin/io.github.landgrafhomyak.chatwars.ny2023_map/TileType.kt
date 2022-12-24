package io.github.landgrafhomyak.chatwars.ny2023_map

import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/**
 * Тип локации.
 * @property serial Символ длинной в 1 байт для сериализации карты.
 * @property cssClass Имя css класса для отображения локации на HTML странице.
 */
public enum class TileType(
    @JvmField
    public val serial: Char,
    @JvmField
    public val cssClass: String
) {
    VALLEY('V', "valley"),
    FOREST('F', "forest"),
    FIELDS('f', "fields"),
    TAVERN('t', "tavern"),
    ZERO('z', "zero"),
    WELL('w', "well"),
    SHOP('s', "shop");

    public companion object {
        /**
         * Преобразует символ в [TileType] в соответствии со значениями полей [TileType.serial]. Если символ не опознан, возвращает `null`.
         */
        @JvmStatic
        public fun fromSerial(serial: Char): TileType? {
            if (serial < 's') {
                if (serial == 'F') return FOREST
                if (serial == 'V') return VALLEY
                if (serial == 'f') return FIELDS
            } else if (serial > 's') {
                if (serial == 't') return TAVERN
                if (serial == 'w') return WELL
                if (serial == 'z') return ZERO
            } else {
                return SHOP
            }
            return null
        }

        /**
         * Пытается прочитать эмодзи в начале строки.
         *
         * @return
         * Если эмодзи - один из типов локации, возвращается соответствующее значение [TileType], иначе `null`.
         * Если символов недостаточно для прочтения - возвращает `null`.
         */
        @JvmStatic
        @Suppress("RemoveRedundantQualifierName", "ReplaceSizeZeroCheckWithIsEmpty")
        public fun fromEmoji(source: String): TileType? {
            if (source.length < 1) return null
            when (source[0]) {
                '\uD83C' -> {
                    if (source.length < 2) return null
                    when (source[1]) {
                        '\uDFD4' -> return TileType.VALLEY
                        '\uDF3B' -> return TileType.FIELDS
                        '\uDF32' -> return TileType.FOREST
                    }
                }
                '\uD83D' -> {
                    if (source.length < 2) return null
                    when (source[1]) {
                        '\uDED6' -> return TileType.TAVERN
                        '\uDD73' -> return TileType.WELL
                    }
                }
                '\uD83E' -> {
                    if (source.length < 2) return null
                    if (source[1] == '\uDDFF') return TileType.ZERO
                }
                '\u26FA' -> {
                    return TileType.SHOP
                }
            }
            return null
        }

        /**
         * Возвращает значение [TileType.serial] или `'\0'` если аргумент `null`.
         */
        @JvmStatic
        public fun serial(self: TileType?): Char {
            return self?.serial ?: '\u0000'
        }
    }
}
