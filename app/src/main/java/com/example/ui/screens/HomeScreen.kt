package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.*
import com.example.util.AppLanguage
import com.example.util.AppStrings

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    stats: StudyStats,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onNavigate: (ScreenDestination) -> Unit
) {
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = textPrimary.copy(alpha = 0.7f)
    val cardBg = MaterialTheme.colorScheme.surface
    val borderCol = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(RoyalBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "StudyHub",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = textPrimary
                    )
                }

                IconButton(onClick = { onNavigate(ScreenDestination.NOTIFICATIONS) }) {
                    Icon(imageVector = Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = textSecondary)
                }
            }
        }

        // Hero Greeting Block with integrated transparent illustration
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Image(
                    painter = painterResource(id = if (isDarkTheme) R.drawable.img_night_transparent else R.drawable.img_morning_transparent),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .align(Alignment.CenterEnd)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                        val (greeting, emoji) = when (hour) {
                            in 5..11 -> Pair(AppStrings.greetingMorning(lang) + ",", "🌅")
                            in 12..16 -> Pair(AppStrings.greetingAfternoon(lang) + ",", "☀️")
                            in 17..20 -> Pair(AppStrings.greetingEvening(lang) + ",", "🌆")
                            else -> Pair(AppStrings.greetingNight(lang) + ",", "🌙")
                        }

                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                            color = textPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Arafat",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = emoji, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "প্রতিদিনের নিয়মিত চর্চায় গড়ে তুলুন নিশ্চিত স্বপ্ন।" else "Small steps every day, big dreams tomorrow.",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(100.dp))
                }
            }
        }

        // Today's Study Time Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(ScreenDestination.FOCUS) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = AppStrings.todayStudyTime(lang), style = MaterialTheme.typography.bodyMedium, color = textSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        val hours = stats.todayStudySeconds / 3600
                        val mins = (stats.todayStudySeconds % 3600) / 60
                        Text(
                            text = "${AppStrings.formatNumber(hours, lang)}h ${AppStrings.formatNumber(mins, lang)}m",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Tap to start studying", style = MaterialTheme.typography.bodySmall, color = RoyalBlue)
                    }

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(RoyalBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = RoyalBlue)
                    }
                }
            }
        }

        // Quick Actions
        item {
            Text(text = "Quick Actions", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = textPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionItem(icon = Icons.Default.Add, label = "Add Subject") { onNavigate(ScreenDestination.SYLLABUS) }
                QuickActionItem(icon = Icons.Default.CalendarToday, label = "Today") { onNavigate(ScreenDestination.SMART_PLANNER) }
                QuickActionItem(icon = Icons.Default.PlayArrow, label = "Focus") { onNavigate(ScreenDestination.FOCUS) }
                QuickActionItem(icon = Icons.Default.TrendingUp, label = "Progress") { onNavigate(ScreenDestination.PROGRESS_ANALYTICS) }
            }
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun QuickActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(RoyalBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = RoyalBlue)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
    }
}
