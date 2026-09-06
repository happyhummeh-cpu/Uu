package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MindPrimaryLight
import com.example.ui.theme.MindUrgentRed
import com.example.ui.theme.MindVerifiedGreen

@Composable
fun JudgeModeDialog(
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("judge_mode_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MindPrimaryLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SIH 2026 Jury Briefing", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Core Architecture Decisions (Read in <2 minutes)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MindPrimaryLight
                )

                JudgeQACard(
                    q = "1. Why AI?",
                    a = "To eliminate barriers for survivors in distress. Voice-first in English, Hindi, and Punjabi, plus multilingual code-mixed speech understanding, removes digital literacy friction."
                )

                JudgeQACard(
                    q = "2. Why NOT an AI Therapist?",
                    a = "MindMatrix is strictly a navigator, not a clinician. Hallucinating therapy in crisis can be fatal. MindMatrix connects survivors directly to real, certified human professionals (Tele-MANAS, KIRAN, NCW)."
                )

                JudgeQACard(
                    q = "3. Why Interpretable Need Classification?",
                    a = "Black-box LLMs can misroute emergency queries or hallucinate clinical diagnoses. Our classifier is rule-and-term audited, produces explicit confidence scores, and asks clarification when uncertain."
                )

                JudgeQACard(
                    q = "4. Why Verified Resources?",
                    a = "Fake helplines cause immense harm. Every single service is backed by official Gazette/Govt notifications (MoHFW, MHA, MWCD, DEPwD) with explicit audit timestamps and source authority."
                )

                JudgeQACard(
                    q = "5. Why Human Escalation & Handoff?",
                    a = "AI should NEVER make consequential support decisions. If human operator is requested or unavailable, the system transparently connects or hands off via an anonymous, zero-PII session packet."
                )

                JudgeQACard(
                    q = "6. What Happens When AI / Network Fails?",
                    a = "Full graceful degradation: text-input fallback, local Room database cache of pan-India emergency hotlines (112, 14416, 1098, 181), and zero loss of life-saving contact pathways."
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Understood")
            }
        }
    )
}

@Composable
private fun JudgeQACard(q: String, a: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = q,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = a,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
