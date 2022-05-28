package xyz.teamgravity.todo.presentation.viewmodel

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import xyz.teamgravity.todo.R
import xyz.teamgravity.todo.data.model.TodoModel
import xyz.teamgravity.todo.data.repository.TodoRepository
import xyz.teamgravity.todo.injection.FullTimeFormatter
import xyz.teamgravity.todo.presentation.screen.destinations.EditTodoScreenDestination
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@Parcelize
data class EditTodoState(
    val name: String,
    val important: Boolean,
    val timestamp: String
) : Parcelable

sealed class EditTodoSideEffect {
    data class InvalidInput(@StringRes val message: Int) : EditTodoSideEffect()
    object TodoUpdated : EditTodoSideEffect()
}

@HiltViewModel
class EditTodoViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val repository: TodoRepository,
    @FullTimeFormatter private val formatter: SimpleDateFormat
) : ViewModel(), ContainerHost<EditTodoState, EditTodoSideEffect> {

    val todo: TodoModel = EditTodoScreenDestination.argsFrom(handle).todo
    override val container = container<EditTodoState, EditTodoSideEffect>(
        EditTodoState(
            todo.name,
            todo.important,
            formatter.format(todo.timestamp)
        ),
        handle
    )

    fun onNameChange(value: String) = intent {
        reduce {
            state.copy(
                name = value
            )
        }
    }

    fun onImportantChange(value: Boolean) = intent {
        reduce {
            state.copy(
                important = value
            )
        }
    }

    fun onUpdateTodo() = intent {
        if (state.name.isBlank()) {
            postSideEffect(EditTodoSideEffect.InvalidInput(message = R.string.error_name))
            return@intent
        }

        repository.updateTodoSync(
             todo.copy(
                 name= state.name,
                 important = state.important
             )
        )

        postSideEffect(EditTodoSideEffect.TodoUpdated)
    }
}