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
import org.orbitmvi.orbit.syntax.simple.repeatOnSubscription
import org.orbitmvi.orbit.viewmodel.container
import xyz.teamgravity.todo.R
import xyz.teamgravity.todo.data.model.TodoModel
import xyz.teamgravity.todo.data.repository.TodoRepository
import javax.inject.Inject

@Parcelize
data class AddTodoState(
    var name: String = "",
    var important: Boolean = false,
) : Parcelable

sealed class AddTodoSideEffect {
    data class InvalidInput(@StringRes val message: Int) : AddTodoSideEffect()
    object TodoAdded : AddTodoSideEffect()
}

@HiltViewModel
class AddTodoViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val repository: TodoRepository
) : ViewModel(), ContainerHost<AddTodoState, AddTodoSideEffect> {

    override val container = container<AddTodoState, AddTodoSideEffect>(AddTodoState(), handle)

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

    fun onSaveTodo() = intent {
        if (state.name.isBlank()) {
            postSideEffect(AddTodoSideEffect.InvalidInput(R.string.error_name))
            return@intent
        }

        repository.insertTodoSync(
            TodoModel(
                name = state.name,
                important = state.important
            )
        )
        postSideEffect(AddTodoSideEffect.TodoAdded)
    }
}