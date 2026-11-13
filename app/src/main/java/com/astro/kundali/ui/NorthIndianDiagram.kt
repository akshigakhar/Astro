package com.astro.kundali.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.astro.kundali.astro.*
import kotlin.math.pow

private val centers =
    listOf(
        .5f to .25f,
        .25f to .10f,
        .10f to .25f,
        .25f to .5f,
        .10f to .75f,
        .25f to .90f,
        .5f to .75f,
        .75f to .90f,
        .90f to .75f,
        .75f to .5f,
        .90f to .25f,
        .75f to .10f,
    )

/** Shared fixed-house renderer for Kundali charts and lesson mini charts. */
@Composable
internal fun NorthIndianDiagram(
    view: VargaChart,
    label: (String) -> String,
    modifier: Modifier = Modifier,
    highlightedHouses: Set<Int> = emptySet(),
    highlightedPlanets: Set<String> = emptySet(),
    showHouseLabels: Boolean = false,
    onHouseClick: ((Int) -> Unit)? = null,
) {
    val tapModifier =
        if (onHouseClick == null) Modifier
        else
            Modifier.pointerInput(view.division, onHouseClick) {
                detectTapGestures { tap ->
                    onHouseClick(
                        centers.indices.minBy {
                            val p = centers[it]
                            (tap.x / size.width - p.first).pow(2) +
                                (tap.y / size.height - p.second).pow(2)
                        } + 1
                    )
                }
            }
    Canvas(
        modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .semantics {
                contentDescription =
                    "North Indian D${view.division} chart. Highlighted houses: ${highlightedHouses.sorted().joinToString().ifEmpty { "none" }}. Highlighted planets: ${highlightedPlanets.sorted().joinToString().ifEmpty { "none" }}. Text details follow."
            }
            .then(tapModifier)
    ) {
        val w = size.width
        val h = size.height
        val ink = Color(0xFF9C7A40)
        highlightedHouses.forEach { house ->
            val p = centers[house - 1]
            drawCircle(
                Color(0xFFE2C989).copy(alpha = .4f),
                radius = w * .075f,
                center = Offset(p.first * w, p.second * h),
            )
        }
        fun line(a: Float, b: Float, x: Float, y: Float) =
            drawLine(ink, Offset(a * w, b * h), Offset(x * w, y * h), 2f)
        line(0f, 0f, 1f, 0f)
        line(1f, 0f, 1f, 1f)
        line(1f, 1f, 0f, 1f)
        line(0f, 1f, 0f, 0f)
        line(0f, 0f, 1f, 1f)
        line(1f, 0f, 0f, 1f)
        line(.5f, 0f, 1f, .5f)
        line(1f, .5f, .5f, 1f)
        line(.5f, 1f, 0f, .5f)
        line(0f, .5f, .5f, 0f)
        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
                textSize = w * .028f
            }
        centers.forEachIndexed { i, p ->
            val house = i + 1
            val houseLabel =
                "${if(showHouseLabels) "H$house S" else ""}${view.signOfHouse(house)+1}${if(house==1) " As" else ""}${if(house in highlightedHouses) " *" else ""}"
            val lines =
                listOf(
                    houseLabel to
                        (house in highlightedHouses ||
                            (house == 1 && "Lagna" in highlightedPlanets))
                ) +
                    view.occupants(house).map { planet ->
                        (label(planet.name) +
                            if (planet.name in highlightedPlanets) " *" else "") to
                            (planet.name in highlightedPlanets)
                    }
            lines.forEachIndexed { j, (text, highlighted) ->
                paint.color =
                    if (highlighted) android.graphics.Color.rgb(117, 68, 0)
                    else android.graphics.Color.rgb(26, 64, 76)
                paint.isFakeBoldText = highlighted
                drawContext.canvas.nativeCanvas.drawText(
                    text,
                    p.first * w,
                    p.second * h + (j - (lines.size - 1) / 2f) * w * .032f,
                    paint,
                )
            }
        }
    }
}
