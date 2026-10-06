package com.example.helloworld

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.helloworld.databinding.ActivityStaticlungeBinding

class StaticLungeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaticlungeBinding
    private var selectedDifficulty: String = FrontCameraActivity.DIFFICULTY_BEGINNER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        binding = ActivityStaticlungeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.staticLungeBack.setOnClickListener { finish() }

        binding.staticLungeCameraIcon.setOnClickListener {
            startActivity(Intent(this, FrontCameraActivity::class.java).apply {
                putExtra(ExercisePoseChecker.EXTRA_EXERCISE_TYPE, ExercisePoseChecker.TYPE_STATIC_LUNGE)
                putExtra(FrontCameraActivity.EXTRA_DIFFICULTY, selectedDifficulty)
            })
        }
        binding.staticLungeNavHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }
        binding.staticLungeNavWorkout.setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
            finish()
        }
        binding.staticLungeNavRewards.setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
            finish()
        }

        binding.btnBeginner.setOnClickListener {
            selectedDifficulty = FrontCameraActivity.DIFFICULTY_BEGINNER
            binding.staticLungeContentImage.setImageResource(R.drawable.static_lunge_screen)
        }
        binding.btnIntermediate.setOnClickListener {
            selectedDifficulty = FrontCameraActivity.DIFFICULTY_INTERMEDIATE
            binding.staticLungeContentImage.setImageResource(R.drawable.static_lunge_screen)
        }
        binding.btnAdvanced.setOnClickListener {
            selectedDifficulty = FrontCameraActivity.DIFFICULTY_ADVANCED
            binding.staticLungeContentImage.setImageResource(R.drawable.static_lunge_screen)
        }
    }
}
