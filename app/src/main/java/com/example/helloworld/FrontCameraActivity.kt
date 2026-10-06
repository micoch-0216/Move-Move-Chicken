package com.example.helloworld

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.helloworld.databinding.ActivityFrontCameraBinding
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FrontCameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFrontCameraBinding
    private lateinit var cameraExecutor: ExecutorService

    private var poseLandmarker: PoseLandmarker? = null
    private val poseModelPath = "pose_landmarker_lite.task"

    /** 目前要校驗的運動類型，null 表示不校驗姿勢 */
    private var exerciseType: String? = null
    private var lastAlertTime = 0L
    private val alertCooldownMs = 2000L
    private var lastPoseFeedbackTime = 0L
    private val poseFeedbackIntervalMs = 800L
    private val poseFeedbackDurationMs = 1500L
    private var toneGenerator: ToneGenerator? = null

    /** 難度：初級／中級／高級 */
    private var difficultyLevel: String = FrontCameraActivity.DIFFICULTY_BEGINNER
    /** 倒數總秒數（依難度：初級 15 分、中級 30 分、高級 60 分） */
    private var totalDurationSec = COUNTDOWN_SEC_BEGINNER
    private var timerStartMs = 0L
    /** 是否已偵測到完整骨架並開始倒數 */
    private var distanceCheckPassed = false
    /** 連續「骨架完整」的幀數，達門檻即開始倒數 */
    private var distanceOkConsecutiveFrames = 0
    /** 上次顯示骨架點數 Toast 的時間，用於節流 */
    private var lastSkeletonToastTime = 0L
    private val skeletonToastIntervalMs = 1500L
    /** 本趟是否已寫入運動量，避免重複累加 */
    private var sessionRecorded = false
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (timerStartMs > 0L && totalDurationSec > 0) {
                val elapsedSec = ((System.currentTimeMillis() - timerStartMs) / 1000).toInt()
                val remainingSec = (totalDurationSec - elapsedSec).coerceAtLeast(0)
                val m = remainingSec / 60
                val s = remainingSec % 60
                binding.frontCameraTimer.text = String.format(Locale.getDefault(), "%02d:%02d", m, s)
                if (remainingSec <= 0) {
                    timerHandler.removeCallbacks(this)
                    recordSessionIfNeeded(elapsedSec)
                    return
                }
            }
            timerHandler.postDelayed(this, 1000L)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startFrontCamera()
        } else {
            Toast.makeText(this, "需要相機權限才能使用前鏡頭", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        binding = ActivityFrontCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraExecutor = Executors.newSingleThreadExecutor()
        exerciseType = intent.getStringExtra(ExercisePoseChecker.EXTRA_EXERCISE_TYPE)
        difficultyLevel = intent.getStringExtra(FrontCameraActivity.EXTRA_DIFFICULTY) ?: DIFFICULTY_BEGINNER

        binding.frontCameraDifficulty.text = when (difficultyLevel) {
            DIFFICULTY_INTERMEDIATE -> getString(R.string.difficulty_intermediate)
            DIFFICULTY_ADVANCED -> getString(R.string.difficulty_advanced)
            else -> getString(R.string.difficulty_beginner)
        }
        totalDurationSec = when (difficultyLevel) {
            DIFFICULTY_INTERMEDIATE -> COUNTDOWN_SEC_INTERMEDIATE
            DIFFICULTY_ADVANCED -> COUNTDOWN_SEC_ADVANCED
            else -> COUNTDOWN_SEC_BEGINNER
        }
        binding.frontCameraTimer.text = formatCountdown(totalDurationSec)

        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            Log.e(TAG, "ToneGenerator init failed", e)
        }

        binding.frontCameraOverlay.mirrorForFrontCamera = false
        binding.frontCameraClose.setOnClickListener { finish() }

        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                startFrontCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCountdownTimer() {
        totalDurationSec = when (difficultyLevel) {
            DIFFICULTY_INTERMEDIATE -> COUNTDOWN_SEC_INTERMEDIATE
            DIFFICULTY_ADVANCED -> COUNTDOWN_SEC_ADVANCED
            else -> COUNTDOWN_SEC_BEGINNER
        }
        timerStartMs = System.currentTimeMillis()
        timerHandler.post(timerRunnable)
    }

    private fun formatCountdown(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }

    override fun onResume() {
        super.onResume()
        if (timerStartMs > 0L) timerHandler.post(timerRunnable)
    }

    override fun onPause() {
        super.onPause()
        timerHandler.removeCallbacks(timerRunnable)
        if (timerStartMs > 0L) {
            val elapsedSec = ((System.currentTimeMillis() - timerStartMs) / 1000).toInt()
            recordSessionIfNeeded(elapsedSec)
        }
    }

    private fun initPoseLandmarker(): Boolean {
        if (!assetExists(poseModelPath)) {
            Log.e(TAG, "Model not found in assets: $poseModelPath")
            runOnUiThread {
                Toast.makeText(
                    this,
                    "缺少姿態模型檔。請將 pose_landmarker_lite.task 放入 app/src/main/assets/",
                    Toast.LENGTH_LONG
                ).show()
            }
            return false
        }
        val confidence = 0.4f
        fun buildOptions(delegate: Delegate): PoseLandmarker.PoseLandmarkerOptions {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(poseModelPath)
                .setDelegate(delegate)
                .build()
            return PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinPoseDetectionConfidence(confidence)
                .setMinPosePresenceConfidence(confidence)
                .setMinTrackingConfidence(confidence)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener(this::onPoseLandmarkerResult)
                .setErrorListener { e -> Log.e(TAG, "MediaPipe Pose error", e) }
                .build()
        }
        return try {
            poseLandmarker = PoseLandmarker.createFromOptions(this, buildOptions(Delegate.GPU))
            Log.d(TAG, "MediaPipe Pose Landmarker initialized (GPU)")
            true
        } catch (_: Exception) {
            try {
                poseLandmarker = PoseLandmarker.createFromOptions(this, buildOptions(Delegate.CPU))
                Log.d(TAG, "MediaPipe Pose Landmarker initialized (CPU)")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Pose Landmarker init failed: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this, "姿態骨架初始化失敗: ${e.message}", Toast.LENGTH_LONG).show()
                }
                false
            }
        }
    }

    private fun assetExists(path: String): Boolean {
        return try {
            assets.open(path).close()
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun onPoseLandmarkerResult(result: PoseLandmarkerResult, image: MPImage) {
        val landmarksList = result.landmarks()
        if (landmarksList.isEmpty()) return
        val landmarks = landmarksList[0]

        // 未開始倒數時：以 Toast 顯示目前骨架點數（節流），骨架完整且連續數幀穩定才開始倒數
        if (!distanceCheckPassed) {
            val now = System.currentTimeMillis()
            if (now - lastSkeletonToastTime >= skeletonToastIntervalMs) {
                lastSkeletonToastTime = now
                runOnUiThread {
                    Toast.makeText(
                        this,
                        getString(R.string.camera_skeleton_points, landmarks.size),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            if (isSkeletonComplete(landmarks)) {
                distanceOkConsecutiveFrames++
                if (distanceOkConsecutiveFrames >= DISTANCE_OK_FRAMES) {
                    runOnUiThread {
                        distanceCheckPassed = true
                        binding.frontCameraDistanceHint.visibility = View.GONE
                        Toast.makeText(this, getString(R.string.camera_skeleton_detected), Toast.LENGTH_SHORT).show()
                        startCountdownTimer()
                    }
                }
            } else {
                distanceOkConsecutiveFrames = 0
            }
            runOnUiThread {
                binding.frontCameraOverlay.setResults(
                    result,
                    image.width,
                    image.height,
                    RunningMode.LIVE_STREAM
                )
            }
            return
        }

        runOnUiThread {
            binding.frontCameraOverlay.setResults(
                result,
                image.width,
                image.height,
                RunningMode.LIVE_STREAM
            )
        }
        val type = exerciseType ?: return
        val correct = ExercisePoseChecker.isPoseCorrect(landmarks, type)
        runOnUiThread {
            showPoseFeedback(correct)
            if (!correct) playWrongPoseAlert()
        }
    }

    /** 偵測結果反饋：短暫顯示「姿勢正確」或「請調整姿勢」，供使用者即時得知偵測結果 */
    private fun showPoseFeedback(isCorrect: Boolean) {
        val now = System.currentTimeMillis()
        if (now - lastPoseFeedbackTime < poseFeedbackIntervalMs) return
        lastPoseFeedbackTime = now
        binding.frontCameraPoseFeedback.text = if (isCorrect) getString(R.string.pose_feedback_correct) else getString(R.string.pose_feedback_wrong)
        binding.frontCameraPoseFeedback.visibility = View.VISIBLE
        timerHandler.removeCallbacks(poseFeedbackHideRunnable)
        timerHandler.postDelayed(poseFeedbackHideRunnable, poseFeedbackDurationMs)
    }

    private val poseFeedbackHideRunnable = Runnable {
        binding.frontCameraPoseFeedback.visibility = View.GONE
    }

    /** MediaPipe 姿態為 33 點，點數足夠視為骨架完整 */
    private fun isSkeletonComplete(landmarks: List<NormalizedLandmark>): Boolean {
        return landmarks.size >= 30
    }

    private fun playWrongPoseAlert() {
        val now = System.currentTimeMillis()
        if (now - lastAlertTime < alertCooldownMs) return
        lastAlertTime = now
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 200)
        } catch (_: Exception) { }
    }

    private fun startFrontCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreviewAndAnalysis(cameraProvider, binding.frontCameraPreview)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreviewAndAnalysis(cameraProvider: ProcessCameraProvider, previewView: PreviewView) {
        val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.rotation ?: 0
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.rotation
        }
        val preview = Preview.Builder()
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        val useAnalysis = initPoseLandmarker()
        val analysis = if (useAnalysis) {
            ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy -> analyzeFrame(imageProxy) }
                }
        } else null

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        try {
            cameraProvider.unbindAll()
            if (analysis != null) {
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis)
            } else {
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
                // 無法使用姿勢偵測時，延遲後直接開始倒數並提示
                timerHandler.postDelayed({
                    if (!distanceCheckPassed && timerStartMs == 0L) {
                        distanceCheckPassed = true
                        binding.frontCameraDistanceHint.visibility = View.GONE
                        Toast.makeText(this, getString(R.string.camera_distance_no_detect), Toast.LENGTH_SHORT).show()
                        startCountdownTimer()
                    }
                }, 2000L)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "此裝置沒有前鏡頭", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private var lastLogTime = 0L
    private fun analyzeFrame(imageProxy: ImageProxy) {
        val format = imageProxy.format
        val planeCount = imageProxy.planes.size
        val bitmap = imageProxyToBitmap(imageProxy)
        imageProxy.close()
        if (bitmap == null) {
            if (System.currentTimeMillis() - lastLogTime > 3000) {
                lastLogTime = System.currentTimeMillis()
                Log.w(TAG, "imageProxyToBitmap returned null (format=$format, planes=$planeCount)")
            }
            return
        }
        if (poseLandmarker == null) return
        val mpImage = BitmapImageBuilder(bitmap).build()
        val timestampMs = System.currentTimeMillis()
        poseLandmarker?.detectAsync(mpImage, timestampMs)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerHandler.removeCallbacks(timerRunnable)
        timerHandler.removeCallbacks(poseFeedbackHideRunnable)
        toneGenerator?.release()
        toneGenerator = null
        poseLandmarker?.close()
        poseLandmarker = null
        cameraExecutor.shutdown()
    }

    /** 若有運動類型且尚未寫入，將本趟實際運動秒數累加到今日運動量 */
    private fun recordSessionIfNeeded(elapsedSec: Int) {
        if (sessionRecorded) return
        val type = exerciseType ?: return
        if (elapsedSec <= 0) return
        val actualSec = minOf(elapsedSec, totalDurationSec)
        ExerciseVolumeHelper.addExerciseSeconds(this, type, actualSec)
        sessionRecorded = true
    }

    companion object {
        private const val TAG = "FrontCamera"
        const val EXTRA_DIFFICULTY = "difficulty"
        const val DIFFICULTY_BEGINNER = "beginner"
        const val DIFFICULTY_INTERMEDIATE = "intermediate"
        const val DIFFICULTY_ADVANCED = "advanced"
        /** 初級 15 分鐘、中級 30 分鐘、高級 60 分鐘（秒） */
        private const val COUNTDOWN_SEC_BEGINNER = 15 * 60
        private const val COUNTDOWN_SEC_INTERMEDIATE = 30 * 60
        private const val COUNTDOWN_SEC_ADVANCED = 60 * 60
        /** 連續多少幀骨架完整才開始倒數（約 0.3 秒穩定即開始） */
        private const val DISTANCE_OK_FRAMES = 10
    }
}
