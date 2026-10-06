package com.example.helloworld

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.helloworld.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private var currentHomeDrawable: Int = R.drawable.home_page

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateHomeImageFromPreference()
        binding.waterBottleArea.setOnClickListener { onWaterBottleClick() }
        binding.bananaArea.setOnClickListener { onBananaClick() }
        binding.wardrobeArea.setOnClickListener { onWardrobeClick() }
        binding.navMuscle.setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
        }
        binding.navRewards.setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateHomeImageFromPreference()
    }

    private fun updateHomeImageFromPreference() {
        val pref = getSharedPreferences("app", MODE_PRIVATE)
        currentHomeDrawable = if (pref.getString("home_image", null) == "newyear") {
            R.drawable.home_page_newyear
        } else {
            R.drawable.home_page
        }
        binding.homeImage.setImageResource(currentHomeDrawable)
    }

    private fun onWaterBottleClick() {
        val waterDrawable = if (currentHomeDrawable == R.drawable.home_page_newyear) {
            R.drawable.home_page_newyear_water
        } else {
            R.drawable.home_page_water
        }
        binding.homeImage.setImageResource(waterDrawable)
        handler.removeCallbacks(restoreHomeImage)
        handler.postDelayed(restoreHomeImage, 2000L)
    }

    private val restoreHomeImage = Runnable {
        binding.homeImage.setImageResource(currentHomeDrawable)
    }

    private fun onBananaClick() {
        val bananaDrawable = if (currentHomeDrawable == R.drawable.home_page_newyear) {
            R.drawable.home_page_newyear_banana
        } else {
            R.drawable.home_page_banana
        }
        binding.homeImage.setImageResource(bananaDrawable)
        handler.removeCallbacks(restoreHomeImage)
        handler.postDelayed(restoreHomeImage, 2000L)
    }

    private fun onWardrobeClick() {
        startActivity(Intent(this, WardrobeActivity::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(restoreHomeImage)
    }
}
