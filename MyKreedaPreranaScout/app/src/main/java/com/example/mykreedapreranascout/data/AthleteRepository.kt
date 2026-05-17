package com.example.mykreedapreranascout.data

import kotlinx.coroutines.flow.Flow

class AthleteRepository(private val athleteDao: AthleteDao) {

    // Gets the live list of all non-archived athletes
    val allAthletes: Flow<List<Athlete>> = athleteDao.getAllAthletes()

    // Inserts a new athlete
    suspend fun insertAthlete(athlete: Athlete) {
        athleteDao.insertAthlete(athlete)
    }

    // Inserts a new trial result
    suspend fun insertTrial(trial: Trial) {
        athleteDao.insertTrial(trial)
    }

    // Gets the history of trials for the Talent Curve
    fun getTrialsForAthlete(athleteId: Int): Flow<List<Trial>> {
        return athleteDao.getTrialsForAthlete(athleteId)
    }
    suspend fun archiveAthlete(id: Int) {
        athleteDao.archiveAthlete(id)
    }

    // Passes the badge update command to the DAO
    suspend fun updateBadge(athleteId: Int, badge: String) {
        athleteDao.updateBadge(athleteId, badge)
    }

    // Passes the undo command to the database
    suspend fun undoLastTrial(athleteId: Int) {
        athleteDao.undoLastTrial(athleteId)
    }

    // Passes the edit command to the database
    suspend fun updateAthlete(athlete: Athlete) {
        athleteDao.updateAthlete(athlete)
    }
}