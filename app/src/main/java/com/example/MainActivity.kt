package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.DeckManager
import com.example.data.OracleDatabase
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize persistent settings, fonts, and account state
        DeckManager.initPreferences(applicationContext)

        val database = OracleDatabase.getDatabase(applicationContext)
        val readingDao = database.readingDao()

        setContent {
            var currentTheme by remember {
                val prefs = applicationContext.getSharedPreferences("mystic_oracle_prefs", MODE_PRIVATE)
                mutableStateOf(prefs.getString("app_theme", "Mystic Purple") ?: "Mystic Purple")
            }

            MyApplicationTheme(
                themeName = currentTheme,
                fontName = DeckManager.selectedFontName,
                fontSizeScale = DeckManager.fontSizeScale
            ) {
                var currentTab by remember { mutableIntStateOf(0) }
                val readings by readingDao.getAllReadings().collectAsStateWithLifecycle(initialValue = emptyList())
                val coroutineScope = rememberCoroutineScope()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text("Mystic Oracle Sanctuary")
                                }
                            },
                            actions = {
                                if (DeckManager.isLoggedIn) {
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                            .clickable { currentTab = 6 },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (DeckManager.userPhotoUrl != null) {
                                            AsyncImage(
                                                model = DeckManager.userPhotoUrl,
                                                contentDescription = "User Avatar",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Profile",
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                } else {
                                    IconButton(onClick = { currentTab = 6 }) {
                                        Icon(Icons.Default.Settings, contentDescription = "Settings & Profile")
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    },
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
                                icon = { Icon(Icons.Default.Help, contentDescription = "Yes/No Oracle") },
                                label = { Text("Yes/No") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 5,
                                onClick = { currentTab = 5 },
                                icon = { Icon(Icons.Default.History, contentDescription = "Journal") },
                                label = { Text("Journal") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            0 -> HomeScreen(
                                onNavigateToRunes = { currentTab = 1 },
                                onNavigateToIChing = { currentTab = 2 },
                                onNavigateToTarot = { currentTab = 3 },
                                onNavigateToHistory = { currentTab = 5 }
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
                            4 -> YesNoOracleScreen(
                                onSaveReading = { reading ->
                                    coroutineScope.launch {
                                        readingDao.insertReading(reading)
                                    }
                                }
                            )
                            5 -> HistoryScreen(
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
                            6 -> SettingsScreen(
                                currentTheme = currentTheme,
                                onThemeChanged = { newTheme ->
                                    currentTheme = newTheme
                                    applicationContext.getSharedPreferences("mystic_oracle_prefs", MODE_PRIVATE)
                                        .edit().putString("app_theme", newTheme).apply()
                                },
                                readings = readings,
                                onImportReadings = { imported ->
                                    coroutineScope.launch {
                                        imported.forEach { readingDao.insertReading(it) }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
