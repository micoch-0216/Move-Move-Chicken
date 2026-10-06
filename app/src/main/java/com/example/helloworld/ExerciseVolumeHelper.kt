package com.example.helloworld

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import java.util.Locale

/**
 * 依「今日」儲存與讀取各運動類型的累計秒數（使用者帶手機在鏡頭前運動的時長）。
 * 卡路里由時數 × 該運動每小時大卡估算。
 */
object ExerciseVolumeHelper {

    private const val PREFS_NAME = "exercise_volume"
    private const val KEY_PREFIX = "exercise_sec_"

    /** 超慢跑：約 300 大卡/小時（MET～5、預設體重 60kg 估算） */
    const val KCAL_PER_HOUR_SLOW_RUN = 300

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun todayKey(exerciseType: String): String {
        val c = Calendar.getInstance()
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH) + 1
        val d = c.get(Calendar.DAY_OF_MONTH)
        return "${KEY_PREFIX}${exerciseType}_${y}-${m.toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"
    }

    /** 將本次運動秒數累加到今日該類型 */
    fun addExerciseSeconds(context: Context, exerciseType: String, seconds: Int) {
        if (seconds <= 0) return
        val key = todayKey(exerciseType)
        val current = prefs(context).getInt(key, 0)
        prefs(context).edit().putInt(key, current + seconds).apply()
    }

    /** 取得今日該運動類型已累計的秒數 */
    fun getExerciseSecondsToday(context: Context, exerciseType: String): Int =
        prefs(context).getInt(todayKey(exerciseType), 0)

    /** 由秒數估算卡路里（大卡）。目前僅超慢跑有常數，其餘可擴充 */
    fun estimateCalories(exerciseType: String, totalSeconds: Int): Int {
        val hours = totalSeconds / 3600.0
        return when (exerciseType) {
            ExercisePoseChecker.TYPE_SLOW_RUN -> (hours * KCAL_PER_HOUR_SLOW_RUN).toInt()
            else -> (hours * KCAL_PER_HOUR_SLOW_RUN).toInt()
        }
    }
}
