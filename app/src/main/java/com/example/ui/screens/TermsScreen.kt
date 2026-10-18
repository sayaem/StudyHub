package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
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
fun TermsScreen(
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

    Box(modifier = modifier.fillMaxSize().background(bg).testTag("terms_screen")) {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("terms_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.termsConditions(lang),
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
                            text = if (lang == AppLanguage.BANGLA) "ব্যবহারের শর্তাবলী (Terms & Conditions)" else "Terms & Conditions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary
                        )

                        Text(
                            text = if (lang == AppLanguage.BANGLA)
                                "StudyHub অ্যাপটি ব্যবহার করার মাধ্যমে আপনি নিচের শর্তাবলীতে সম্মত হচ্ছেন। দয়া করে অ্যাপ ব্যবহারের পূর্বে শর্তগুলো পড়ে নিন।"
                            else
                                "By downloading or using StudyHub, these terms will automatically apply to you. Please read them carefully before using the app.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textPrimary
                        )

                        Text(
                            text = if (lang == AppLanguage.BANGLA) "১. অ্যাপের ব্যবহার:" else "1. Scope of Use:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary
                        )
                        Text(
                            text = if (lang == AppLanguage.BANGLA)
                                "StudyHub একটি ব্যক্তিগত স্টাডি ও ভর্তি প্রস্তুতি ট্র্যাকিং টুল। এটি শিক্ষার্থীদের নিজস্ব পড়াশোনা, সিলেবাস এবং ফোকাস পরিচালনার জন্য ডিজাইন করা হয়েছে।"
                            else
                                "StudyHub is designed as a personal student productivity and admission preparation tracking assistant. You agree to use it for lawful study organization purposes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )

                        Text(
                            text = if (lang == AppLanguage.BANGLA) "২. বুদ্ধিবৃত্তিক সম্পত্তি (Intellectual Property):" else "2. Intellectual Property:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary
                        )
                        Text(
                            text = if (lang == AppLanguage.BANGLA)
                                "অ্যাপটির ডিজাইন, লোগো, সোর্স কোড এবং ইন্টারফেসের স্বত্ব সংরক্ষিত। বাণিজ্যিক উদ্দেশ্যে এর অননুমোদিত ব্যবহার নিষিদ্ধ।"
                            else
                                "All intellectual property rights, trademarks, and source code associated with StudyHub remain the property of the developers.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )

                        Text(
                            text = if (lang == AppLanguage.BANGLA) "৩. দায়বদ্ধতা সীমাবদ্ধতা (Limitation of Liability):" else "3. Limitation of Liability:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary
                        )
                        Text(
                            text = if (lang == AppLanguage.BANGLA)
                                "StudyHub আপনার পড়াশোনায় সহায়তা করার জন্য নির্মিত। পরীক্ষার ফলাফল বা ভর্তির সফলতার কোনো আনুষ্ঠানিক নিশ্চয়তা অ্যাপ প্রদান করে না।"
                            else
                                "StudyHub provides study planning tools to assist your preparation. We do not guarantee specific academic exam results or university admission outcomes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )
                    }
                }
            }
        }
    }
}
