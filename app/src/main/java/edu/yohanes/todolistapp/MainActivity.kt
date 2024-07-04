package edu.yohanes.todolistapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.yohanes.todolistapp.ui.theme.TodoListAppTheme
import kotlinx.coroutines.launch
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.saveable.rememberSaveable

// Data class to store todo item information
data class TodoItem(val text: String, var isChecked: Boolean)

// Placeholder data for todo items
val placeholderTodos = listOf(
    TodoItem("ICS 140", true),
    TodoItem("ICS 141", true),
    TodoItem("ICS 232", true),
    TodoItem("ICS 240", true),
    TodoItem("ICS 311", true),
    TodoItem("ICS 340", true),
    TodoItem("ICS 365", true),
    TodoItem("ICS 372", true),
    TodoItem("ICS 342", true),
    TodoItem("ICS 440", true),
    TodoItem("ICS 462", true),
    TodoItem("ICS 460", false),
    TodoItem("ICS ICS 499", false),

)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoListApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListApp() {
    TodoListAppTheme {
        val sheetState = rememberModalBottomSheetState()
        var isSheetOpen by rememberSaveable { mutableStateOf(false) }

        // Initialize todoItems with placeholder data
        val todoItems = remember { mutableStateListOf(*placeholderTodos.toTypedArray()) }

        var text by remember { mutableStateOf("") }
        var showError by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.padding(bottom = 12.dp),
                    title = {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = stringResource(id = R.string.todo_list_title)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Gray
                    ),
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { isSheetOpen = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.add_icon),
                        contentDescription = stringResource(id = R.string.add)
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    // Displaying the list of todo items
                    for (todo in todoItems) {
                        TodoRow(todo, onCheckedChange = { isChecked ->
                            val index = todoItems.indexOf(todo)
                            todoItems[index] = todo.copy(isChecked = isChecked)
                        })
                    }
                }

                if (isSheetOpen) {
                    ModalBottomSheet(
                        sheetState = sheetState,
                        onDismissRequest = { isSheetOpen = false }
                    ) {
                        BottomSheetContent(
                            text = text,
                            onTextChanged = { newText ->
                                text = newText
                                showError = false
                            },
                            onSave = {
                                if (text.isEmpty()) {
                                    showError = true
                                } else {
                                    todoItems.add(TodoItem(text, false))
                                    text = ""
                                    coroutineScope.launch {
                                        isSheetOpen = false
                                    }
                                }
                            },
                            onCancel = {
                                coroutineScope.launch {
                                    isSheetOpen = false
                                }
                            }
                        )
                        if (showError) {
                            Text(
                                text = stringResource(id = R.string.enter_todo_error),
                                color = Color.Red,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodoRow(todo: TodoItem, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .background(Color.Cyan)
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            todo.text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)  // This pushes everything else to the right
                .padding(start = 8.dp)
        )

        Checkbox(
            checked = todo.isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

@Composable
fun BottomSheetContent(
    text: String,
    onTextChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChanged,
            label = { Text(stringResource(id = R.string.new_todo)) },
            trailingIcon = {
                IconButton(onClick = { onTextChanged("") }) {
                    Icon(Icons.Outlined.Clear, contentDescription = stringResource(id = R.string.clear_text))
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(8.dp, 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Text(stringResource(id = R.string.save))
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(8.dp, 16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(stringResource(id = R.string.cancel), color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TodoListApp()
}
