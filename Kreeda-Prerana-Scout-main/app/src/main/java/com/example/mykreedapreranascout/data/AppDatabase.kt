package com.example.mykreedapreranascout.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Athlete::class, Trial::class], version = 2, exportSchema = false) // Changed to version 2
abstract class AppDatabase : RoomDatabase() {

    abstract fun athleteDao(): AthleteDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "kreeda_database")
                    .fallbackToDestructiveMigration() // This safely resets the DB for the new columns
                    .build()
                    .also { Instance = it }
            }
        }
    }
}