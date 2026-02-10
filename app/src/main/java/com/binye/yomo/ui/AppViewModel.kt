package com.binye.yomo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binye.yomo.data.repository.DeviceSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val deviceSyncRepository: DeviceSyncRepository
) : ViewModel() {

    fun onSignedIn() {
        viewModelScope.launch {
            deviceSyncRepository.registerDevice()
            deviceSyncRepository.updateLastActive()
        }
    }
}

