package com.example.helloworld

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.helloworld.databinding.ActivitySlowrunBinding

class SlowRunActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySlowrunBinding
    private var selectedDifficulty: String = FrontCameraActivity.DIFFICULTY_BEGINNER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        binding = ActivitySlowrunBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.slowrunContentImage.setImageResource(R.drawable.slowrun_screen)
        binding.slowrunBack.setOnClickListener { finish() }

        binding.btnBeginner.setOnClickListener {
            selectedDifficulty = FrontCameraActivity.DIFFICULTY_BEGINNER
            binding.slowrunContentImage.setImageResource(R.drawable.slowrun_beginner)
        }
        binding.btnIntermediate.setOnClickListener {
            selectedDifficulty = FrontCameraActivity.DIFFICULTY_INTERMEDIATE
            binding.slowrunContentImage.setImageResource(R.drawable.slowrun_intermediate)
        }
        binding.btnAdvanced.setOnClickListener {
            selectedDifficulty = FrontCameraActivity.DIFFICULTY_ADVANCED
            binding.slowrunContentImage.setImageResource(R.drawable.slowrun_advanced)
        }
        binding.slowrunNavHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }

        binding.slowrunCameraIcon.setOnClickListener {
            startActivity(Intent(this, FrontCameraActivity::class.java).apply {
                putExtra(ExercisePoseChecker.EXTRA_EXERCISE_TYPE, ExercisePoseChecker.TYPE_SLOW_RUN)
                putExtra(FrontCameraActivity.EXTRA_DIFFICULTY, selectedDifficulty)
            })
        }
    }
}
