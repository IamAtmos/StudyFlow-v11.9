package com.studyflow.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.studyflow.app.viewmodel.TimerViewModel

/**
 * Receives the exact alarm scheduled by [TimerViewModel] when the countdown
 * timer starts. This is what makes the timer reliable when the app is
 * backgrounded or its process is killed — AlarmManager wakes the device and
 * delivers this broadcast independent of any live ViewModel/coroutine, which
 * a `delay()`-based loop tied to viewModelScope simply cannot guarantee.
 */
class TimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        TimerViewModel.onAlarmFired(context)
    }
}
