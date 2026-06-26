package com.studyflow.app.data

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ── Backup ───────────────────────────────────────────────────────────────────
 *
 * Single source of truth for turning ALL user data into one JSON document
 * and back. This class deliberately knows NOTHING about files, Uris, or
 * Android storage APIs — that's the UI's job (via the system file picker /
 * Storage Access Framework), which is what makes this reliable across every
 * Android version instead of fighting MediaStore/Downloads-folder quirks.
 *
 * Everything the app stores lives in ONE JSON file: Subjects, Sessions,
 * Daily Board items, and the Daily Goal setting. There is no multi-file
 * scheme to keep in sync.
 */
class BackupManager(
    private val dao: StudyDao,
    private val dailyBoardDao: DailyBoardDao,
) {

    companion object {
        const val SCHEMA_VERSION = 2
    }

    /** Serializes every piece of user data into a single JSON string. */
    suspend fun exportAllDataToJson(dailyGoalMillis: Long): String {
        val subjects   = dao.getAllSubjectsOnce()
        val sessions   = dao.getAllSessionsOnce()
        val boardItems = dailyBoardDao.getAllItemsOnce()

        val root = JSONObject()
        root.put("version", SCHEMA_VERSION)
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
        root.put("dailyGoalMillis", dailyGoalMillis)

        root.put("subjects", JSONArray().apply {
            subjects.forEach { s ->
                put(JSONObject().apply {
                    put("id", s.id)
                    put("name", s.name)
                    put("colorIndex", s.colorIndex)
                })
            }
        })

        root.put("sessions", JSONArray().apply {
            sessions.forEach { s ->
                put(JSONObject().apply {
                    put("id", s.id)
                    put("subjectId", s.subjectId)
                    put("subjectName", s.subjectName)
                    put("subjectColorIndex", s.subjectColorIndex)
                    put("durationMillis", s.durationMillis)
                    put("testCount", s.testCount)
                    put("note", s.note)
                    put("date", s.date)
                    put("timestamp", s.timestamp)
                })
            }
        })

        root.put("dailyBoardItems", JSONArray().apply {
            boardItems.forEach { item ->
                put(JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("note", item.note)
                    put("type", item.type)
                    put("isCompleted", item.isCompleted)
                    put("date", item.date)
                    put("linkedSubjectId", item.linkedSubjectId ?: -1)
                    put("linkedSessionId", item.linkedSessionId ?: -1)
                    put("createdAt", item.createdAt)
                })
            }
        })

        return root.toString(2)
    }

    /**
     * Replaces ALL current data with what's inside [json]. This is a full
     * replace, not a merge — after import, the file's contents become the
     * one and only source of truth. That's the simplest, least ambiguous
     * behavior for the intended use case: restoring onto a fresh install or
     * a new device.
     *
     * Returns the Daily Goal stored in the file, or null if the file
     * predates that field (so the caller can decide whether to apply it).
     */
    suspend fun importAllDataFromJson(json: String): Long? {
        val root = JSONObject(json)

        dao.clearAllSessions()
        dao.clearAllSubjects()
        dailyBoardDao.clearAllItems()

        root.optJSONArray("subjects")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                dao.insertSubject(
                    Subject(
                        id = o.getInt("id"),
                        name = o.getString("name"),
                        colorIndex = o.getInt("colorIndex"),
                    )
                )
            }
        }

        root.optJSONArray("sessions")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                dao.insertSession(
                    StudySession(
                        id = o.getInt("id"),
                        subjectId = o.getInt("subjectId"),
                        subjectName = o.getString("subjectName"),
                        subjectColorIndex = o.getInt("subjectColorIndex"),
                        durationMillis = o.getLong("durationMillis"),
                        testCount = o.optInt("testCount", 0),
                        note = o.optString("note", ""),
                        date = o.getString("date"),
                        timestamp = o.getLong("timestamp"),
                    )
                )
            }
        }

        root.optJSONArray("dailyBoardItems")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val linkedSubjectId = o.optInt("linkedSubjectId", -1).takeIf { it != -1 }
                val linkedSessionId = o.optInt("linkedSessionId", -1).takeIf { it != -1 }
                dailyBoardDao.insertItem(
                    DailyBoardItem(
                        id = o.getInt("id"),
                        title = o.getString("title"),
                        note = o.optString("note", ""),
                        type = o.getString("type"),
                        isCompleted = o.optBoolean("isCompleted", false),
                        date = o.getString("date"),
                        linkedSubjectId = linkedSubjectId,
                        linkedSessionId = linkedSessionId,
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                    )
                )
            }
        }

        return if (root.has("dailyGoalMillis")) root.optLong("dailyGoalMillis") else null
    }
}
