package com.example.helloworld

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.helloworld.databinding.ActivityStaticLungeDetailBinding

/** 靜態弓箭步目標時數（小時） */
private const val STATIC_LUNGE_GOAL_HOURS = 3

/**
 * 運動量詳情頁：靜態弓箭步。顯示今日已運動時數與消耗卡路里。
 */
class StaticLungeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaticLungeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.BLACK

        binding = ActivityStaticLungeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.staticLungeDetailBack.setOnClickListener { finish() }

        updateVolumeDisplay()

        binding.staticLungeDetailNavWorkout.setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
            finish()
        }
        binding.staticLungeDetailNavHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }
        binding.staticLungeDetailNavRewards.setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        updateVolumeDisplay()
    }

    private fun updateVolumeDisplay() {
        val seconds = ExerciseVolumeHelper.getExerciseSecondsToday(this, ExercisePoseChecker.TYPE_STATIC_LUNGE)
        val hours = seconds / 3600.0
        val calories = ExerciseVolumeHelper.estimateCalories(ExercisePoseChecker.TYPE_STATIC_LUNGE, seconds)
        binding.staticLungeDetailHours.text = getString(R.string.exercise_volume_hours_decimal, hours, STATIC_LUNGE_GOAL_HOURS)
        binding.staticLungeDetailCalories.text = getString(R.string.exercise_volume_calories_value, calories)
    }
}
