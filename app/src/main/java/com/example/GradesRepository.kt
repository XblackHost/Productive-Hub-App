package com.example

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object GradesRepository {
    private const val PREFS_NAME = "hatif_grades_data"
    private const val KEY_COURSES_JSON = "key_courses_json"

    data class CourseItem(
        val id: String = UUID.randomUUID().toString(),
        val courseName: String,
        val credits: Int = 3,
        val currentGradePercent: Double = 85.0,
        val targetGradePercent: Double = 92.0,
        val studyHoursGoal: Int = 20,
        val studyHoursCompleted: Int = 12
    ) {
        val letterGrade: String
            get() = when {
                currentGradePercent >= 90.0 -> "A"
                currentGradePercent >= 85.0 -> "A-"
                currentGradePercent >= 80.0 -> "B+"
                currentGradePercent >= 75.0 -> "B"
                currentGradePercent >= 70.0 -> "C+"
                currentGradePercent >= 60.0 -> "C"
                else -> "D/F"
            }

        val gpaPoints: Double
            get() = when {
                currentGradePercent >= 93.0 -> 4.0
                currentGradePercent >= 90.0 -> 3.7
                currentGradePercent >= 87.0 -> 3.3
                currentGradePercent >= 83.0 -> 3.0
                currentGradePercent >= 80.0 -> 2.7
                currentGradePercent >= 77.0 -> 2.3
                currentGradePercent >= 73.0 -> 2.0
                currentGradePercent >= 70.0 -> 1.7
                else -> 1.0
            }
    }

    private val _coursesFlow = MutableStateFlow<List<CourseItem>>(emptyList())
    val coursesFlow: StateFlow<List<CourseItem>> = _coursesFlow.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun init(context: Context) {
        val prefs = getPrefs(context)
        if (!prefs.contains(KEY_COURSES_JSON)) {
            // Seed with high school subjects
            val sample = listOf(
                CourseItem(courseName = "Mathematics", credits = 4, currentGradePercent = 91.5, targetGradePercent = 95.0, studyHoursGoal = 30, studyHoursCompleted = 22),
                CourseItem(courseName = "Physics", credits = 3, currentGradePercent = 84.0, targetGradePercent = 90.0, studyHoursGoal = 25, studyHoursCompleted = 18),
                CourseItem(courseName = "English Literature", credits = 3, currentGradePercent = 88.5, targetGradePercent = 92.0, studyHoursGoal = 20, studyHoursCompleted = 14)
            )
            saveCoursesInternal(context, sample)
        } else {
            loadCourses(context)
        }
    }

    @Synchronized
    fun loadCourses(context: Context) {
        val prefs = getPrefs(context)
        val rawJson = prefs.getString(KEY_COURSES_JSON, null) ?: "[]"
        try {
            val arr = JSONArray(rawJson)
            val list = mutableListOf<CourseItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    CourseItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        courseName = obj.optString("courseName", "Course"),
                        credits = obj.optInt("credits", 3),
                        currentGradePercent = obj.optDouble("currentGradePercent", 80.0),
                        targetGradePercent = obj.optDouble("targetGradePercent", 90.0),
                        studyHoursGoal = obj.optInt("studyHoursGoal", 20),
                        studyHoursCompleted = obj.optInt("studyHoursCompleted", 0)
                    )
                )
            }
            _coursesFlow.value = list
        } catch (e: Exception) {
            _coursesFlow.value = emptyList()
        }
    }

    @Synchronized
    private fun saveCoursesInternal(context: Context, list: List<CourseItem>) {
        val arr = JSONArray()
        for (c in list) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("courseName", c.courseName)
                put("credits", c.credits)
                put("currentGradePercent", c.currentGradePercent)
                put("targetGradePercent", c.targetGradePercent)
                put("studyHoursGoal", c.studyHoursGoal)
                put("studyHoursCompleted", c.studyHoursCompleted)
            }
            arr.put(obj)
        }
        getPrefs(context).edit().putString(KEY_COURSES_JSON, arr.toString()).apply()
        _coursesFlow.value = list
    }

    fun addCourse(context: Context, course: CourseItem) {
        val updated = _coursesFlow.value.toMutableList().apply { add(course) }
        saveCoursesInternal(context, updated)
    }

    fun updateCourse(context: Context, course: CourseItem) {
        val updated = _coursesFlow.value.map { if (it.id == course.id) course else it }
        saveCoursesInternal(context, updated)
    }

    fun deleteCourse(context: Context, courseId: String) {
        val updated = _coursesFlow.value.filterNot { it.id == courseId }
        saveCoursesInternal(context, updated)
    }

    fun calculateCumulativeGpa(): Double {
        val list = _coursesFlow.value
        if (list.isEmpty()) return 0.0
        val totalCredits = list.sumOf { it.credits }
        if (totalCredits == 0) return 0.0
        val totalPoints = list.sumOf { it.credits * it.gpaPoints }
        val gpa = totalPoints / totalCredits
        return (gpa * 100).toInt() / 100.0
    }

    fun calculateAveragePercent(): Double {
        val list = _coursesFlow.value
        if (list.isEmpty()) return 0.0
        val avg = list.sumOf { it.currentGradePercent } / list.size
        return (avg * 10).toInt() / 10.0
    }
}
