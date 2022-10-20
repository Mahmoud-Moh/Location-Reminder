package com.udacity.project4.locationreminders.savereminder

import android.content.Context
import android.provider.Settings.Global.getString
import android.provider.Settings.System.getString
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel

    private val reminderData = ReminderDataItem(
        title = "Test",
        description = "testing",
        location = "test",
        latitude = 20.0,
        longitude = 1.30,
        id="300"
    )

    private val reminderData2 = ReminderDataItem(
        title = "",
        description = "testing",
        location = "test",
        latitude = 25.0,
        longitude = 24.0,
        id="301"
    )

    private val reminderData3 = ReminderDataItem(
        title = null,
        description = "testing",
        location = "test",
        latitude = 28.0,
        longitude = 80.0,
        id="302"
    )

    private val reminderData4 = ReminderDataItem(
        title = "title",
        description = "testing",
        location = null,
        latitude = 78.2,
        longitude = 10.0,
        id="302"
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()


    @Before
    fun setup() {
        stopKoin()

        fakeDataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun test1() {
        val isValid = viewModel.validateAndSaveReminder(reminderData)
        assertThat(isValid).isTrue()
        assertThat(viewModel.showToast.getOrAwaitValue().equals(ApplicationProvider.getApplicationContext<Context?>().getString(R.string.reminder_saved))).isTrue()
        assertThat(viewModel.navigationCommand.getOrAwaitValue()).isEqualTo(NavigationCommand.Back)

    }




    @Test
    fun savingItemWithEmptyTitle() {
        val returnValue = viewModel.validateAndSaveReminder(reminderData2)
        assertThat(returnValue).isFalse()
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)
    }

    @Test
    fun savingItemWithNullTitle() {
        val returnValue = viewModel.validateAndSaveReminder(reminderData3)
        assertThat(returnValue).isFalse()
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)
    }

    @Test
    fun savingItemWithNullLocation() {
        val returnValue = viewModel.validateAndSaveReminder(reminderData4)
        assertThat(returnValue).isFalse()
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)
    }



}