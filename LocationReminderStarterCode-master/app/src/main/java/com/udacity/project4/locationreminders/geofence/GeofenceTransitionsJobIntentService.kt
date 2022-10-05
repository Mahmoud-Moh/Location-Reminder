package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.common.GooglePlayServicesUtil.getErrorString
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private val remindersLocalRepository by inject<ReminderDataSource>()

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val TAG = "GeofenceTransitionsJobI"
        val ID = 100

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, GeofenceTransitionsJobIntentService::class.java, ID, intent)
        }
    }

    private fun sendNotification(geofence: Geofence) {
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            val result = remindersLocalRepository.getReminder(geofence.requestId)

            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                sendNotification(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }

    override fun onHandleWork(intent: Intent) {

        val geofencingEvent = GeofencingEvent . fromIntent (intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "" + getErrorString(geofencingEvent.getErrorCode()));
            return;
        }
        val geofenceTransition = geofencingEvent.getGeofenceTransition()

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            for(geofence in geofencingEvent.triggeringGeofences)
                sendNotification(geofence)
        }
    }
}
