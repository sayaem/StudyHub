package com.example.util

enum class AppLanguage {
    ENGLISH, BANGLA
}

object AppStrings {
    fun greetingMorning(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শুভ সকাল" else "Good morning"
    fun greetingAfternoon(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শুভ দুপুর" else "Good afternoon"
    fun greetingEvening(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শুভ সন্ধ্যা" else "Good evening"
    fun greetingNight(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শুভ রাত্রি" else "Good night"

    fun todayStudyTime(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আজকের পড়াশোনার সময়" else "Today's Study Time"
    
    fun formatNumber(num: Long, lang: AppLanguage): String {
        val s = num.toString()
        if (lang != AppLanguage.BANGLA) return s
        val bengaliDigits = listOf('০','১','২','৩','৪','৫','৬','৭','৮','৯')
        return s.map { if (it.isDigit()) bengaliDigits[it - '0'] else it }.joinToString("")
    }
}
