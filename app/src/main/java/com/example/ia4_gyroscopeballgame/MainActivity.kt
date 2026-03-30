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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BallMazeGame()
                }
            }
        }
    }
}

@Composable
fun BallMazeGame() {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    // Using accelerometer makes emulator testing easier.
    val sensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    var pitch by remember { mutableFloatStateOf(0f) }
    var roll by remember { mutableFloatStateOf(0f) }

    var ballX by remember { mutableFloatStateOf(60f) }
    var ballY by remember { mutableFloatStateOf(60f) }

    DisposableEffect(sensor) {
        if (sensor == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event == null) return
                    roll = event.values[0]
                    pitch = event.values[1]
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(
                listener,
                sensor,
                SensorManager.SENSOR_DELAY_GAME
            )

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
    ) {
        val screenWidth = size.width
        val screenHeight = size.height

        val wallThickness = 8f
        val ballRadius = 18f
        val speedFactor = 2.2f

        // Keep the starting point inside the playable area.
        if (ballX < wallThickness + ballRadius) {
            ballX = wallThickness + ballRadius + 8f
        }
        if (ballY < wallThickness + ballRadius) {
            ballY = wallThickness + ballRadius + 8f
        }

        val obstacles = listOf(
            Rect(
                left = screenWidth * 0.25f,
                top = screenHeight * 0.20f,
                right = screenWidth * 0.75f,
                bottom = screenHeight * 0.26f
            ),
            Rect(
                left = screenWidth * 0.15f,
                top = screenHeight * 0.45f,
                right = screenWidth * 0.48f,
                bottom = screenHeight * 0.51f
            ),
            Rect(
                left = screenWidth * 0.48f,
                top = screenHeight * 0.68f,
                right = screenWidth * 0.85f,
                bottom = screenHeight * 0.74f
            )
        )

        val goalRect = Rect(
            left = screenWidth * 0.82f,
            top = screenHeight * 0.82f,
            right = screenWidth * 0.95f,
            bottom = screenHeight * 0.95f
        )

        // Adjust signs if the direction feels reversed.
        val dx = roll * speedFactor
        val dy = -pitch * speedFactor

        // Try X movement first.
        val tryX = min(
            max(wallThickness + ballRadius, ballX + dx),
            screenWidth - wallThickness - ballRadius
        )

        val rectAfterX = Rect(
            left = tryX - ballRadius,
            top = ballY - ballRadius,
            right = tryX + ballRadius,
            bottom = ballY + ballRadius
        )

        val hitObstacleX = obstacles.any { it.overlaps(rectAfterX) }
        if (!hitObstacleX && ballX != tryX) {
            ballX = tryX
        }

        // Then try Y movement.
        val tryY = min(
            max(wallThickness + ballRadius, ballY + dy),
            screenHeight - wallThickness - ballRadius
        )

        val rectAfterY = Rect(
            left = ballX - ballRadius,
            top = tryY - ballRadius,
            right = ballX + ballRadius,
            bottom = tryY + ballRadius
        )

        val hitObstacleY = obstacles.any { it.overlaps(rectAfterY) }
        if (!hitObstacleY && ballY != tryY) {
            ballY = tryY
        }

        // Draw walls.
        drawRect(Color.Black, Offset(0f, 0f), Size(screenWidth, wallThickness))
        drawRect(Color.Black, Offset(0f, screenHeight - wallThickness), Size(screenWidth, wallThickness))
        drawRect(Color.Black, Offset(0f, 0f), Size(wallThickness, screenHeight))
        drawRect(Color.Black, Offset(screenWidth - wallThickness, 0f), Size(wallThickness, screenHeight))

        // Draw obstacles.
        obstacles.forEach { rect ->
            drawRect(
                color = Color.DarkGray,
                topLeft = Offset(rect.left, rect.top),
                size = Size(rect.width, rect.height)
            )
        }

        // Draw goal area.
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
}