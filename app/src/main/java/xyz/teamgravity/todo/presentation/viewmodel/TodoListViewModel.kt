package xyz.teamgravity.todo.presentation.viewmodel

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.parcelize.Parcelize
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import xyz.teamgravity.todo.core.util.Preferences
import xyz.teamgravity.todo.core.util.PreferencesModel
import xyz.teamgravity.todo.core.util.TodoSort
import xyz.teamgravity.todo.data.model.TodoModel
import xyz.teamgravity.todo.data.repository.TodoRepository
import javax.inject.Inject

@Parcelize
data class TodoListState(
    val query: String = "",
    var todos: List<TodoModel> = emptyList(),
    var searchExpanded: Boolean = false,
    var menuExpanded: Boolean = false,
    var sortExpanded: Boolean = false,
    var hideCompleted: Boolean = false,
    var deleteCompletedDialog: Boolean = false,
    var deleteAllDialog: Boolean = false
) : Parcelable

sealed class TodoListSideEffect() {
    object TodoDeleted : TodoListSideEffect()
}

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val repository: TodoRepository,
    private val preferences: Preferences
) : ViewModel(), ContainerHost<TodoListState, TodoListSideEffect> {

    override val container = container<TodoListState, TodoListSideEffect>(TodoListState())
    init
    {
        observeTodos()
    }

    private var deletedTodo: TodoModel? = null

    private fun observeTodos() = intent {
        // 目前来看 自动查询的触发 来自于 preferences
        // sort 和 hideCompleted 在UI侧被修改后 , 会执行store的修改方法, 执行后 preferences的 state会被VM观察到
        // flatMapLatest 会再次触发 repository.getTodos 然后UI刷新
        val preferencesModel: PreferencesModel = preferences.preferences.first()

        repository.getTodos(state.query, preferencesModel.hideCompleted, preferencesModel.sort)
            .collectLatest {
            reduce {
                state.copy(
                    hideCompleted = preferencesModel.hideCompleted,
                    todos = it
                )
            }
        }
    }


    fun onQueryChange(value: String) = intent {
        reduce {
            state.copy(
                query = value
            )
        }
    }

    fun onTodoChecked(todo: TodoModel, checked: Boolean) = intent {
        repository.updateTodoSync(todo.copy(completed = checked))
    }

    fun onTodoDelete(todo: TodoModel) = intent {
        deletedTodo = todo
        repository.deleteTodoSync(todo)
        postSideEffect(TodoListSideEffect.TodoDeleted)
    }

    fun onUndoDeletedTodo() = intent {
        deletedTodo?.let {
            repository.insertTodoSync(it.copy(_id = 0))
        }
    }

    fun onSearchExpanded() = intent {
        reduce {
            state.copy(
                searchExpanded = true
            )
        }
    }

    fun onSearchCollapsed() = intent {
        reduce {
            state.copy(
                query = "",
                searchExpanded = false
            )
        }
    }

    fun onMenuExpanded() = intent {
        reduce {
            state.copy(
                menuExpanded = true
            )
        }
    }

    fun onMenuCollapsed() = intent {
        reduce {
            state.copy(
                menuExpanded = false
            )
        }
    }

    fun onSortExpanded() = intent {
        reduce {
            state.copy(
                sortExpanded = true
            )
        }
    }

    fun onSortCollapsed() = intent {
        reduce {
            state.copy(
                sortExpanded = false
            )
        }
    }

    fun onSort(sort: TodoSort) = intent {
        preferences.updateTodoSort(sort = sort)
        onSortCollapsed()
    }

    fun onHideCompletedChange() = intent {
        preferences.updateHideCompleted(!state.hideCompleted)
        onMenuCollapsed()
    }

    fun onDeleteCompletedDialogShow() = intent {
        reduce {
            state.copy(
                deleteCompletedDialog = true
            )
        }
        onMenuCollapsed()
    }

    fun onDeleteCompletedDialogDismiss() = intent {
        reduce {
            state.copy(
                deleteCompletedDialog = false
            )
        }
    }

    fun onDeleteCompleted() = intent {
        repository.deleteAllCompletedTodo()
        onDeleteCompletedDialogDismiss()

    }

    fun onDeleteAllDialogShow() = intent {
        reduce {
            state.copy(
                deleteAllDialog = true
            )
        }
        onMenuCollapsed()
    }

    fun onDeleteAllDialogDismiss() = intent {
        reduce {
            state.copy(
                deleteAllDialog = false
            )
        }
    }

    fun onDeleteAll() = intent {
        repository.deleteAllTodo()
        onDeleteAllDialogDismiss()
    }

}