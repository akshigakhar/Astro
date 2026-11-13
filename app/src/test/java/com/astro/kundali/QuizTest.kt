package com.astro.kundali

import com.astro.kundali.astro.*
import com.astro.kundali.content.*
import java.time.*
import org.junit.Assert.*
import org.junit.Test

class QuizTest {
    private val india = Engine.calculate(Cities.sample)
    private val toronto =
        Engine.calculate(
            Birth(
                "Learner",
                LocalDate.of(1990, 6, 10),
                LocalTime.of(9, 30),
                Cities.all.first { it.name == "Toronto" },
            )
        )

    @Test
    fun everyLessonHasFiveValidExplainedQuestionsOnDifferentCharts() {
        listOf(india, toronto).forEach { c ->
            val all =
                lessons.indices.flatMap { index ->
                    val questions = lessonQuiz(c, index)
                    assertEquals(5, questions.size)
                    assertEquals(5, questions.map { it.id }.distinct().size)
                    questions.forEach { q ->
                        assertTrue(q.prompt.isNotBlank())
                        assertTrue(q.explanation.length > 30)
                        assertTrue(q.choices.size in 2..4)
                        assertEquals(q.choices.size, q.choices.distinct().size)
                        assertTrue(q.answer in q.choices.indices)
                        assertTrue(q.choices.all { it.isNotBlank() })
                    }
                    questions
                }
            assertEquals(70, all.map { it.id }.distinct().size)
        }
    }

    @Test
    fun generatedAnswersMatchTheLearnersActualChart() {
        listOf(india, toronto).forEach { c ->
            val house = lessonQuiz(c, 4).first { it.prompt.contains("10th-house lord occupy") }
            assertEquals("House ${c.lord(10).house}", house.choices[house.answer])
            val nak = lessonQuiz(c, 5).first { it.prompt.contains("Moon's nakshatra?") }
            assertEquals(nakshatras[c.moon.nak], nak.choices[nak.answer])
            val ak = lessonQuiz(c, 10).first()
            assertEquals(c.atmakaraka.name, ak.choices[ak.answer])
            val d10 = lessonQuiz(c, 11)[1]
            assertEquals(signs[c.asc.d10], d10.choices[d10.answer])
        }
        assertNotEquals(lessonQuiz(india, 4), lessonQuiz(toronto, 4))
    }

    @Test
    fun knownSampleQuestionsHaveIndependentExpectedAnswers() {
        val houses = lessonQuiz(india, 4)
        assertEquals("Aquarius", houses[0].choices[houses[0].answer])
        assertEquals("Saturn", houses[1].choices[houses[1].answer])
        assertEquals("House 3", houses[2].choices[houses[2].answer])
        val lunar = lessonQuiz(india, 5)
        assertEquals("Pushya", lunar[0].choices[lunar[0].answer])
        assertEquals("Saturn", lunar[2].choices[lunar[2].answer])
        assertEquals("Saturn", lessonQuiz(india, 9)[0].let { it.choices[it.answer] })
    }

    @Test
    fun aspectsAreCountedInclusivelyAndAnswersAreStable() {
        val q = lessonQuiz(india, 7)
        assertEquals("House 10", q[0].choices[q[0].answer]) // Jupiter in natal sixth, fifth aspect.
        assertEquals("House 5", q[1].choices[q[1].answer]) // Saturn in third, third aspect.
        assertEquals("House 9", q[2].choices[q[2].answer]) // Mars in second, eighth aspect.
        assertEquals(q, lessonQuiz(india, 7))
    }

    @Test
    fun allQuizFeedbackCanBeScoredAndRetakenWithoutMutatingQuestions() {
        lessons.indices.forEach { index ->
            val questions = lessonQuiz(india, index)
            assertNull(quizScore(questions, List(5) { -1 }))
            assertNull(quizScore(questions, listOf(questions[0].answer, -1, -1, -1, -1)))
            assertEquals(5, quizScore(questions, questions.map { it.answer }))
            assertEquals(
                0,
                quizScore(questions, questions.map { (it.answer + 1) % it.choices.size }),
            )
            assertThrows(IllegalArgumentException::class.java) {
                quizScore(questions, List(5) { 99 })
            }
        }
    }

    @Test
    fun progressKeepsBestScoresAndCountsCompletionEvenAtZero() {
        val first = recordLessonScore(emptyMap(), 0, 0)
        assertEquals(1, first.size)
        assertEquals(0, first["lesson-1"])
        val better = recordLessonScore(first, 0, 4)
        val worse = recordLessonScore(better, 0, 2)
        assertEquals(better, worse)
        assertEquals(0, first["lesson-1"]) // Input map was not mutated.
        assertEquals(2, recordLessonScore(worse, 13, 5).size)
        assertThrows(IllegalArgumentException::class.java) { recordLessonScore(first, 0, 6) }
    }

    @Test
    fun chartFocusHasValidSpecificHousesAndPlanetsForEveryLesson() {
        listOf(india, toronto).forEach { c ->
            lessons.indices.forEach { index ->
                val focus = lessonFocus(c, index)
                assertTrue(focus.houses.isNotEmpty())
                assertTrue(focus.houses.all { it in 1..12 })
                assertTrue(focus.planets.isNotEmpty())
                assertTrue(focus.planets.all { p -> c.positions.any { it.name == p } })
                assertTrue(focus.explanation.contains("D1 reference only"))
            }
            assertEquals(setOf(c.moon.house), lessonFocus(c, 5).houses)
            assertEquals(setOf("Moon"), lessonFocus(c, 5).planets)
            assertTrue(lessonFocus(c, 4).houses.containsAll(setOf(10, c.lord(10).house)))
        }
    }

    @Test
    fun invalidLessonIdsCannotWriteOrGenerateContent() {
        listOf(-1, 14).forEach {
            assertThrows(IllegalArgumentException::class.java) { lessonQuiz(india, it) }
            assertThrows(IllegalArgumentException::class.java) { lessonFocus(india, it) }
            assertThrows(IllegalArgumentException::class.java) {
                recordLessonScore(emptyMap(), it, 1)
            }
        }
    }
}
