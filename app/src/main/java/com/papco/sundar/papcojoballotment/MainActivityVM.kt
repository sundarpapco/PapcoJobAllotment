package com.papco.sundar.papcojoballotment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.papco.sundar.papcojoballotment.utility.EventMessage

class MainActivityVM(app:Application):AndroidViewModel(app){

    val eventBus=MutableLiveData<EventMessage>()
}