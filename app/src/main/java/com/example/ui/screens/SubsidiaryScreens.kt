package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.util.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestScreen(viewModel: MainViewModel, isDarkTheme: Boolean, lang: AppLanguage, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Mock Tests") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(p).padding(16.dp)) { Text("Mock Tests") }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MistakeBookScreen(viewModel: MainViewModel, isDarkTheme: Boolean, lang: AppLanguage, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Mistake Book") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(p).padding(16.dp)) { Text("Mistake Book") }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(viewModel: MainViewModel, isDarkTheme: Boolean, lang: AppLanguage, onBack: () -> Unit, onNavigate: (com.example.ui.ScreenDestination) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("About StudyHub") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(p).padding(16.dp)) { Text("StudyHub v1.0") }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(viewModel: MainViewModel, isDarkTheme: Boolean, lang: AppLanguage, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Data Management") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(p).padding(16.dp)) { Text("Data Management") }
    }
}
