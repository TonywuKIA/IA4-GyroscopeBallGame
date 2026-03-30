package com.example.ia4_gyroscopeballgame

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

/**
 * IA4 Q1 - Gyroscope/tilt-controlled ball game
 *
 * This app uses a device motion sensor to move a ball across the screen.
 * The player must avoid obstacles and reach the green goal area.
 *
 * Note:
 * For emulator testing, TYPE_ACCELEROMETER is usually more reliable than
 * TYPE_ROTATION_VECTOR. A physical Android device can also be used.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GyroBallGame()
                }
            }
        }
    }
}

@Composable
fun GyroBallGame() {
    val context = LocalContext.current

    // Access the device sensor service.
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    // Accelerometer is more reliable in the emulator for tilt-style testing.
    val motionSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    // Sensor values used to estimate tilt direction.
    var pitch by remember { mutableFloatStateOf(0f) }
    var roll by remember { mutableFloatStateOf(0f) }

    // Screen-dependent ball position will be initialized once inside Canvas.
    var ballX by remember { mutableFloatStateOf(-1f) }
    var ballY by remember { mutableFloatStateOf(-1f) }

    // Game state flags.
    var hasWon by remember { mutableStateOf(false) }
    var resetRequested by remember { mutableStateOf(false) }

    // Register and unregister the sensor listener safely.
    DisposableEffect(motionSensor) {
        if (motionSensor == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event == null) return

                    // event.values[0] -> left/right tilt
                    // event.values[1] -> forward/back tilt
                    roll = event.values[0]
                    pitch = event.values[1]
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(
                listener,
                motionSensor,
                SensorManager.SENSOR_DELAY_GAME
            )

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .safeDrawingPadding()
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenWidth = size.width
            val screenHeight = size.height

            val wallThickness = 8f
            val ballRadius = 24f
            val speedFactor = 3.5f

            // Initialize the ball only once at the upper-left playable area.
            if (ballX < 0f || ballY < 0f || resetRequested) {
                ballX = wallThickness + ballRadius + 12f
                ballY = wallThickness + ballRadius + 12f
                hasWon = false
                resetRequested = false
            }

            // Define obstacles using screen proportions.
            val obstacles = listOf(
                Rect(
                    left = screenWidth * 0.25f,
                    top = screenHeight * 0.18f,
                    right = screenWidth * 0.75f,
                    bottom = screenHeight * 0.25f
                ),
                Rect(
                    left = screenWidth * 0.15f,
                    top = screenHeight * 0.42f,
                    right = screenWidth * 0.48f,
                    bottom = screenHeight * 0.49f
                ),
                Rect(
                    left = screenWidth * 0.45f,
                    top = screenHeight * 0.67f,
                    right = screenWidth * 0.85f,
                    bottom = screenHeight * 0.74f
                )
            )

            // Define the goal area.
            val goalRect = Rect(
                left = screenWidth * 0.82f,
                top = screenHeight * 0.82f,
                right = screenWidth * 0.95f,
                bottom = screenHeight * 0.95f
            )

            // Convert sensor data into movement.
            // Adjust signs if direction feels reversed on your device.
            val dx = -roll * speedFactor
            val dy = pitch * speedFactor

            val targetX = ballX + dx
            val targetY = ballY + dy

            // Keep the ball inside the screen walls.
            val clampedX = min(
                max(wallThickness + ballRadius, targetX),
                screenWidth - wallThickness - ballRadius
            )
            val clampedY = min(
                max(wallThickness + ballRadius, targetY),
                screenHeight - wallThickness - ballRadius
            )

            // Predict the ball's next rectangle for collision checking.
            val nextBallRect = Rect(
                left = clampedX - ballRadius,
                top = clampedY - ballRadius,
                right = clampedX + ballRadius,
                bottom = clampedY + ballRadius
            )

            // Prevent movement through obstacles after winning state is reached.
            if (!hasWon) {
                val hitObstacle = obstacles.any { it.overlaps(nextBallRect) }

                if (!hitObstacle) {
                    ballX = clampedX
                    ballY = clampedY
                }
            }

            // Use the current actual ball position to check goal overlap.
            val currentBallRect = Rect(
                left = ballX - ballRadius,
                top = ballY - ballRadius,
                right = ballX + ballRadius,
                bottom = ballY + ballRadius
            )

            if (currentBallRect.overlaps(goalRect)) {
                hasWon = true
            }

            // Draw outer walls.
            drawRect(
                color = Color.Black,
                topLeft = Offset(0f, 0f),
                size = Size(screenWidth, wallThickness)
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(0f, screenHeight - wallThickness),
                size = Size(screenWidth, wallThickness)
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(0f, 0f),
                size = Size(wallThickness, screenHeight)
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(screenWidth - wallThickness, 0f),
                size = Size(wallThickness, screenHeight)
            )

            // Draw maze obstacles.
            obstacles.forEach { rect ->
                drawRect(
                    color = Color.DarkGray,
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height)
                )
            }

            // Draw the goal area.
            drawRect(
                color = Color(0xFF4CAF50),
                topLeft = Offset(goalRect.left, goalRect.top),
                size = Size(goalRect.width, goalRect.height)
            )

            // Draw the ball.
            drawCircle(
                color = Color.Red,
                radius = ballRadius,
                center = Offset(ballX, ballY)
            )
        }

        // Top message area.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (hasWon) "You Win!" else "Tilt the device to move the ball",
                style = MaterialTheme.typography.titleMedium,
                color = if (hasWon) Color(0xFF2E7D32) else Color.Black
            )
        }

        // Bottom reset button.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { resetRequested = true }
            ) {
                Text("Reset")
            }
        }
    }
}