package com.studyflow.app.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.AlarmReceiver
import com.studyflow.app.TimerAlarmReceiver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ── Timer reliability ────────────────────────────────────────────────────────
 *
 * The actual "did time run out" deadline is scheduled with AlarmManager
 * (wall-clock based, OS-level, survives the app being backgrounded or its
 * process killed). The coroutine loop in this ViewModel only exists to
 * animate the on-screen countdown WHILE the app is open — it recomputes
 * "remaining" from the same absolute target timestamp every tick, so it can
 * never drift even if a tick is delayed; it does not own the deadline.
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val ACTION_STOP_ALARM = "com.studyflow.app.STOP_ALARM"

        private const val CHANNEL_ID         = "studyflow_timer"
        private const val NOTIFICATION_ID    = 1001
        private const val ALARM_REQUEST_CODE = 5001
        private const val PREFS_NAME         = "studyflow_timer"

        @Volatile private var currentRingtone: Ringtone? = null

        fun stopAlarmStatic(context: Context) {
            currentRingtone?.stop()
            currentRingtone = null
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(NOTIFICATION_ID)
        }

        /**
         * Called by [TimerAlarmReceiver] when the scheduled alarm fires.
         * Works even if the app process is completely dead — it only needs
         * a Context (supplied by the OS when delivering the broadcast), not
         * a live ViewModel instance.
         */
        fun onAlarmFired(context: Context) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean("is_running", false)
                .putBoolean("is_finished", true)
                .putLong("remaining_ms", 0L)
                .remove("target_time_ms")
                .apply()
            fireAlarmStatic(context)
        }

        private fun alarmPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, TimerAlarmReceiver::class.java)
            return PendingIntent.getBroadcast(
                context, ALARM_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun scheduleExactAlarm(context: Context, targetTimeMillis: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = alarmPendingIntent(context)
            val canBeExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
            if (canBeExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTimeMillis, pendingIntent)
            } else {
                // Graceful fallback — still bypasses Doze reasonably well,
                // just without the "exact" guarantee, and needs no special
                // user-granted permission on any API level.
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTimeMillis, pendingIntent)
            }
        }

        private fun cancelScheduledAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(alarmPendingIntent(context))
        }

        private fun fireAlarmStatic(context: Context) {
            ensureChannelStatic(context)
            vibrateStatic(context)

            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            currentRingtone = RingtoneManager.getRingtone(context, alarmUri)?.also { it.play() }

            val stopPending = PendingIntent.getBroadcast(
                context, 0,
                Intent(context, AlarmReceiver::class.java).setAction(ACTION_STOP_ALARM),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val openPending = PendingIntent.getActivity(
                context, 1,
                Intent(context, Class.forName("com.studyflow.app.MainActivity"))
                    .apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("⏳ StudyFlow — Timer Done!")
                .setContentText("تایمر تموم شد. ضربه بزن برای بستن.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(openPending)
                .addAction(android.R.drawable.ic_media_pause, "Stop Alarm", stopPending)
                .build()

            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(NOTIFICATION_ID, notification)
        }

        private fun vibrateStatic(context: Context) {
            val pattern = longArrayOf(0, 500, 200, 500, 200, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(VibratorManager::class.java)
                    ?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)?.let { v ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        v.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    else @Suppress("DEPRECATION") v.vibrate(pattern, -1)
                }
            }
        }

        private fun ensureChannelStatic(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val ch = NotificationChannel(CHANNEL_ID, "Timer Alarm", NotificationManager.IMPORTANCE_HIGH)
                context.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
            }
        }
    }

    private val ctx   = application.applicationContext
    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _presets = MutableStateFlow(mutableListOf(5, 10, 25, 50))
    val presets: StateFlow<MutableList<Int>> = _presets

    private val _totalMillis = MutableStateFlow(25 * 60_000L)
    val totalMillis: StateFlow<Long> = _totalMillis

    private val _remainingMillis = MutableStateFlow(25 * 60_000L)
    val remainingMillis: StateFlow<Long> = _remainingMillis

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished

    private var tickJob: Job? = null

    init {
        restoreFromPrefs()
    }

    /**
     * Recomputes the on-screen state from what AlarmManager / the receiver
     * already know, rather than trusting a coroutine that may not have
     * survived. Called on every ViewModel (re)creation — i.e. every time
     * the app is reopened.
     */
    private fun restoreFromPrefs() {
        _totalMillis.value = prefs.getLong("total_ms", _totalMillis.value)

        val wasFinished = prefs.getBoolean("is_finished", false)
        if (wasFinished) {
            _isFinished.value      = true
            _isRunning.value       = false
            _remainingMillis.value = 0L
            return
        }

        val wasRunning = prefs.getBoolean("is_running", false)
        if (!wasRunning) {
            _remainingMillis.value = prefs.getLong("remaining_ms", _totalMillis.value)
            return
        }

        val targetTime = prefs.getLong("target_time_ms", 0L)
        val remaining  = (targetTime - System.currentTimeMillis()).coerceAtLeast(0L)

        if (remaining <= 0L) {
            // The deadline already passed while we were away. Normally the
            // alarm receiver already handled this (is_finished would be
            // true), but if for any reason it hasn't yet, don't leave the
            // UI stuck mid-countdown.
            _isRunning.value       = false
            _isFinished.value      = true
            _remainingMillis.value = 0L
            persistWidgetState()
        } else {
            _remainingMillis.value = remaining
            _isRunning.value       = true
            startTicking()
        }
    }

    fun addPreset(minutes: Int) {
        if (minutes <= 0 || minutes > 999 || _presets.value.contains(minutes)) return
        _presets.value = _presets.value.toMutableList().also { it.add(minutes); it.sort() }
    }

    fun removePreset(minutes: Int) {
        _presets.value = _presets.value.toMutableList().also { it.remove(minutes) }
    }

    fun setPreset(minutes: Int) {
        if (_isRunning.value) return
        _totalMillis.value     = minutes * 60_000L
        _remainingMillis.value = minutes * 60_000L
        _isFinished.value      = false
        persistWidgetState()
    }

    fun setCustomTime(minutes: Int) {
        if (_isRunning.value || minutes <= 0) return
        _totalMillis.value     = minutes * 60_000L
        _remainingMillis.value = minutes * 60_000L
        _isFinished.value      = false
        persistWidgetState()
    }

    fun start(context: Context) {
        if (_isRunning.value) return
        stopAlarmStatic(context)
        _isFinished.value = false
        _isRunning.value  = true

        val targetTime = System.currentTimeMillis() + _remainingMillis.value
        prefs.edit().putLong("target_time_ms", targetTime).apply()
        scheduleExactAlarm(context, targetTime)
        persistWidgetState()
        startTicking()
    }

    /** Foreground-only cosmetic countdown — always re-derives from the
     *  absolute target timestamp, so a slow/late tick never causes drift. */
    private fun startTicking() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (_isRunning.value) {
                val targetTime = prefs.getLong("target_time_ms", 0L)
                val remaining  = (targetTime - System.currentTimeMillis()).coerceAtLeast(0L)
                _remainingMillis.value = remaining
                persistWidgetState()

                if (remaining <= 0L) {
                    _isRunning.value  = false
                    _isFinished.value = true
                    persistWidgetState()
                    // We got here first — cancel the OS alarm so it doesn't
                    // also fire a moment later and double up the alert.
                    cancelScheduledAlarm(ctx)
                    fireAlarmStatic(ctx)
                    break
                }
                delay(1_000L)
            }
        }
    }

    fun pause() {
        _isRunning.value = false
        tickJob?.cancel()
        cancelScheduledAlarm(ctx)
        prefs.edit().remove("target_time_ms").apply()
        persistWidgetState()
    }

    fun reset() {
        pause()
        stopAlarmStatic(ctx)
        _remainingMillis.value = _totalMillis.value
        _isFinished.value      = false
        persistWidgetState()
    }

    /** Called from UI dismiss button */
    fun dismissAlarm() {
        stopAlarmStatic(ctx)
        _isFinished.value      = false
        _remainingMillis.value = _totalMillis.value
        persistWidgetState()
    }

    private fun persistWidgetState() {
        prefs.edit()
            .putLong("remaining_ms", _remainingMillis.value)
            .putLong("total_ms", _totalMillis.value)
            .putBoolean("is_running", _isRunning.value)
            .putBoolean("is_finished", _isFinished.value)
            .apply()
    }

    override fun onCleared() {
        super.onCleared()
        tickJob?.cancel()
        // Deliberately NOT cancelling the scheduled AlarmManager alarm here —
        // onCleared() fires when the app is backgrounded/closed, which is
        // exactly when the real deadline still needs to fire.
    }
}
