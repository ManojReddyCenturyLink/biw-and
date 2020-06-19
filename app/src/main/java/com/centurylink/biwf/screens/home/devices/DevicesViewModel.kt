package com.centurylink.biwf.screens.home.devices

import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.repos.DevicesRepository
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.preferences.Preferences
import kotlinx.coroutines.launch
import javax.inject.Inject

class DevicesViewModel @Inject constructor(
    private val sharedPreferences: Preferences,
    private val devicesRepository:DevicesRepository
) : BaseViewModel() {

    var errorMessageFlow = EventFlow<String>()
    var progressViewFlow = EventFlow<Boolean>()

    init {
       initApis()
    }

    fun initApis() {
        viewModelScope.launch {
            requestDevices()
        }
    }

     private suspend fun requestDevices(){

    }
}