package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.ParticipantEntity
import com.nima.app.imanage.data.db.entity.TripEntity
import com.nima.app.imanage.data.repository.ParticipantRepository
import com.nima.app.imanage.data.repository.TripRepository
import com.nima.app.imanage.ui.theme.NoteBoxPalettes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TripListViewModel(
    private val tripRepository: TripRepository,
    private val participantRepository: ParticipantRepository
) : ViewModel() {

    val trips = tripRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _tripParticipants = MutableStateFlow<List<ParticipantEntity>>(emptyList())
    val tripParticipants: StateFlow<List<ParticipantEntity>> = _tripParticipants.asStateFlow()

    private val _participantNames = MutableStateFlow<List<String>>(emptyList())
    val participantNames: StateFlow<List<String>> = _participantNames.asStateFlow()

    private val _hostId = MutableStateFlow<Int?>(null)
    val hostId: StateFlow<Int?> = _hostId.asStateFlow()

    private val _editingTrip = MutableStateFlow<TripEntity?>(null)
    val editingTrip: StateFlow<TripEntity?> = _editingTrip.asStateFlow()

    fun createTrip(
        name: String,
        startDate: Long,
        endDate: Long?,
        participantNames: List<String>,
        hostIndex: Int?
    ) {
        viewModelScope.launch {
            val trip = TripEntity(
                name = name,
                startDate = startDate,
                endDate = endDate,
                hostParticipantId = null,
                createdAt = System.currentTimeMillis()
            )
            tripRepository.insert(trip)
            val savedTrip = tripRepository.getLastInserted() ?: return@launch
            saveParticipantsAndHost(savedTrip, participantNames, hostIndex)
        }
    }

    fun updateTrip(
        tripId: Int,
        name: String,
        startDate: Long,
        endDate: Long?,
        participantNames: List<String>,
        hostIndex: Int?
    ) {
        viewModelScope.launch {
            val existing = tripRepository.getOnce(tripId) ?: return@launch
            existing.name = name
            existing.startDate = startDate
            existing.endDate = endDate
            val existingParticipants = participantRepository.getByTripOnce(tripId)
            existingParticipants.forEach { participantRepository.delete(it) }
            val updatedTrip = existing.copy()
            tripRepository.update(updatedTrip)
            saveParticipantsAndHost(updatedTrip, participantNames, hostIndex)
        }
    }

    private suspend fun saveParticipantsAndHost(
        trip: TripEntity,
        names: List<String>,
        hostIndex: Int?
    ) {
        val colorCount = NoteBoxPalettes.size
        val participants = names.filter { it.isNotBlank() }.mapIndexed { index, name ->
            ParticipantEntity(
                tripId = trip.id,
                name = name.trim(),
                colorIndex = index % colorCount
            )
        }
        participants.forEach { participantRepository.insert(it) }
        val savedParticipants = participantRepository.getByTripOnce(trip.id)
        val hostId = if (hostIndex != null && hostIndex < savedParticipants.size) {
            savedParticipants[hostIndex].id
        } else {
            savedParticipants.firstOrNull()?.id
        }
        trip.hostParticipantId = hostId
        tripRepository.update(trip)
    }

    fun deleteTrip(trip: TripEntity) {
        viewModelScope.launch { tripRepository.delete(trip) }
    }

    fun loadTripForEdit(tripId: Int) {
        viewModelScope.launch {
            val trip = tripRepository.getOnce(tripId)
            _editingTrip.value = trip
            if (trip != null) {
                val participants = participantRepository.getByTripOnce(tripId)
                _tripParticipants.value = participants
                _participantNames.value = participants.map { it.name }
                _hostId.value = trip.hostParticipantId
            }
        }
    }

    fun resetEditState() {
        _editingTrip.value = null
        _tripParticipants.value = emptyList()
        _participantNames.value = emptyList()
        _hostId.value = null
    }

    fun addParticipantName(name: String) {
        _participantNames.value = _participantNames.value + name
    }

    fun updateParticipantName(index: Int, name: String) {
        val list = _participantNames.value.toMutableList()
        if (index < list.size) list[index] = name
        _participantNames.value = list
    }

    fun removeParticipantName(index: Int) {
        val list = _participantNames.value.toMutableList()
        if (index < list.size) list.removeAt(index)
        _participantNames.value = list
    }

    fun setHostIndex(index: Int?) {
        _hostId.value = index?.let { idx ->
            val participants = _tripParticipants.value
            if (participants.isEmpty()) _participantNames.value.getOrNull(idx)?.hashCode()
            else participants.getOrNull(idx)?.id
        }
    }
}
