package com.example.helloworld

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.helloworld.databinding.ActivityStepExerciseBinding

class StepExerciseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStepExerciseBinding
    private var selectedDifficulty: String = FrontCameraActivity.DIFFICULTY_BEGINNER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        binding = ActivityStepExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.stepExerciseBack.setOnClickListener { finish() }

        binding.stepExerciseCameraIcon.setOnClickListener {
            startActivity(Intent(this, FrontCameraActivity::class.java).apply {
                putExtra(ExercisePoseChecker.EXTRA_EXERCISE_TYPE, ExercisePoseChecker.TYPE_STEP)
                putExtra(FrontCameraActivity.EXTRA_DIFFICULTY, selectedDifficulty)
            })
        }

        binding.btnBeginner.setOnClickListener {
            selectedDifficulty = FrontCameraActivity.DIFFICULTY_BEGINNER
            binding.stepExerciseContentImage.setImageResource(R.drawable.step_exercise_screen)
        }
        binding.btnIntermediate.setOnClickListener {
            selectedDifficulty = FrontCameraActivity.DIFFICULTY_INTERMEDIATE
            binding.stepExerciseContentImage.setImageResource(R.drawable.step_exercise_screen)
        }
        binding.btnAdvanced.setOnClickListener {
            selectedDifficulty = FrontCameraActivity.DIFFICULTY_ADVANCED
            binding.stepExerciseContentImage.setImageResource(R.drawable.step_exercise_screen)
        }

        binding.stepExerciseNavWorkout.setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
            finish()
        }
        binding.stepExerciseNavHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }
        binding.stepExerciseNavRewards.setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
            finish()
        }
    }
}
