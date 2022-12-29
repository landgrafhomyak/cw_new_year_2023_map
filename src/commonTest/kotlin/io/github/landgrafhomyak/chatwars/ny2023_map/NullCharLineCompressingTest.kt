package io.github.landgrafhomyak.chatwars.ny2023_map

import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class NullCharLineCompressingTest {
    private val enum = arrayOf(TileType.ZERO, TileType.SHOP, TileType.TAVERN, TileType.WELL, TileType.FIELDS, TileType.VALLEY, TileType.FOREST)

    private fun buildTest(width: Int, height: Int, nullChance: Double) {
        val rnd = Random('@'.code)
        val map = Array<TileType?>(width * height) { null }
        for (i in map.indices) {
            val value = rnd.nextDouble()
            if (value > nullChance) {
                map[i] = this.enum[floor(((value - nullChance) / (1 - nullChance)) * enum.size).roundToInt()]
            }
        }

        val encoded = ByteArray(map.size)
        val compressedSize = NullCharLineCompressing.compress(map, encoded)
        val decoded = NullCharLineCompressing.decompress(encoded
            .slice(0 until compressedSize)
            .joinToString(separator = "") { b -> Char(b.toInt()).toString() }
        )
        assertEquals(map.toList(), decoded)
    }

    @Test
    fun withoutNullsSmall() = buildTest(3, 3, 0.0)

    @Test
    fun withoutNulls() = buildTest(1000, 1000, 0.0)


    @Test
    fun onlyNullsSmall() = buildTest(3, 3, 1.0)

    @Test
    fun onlyNulls() = buildTest(1000, 1000, 1.0)


    @Test
    fun halfNullsSmall() = buildTest(5, 5, 0.5)

    @Test
    fun halfNulls() = buildTest(1000, 1000, 0.5)

    @Test
    fun manyNullsSmall() = buildTest(5, 5, 0.75)

    @Test
    fun manyNulls() = buildTest(1000, 1000, 0.75)

    @Test
    fun lessNullsSmall() = buildTest(5, 5, 0.25)

    @Test
    fun lessNulls() = buildTest(1000, 1000, 0.25)
}