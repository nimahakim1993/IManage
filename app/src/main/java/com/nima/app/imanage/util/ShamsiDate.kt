package com.nima.app.imanage.util

import java.util.Calendar

object ShamsiDate {

    val MONTH_NAMES = listOf(
        "فروردین",
        "اردیبهشت",
        "خرداد",
        "تیر",
        "مرداد",
        "شهریور",
        "مهر",
        "آبان",
        "آذر",
        "دی",
        "بهمن",
        "اسفند"
    )

    val DAY_NAMES = listOf(
        "شنبه",
        "یکشنبه",
        "دوشنبه",
        "سه\u200cشنبه",
        "چهارشنبه",
        "پنج\u200cشنبه",
        "جمعه"
    )

    private const val PERSIAN_DIGITS = "۰۱۲۳۴۵۶۷۸۹"

    fun toPersianDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (c in input) {
            sb.append(if (c in '0'..'9') PERSIAN_DIGITS[c - '0'] else c)
        }
        return sb.toString()
    }

    fun toLatinDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (c in input) {
            sb.append(
                when (c) {
                    in '۰'..'۹' -> ('0' + (c.code - '۰'.code)).toChar()
                    in '\u0660'..'\u0669' -> ('0' + (c.code - '\u0660'.code)).toChar()
                    else -> c
                }
            )
        }
        return sb.toString()
    }

    fun today(): Triple<Int, Int, Int> = fromMillis(System.currentTimeMillis())

    fun todayMillis(): Long = startOfDayMillis(System.currentTimeMillis())

    fun fromMillis(millis: Long): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        val gy = cal.get(Calendar.YEAR)
        val gm = cal.get(Calendar.MONTH) + 1
        val gd = cal.get(Calendar.DAY_OF_MONTH)
        return gregorianToJalali(gy, gm, gd)
    }

    fun toMillis(jy: Int, jm: Int, jd: Int): Long {
        val (gy, gm, gd) = jalaliToGregorian(jy, jm, jd)
        val cal = Calendar.getInstance()
        cal.set(gy, gm - 1, gd, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun startOfDayMillis(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun daysInMonth(jy: Int, jm: Int): Int {
        if (jm in 1..6) return 31
        if (jm in 7..11) return 30
        return if (isLeapJalaliYear(jy)) 30 else 29
    }

    fun isLeapJalaliYear(jy: Int): Boolean {
        val esfand30 = jalaliToGregorian(jy, 12, 30)
        val back = gregorianToJalali(esfand30.first, esfand30.second, esfand30.third)
        return back.first == jy && back.second == 12 && back.third == 30
    }

    fun format(jy: Int, jm: Int, jd: Int): String {
        val s = String.format("%04d/%02d/%02d", jy, jm, jd)
        return toPersianDigits(s)
    }

    fun format(millis: Long): String {
        val (jy, jm, jd) = fromMillis(millis)
        return format(jy, jm, jd)
    }

    fun formatLong(millis: Long): String {
        val (jy, jm, jd) = fromMillis(millis)
        val monthName = getMonthName(jm)
        val day = toPersianDigits(jd.toString())
        val year = toPersianDigits(jy.toString())
        return "$day $monthName $year"
    }

    fun getMonthName(jm: Int): String = MONTH_NAMES.getOrElse(jm - 1) { "" }

    fun getDayName(millis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> 0
            Calendar.SUNDAY -> 1
            Calendar.MONDAY -> 2
            Calendar.TUESDAY -> 3
            Calendar.WEDNESDAY -> 4
            Calendar.THURSDAY -> 5
            Calendar.FRIDAY -> 6
            else -> 0
        }
        return DAY_NAMES[dayOfWeek]
    }

    private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val g_d_m = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 + gd + g_d_m[gm - 1]
        var jy = -1595 + 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + days / 31 else 7 + (days - 186) / 30
        val jd = 1 + if (days < 186) days % 31 else (days - 186) % 30
        return Triple(jy, jm, jd)
    }

    private fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val approxGy = jy + 621
        for (gy in approxGy..(approxGy + 1)) {
            for (gm in 1..12) {
                val maxDay = when (gm) {
                    2 -> if ((gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0) 29 else 28
                    4, 6, 9, 11 -> 30
                    else -> 31
                }
                for (gd in 1..maxDay) {
                    val (jyy, jmm, jdd) = gregorianToJalali(gy, gm, gd)
                    if (jyy == jy && jmm == jm && jdd == jd) {
                        return Triple(gy, gm, gd)
                    }
                }
            }
        }
        return Triple(approxGy, 1, 1)
    }
}
