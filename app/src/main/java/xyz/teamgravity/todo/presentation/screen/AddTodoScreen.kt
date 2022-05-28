package xyz.teamgravity.todo.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import xyz.teamgravity.todo.R
import xyz.teamgravity.todo.presentation.component.button.TodoFloatingActionButton
import xyz.teamgravity.todo.presentation.component.misc.TodoConfigure
import xyz.teamgravity.todo.presentation.component.topbar.TopBarIconButton
import xyz.teamgravity.todo.presentation.component.topbar.TopBarTitle
import xyz.teamgravity.todo.presentation.theme.backgroundLayout
import xyz.teamgravity.todo.presentation.viewmodel.AddTodoSideEffect
import xyz.teamgravity.todo.presentation.viewmodel.AddTodoViewModel

@Destination
@Composable
fun AddTodoScreen(
    viewmodel: AddTodoViewModel = hiltViewModel(),
    scaffold: ScaffoldState = rememberScaffoldState(),
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val state = viewmodel.collectAsState().value

    suspend fun handleSideEffect(sideEffect: AddTodoSideEffect) {
        when (sideEffect) {
            is AddTodoSideEffect.InvalidInput -> {
                scaffold.snackbarHostState.showSnackbar(message = context.getString(sideEffect.message))
            }

            AddTodoSideEffect.TodoAdded -> {
                navigator.popBackStack()
            }
        }
    }


    viewmodel.collectSideEffect(sideEffect = ::handleSideEffect)

    Scaffold(
        scaffoldState = scaffold,
        topBar = {
            TopAppBar(
                title = { TopBarTitle(title = R.string.new_task) },
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
                onClick = viewmodel::onSaveTodo,
                icon = Icons.Default.Done,
                contentDescription = R.string.cd_done_button
            )
        }
    ) {
        Box(
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
        }
    }

}

