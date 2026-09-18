package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReadingEntity
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YesNoOracleScreen(
    onSaveReading: (ReadingEntity) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var enableChance by remember { mutableStateOf(false) }
    var chanceSliderValue by remember { mutableStateOf(50f) } // 0.000 to 100.000
    var resultOutcome by remember { mutableStateOf<Pair<String, Double>?>(null) }
    var saved by remember { mutableStateOf(false) }

    // Format chance to 3 decimal places (.000%)
    val formattedChance = String.format(Locale.US, "%.3f", chanceSliderValue)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Yes/No Oracle",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ask any question of fate and consult the mystic pendulum. Optionally specify a precise probability threshold up to .000%.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = question,
                        onValueChange = {
                            question = it
                            saved = false
                        },
                        label = { Text("Your Question for the Oracle") },
                        placeholder = { Text("e.g., Should I embark on this new journey?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Percent,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Chance Percentage Option",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Precise odds weighting up to .000%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Switch(
                            checked = enableChance,
                            onCheckedChange = { enableChance = it }
                        )
                    }

                    if (enableChance) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Target Success Probability", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "$formattedChance%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = chanceSliderValue,
                                onValueChange = { chanceSliderValue = it },
                                valueRange = 0f..100f,
                                steps = 10000 // allows granular adjustment
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "0.000%", style = MaterialTheme.typography.labelSmall)
                                Text(text = "50.000%", style = MaterialTheme.typography.labelSmall)
                                Text(text = "100.000%", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val roll = Random.nextDouble(0.0, 100.0)
                            val outcome = if (enableChance) {
                                if (roll <= chanceSliderValue) "YES" else "NO"
                            } else {
                                if (Random.nextBoolean()) "YES" else "NO"
                            }
                            resultOutcome = outcome to roll
                            saved = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Help, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Consult the Oracle")
                    }
                }
            }
        }

        if (resultOutcome != null) {
            item {
                val (outcome, roll) = resultOutcome!!
                val isYes = outcome == "YES"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isYes) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (question.isNotBlank()) "Question: \"$question\"" else "Oracle Decree",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (isYes) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )

                        Text(
                            text = outcome,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isYes) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )

                        if (enableChance) {
                            val formattedRoll = String.format(Locale.US, "%.3f", roll)
                            Text(
                                text = "Configured Threshold: $formattedChance% | Fate Roll: $formattedRoll%",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isYes) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (!saved) {
                                    val qTitle = if (question.isNotBlank()) question else "Yes/No Consultation"
                                    val desc = buildString {
                                        append("Answer: $outcome\n")
                                        if (enableChance) {
                                            append("Chance Threshold: $formattedChance%\n")
                                            append("Fate Roll: ${String.format(Locale.US, "%.3f", roll)}%")
                                        } else {
                                            append("Standard 50/50 Oracle Consultation")
                                        }
                                    }
                                    onSaveReading(
                                        ReadingEntity(
                                            type = "YES_NO",
                                            title = qTitle,
                                            description = desc
                                        )
                                    )
                                    saved = true
                                }
                            },
                            enabled = !saved,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (saved) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(if (saved) "Saved to Journal ✓" else "Save Reading to Journal")
                        }
                    }
                }
            }
        }
    }
}
