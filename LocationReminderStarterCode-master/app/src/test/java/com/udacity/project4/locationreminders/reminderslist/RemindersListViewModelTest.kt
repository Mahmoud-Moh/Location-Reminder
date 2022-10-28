package com.udacity.project4.locationreminders.reminderslist


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var viewModel: RemindersListViewModel

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        stopKoin()

        fakeDataSource = FakeDataSource()

    }


    @Test
    fun withReminders_resultNotEmpty() = runBlockingTest {
        viewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
        fakeDataSource.saveReminder(
            ReminderDTO(
                "Test",
                "testing",
                null,
                2.89893,
                1.98893,
                "100"
            )
        )

        viewModel.loadReminders()

        assertThat(viewModel.remindersList.getOrAwaitValue().isEmpty()).isFalse()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()
        assertThat(viewModel.showNoData.getOrAwaitValue()).isFalse()
    }


    @Test
    fun ifNoReminders_showsNoData() = runBlockingTest {
        viewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
        viewModel.loadReminders()
        assertThat(viewModel.remindersList.getOrAwaitValue().isEmpty()).isTrue()
        assertThat(viewModel.showNoData.getOrAwaitValue()).isTrue()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()
    }


    @Test
    fun check_loading() = runBlockingTest {
        viewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
        mainCoroutineRule.pauseDispatcher()
        viewModel.loadReminders()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()

    }

    @Test
    fun shouldReturnErrorTest() = runBlockingTest {
        fakeDataSource.setShouldReturnError(true)
        viewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
        viewModel.loadReminders()
        assertThat(viewModel.showSnackBar.value!!.equals(Result.Error("Error occurred").message))
    }

}