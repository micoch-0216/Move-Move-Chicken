package com.example.helloworld

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.helloworld.databinding.ActivitySlowRunDetailBinding

/** 超慢跑目標時數（小時） */
private const val SLOW_RUN_GOAL_HOURS = 3

/**
 * 運動量詳情頁：超慢跑。顯示今日已運動時數與消耗卡路里（依帶手機在鏡頭前運動的時長計算）。
 */
class SlowRunDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySlowRunDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.BLACK

        binding = ActivitySlowRunDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.slowRunDetailBack.setOnClickListener { finish() }

        updateVolumeDisplay()

        binding.slowRunDetailNavWorkout.setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
            finish()
        }
        binding.slowRunDetailNavHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }
        binding.slowRunDetailNavRewards.setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        updateVolumeDisplay()
    }

    /** 從儲存讀取今日超慢跑秒數，換算時數與卡路里並更新畫面 */
    private fun updateVolumeDisplay() {
        val seconds = ExerciseVolumeHelper.getExerciseSecondsToday(this, ExercisePoseChecker.TYPE_SLOW_RUN)
        val hours = seconds / 3600.0
        val calories = ExerciseVolumeHelper.estimateCalories(ExercisePoseChecker.TYPE_SLOW_RUN, seconds)
        binding.slowRunDetailHours.text = getString(R.string.exercise_volume_hours_decimal, hours, SLOW_RUN_GOAL_HOURS)
        binding.slowRunDetailCalories.text = getString(R.string.exercise_volume_calories_value, calories)
    }
}
