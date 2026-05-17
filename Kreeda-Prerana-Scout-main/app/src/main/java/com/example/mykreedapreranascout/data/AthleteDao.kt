package com.example.mykreedapreranascout.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Update

@Dao
interface AthleteDao {

    // THE FIX: It is 'onConflict =', not 'onConflictStrategy ='
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAthlete(athlete: Athlete): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrial(trial: Trial): Long

    // Command to get a list of all athletes (hiding the archived ones)
    @Query("SELECT * FROM athlete WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllAthletes(): Flow<List<Athlete>>

    // Command to get all trials for a specific athlete
    @Query("SELECT * FROM trial WHERE athleteId = :athleteId ORDER BY trialDate ASC")
    fun getTrialsForAthlete(athleteId: Int): Flow<List<Trial>>

    // Command to archive an athlete without deleting data
    @Query("UPDATE athlete SET isArchived = 1 WHERE id = :athleteId")
    suspend fun archiveAthlete(athleteId: Int): Int

    @Query("UPDATE athlete SET earnedBadge = :badge WHERE id = :athleteId")
    suspend fun updateBadge(athleteId: Int, badge: String): Int

    @Query("DELETE FROM trial WHERE id = (SELECT id FROM trial WHERE athleteId = :athleteId ORDER BY trialDate DESC LIMIT 1)")
    suspend fun undoLastTrial(athleteId: Int): Int

    // Command to overwrite an existing athlete's profile data
    @Update
    suspend fun updateAthlete(athlete: Athlete): Int
}