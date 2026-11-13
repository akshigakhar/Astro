package com.astro.kundali.content

import com.astro.kundali.astro.*

data class QuizQuestion(
    val id: String,
    val prompt: String,
    val choices: List<String>,
    val answer: Int,
    val explanation: String,
)

data class LessonFocus(val houses: Set<Int>, val planets: Set<String>, val explanation: String)

fun lessonKey(index: Int): String {
    require(index in lessons.indices)
    return "lesson-${index+1}"
}

fun lessonQuiz(c: Chart, index: Int): List<QuizQuestion> {
    lessonKey(index)
    val out = mutableListOf<QuizQuestion>()
    fun q(prompt: String, answer: String, pool: List<String>, why: String) {
        val choices =
            (listOf(answer) + pool.filter { it != answer }.distinct().take(3)).toMutableList()
        val shift = (index + out.size + 1) % choices.size
        repeat(shift) { choices.add(choices.removeAt(0)) }
        out +=
            QuizQuestion(
                "${lessonKey(index)}-q${out.size+1}",
                prompt,
                choices,
                choices.indexOf(answer),
                why,
            )
    }
    fun fact(prompt: String, answer: String, vararg distractors: String, why: String) =
        q(prompt, answer, distractors.toList(), why)
    fun house(prompt: String, number: Int, why: String) =
        q(prompt, "House $number", (1..12).map { "House $it" }, why)
    fun planetHouse(name: String) =
        house(
            "Which natal house does your $name occupy?",
            c.p(name).house,
            "$name is in ${signs[c.p(name).sign]}, house ${c.p(name).house}, counted inclusively from ${signs[c.asc.sign]} Lagna. Occupation is separate from ownership.",
        )
    fun sign(prompt: String, s: Int, why: String) = q(prompt, signs[s], signs, why)
    fun lordHouse(h: Int) =
        house(
            "Which natal house does your ${when(h) { 1 -> "1st"
 2 -> "2nd"
 3 -> "3rd"
 else -> "${h}th" }}-house lord occupy?",
            c.lord(h).house,
            "House $h has ${signs[c.signOfHouse(h)]}, ruled by ${c.lord(h).name}. That planet occupies house ${c.lord(h).house}, linking those topics; this is not an event prediction.",
        )
    fun lordName(h: Int) =
        q(
            "Who rules your natal house $h?",
            c.lord(h).name,
            names.take(7),
            "House $h is ${signs[c.signOfHouse(h)]}, whose classical ruler is ${c.lord(h).name}.",
        )
    fun yesNo(prompt: String, yes: Boolean, why: String) =
        q(prompt, if (yes) "Yes" else "No", listOf("Yes", "No"), why)
    fun both() =
        fact(
            "Which timing confirmation does the app teach?",
            "Vimshottari AND Chara",
            "Transits alone",
            "One impressive yoga",
            "Sun sign alone",
            why =
                "Rao-inspired verification seeks agreement between two timing systems after assessing natal testimony; agreement still does not establish certainty.",
        )
    when (index) {
        0 -> {
            sign(
                "What is your Lagna sign?",
                c.asc.sign,
                "${signs[c.asc.sign]} rises; its ruler ${c.lord(1).name} anchors the house framework.",
            )
            lordHouse(1)
            both()
            fact(
                "Which consultation practice fits this course?",
                "Explain uncertainty without fear",
                "Predict death",
                "Sell a fear-based gemstone",
                "Ignore consent",
                why =
                    "The course preserves dignity, agency, birth-time uncertainty and ethical limits. It does not sell remedies or predict death.",
            )
            fact(
                "What does Desha-Kala-Patra ask you to consider?",
                "Place, time and individual circumstances",
                "Only planetary names",
                "Only a birth year",
                "A guaranteed outcome",
                why =
                    "Interpretation must account for social conditions, the historical moment and the individual, rather than erasing real circumstances.",
            )
        }
        1 -> {
            fact(
                "Which are the kendras?",
                "1, 4, 7, 10",
                "2, 5, 8, 11",
                "3, 6, 9, 12",
                "5, 9, 11",
                why = "Kendras are angular houses: self, home, partnership and public role.",
            )
            fact(
                "Which are the usual dusthanas?",
                "6, 8, 12",
                "1, 5, 9",
                "1, 4, 7, 10",
                "2, 3, 11",
                why =
                    "Six, eight and twelve include challenges, but also constructive fields such as service, research and retreat. They are not a sentence of suffering.",
            )
            fact(
                "Which are the upachayas?",
                "3, 6, 10, 11",
                "1, 4, 7, 10",
                "2, 5, 9, 12",
                "6, 8, 12",
                why = "Upachayas describe fields where effort and experience can develop capacity.",
            )
            lordHouse(10)
            lordName(5)
        }
        2 -> {
            planetHouse("Mercury")
            planetHouse("Sun")
            sign(
                "What is your Jupiter's natal sign?",
                c.p("Jupiter").sign,
                "Jupiter is in ${signs[c.p("Jupiter").sign]}; compare its natural meanings with owned and occupied houses.",
            )
            fact(
                "Which natural themes belong especially to Mercury?",
                "Language, analysis and exchange",
                "Only physical strength",
                "Only retirement",
                "Only lunar rhythms",
                why =
                    "Mercury's natural significations include language, calculation, study and exchange. Its functional role also depends on lordship and occupation.",
            )
            fact(
                "How does this app assign classical house ownership to nodes?",
                "No independent ownership",
                "Rahu owns every house",
                "Ketu owns the tenth for everyone",
                "Both own Cancer",
                why =
                    "This convention gives classical signs to the seven visible grahas. For Rahu/Ketu, investigate the sign lord and associations.",
            )
        }
        3 -> {
            val element = listOf("Fire", "Earth", "Air", "Water")[c.asc.sign % 4]
            q(
                "What element is your ${signs[c.asc.sign]} Lagna sign?",
                element,
                listOf("Fire", "Earth", "Air", "Water"),
                "Elements repeat fire, earth, air, water from Aries. They are interpretive categories, not personal worth.",
            )
            val quality = listOf("Movable", "Fixed", "Dual")[c.asc.sign % 3]
            q(
                "What quality is ${signs[c.asc.sign]}?",
                quality,
                listOf("Movable", "Fixed", "Dual"),
                "Qualities repeat movable, fixed, dual from Aries; they also determine D9 starting signs.",
            )
            q(
                "What basic dignity label does the app give your Sun?",
                c.p("Sun").dignity,
                listOf("Exalted", "Debilitated", "Own sign", "Ordinary sign placement"),
                "Your Sun is in ${signs[c.p("Sun").sign]}. Exaltation is Aries, debilitation Libra and own sign Leo; dignity alone is not a verdict.",
            )
            sign(
                "What is your Moon's natal sign?",
                c.moon.sign,
                "Moon is at ${c.moon.degree.fmt()}° ${signs[c.moon.sign]}; a sign is a 30° division.",
            )
            fact(
                "Where is a planet's debilitation sign relative to its exaltation sign?",
                "Opposite, six signs away",
                "Always the next sign",
                "Always its own sign",
                "Always Pisces",
                why =
                    "Classical debilitation is opposite exaltation. Neecha Bhanga needs additional conditions and full-chart evaluation.",
            )
        }
        4 -> {
            sign(
                "What sign occupies your natal tenth house?",
                c.signOfHouse(10),
                "Counting whole signs from ${signs[c.asc.sign]} gives ${signs[c.signOfHouse(10)]} at house ten.",
            )
            lordName(10)
            lordHouse(10)
            val occupants = c.occupants(10).joinToString { it.name }.ifEmpty { "No planets" }
            q(
                "Which planets occupy your natal tenth house?",
                occupants,
                listOf("No planets", "Sun only", "Moon only", "All nine grahas"),
                "Occupants are found in the tenth sign from Lagna: $occupants. An empty house still has a lord.",
            )
            fact(
                "Which is the four-step house checklist?",
                "House → occupants → lord → karaka",
                "Yoga name → prediction",
                "Transit → remedy → certainty",
                "Only count occupants",
                why =
                    "Describe the house, examine occupants, locate and assess the lord, then compare the natural significator. Preserve conflicting testimony.",
            )
        }
        5 -> {
            q(
                "What is your Moon's nakshatra?",
                nakshatras[c.moon.nak],
                nakshatras,
                "Divide sidereal longitude by 13°20′: your Moon falls in ${nakshatras[c.moon.nak]}.",
            )
            q(
                "What is your Moon's pada?",
                c.moon.pada.toString(),
                (1..4).map { it.toString() },
                "Each nakshatra has four 3°20′ padas. Your Moon occupies pada ${c.moon.pada}.",
            )
            q(
                "Which lord rules your Moon's nakshatra?",
                c.moon.nakLord,
                dashaLords,
                "${nakshatras[c.moon.nak]} is ruled by ${c.moon.nakLord}; this sets the birth Mahadasha lord.",
            )
            sign(
                "In which natal house sign is your Moon?",
                c.moon.sign,
                "Your Moon is in ${signs[c.moon.sign]}, a sign boundary distinct from nakshatra boundaries.",
            )
            fact(
                "How wide is one nakshatra?",
                "13°20′",
                "30°",
                "3°",
                "1°",
                why = "360° divided by 27 is 13°20′. Each segment then divides into four padas.",
            )
        }
        6 -> {
            q(
                "What daily-motion flag does the app give your Mercury?",
                if (c.p("Mercury").retro) "Retrograde" else "Direct",
                listOf("Retrograde", "Direct"),
                "The engine compares geocentric longitude one day apart. This estimates motion and can differ near stations; retrograde is not automatically weak.",
            )
            yesNo(
                "Is your Mercury within this app's combustion threshold?",
                c.p("Mercury").combust,
                "Mercury's chosen limit is 14° from Sun; your angular separation is ${separation(c.p("Mercury").longitude,c.p("Sun").longitude).fmt()}°. Combustion and sign dignity are different indicators.",
            )
            planetHouse("Saturn")
            q(
                "What basic dignity label does your Jupiter have?",
                c.p("Jupiter").dignity,
                listOf("Exalted", "Debilitated", "Own sign", "Ordinary sign placement"),
                "Jupiter is in ${signs[c.p("Jupiter").sign]}. Its exaltation is Cancer, debilitation Capricorn and own signs Sagittarius/Pisces.",
            )
            fact(
                "How should a retrograde or combust label be used?",
                "Combine it with lordship, associations and context",
                "Treat it as evil",
                "Ignore the rest of the chart",
                "Predict a fixed loss",
                why =
                    "Strength has several dimensions. A single motion or visibility flag cannot justify a frightening verdict.",
            )
        }
        7 -> {
            house(
                "Which natal house receives your Jupiter's fifth aspect?",
                (c.p("Jupiter").house + 3) % 12 + 1,
                "Count Jupiter's occupied house as one; add four house intervals for its fifth aspect.",
            )
            house(
                "Which natal house receives your Saturn's third aspect?",
                (c.p("Saturn").house + 1) % 12 + 1,
                "Saturn's third aspect advances two house intervals, counted inclusively.",
            )
            house(
                "Which natal house receives your Mars's eighth aspect?",
                (c.p("Mars").house + 6) % 12 + 1,
                "Mars's eighth aspect advances seven house intervals; wrap after house twelve.",
            )
            yesNo(
                "Are your Sun and Mercury conjunct under the app's sign-based definition?",
                c.p("Sun").sign == c.p("Mercury").sign,
                "Sun: ${signs[c.p("Sun").sign]}; Mercury: ${signs[c.p("Mercury").sign]}. Same sign meets this conjunction definition; exact separation still matters.",
            )
            fact(
                "Which aspect is shared by all classical planets?",
                "Seventh",
                "Third",
                "Fifth",
                "Eighth",
                why =
                    "All have a seventh aspect. Mars, Jupiter and Saturn have additional specified aspects.",
            )
        }
        8 -> {
            val ys = yogas(c)
            yesNo(
                "Does your chart meet the app's Gajakesari definition?",
                ys.first { it.name == "Gajakesari" }.present,
                ys.first { it.name == "Gajakesari" }.verify,
            )
            yesNo(
                "Does your chart meet the app's Budha-Aditya definition?",
                ys.first { it.name == "Budha-Aditya" }.present,
                ys.first { it.name == "Budha-Aditya" }.verify,
            )
            planetHouse("Jupiter")
            fact(
                "What must follow an initial Kemadruma check?",
                "Check cancellations and wider support",
                "Frighten the client",
                "Guarantee isolation",
                "Sell a gemstone",
                why =
                    "Empty neighboring signs are only the initial test. Explain cancellations and never infer inevitable hardship from a label.",
            )
            fact(
                "When is a yoga's timing considered?",
                "In its planets' dashas, with strength and context",
                "Every day equally",
                "Only on a birthday",
                "Without natal support",
                why =
                    "A named combination needs strength, relevant period activation and wider confirmation. It is not magic or a guaranteed event.",
            )
        }
        9 -> {
            val md = Engine.vimshottari(c).first()
            val ad = Engine.antardashas(md).first { it.active(c.birth.instant) }
            q(
                "Which Mahadasha is running at your birth?",
                md.lord,
                dashaLords,
                "Moon in ${nakshatras[c.moon.nak]} sets ${md.lord}; only the untraversed fraction remains after birth.",
            )
            q(
                "Which Antardasha is running at your birth?",
                ad.lord,
                dashaLords,
                "Subperiods are anchored at the major period's original start, often before birth; the active sublord at your birth is ${ad.lord}.",
            )
            q(
                "How many full years belong to your birth Mahadasha lord ${md.lord}?",
                dashaYears[dashaLords.indexOf(md.lord)].toString(),
                dashaYears.distinct().map { it.toString() },
                "The full ${md.lord} period has ${dashaYears[dashaLords.indexOf(md.lord)]} years. Its balance at birth is shortened by Moon's traversed fraction.",
            )
            fact(
                "What determines the first Mahadasha balance?",
                "Moon's untraversed nakshatra fraction",
                "The person's name",
                "The occupied house alone",
                "The calendar month",
                why =
                    "The remaining fraction of Moon's nakshatra multiplies the lord's full duration. Subperiod anchoring must preserve the elapsed portion.",
            )
            lordHouse(10)
        }
        10 -> {
            q(
                "Who is your seven-karaka Atmakaraka?",
                c.atmakaraka.name,
                names.take(7),
                "Among Sun through Saturn, ${c.atmakaraka.name} has the highest degree-in-sign: ${c.atmakaraka.degree.fmt()}°. Rahu is excluded in this convention.",
            )
            sign(
                "What natal sign holds your Atmakaraka?",
                c.atmakaraka.sign,
                "${c.atmakaraka.name} is in ${signs[c.atmakaraka.sign]}; this is an interpretive indicator, not spiritual rank.",
            )
            fact(
                "What is ranked in the app's seven-karaka scheme?",
                "Degree traversed within the sign",
                "Absolute longitude only",
                "House number",
                "Planet mass",
                why =
                    "Degrees within each sign are compared for Sun through Saturn. A near-tie deserves a precision check.",
            )
            fact(
                "Which graha is excluded from this seven-karaka ranking?",
                "Rahu",
                "Sun",
                "Moon",
                "Saturn",
                why =
                    "The app uses the seven visible grahas and does not silently mix the eight-karaka convention.",
            )
            sign(
                "Which sign starts your introductory Chara table?",
                c.asc.sign,
                "The implemented introductory Chara table starts at Lagna, ${signs[c.asc.sign]}, with an explicit odd/even direction rule.",
            )
        }
        11 -> {
            sign(
                "What is your Moon's D9 sign?",
                c.moon.d9,
                "D9 maps nine 3°20′ portions with movable/fixed/dual starting rules; your Moon maps to ${signs[c.moon.d9]}.",
            )
            sign(
                "What is your D10 Lagna sign?",
                c.asc.d10,
                "Ten 3° portions, odd signs from themselves and even signs from the ninth, put your rising degree in ${signs[c.asc.d10]} D10.",
            )
            yesNo(
                "Is your Moon Vargottama?",
                c.moon.vargottama,
                "D1 Moon: ${signs[c.moon.sign]}; D9 Moon: ${signs[c.moon.d9]}. The signs must match; this is repetition, not guaranteed success.",
            )
            sign(
                "What is your D9 Lagna sign?",
                c.asc.d9,
                "Your rising degree maps to ${signs[c.asc.d9]} in D9. Vargas use their own Lagna and houses.",
            )
            fact(
                "Why be especially cautious with D60 Lagna?",
                "It is highly sensitive to birth-time error",
                "It ignores birth time",
                "It guarantees a profession",
                "It replaces D1",
                why =
                    "D60 uses half-degree portions. Approximate or rounded birth times cannot support minute-sensitive claims.",
            )
        }
        12 -> {
            sign(
                "What natal Moon sign anchors your Sade Sati reference?",
                c.moon.sign,
                "Sade Sati is counted from natal Moon, here ${signs[c.moon.sign]}; it is not a personalized threat.",
            )
            house(
                "Which natal house receives your Saturn's tenth aspect?",
                (c.p("Saturn").house + 8) % 12 + 1,
                "Count inclusively from natal Saturn in house ${c.p("Saturn").house}; this is a natal aspect exercise, not today's transit.",
            )
            fact(
                "Which planets form the taught double-transit check?",
                "Saturn AND Jupiter",
                "Sun AND Venus",
                "Moon only",
                "Rahu only",
                why =
                    "Look for Saturn and Jupiter both occupying/aspecting a relevant house, then confirm natal and dasha support.",
            )
            fact(
                "Which positions define Sade Sati?",
                "Saturn 12th, 1st and 2nd from natal Moon",
                "Jupiter in the tenth",
                "Sun in every sign",
                "Rahu in the seventh",
                why =
                    "This broad recurring cycle is contextualized without fear. It does not guarantee distress.",
            )
            fact(
                "How should transits be used?",
                "Corroborate natal promise and both dashas",
                "Replace the birth chart",
                "Promise an event alone",
                "Override personal choice",
                why =
                    "A transit is supporting testimony, not sufficient evidence for a specific event or a reason to ignore agency.",
            )
        }
        13 -> {
            fact(
                "What comes first in the eight-step workflow?",
                "Validate birth data and its source",
                "Select a dramatic yoga",
                "Sell a remedy",
                "Predict an outcome",
                why =
                    "Check date, time source, place and historical zone before interpretation. Numerical precision cannot repair uncertain input.",
            )
            lordHouse(10)
            lordHouse(7)
            both()
            fact(
                "What belongs in a research notebook?",
                "Matches, misses, uncertainties and dated evidence",
                "Only successful predictions",
                "Only impressive yoga names",
                "No original assumptions",
                why =
                    "Record what was known beforehand and preserve contradictory evidence to reduce hindsight and selective memory.",
            )
        }
    }
    check(out.size == 5)
    return out
}

fun quizScore(questions: List<QuizQuestion>, answers: List<Int>): Int? {
    require(questions.size == 5 && answers.size == 5)
    require(
        answers.indices.all { answers[it] == -1 || answers[it] in questions[it].choices.indices }
    )
    return if (answers.any { it == -1 }) null
    else answers.indices.count { answers[it] == questions[it].answer }
}

fun lessonFocus(c: Chart, index: Int): LessonFocus {
    lessonKey(index)
    val pairs =
        when (index) {
            0 -> setOf(1, 9, 10) to setOf("Lagna", c.lord(1).name)
            1 -> setOf(1, 4, 5, 7, 9, 10) to setOf("Lagna", c.lord(10).name)
            2 ->
                setOf(c.p("Mercury").house, c.p("Jupiter").house, c.p("Sun").house) to
                    setOf("Sun", "Mercury", "Jupiter")
            3 -> setOf(1, c.p("Sun").house, c.moon.house) to setOf("Lagna", "Sun", "Moon")
            4 -> setOf(10, c.lord(10).house) to setOf(c.lord(10).name)
            5 -> setOf(c.moon.house) to setOf("Moon")
            6 ->
                setOf(c.p("Mercury").house, c.p("Sun").house, c.p("Saturn").house) to
                    setOf("Mercury", "Sun", "Saturn")
            7 ->
                (1..12)
                    .filter {
                        aspects(c.p("Jupiter"), it) ||
                            aspects(c.p("Saturn"), it) ||
                            aspects(c.p("Mars"), it)
                    }
                    .toSet() to setOf("Mars", "Jupiter", "Saturn")
            8 ->
                setOf(c.moon.house, c.p("Jupiter").house, c.p("Sun").house, c.p("Mercury").house) to
                    setOf("Moon", "Jupiter", "Sun", "Mercury")
            9 ->
                setOf(c.moon.house, c.lord(10).house) to
                    setOf("Moon", c.moon.nakLord, c.lord(10).name)
            10 -> setOf(c.atmakaraka.house, 1) to setOf(c.atmakaraka.name, "Lagna")
            11 -> setOf(1, c.moon.house) to setOf("Lagna", "Moon")
            12 ->
                setOf(c.moon.house, c.p("Saturn").house, c.p("Jupiter").house) to
                    setOf("Moon", "Saturn", "Jupiter")
            else ->
                setOf(1, 7, 10, c.lord(7).house, c.lord(10).house) to
                    setOf("Lagna", c.lord(7).name, c.lord(10).name)
        }
    return LessonFocus(
        pairs.first,
        pairs.second,
        "D1 reference only. Marked houses and planets are the starting points for this lesson; divisional signs and current transits are not plotted on this mini chart. Read the chart together with the lesson and quiz explanations.",
    )
}
