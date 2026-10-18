package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StudyHubCard
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.AppStrings

@Composable
fun PrivacyPolicyScreen(
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isDarkTheme) DarkMidnightBg else Color(0xFFF8FAFC)
    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else MaterialTheme.colorScheme.outlineVariant
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

    Box(modifier = modifier.fillMaxSize().background(bg).testTag("privacy_policy_screen")) {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("privacy_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.privacyPolicy(lang),
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
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "গোপনীয়তা নীতি ও ডাটা সুরক্ষা" else "Privacy Policy & Data Protection",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary
                        )

                        Text(
                            text = if (lang == AppLanguage.BANGLA)
                                "StudyHub আপনার গোপনীয়তাকে অত্যন্ত গুরুত্ব দেয়। এই অ্যাপটি আপনার ভর্তি প্রস্তুতি, সিলেবাস অগ্রগতি, ফোকাস সেশন এবং স্টাডি প্ল্যান সম্পূর্ণ নিরাপদ ও গোপনীয়ভাবে আপনার ডিভাইসেই সংরক্ষণ করে।"
                            else
                                "StudyHub respects your privacy. This app stores your admission preparation progress, syllabus chapters, focus sessions, and study schedules locally on your device in a secure database.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textPrimary
                        )

                        Text(
                            text = if (lang == AppLanguage.BANGLA) "১. সংগৃহীত তথ্য:" else "1. Information Handled:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary
                        )
                        Text(
                            text = if (lang == AppLanguage.BANGLA)
                                "• বিষয়, অধ্যায় এবং সিলেবাসের অগ্রগতি।\n• ফোকাস টাইমার সেশন এবং অধ্যয়নের সময়।\n• মক টেস্টের ফলাফল এবং ভুল সংশোধনের তথ্য (Mistake Book)।\n• বিশ্ববিদ্যালয় ভর্তি পরীক্ষার ডেডলাইন ও ক্যালেন্ডার নোট।"
                            else
                                "• Subjects, chapters, and syllabus progress.\n• Focus timer sessions and study duration.\n• Mock test records and mistake book entries.\n• University admission deadlines and user preferences.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )

                        Text(
                            text = if (lang == AppLanguage.BANGLA) "২. ডাটা স্টোরেজ ও নিরাপত্তা:" else "2. Data Storage & Security:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary
                        )
                        Text(
                            text = if (lang == AppLanguage.BANGLA)
                                "আপনার সমস্ত ব্যক্তিগত স্টাডি ডাটা শুধুমাত্র আপনার ডিভাইসের স্থানীয় মেমোরিতে (Local Room Database) সংরক্ষিত থাকে। কোনো তথ্য বাহ্যিক সার্ভারে পাঠানো হয় না।"
                            else
                                "All your personal study data is stored exclusively on your device using local encrypted storage. No personal data is transmitted to external servers.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )

                        Text(
                            text = if (lang == AppLanguage.BANGLA) "৩. ব্যবহারকারী নিয়ন্ত্রণ:" else "3. User Controls:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary
                        )
                        Text(
                            text = if (lang == AppLanguage.BANGLA)
                                "সেটিংস থেকে আপনি যেকোনো সময় আপনার সমস্ত ডাটা ব্যাকআপ, এক্সপোর্ট বা রিসেট (Clear Data) করতে পারেন।"
                            else
                                "You maintain full control over your data. You can export JSON backups, restore previous backups, or reset all data anytime via settings.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )
                    }
                }
            }
        }
    }
}
