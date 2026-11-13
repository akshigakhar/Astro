package com.astro.kundali.content

import com.astro.kundali.astro.*

fun chartHouseDescription(
    view: VargaChart,
    house: Int,
    highlightedHouses: Set<Int> = emptySet(),
    highlightedPlanets: Set<String> = emptySet(),
): String {
    require(house in 1..12)
    val occupants =
        view
            .occupants(house)
            .joinToString {
                "${it.name}${if(it.name in highlightedPlanets) ", highlighted" else ""}"
            }
            .ifEmpty { "no planets" }
    return "House $house${if(house==1) ", Lagna" else ""}, ${signs[view.signOfHouse(house)]}${if(house in highlightedHouses) ", highlighted" else ""}. Occupants: $occupants."
}

fun chartDescription(
    view: VargaChart,
    highlightedHouses: Set<Int> = emptySet(),
    highlightedPlanets: Set<String> = emptySet(),
): String =
    "North Indian D${view.division} chart. Lagna ${signs[view.ascSign]}. " +
        (1..12).joinToString(" ") {
            chartHouseDescription(view, it, highlightedHouses, highlightedPlanets)
        } +
        " Text placements and explanations follow the chart."
