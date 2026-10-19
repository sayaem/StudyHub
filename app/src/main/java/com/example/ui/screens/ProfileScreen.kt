package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.util.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onNavigate: (ScreenDestination) -> Unit
) {
    val dark by viewModel.isDarkTheme.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (lang == AppLanguage.BANGLA) "প্রোফাইল" else "Profile") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Dark Theme", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = dark, onCheckedChange = { viewModel.setDarkTheme(it) })
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(ScreenDestination.PRIVACY) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    ListItem(
                        headlineContent = { Text(if (lang == AppLanguage.BANGLA) "গোপনীয়তা নীতি" else "Privacy Policy") },
                        trailingContent = { Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(ScreenDestination.TERMS) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    ListItem(
                        headlineContent = { Text(if (lang == AppLanguage.BANGLA) "শর্তাবলী" else "Terms & Conditions") },
                        trailingContent = { Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(ScreenDestination.ABOUT) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    ListItem(
                        headlineContent = { Text("About StudyHub") },
                        trailingContent = { Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }
            }
        }
    }
}
