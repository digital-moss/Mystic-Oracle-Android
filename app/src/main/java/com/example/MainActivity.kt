package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.OracleDatabase
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = OracleDatabase.getDatabase(applicationContext)
        val readingDao = database.readingDao()

        setContent {
            MyApplicationTheme {
                var currentTab by remember { mutableStateOf(0) }
                val readings by readingDao.getAllReadings().collectAsStateWithLifecycle(initialValue = emptyList())
                val coroutineScope = rememberCoroutineScope()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = { Icon(Icons.Default.Psychology, contentDescription = "Runes") },
                                label = { Text("Runes") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = { Icon(Icons.Default.Book, contentDescription = "I Ching") },
                                label = { Text("I Ching") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 },
                                icon = { Icon(Icons.Default.Style, contentDescription = "Tarot") },
                                label = { Text("Tarot") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 4,
                                onClick = { currentTab = 4 },
                                icon = { Icon(Icons.Default.History, contentDescription = "Journal") },
                                label = { Text("Journal") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 5,
                                onClick = { currentTab = 5 },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            0 -> HomeScreen(
                                onNavigateToRunes = { currentTab = 1 },
                                onNavigateToIChing = { currentTab = 2 },
                                onNavigateToHistory = { currentTab = 4 }
                            )
                            1 -> RuneDeckScreen(
                                onSaveReading = { reading ->
                                    coroutineScope.launch {
                                        readingDao.insertReading(reading)
                                    }
                                }
                            )
                            2 -> IChingScreen(
                                onSaveReading = { reading ->
                                    coroutineScope.launch {
                                        readingDao.insertReading(reading)
                                    }
                                }
                            )
                            3 -> TarotScreen(
                                onSaveReading = { reading ->
                                    coroutineScope.launch {
                                        readingDao.insertReading(reading)
                                    }
                                }
                            )
                            4 -> HistoryScreen(
                                readings = readings,
                                onDeleteReading = { id ->
                                    coroutineScope.launch {
                                        readingDao.deleteReading(id)
                                    }
                                },
                                onClearAll = {
                                    coroutineScope.launch {
                                        readingDao.clearAll()
                                    }
                                }
                            )
                            5 -> SettingsScreen()
                        }
                    }
                }
            }
        }
    }
}
