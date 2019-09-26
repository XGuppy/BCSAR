package com.example.bcsar

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val REQUEST_COARSE_LOCATION: Int = 135
    private lateinit var mSensorManager: SensorManager
    private var mLinearAcceleration: Sensor? = null
    private var mGyroscope: Sensor? = null
    private var state = true
    private var mapOfDevices: MutableMap<String, BluetoothDevice> = mutableMapOf()
    private lateinit var listOfNameDevices: ArrayAdapter<String>
    private var btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            Log.i("TAG", listOfNameDevices.count.toString())
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    Log.i("ACTION", listOfNameDevices.count.toString())
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    mapOfDevices[device.name] = device
                    listOfNameDevices.add(device.name)
                }
            }
        }
    }
    fun requestPermission()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_COARSE_LOCATION)
        }
    }
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission()
        setContentView(R.layout.activity_main)
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        listOfNameDevices = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        listOfNameDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spin = findViewById<Spinner>(R.id.choose_device)
        spin.adapter = listOfNameDevices
        if(btAdapter != null)
        {
            if (btAdapter?.isEnabled == false)
            {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1)
            }
            val pairedDevices: Set<BluetoothDevice>? = btAdapter?.bondedDevices
            pairedDevices?.forEach { device ->
                mapOfDevices[device.name] = device
                listOfNameDevices.add(device.name)
            }

        }

        spin.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(btAdapter!!.isDiscovering)
                {
                    btAdapter?.cancelDiscovery()
                }


            }

        }
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
        }
        else if(event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION)
        {
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Empty
    }

    override fun onResume() {
        super.onResume()
        Log.i("RESUME", listOfNameDevices.count.toString())
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
        mSensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        btAdapter?.startDiscovery()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode)
        {
            REQUEST_COARSE_LOCATION -> {
                if (grantResults.count() > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    requestPermission()
                }
            }
        }
    }
}
