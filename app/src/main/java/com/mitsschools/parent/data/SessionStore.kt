package com.mitsschools.parent.data

import android.content.Context
import com.mitsschools.parent.data.model.ParentSession
import com.mitsschools.parent.data.model.SchoolClass

/**
 * Small on-device store for the signed-in parent, their chosen class and
 * per-device UI state (read notifications, homework ticked as done, theme).
 */
class SessionStore(context: Context) {

    private val prefs = context.getSharedPreferences("mits_parent", Context.MODE_PRIVATE)

    fun session(): ParentSession? {
        val mobile = prefs.getString(KEY_MOBILE, null) ?: return null
        return ParentSession(mobile, prefs.getString(KEY_NAME, "Parent") ?: "Parent")
    }

    fun saveSession(session: ParentSession) {
        prefs.edit()
            .putString(KEY_MOBILE, session.mobile)
            .putString(KEY_NAME, session.parentName)
            .apply()
    }

    fun selectedClass(): SchoolClass? =
        prefs.getString(KEY_CLASS, null)?.let { name -> SchoolClass.entries.firstOrNull { it.name == name } }

    fun saveClass(schoolClass: SchoolClass) {
        prefs.edit().putString(KEY_CLASS, schoolClass.name).apply()
    }

    fun readIds(): Set<String> = prefs.getStringSet(KEY_READ, emptySet())?.toSet() ?: emptySet()
    fun saveReadIds(ids: Set<String>) = prefs.edit().putStringSet(KEY_READ, ids).apply()

    fun doneHomework(): Set<String> = prefs.getStringSet(KEY_DONE, emptySet())?.toSet() ?: emptySet()
    fun saveDoneHomework(ids: Set<String>) = prefs.edit().putStringSet(KEY_DONE, ids).apply()

    fun themeMode(): String = prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM"
    fun saveThemeMode(mode: String) = prefs.edit().putString(KEY_THEME, mode).apply()

    /** Signs out but keeps device preferences such as theme. */
    fun clearSession() {
        prefs.edit()
            .remove(KEY_MOBILE).remove(KEY_NAME).remove(KEY_CLASS)
            .remove(KEY_READ).remove(KEY_DONE)
            .apply()
    }

    private companion object {
        const val KEY_MOBILE = "mobile"
        const val KEY_NAME = "parent_name"
        const val KEY_CLASS = "class"
        const val KEY_READ = "read_ids"
        const val KEY_DONE = "done_homework"
        const val KEY_THEME = "theme"
    }
}
