package com.example.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.PreferencesManager
import kotlinx.coroutines.delay

class BlockedAppActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: ""
        val prefs = PreferencesManager(this)

        val appLabel = try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(blockedPackage, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            blockedPackage.substringAfterLast('.').replaceFirstChar { it.uppercase() }
        }

        setContent {
            val isDarkTheme = prefs.isDarkTheme
            val lang = if (prefs.language == "bn") AppLanguage.BANGLA else AppLanguage.ENGLISH

            MyApplicationTheme(darkTheme = isDarkTheme) {
                BlockedAppScreen(
                    blockedAppLabel = appLabel,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onReturnToStudy = {
                        val returnIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra("navigate_to", "FOCUS")
                        }
                        startActivity(returnIntent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun BlockedAppScreen(
    blockedAppLabel: String,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onReturnToStudy: () -> Unit
) {
    BackHandler {
        onReturnToStudy()
    }

    val bg = if (isDarkTheme) DarkMidnightBg else Color(0xFFF8FAFC)
    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else SlateBorder
    val textPrimary = if (isDarkTheme) DarkTextWhite else SlateDark
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("blocked_app_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Calm Shield Icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(RoyalBlue.copy(alpha = if (isDarkTheme) 0.18f else 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = RoyalBlue,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Heading
            Text(
                text = if (lang == AppLanguage.BANGLA) "ফোকাস সেশন চলছে!" else "Focus Session in Progress",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // App name explanation
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = cardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, borderCol),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (lang == AppLanguage.BANGLA)
                            "\"$blockedAppLabel\" ফোকাস মোডে সাময়িক স্থগিত রাখা হয়েছে。"
                        else
                            "\"$blockedAppLabel\" is paused during this focus session to help you concentrate on your admission goals.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lang == AppLanguage.BANGLA)
                                "মনোযোগ বজায় রাখুন, স্বপ্ন পূরণে আর অল্প বাকি!"
                            else
                                "Stay in the flow, you are building your future!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RoyalBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Primary Return Action
            Button(
                onClick = onReturnToStudy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("button_return_to_study")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (lang == AppLanguage.BANGLA) "পড়াশোনায় ফিরে যান" else "Return to Study Session",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary option to pause/stop in StudyHub
            TextButton(
                onClick = onReturnToStudy,
                modifier = Modifier.testTag("button_emergency_pause")
            ) {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "বিরতি নিতে স্টাডিহাবে যান" else "Open StudyHub to Pause or Finish",
                    fontSize = 13.sp,
                    color = textSecondary
                )
            }
        }
    }
}
