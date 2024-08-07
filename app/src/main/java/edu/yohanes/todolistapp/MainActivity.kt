package edu.yohanes.todolistapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.yohanes.todolistapp.data.TodoItem
import edu.yohanes.todolistapp.ui.theme.TodoListAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = SharedPref(this)
        val apiService = Retrofit.todoApiService
        val loginViewModel = LoginViewModel(sharedPref, apiService, this)
        val createAccountViewModel = CreateAccountViewModel(sharedPref, apiService, this)
        val mainViewModel = TodoListViewModel(sharedPref, apiService, this)

        setContent {
            TodoListAppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "LoginScreen") {
                    composable("LoginScreen") {
                        LoginScreen(
                            onLoginSuccess = { navController.navigate("ToDoListScreen") },
                            onCreateAccountClick = { navController.navigate("CreateAccountScreen") },
                            viewModel = loginViewModel
                        )
                    }
                    composable("ToDoListScreen") {
                        mainViewModel.loadTodos()
                        ToDoListScreen(mainViewModel)
                    }
                    composable("CreateAccountScreen") {
                        CreateAccountScreen(
                            onCreateAccountSuccess = { navController.navigate("ToDoListScreen") },
                            onLoginClick = { navController.navigate("LoginScreen") },
                            viewModel = createAccountViewModel
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoListScreen(viewModel: TodoListViewModel = viewModel()) {
    val context = LocalContext.current
    val toDoList by viewModel.todos.observeAsState(emptyList())

    var text by remember { mutableStateOf("") }
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    val showError by viewModel.showError.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState("")

    if (showError) {
        AlertDialog(
            onDismissRequest = { viewModel.displayUpdatedErrorLog(false) },
            title = { Text(text = context.getString(R.string.error)) },
            text = { Text(text = errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.displayUpdatedErrorLog(false) }) {
                    Text(text = context.getString(R.string.okay))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(context.getString(R.string.todo))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Gray
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isSheetOpen = true }) {
                Icon(Icons.Default.Add, contentDescription = context.getString(R.string.add))
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(toDoList) { item ->
                ItemView(content = item, onToggle = { viewModel.updateCheckBox(item) })
            }
        }

        if (isSheetOpen) {
            ModalBottomSheet(onDismissRequest = { isSheetOpen = false }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text(context.getString(R.string.new_todo)) },
                        trailingIcon = {
                            IconButton(onClick = { text = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = context.getString(R.string.clear_text))
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                val todoItem = TodoItem("", text, false)
                                viewModel.addTodo(todoItem)
                                text = ""
                                isSheetOpen = false
                            } else {
                                viewModel.errorContent(context.getString(R.string.blank_error_message))
                                viewModel.displayUpdatedErrorLog(true)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(context.getString(R.string.save))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            isSheetOpen = false
                            text = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(context.getString(R.string.cancel))
                    }
                }
            }
        }
    }
}

@Composable
fun ItemView(content: TodoItem, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = content.description,
            modifier = Modifier.padding(start = 8.dp)
        )
        Checkbox(
            checked = content.completed,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onCreateAccountClick: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val email by viewModel.email.observeAsState("")
    val password by viewModel.password.observeAsState("")
    val showError by viewModel.showError.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState("")

    if (showError) {
        AlertDialog(
            onDismissRequest = { viewModel.updateShowError(false) },
            title = { Text(text = context.getString(R.string.error)) },
            text = { Text(text = errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.updateShowError(false) }) {
                    Text(text = context.getString(R.string.okay))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text(context.getString(R.string.email)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text(context.getString(R.string.password)) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    viewModel.login(onSuccess = onLoginSuccess, onError = { message ->
                        viewModel.errorPayload(message)
                        viewModel.updateShowError(true)
                    })
                } else {
                    viewModel.errorPayload(context.getString(R.string.empty_fields_error))
                    viewModel.updateShowError(true)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(context.getString(R.string.login_button))
        }
        TextButton(
            onClick = onCreateAccountClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(context.getString(R.string.create_account_button))
        }
    }
}

@Composable
fun CreateAccountScreen(
    onCreateAccountSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: CreateAccountViewModel = viewModel()
) {
    val context = LocalContext.current
    val name by viewModel.name.observeAsState("")
    val email by viewModel.email.observeAsState("")
    val password by viewModel.password.observeAsState("")
    val showError by viewModel.showError.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState("")
    val showSuccess by viewModel.showSuccess.observeAsState(false)

    if (showError) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleErrorDisplay(false) },
            title = { Text(text = context.getString(R.string.error)) },
            text = { Text(text = errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearErrorDisplay() }) {
                    Text(text = context.getString(R.string.okay))
                }
            }
        )
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSuccessDisplay() },
            title = { Text(text = context.getString(R.string.success)) },
            text = { Text(context.getString(R.string.account_creation_success)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearSuccessDisplay()
                    onCreateAccountSuccess()
                }) {
                    Text(text = context.getString(R.string.okay))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { viewModel.updateUsername(it) },
            label = { Text(context.getString(R.string.name)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text(context.getString(R.string.email)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text(context.getString(R.string.password)) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                    viewModel.createAccount(onSuccess = { viewModel.toggleSuccessDisplay(true) }, onError = { message ->
                        viewModel.updateErrorText(message)
                        viewModel.toggleErrorDisplay(true)
                    })
                } else {
                    viewModel.updateErrorText(context.getString(R.string.create_account_empty_fields_error))
                    viewModel.toggleErrorDisplay(true)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(context.getString(R.string.create_account_button))
        }
        TextButton(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(context.getString(R.string.login_button))
        }
    }
}
