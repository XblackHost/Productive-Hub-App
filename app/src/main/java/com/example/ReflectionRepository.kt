package com.example

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReflectionRepository {
    private const val PREFS_NAME = "hatif_reflection_data"
    private const val KEY_REFLECTIONS_JSON = "key_reflections_json"
    private const val KEY_CYCLE_START_TIME = "key_cycle_start_time"

    const val MAX_CYCLE_DAYS = 30

    data class DayReflection(
        val dayNumber: Int,          // 1 to 30
        val dateString: String,      // "2026-09-18"
        val rating: Int,             // 1 to 10
        val reflectionText: String,
        val highlights: String = "",
        val challenges: String = "",
        val timestamp: Long = System.currentTimeMillis()
    ) {
        val formattedDate: String
            get() = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    private val _reflectionsFlow = MutableStateFlow<List<DayReflection>>(emptyList())
    val reflectionsFlow: StateFlow<List<DayReflection>> = _reflectionsFlow.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun init(context: Context) {
        val prefs = getPrefs(context)
        if (!prefs.contains(KEY_CYCLE_START_TIME)) {
            prefs.edit().putLong(KEY_CYCLE_START_TIME, System.currentTimeMillis()).apply()
        }
        loadReflections(context)
    }

    @Synchronized
    fun loadReflections(context: Context) {
        val prefs = getPrefs(context)
        val rawJson = prefs.getString(KEY_REFLECTIONS_JSON, null) ?: "[]"
        try {
            val jsonArray = JSONArray(rawJson)
            val list = mutableListOf<DayReflection>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    DayReflection(
                        dayNumber = obj.optInt("dayNumber", i + 1),
                        dateString = obj.optString("dateString", ""),
                        rating = obj.optInt("rating", 7),
                        reflectionText = obj.optString("reflectionText", ""),
                        highlights = obj.optString("highlights", ""),
                        challenges = obj.optString("challenges", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            _reflectionsFlow.value = list.sortedBy { it.dayNumber }
        } catch (e: Exception) {
            _reflectionsFlow.value = emptyList()
        }
    }

    @Synchronized
    fun saveReflection(
        context: Context,
        dayNumber: Int,
        rating: Int,
        reflectionText: String,
        highlights: String,
        challenges: String
    ) {
        val today = getTodayDateString()
        val currentList = _reflectionsFlow.value.filterNot { it.dayNumber == dayNumber }.toMutableList()

        val newReflection = DayReflection(
            dayNumber = dayNumber.coerceIn(1, MAX_CYCLE_DAYS),
            dateString = today,
            rating = rating.coerceIn(1, 10),
            reflectionText = reflectionText.trim(),
            highlights = highlights.trim(),
            challenges = challenges.trim(),
            timestamp = System.currentTimeMillis()
        )

        currentList.add(newReflection)
        val sorted = currentList.sortedBy { it.dayNumber }

        val jsonArray = JSONArray()
        for (item in sorted) {
            val obj = JSONObject().apply {
                put("dayNumber", item.dayNumber)
                put("dateString", item.dateString)
                put("rating", item.rating)
                put("reflectionText", item.reflectionText)
                put("highlights", item.highlights)
                put("challenges", item.challenges)
                put("timestamp", item.timestamp)
            }
            jsonArray.put(obj)
        }

        getPrefs(context).edit().putString(KEY_REFLECTIONS_JSON, jsonArray.toString()).apply()
        _reflectionsFlow.value = sorted
    }

    fun getNextDayNumberToRecord(): Int {
        val list = _reflectionsFlow.value
        if (list.isEmpty()) return 1
        val recordedDays = list.map { it.dayNumber }.toSet()
        for (day in 1..MAX_CYCLE_DAYS) {
            if (!recordedDays.contains(day)) {
                return day
            }
        }
        return MAX_CYCLE_DAYS
    }

    fun calculateAverageRating(): Double {
        val list = _reflectionsFlow.value
        if (list.isEmpty()) return 0.0
        val sum = list.sumOf { it.rating }
        return (sum.toDouble() / list.size * 10).toInt() / 10.0
    }

    fun calculateCompletedDays(): Int {
        return _reflectionsFlow.value.size
    }

    /**
     * Generates a comprehensive, formatted .txt export ready to be pasted
     * or uploaded into ChatGPT, Claude, or Gemini for AI analysis.
     */
    fun generateAiExportText(): String {
        val list = _reflectionsFlow.value.sortedBy { it.dayNumber }
        val completedCount = list.size
        val avgRating = calculateAverageRating()
        val dateNow = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

        val sb = StringBuilder()
        sb.append("=================================================================\n")
        sb.append("         HATIF WORKSPACE: 30-DAY SELF-REFLECTION REPORT          \n")
        sb.append("=================================================================\n\n")
        sb.append("Report Date: $dateNow\n")
        sb.append("Cycle Progress: $completedCount of $MAX_CYCLE_DAYS Days Completed\n")
        sb.append("Average Daily Score: $avgRating / 10\n\n")

        sb.append("-----------------------------------------------------------------\n")
        sb.append("[SYSTEM PROMPT FOR AI EVALUATION]\n")
        sb.append("Paste or upload this text to ChatGPT, Claude, or Gemini with the following instruction:\n\n")
        sb.append("\"Act as an expert personal growth coach, psychologist, and productivity advisor.\n")
        sb.append("Carefully analyze my 30-day self-reflection journal entries below.\n")
        sb.append("1. Growth Assessment: Identify evidence of emotional maturity, resilience, and personal improvement.\n")
        sb.append("2. High vs Low Pattern Analysis: What specific factors triggered my lowest-rated days compared to my peak days?\n")
        sb.append("3. Consistency & Focus: How well did I sustain discipline across the 30-day period?\n")
        sb.append("4. Concrete Roadmap: Provide 3 prioritized, highly actionable goals for my next 30-day cycle.\"\n")
        sb.append("-----------------------------------------------------------------\n\n")

        sb.append("==================== DAILY REFLECTION LOGS =====================\n\n")

        if (list.isEmpty()) {
            sb.append("No daily reflections recorded yet.\n")
        } else {
            for (item in list) {
                sb.append("-----------------------------------------------------------------\n")
                sb.append("DAY ${item.dayNumber} of $MAX_CYCLE_DAYS | Date: ${item.dateString} | Rating: ${item.rating}/10\n")
                sb.append("Recorded: ${item.formattedDate}\n\n")
                sb.append("Daily Reflection:\n${item.reflectionText.ifEmpty { "(No detailed reflection provided)" }}\n\n")
                if (item.highlights.isNotEmpty()) {
                    sb.append("Key Win / Highlight: ${item.highlights}\n")
                }
                if (item.challenges.isNotEmpty()) {
                    sb.append("Struggle / Challenge Faced: ${item.challenges}\n")
                }
                sb.append("\n")
            }
        }

        sb.append("=================================================================\n")
        sb.append("                 END OF 30-DAY REFLECTION REPORT                 \n")
        sb.append("=================================================================\n")

        return sb.toString()
    }

    fun shareExportText(context: Context) {
        val report = generateAiExportText()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Hatif Workspace - 30-Day Self-Reflection Report")
            putExtra(Intent.EXTRA_TEXT, report)
        }
        val chooser = Intent.createChooser(intent, "Share / Upload 30-Day Reflection Report")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun resetCycle(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_REFLECTIONS_JSON)
            .putLong(KEY_CYCLE_START_TIME, System.currentTimeMillis())
            .apply()
        _reflectionsFlow.value = emptyList()
    }
}
