package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.GradesRepository
import com.example.ui.theme.*

@Composable
fun GradesScreen(context: Context) {
    val courses by GradesRepository.coursesFlow.collectAsState()
    val avgScore = remember(courses) { GradesRepository.calculateAveragePercent() }
    val highestScore = remember(courses) { if (courses.isEmpty()) 0.0 else courses.maxOf { it.currentGradePercent } }
    val passingCount = remember(courses) { courses.count { it.currentGradePercent >= 60.0 } }

    var showAddCourseDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // High School Academic Performance Overview Card (Percentage Focused)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = XboxDarkSurface),
                border = BorderStroke(1.5.dp, StatusPurple.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)) {
                            Text(
                                text = "ACADEMIC PORTFOLIO",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusPurple,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "High School Grades",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = XboxTextPrimary
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = StatusPurple.copy(alpha = 0.2f),
                            border = BorderStroke(1.5.dp, StatusPurple)
                        ) {
                            Text(
                                text = "${courses.size} Subjects",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusPurple,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Percentage Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = XboxDarkSurfaceVariant,
                            border = BorderStroke(1.dp, XboxNeonGreen.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (avgScore > 0.0) "$avgScore%" else "N/A",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = XboxNeonGreen
                                )
                                Text("Overall Avg", style = MaterialTheme.typography.labelSmall, color = XboxTextSecondary, fontSize = 10.sp, maxLines = 1)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = XboxDarkSurfaceVariant,
                            border = BorderStroke(1.dp, XboxOutline)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (highestScore > 0.0) "$highestScore%" else "N/A",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = StatusInfoCyan
                                )
                                Text("Top Score", style = MaterialTheme.typography.labelSmall, color = XboxTextSecondary, fontSize = 10.sp, maxLines = 1)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = XboxDarkSurfaceVariant,
                            border = BorderStroke(1.dp, XboxOutline)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$passingCount/${courses.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = StatusPurple
                                )
                                Text("Passing", style = MaterialTheme.typography.labelSmall, color = XboxTextSecondary, fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // Courses / Subjects List
            Text(
                text = "Subjects & Scores",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = XboxTextPrimary
            )

            if (courses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = XboxOutlineHighlight, modifier = Modifier.size(48.dp))
                        Text("No subjects added yet", color = XboxTextSecondary, style = MaterialTheme.typography.titleMedium)
                        Text("Tap the + button to add your school subjects and scores.", color = XboxTextMuted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
                    items(courses, key = { it.id }) { course ->
                        CourseCard(
                            course = course,
                            onDelete = {
                                GradesRepository.deleteCourse(context, course.id)
                                Toast.makeText(context, "${course.courseName} removed.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddCourseDialog = true },
            containerColor = StatusPurple,
            contentColor = XboxBlack,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Subject")
        }
    }

    if (showAddCourseDialog) {
        AddCourseDialog(
            context = context,
            onDismiss = { showAddCourseDialog = false },
            onSaved = { showAddCourseDialog = false }
        )
    }
}

@Composable
fun CourseCard(
    course: GradesRepository.CourseItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = XboxDarkSurface),
        border = BorderStroke(1.dp, XboxOutline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusPurple.copy(alpha = 0.2f),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = course.letterGrade,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusPurple,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Column {
                        Text(
                            text = course.courseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = XboxTextPrimary
                        )
                        Text(
                            text = "Target Goal: ${course.targetGradePercent}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = XboxTextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${course.currentGradePercent}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (course.currentGradePercent >= course.targetGradePercent) XboxNeonGreen else StatusWarningAmber
                    )
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = XboxTextSecondary)
                    }
                }
            }

            // Study hours progress
            val studyProgress = if (course.studyHoursGoal > 0) {
                (course.studyHoursCompleted.toFloat() / course.studyHoursGoal.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Study Hours: ${course.studyHoursCompleted}h / ${course.studyHoursGoal}h",
                        style = MaterialTheme.typography.labelSmall,
                        color = XboxTextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${(studyProgress * 100).toInt()}% Goal",
                        style = MaterialTheme.typography.labelSmall,
                        color = XboxNeonGreen,
                        fontSize = 11.sp
                    )
                }

                LinearProgressIndicator(
                    progress = { studyProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = XboxNeonGreen,
                    trackColor = XboxDarkSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddCourseDialog(
    context: Context,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    var courseName by remember { mutableStateOf("") }
    var currentScoreInput by remember { mutableStateOf("85.0") }
    var targetScoreInput by remember { mutableStateOf("92.0") }
    var studyGoalInput by remember { mutableStateOf("20") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = XboxDarkSurface,
        title = { Text("Add Subject & Score", color = XboxTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Subject Name (e.g. Physics, Chemistry)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StatusPurple, unfocusedBorderColor = XboxOutline)
                )

                OutlinedTextField(
                    value = currentScoreInput,
                    onValueChange = { currentScoreInput = it },
                    label = { Text("Current Percentage (%)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StatusPurple, unfocusedBorderColor = XboxOutline)
                )

                OutlinedTextField(
                    value = targetScoreInput,
                    onValueChange = { targetScoreInput = it },
                    label = { Text("Target Goal Percentage (%)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StatusPurple, unfocusedBorderColor = XboxOutline)
                )

                OutlinedTextField(
                    value = studyGoalInput,
                    onValueChange = { studyGoalInput = it },
                    label = { Text("Weekly Study Target (Hours)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StatusPurple, unfocusedBorderColor = XboxOutline)
                )

                errorMsg?.let { Text(it, color = StatusErrorRed, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (courseName.trim().isEmpty()) {
                        errorMsg = "Please enter subject name."
                    } else {
                        val current = currentScoreInput.toDoubleOrNull() ?: 80.0
                        val target = targetScoreInput.toDoubleOrNull() ?: 90.0
                        val studyGoal = studyGoalInput.toIntOrNull() ?: 20

                        val newCourse = GradesRepository.CourseItem(
                            courseName = courseName.trim(),
                            credits = 1,
                            currentGradePercent = current,
                            targetGradePercent = target,
                            studyHoursGoal = studyGoal,
                            studyHoursCompleted = 0
                        )

                        GradesRepository.addCourse(context, newCourse)
                        Toast.makeText(context, "Subject added.", Toast.LENGTH_SHORT).show()
                        onSaved()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusPurple, contentColor = XboxBlack)
            ) {
                Text("Save Subject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = XboxTextSecondary) }
        }
    )
}
