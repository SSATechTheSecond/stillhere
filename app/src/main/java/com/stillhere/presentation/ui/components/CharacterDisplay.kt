package com.stillhere.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.stillhere.domain.model.EchoAnimationState
import com.stillhere.domain.model.EchoMood
import com.stillhere.domain.model.TouchReaction
import kotlin.math.roundToInt

/**
 * Animated Echo character display.
 * A living, breathing companion character.
 */
@Composable
fun CharacterDisplay(
    mood: EchoMood,
    animationState: EchoAnimationState,
    onTouch: (TouchReaction) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "echo_anim")
    
    // Breathing animation
    val breathOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // Bounce for excited state
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Scale animation for touch
    val scale by animateFloatAsState(
        targetValue = when (animationState) {
            EchoAnimationState.REACTING -> 1.1f
            else -> 1f
        },
        animationSpec = tween(150),
        label = "scale"
    )

    val yOffset = when (animationState) {
        EchoAnimationState.IDLE -> breathOffset * 5
        EchoAnimationState.EXCITED, EchoAnimationState.CELEBRATING -> bounceOffset
        EchoAnimationState.SLEEPING -> 0f
        else -> 0f
    }

    val density = LocalDensity.current

    Box(
        modifier = modifier
            .size(200.dp)
            .offset { IntOffset(0, with(density) { yOffset.roundToInt() }) }
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onTouch(TouchReaction.POKE) },
                    onLongPress = { onTouch(TouchReaction.PET) }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        EchoCharacter(
            mood = mood,
            animationState = animationState
        )
    }
}

@Composable
private fun EchoCharacter(
    mood: EchoMood,
    animationState: EchoAnimationState
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    
    // Eye blink animation
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    val eyeOpenness = when (animationState) {
        EchoAnimationState.SLEEPING -> 0f
        EchoAnimationState.SURPRISED -> 1.2f
        else -> blinkAlpha
    }

    // Blush animation
    val blushAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blush"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        getEchoColor(mood),
                        getEchoColor(mood).copy(alpha = 0.7f)
                    )
                )
            )
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Draw Echo body/head
        drawCircle(
            color = Color.White,
            radius = size.minDimension / 2.2f,
            center = Offset(centerX, centerY)
        )

        // Draw eyes
        drawEyes(centerX, centerY, mood, eyeOpenness)

        // Draw blush for certain moods
        if (mood in listOf(EchoMood.HAPPY, EchoMood.LOVING, EchoMood.PLAYFUL, EchoMood.EMBARRASSED)) {
            drawCircle(
                color = Color(0xFFFF69B4).copy(alpha = blushAlpha),
                radius = size.minDimension / 12f,
                center = Offset(centerX - size.width / 4, centerY + size.height / 8)
            )
            drawCircle(
                color = Color(0xFFFF69B4).copy(alpha = blushAlpha),
                radius = size.minDimension / 12f,
                center = Offset(centerX + size.width / 4, centerY + size.height / 8)
            )
        }

        // Draw mouth
        drawMouth(centerX, centerY, mood)
    }
}

private fun DrawScope.drawEyes(
    centerX: Float,
    centerY: Float,
    mood: EchoMood,
    openness: Float
) {
    val eyeRadius = size.minDimension / 20f
    val eyeY = centerY - size.height / 12
    val eyeSpacing = size.width / 6

    val eyeColor = when (mood) {
        EchoMood.SAD -> Color(0xFF4A90D9)
        EchoMood.ANGRY -> Color(0xFFE74C3C)
        EchoMood.LOVING -> Color(0xFFFF69B4)
        EchoMood.EXCITED -> Color(0xFFFFD700)
        else -> Color(0xFF7B68EE)  // Purple for Echo
    }

    // Eye whites
    drawCircle(Color.White, eyeRadius * 1.5f * openness, Offset(centerX - eyeSpacing, eyeY))
    drawCircle(Color.White, eyeRadius * 1.5f * openness, Offset(centerX + eyeSpacing, eyeY))

    // Irises
    drawCircle(eyeColor, eyeRadius * openness, Offset(centerX - eyeSpacing, eyeY))
    drawCircle(eyeColor, eyeRadius * openness, Offset(centerX + eyeSpacing, eyeY))

    // Pupils
    val pupilRadius = eyeRadius / 2
    drawCircle(Color.Black, pupilRadius * openness, Offset(centerX - eyeSpacing, eyeY))
    drawCircle(Color.Black, pupilRadius * openness, Offset(centerX + eyeSpacing, eyeY))
}

private fun DrawScope.drawMouth(
    centerX: Float,
    centerY: Float,
    mood: EchoMood
) {
    val mouthPath = Path()
    val mouthY = centerY + size.height / 8

    when (mood) {
        EchoMood.HAPPY, EchoMood.EXCITED, EchoMood.PLAYFUL -> {
            mouthPath.moveTo(centerX - 20f, mouthY)
            mouthPath.quadraticBezierTo(centerX, mouthY + 25f, centerX + 20f, mouthY)
        }
        EchoMood.SAD, EchoMood.BORED -> {
            mouthPath.moveTo(centerX - 15f, mouthY + 10f)
            mouthPath.quadraticBezierTo(centerX, mouthY - 5f, centerX + 15f, mouthY + 10f)
        }
        EchoMood.LOVING, EchoMood.CARING -> {
            mouthPath.moveTo(centerX - 12f, mouthY + 5f)
            mouthPath.quadraticBezierTo(centerX, mouthY + 12f, centerX + 12f, mouthY + 5f)
        }
        EchoMood.SURPRISED -> {
            drawCircle(Color(0xFF8B0000), 8f, Offset(centerX, mouthY + 8f))
            return
        }
        EchoMood.ANGRY -> {
            mouthPath.moveTo(centerX - 15f, mouthY + 8f)
            mouthPath.lineTo(centerX + 15f, mouthY + 8f)
        }
        else -> {
            mouthPath.moveTo(centerX - 10f, mouthY + 5f)
            mouthPath.quadraticBezierTo(centerX, mouthY + 10f, centerX + 10f, mouthY + 5f)
        }
    }

    drawPath(path = mouthPath, color = Color(0xFF8B0000), alpha = 0.8f)
}

private fun getEchoColor(mood: EchoMood): Color {
    return when (mood) {
        EchoMood.HAPPY, EchoMood.EXCITED -> Color(0xFFFF69B4)  // Hot pink
        EchoMood.SAD, EchoMood.SLEEPY -> Color(0xFF87CEEB)     // Sky blue
        EchoMood.LOVING, EchoMood.CARING -> Color(0xFFFFB6C1)  // Light pink
        EchoMood.ANGRY -> Color(0xFFFF6B6B)                    // Red
        EchoMood.SURPRISED -> Color(0xFF7B68EE)                 // Purple
        EchoMood.PLAYFUL -> Color(0xFFDDA0DD)                   // Plum
        EchoMood.EMBARRASSED -> Color(0xFFFFB6C1)
        else -> Color(0xFF7B68EE)                               // Echo's base purple
    }
}
