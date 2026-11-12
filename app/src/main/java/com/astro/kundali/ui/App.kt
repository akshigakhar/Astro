package com.astro.kundali.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Paint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.astro.kundali.astro.*
import com.astro.kundali.astro.Period
import com.astro.kundali.content.*
import java.time.*
import kotlin.math.pow

private val tabs =
    listOf(
        "Overview",
        "Kundali",
        "Planets",
        "Houses",
        "Life Topics",
        "Yogas",
        "Dasha",
        "Transits",
        "Learn Astrology",
    )

@Composable
fun AstroApp() {
    val colors =
        lightColorScheme(
            primary = Color(0xFF285968),
            secondary = Color(0xFF845C24),
            background = Color(0xFFF7F5EF),
            surface = Color(0xFFFFFCF5),
        )
    MaterialTheme(colorScheme = colors) {
        Surface(Modifier.fillMaxSize()) {
            val chartSaver =
                Saver<Chart?, List<String>>(
                    save = { value ->
                        value?.birth?.let { b ->
                            listOf(
                                b.name,
                                b.date.toString(),
                                b.time.toString(),
                                b.place.name,
                                b.place.lat.toString(),
                                b.place.lon.toString(),
                                b.place.zone,
                            )
                        } ?: emptyList()
                    },
                    restore = { a ->
                        if (a.isEmpty()) null
                        else
                            Engine.calculate(
                                Birth(
                                    a[0],
                                    LocalDate.parse(a[1]),
                                    LocalTime.parse(a[2]),
                                    Place(a[3], a[4].toDouble(), a[5].toDouble(), a[6]),
                                )
                            )
                    },
                )
            var chart by rememberSaveable(stateSaver = chartSaver) { mutableStateOf<Chart?>(null) }
            if (chart == null) BirthScreen { chart = it } else Results(chart!!) { chart = null }
        }
    }
}

@Composable
private fun TextCard(title: String, body: String, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun Expand(title: String, body: String, highlight: Boolean = false) {
    var open by rememberSaveable(title) { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            TextButton(onClick = { open = !open }) {
                Text("${if(highlight)"● " else ""}$title ${if(open)"−" else "+"}")
            }
            if (open) Text(body)
        }
    }
}

@Composable
private fun BirthScreen(onChart: (Chart) -> Unit) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("2000-01-01") }
    var time by rememberSaveable { mutableStateOf("12:00") }
    var query by rememberSaveable { mutableStateOf("") }
    var selected by remember { mutableStateOf(Cities.all.first()) }
    var custom by rememberSaveable { mutableStateOf(false) }
    var placeName by rememberSaveable { mutableStateOf("") }
    var lat by rememberSaveable { mutableStateOf("") }
    var lon by rememberSaveable { mutableStateOf("") }
    var zone by rememberSaveable { mutableStateOf("Asia/Kolkata") }
    var error by remember { mutableStateOf<String?>(null) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            "ASTRO",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
        Text("Your chart.\nA way to learn.", style = MaterialTheme.typography.headlineLarge)
        Text("Kundali & Learn Jyotish", style = MaterialTheme.typography.titleMedium)
        Text(
            "Explore the reasoning behind your Vedic birth chart, following a research-minded, non-fearful approach inspired by K. N. Rao."
        )
        OutlinedTextField(
            name,
            { name = it },
            label = { Text("Full name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    val d = LocalDate.parse(date)
                    DatePickerDialog(
                            context,
                            { _, y, m, day -> date = LocalDate.of(y, m + 1, day).toString() },
                            d.year,
                            d.monthValue - 1,
                            d.dayOfMonth,
                        )
                        .apply {
                            datePicker.minDate =
                                LocalDate.of(1900, 1, 1)
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()
                            datePicker.maxDate = System.currentTimeMillis()
                        }
                        .show()
                }
            ) {
                Text(date)
            }
            OutlinedButton(
                onClick = {
                    val t = LocalTime.parse(time)
                    TimePickerDialog(
                            context,
                            { _, h, m -> time = LocalTime.of(h, m).toString() },
                            t.hour,
                            t.minute,
                            true,
                        )
                        .show()
                }
            ) {
                Text("$time • 24h")
            }
        }
        Text(
            "Birth-time accuracy matters: a few minutes can change Lagna and divisional charts.",
            style = MaterialTheme.typography.bodySmall,
        )
        Row {
            Checkbox(custom, { custom = it })
            Text("Enter a custom place", Modifier.padding(top = 12.dp))
        }
        if (custom) {
            OutlinedTextField(
                placeName,
                { placeName = it },
                label = { Text("Place name") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                lat,
                { lat = it },
                label = { Text("Latitude −89.9 to 89.9") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                lon,
                { lon = it },
                label = { Text("Longitude −180 to 180") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                zone,
                { zone = it },
                label = { Text("IANA time zone, e.g. America/Toronto") },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            OutlinedTextField(
                query,
                { query = it },
                label = { Text("Search ${Cities.all.size} birth cities") },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Selected: ${selected.name} • ${selected.zone}",
                style = MaterialTheme.typography.labelLarge,
            )
            if (query.isNotBlank()) {
                val matches = Cities.all.filter { it.name.contains(query, true) }
                if (matches.isEmpty()) Text("No matching city. Use a custom place.")
                matches.take(8).forEach { p ->
                    TextButton(
                        onClick = {
                            selected = p
                            query = ""
                        }
                    ) {
                        Text("${p.name} · ${p.zone}")
                    }
                }
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = {
                runCatching {
                        require(name.isNotBlank()) { "Please enter your name." }
                        val d = LocalDate.parse(date)
                        require(!d.isAfter(LocalDate.now()) && d.year >= 1900) {
                            "Birth date must be between 1900 and today."
                        }
                        val p =
                            if (custom) {
                                val la = lat.toDouble()
                                val lo = lon.toDouble()
                                require(
                                    la.isFinite() &&
                                        la in -89.9..89.9 &&
                                        lo.isFinite() &&
                                        lo in -180.0..180.0
                                ) {
                                    "Check latitude and longitude ranges."
                                }
                                require(placeName.isNotBlank()) { "Enter the place name." }
                                ZoneId.of(zone.trim())
                                Place(placeName.trim(), la, lo, zone.trim())
                            } else selected
                        val local = LocalDateTime.of(d, LocalTime.parse(time))
                        require(ZoneId.of(p.zone).rules.getValidOffsets(local).isNotEmpty()) {
                            "This local time did not exist due to a clock change. Please check the record."
                        }
                        Engine.calculate(Birth(name.trim(), d, LocalTime.parse(time), p))
                    }
                    .onSuccess(onChart)
                    .onFailure { error = it.message ?: "Please check your birth details." }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Generate & explore")
        }
        OutlinedButton(
            onClick = { onChart(Engine.calculate(Cities.sample)) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("India — 15 Aug 1947, 00:00, Delhi")
        }
        TextCard(
            "Offline, private, thoughtful",
            "No account, ads, network permissions or paid remedies. Your birth details stay in this session. This is an educational tool, not medical, financial or legal advice.",
        )
        Text(accuracy, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun Results(c: Chart, onBack: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var lesson by rememberSaveable { mutableStateOf(-1) }
    BackHandler { if (lesson >= 0) lesson = -1 else onBack() }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = { if (lesson >= 0) lesson = -1 else onBack() }) {
                Text("‹ ${if(lesson>=0)"Lessons" else "Birth details"}")
            }
            Text("ASTRO", Modifier.padding(16.dp), style = MaterialTheme.typography.labelLarge)
        }
        if (lesson >= 0) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(lessons[lesson].title, style = MaterialTheme.typography.headlineMedium)
                Text(lessons[lesson].text)
                TextCard("Practice with your own chart", exercise(c, lessons[lesson]))
            }
        } else {
            ScrollableTabRow(selectedTabIndex = tab, edgePadding = 8.dp) {
                tabs.forEachIndexed { i, t -> Tab(tab == i, { tab = i }, text = { Text(t) }) }
            }
            key(tab) {
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    when (tab) {
                        0 -> {
                            Text(
                                "${c.birth.name}'s chart",
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Text(
                                "${c.birth.date} · ${c.birth.time} · ${c.birth.place.name}\n${c.birth.place.zone} · ${c.birth.place.lat}, ${c.birth.place.lon}\nUTC ${c.birth.instant}"
                            )
                            TextCard(
                                "The essentials",
                                "Lagna: ${signs[c.asc.sign]} ${c.asc.degree.fmt()}°\nMoon: ${signs[c.moon.sign]} · Sun: ${signs[c.p("Sun").sign]}\n${nakshatras[c.moon.nak]} · pada ${c.moon.pada} · lord ${c.moon.nakLord}\nTithi ${c.tithi} (${if(c.tithi<=15)"waxing" else "waning"})\nAtmakaraka: ${c.atmakaraka.name}\nLahiri ayanamsa: ${c.ayanamsa.fmt()}°\nRunning Vimshottari: ${running(c)}",
                            )
                            TextCard(
                                "Five observations",
                                "1. ${signs[c.asc.sign]} rises, setting ${rulers[c.asc.sign]} as Lagna lord.\n2. That lord occupies house ${c.lord(1).house}, linking self with ${houseTitles[c.lord(1).house-1].lowercase()}.\n3. Moon in house ${c.moon.house} gives a separate emotional reference.\n4. Tenth lord ${c.lord(10).name} occupies house ${c.lord(10).house}; start career inquiry with this link.\n5. Running ${running(c)} supplies timing questions, to confirm with Chara and lived evidence.",
                            )
                            TextCard("Read with care", accuracy)
                        }
                        1 -> {
                            Text(
                                "North Indian • D1",
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            Text(
                                "Fixed houses; numbers denote signs. Tap a house to inspect its reasoning."
                            )
                            NorthChart(c)
                            PlacementTable(c)
                        }
                        2 -> c.positions.forEach { TextCard(it.name, planetReading(c, it)) }
                        3 ->
                            (1..12).forEach {
                                TextCard("$it · ${houseTitles[it-1]}", houseReading(c, it))
                            }
                        4 ->
                            topics.forEach {
                                Expand(it.title, topicReading(c, it))
                                TextButton(onClick = { tab = 6 }) {
                                    Text("Explore running ${running(c)} dasha →")
                                }
                            }
                        5 -> {
                            TextCard(
                                "Combinations need context",
                                "A yoga fructifies through its planets' dashas and depends on strength and the whole chart. Confirm in both dashas; names are not promises.",
                            )
                            yogas(c).forEach {
                                TextCard(
                                    "${if(it.present)"Present" else "Not present"} · ${it.name}",
                                    it.explanation + "\n\nHow to verify this yourself: " + it.verify,
                                )
                            }
                        }
                        6 -> {
                            TextCard(
                                "Vimshottari",
                                "Periods use 365.25-day years. The first major period begins before birth; only its remaining balance is lived after birth. Judge natural meaning → owned houses → occupied house → associations → strength. Dates below are UTC; current periods are marked ●.",
                            )
                            val now = Instant.now()
                            Engine.vimshottari(c).forEach { p ->
                                Expand(
                                    "${p.lord}${if(p.start.isBefore(c.birth.instant)) " (birth balance)" else ""} · ${periodDates(p, c.birth.instant)}",
                                    Engine.antardashas(p)
                                        .filter { it.end.isAfter(c.birth.instant) }
                                        .joinToString("\n") {
                                            "${if(it.active(now))"● " else ""}${it.lord}: ${periodDates(it, c.birth.instant)}"
                                        },
                                    p.active(now),
                                )
                            }
                            TextCard(
                                "Chara • introductory Rao version",
                                "From Lagna: forward for odd-numbered signs, backward for even; directional distance to lord, twelve years if in own sign. This is an introductory scheme, not a complete advanced Chara implementation. Rao's study rule: confirm in BOTH dashas.",
                            )
                            Engine.chara(c).forEach {
                                TextCard(
                                    "${if(it.active(now))"● " else ""}${it.lord}",
                                    periodDates(it),
                                )
                            }
                        }
                        7 -> transits(c, Instant.now()).forEach { (a, b) -> TextCard(a, b) }
                        8 -> {
                            TextCard(
                                "Learn by checking",
                                "Fourteen original lessons inspired by Rao's research, synthesis and ethics. This app is independent and does not claim his endorsement. Each lesson ends with an exercise on your chart.",
                            )
                            lessons.forEachIndexed { i, l ->
                                OutlinedButton(
                                    onClick = { lesson = i },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(l.title)
                                }
                            }
                            glossary.forEach { (a, b) -> Expand(a, b) }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

private fun periodDates(p: Period, earliest: Instant = p.start) =
    "${maxOf(p.start, earliest).atZone(ZoneOffset.UTC).toLocalDate()} → ${p.end.atZone(ZoneOffset.UTC).toLocalDate()}"

// House centers follow the fixed North Indian layout, counter-clockwise from top.
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

@Composable
private fun NorthChart(c: Chart) {
    var selected by remember { mutableStateOf(1) }
    Canvas(
        Modifier.fillMaxWidth()
            .aspectRatio(1f)
            .semantics {
                contentDescription =
                    "North Indian birth chart; twelve houses, accessible house table follows"
            }
            .pointerInput(Unit) {
                detectTapGestures { tap ->
                    selected =
                        centers.indices.minBy {
                            val p = centers[it]
                            (tap.x / size.width - p.first).pow(2) +
                                (tap.y / size.height - p.second).pow(2)
                        } + 1
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val ink = Color(0xFF9C7A40)
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
                color = android.graphics.Color.rgb(26, 64, 76)
                textAlign = Paint.Align.CENTER
                textSize = w * .028f
            }
        centers.forEachIndexed { i, p ->
            val hno = i + 1
            val list =
                listOf("${c.signOfHouse(hno)+1}${if(hno==1)" As" else ""}") +
                    c.occupants(hno).map { "${it.name.take(2)} ${it.degree.fmt()}°" }
            list.forEachIndexed { j, s ->
                drawContext.canvas.nativeCanvas.drawText(
                    s,
                    p.first * w,
                    p.second * h + (j - (list.size - 1) / 2f) * w * .032f,
                    paint,
                )
            }
        }
    }
    TextCard("House $selected • ${houseTitles[selected-1]}", houseReading(c, selected))
}

@Composable
private fun PlacementTable(c: Chart) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text("Graha", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text(
                    "Sign / degree",
                    Modifier.weight(1.5f),
                    style = MaterialTheme.typography.labelMedium,
                )
                Text("House", Modifier.weight(.6f), style = MaterialTheme.typography.labelMedium)
            }
            HorizontalDivider()
            c.positions.forEach { p ->
                Row(Modifier.fillMaxWidth()) {
                    Text(p.name, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${signs[p.sign]} ${p.degree.fmt()}°",
                        Modifier.weight(1.5f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        p.house.toString(),
                        Modifier.weight(.6f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
