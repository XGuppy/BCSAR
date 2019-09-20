package com.example.bcsar

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private var mLinearAcceleration: Sensor? = null
    private var mGyroscope: Sensor? = null
    private lateinit var mTextGyro: TextView
    private lateinit var mTextAccel: TextView
    private var state = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        mTextGyro = findViewById(R.id.Test)
        mTextAccel = findViewById(R.id.Test2)

        findViewById<Button>(R.id.buttonService).setOnClickListener {
            if (state) {
                Log.i("TAG", "START")
                startService(Intent(this, SensorService::class.java))
            }
            else {
                Log.i("TAG", "STOP")
                stopService(Intent(this, SensorService::class.java))
            }
            state = !state
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        if(event == null)
        {
            return
        }

        if(event.sensor.type == Sensor.TYPE_GYROSCOPE)
        {
            mTextGyro.text = "${event.values[0]} ${event.values[1]} ${event.values[2]}"
        }
        else if(event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION)
        {
            mTextAccel.text = "${event.values[0]} ${event.values[1]} ${event.values[2]}"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Empty
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.settingsAction)
        {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        return true
    }
}
