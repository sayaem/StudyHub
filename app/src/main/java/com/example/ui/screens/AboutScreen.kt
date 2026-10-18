package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.StudyHubCard
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.AppStrings

@Composable
fun AboutScreen(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onBack: () -> Unit,
    onNavigate: (ScreenDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isDarkTheme) DarkMidnightBg else Color(0xFFF8FAFC)
    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else MaterialTheme.colorScheme.outlineVariant
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

    Box(modifier = modifier.fillMaxSize().background(bg).testTag("about_screen")) {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("about_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.sectionAbout(lang),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = textPrimary
                    )
                }
            }

            item {
                StudyHubCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = cardBg,
                    borderColor = borderCol
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = RoyalBlue.copy(alpha = 0.15f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "StudyHub",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${AppStrings.appVersionLabel(lang)}: 1.0.0 (Build 100)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = AppStrings.helpContent(lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textPrimary
                        )
                    }
                }
            }

            item {
                StudyHubCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = cardBg,
                    borderColor = borderCol
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "আইনগত ও নীতি" else "Legal & Policies",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary
                        )

                        // Privacy Policy Row
                        Surface(
                            onClick = { onNavigate(ScreenDestination.PRIVACY) },
                            color = Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalBlue)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = AppStrings.privacyPolicy(lang),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        HorizontalDivider(color = borderCol)

                        // Terms & Conditions Row
                        Surface(
                            onClick = { onNavigate(ScreenDestination.TERMS) },
                            color = Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = RoyalBlue)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = AppStrings.termsConditions(lang),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        HorizontalDivider(color = borderCol)

                        // Open Source Licenses Row
                        Surface(
                            onClick = { /* Could show info dialog */ },
                            color = Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = RoyalBlue)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = AppStrings.openSourceLicenses(lang),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
