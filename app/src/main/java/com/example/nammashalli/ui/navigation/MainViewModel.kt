package com.example.nammashalli.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammashalli.data.repository.RepairRepository
import com.example.nammashalli.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repairRepository: RepairRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val pendingRepairCount: StateFlow<Int> = sessionManager.userId
        .flatMapLatest { uid ->
            if (uid < 0L) flowOf(0)
            else repairRepository.getPending(uid).map { it.size }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
