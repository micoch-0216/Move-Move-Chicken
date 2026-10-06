package com.example.helloworld

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.helloworld.databinding.ActivityShoulderStretchBinding

class ShoulderStretchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShoulderStretchBinding
    private var selectedDifficulty: String = FrontCameraActivity.DIFFICULTY_BEGINNER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        binding = ActivityShoulderStretchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.shoulderStretchBack.setOnClickListener { finish() }

        binding.shoulderStretchCameraIcon.setOnClickListener {
            startActivity(Intent(this, FrontCameraActivity::class.java).apply {
                putExtra(ExercisePoseChecker.EXTRA_EXERCISE_TYPE, ExercisePoseChecker.TYPE_SHOULDER_STRETCH)
                putExtra(FrontCameraActivity.EXTRA_DIFFICULTY, selectedDifficulty)
            })
        }

        binding.btnBeginner.setOnClickListener {
            selectedDifficulty = FrontCameraActivity.DIFFICULTY_BEGINNER
            binding.shoulderStretchContentImage.setImageResource(R.drawable.shoulder_stretch_screen)
        }
        binding.btnIntermediate.setOnClickListener {
            selectedDifficulty = FrontCameraActivity.DIFFICULTY_INTERMEDIATE
            binding.shoulderStretchContentImage.setImageResource(R.drawable.shoulder_stretch_screen)
        }
        binding.btnAdvanced.setOnClickListener {
            selectedDifficulty = FrontCameraActivity.DIFFICULTY_ADVANCED
            binding.shoulderStretchContentImage.setImageResource(R.drawable.shoulder_stretch_screen)
        }

        binding.shoulderStretchNavWorkout.setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
            finish()
        }
        binding.shoulderStretchNavHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }
        binding.shoulderStretchNavRewards.setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
            finish()
        }
    }
}
