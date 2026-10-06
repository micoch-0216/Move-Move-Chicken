package com.example.helloworld

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.helloworld.databinding.ActivityWardrobeBinding

class WardrobeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWardrobeBinding
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        binding = ActivityWardrobeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.wardrobeCardsImage.setImageResource(R.drawable.wardrobe_cards)
        // 每次點擊衣櫃進入此頁時顯示右上角關閉鈕
        binding.wardrobeClose.visibility = View.VISIBLE
        binding.wardrobeClose.setOnClickListener { finish() }
        binding.wardrobeNavHome.setOnClickListener { finish() }

        binding.sportyCardArea.setOnClickListener {
            binding.sportyOverlayImage.setImageResource(R.drawable.wardrobe_sporty)
            binding.sportyOverlay.visibility = View.VISIBLE
            binding.wardrobeClose.visibility = View.GONE
            getSharedPreferences("app", MODE_PRIVATE).edit()
                .putString("home_image", "sporty")
                .apply()
            finish()
        }
        binding.sportyOverlayClose.setOnClickListener { hideSportyOverlay() }
        binding.sportyWardrobeArea.setOnClickListener { hideSportyOverlay() }
        binding.sportyWaterArea.setOnClickListener {
            binding.sportyOverlayImage.setImageResource(R.drawable.wardrobe_sporty_water)
            handler.removeCallbacks(restoreSportyImage)
            handler.postDelayed(restoreSportyImage, 2000L)
        }
        binding.sportyBananaArea.setOnClickListener {
            binding.sportyOverlayImage.setImageResource(R.drawable.wardrobe_sporty_banana)
            handler.removeCallbacks(restoreSportyImage)
            handler.postDelayed(restoreSportyImage, 2000L)
        }
        binding.sportyNavMuscle.setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
            finish()
        }

        binding.newyearCardArea.setOnClickListener {
            getSharedPreferences("app", MODE_PRIVATE).edit()
                .putString("home_image", "newyear")
                .apply()
            finish()
        }

        binding.halloweenCardArea.setOnClickListener {
            binding.halloweenOverlayImage.setImageResource(R.drawable.wardrobe_halloween)
            binding.halloweenOverlay.visibility = View.VISIBLE
            binding.wardrobeClose.visibility = View.GONE
        }
        binding.halloweenOverlayClose.setOnClickListener { hideHalloweenOverlay() }
        binding.halloweenWardrobeArea.setOnClickListener { hideHalloweenOverlay() }
        binding.halloweenWaterArea.setOnClickListener {
            binding.halloweenOverlayImage.setImageResource(R.drawable.wardrobe_halloween_water)
            handler.removeCallbacks(restoreHalloweenImage)
            handler.postDelayed(restoreHalloweenImage, 2000L)
        }
        binding.halloweenBananaArea.setOnClickListener {
            binding.halloweenOverlayImage.setImageResource(R.drawable.wardrobe_halloween_banana)
            handler.removeCallbacks(restoreHalloweenImage)
            handler.postDelayed(restoreHalloweenImage, 2000L)
        }
        binding.halloweenNavMuscle.setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
            finish()
        }
    }

    private val restoreSportyImage = Runnable {
        binding.sportyOverlayImage.setImageResource(R.drawable.wardrobe_sporty)
    }

    private fun hideSportyOverlay() {
        handler.removeCallbacks(restoreSportyImage)
        binding.sportyOverlay.visibility = View.GONE
        // 選擇完後不顯示叉叉，等下次點擊衣櫃再出現
    }

    private val restoreHalloweenImage = Runnable {
        binding.halloweenOverlayImage.setImageResource(R.drawable.wardrobe_halloween)
    }

    private fun hideHalloweenOverlay() {
        handler.removeCallbacks(restoreHalloweenImage)
        binding.halloweenOverlay.visibility = View.GONE
        // 選擇完後不顯示叉叉，等下次點擊衣櫃再出現
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(restoreSportyImage)
        handler.removeCallbacks(restoreHalloweenImage)
    }
}
