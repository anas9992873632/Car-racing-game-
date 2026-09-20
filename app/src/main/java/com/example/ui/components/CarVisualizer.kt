package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.CarDef

@Composable
fun CarPreviewCanvas(
    carDef: CarDef,
    overrideColorHex: Long? = null,
    modifier: Modifier = Modifier,
    isNitroActive: Boolean = false,
    tiltAngle: Float = 0f,
    viewFromRear: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "car_anim")
    val flamePulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(80, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_pulse"
    )

    val bodyColor = Color(overrideColorHex ?: carDef.primaryColor)
    val secondaryColor = Color(carDef.secondaryColor)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (viewFromRear) {
                drawRearCar(
                    w = w,
                    h = h,
                    bodyColor = bodyColor,
                    secondaryColor = secondaryColor,
                    carDef = carDef,
                    tilt = tiltAngle,
                    isNitro = isNitroActive,
                    flamePulse = flamePulse
                )
            } else {
                drawTopDownCar(
                    w = w,
                    h = h,
                    bodyColor = bodyColor,
                    secondaryColor = secondaryColor,
                    carDef = carDef
                )
            }
        }
    }
}

fun DrawScope.drawRearCar(
    w: Float,
    h: Float,
    bodyColor: Color,
    secondaryColor: Color,
    carDef: CarDef,
    tilt: Float,
    isNitro: Boolean,
    flamePulse: Float
) {
    val cx = w / 2f + (tilt * 2.5f)
    val cy = h / 2f

    // Car Shadow on road
    drawOval(
        color = Color(0x66000000),
        topLeft = Offset(cx - w * 0.38f, cy + h * 0.28f),
        size = Size(w * 0.76f, h * 0.18f)
    )

    // Wide Racing Wheels (Left & Right)
    val wheelW = w * 0.16f
    val wheelH = h * 0.38f
    val wheelY = cy + h * 0.02f

    // Left Wheel
    drawRoundRect(
        color = Color(0xFF111827),
        topLeft = Offset(cx - w * 0.44f, wheelY),
        size = Size(wheelW, wheelH),
        cornerRadius = CornerRadius(8f, 8f)
    )
    // Left Wheel Rim
    drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(cx - w * 0.42f, wheelY + 8f),
        size = Size(wheelW * 0.75f, wheelH - 16f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Right Wheel
    drawRoundRect(
        color = Color(0xFF111827),
        topLeft = Offset(cx + w * 0.28f, wheelY),
        size = Size(wheelW, wheelH),
        cornerRadius = CornerRadius(8f, 8f)
    )
    // Right Wheel Rim
    drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(cx + w * 0.30f, wheelY + 8f),
        size = Size(wheelW * 0.75f, wheelH - 16f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Lower Diffuser & Carbon Skirts
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(cx - w * 0.34f, cy + h * 0.16f),
        size = Size(w * 0.68f, h * 0.18f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Dual Exhaust Pipes & Nitro Flames
    val leftExhaustX = cx - w * 0.18f
    val rightExhaustX = cx + w * 0.18f
    val exhaustY = cy + h * 0.28f

    // Left pipe rim
    drawCircle(Color(0xFF334155), radius = 10f, center = Offset(leftExhaustX, exhaustY))
    drawCircle(Color(0xFF020617), radius = 7f, center = Offset(leftExhaustX, exhaustY))

    // Right pipe rim
    drawCircle(Color(0xFF334155), radius = 10f, center = Offset(rightExhaustX, exhaustY))
    drawCircle(Color(0xFF020617), radius = 7f, center = Offset(rightExhaustX, exhaustY))

    // NITRO FLAMES
    if (isNitro) {
        val flameLen = 45f * flamePulse
        // Left flame
        drawOval(
            brush = Brush.radialGradient(listOf(Color(0xFF67E8F9), Color(0xFF06B6D4), Color(0x0006B6D4))),
            topLeft = Offset(leftExhaustX - 16f, exhaustY + 2f),
            size = Size(32f, flameLen)
        )
        // Right flame
        drawOval(
            brush = Brush.radialGradient(listOf(Color(0xFF67E8F9), Color(0xFF06B6D4), Color(0x0006B6D4))),
            topLeft = Offset(rightExhaustX - 16f, exhaustY + 2f),
            size = Size(32f, flameLen)
        )
    }

    // Main Bumper & Body Shell
    val bodyPath = Path().apply {
        moveTo(cx - w * 0.36f, cy + h * 0.20f)
        lineTo(cx - w * 0.34f, cy - h * 0.05f)
        lineTo(cx - w * 0.24f, cy - h * 0.25f)
        lineTo(cx + w * 0.24f, cy - h * 0.25f)
        lineTo(cx + w * 0.34f, cy - h * 0.05f)
        lineTo(cx + w * 0.36f, cy + h * 0.20f)
        close()
    }
    drawPath(bodyPath, color = bodyColor)

    // Body Gradient / Highlights
    drawPath(
        bodyPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.25f), Color.Transparent, Color.Black.copy(alpha = 0.35f)),
            startY = cy - h * 0.25f,
            endY = cy + h * 0.20f
        )
    )

    // Rear Windshield (Cabin Glass)
    val glassPath = Path().apply {
        moveTo(cx - w * 0.22f, cy - h * 0.06f)
        lineTo(cx - w * 0.17f, cy - h * 0.23f)
        lineTo(cx + w * 0.17f, cy - h * 0.23f)
        lineTo(cx + w * 0.22f, cy - h * 0.06f)
        close()
    }
    drawPath(
        glassPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
        )
    )
    // Glass reflection glare line
    drawLine(
        color = Color.White.copy(alpha = 0.3f),
        start = Offset(cx - w * 0.12f, cy - h * 0.20f),
        end = Offset(cx - w * 0.06f, cy - h * 0.08f),
        strokeWidth = 3f
    )

    // Red LED Taillights (Cyber bar or dual pods)
    val tailLightY = cy + h * 0.02f
    drawRoundRect(
        color = Color(0xFFFF1744),
        topLeft = Offset(cx - w * 0.32f, tailLightY),
        size = Size(w * 0.22f, h * 0.055f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = Color(0xFFFF1744),
        topLeft = Offset(cx + w * 0.10f, tailLightY),
        size = Size(w * 0.22f, h * 0.055f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // High-mount neon light strip
    drawRoundRect(
        color = Color(0xFFFF5252),
        topLeft = Offset(cx - w * 0.08f, cy - h * 0.06f),
        size = Size(w * 0.16f, h * 0.02f),
        cornerRadius = CornerRadius(2f, 2f)
    )

    // Rear Aero Wing / Spoiler
    if (carDef.bodyStyle != "truck") {
        val spoilerY = cy - h * 0.15f
        // Stanchions (Pillars)
        drawLine(Color(0xFF0F172A), Offset(cx - w * 0.18f, spoilerY), Offset(cx - w * 0.18f, cy - h * 0.03f), strokeWidth = 5f)
        drawLine(Color(0xFF0F172A), Offset(cx + w * 0.18f, spoilerY), Offset(cx + w * 0.18f, cy - h * 0.03f), strokeWidth = 5f)
        // Wing blade
        drawRoundRect(
            color = Color(0xFF020617),
            topLeft = Offset(cx - w * 0.34f, spoilerY - 4f),
            size = Size(w * 0.68f, h * 0.05f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        // Wing top color accent
        drawRoundRect(
            color = secondaryColor,
            topLeft = Offset(cx - w * 0.30f, spoilerY - 3f),
            size = Size(w * 0.60f, 3f),
            cornerRadius = CornerRadius(1f, 1f)
        )
    }

    // License Plate
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(cx - w * 0.11f, cy + h * 0.10f),
        size = Size(w * 0.22f, h * 0.06f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    drawRoundRect(
        color = Color(0xFFFACC15),
        topLeft = Offset(cx - w * 0.09f, cy + h * 0.11f),
        size = Size(w * 0.18f, h * 0.04f),
        cornerRadius = CornerRadius(2f, 2f)
    )
}

private fun DrawScope.drawTopDownCar(
    w: Float,
    h: Float,
    bodyColor: Color,
    secondaryColor: Color,
    carDef: CarDef
) {
    val cx = w / 2f
    val cy = h / 2f

    // Shadow
    drawOval(
        color = Color(0x55000000),
        topLeft = Offset(cx - w * 0.32f, cy - h * 0.44f),
        size = Size(w * 0.64f, h * 0.88f)
    )

    // 4 Wheels
    val wheelW = w * 0.12f
    val wheelH = h * 0.20f
    drawRoundRect(Color(0xFF1E293B), Offset(cx - w * 0.38f, cy - h * 0.38f), Size(wheelW, wheelH), CornerRadius(4f, 4f))
    drawRoundRect(Color(0xFF1E293B), Offset(cx + w * 0.26f, cy - h * 0.38f), Size(wheelW, wheelH), CornerRadius(4f, 4f))
    drawRoundRect(Color(0xFF1E293B), Offset(cx - w * 0.38f, cy + h * 0.18f), Size(wheelW, wheelH), CornerRadius(4f, 4f))
    drawRoundRect(Color(0xFF1E293B), Offset(cx + w * 0.26f, cy + h * 0.18f), Size(wheelW, wheelH), CornerRadius(4f, 4f))

    // Car Body
    val bodyPath = Path().apply {
        moveTo(cx - w * 0.22f, cy - h * 0.42f)
        lineTo(cx + w * 0.22f, cy - h * 0.42f)
        lineTo(cx + w * 0.28f, cy - h * 0.15f)
        lineTo(cx + w * 0.30f, cy + h * 0.32f)
        lineTo(cx + w * 0.24f, cy + h * 0.42f)
        lineTo(cx - w * 0.24f, cy + h * 0.42f)
        lineTo(cx - w * 0.30f, cy + h * 0.32f)
        lineTo(cx - w * 0.28f, cy - h * 0.15f)
        close()
    }
    drawPath(bodyPath, color = bodyColor)

    // Racing Stripe
    drawLine(
        color = secondaryColor,
        start = Offset(cx, cy - h * 0.42f),
        end = Offset(cx, cy + h * 0.42f),
        strokeWidth = w * 0.09f
    )

    // Cockpit Roof & Windshields
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(cx - w * 0.18f, cy - h * 0.18f),
        size = Size(w * 0.36f, h * 0.36f),
        cornerRadius = CornerRadius(10f, 10f)
    )
    // Windshield (Front glass)
    drawRoundRect(
        color = Color(0xFF38BDF8),
        topLeft = Offset(cx - w * 0.15f, cy - h * 0.15f),
        size = Size(w * 0.30f, h * 0.08f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Headlights (Front LED beams)
    drawOval(Color(0xFFFEF08A), Offset(cx - w * 0.20f, cy - h * 0.41f), Size(w * 0.08f, h * 0.04f))
    drawOval(Color(0xFFFEF08A), Offset(cx + w * 0.12f, cy - h * 0.41f), Size(w * 0.08f, h * 0.04f))
}
