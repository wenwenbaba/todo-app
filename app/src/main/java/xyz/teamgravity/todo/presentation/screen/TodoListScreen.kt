package xyz.teamgravity.todo.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import xyz.teamgravity.todo.R
import xyz.teamgravity.todo.presentation.component.button.TodoFloatingActionButton
import xyz.teamgravity.todo.presentation.component.card.TodoSwipeCard
import xyz.teamgravity.todo.presentation.component.dialog.TodoAlertDialog
import xyz.teamgravity.todo.presentation.component.topbar.*
import xyz.teamgravity.todo.presentation.screen.destinations.AboutScreenDestination
import xyz.teamgravity.todo.presentation.screen.destinations.AddTodoScreenDestination
import xyz.teamgravity.todo.presentation.screen.destinations.EditTodoScreenDestination
import xyz.teamgravity.todo.presentation.theme.backgroundLayout
import xyz.teamgravity.todo.presentation.viewmodel.TodoListSideEffect
import xyz.teamgravity.todo.presentation.viewmodel.TodoListViewModel

@RootNavGraph(start = true)
@Destination
@Composable
fun TodoListScreen(
    viewmodel: TodoListViewModel = hiltViewModel(),
    scaffold: ScaffoldState = rememberScaffoldState(),
    navigator: DestinationsNavigator
) {

    val context = LocalContext.current
    val state = viewmodel.collectAsState().value

    suspend fun handleSideEffect(sideEffect: TodoListSideEffect) {
        when (sideEffect) {
            is TodoListSideEffect.TodoDeleted -> {
                val result = scaffold.snackbarHostState.showSnackbar(
                    message = context.getString(R.string.deleted_successfully),
                    actionLabel = context.getString(R.string.undo)
                )
                if (result == SnackbarResult.ActionPerformed) viewmodel.onUndoDeletedTodo()
            }
        }
    }


    viewmodel.collectSideEffect(sideEffect = ::handleSideEffect)

    Scaffold(
        scaffoldState = scaffold,
        topBar = {
            TopAppBar(
                title = {
                    if (state.searchExpanded) {
                        TopBarSearch(
                            query = state.query,
                            onQueryChange = viewmodel::onQueryChange,
                            onCancel = viewmodel::onSearchCollapsed
                        )
                    } else {
                        TopBarTitle(title = R.string.tasks)
                    }
                },
                actions = {
                    if (!state.searchExpanded) {
                        TopBarIconButton(
                            onClick = viewmodel::onSearchExpanded,
                            icon = Icons.Default.Search,
                            contentDescription = R.string.cd_search_button
                        )
                    }
                    TopBarSortMenu(
                        expanded = state.sortExpanded,
                        onExpand = viewmodel::onSortExpanded,
                        onDismiss = viewmodel::onSortCollapsed,
                        onSort = viewmodel::onSort
                    )
                    TopBarMoreMenu(
                        expanded = state.menuExpanded,
                        onExpand = viewmodel::onMenuExpanded,
                        onDismiss = viewmodel::onMenuCollapsed,
                        hideCompleted = state.hideCompleted,
                        onHideCompletedChange = viewmodel::onHideCompletedChange,
                        onDeleteCompletedClick = viewmodel::onDeleteCompletedDialogShow,
                        onDeleteAllClick = viewmodel::onDeleteAllDialogShow,
                        onAboutClick = {
                            navigator.navigate(AboutScreenDestination)
                            viewmodel.onMenuCollapsed()
                        }
                    )
                }
            )
        },
        floatingActionButton = {
            TodoFloatingActionButton(
                onClick = { navigator.navigate(AddTodoScreenDestination) },
                icon = Icons.Default.Add,
                contentDescription = R.string.cd_task_add
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.backgroundLayout)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    items = state.todos,
                    key = { it._id }
                ) { todo ->
                    TodoSwipeCard(
                        todo = todo,
                        onTodoClick = { navigator.navigate(EditTodoScreenDestination(todo = it)) },
                        onTodoCheckedChange = viewmodel::onTodoChecked,
                        onTodoDismissed = viewmodel::onTodoDelete
                    )
                }
            }
            if (state.deleteCompletedDialog) {
                TodoAlertDialog(
                    title = R.string.confirm_deletion,
                    message = R.string.wanna_delete_completed,
                    onDismiss = viewmodel::onDeleteCompletedDialogDismiss,
                    onConfirm = viewmodel::onDeleteCompleted
                )
            }
            if (state.deleteAllDialog) {
                TodoAlertDialog(
                    title = R.string.confirm_deletion,
                    message = R.string.wanna_delete_all,
                    onDismiss = viewmodel::onDeleteAllDialogDismiss,
                    onConfirm = viewmodel::onDeleteAll
                )
            }
        }
    }
}