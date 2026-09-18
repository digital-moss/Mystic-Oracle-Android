package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.DeckManager
import kotlin.math.sqrt

class ShakeDetector(
    context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vm.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private var lastShakeTime = 0L

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

        // Determine threshold based on sensitivity
        val threshold = when (DeckManager.shakeSensitivity) {
            "Low" -> 3.2f
            "High" -> 2.0f
            else -> 2.5f // Medium
        }

        if (gForce > threshold) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > 1000) { // Cooldown 1 second
                lastShakeTime = now
                if (DeckManager.hapticsEnabled) {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(100)
                        }
                    } catch (_: Exception) {}
                }
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
