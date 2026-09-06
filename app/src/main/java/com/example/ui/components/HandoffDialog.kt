package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.handoff.HandoffPacket
import com.example.domain.handoff.HandoffState
import com.example.ui.theme.MindPrimaryLight
import com.example.ui.theme.MindUrgentRed
import com.example.ui.theme.MindVerifiedGreen

@Composable
fun HandoffDialog(
    handoffState: HandoffState,
    onDismiss: () -> Unit,
    onConfirmHandoff: (HandoffPacket) -> Unit
) {
    val context = LocalContext.current

    if (handoffState is HandoffState.Idle) return

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("handoff_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = MindPrimaryLight,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Human Support Handoff", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (handoffState) {
                    is HandoffState.Drafting -> {
                        val packet = handoffState.packet
                        Text(
                            text = "Review Handoff Packet (Minimal PII)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MindPrimaryLight
                        )
                        Text(
                            text = "To protect your privacy, only non-identifying navigation context is shared with verified human support responders.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MindVerifiedGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Anonymous ID: ${packet.anonymousSessionId}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Text("Requested Service: ${packet.serviceCategory.displayName}", fontSize = 12.sp)
                                Text("Preferred Language: ${packet.language}", fontSize = 12.sp)
                                Text("Location Scope: ${packet.optionalLocation}", fontSize = 12.sp)
                                Text("Sanitized Summary: \"${packet.userSummary}\"", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Text(
                            text = "The operator makes the final support decisions, never the AI.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is HandoffState.Connecting -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MindPrimaryLight,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Connecting with verified helpline queue...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Session ID: ${handoffState.packet.anonymousSessionId}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is HandoffState.OperatorUnavailable -> {
                        // Section 11: "If a human operator is unavailable: Show verified alternative support resources. Never pretend that a human is available when one is not."
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MindUrgentRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Direct app-chat operator is currently busy or offline. MindMatrix never pretends an operator is present.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MindUrgentRed
                                    )
                                }
                            }

                            Text(
                                text = "Immediate Verified Telephone Fallback:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${handoffState.fallbackPhone}"))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MindPrimaryLight),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Call ${handoffState.fallbackName} (${handoffState.fallbackPhone})")
                            }

                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Call 112 National Emergency Hotline")
                            }
                        }
                    }

                    is HandoffState.Completed -> {
                        Text(text = handoffState.message, fontSize = 13.sp)
                    }
                    HandoffState.Idle -> {}
                }


            }
        },
        confirmButton = {
            when (handoffState) {
                is HandoffState.Drafting -> {
                    Button(
                        onClick = { onConfirmHandoff(handoffState.packet) },
                        colors = ButtonDefaults.buttonColors(containerColor = MindPrimaryLight)
                    ) {
                        Text("Approve & Connect")
                    }
                }
                is HandoffState.Connecting -> {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
                else -> {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        },
        dismissButton = {
            if (handoffState is HandoffState.Drafting) {
                TextButton(onClick = onDismiss) {
                    Text("Decline")
                }
            }
        }
    )
}
