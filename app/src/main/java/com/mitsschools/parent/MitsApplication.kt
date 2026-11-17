package com.mitsschools.parent

import android.app.Application
import com.mitsschools.parent.data.MockSchoolRepository
import com.mitsschools.parent.data.SchoolRepository
import com.mitsschools.parent.data.SessionStore

class MitsApplication : Application() {

    /** Swap MockSchoolRepository for a real API-backed implementation when the backend is ready. */
    val repository: SchoolRepository by lazy { MockSchoolRepository() }

    val sessionStore: SessionStore by lazy { SessionStore(this) }
}
