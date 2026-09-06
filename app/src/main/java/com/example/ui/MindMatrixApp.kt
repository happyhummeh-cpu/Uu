package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.ServiceCategory
import com.example.domain.voice.VoiceState
import com.example.ui.components.AddResourceDialog
import com.example.ui.components.AdminDialog
import com.example.ui.components.HandoffDialog
import com.example.ui.components.JudgeModeDialog
import com.example.ui.components.PrivacyDialog
import com.example.ui.components.ResourceCard
import com.example.ui.components.SafetyEscalationBanner
import com.example.ui.components.SampleVoicePromptsDialog
import com.example.ui.theme.MindBackgroundLight
import com.example.ui.theme.MindFooterBg
import com.example.ui.theme.MindOnPrimaryContainerLight
import com.example.ui.theme.MindOutline
import com.example.ui.theme.MindOutlineVariant
import com.example.ui.theme.MindPrimaryContainerLight
import com.example.ui.theme.MindPrimaryLight
import com.example.ui.theme.MindSurfaceVariantLight
import com.example.ui.theme.MindUrgentRed
import com.example.ui.theme.MindVerifiedGreen
import com.example.ui.viewmodel.MindMatrixViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MindMatrixApp(viewModel: MindMatrixViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val rankedResources by viewModel.rankedResources.collectAsState()
    val adminResources by viewModel.adminResources.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val voiceState by viewModel.voiceInputManager.voiceState.collectAsState()

    var selectedBottomNav by remember { mutableIntStateOf(0) }
    var languageMenuExpanded by remember { mutableStateOf(false) }

    // Audio Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceListening()
        } else {
            Toast.makeText(context, "Microphone permission required for voice input", Toast.LENGTH_SHORT).show()
        }
    }

    // Camouflaged Quick Exit Screen
    if (uiState.isQuickExited) {
        CamouflageSafeScreen(onResume = { viewModel.resumeFromQuickExit() })
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mindmatrix_scaffold"),
        containerColor = MindBackgroundLight,
        topBar = {
            // Clean Utility Header from theme
            Surface(
                color = MindBackgroundLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Brand: Purple Hub Icon + Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MindPrimaryLight,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = "MindMatrix Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = "MindMatrix",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Right Actions: Language + Judge + Quick Exit
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Language Selector
                        Box {
                            IconButton(
                                onClick = { languageMenuExpanded = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Language",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            DropdownMenu(
                                expanded = languageMenuExpanded,
                                onDismissRequest = { languageMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("English") },
                                    onClick = {
                                        viewModel.onLanguageSelected("English")
                                        languageMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("हिन्दी (Hindi)") },
                                    onClick = {
                                        viewModel.onLanguageSelected("Hindi")
                                        languageMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("ਪੰਜਾਬੀ (Punjabi)") },
                                    onClick = {
                                        viewModel.onLanguageSelected("Punjabi")
                                        languageMenuExpanded = false
                                    }
                                )
                            }
                        }

                        // SIH Judge Briefing
                        IconButton(
                            onClick = { viewModel.toggleJudgeDialog(true) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Judge Briefing",
                                tint = MindPrimaryLight
                            )
                        }

                        // Admin Console
                        IconButton(
                            onClick = { viewModel.toggleAdminDialog(true) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Registry",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Emergency Quick Exit Button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MindUrgentRed,
                            modifier = Modifier
                                .clickable { viewModel.triggerQuickExit() }
                                .testTag("quick_exit_header_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Quick Exit",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Exit",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Clean Utility Bottom Bar from design
            Surface(
                color = MindFooterBg,
                border = BorderStroke(1.dp, MindOutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavTab(
                        icon = Icons.Default.Home,
                        label = "Home",
                        selected = selectedBottomNav == 0,
                        onClick = { selectedBottomNav = 0 }
                    )
                    BottomNavTab(
                        icon = Icons.Default.Explore,
                        label = "Resources",
                        selected = selectedBottomNav == 1,
                        onClick = { selectedBottomNav = 1 }
                    )
                    BottomNavTab(
                        icon = Icons.Default.Shield,
                        label = "Privacy",
                        selected = selectedBottomNav == 2,
                        onClick = {
                            selectedBottomNav = 2
                            viewModel.togglePrivacyDialog(true)
                        }
                    )
                    BottomNavTab(
                        icon = Icons.Default.Psychology,
                        label = "SIH Jury",
                        selected = selectedBottomNav == 3,
                        onClick = {
                            selectedBottomNav = 3
                            viewModel.toggleJudgeDialog(true)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Title and Subtitle
                Text(
                    text = when (uiState.selectedLanguage) {
                        "Hindi" -> "हम आज आपकी कैसे सहायता कर सकते हैं?"
                        "Punjabi" -> "ਅਸੀਂ ਅੱਜ ਤੁਹਾਡੀ ਕਿਵੇਂ ਮਦਦ ਕਰ ਸਕਦੇ ਹਾਂ?"
                        else -> "How can we help you today?"
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 26.sp,
                        lineHeight = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when (uiState.selectedLanguage) {
                        "Hindi" -> "अंग्रेजी, हिंदी या पंजाबी में अपनी आवश्यकता बोलें या लिखें।"
                        "Punjabi" -> "ਅੰਗਰੇਜ਼ੀ, ਹਿੰਦੀ ਜਾਂ ਪੰਜਾਬੀ ਵਿੱਚ ਆਪਣੀ ਲੋੜ ਬੋਲੋ ਜਾਂ ਲਿਖੋ।"
                        else -> "Speak or type your needs in English, Hindi, or Punjabi."
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Utility Badges: Verified Resources + Privacy Shield Active
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MindPrimaryContainerLight,
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(
                            text = "Verified Resources (100% Real)",
                            color = MindOnPrimaryContainerLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = MindSurfaceVariantLight,
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(
                            text = "Privacy Shield Active",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Safety Escalation Banner (Rule-triggered)
            if (!uiState.isEmergencyDismissed && uiState.safetyAssessment?.isUrgent == true) {
                item {
                    SafetyEscalationBanner(
                        assessment = uiState.safetyAssessment,
                        onDismiss = { viewModel.dismissEmergencyBanner() }
                    )
                }
            }

            // Large Minimal Central Voice Section from Design Theme
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Pulsing Voice Button with concentric layers
                        ConcentricVoiceButton(
                            isListening = voiceState is VoiceState.Listening,
                            onClick = {
                                if (voiceState is VoiceState.Listening) {
                                    viewModel.stopVoiceListening()
                                } else {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        viewModel.startVoiceListening()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Voice Label
                        Text(
                            text = when (voiceState) {
                                is VoiceState.Listening -> "Listening... (Speaking now)"
                                is VoiceState.Processing -> "Processing speech..."
                                is VoiceState.Error -> (voiceState as VoiceState.Error).message
                                else -> "Tap to Speak"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = MindPrimaryLight
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Sample Voice Dialog Link for Judge Testing
                        OutlinedButton(
                            onClick = { viewModel.toggleSampleVoiceDialog(true) },
                            shape = RoundedCornerShape(100.dp),
                            modifier = Modifier.testTag("sample_voice_prompts_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Try Sample Voice Scenarios (EN / HI / PA)",
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Text Input Box Fallback from Theme Design
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Input box with send button embedded on the right
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(MindSurfaceVariantLight, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))

                            TextField(
                                value = uiState.inputText,
                                onValueChange = { viewModel.onQueryChanged(it) },
                                placeholder = {
                                    Text(
                                        text = "Describe what kind of help you need...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("query_text_input"),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    viewModel.processQuery(uiState.inputText)
                                })
                            )

                            if (uiState.inputText.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        viewModel.onQueryChanged("")
                                        viewModel.processQuery("")
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Send Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MindPrimaryLight,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable { viewModel.processQuery(uiState.inputText) }
                                    .testTag("submit_query_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Search",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Two Primary Action Buttons from Theme: Connect Agent & Emergency Exit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.startHumanHandoff() },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MindOutline),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("connect_agent_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Connect Agent",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = { viewModel.triggerQuickExit() },
                            colors = ButtonDefaults.buttonColors(containerColor = MindUrgentRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("emergency_exit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Emergency Exit",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Clarification Interface (Low-confidence safety rule from Section 6)
            if (uiState.isClarificationVisible) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MindPrimaryContainerLight.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = MindPrimaryLight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "What kind of help are you looking for?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MindOnPrimaryContainerLight
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "To give you accurate support, please select a specific area below:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ServiceCategory.values().filter { it != ServiceCategory.GENERAL_SUPPORT }.forEach { cat ->
                                    Button(
                                        onClick = { viewModel.selectClarifiedCategory(cat) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MindPrimaryLight),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text(cat.displayName, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Verified Support Helplines",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )

                        // Real Measured Latency Benchmark Display
                        Surface(
                            color = MindSurfaceVariantLight,
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text(
                                text = "${rankedResources.size} resources • ${uiState.searchLatencyMs}ms",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.activeCategoryFilter == null,
                                onClick = { viewModel.onCategoryFilterClicked(null) },
                                label = { Text("All Verified (${rankedResources.size})", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MindPrimaryContainerLight,
                                    selectedLabelColor = MindOnPrimaryContainerLight
                                )
                            )
                        }

                        items(ServiceCategory.values().filter { it != ServiceCategory.GENERAL_SUPPORT }) { cat ->
                            FilterChip(
                                selected = uiState.activeCategoryFilter == cat,
                                onClick = { viewModel.onCategoryFilterClicked(cat) },
                                label = { Text(cat.displayName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MindPrimaryContainerLight,
                                    selectedLabelColor = MindOnPrimaryContainerLight
                                )
                            )
                        }
                    }
                }
            }

            // Ranked Resource Cards
            if (rankedResources.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MindSurfaceVariantLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No matching services found for current filter.",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try clearing filters or call national emergency dispatch at 112.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(rankedResources, key = { it.resource.id }) { ranked ->
                    ResourceCard(rankedResource = ranked)
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Dialogs
    if (uiState.showAdminDialog) {
        AdminDialog(
            resources = adminResources,
            auditLogs = auditLogs,
            onDismiss = { viewModel.toggleAdminDialog(false) },
            onVerifyResource = { viewModel.markResourceVerified(it) },
            onRequestReverify = { viewModel.markResourceNeedsVerification(it) },
            onToggleActive = { id, active -> viewModel.toggleResourceActive(id, active) },
            onDeleteResource = { viewModel.deleteResource(it) },
            onAddNewResourceClick = {
                viewModel.toggleAdminDialog(false)
                viewModel.toggleAddResourceDialog(true)
            }
        )
    }

    if (uiState.showAddResourceDialog) {
        AddResourceDialog(
            initialResource = uiState.editingResource,
            onDismiss = { viewModel.toggleAddResourceDialog(false) },
            onSave = { viewModel.saveResource(it) }
        )
    }


    if (uiState.showJudgeDialog) {
        JudgeModeDialog(onDismiss = { viewModel.toggleJudgeDialog(false) })
    }

    if (uiState.showPrivacyDialog) {
        PrivacyDialog(
            onDismiss = { viewModel.togglePrivacyDialog(false) },
            onWipeData = { viewModel.wipeAllData() }
        )
    }

    if (uiState.showSampleVoiceDialog) {
        SampleVoicePromptsDialog(
            onDismiss = { viewModel.toggleSampleVoiceDialog(false) },
            onSelectPrompt = { viewModel.onSampleVoicePicked(it) }
        )
    }

    HandoffDialog(
        handoffState = uiState.handoffState,
        onDismiss = { viewModel.dismissHandoff() },
        onConfirmHandoff = { viewModel.confirmAndSendHandoff(it) }
    )
}

@Composable
fun ConcentricVoiceButton(
    isListening: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer Translucent Circle
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(if (isListening) pulseScale else 1.0f)
                .background(MindPrimaryLight.copy(alpha = 0.08f), CircleShape)
        )

        // Middle Translucent Circle
        Box(
            modifier = Modifier
                .size(115.dp)
                .scale(if (isListening) pulseScale * 0.95f else 1.0f)
                .background(MindPrimaryLight.copy(alpha = 0.18f), CircleShape)
        )

        // Center Mic Action Button
        Surface(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .clickable { onClick() }
                .testTag("voice_mic_button"),
            color = if (isListening) MindUrgentRed else MindPrimaryLight,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Composable
fun BottomNavTab(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) MindOnPrimaryContainerLight else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.5.sp,
            color = if (selected) MindOnPrimaryContainerLight else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CamouflageSafeScreen(
    onResume: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF9F9FB)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Notes & Weather",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "New Delhi: 28°C • Partly Cloudy",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Weekly Checklist", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• Grocery shopping (rice, lentils)\n• Power bill due next Monday\n• Water plants in terrace", fontSize = 13.sp, color = Color.DarkGray)
                    }
                }
            }

            Button(
                onClick = onResume,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("resume_from_exit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A6572)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Resume MindMatrix Session")
            }
        }
    }
}
