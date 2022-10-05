/*
package com.udacity.project4.utils

import android.content.Context
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object ServiceLocator {

    private var database: RemindersDatabase? = null
    @Volatile
    var remindersLocalRepository: RemindersLocalRepository? = null

    var localdatasrc : LocalDB = null

    fun provideTasksRepository(context: Context): RemindersLocalRepository {
        synchronized(this) {
            return remindersLocalRepository ?: createTasksRepository(context)
        }
    }

    private fun createTasksRepository(context: Context): RemindersLocalRepository {
        val newRepo = RemindersLocalRepository()
        remindersLocalRepository = newRepo
        return newRepo
    }

    private fun createTaskLocalDataSource(context: Context): TasksDataSource {
        val database = database ?: createDataBase(context)
        return TasksLocalDataSource(database.taskDao())
    }

    private fun createDataBase(context: Context): ToDoDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            ToDoDatabase::class.java, "Tasks.db"
        ).build()
        database = result
        return result
    }
}
*/
