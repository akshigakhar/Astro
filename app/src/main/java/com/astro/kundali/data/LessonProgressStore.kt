package com.astro.kundali.data

import android.content.Context
import com.astro.kundali.content.*

class LessonProgressStore(context: Context) {
    private val preferences =
        context.applicationContext.getSharedPreferences(
            "learning_progress_v1",
            Context.MODE_PRIVATE,
        )

    fun load(): Map<String, Int> =
        lessons.indices
            .mapNotNull { i ->
                val key = lessonKey(i)
                if (preferences.contains(key))
                    preferences.getInt(key, 0).takeIf { it in 0..5 }?.let { key to it }
                else null
            }
            .toMap()

    fun record(index: Int, score: Int): Map<String, Int> {
        val scores = recordLessonScore(load(), index, score)
        val key = lessonKey(index)
        preferences.edit().putInt(key, scores.getValue(key)).apply()
        return scores
    }
}
