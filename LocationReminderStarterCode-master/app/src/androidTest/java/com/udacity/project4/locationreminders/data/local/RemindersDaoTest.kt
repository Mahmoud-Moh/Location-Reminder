package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase
    private lateinit var dao: RemindersDao



    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries()
            .build()

        dao = database.reminderDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertIntoDBSucceeds() = runBlockingTest{
        val reminder1 = ReminderDTO(
        title = "rem1",
        description = "a rem",
        location = "Somewhere",
        latitude = 30.0,
        longitude =20.0 ,
        id="56555")
        dao.saveReminder(reminder1)

        val reminder2 = ReminderDTO(
            title = "rem2",
            description = "a rem2",
            location = "Somewhere2",
            latitude = 30.0,
            longitude =20.0 ,
            id="565519")
        dao.saveReminder(reminder1)

        assertThat(dao.getReminders()).contains(reminder1)
    }

    @Test
    fun retrieveFromDBSucceeds() = runBlockingTest {
        val reminder1 = ReminderDTO(
            title = "rem1",
            description = "a rem",
            location = "Somewhere",
            latitude = 30.0,
            longitude =20.0 ,
            id="56555")
        dao.saveReminder(reminder1)

        val reminder = dao.getReminderById(reminder1.id)
    }

    @Test
    fun deleteAllElements() = runBlockingTest {
        val reminder1 = ReminderDTO(
            title = "rem1",
            description = "a rem",
            location = "Somewhere",
            latitude = 30.0,
            longitude =20.0 ,
            id="56555")
        dao.saveReminder(reminder1)

        val reminder2 = ReminderDTO(
            title = "rem2",
            description = "a rem2",
            location = "Somewhere2",
            latitude = 30.0,
            longitude =20.0 ,
            id="565519")

        val reminder3 = ReminderDTO(
            title = "rem3",
            description = "a rem3",
            location = "Somewhere3",
            latitude = 30.0,
            longitude =20.0 ,
            id="10")


        dao.saveReminder(reminder1)
        dao.saveReminder(reminder2)
        dao.saveReminder(reminder3)
        dao.deleteAllReminders()
        assertThat(dao.getReminders() == null).isFalse()
        assertThat(dao.getReminders()).isEmpty()
    }
}