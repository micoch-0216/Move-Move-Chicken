package com.example.helloworld

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.helloworld.databinding.ActivityRewardsBinding

class RewardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        binding = ActivityRewardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rewardsBack.setOnClickListener { finish() }
        binding.root.findViewById<android.view.View>(R.id.rewardsExerciseSlowRunRow).setOnClickListener {
            startActivity(Intent(this, SlowRunDetailActivity::class.java))
        }
        binding.root.findViewById<android.view.View>(R.id.rewardsExerciseStaticLungeRow).setOnClickListener {
            startActivity(Intent(this, StaticLungeDetailActivity::class.java))
        }
        binding.rewardsCheckinButton.setOnClickListener {
            binding.rewardsContentImage.setImageResource(R.drawable.rewards_screen_after_checkin)
        }
        binding.rewardsNavWorkout.setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
        }
        binding.rewardsNavHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }
    }
}
