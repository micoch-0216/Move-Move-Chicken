package com.example.helloworld

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.helloworld.databinding.ActivityDailyTasksBinding

class DailyTasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDailyTasksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        binding = ActivityDailyTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 簽到：點擊後切換為附上的簽到後圖片（明天再回來簽到以領取獎勵）
        binding.dailyTasksCheckinButton.setOnClickListener {
            binding.dailyTasksContentImage.setImageResource(R.drawable.daily_tasks_screen_after_checkin)
            binding.dailyTasksCheckinButton.isEnabled = false
            binding.dailyTasksCheckinButton.alpha = 0.7f
        }

        // 運動量區：超慢跑、靜態弓箭步可點擊進入詳情頁
        binding.root.findViewById<android.view.View>(R.id.rewardsExerciseSlowRunRow)?.setOnClickListener {
            startActivity(Intent(this, SlowRunDetailActivity::class.java))
        }
        binding.root.findViewById<android.view.View>(R.id.rewardsExerciseStaticLungeRow)?.setOnClickListener {
            startActivity(Intent(this, StaticLungeDetailActivity::class.java))
        }

        // 底部導覽：運動頁
        binding.dailyTasksNavWorkout.setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
            finish()
        }
        // 底部導覽：首頁
        binding.dailyTasksNavHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }
        // 底部導覽：獎勵／打卡頁
        binding.dailyTasksNavRewards.setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
            finish()
        }
    }
}
