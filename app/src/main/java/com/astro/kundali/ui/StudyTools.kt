@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.astro.kundali.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.astro.kundali.astro.*
import com.astro.kundali.content.*
import java.time.LocalDate

@Composable
internal fun PlanetCard(c: Chart, p: Position) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                p.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (p.name != "Lagna" && p.vargottama) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text(
                        "Vargottama • D1 = D9",
                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Text(
                    "Same sign in D1 and D9: ${signs[p.sign]}. Repeated testimony, not a guarantee of success.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(planetReading(c, p))
        }
    }
}

@Composable
internal fun EventWorksheet(
    c: Chart,
    entries: List<String>,
    checked: List<String>,
    error: String?,
    onEdit: (Int, String) -> Unit,
    onCheck: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Past events check", style = MaterialTheme.typography.titleLarge)
            Text(
                "Enter three dated events, such as education, a move or a work change. Dates use the birth place's time zone, at local noon (or birth instant if later that day). Results flag same-day period changes. Chara is available within the existing introductory twelve-sign cycle; dates beyond it are marked unavailable. This is a learning worksheet, not proof of causation or a birth-time rectification tool. Entries stay in Android saved UI state for this chart; they are not included in sharing or export."
            )
            (0..2).forEach { i ->
                OutlinedTextField(
                    entries[i * 2],
                    { onEdit(i * 2, it) },
                    label = { Text("Event ${i+1} date • YYYY-MM-DD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    entries[i * 2 + 1],
                    { onEdit(i * 2 + 1, it) },
                    label = { Text("Event ${i+1} description") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = onCheck) { Text("Check 3 events") }
            if (checked.size == 6)
                (0..2).forEach { i ->
                    val result = checkEvent(c, LocalDate.parse(checked[i * 2].trim()))
                    HorizontalDivider()
                    Text(
                        "${result.date} • ${checked[i*2+1]}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(eventReading(c, result))
                }
        }
    }
}

@Composable
internal fun ExportActions(c: Chart) {
    val context = LocalContext.current
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingText by rememberSaveable { mutableStateOf("") }
    val save =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {
            uri ->
            if (uri != null) {
                runCatching {
                        val stream =
                            context.contentResolver.openOutputStream(uri, "wt")
                                ?: error("Cannot open the selected file.")
                        stream.bufferedWriter(Charsets.UTF_8).use { it.write(pendingText) }
                    }
                    .onSuccess { message = "Summary saved as plain text." }
                    .onFailure {
                        message =
                            "Could not save: ${it.localizedMessage ?: "choose another location"}"
                    }
            }
            pendingText = ""
        }
    Column(Modifier.padding(horizontal = 16.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    val send =
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Astro Kundali summary")
                            putExtra(Intent.EXTRA_TEXT, kundaliSummary(c))
                        }
                    runCatching {
                            context.startActivity(
                                Intent.createChooser(send, "Share Kundali summary")
                            )
                        }
                        .onFailure {
                            message = "No app is available to share text. Use Save .txt instead."
                        }
                }
            ) {
                Text("Share text")
            }
            OutlinedButton(
                onClick = {
                    pendingText = kundaliSummary(c)
                    runCatching { save.launch("Astro-kundali.txt") }
                        .onFailure {
                            pendingText = ""
                            message = "No document picker is available. Use Share text instead."
                        }
                }
            ) {
                Text("Save .txt")
            }
        }
        Text(
            "Includes birth details. Worksheet events are excluded. Choose a local folder for offline export; the receiving app or file provider controls any later upload.",
            style = MaterialTheme.typography.bodySmall,
        )
        message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
}
