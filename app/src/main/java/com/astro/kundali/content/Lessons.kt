package com.astro.kundali.content

import com.astro.kundali.astro.*

data class Lesson(val title: String, val text: String, val house: Int)

val lessons =
    listOf(
        Lesson(
            "1. The K. N. Rao approach",
            """Begin with a question that can be checked. A chart is a structured set of symbols, while a person's life is evidence with dates, uncertainty and context. The approach taught here is inspired by K. N. Rao's emphasis on research, disciplined synthesis and humane consultation. It does not claim his endorsement or replace study with a teacher.

Record the source of the birth time before interpreting anything. Hospital records, family memory and rounded clock times deserve different confidence. Lagna and divisional charts can change quickly. Precise numerical output cannot repair uncertain input. Ask what changes if the time moves several minutes.

Use Parashari houses, lordship and aspects alongside Jaimini karakas and sign periods. Seek convergence rather than selecting whichever technique gives an attractive answer. Confirm a timing hypothesis in Vimshottari and Chara, then consider transits. The simplified Chara scheme here is an introductory exercise. Desha-Kala-Patra means place, time and individual circumstances: the same educational indication can have different expressions across generations, societies and opportunities.

Study practice: treat interpretation as a hypothesis to verify across many charts. Keep astronomical placement separate from traditional rule and from actual events. Consider Desha-Kala-Patra: place, historical time and individual circumstances. Seek agreement between Parashari and Jaimini evidence and confirm timing in both Vimshottari and Chara. If testimony disagrees, say so. Birth-time uncertainty may limit what can responsibly be addressed. Never frighten a client, predict death or sell remedies and gemstones. Learning should leave a person better informed and free to choose. For this lesson, write your reasoning before looking back at the app's generated reading. Count signs manually and compare conventions before deciding that a discrepancy is an error.""",
            1,
        ),
        Lesson(
            "2. Lagna and the house groups",
            """Lagna is the ecliptic degree rising at the eastern horizon. Its sidereal sign becomes the whole first house in this app; subsequent signs become houses two through twelve. First check that another program uses the same ayanamsa and house convention before comparing results. A house number and a sign number are different things.

Kendras are houses one, four, seven and ten: self, home, partnership and public work. Trikonas are one, five and nine, often used for identity, learning and meaning. The first belongs to both groups. These categories help identify relationships between house lords; they are not a hierarchy of human value.

Dusthanas, usually six, eight and twelve, involve friction, vulnerability, change and release, but also service, research and contemplation. Upachayas are three, six, ten and eleven, fields where effort and experience can develop capacity. Find each group's signs, identify their classical rulers, and locate those rulers. A tenth lord in the fourth links public activity and private foundations; it does not name a particular profession.

Study practice: treat interpretation as a hypothesis to verify across many charts. Keep astronomical placement separate from traditional rule and from actual events. Consider Desha-Kala-Patra: place, historical time and individual circumstances. Seek agreement between Parashari and Jaimini evidence and confirm timing in both Vimshottari and Chara. If testimony disagrees, say so. Birth-time uncertainty may limit what can responsibly be addressed. Never frighten a client, predict death or sell remedies and gemstones. Learning should leave a person better informed and free to choose. For this lesson, write your reasoning before looking back at the app's generated reading. Count signs manually and compare conventions before deciding that a discrepancy is an error.""",
            2,
        ),
        Lesson(
            "3. Nine grahas and karakatwas",
            """A graha is a symbolic factor in Jyotish, not necessarily a physical planet. Sun and Moon are luminaries; Rahu and Ketu are lunar nodes. The seven visible grahas have classical sign ownership. The app assigns no independent house ownership to nodes; investigate their sign lord and associations instead.

Sun signifies responsibility, identity and authority. Moon concerns mind, care and rhythms. Mars suggests initiative, tools and conflict. Mercury concerns language, calculation and exchange. Jupiter relates to judgment, teaching and growth. Venus concerns agreements, relationships and art. Saturn brings labor, limits, endurance and time. Rahu can indicate unfamiliar experience; Ketu suggests detachment and investigation. These are starting points, not personality diagnoses.

Separate three layers: natural meaning, houses owned and house occupied. Mercury naturally represents learning, but its particular house ownership carries specific topics to its location. A naturally benefic planet may own a difficult house. Conjunctions, aspects and dignity qualify expression. A dasha brings the planet's network into a timing discussion; it cannot manufacture every event associated with its vocabulary.

Study practice: treat interpretation as a hypothesis to verify across many charts. Keep astronomical placement separate from traditional rule and from actual events. Consider Desha-Kala-Patra: place, historical time and individual circumstances. Seek agreement between Parashari and Jaimini evidence and confirm timing in both Vimshottari and Chara. If testimony disagrees, say so. Birth-time uncertainty may limit what can responsibly be addressed. Never frighten a client, predict death or sell remedies and gemstones. Learning should leave a person better informed and free to choose. For this lesson, write your reasoning before looking back at the app's generated reading. Count signs manually and compare conventions before deciding that a discrepancy is an error.""",
            3,
        ),
        Lesson(
            "4. Twelve rashis and dignity",
            """The twelve rashis divide the ecliptic into equal thirty-degree signs. Aries, Leo and Sagittarius are fire; Taurus, Virgo and Capricorn earth; Gemini, Libra and Aquarius air; Cancer, Scorpio and Pisces water. Elements are interpretive categories, not measurements of human worth.

Movable signs are Aries, Cancer, Libra and Capricorn. Fixed signs are Taurus, Leo, Scorpio and Aquarius. Dual signs are Gemini, Virgo, Sagittarius and Pisces. These qualities matter in varga construction. Classical rulership connects Sun with Leo, Moon with Cancer, Mars with Aries and Scorpio, Mercury with Gemini and Virgo, Jupiter with Sagittarius and Pisces, Venus with Taurus and Libra, and Saturn with Capricorn and Aquarius.

Exaltations are Sun in Aries, Moon in Taurus, Mars in Capricorn, Mercury in Virgo, Jupiter in Cancer, Venus in Pisces and Saturn in Libra. Debilitation is opposite. Neecha Bhanga describes modification of debility under specified conditions. Investigate the dispositor and exaltation-sign lord in kendras, but learn the full traditional conditions before declaring cancellation. The app reports basic dignity, not every cancellation or a guaranteed Raja yoga.

Study practice: treat interpretation as a hypothesis to verify across many charts. Keep astronomical placement separate from traditional rule and from actual events. Consider Desha-Kala-Patra: place, historical time and individual circumstances. Seek agreement between Parashari and Jaimini evidence and confirm timing in both Vimshottari and Chara. If testimony disagrees, say so. Birth-time uncertainty may limit what can responsibly be addressed. Never frighten a client, predict death or sell remedies and gemstones. Learning should leave a person better informed and free to choose. For this lesson, write your reasoning before looking back at the app's generated reading. Count signs manually and compare conventions before deciding that a discrepancy is an error.""",
            4,
        ),
        Lesson(
            "5. Twelve bhavas and the checklist",
            """A bhava organizes a field of experience. First concerns self; second resources and speech; third initiative and siblings; fourth home; fifth learning and children; sixth service and challenges. Seventh concerns partnership; eighth change and shared resources; ninth teachers and meaning; tenth work; eleventh gains and networks; twelfth retreat and release. Each has multiple expressions.

Use four steps. First name the house and sign. Second describe occupants through their natural meanings and owned houses. Third locate the house lord and assess dignity, associations and aspects. Fourth compare the relevant karaka, the natural significator. This order reduces the temptation to choose one dramatic placement.

An empty house is not an absent life topic: its lord still exists and participates in periods. Many occupants do not mean a topic dominates every year. If evidence conflicts, preserve the conflict. A supported lord and troubled karaka may suggest mixed testimony or the limits of interpretation. The generated paragraphs intentionally expose ingredients so you can challenge the reasoning, compare actual circumstances and expand it with study.

Study practice: treat interpretation as a hypothesis to verify across many charts. Keep astronomical placement separate from traditional rule and from actual events. Consider Desha-Kala-Patra: place, historical time and individual circumstances. Seek agreement between Parashari and Jaimini evidence and confirm timing in both Vimshottari and Chara. If testimony disagrees, say so. Birth-time uncertainty may limit what can responsibly be addressed. Never frighten a client, predict death or sell remedies and gemstones. Learning should leave a person better informed and free to choose. For this lesson, write your reasoning before looking back at the app's generated reading. Count signs manually and compare conventions before deciding that a discrepancy is an error.""",
            5,
        ),
        Lesson(
            "6. Moon, nakshatra and pada",
            """Nakshatras divide the zodiac into twenty-seven equal segments of thirteen degrees twenty minutes, beginning with Ashwini at zero sidereal Aries and ending with Revati. Their boundaries often fall inside a rashi. Each has four padas of three degrees twenty minutes, making one hundred eight divisions around the zodiac.

Divide normalized sidereal Moon longitude by thirteen and one third. The integer part gives the zero-based nakshatra index. Divide the remainder by three and one third, adding one for the pada. Near a boundary, small input or ephemeris differences can change the label; record that uncertainty.

The Vimshottari sequence is Ketu, Venus, Sun, Moon, Mars, Rahu, Jupiter, Saturn and Mercury, repeating three times across the nakshatras. Moon's segment determines the starting lord; the untraversed fraction determines the period balance. The India sample has Moon in Pushya and therefore Saturn at birth. Checking this result tests the chain from local time through UTC, tropical longitude, ayanamsa and segmentation. It does not prove uniform historical accuracy for every chart.

Study practice: treat interpretation as a hypothesis to verify across many charts. Keep astronomical placement separate from traditional rule and from actual events. Consider Desha-Kala-Patra: place, historical time and individual circumstances. Seek agreement between Parashari and Jaimini evidence and confirm timing in both Vimshottari and Chara. If testimony disagrees, say so. Birth-time uncertainty may limit what can responsibly be addressed. Never frighten a client, predict death or sell remedies and gemstones. Learning should leave a person better informed and free to choose. For this lesson, write your reasoning before looking back at the app's generated reading. Count signs manually and compare conventions before deciding that a discrepancy is an error.""",
            6,
        ),
        Lesson(
            "7. Strength, combustion and retrogression",
            """Strength is not one score. Dignity, angularity, associations, divisional confirmation and functional role answer different questions. This app reports basic dignity rather than pretending to provide complete Shadbala. An exalted planet can carry demanding topics; a debilitated planet need not deny every constructive expression.

Combustion describes proximity to Sun under a chosen angular threshold. We use Moon twelve degrees, Mars seventeen, Mercury fourteen, Venus ten, Jupiter eleven and Saturn fifteen. Traditions can distinguish limits by motion and add other conditions. Read the label as a transparent convention, not the final word on strength.

Retrogression is apparent backward geocentric motion. Here it is determined by comparing longitudes one day apart, wrapping across zero Aries. Near a station the daily estimate may differ from instantaneous motion. Mean nodes ordinarily move backward. Retrograde does not mean evil or universally weak. A combust planet still owns houses, receives support and participates in periods. Keep separate columns for astronomical position, traditional rule and interpretation, then compare multiple indicators before writing any conclusion.

Study practice: treat interpretation as a hypothesis to verify across many charts. Keep astronomical placement separate from traditional rule and from actual events. Consider Desha-Kala-Patra: place, historical time and individual circumstances. Seek agreement between Parashari and Jaimini evidence and confirm timing in both Vimshottari and Chara. If testimony disagrees, say so. Birth-time uncertainty may limit what can responsibly be addressed. Never frighten a client, predict death or sell remedies and gemstones. Learning should leave a person better informed and free to choose. For this lesson, write your reasoning before looking back at the app's generated reading. Count signs manually and compare conventions before deciding that a discrepancy is an error.""",
            7,
        ),
        Lesson(
            "8. Aspects and conjunctions",
            """A conjunction here means two grahas occupy the same sidereal sign. Exact separation still matters: a wide same-sign conjunction is not identical to a close one. Degree-based strength and planetary war need additional rules beyond these introductory checks.

Every classical planet has a seventh aspect, counted inclusively from its occupied house. Mars additionally aspects fourth and eighth; Jupiter fifth and ninth; Saturn third and tenth. From house two, Jupiter's fifth aspect reaches six, seventh reaches eight and ninth reaches ten. Practice counting rather than memorizing disconnected destinations.

Occupation counts as contact in the double-transit check. Mutual seventh aspect means planets oppose across six sign intervals. Special aspects need not be mutual: Saturn's third aspect need not receive the other planet's seventh. Node aspects vary by tradition; this app assigns no special node aspects. It also keeps Jaimini rashi aspects separate from Parashari yoga rules. Draw arrows, name each rule and identify which owned-house topics connect. Contact alone does not specify an event, its quality or its date.

Study practice: treat interpretation as a hypothesis to verify across many charts. Keep astronomical placement separate from traditional rule and from actual events. Consider Desha-Kala-Patra: place, historical time and individual circumstances. Seek agreement between Parashari and Jaimini evidence and confirm timing in both Vimshottari and Chara. If testimony disagrees, say so. Birth-time uncertainty may limit what can responsibly be addressed. Never frighten a client, predict death or sell remedies and gemstones. Learning should leave a person better informed and free to choose. For this lesson, write your reasoning before looking back at the app's generated reading. Count signs manually and compare conventions before deciding that a discrepancy is an error.""",
            8,
        ),
        Lesson(
            "9. Yogas: combinations, not magic",
            """A yoga is a defined combination. Its name organizes a pattern, but does not prove a promised result. State the definition first, since authors may add different qualifications, cancellations or strength conditions. A long list of impressive names is not a careful reading.

Gajakesari places Jupiter in a kendra from Moon. Budha-Aditya places Sun with Mercury. Chandra-Mangala links Moon and Mars by conjunction or mutual seventh aspect here. Raja checks connect distinct kendra lords with fifth or ninth lords; Dhana checks connect distinct lords among two, five, nine and eleven. The detector shows ingredients so you can verify each.

Kemadruma requires special care: empty neighboring signs around Moon are only an initial test. Cancellations and support can change interpretation. The app explains its cancellation subset and excludes Sun and nodes from the adjacent-planet test. Amala checks a benefic in tenth from Lagna or Moon. Mercury's status needs association-based qualification. Inspect all participants' lordship, strength and vargas, then their periods. Absence of a named yoga never implies an unsuccessful life.

Study practice: treat interpretation as a hypothesis to verify across many charts. Keep astronomical placement separate from traditional rule and from actual events. Consider Desha-Kala-Patra: place, historical time and individual circumstances. Seek agreement between Parashari and Jaimini evidence and confirm timing in both Vimshottari and Chara. If testimony disagrees, say so. Birth-time uncertainty may limit what can responsibly be addressed. Never frighten a client, predict death or sell remedies and gemstones. Learning should leave a person better informed and free to choose. For this lesson, write your reasoning before looking back at the app's generated reading. Count signs manually and compare conventions before deciding that a discrepancy is an error.""",
            9,
        ),
        Lesson(
            "10. Vimshottari and past events",
            """Vimshottari is a one-hundred-twenty-year cycle with unequal periods. Ketu has seven years, Venus twenty, Sun six, Moon ten, Mars seven, Rahu eighteen, Jupiter sixteen, Saturn nineteen and Mercury seventeen. The starting point comes from Moon's nakshatra and the fraction traversed at birth.

Each Mahadasha contains Antardashas in the cyclic sequence beginning with its own lord. A subperiod length equals major-period years multiplied by sublord years divided by one hundred twenty. The birth period is already in progress. This app reconstructs its original start before birth so subperiod dates retain their correct position.

Judge natural meaning, owned houses, occupation, associations and strength for both lords. Examine their relationship rather than assuming Venus always means marriage or Saturn loss. Test dated past education, moves and work changes before considering future hypotheses. Record approximate dates and competing explanations. Avoid changing a birth time to fit one favored event. Seek agreement with Chara before supportive transits. A research notebook should preserve misses and uneventful periods as carefully as apparent matches.

Study practice: treat interpretation as a hypothesis to verify across many charts. Keep astronomical placement separate from traditional rule and from actual events. Consider Desha-Kala-Patra: place, historical time and individual circumstances. Seek agreement between Parashari and Jaimini evidence and confirm timing in both Vimshottari and Chara. If testimony disagrees, say so. Birth-time uncertainty may limit what can responsibly be addressed. Never frighten a client, predict death or sell remedies and gemstones. Learning should leave a person better informed and free to choose. For this lesson, write your reasoning before looking back at the app's generated reading. Count signs manually and compare conventions before deciding that a discrepancy is an error.""",
            10,
        ),
        Lesson(
            "11. Jaimini karakas and Chara dasha",
            """The introductory seven-karaka scheme ranks Sun through Saturn by degree traversed within their current sign. The highest is Atmakaraka. This app excludes Rahu, explicitly distinguishing seven- and eight-karaka systems. Minutes and seconds matter when degrees are close; near-ties deserve a precision check.

Atmakaraka is an interpretive indicator, not a measure of spiritual rank. Study sign, house, associations and D9 while avoiding grand destiny claims. No placement establishes moral superiority or removes personal agency. Connect symbolic themes to observations cautiously.

Chara is sign-based timing with substantial methodological variation. Here an introductory Rao-style exercise begins at Lagna, proceeds forward for odd-numbered signs and backward for even signs, and assigns the directional distance to the sign lord. A lord in its own sign gives twelve years. Advanced methods have additional rules requiring separate study. A sign period invites examination of sign, lord and Jaimini relationships; do not silently substitute Parashari aspects for all Jaimini techniques. Compare its testimony with Vimshottari, record disagreements, and avoid claiming that this simplified table exhausts either tradition.

Study practice: treat interpretation as a hypothesis to verify across many charts. Keep astronomical placement separate from traditional rule and from actual events. Consider Desha-Kala-Patra: place, historical time and individual circumstances. Seek agreement between Parashari and Jaimini evidence and confirm timing in both Vimshottari and Chara. If testimony disagrees, say so. Birth-time uncertainty may limit what can responsibly be addressed. Never frighten a client, predict death or sell remedies and gemstones. Learning should leave a person better informed and free to choose. For this lesson, write your reasoning before looking back at the app's generated reading. Count signs manually and compare conventions before deciding that a discrepancy is an error.""",
            11,
        ),
        Lesson(
            "12. Vargas: D9, D10 and precision",
            """Vargas divide signs into smaller portions mapped into a divisional zodiac. They refine topics supported in the main chart, rather than supplying independent answers from which to select a preferred outcome. Establish the D1 framework first, including reliable input and explicit conventions.

Navamsa, D9, divides each sign into nine portions of three degrees twenty minutes. Movable signs begin from themselves, fixed signs from their ninth sign and dual signs from their fifth. Subsequent portions proceed zodiacally. The app computes D9 for all grahas and Lagna. Vargottama means the same sign in D1 and D9, not an automatic guarantee of ease.

Dashamsa, D10, uses ten three-degree portions and is studied for public work. A common convention begins odd signs from themselves, even signs from the ninth. D10 is displayed alongside D1 and D9 on the Kundali tab. D60 uses sixty half-degree portions and is highly time-sensitive, especially its ascendant. Minute-accurate clock records can still contain rounding error. Compare nearby times and document uncertainty instead of using sensitive vargas to justify confident claims from approximate inputs.

Study practice: treat interpretation as a hypothesis to verify across many charts. Keep astronomical placement separate from traditional rule and from actual events. Consider Desha-Kala-Patra: place, historical time and individual circumstances. Seek agreement between Parashari and Jaimini evidence and confirm timing in both Vimshottari and Chara. If testimony disagrees, say so. Birth-time uncertainty may limit what can responsibly be addressed. Never frighten a client, predict death or sell remedies and gemstones. Learning should leave a person better informed and free to choose. For this lesson, write your reasoning before looking back at the app's generated reading. Count signs manually and compare conventions before deciding that a discrepancy is an error.""",
            12,
        ),
        Lesson(
            "13. Transits and Sade Sati without fear",
            """Gochara compares the moving sky with natal reference points. The app uses the same engine and Lahiri convention for transits as for birth positions. It counts Saturn, Jupiter and Rahu from both natal Lagna and natal Moon; these reference frames answer different questions.

Sade Sati describes Saturn in the twelfth, first and second signs from natal Moon, a roughly seven-and-a-half-year cycle with variation from motion. It applies to enormous numbers of people and is not a personalized prediction of catastrophe. Never sell fear, gemstones or remedies using this label.

Double transit asks whether Saturn and Jupiter both influence a relevant house. This app includes occupation, Saturn's third, seventh and tenth aspects, and Jupiter's fifth, seventh and ninth. A highlighted house is a subject to investigate, not an event. Begin with natal support, check both timing systems, and only then consider a transit as corroboration. A career transit cannot specify an offer; a partnership transit never overrides consent. Record the observation date and cases where the same pattern produced no obvious event.

Study practice: treat interpretation as a hypothesis to verify across many charts. Keep astronomical placement separate from traditional rule and from actual events. Consider Desha-Kala-Patra: place, historical time and individual circumstances. Seek agreement between Parashari and Jaimini evidence and confirm timing in both Vimshottari and Chara. If testimony disagrees, say so. Birth-time uncertainty may limit what can responsibly be addressed. Never frighten a client, predict death or sell remedies and gemstones. Learning should leave a person better informed and free to choose. For this lesson, write your reasoning before looking back at the app's generated reading. Count signs manually and compare conventions before deciding that a discrepancy is an error.""",
            1,
        ),
        Lesson(
            "14. Reading in eight steps and ethics",
            """Use a repeatable order with traceable conclusions. First validate date, time source, coordinates and historical zone. Second inspect Lagna, its lord and Moon. Third map houses relevant to the question using the four-step checklist. Fourth examine strength, associations and repeating patterns; named yogas belong after their ingredients are understood.

Fifth compare the relevant varga, particularly D9, respecting time uncertainty. Sixth judge the running Vimshottari major and subperiod lords. Seventh seek independent Chara confirmation under explicit rules. Eighth use transits as corroboration and express a bounded hypothesis. Do not replace conflicting testimony with certainty.

Keep input sources, conventions, observations, interpretation and later outcomes in a research record. Separate original knowledge from hindsight. Study contradictory charts too. Consultation must preserve dignity and agency: never predict death, diagnose illness, judge a child's worth or pressure a relationship. Respect privacy and consent when studying another person's chart. Offline calculation protects network privacy, but screenshots and personal notes still deserve careful handling. Social conditions, historical circumstances and individual choices remain part of every responsible interpretation.

Study practice: treat interpretation as a hypothesis to verify across many charts. Keep astronomical placement separate from traditional rule and from actual events. Consider Desha-Kala-Patra: place, historical time and individual circumstances. Seek agreement between Parashari and Jaimini evidence and confirm timing in both Vimshottari and Chara. If testimony disagrees, say so. Birth-time uncertainty may limit what can responsibly be addressed. Never frighten a client, predict death or sell remedies and gemstones. Learning should leave a person better informed and free to choose. For this lesson, write your reasoning before looking back at the app's generated reading. Count signs manually and compare conventions before deciding that a discrepancy is an error.""",
            2,
        ),
    )

fun exercise(c: Chart, l: Lesson): String {
    val task =
        when (l.title.substringBefore('.').toInt()) {
            1 ->
                "Record the source and uncertainty of your birth time (${c.birth.time}, ${c.birth.place.zone}). Choose one dated life event and write what would count as a failed interpretation."
            2 ->
                "Your Lagna is ${signs[c.asc.sign]}. List signs and lords of houses 1/4/7/10 and 5/9; check that tenth lord ${c.lord(10).name} sits in house ${c.lord(10).house}."
            3 ->
                "Mercury is in house ${c.p("Mercury").house}. Find every house it owns in your Planets tab. Write three separate statements for natural meaning, ownership and occupation."
            4 ->
                "Your Sun is in ${signs[c.p("Sun").sign]}: ${c.p("Sun").dignity}. Name that sign's element and quality. Find its dispositor and distinguish basic dignity from a cancellation claim."
            5 ->
                "Apply all four steps to your seventh house: ${signs[c.signOfHouse(7)]}, lord ${c.lord(7).name} in house ${c.lord(7).house}. Compare Venus in house ${c.p("Venus").house}."
            6 ->
                "Moon is ${c.moon.longitude.fmt()}° sidereal: ${nakshatras[c.moon.nak]}, pada ${c.moon.pada}. Divide by 13⅓ and verify the remainder; explain why ${c.moon.nakLord} starts your Vimshottari."
            7 ->
                "Inspect Mercury: ${c.p("Mercury").dignity}, retrograde ${c.p("Mercury").retro}, combust ${c.p("Mercury").combust}. Check its separation from Sun and explain why these labels are different kinds of evidence."
            8 ->
                "Jupiter is in house ${c.p("Jupiter").house}. Count its fifth, seventh and ninth aspects and name the target houses. Draw Saturn's third and tenth separately."
            9 ->
                "Gajakesari is marked ${if(yogas(c).first().present)"present" else "absent"}. Count Jupiter from Moon manually and identify one strength qualification before discussing its possible expression."
            10 ->
                "Your birth Mahadasha is ${Engine.vimshottari(c).first().lord}. Explain the Moon fraction already traversed, then expand the running ${running(c)} row and compare one dated past event without changing the birth time."
            11 ->
                "Your Atmakaraka is ${c.atmakaraka.name} at ${c.atmakaraka.degree.fmt()}° within sign. Rank all seven classical planets by degree. Explain the direction and first duration of your Chara table."
            12 ->
                "Lagna D1 is ${signs[c.asc.sign]} and D9 is ${signs[c.asc.d9]}. Recalculate its ninth-part index and starting sign. Find any Vargottama planet without treating repetition as a promised result."
            13 ->
                "Open Transits and reproduce the Saturn/Jupiter double-contact houses by inclusive counting from natal Lagna ${signs[c.asc.sign]}. Name a reason why transit alone cannot promise an event."
            else ->
                "Use all eight steps on career: tenth lord ${c.lord(10).name} occupies house ${c.lord(10).house}. Record natal evidence, D9, both dashas, transit support and uncertainties as separate entries."
        }
    return task +
        "\n\nTiming context: running ${running(c)}. Preserve conflicting evidence and Desha-Kala-Patra; write one observation you can verify and one limit on your conclusion."
}
