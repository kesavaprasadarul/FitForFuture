package com.bosch.vaehiclefitness.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.*

class AudioRecorderViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val SEARCH_REFRESH_INTERVAL_MILLIS = 1000L
        private const val TIME_24_HOUR_FORMAT = "HH:mm:ss"
    }

    private var timeDuration = 0L
    private var timer = Timer()
    private val recordTimerLiveData = MutableLiveData<String>()

    fun getRecordTimerLiveData(): LiveData<String> = recordTimerLiveData

    fun startRecordingTimer() {
        stopRecordingTimer()
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                timeDuration++
                recordTimerLiveData.postValue(formatTime(timeDuration * 1000))
            }
        }
        timer.scheduleAtFixedRate(
            timerTask, SEARCH_REFRESH_INTERVAL_MILLIS,
            SEARCH_REFRESH_INTERVAL_MILLIS
        )
    }

    fun stopRecordingTimer() {
        timer.cancel()
        timer = Timer()
        timeDuration = 0
    }

    fun formatTime(time: Long): String? {
        val formatter = SimpleDateFormat(TIME_24_HOUR_FORMAT, Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(time)
    }

}