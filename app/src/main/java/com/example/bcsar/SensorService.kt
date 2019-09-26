package com.example.bcsar

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import java.util.*

class SensorService: Service(), SensorEventListener {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private lateinit var mSensorManager: SensorManager
    private var mLinearAcceleration: Sensor? = null
    private var mGyroscope: Sensor? = null
    private lateinit var btSocket: BluetoothSocket
    override fun onCreate() {
        super.onCreate()
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        mSensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    @ExperimentalStdlibApi
    override fun onSensorChanged(event: SensorEvent?) {
        if(event == null) {
            return
        }

        if(event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            btSocket.outputStream.write("g:${event.values[0]}:{event.values[1]}:${event.values[2]}".encodeToByteArray())
        }
        else if(event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            btSocket.outputStream.write("a:${event.values[0]}:{event.values[1]}:${event.values[2]}".encodeToByteArray())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        btSocket = (intent?.extras?.get("device") as BluetoothDevice).createInsecureRfcommSocketToServiceRecord(
            UUID.randomUUID())
        btSocket.connect()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mSensorManager.unregisterListener(this)
        btSocket.close()
        super.onDestroy()
    }
}