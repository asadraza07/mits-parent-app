package com.mitsschools.parent.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mitsschools.parent.MitsApplication
import com.mitsschools.parent.data.model.DailyDiary
import com.mitsschools.parent.data.model.ParentSession
import com.mitsschools.parent.data.model.SchoolClass
import com.mitsschools.parent.data.model.SchoolEvent
import com.mitsschools.parent.data.model.SchoolNotification
import com.mitsschools.parent.data.model.Subject
import com.mitsschools.parent.data.model.WeeklyDiary
import com.mitsschools.parent.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** App-wide state shared by every screen. */
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val mits = app as MitsApplication
    private val repo = mits.repository
    private val store = mits.sessionStore

    private val _session = MutableStateFlow(store.session())
    val session: StateFlow<ParentSession?> = _session.asStateFlow()

    private val _selectedClass = MutableStateFlow(store.selectedClass())
    val selectedClass: StateFlow<SchoolClass?> = _selectedClass.asStateFlow()

    private val _themeMode = MutableStateFlow(
        ThemeMode.entries.firstOrNull { it.name == store.themeMode() } ?: ThemeMode.SYSTEM
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _notifications = MutableStateFlow<List<SchoolNotification>>(emptyList())
    val notifications: StateFlow<List<SchoolNotification>> = _notifications.asStateFlow()

    private val _events = MutableStateFlow<List<SchoolEvent>>(emptyList())
    val events: StateFlow<List<SchoolEvent>> = _events.asStateFlow()

    private val _readIds = MutableStateFlow(store.readIds())
    val readIds: StateFlow<Set<String>> = _readIds.asStateFlow()

    private val _doneHomework = MutableStateFlow(store.doneHomework())
    val doneHomework: StateFlow<Set<String>> = _doneHomework.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    init {
        refresh()
    }

    /* ---------- Auth ---------- */

    suspend fun login(mobile: String, password: String): Result<ParentSession> {
        val result = repo.login(mobile, password)
        result.onSuccess {
            store.saveSession(it)
            _session.value = it
        }
        return result
    }

    fun logout() {
        store.clearSession()
        _session.value = null
        _selectedClass.value = null
        _readIds.value = emptySet()
        _doneHomework.value = emptySet()
        _notifications.value = emptyList()
        _events.value = emptyList()
    }

    /* ---------- Class ---------- */

    fun selectClass(schoolClass: SchoolClass) {
        store.saveClass(schoolClass)
        _selectedClass.value = schoolClass
        refresh()
    }

    /* ---------- Data ---------- */

    fun refresh() {
        val cls = _selectedClass.value ?: return
        viewModelScope.launch {
            _refreshing.value = true
            try {
                _events.value = repo.yearlyCalendar(cls)
                _notifications.value = repo.notifications(cls)
            } finally {
                _refreshing.value = false
            }
        }
    }

    suspend fun dailyDiary(cls: SchoolClass, date: LocalDate): DailyDiary = repo.dailyDiary(cls, date)
    suspend fun weeklyDiary(cls: SchoolClass, weekStart: LocalDate): WeeklyDiary = repo.weeklyDiary(cls, weekStart)

    /* ---------- Notifications ---------- */

    fun markRead(id: String) {
        if (id in _readIds.value) return
        _readIds.update { it + id }
        store.saveReadIds(_readIds.value)
    }

    fun markAllRead() {
        _readIds.update { it + _notifications.value.map { n -> n.id } }
        store.saveReadIds(_readIds.value)
    }

    /* ---------- Homework tracking (parent ticks off completed homework) ---------- */

    fun homeworkKey(cls: SchoolClass, date: LocalDate, subject: Subject) = "${cls.name}|$date|${subject.name}"

    fun toggleHomework(key: String) {
        _doneHomework.update { if (key in it) it - key else it + key }
        store.saveDoneHomework(_doneHomework.value)
    }

    /* ---------- Appearance ---------- */

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        store.saveThemeMode(mode.name)
    }
}
