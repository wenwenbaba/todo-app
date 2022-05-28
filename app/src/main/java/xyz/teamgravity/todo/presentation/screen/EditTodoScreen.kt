package xyz.teamgravity.todo.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import xyz.teamgravity.todo.R
import xyz.teamgravity.todo.data.model.TodoModel
import xyz.teamgravity.todo.presentation.component.button.TodoFloatingActionButton
import xyz.teamgravity.todo.presentation.component.misc.TodoConfigure
import xyz.teamgravity.todo.presentation.component.topbar.TopBarIconButton
import xyz.teamgravity.todo.presentation.component.topbar.TopBarTitle
import xyz.teamgravity.todo.presentation.theme.backgroundLayout
import xyz.teamgravity.todo.presentation.theme.textSecondary
import xyz.teamgravity.todo.presentation.viewmodel.EditTodoSideEffect
import xyz.teamgravity.todo.presentation.viewmodel.EditTodoViewModel

@Destination(navArgsDelegate = EditScreenNavArgs::class)
@Composable
fun EditTodoScreen(
    viewmodel: EditTodoViewModel = hiltViewModel(),
    scaffold: ScaffoldState = rememberScaffoldState(),
    navigator: DestinationsNavigator
) {

    val context = LocalContext.current
    val state = viewmodel.collectAsState().value

    suspend fun handleSideEffect(sideEffect: EditTodoSideEffect) {
        when (sideEffect) {
            is EditTodoSideEffect.InvalidInput -> {
                scaffold.snackbarHostState.showSnackbar(message = context.getString(sideEffect.message))
            }

            EditTodoSideEffect.TodoUpdated -> {
                navigator.popBackStack()
            }
        }
    }

    viewmodel.collectSideEffect(sideEffect = ::handleSideEffect)

    Scaffold(
        scaffoldState = scaffold,
        topBar = {
            TopAppBar(
                title = { TopBarTitle(title = R.string.edit_task) },
                navigationIcon = {
                    TopBarIconButton(
                        onClick = navigator::popBackStack,
                        icon = Icons.Default.ArrowBack,
                        contentDescription = R.string.cd_back_button
                    )
                }
            )
        },
        floatingActionButton = {
            TodoFloatingActionButton(
                onClick = viewmodel::onUpdateTodo,
                icon = Icons.Default.Done,
                contentDescription = R.string.cd_done_button
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.backgroundLayout)
        ) {
            TodoConfigure(
                name = state.name,
                onNameChange = viewmodel::onNameChange,
                important = state.important,
                onImportantChange = viewmodel::onImportantChange
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(id = R.string.your_created_timestamp, state.timestamp),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.textSecondary,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

data class EditScreenNavArgs(
    val todo: TodoModel
)