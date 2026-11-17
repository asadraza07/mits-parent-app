package com.mitsschools.parent.data

import com.mitsschools.parent.data.model.DailyDiary
import com.mitsschools.parent.data.model.DiaryEntry
import com.mitsschools.parent.data.model.EventType
import com.mitsschools.parent.data.model.NoticeCategory
import com.mitsschools.parent.data.model.ParentSession
import com.mitsschools.parent.data.model.SchoolClass
import com.mitsschools.parent.data.model.SchoolEvent
import com.mitsschools.parent.data.model.SchoolNotification
import com.mitsschools.parent.data.model.Subject
import com.mitsschools.parent.data.model.WeeklyDiary
import com.mitsschools.parent.data.model.WeeklyPlan
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * Sample data for phase one. Content is generated deterministically from the
 * class and date so the app looks and behaves like a live school feed.
 */
class MockSchoolRepository : SchoolRepository {

    override suspend fun login(mobile: String, password: String): Result<ParentSession> {
        delay(900)
        val digits = mobile.filter { it.isDigit() }
        return when {
            digits.length < 7 -> Result.failure(IllegalArgumentException("Enter your registered mobile number."))
            password.length < 4 -> Result.failure(IllegalArgumentException("Incorrect mobile number or password."))
            else -> Result.success(ParentSession(mobile = digits, parentName = "Parent"))
        }
    }

    override suspend fun dailyDiary(schoolClass: SchoolClass, date: LocalDate): DailyDiary {
        delay(250)
        if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
            return DailyDiary(date, isSchoolDay = false, closedReason = "Weekend")
        }
        val holiday = buildCalendar(schoolClass).firstOrNull { it.type == EventType.HOLIDAY && it.covers(date) }
        if (holiday != null) {
            return DailyDiary(date, isSchoolDay = false, closedReason = holiday.title)
        }
        val subjects = subjectsFor(schoolClass)
        val perDay = if (schoolClass.isEarlyYears) 4 else 5
        val day = date.toEpochDay().toInt()
        val start = Math.floorMod(day, subjects.size)
        val todays = (0 until perDay).map { subjects[(start + it) % subjects.size] }
        val entries = todays.mapIndexed { i, subject ->
            val bank = Content.bank(subject)
            val seed = day + i * 7 + schoolClass.ordinal * 3
            val hw = bank.homework.getOrNull(Math.floorMod(seed, bank.homework.size + 1))
            DiaryEntry(
                subject = subject,
                classwork = bank.classwork[Math.floorMod(seed, bank.classwork.size)],
                homework = hw,
            )
        }
        val note = Content.teacherNotes(schoolClass)[Math.floorMod(day, 7)]
        return DailyDiary(date, isSchoolDay = true, teacherNote = note, entries = entries)
    }

    override suspend fun weeklyDiary(schoolClass: SchoolClass, weekStart: LocalDate): WeeklyDiary {
        delay(250)
        val monday = weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val week = (monday.toEpochDay() / 7).toInt()
        val plans = subjectsFor(schoolClass).mapIndexed { i, subject ->
            val bank = Content.bank(subject)
            val topic = bank.topics[Math.floorMod(week + i, bank.topics.size)]
            WeeklyPlan(
                subject = subject,
                topic = topic.first,
                objectives = topic.second,
                homework = bank.homework.getOrNull(Math.floorMod(week + i * 2, bank.homework.size + 1)),
            )
        }
        val focus = Content.weeklyFocus[Math.floorMod(week, Content.weeklyFocus.size)]
        return WeeklyDiary(monday, focus, plans)
    }

    override suspend fun yearlyCalendar(schoolClass: SchoolClass): List<SchoolEvent> {
        delay(250)
        return buildCalendar(schoolClass)
    }

    override suspend fun notifications(schoolClass: SchoolClass): List<SchoolNotification> {
        delay(300)
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val fmt = DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())
        val upcoming = buildCalendar(schoolClass).filter { !it.start.isBefore(today) }.sortedBy { it.start }
        val list = mutableListOf(
            SchoolNotification(
                id = "diary-$today-${schoolClass.name}",
                category = NoticeCategory.DIARY,
                title = "Today's diary is ready",
                body = "Classwork and homework for ${schoolClass.label} on ${today.format(fmt)} have been posted by the class teacher.",
                time = now.minusMinutes(12),
            ),
            SchoolNotification(
                id = "hw-$today-${schoolClass.name}",
                category = NoticeCategory.HOMEWORK,
                title = "Homework reminder",
                body = "Please check today's homework in the Daily Diary and help your child complete it before tomorrow.",
                time = now.minusHours(3),
            ),
            SchoolNotification(
                id = "weekly-${today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))}-${schoolClass.name}",
                category = NoticeCategory.DIARY,
                title = "Weekly diary published",
                body = "This week's syllabus, learning objectives and homework plan for ${schoolClass.label} are now available.",
                time = now.minusDays(1).withHour(8).withMinute(15),
            ),
            SchoolNotification(
                id = "uniform-${today.year}",
                category = NoticeCategory.NOTICE,
                title = "Winter uniform",
                body = "Students may wear the school winter uniform (blazer or school sweater) from 1 December. Please label all items with your child's name.",
                time = now.minusDays(2).withHour(10).withMinute(0),
            ),
            SchoolNotification(
                id = "library-${today.year}",
                category = NoticeCategory.NOTICE,
                title = "Library books due",
                body = "Kindly return borrowed library books by Friday so new books can be issued next week.",
                time = now.minusDays(6).withHour(9).withMinute(30),
            ),
        )
        upcoming.take(3).forEachIndexed { i, event ->
            val days = ChronoUnit.DAYS.between(today, event.start)
            list += SchoolNotification(
                id = "event-${event.id}",
                category = NoticeCategory.ANNOUNCEMENT,
                title = event.title,
                body = "${event.description} Date: ${event.start.format(fmt)}" +
                    (if (event.isMultiDay) " – ${event.end.format(fmt)}." else ".") +
                    if (days in 0L..7L) " (in $days day${if (days == 1L) "" else "s"})" else "",
                time = now.minusDays(i + 1L).withHour(11).withMinute(0),
            )
        }
        return list.sortedByDescending { it.time }
    }

    /* ------------------------------------------------------------------ */

    private fun subjectsFor(schoolClass: SchoolClass): List<Subject> = when {
        schoolClass.isEarlyYears -> listOf(
            Subject.PHONICS, Subject.NUMBERS, Subject.GENERAL, Subject.RHYMES, Subject.ART, Subject.PLAY,
        )
        schoolClass <= SchoolClass.GRADE_2 -> listOf(
            Subject.ENGLISH, Subject.MATHS, Subject.EVS, Subject.COMPUTER, Subject.ART,
        )
        else -> listOf(
            Subject.ENGLISH, Subject.MATHS, Subject.SCIENCE, Subject.SOCIAL, Subject.COMPUTER, Subject.ART,
        )
    }

    /** Academic year runs August → June. */
    private fun buildCalendar(schoolClass: SchoolClass): List<SchoolEvent> {
        val today = LocalDate.now()
        val y = if (today.monthValue >= 8) today.year else today.year - 1
        val n = y + 1
        val exam = if (schoolClass.isEarlyYears) "Assessment Week" else "Examinations"
        fun d(year: Int, month: Int, day: Int) = LocalDate.of(year, month, day)
        return listOf(
            SchoolEvent("start", "First Day of School", EventType.EVENT, d(y, 8, 18), description = "Welcome back! School begins for the new academic year."),
            SchoolEvent("orient", "Parent Orientation", EventType.MEETING, d(y, 8, 29), description = "Meet the class teacher and learn about the year ahead."),
            SchoolEvent("ut1", "Unit Test 1", EventType.EXAM, d(y, 9, 22), d(y, 9, 26), "First unit test for all subjects. Revision plan is shared in the weekly diary."),
            SchoolEvent("ptm1", "Parent–Teacher Meeting", EventType.MEETING, d(y, 10, 3), description = "Discuss your child's progress with subject teachers."),
            SchoolEvent("midbreak", "Mid-Term Break", EventType.HOLIDAY, d(y, 10, 20), d(y, 10, 24), "School remains closed for the mid-term break."),
            SchoolEvent("funfair", "Fun Fair", EventType.EVENT, d(y, 11, 14), description = "Games, food stalls and activities for the whole family."),
            SchoolEvent("science", "Science Exhibition", EventType.EVENT, d(y, 11, 20), description = "Students present their science projects. Parents are warmly invited from 10 am."),
            SchoolEvent("ptm2", "Parent–Teacher Meeting", EventType.MEETING, d(y, 11, 28), description = "Term 1 progress review with the class teacher."),
            SchoolEvent("term1", "Term 1 $exam", EventType.EXAM, d(y, 12, 7), d(y, 12, 11), "Term 1 $exam. The date sheet and syllabus are shared in the weekly diary."),
            SchoolEvent("sports", "Annual Sports Day", EventType.EVENT, d(y, 12, 18), description = "Track events, team games and the house cup. Students come in sports uniform."),
            SchoolEvent("winter", "Winter Break", EventType.HOLIDAY, d(y, 12, 21), d(n, 1, 1), "School closed for winter break. Classes resume on the next school day."),
            SchoolEvent("reopen", "School Reopens", EventType.EVENT, d(n, 1, 5), description = "Classes resume after the winter break."),
            SchoolEvent("annual", "Annual Day Concert", EventType.EVENT, d(n, 2, 12), description = "Performances by every class. Seating details will be shared closer to the date."),
            SchoolEvent("ptm3", "Parent–Teacher Meeting", EventType.MEETING, d(n, 2, 26), description = "Progress review and preparation for final $exam."),
            SchoolEvent("ut2", "Unit Test 2", EventType.EXAM, d(n, 3, 9), d(n, 3, 13), "Second unit test for all subjects."),
            SchoolEvent("spring", "Spring Break", EventType.HOLIDAY, d(n, 3, 23), d(n, 3, 27), "School closed for spring break."),
            SchoolEvent("final", "Final $exam", EventType.EXAM, d(n, 4, 13), d(n, 4, 24), "End-of-year $exam. Please ensure regular attendance and rest."),
            SchoolEvent("labour", "Labour Day", EventType.HOLIDAY, d(n, 5, 1), description = "School closed."),
            SchoolEvent("result", "Result Day & Parent Meeting", EventType.MEETING, d(n, 5, 15), description = "Collect report cards and meet the class teacher."),
            SchoolEvent("summer", "Summer Vacation", EventType.HOLIDAY, d(n, 6, 8), d(n, 8, 14), "Have a wonderful summer! Holiday homework is shared in the weekly diary."),
        )
    }
}

/** Sample diary content bank. */
private object Content {

    class Bank(
        val classwork: List<String>,
        val homework: List<String>,
        val topics: List<Pair<String, List<String>>>,
    )

    val weeklyFocus = listOf(
        "Reading week — please read with your child for 15 minutes every evening.",
        "Kindness week — we are practising sharing, helping and saying thank you.",
        "Healthy habits — pack a fruit or vegetable snack every day this week.",
        "Revision week — short daily revision helps more than long sessions.",
        "Curiosity week — encourage your child to ask “why?” and find answers together.",
        "Organisation week — help your child pack their own school bag each evening.",
    )

    fun teacherNotes(c: SchoolClass): List<String?> = if (c.isEarlyYears) listOf(
        "Please send a spare set of clothes in your child's bag.",
        null,
        "Show-and-tell on Friday: your child may bring one favourite toy.",
        "Thank you for sending the leaves for our art activity!",
        null,
        "Please ensure water bottles are labelled.",
        "Library day tomorrow — please return the picture book.",
    ) else listOf(
        "Please sign the test copy and return it tomorrow.",
        null,
        "Bring the science project material by Thursday.",
        "Library period tomorrow — please return borrowed books.",
        null,
        "Revise spelling list for Friday's dictation.",
        "Well done to everyone on a great week of reading!",
    )

    fun bank(s: Subject): Bank = when (s) {
        Subject.PHONICS -> Bank(
            listOf(
                "Learnt the sound of letter ‘b’ with picture cards.",
                "Traced letters a–e on the activity sheet.",
                "Story time: listened to “The Little Red Hen” and answered questions.",
                "Matched beginning sounds to pictures.",
                "Practised pencil grip with pattern writing.",
            ),
            listOf(
                "Find 3 things at home that start with ‘b’ and say them aloud.",
                "Trace letters a–e in the activity book, page 12.",
                "Colour the ‘Letter Sounds’ worksheet.",
            ),
            listOf(
                "Letter sounds a–e" to listOf("Recognise the sounds of a–e", "Match sounds to pictures"),
                "Pattern writing" to listOf("Hold a pencil correctly", "Trace standing, sleeping and curved lines"),
                "Listening & speaking" to listOf("Listen to a short story", "Answer simple ‘who’ and ‘what’ questions"),
            ),
        )
        Subject.NUMBERS -> Bank(
            listOf(
                "Counted objects 1–10 using beads.",
                "Learnt number 6 — writing and counting.",
                "Sorted shapes: circle, square and triangle.",
                "Big and small: compared objects in class.",
                "Number rhyme and counting game.",
            ),
            listOf(
                "Count the spoons at home and draw them.",
                "Write numbers 1–5 in the number book.",
                "Find circle-shaped things at home.",
            ),
            listOf(
                "Counting 1–10" to listOf("Count objects up to 10", "Recognise numerals 1–10"),
                "Shapes around us" to listOf("Name circle, square and triangle", "Find shapes in the classroom"),
                "Comparing" to listOf("Big and small", "More and less"),
            ),
        )
        Subject.GENERAL -> Bank(
            listOf(
                "My family: talked about family members.",
                "Fruits and vegetables: identified them by colour.",
                "Good habits: washing hands before meals.",
                "Animals and the sounds they make.",
                "Means of transport: land, water and air.",
            ),
            listOf(
                "Paste a family photo in the scrapbook.",
                "Bring one fruit to share on Friday.",
            ),
            listOf(
                "Myself and my family" to listOf("Say their name and age", "Name family members"),
                "Good habits" to listOf("Wash hands properly", "Keep the classroom tidy"),
                "Animals around us" to listOf("Name farm and pet animals", "Copy animal sounds"),
            ),
        )
        Subject.RHYMES -> Bank(
            listOf(
                "Learnt the rhyme “Twinkle, Twinkle, Little Star”.",
                "Action song with clapping rhythm.",
                "Musical instruments: drum and bell.",
            ),
            listOf("Practise the new rhyme with family."),
            listOf(
                "Rhymes with actions" to listOf("Recite a rhyme with actions", "Keep a simple beat"),
                "Sounds and music" to listOf("Tell loud and soft sounds apart", "Play a simple percussion instrument"),
            ),
        )
        Subject.ART -> Bank(
            listOf(
                "Finger painting: autumn leaves.",
                "Paper folding: made a boat.",
                "Colouring within the lines.",
                "Clay modelling: fruits.",
                "Collage using coloured paper.",
            ),
            listOf("Collect a few dry leaves for tomorrow's leaf art."),
            listOf(
                "Colours and textures" to listOf("Mix two colours to make a new one", "Explore different textures"),
                "Paper craft" to listOf("Fold paper along a line", "Make a simple paper boat"),
            ),
        )
        Subject.PLAY -> Bank(
            listOf(
                "Ball throwing and catching.",
                "Obstacle course and balance walk.",
                "Freeze dance and movement games.",
            ),
            emptyList(),
            listOf(
                "Gross motor skills" to listOf("Throw and catch a ball", "Balance on one foot"),
                "Team games" to listOf("Take turns", "Follow simple game rules"),
            ),
        )
        Subject.ENGLISH -> Bank(
            listOf(
                "Reading comprehension: “The Clever Crow”.",
                "Nouns and pronouns — exercise 3A.",
                "Creative writing: describe your favourite place.",
                "Spelling dictation — list 6.",
                "Punctuation: full stops, commas and question marks.",
                "Reading aloud and new vocabulary.",
            ),
            listOf(
                "Learn spelling list 7 for Friday's dictation.",
                "Complete workbook page 24 (pronouns).",
                "Write 5 sentences about your weekend.",
                "Read chapter 3 of the reader and note 5 new words.",
            ),
            listOf(
                "Nouns and pronouns" to listOf("Identify nouns in a sentence", "Replace nouns with pronouns", "Use pronouns in own writing"),
                "Reading comprehension" to listOf("Read a passage fluently", "Answer factual and inference questions"),
                "Descriptive writing" to listOf("Use adjectives to describe", "Write a paragraph with a clear opening"),
                "Punctuation" to listOf("Use full stops and capital letters", "Use question marks and commas correctly"),
            ),
        )
        Subject.MATHS -> Bank(
            listOf(
                "Place value up to thousands.",
                "Addition with regrouping — practice sheet.",
                "Multiplication tables 6 and 7.",
                "Introduction to fractions using shapes.",
                "Word problems on money.",
                "Measuring length in cm and m.",
            ),
            listOf(
                "Exercise 4.2, questions 1–10.",
                "Learn tables 6 and 7.",
                "Fractions worksheet (both sides).",
                "Solve 5 word problems from page 58.",
            ),
            listOf(
                "Place value" to listOf("Read and write large numbers", "Find the place value of a digit", "Compare and order numbers"),
                "Fractions" to listOf("Understand halves, thirds and quarters", "Shade fractions of shapes"),
                "Measurement" to listOf("Measure length using a ruler", "Convert between cm and m"),
                "Multiplication" to listOf("Recall tables 2–10", "Solve simple multiplication word problems"),
            ),
        )
        Subject.SCIENCE -> Bank(
            listOf(
                "Parts of a plant and their functions.",
                "States of matter: solid, liquid and gas.",
                "The water cycle — diagram and discussion.",
                "Food and nutrition: a balanced diet.",
                "Simple machines around us.",
            ),
            listOf(
                "Label the parts of a plant diagram.",
                "List 5 solids, liquids and gases at home.",
                "Draw and label the water cycle.",
                "Plan a balanced lunchbox for one day.",
            ),
            listOf(
                "Plants" to listOf("Name the parts of a plant", "Explain what each part does"),
                "States of matter" to listOf("Classify materials as solid, liquid or gas", "Describe melting and freezing"),
                "The water cycle" to listOf("Explain evaporation and condensation", "Draw the water cycle"),
            ),
        )
        Subject.EVS -> Bank(
            listOf(
                "Our neighbourhood helpers.",
                "Living and non-living things.",
                "Seasons and the clothes we wear.",
                "Keeping our surroundings clean.",
            ),
            listOf(
                "Draw your favourite community helper.",
                "List 5 living and 5 non-living things.",
            ),
            listOf(
                "Community helpers" to listOf("Name community helpers", "Explain how they help us"),
                "Living and non-living" to listOf("Sort objects as living or non-living", "Give reasons for sorting"),
                "Seasons" to listOf("Name the seasons", "Match clothes to the weather"),
            ),
        )
        Subject.SOCIAL -> Bank(
            listOf(
                "Maps and directions.",
                "Landforms: mountains, plains and plateaus.",
                "Our community and local government.",
                "Natural resources and conservation.",
                "Early human settlements.",
            ),
            listOf(
                "Mark the four directions on a map of your home.",
                "Answer questions 1–5 on page 40.",
                "Make a poster on saving water.",
            ),
            listOf(
                "Maps and directions" to listOf("Use the four main directions", "Read simple map symbols"),
                "Landforms" to listOf("Describe mountains, plains and plateaus", "Locate landforms on a map"),
                "Natural resources" to listOf("Name renewable and non-renewable resources", "Suggest ways to conserve them"),
            ),
        )
        Subject.COMPUTER -> Bank(
            listOf(
                "Parts of a computer.",
                "Typing practice — home row keys.",
                "Introduction to Scratch: moving a sprite.",
                "Using a paint program to draw shapes.",
                "Staying safe on the internet.",
            ),
            listOf(
                "Label the parts of a computer worksheet.",
                "Write 3 rules for staying safe online.",
            ),
            listOf(
                "Computer basics" to listOf("Name input and output devices", "Start and shut down safely"),
                "Coding with Scratch" to listOf("Move and turn a sprite", "Use a loop block"),
                "Online safety" to listOf("Keep personal information private", "Tell an adult if something feels wrong"),
            ),
        )
    }
}
