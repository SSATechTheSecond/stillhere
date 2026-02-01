package com.stillhere.domain.model.animation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Camera state for Echo's view.
 * All camera movements are controlled by Echo, not the user.
 */
data class CameraState(
    val zoom: Float = 1f,
    val panX: Float = 0f,      // -1 to 1 (left to right)
    val panY: Float = 0f,      // -1 to 1 (down to up)
    val tilt: Float = 0f,      // Degrees
    val headRotationX: Float = 0f,  // Head tilt up/down
    val headRotationY: Float = 0f,  // Head turn left/right
    val eyeX: Float = 0f,      // Eye offset X (-1 to 1)
    val eyeY: Float = 0f       // Eye offset Y (-1 to 1)
)

/**
 * Target camera state for smooth transitions.
 */
data class CameraTarget(
    val zoom: Float = 1f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val tilt: Float = 0f,
    val headRotationX: Float = 0f,
    val headRotationY: Float = 0f,
    val eyeX: Float = 0f,
    val eyeY: Float = 0f
)

/**
 * Camera controller for Echo's autonomous camera movements.
 */
class CameraController {

    var state by mutableStateOf(CameraState())
        private set

    private var target by mutableStateOf(CameraTarget())
        private set

    private val animationQueue = mutableListOf<CameraAnimation>()
    private var currentAnimation: CameraAnimation? = null

    // Mood-based default camera settings
    private val moodSettings = mapOf(
        EchoMood.HAPPY to CameraTarget(zoom = 1f, panX = 0f, panY = 0f, tilt = 0f),
        EchoMood.SAD to CameraTarget(zoom = 0.9f, panX = 0f, panY = -0.05f, tilt = -2f),
        EchoMood.EXCITED to CameraTarget(zoom = 1.1f, panX = 0f, panY = 0f, tilt = 1f),
        EchoMood.LOVING to CameraTarget(zoom = 1.2f, panX = 0f, panY = 0.05f, tilt = 0f),
        EchoMood.THINKING to CameraTarget(zoom = 1f, panX = 0f, panY = 0f, tilt = 0f),
        EchoMood.SURPRISED to CameraTarget(zoom = 1.3f, panX = 0f, panY = 0f, tilt = 0f),
        EchoMood.EMBARRASSED to CameraTarget(zoom = 0.95f, panX = -0.1f, panY = -0.02f, tilt = -1f),
        EchoMood.BORED to CameraTarget(zoom = 0.9f, panX = 0.05f, panY = 0f, tilt = 0f),
        EchoMood.SLEEPY to CameraTarget(zoom = 0.85f, panX = 0f, panY = -0.08f, tilt = -3f),
        EchoMood.CURIOUS to CameraTarget(zoom = 1f, panX = 0f, panY = 0.02f, tilt = 1f)
    )

    /**
     * Set target camera state with smooth animation.
     */
    fun setTarget(
        zoom: Float = target.zoom,
        panX: Float = target.panX,
        panY: Float = target.panY,
        tilt: Float = target.tilt,
        headRotationX: Float = target.headRotationX,
        headRotationY: Float = target.headRotationY,
        eyeX: Float = target.eyeX,
        eyeY: Float = target.eyeY,
        durationMs: Long = 500L
    ) {
        target = CameraTarget(zoom, panX, panY, tilt, headRotationX, headRotationY, eyeX, eyeY)
        animateToTarget(durationMs)
    }

    /**
     * Apply mood-based camera settings.
     */
    fun applyMood(mood: EchoMood, durationMs: Long = 800L) {
        val moodTarget = moodSettings[mood] ?: CameraTarget()
        target = target.copy(
            zoom = moodTarget.zoom,
            panX = moodTarget.panX,
            panY = moodTarget.panY,
            tilt = moodTarget.tilt
        )
        animateToTarget(durationMs)
    }

    /**
     * Look at a specific point on screen (0-1 coordinates).
     */
    fun lookAt(pointX: Float, pointY: Float, durationMs: Long = 300L) {
        // Convert screen coordinates to camera offsets
        val eyeX = (pointX - 0.5f) * 2f * 0.8f  // -0.8 to 0.8
        val eyeY = (0.5f - pointY) * 2f * 0.8f  // -0.8 to 0.8
        
        // Head follows slightly (30% of eye movement)
        val headY = eyeY * 0.3f
        val headX = eyeX * 0.3f
        
        target = target.copy(
            eyeX = eyeX.coerceIn(-1f, 1f),
            eyeY = eyeY.coerceIn(-1f, 1f),
            headRotationY = headX,
            headRotationX = headY
        )
        animateToTarget(durationMs)
    }

    /**
     * Look away from a point.
     */
    fun lookAway(pointX: Float = 0.5f, pointY: Float = 0.5f, intensity: Float = 0.5f) {
        val awayX = -(pointX - 0.5f) * 2f * intensity
        val awayY = (0.5f - pointY) * 2f * intensity
        
        target = target.copy(
            eyeX = awayX.coerceIn(-1f, 1f),
            eyeY = awayY.coerceIn(-1f, 1f)
        )
        animateToTarget(400L)
    }

    /**
     * Look in a random direction.
     */
    fun lookRandomly(durationMs: Long = 500L) {
        val randomX = (Random.nextFloat() - 0.5f) * 1.2f
        val randomY = (Random.nextFloat() - 0.5f) * 0.8f
        
        target = target.copy(
            eyeX = randomX.coerceIn(-1f, 1f),
            eyeY = randomY.coerceIn(-1f, 1f),
            headRotationY = randomX * 0.3f
        )
        animateToTarget(durationMs)
    }

    /**
     * Center eyes and head.
     */
    fun centerLook(durationMs: Long = 600L) {
        target = target.copy(
            eyeX = 0f,
            eyeY = 0f,
            headRotationX = 0f,
            headRotationY = 0f
        )
        animateToTarget(durationMs)
    }

    /**
     * Set focus level (zoom based on conversation intensity).
     */
    fun setFocusLevel(level: Float, durationMs: Long = 800L) {
        val targetZoom = when {
            level <= 0.25f -> 1.0f
            level <= 0.5f -> 1.2f
            level <= 0.75f -> 1.5f
            else -> 2.0f
        }
        
        val targetPanY = when {
            level <= 0.25f -> 0f
            level <= 0.5f -> -0.02f
            level <= 0.75f -> -0.05f
            else -> -0.08f
        }
        
        target = target.copy(
            zoom = targetZoom,
            panY = targetPanY
        )
        animateToTarget(durationMs)
    }

    /**
     * Add shake effect (e.g., when poked).
     */
    fun shake(intensity: Float = 10f, durationMs: Long = 200L) {
        val originalPanX = target.panX
        val shakeAnimation = ShakeAnimation(
            intensity = intensity,
            duration = durationMs,
            onComplete = {
                target = target.copy(panX = originalPanX)
                updateState()
            }
        )
        currentAnimation = shakeAnimation
    }

    /**
     * Pan toward a target area.
     */
    fun panToward(targetX: Float, targetY: Float, durationMs: Long = 400L) {
        target = target.copy(
            panX = (targetX - 0.5f) * 0.5f,
            panY = (0.5f - targetY) * 0.3f
        )
        animateToTarget(durationMs)
    }

    /**
     * Return to default position.
     */
    fun reset(durationMs: Long = 500L) {
        target = CameraTarget()
        animateToTarget(durationMs)
    }

    private fun animateToTarget(durationMs: Long) {
        // Linear interpolation toward target each frame
        // Actual animation handled in update loop
        val animation = InterpolateAnimation(
            from = state,
            to = target,
            duration = durationMs
        )
        currentAnimation = animation
    }

    /**
     * Update camera state (call this in animation loop).
     */
    fun update(deltaTimeMs: Long) {
        currentAnimation?.let { anim ->
            val progress = anim.elapsed.coerceAtMost(anim.duration).toFloat() / anim.duration.toFloat()
            val easedProgress = easeInOutCubic(progress)
            
            state = CameraState(
                zoom = lerp(anim.from.zoom, anim.to.zoom, easedProgress),
                panX = lerp(anim.from.panX, anim.to.panX, easedProgress),
                panY = lerp(anim.from.panY, anim.to.panY, easedProgress),
                tilt = lerp(anim.from.tilt, anim.to.tilt, easedProgress),
                headRotationX = lerp(anim.from.headRotationX, anim.to.headRotationX, easedProgress),
                headRotationY = lerp(anim.from.headRotationY, anim.to.headRotationY, easedProgress),
                eyeX = lerp(anim.from.eyeX, anim.to.eyeX, easedProgress),
                eyeY = lerp(anim.from.eyeY, anim.to.eyeY, easedProgress)
            )
            
            anim.elapsed += deltaTimeMs
            if (anim.elapsed >= anim.duration) {
                currentAnimation = null
            }
        } ?: run {
            // Smooth approach to target when no animation
            state = state.copy(
                zoom = approach(state.zoom, target.zoom, 0.05f),
                panX = approach(state.panX, target.panX, 0.02f),
                panY = approach(state.panY, target.panY, 0.02f),
                tilt = approach(state.tilt, target.tilt, 0.5f),
                headRotationX = approach(state.headRotationX, target.headRotationX, 0.05f),
                headRotationY = approach(state.headRotationY, target.headRotationY, 0.05f),
                eyeX = approach(state.eyeX, target.eyeX, 0.08f),
                eyeY = approach(state.eyeY, target.eyeY, 0.08f)
            )
        }
    }

    private fun updateState() {
        state = state.copy(
            panX = target.panX,
            panY = target.panY,
            tilt = target.tilt
        )
    }

    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t
    }

    private fun approach(current: Float, target: Float, speed: Float): Float {
        return if (abs(target - current) < 0.001f) {
            target
        } else {
            current + (target - current) * speed
        }
    }

    private fun easeInOutCubic(t: Float): Float {
        return if (t < 0.5f) {
            4 * t * t * t
        } else {
            1 - (-2 * t + 2).pow(3) / 2
        }
    }

    private fun Float.pow(n: Float): Float {
        var result = 1f
        repeat(n.toInt()) { result *= this }
        return result
    }
}

/**
 * Camera animation types.
 */
sealed class CameraAnimation {
    abstract val duration: Long
    abstract var elapsed: Long
    abstract fun complete(): Boolean
}

data class InterpolateAnimation(
    val from: CameraState,
    val to: CameraTarget,
    override val duration: Long,
    override var elapsed: Long = 0L
) : CameraAnimation() {
    override fun complete() = elapsed >= duration
}

data class ShakeAnimation(
    val intensity: Float,
    override val duration: Long,
    override var elapsed: Long = 0L,
    val onComplete: () -> Unit
) : CameraAnimation() {
    override fun complete(): Boolean {
        if (elapsed >= duration) {
            onComplete()
            return true
        }
        return false
    }
}
