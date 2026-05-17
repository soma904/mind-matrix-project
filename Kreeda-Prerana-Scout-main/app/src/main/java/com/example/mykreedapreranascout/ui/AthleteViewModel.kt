package com.example.mykreedapreranascout.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mykreedapreranascout.data.Athlete
import com.example.mykreedapreranascout.data.AthleteRepository
import com.example.mykreedapreranascout.data.Trial
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AthleteViewModel(private val repository: AthleteRepository) : ViewModel() {

    // Holds the text typed into the search bar
    var searchQuery = mutableStateOf("")

    // The live list of athletes, filtered by search query and archive status
    val athletes: StateFlow<List<Athlete>> = repository.allAthletes
        .map { list ->
            list.filter { athlete ->
                // Only show athletes who are NOT archived
                val matchesArchive = !athlete.isArchived
                // Check if search text matches name or sport
                val matchesSearch = athlete.name.contains(searchQuery.value, ignoreCase = true) ||
                        athlete.sport.contains(searchQuery.value, ignoreCase = true)

                matchesArchive && matchesSearch
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Adds a new athlete with all required 5.1 fields
    // We added rollNumber: String right after name: String!
    fun addAthlete(name: String, rollNumber: String, age: Int, studentClass: String, school: String, sport: String, photoUri: String?) {
        viewModelScope.launch {
            val newAthlete = Athlete(
                name = name,
                rollNumber = rollNumber,
                age = age,
                studentClass = studentClass,
                school = school,
                sport = sport,
                photoUri = photoUri,
                isArchived = false
            )
            repository.insertAthlete(newAthlete)
        }
    }

    // Archives an athlete to hide them without losing their history
    fun archiveAthlete(id: Int) {
        viewModelScope.launch {
            repository.archiveAthlete(id)
        }
    }

    // Triggers the Undo command
    fun undoLastTrial(athleteId: Int) {
        viewModelScope.launch {
            repository.undoLastTrial(athleteId)
        }
    }

    // Triggers the profile edit
    fun updateAthlete(athlete: Athlete) {
        viewModelScope.launch {
            repository.updateAthlete(athlete)
        }
    }

    // Calculates a composite score based on the highest badge earned
    fun calculateCompositeScore(athlete: Athlete): Int {
        var baseScore = 0

        // Base points for having an active profile
        if (!athlete.isArchived) baseScore += 10

        // Massive points for hitting PRD Benchmarks
        when (athlete.earnedBadge) {
            "State Potential" -> baseScore += 300
            "District Level Ready" -> baseScore += 200
            "School Level Ready" -> baseScore += 100
        }
        return baseScore
    }

    // Records a trial and triggers the auto-badge logic
    // Records a trial and triggers the auto-badge gamification logic
    fun logTrial(athleteId: Int, eventType: String, result: Double, unit: String) {
        viewModelScope.launch {
            // 1. Save the trial
            val trial = Trial(athleteId = athleteId, eventType = eventType, resultValue = result, unit = unit)
            repository.insertTrial(trial)

            // 2. The Gamification Engine (Based on PRD U-17 Benchmarks)
            var badgeToAward: String? = null

            when (eventType) {
                "100m Sprint" -> {
                    if (result < 11.5) badgeToAward = "State Potential"
                    else if (result < 12.5) badgeToAward = "District Level Ready"
                    else if (result < 14.0) badgeToAward = "School Level Ready"
                }
                "400m Run" -> {
                    if (result < 58.0) badgeToAward = "State Potential"
                    else if (result < 65.0) badgeToAward = "District Level Ready"
                    else if (result < 75.0) badgeToAward = "School Level Ready"
                }
                "Long Jump" -> {
                    if (result > 5.5) badgeToAward = "State Potential"
                    else if (result > 4.5) badgeToAward = "District Level Ready"
                    else if (result > 3.5) badgeToAward = "School Level Ready"
                }
                "Shot Put" -> {
                    if (result > 10.0) badgeToAward = "State Potential"
                    else if (result > 8.0) badgeToAward = "District Level Ready"
                    else if (result > 6.0) badgeToAward = "School Level Ready"
                }
                "High Jump" -> {
                    if (result > 1.5) badgeToAward = "State Potential"
                    else if (result > 1.3) badgeToAward = "District Level Ready"
                    else if (result > 1.1) badgeToAward = "School Level Ready"
                }
            }

            // 3. If a badge was earned, save it permanently to the Athlete's profile!
            if (badgeToAward != null) {
                repository.updateBadge(athleteId, badgeToAward)
            }
        }
    }

    fun getAthleteById(athleteId: Int): Athlete? {
        return athletes.value.find { it.id == athleteId }
    }

    fun getTrialsForAthlete(athleteId: Int): kotlinx.coroutines.flow.Flow<List<Trial>> {
        return repository.getTrialsForAthlete(athleteId)
    }
}

class AthleteViewModelFactory(private val repository: AthleteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AthleteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AthleteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}