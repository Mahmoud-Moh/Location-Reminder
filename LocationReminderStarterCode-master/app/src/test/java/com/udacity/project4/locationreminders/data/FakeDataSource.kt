package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeDataSource(
    val reminders: MutableList<ReminderDTO> = mutableListOf()
) : ReminderDataSource {
    private var shouldReturnError = false

    fun setShouldReturnError(shouldReturn: Boolean) {
        this.shouldReturnError = shouldReturn
    }
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        reminders?.let { return Result.Success(ArrayList(it)) }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = reminders.find{it.id == id}
        if(reminder == null)
            return Result.Error("not found")
        else
            return Result.Success(reminder)

    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

}