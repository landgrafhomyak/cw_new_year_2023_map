package io.github.landgrafhomyak.chatwars.ny2023_map

import kotlin.jvm.JvmStatic

public object CharLineCompressing {
    @JvmStatic
    public fun compress(from: Array<TileType?>, to: ByteArray): Int {
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

    @JvmStatic
    public fun decompress(from: String): List<TileType?> {
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
