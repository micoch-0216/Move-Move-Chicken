package com.example.helloworld

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.helloworld.databinding.ActivityWorkoutBinding

class WorkoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        binding = ActivityWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.workoutListImage.setImageResource(R.drawable.workout_list)

        binding.cardSlowRun.setOnClickListener {
            startActivity(Intent(this, SlowRunActivity::class.java))
        }
        binding.cardStaticLunge.setOnClickListener {
            startActivity(Intent(this, StaticLungeActivity::class.java))
        }
        binding.cardStepUp.setOnClickListener {
            startActivity(Intent(this, StepExerciseActivity::class.java))
        }
        binding.cardSquat.setOnClickListener {
            startActivity(Intent(this, SquatActivity::class.java))
        }
        binding.cardShoulderStretch.setOnClickListener {
            startActivity(Intent(this, ShoulderStretchActivity::class.java))
        }
        binding.workoutNavHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }
        binding.workoutNavRewards.setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
        }
    }
}
