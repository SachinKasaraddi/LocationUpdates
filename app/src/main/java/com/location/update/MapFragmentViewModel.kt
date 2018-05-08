package com.location.update

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.location.Location

class MapFragmentViewModel(application: Application) : AndroidViewModel(application) {

    var currentLocation: MutableLiveData<Location>? = MutableLiveData()

    fun updateLocation(location: Location?) {
        currentLocation?.value = location
    }


}