package xyz.teamgravity.todo.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class Preferences(context: Context) {

    companion object {
        /**
         *  Preferences name
         */
        private const val PREFS = "prefs"

        /**
         * Task sort order
         */
        private val TODO_SORT = stringPreferencesKey("taskSort")

        /**
         * Task hide completed
         */
        private val HIDE_COMPLETED = booleanPreferencesKey("hideCompleted")

        /**
         * Language
         */
        private val LANGUAGE = stringPreferencesKey("language")
    }

    private val store = context.dataStore

    val preferences: Flow<PreferencesModel> = store.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
            val sort = TodoSort.valueOf(preferences[TODO_SORT] ?: TodoSort.BY_DATE.name)

            val hideCompleted = preferences[HIDE_COMPLETED] ?: false

            val language = preferences[LANGUAGE] ?: "def"

            return@map PreferencesModel(sort = sort, hideCompleted = hideCompleted, language = language)
        }

    suspend fun updateTodoSort(sort: TodoSort) {
        store.edit { it[TODO_SORT] = sort.name }
    }

    suspend fun updateHideCompleted(hideCompleted: Boolean) {
        store.edit { it[HIDE_COMPLETED] = hideCompleted }
    }

    suspend fun updateLanguage(language: String) {
        store.edit { it[LANGUAGE] = language }
    }
}

data class PreferencesModel(
    val sort: TodoSort,
    val hideCompleted: Boolean,
    val language: String
)