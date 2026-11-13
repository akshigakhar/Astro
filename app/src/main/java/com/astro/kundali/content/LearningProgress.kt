package com.astro.kundali.content

/** Only completed-quiz best scores are retained; no chart data or answers. */
fun recordLessonScore(previous: Map<String, Int>, lessonIndex: Int, score: Int): Map<String, Int> {
    require(score in 0..5)
    val key = lessonKey(lessonIndex)
    return previous + (key to maxOf(previous[key] ?: 0, score))
}
