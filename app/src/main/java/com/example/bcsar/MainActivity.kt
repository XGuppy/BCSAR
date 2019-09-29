package com.example.bcsar

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {
    private var state = true
    private var mapOfDevices: MutableMap<String, BluetoothDevice> = mutableMapOf()
    private lateinit var listOfNameDevices: ArrayAdapter<String>
    private var btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private lateinit var spinChooseDevice: Spinner
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    mapOfDevices[device.name] = device
                    listOfNameDevices.add(device.name)
                }
            }
        }
    }
    private  val localReceiver = LocalBroadcastManager.getInstance(this).registerReceiver(object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val connectData = intent.getStringExtra("connect")
            if(connectData == "break")
            {
                buttonStartService.isEnabled = false
                spinChooseDevice.isEnabled = true
                stopService(Intent(this@MainActivity, SensorService::class.java))
            }
        }
    }, IntentFilter("serviceEvent"))
    private lateinit var buttonStartService: Button

    private fun requestPermission()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_COARSE_LOCATION
            )
        }
    }
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission()
        setContentView(R.layout.activity_main)
        listOfNameDevices = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        listOfNameDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinChooseDevice = findViewById(R.id.choose_device)
        spinChooseDevice.adapter = listOfNameDevices
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

        spinChooseDevice.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                buttonStartService.isEnabled = true
                if(btAdapter!!.isDiscovering)
                {
                    btAdapter?.cancelDiscovery()
                }
            }

        }
        buttonStartService = findViewById(R.id.buttonService)
        buttonStartService.setOnClickListener {

            if (state) {
                spinChooseDevice.isEnabled = false
                val dev = mapOfDevices[spinChooseDevice.selectedItem.toString()]
                while (dev?.bondState != BluetoothDevice.BOND_BONDED)
                {
                    dev?.createBond()
                }
                val sp  = PreferenceManager.getDefaultSharedPreferences(this)
                startService(Intent(this, SensorService::class.java)
                    .putExtra("device", mapOfDevices[spinChooseDevice.selectedItem.toString()])
                    .putExtra("inversX", sp.getBoolean("inversX", false))
                    .putExtra("inversY", sp.getBoolean("inversY", false))
                    .putExtra("mode", sp.getInt("modes", 0)))

            }
            else {
                spinChooseDevice.isEnabled = true
                stopService(Intent(this, SensorService::class.java))
            }
            state = !state
        }
        buttonStartService.isEnabled = false

    }

    override fun onResume() {
        super.onResume()
        Log.i("RESUME", listOfNameDevices.count.toString())
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
        btAdapter?.startDiscovery()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
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

    companion object {
        private const val REQUEST_COARSE_LOCATION: Int = 135
    }
}
