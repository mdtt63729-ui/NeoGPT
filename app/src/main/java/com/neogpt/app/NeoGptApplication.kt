package com.neogpt.app

import android.app.Application
import androidx.room.Room
import com.neogpt.app.data.local.NeoDatabase

class NeoGptApplication : Application() {
    val database: NeoDatabase by lazy {
        Room.databaseBuilder(this, NeoDatabase::class.java, "neo_gpt.db").build()
    }
}
