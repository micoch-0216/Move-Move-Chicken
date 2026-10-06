package com.example.helloworld

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.acos
import kotlin.math.sqrt
import kotlin.math.PI

/**
 * 依 MediaPipe 姿態偵測結果，判斷目前姿勢是否符合該運動的正確姿勢。
 * 若不符合則可觸發警示音提醒使用者。
 *
 * MediaPipe 33 點索引：11/12 肩、13/14 肘、15/16 腕、23/24 髋、25/26 膝、27/28 踝
 */
object ExercisePoseChecker {

    const val EXTRA_EXERCISE_TYPE = "exercise_type"

    /** 運動類型，與各 Activity 傳入的 value 一致 */
    const val TYPE_SLOW_RUN = "slow_run"
    const val TYPE_STATIC_LUNGE = "static_lunge"
    const val TYPE_SQUAT = "squat"
    const val TYPE_STEP = "step"
    const val TYPE_SHOULDER_STRETCH = "shoulder_stretch"

    private const val DEG = 180f / PI.toFloat()

    /** 三點夾角（B 為頂點），單位度數；若無法計算回傳 null */
    private fun angleDeg(a: NormalizedLandmark, b: NormalizedLandmark, c: NormalizedLandmark): Float? {
        val ax = a.x(); val ay = a.y()
        val bx = b.x(); val by = b.y()
        val cx = c.x(); val cy = c.y()
        val v1x = ax - bx; val v1y = ay - by
        val v2x = cx - bx; val v2y = cy - by
        val len1 = sqrt(v1x * v1x + v1y * v1y)
        val len2 = sqrt(v2x * v2x + v2y * v2y)
        if (len1 < 1e-6f || len2 < 1e-6f) return null
        val dot = v1x * v2x + v1y * v2y
        val cos = (dot / (len1 * len2)).coerceIn(-1f, 1f)
        return acos(cos) * DEG
    }

    private fun getL(landmarks: List<NormalizedLandmark>, i: Int): NormalizedLandmark? =
        if (i < landmarks.size) landmarks[i] else null

    /** 左膝角：髋-膝-踝 */
    private fun leftKneeAngle(landmarks: List<NormalizedLandmark>): Float? {
        val h = getL(landmarks, 23) ?: return null
        val k = getL(landmarks, 25) ?: return null
        val a = getL(landmarks, 27) ?: return null
        return angleDeg(h, k, a)
    }

    /** 右膝角 */
    private fun rightKneeAngle(landmarks: List<NormalizedLandmark>): Float? {
        val h = getL(landmarks, 24) ?: return null
        val k = getL(landmarks, 26) ?: return null
        val a = getL(landmarks, 28) ?: return null
        return angleDeg(h, k, a)
    }

    /** 左肘角：肩-肘-腕 */
    private fun leftElbowAngle(landmarks: List<NormalizedLandmark>): Float? {
        val s = getL(landmarks, 11) ?: return null
        val e = getL(landmarks, 13) ?: return null
        val w = getL(landmarks, 15) ?: return null
        return angleDeg(s, e, w)
    }

    /** 右肘角 */
    private fun rightElbowAngle(landmarks: List<NormalizedLandmark>): Float? {
        val s = getL(landmarks, 12) ?: return null
        val e = getL(landmarks, 14) ?: return null
        val w = getL(landmarks, 16) ?: return null
        return angleDeg(s, e, w)
    }

    /**
     * 檢查目前姿勢是否大致正確。
     * @param landmarks MediaPipe 單一人的 33 點
     * @param exerciseType 運動類型常數
     * @return true 表示姿勢在可接受範圍，false 表示需警示
     */
    fun isPoseCorrect(landmarks: List<NormalizedLandmark>, exerciseType: String): Boolean {
        if (landmarks.size < 29) return true // 點不足時不亂警示
        return when (exerciseType) {
            TYPE_STATIC_LUNGE -> checkStaticLunge(landmarks)
            TYPE_SQUAT -> checkSquat(landmarks)
            TYPE_SLOW_RUN -> checkSlowRun(landmarks)
            TYPE_STEP -> checkStep(landmarks)
            TYPE_SHOULDER_STRETCH -> checkShoulderStretch(landmarks)
            else -> true
        }
    }

    /** 靜態弓箭步：一腳前彎約 90°，另一腳較直 */
    private fun checkStaticLunge(landmarks: List<NormalizedLandmark>): Boolean {
        val lKnee = leftKneeAngle(landmarks) ?: return true
        val rKnee = rightKneeAngle(landmarks) ?: return true
        val frontMin = 70f
        val frontMax = 110f
        val backMin = 140f
        val (front, back) = if (lKnee < rKnee) lKnee to rKnee else rKnee to lKnee
        return front in frontMin..frontMax && back >= backMin
    }

    /** 深蹲：雙膝彎曲約 70°~120° */
    private fun checkSquat(landmarks: List<NormalizedLandmark>): Boolean {
        val lKnee = leftKneeAngle(landmarks) ?: return true
        val rKnee = rightKneeAngle(landmarks) ?: return true
        val minDeg = 60f
        val maxDeg = 130f
        return lKnee in minDeg..maxDeg && rKnee in minDeg..maxDeg
    }

    /** 超慢跑：至少一腳膝蓋有彎（例如 120°~160° 或 80°~100° 模擬跨步） */
    private fun checkSlowRun(landmarks: List<NormalizedLandmark>): Boolean {
        val lKnee = leftKneeAngle(landmarks) ?: return true
        val rKnee = rightKneeAngle(landmarks) ?: return true
        val bentMin = 80f
        val bentMax = 160f
        return lKnee in bentMin..bentMax || rKnee in bentMin..bentMax
    }

    /** 踏階運動：一腳抬起（膝角較小）或雙腳站姿可接受 */
    private fun checkStep(landmarks: List<NormalizedLandmark>): Boolean {
        val lKnee = leftKneeAngle(landmarks) ?: return true
        val rKnee = rightKneeAngle(landmarks) ?: return true
        val liftedMax = 120f
        val standingMin = 140f
        return (lKnee <= liftedMax && rKnee >= standingMin) ||
                (rKnee <= liftedMax && lKnee >= standingMin) ||
                (lKnee in 140f..180f && rKnee in 140f..180f)
    }

    /** 肩部伸展：至少一手肘抬高（肘角較大，手臂伸展） */
    private fun checkShoulderStretch(landmarks: List<NormalizedLandmark>): Boolean {
        val lElbow = leftElbowAngle(landmarks) ?: return true
        val rElbow = rightElbowAngle(landmarks) ?: return true
        val stretchedMin = 150f
        return lElbow >= stretchedMin || rElbow >= stretchedMin
    }
}
