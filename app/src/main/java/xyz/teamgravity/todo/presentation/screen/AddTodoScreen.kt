package xyz.teamgravity.todo.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.collectLatest
import xyz.teamgravity.todo.R
import xyz.teamgravity.todo.presentation.component.TodoFloatingActionButton
import xyz.teamgravity.todo.presentation.component.TodoImportantCheckbox
import xyz.teamgravity.todo.presentation.component.TodoTextField
import xyz.teamgravity.todo.presentation.component.TopAppBarTitle
import xyz.teamgravity.todo.presentation.theme.SuperLightWhite
import xyz.teamgravity.todo.presentation.viewmodel.AddTodoViewModel

@Destination
@Composable
fun AddTodoScreen(
    viewmodel: AddTodoViewModel = hiltViewModel(),
    scaffold: ScaffoldState = rememberScaffoldState(),
    navigator: DestinationsNavigator
) {

    val context = LocalContext.current

    LaunchedEffect(key1 = viewmodel.event) {
        viewmodel.event.collectLatest { event ->
            when (event) {
                is AddTodoViewModel.AddTodoEvent.InvalidInput -> {
                    scaffold.snackbarHostState.showSnackbar(message = context.getString(event.message))
                }

                AddTodoViewModel.AddTodoEvent.TodoAdded -> {
                    navigator.popBackStack()
                }
            }
        }
    }

    Scaffold(
        scaffoldState = scaffold,
        topBar = {
            TopAppBar(
                title = {
                    TopAppBarTitle(title = stringResource(id = R.string.new_task))
                },
                navigationIcon = {
                    IconButton(onClick = navigator::popBackStack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.cd_back_button)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            TodoFloatingActionButton(
                onClick = viewmodel::onSaveTodo,
                icon = Icons.Default.Done,
                contentDescription = stringResource(id = R.string.cd_done_button)
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(SuperLightWhite)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TodoTextField(
                value = viewmodel.name,
                onValueChange = viewmodel::onNameChange
            )
            Spacer(modifier = Modifier.height(8.dp))
            TodoImportantCheckbox(
                important = viewmodel.important,
                onImportantChange = viewmodel::onImportantChange
            )
        }
    }
}