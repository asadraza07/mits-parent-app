package com.mitsschools.parent.data.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class Stage(val label: String) {
    EARLY_YEARS("Early Years"),
    PRIMARY("Primary"),
}

/** Classes offered at MITS Schools, as listed in the parent-app flowchart. */
enum class SchoolClass(val label: String, val badge: String, val stage: Stage) {
    PLAY_GROUP("Play Group", "PG", Stage.EARLY_YEARS),
    NURSERY("Nursery", "N", Stage.EARLY_YEARS),
    KG("KG", "KG", Stage.EARLY_YEARS),
    GRADE_1("Grade 1", "1", Stage.PRIMARY),
    GRADE_2("Grade 2", "2", Stage.PRIMARY),
    GRADE_3("Grade 3", "3", Stage.PRIMARY),
    GRADE_4("Grade 4", "4", Stage.PRIMARY),
    GRADE_5("Grade 5", "5", Stage.PRIMARY),
    GRADE_6("Grade 6", "6", Stage.PRIMARY);

    val isEarlyYears: Boolean get() = stage == Stage.EARLY_YEARS
}

enum class Subject(val title: String) {
    PHONICS("Phonics & Language"),
    NUMBERS("Numbers"),
    GENERAL("General Awareness"),
    RHYMES("Rhymes & Music"),
    PLAY("Physical Play"),
    ENGLISH("English"),
    MATHS("Mathematics"),
    SCIENCE("Science"),
    EVS("EVS"),
    SOCIAL("Social Studies"),
    COMPUTER("Computer Science"),
    ART("Art & Craft"),
}

data class ParentSession(
    val mobile: String,
    val parentName: String,
)

/* ---------- Daily diary ---------- */

data class DiaryEntry(
    val subject: Subject,
    val classwork: String,
    val homework: String?,
)

data class DailyDiary(
    val date: LocalDate,
    val isSchoolDay: Boolean,
    val closedReason: String? = null,
    val teacherNote: String? = null,
    val entries: List<DiaryEntry> = emptyList(),
)

/* ---------- Weekly diary ---------- */

data class WeeklyPlan(
    val subject: Subject,
    val topic: String,
    val objectives: List<String>,
    val homework: String?,
)

data class WeeklyDiary(
    val weekStart: LocalDate,
    val focus: String,
    val plans: List<WeeklyPlan>,
)

/* ---------- Yearly calendar ---------- */

enum class EventType(val label: String, val plural: String) {
    HOLIDAY("Holiday", "Holidays"),
    EXAM("Exam", "Exams"),
    EVENT("Event", "Events"),
    MEETING("Parent meeting", "Meetings"),
}

data class SchoolEvent(
    val id: String,
    val title: String,
    val type: EventType,
    val start: LocalDate,
    val end: LocalDate = start,
    val description: String,
) {
    fun covers(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)
    val isMultiDay: Boolean get() = end.isAfter(start)
}

/* ---------- Notifications ---------- */

enum class NoticeCategory(val label: String) {
    DIARY("Diary"),
    HOMEWORK("Homework"),
    ANNOUNCEMENT("Announcements"),
    NOTICE("Important"),
}

data class SchoolNotification(
    val id: String,
    val category: NoticeCategory,
    val title: String,
    val body: String,
    val time: LocalDateTime,
)
