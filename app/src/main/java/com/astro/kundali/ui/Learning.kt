package com.astro.kundali.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.astro.kundali.astro.*
import com.astro.kundali.content.*

@Composable
internal fun LearningProgress(scores: Map<String, Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "${scores.size} of ${lessons.size} lessons completed",
            style = MaterialTheme.typography.titleMedium,
        )
        LinearProgressIndicator(
            progress = { scores.size.toFloat() / lessons.size },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "Answer all five questions to complete a lesson. Best quiz scores are saved on this device, across charts and app restarts; retakes can improve them. No account or sync.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
internal fun LessonMiniChart(c: Chart, index: Int) {
    val focus = remember(c, index) { lessonFocus(c, index) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Chart of the lesson • your D1", style = MaterialTheme.typography.titleMedium)
            NorthIndianDiagram(
                c.varga(1),
                label = { it.take(2) },
                highlightedHouses = focus.houses,
                highlightedPlanets = focus.planets,
                showHouseLabels = true,
                modifier = Modifier.widthIn(max = 360.dp),
            )
            Text(
                "H = house; S = sign number. * and bold text mark lesson focus; shaded circles mark houses.",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "Houses: " + focus.houses.sorted().joinToString { "$it (${houseTitles[it-1]})" },
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "Planets/points: " +
                    focus.planets.sorted().joinToString { "$it in house ${c.p(it).house}" },
                style = MaterialTheme.typography.bodySmall,
            )
            Text(focus.explanation, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun LessonQuiz(c: Chart, index: Int, best: Int?, onCompleted: (Int) -> Unit) {
    val questions = remember(c, index) { lessonQuiz(c, index) }
    var answers by rememberSaveable(c.birth, index) { mutableStateOf(List(5) { -1 }) }
    val score = quizScore(questions, answers)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Five-question quiz", style = MaterialTheme.typography.headlineSmall)
        Text(
            "${answers.count { it>=0 }} of 5 answered${best?.let { " • saved best $it/5" } ?: ""}. Each first choice is final for this attempt; feedback explains the rule. Retake to practise."
        )
        questions.forEachIndexed { questionIndex, q ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${questionIndex+1}. ${q.prompt}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    q.choices.forEachIndexed { choiceIndex, choice ->
                        OutlinedButton(
                            enabled = answers[questionIndex] == -1,
                            onClick = {
                                if (answers[questionIndex] == -1) {
                                    val next =
                                        answers.toMutableList().also {
                                            it[questionIndex] = choiceIndex
                                        }
                                    answers = next
                                    quizScore(questions, next)?.let(onCompleted)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                "${if(answers[questionIndex]==choiceIndex) "Your choice: " else ""}$choice"
                            )
                        }
                    }
                    if (answers[questionIndex] >= 0) {
                        val correct = answers[questionIndex] == q.answer
                        Text(
                            if (correct) "Correct"
                            else "Not quite. Correct answer: ${q.choices[q.answer]}",
                            style = MaterialTheme.typography.labelLarge,
                            color =
                                if (correct) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error,
                        )
                        Text(q.explanation)
                    }
                }
            }
        }
        if (score != null)
            Text(
                "Lesson completed • this attempt $score/5. Best score saved locally: ${maxOf(best ?: 0,score)}/5. Use explanations to check your reasoning, including any uncertainty.",
                style = MaterialTheme.typography.titleMedium,
            )
        OutlinedButton(onClick = { answers = List(5) { -1 } }) {
            Text(if (score == null) "Restart quiz" else "Retake quiz")
        }
    }
}
