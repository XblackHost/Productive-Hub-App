package com.example

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object DiaryRepository {
    private const val PREFS_DIARY = "hatif_encrypted_diary_data"
    private const val KEY_ENCRYPTED_ENTRIES_JSON = "key_encrypted_entries_json"

    data class DiaryEntry(
        val id: String = UUID.randomUUID().toString(),
        val timestamp: Long = System.currentTimeMillis(),
        val title: String,
        val content: String,
        val mood: String = "🌱 Hopeful", // e.g. "⚡ Productive", "🌱 Hopeful", "🎯 Focused", "😌 Calm", "🌪️ Stressed"
        val tags: List<String> = emptyList(),
        val isFavorite: Boolean = false
    ) {
        val formattedDate: String
            get() = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault()).format(Date(timestamp))

        val dateDayKey: String
            get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timestamp))
    }

    private val _entriesFlow = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entriesFlow: StateFlow<List<DiaryEntry>> = _entriesFlow.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_DIARY, Context.MODE_PRIVATE)
    }

    fun init(context: Context) {
        loadEntries(context)
    }

    @Synchronized
    fun loadEntries(context: Context) {
        val prefs = getPrefs(context)
        val encryptedPayload = prefs.getString(KEY_ENCRYPTED_ENTRIES_JSON, null)
        if (encryptedPayload.isNullOrEmpty()) {
            _entriesFlow.value = emptyList()
            return
        }

        try {
            val decryptedJson = HatifSecurityManager.decryptData(encryptedPayload)
            if (decryptedJson.isEmpty()) {
                _entriesFlow.value = emptyList()
                return
            }

            val jsonArray = JSONArray(decryptedJson)
            val list = mutableListOf<DiaryEntry>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val tagsArray = obj.optJSONArray("tags")
                val tagsList = mutableListOf<String>()
                if (tagsArray != null) {
                    for (t in 0 until tagsArray.length()) {
                        tagsList.add(tagsArray.getString(t))
                    }
                }

                list.add(
                    DiaryEntry(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        title = obj.optString("title", "Untitled"),
                        content = obj.optString("content", ""),
                        mood = obj.optString("mood", "🌱 Hopeful"),
                        tags = tagsList,
                        isFavorite = obj.optBoolean("isFavorite", false)
                    )
                )
            }

            // Sort by latest timestamp first
            _entriesFlow.value = list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            _entriesFlow.value = emptyList()
        }
    }

    @Synchronized
    private fun saveEntriesInternal(context: Context, entries: List<DiaryEntry>) {
        val jsonArray = JSONArray()
        for (e in entries) {
            val obj = JSONObject().apply {
                put("id", e.id)
                put("timestamp", e.timestamp)
                put("title", e.title)
                put("content", e.content)
                put("mood", e.mood)
                put("isFavorite", e.isFavorite)
                val tagsArr = JSONArray()
                e.tags.forEach { tagsArr.put(it) }
                put("tags", tagsArr)
            }
            jsonArray.put(obj)
        }

        val plainJson = jsonArray.toString()
        val encrypted = HatifSecurityManager.encryptData(plainJson)

        getPrefs(context).edit()
            .putString(KEY_ENCRYPTED_ENTRIES_JSON, encrypted)
            .apply()

        _entriesFlow.value = entries.sortedByDescending { it.timestamp }
    }

    fun addEntry(
        context: Context,
        title: String,
        content: String,
        mood: String,
        tags: List<String>,
        isFavorite: Boolean
    ): DiaryEntry {
        val newEntry = DiaryEntry(
            title = title.trim().ifEmpty { "Entry" },
            content = content.trim(),
            mood = mood,
            tags = tags.map { it.trim().removePrefix("#") }.filter { it.isNotEmpty() },
            isFavorite = isFavorite
        )

        val updated = _entriesFlow.value.toMutableList()
        updated.add(0, newEntry)
        saveEntriesInternal(context, updated)
        return newEntry
    }

    fun updateEntry(context: Context, entry: DiaryEntry) {
        val updated = _entriesFlow.value.map {
            if (it.id == entry.id) entry else it
        }
        saveEntriesInternal(context, updated)
    }

    fun deleteEntry(context: Context, entryId: String) {
        val updated = _entriesFlow.value.filterNot { it.id == entryId }
        saveEntriesInternal(context, updated)
    }

    fun toggleFavorite(context: Context, entryId: String) {
        val updated = _entriesFlow.value.map {
            if (it.id == entryId) it.copy(isFavorite = !it.isFavorite) else it
        }
        saveEntriesInternal(context, updated)
    }

    fun exportEncryptedBackup(context: Context): String {
        return getPrefs(context).getString(KEY_ENCRYPTED_ENTRIES_JSON, "") ?: ""
    }

    /**
     * Imports an encrypted or plaintext JSON backup string.
     * Returns true if restored successfully without data loss.
     */
    fun importBackupPayload(context: Context, payload: String): Boolean {
        val trimmed = payload.trim()
        if (trimmed.isEmpty()) return false

        try {
            // First check if it's plaintext JSON
            val jsonArray = if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                JSONArray(trimmed)
            } else {
                // Otherwise attempt AES decryption
                val decrypted = HatifSecurityManager.decryptData(trimmed)
                if (decrypted.isNotEmpty() && decrypted.startsWith("[")) {
                    JSONArray(decrypted)
                } else {
                    return false
                }
            }

            val importedEntries = mutableListOf<DiaryEntry>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id", UUID.randomUUID().toString())
                val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                val title = obj.optString("title", "Untitled Entry")
                val content = obj.optString("content", "")
                val mood = obj.optString("mood", "🌱 Hopeful")
                val isFavorite = obj.optBoolean("isFavorite", false)
                val tagsArr = obj.optJSONArray("tags")
                val tags = mutableListOf<String>()
                if (tagsArr != null) {
                    for (t in 0 until tagsArr.length()) {
                        tags.add(tagsArr.getString(t))
                    }
                }
                importedEntries.add(
                    DiaryEntry(
                        id = id,
                        timestamp = timestamp,
                        title = title,
                        content = content,
                        mood = mood,
                        tags = tags,
                        isFavorite = isFavorite
                    )
                )
            }

            // Merge with existing entries to prevent packet/entry loss
            val existingIds = _entriesFlow.value.map { it.id }.toSet()
            val newUnique = importedEntries.filterNot { it.id in existingIds }
            val merged = (_entriesFlow.value + newUnique).sortedByDescending { it.timestamp }

            saveEntriesInternal(context, merged)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Exports entries as clean portable JSON for transferring between different phones/architectures.
     */
    fun exportPortableJson(): String {
        val jsonArray = JSONArray()
        for (e in _entriesFlow.value) {
            val obj = JSONObject().apply {
                put("id", e.id)
                put("timestamp", e.timestamp)
                put("title", e.title)
                put("content", e.content)
                put("mood", e.mood)
                put("isFavorite", e.isFavorite)
                val tagsArr = JSONArray()
                e.tags.forEach { tagsArr.put(it) }
                put("tags", tagsArr)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }
}
