package com.mitsschools.parent.data

import com.mitsschools.parent.data.model.DailyDiary
import com.mitsschools.parent.data.model.ParentSession
import com.mitsschools.parent.data.model.SchoolClass
import com.mitsschools.parent.data.model.SchoolEvent
import com.mitsschools.parent.data.model.SchoolNotification
import com.mitsschools.parent.data.model.WeeklyDiary
import java.time.LocalDate

/**
 * Everything the parent app needs from the school.
 *
 * Phase one ships with [MockSchoolRepository] (realistic sample data).
 * To go live, implement this interface against the school's backend / API
 * and swap it in [com.mitsschools.parent.MitsApplication].
 */
interface SchoolRepository {
    suspend fun login(mobile: String, password: String): Result<ParentSession>
    suspend fun dailyDiary(schoolClass: SchoolClass, date: LocalDate): DailyDiary
    suspend fun weeklyDiary(schoolClass: SchoolClass, weekStart: LocalDate): WeeklyDiary
    suspend fun yearlyCalendar(schoolClass: SchoolClass): List<SchoolEvent>
    suspend fun notifications(schoolClass: SchoolClass): List<SchoolNotification>
}
