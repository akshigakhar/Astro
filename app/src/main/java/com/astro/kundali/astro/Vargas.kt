package com.astro.kundali.astro

/** Parashari D10: ten half-open 3° portions, odd signs from self, even from ninth. */
fun dashamsha(longitude: Double): Int {
    require(longitude.isFinite()) { "Longitude must be finite." }
    val lon = norm(longitude)
    val sign = (lon / 30).toInt()
    val portion = ((lon - sign * 30) / 3).toInt().coerceIn(0, 9)
    return (sign + (if (sign % 2 == 0) 0 else 8) + portion) % 12
}

data class VargaPosition(val name: String, val sign: Int, val house: Int)

data class VargaChart(val division: Int, val ascSign: Int, val positions: List<VargaPosition>) {
    fun signOfHouse(house: Int) = (ascSign + house - 1) % 12

    fun occupants(house: Int) = positions.filter { it.name != "Lagna" && it.house == house }
}

fun Chart.varga(division: Int): VargaChart {
    fun sign(p: Position) =
        when (division) {
            1 -> p.sign
            9 -> p.d9
            10 -> p.d10
            else -> error("Supported charts: D1, D9, D10")
        }
    val rising = sign(asc)
    return VargaChart(
        division,
        rising,
        positions.map { VargaPosition(it.name, sign(it), (sign(it) - rising + 12) % 12 + 1) },
    )
}
