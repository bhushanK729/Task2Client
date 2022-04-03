package com.example.aidlserver

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ROTATION_VECTOR
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import android.os.*
import android.widget.Toast

class OrientationService : Service(), SensorEventListener {

    var orientationData = IntArray(2) { _ -> 0 }

    var clientCounter = 0

    val SENSOR_DELAY = 8 // 8 ms

    private lateinit var rotationSensor: Sensor

    private val FROM_RADS_TO_DEGS = -57

    override fun onBind(intent: Intent?): IBinder {
        synchronized(this) {
            clientCounter++
            if (clientCounter == 1) {
                // Start listening to events
                startListening()
            }
        }

        return aidlBinder
    }

    private val aidlBinder = object : IOrientationService.Stub() {
        override fun getOrientation(): IntArray {
            return orientationData
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        synchronized(this) {
            clientCounter--
            if (clientCounter == 0) {
                // Stop listening to events
                stopListening()
            }
        }
        return super.onUnbind(intent)
    }

    private fun startListening() {
        try {
            val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            rotationSensor = sensorManager.getDefaultSensor(TYPE_ROTATION_VECTOR)
            sensorManager.registerListener(this, rotationSensor, SENSOR_DELAY)
        } catch (e: Exception) {
            Toast.makeText(this, "startListening() Hardware compatibility issue", Toast.LENGTH_LONG)
                .show()
            Log.e("Swapnil", "startListening() Hardware compatibility issue")
        }
    }

    private fun stopListening() {
        try {
            val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            rotationSensor = sensorManager.getDefaultSensor(TYPE_ROTATION_VECTOR)
            sensorManager.unregisterListener(this, rotationSensor)
        } catch (e: Exception) {
            Toast.makeText(this, "stopListening() Hardware compatibility issue", Toast.LENGTH_LONG)
                .show()
            Log.e("Swapnil", "stopListening() Hardware compatibility issue")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == rotationSensor) {
            if (event.values.size > 4) {
                val truncatedRotationVector = FloatArray(4)//Array(4){0.0f}}
                event.values.copyInto(truncatedRotationVector, 0, 0, 4)
                update(truncatedRotationVector)
            } else {
                update(event.values)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun update(vectors: FloatArray) {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors)
        val worldAxisX = SensorManager.AXIS_X
        val worldAxisZ = SensorManager.AXIS_Z
        val adjustedRotationMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            worldAxisX,
            worldAxisZ,
            adjustedRotationMatrix
        )
        val orientation = FloatArray(3)
        SensorManager.getOrientation(adjustedRotationMatrix, orientation)
        val pitch = orientation[1] * FROM_RADS_TO_DEGS
        val roll = orientation[2] * FROM_RADS_TO_DEGS
        orientationData[0] = pitch.toInt()
        orientationData[1] = roll.toInt()
    }
}