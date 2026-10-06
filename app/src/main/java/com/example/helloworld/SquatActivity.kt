package com.example.helloworld

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.helloworld.databinding.ActivitySquatBinding

class SquatActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySquatBinding
    private var selectedDifficulty: String = FrontCameraActivity.DIFFICULTY_BEGINNER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        binding = ActivitySquatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.squatBack.setOnClickListener { finish() }

        binding.squatCameraIcon.setOnClickListener {
            startActivity(Intent(this, FrontCameraActivity::class.java).apply {
                putExtra(ExercisePoseChecker.EXTRA_EXERCISE_TYPE, ExercisePoseChecker.TYPE_SQUAT)
                putExtra(FrontCameraActivity.EXTRA_DIFFICULTY, selectedDifficulty)
            })
        }

        binding.btnBeginner.setOnClickListener {
            selectedDifficulty = FrontCameraActivity.DIFFICULTY_BEGINNER
            binding.squatContentImage.setImageResource(R.drawable.squat_screen)
        }
        binding.btnIntermediate.setOnClickListener {
            selectedDifficulty = FrontCameraActivity.DIFFICULTY_INTERMEDIATE
            binding.squatContentImage.setImageResource(R.drawable.squat_screen)
        }
        binding.btnAdvanced.setOnClickListener {
            selectedDifficulty = FrontCameraActivity.DIFFICULTY_ADVANCED
            binding.squatContentImage.setImageResource(R.drawable.squat_screen)
        }

        binding.squatNavWorkout.setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
            finish()
        }
        binding.squatNavHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }
        binding.squatNavRewards.setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
            finish()
        }
    }
}
