package com.kocgokhan.bluetoothconnector

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kocgokhan.bluetoothconnector.Adapter.DeviceAdapter
import com.kocgokhan.bluetoothconnector.BlueTooth.BluetoothController
import com.kocgokhan.bluetoothconnector.BlueTooth.View.ListInteractionListener
import com.kocgokhan.bluetoothconnector.BlueTooth.View.RecyclerViewProgressEmptySupport
import kotlinx.android.synthetic.main.list_device.view.*

class BluetoothConnector @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr), ListInteractionListener<BluetoothDevice> {

    private  var bluetooth : BluetoothController
    private  var deviceAdapter : DeviceAdapter
    private var recyclerView : RecyclerViewProgressEmptySupport
    var buttonTapped: (() -> Unit)? = null
    private lateinit var bondingProgressDialog: ProgressDialog
    private var progressBar : ProgressBar



    init {
        val view = LayoutInflater.from(context).inflate(R.layout.list_device, this, false)
        val set = ConstraintSet()
        addView(view)
        set.clone(this)
        set.match(view, this)


        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                (context as Activity?)!!, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 2
            )
        }

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                (context as Activity?)!!, arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT
                ), 2
            )
        }

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                (context as Activity?)!!, arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN
                ), 2
            )
        }


        deviceAdapter  = DeviceAdapter(this)
        recyclerView = view.findViewById(R.id.device_item_recy)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL,false)
        val emptyView = findViewById<View>(R.id.empty_list)
        recyclerView.setEmptyView(emptyView)
        progressBar = view.findViewById(R.id.progressBar)
        recyclerView.setProgressView(progressBar)
        recyclerView.adapter = deviceAdapter
        recyclerView.adapter!!.notifyDataSetChanged()


        this.bluetooth =  BluetoothController(context, BluetoothAdapter.getDefaultAdapter(), deviceAdapter)

        button.setOnClickListener {
            buttonTapped?.invoke()
            // If the bluetooth is not enabled, turns it on.
            // If the bluetooth is not enabled, turns it on.
            if (!bluetooth.isBluetoothEnabled) {
                Snackbar.make(view, R.string.enabling_bluetooth, Snackbar.LENGTH_SHORT).show()
                bluetooth.turnOnBluetoothAndScheduleDiscovery()
            } else {
                //Prevents the user from spamming the button and thus glitching the UI.
                if (!bluetooth.isDiscovering) {
                    // Starts the discovery.
                    Snackbar.make(view, R.string.device_discovery_started, Snackbar.LENGTH_SHORT)
                        .show()
                    bluetooth.startDiscovery()
                } else {
                    Snackbar.make(view, R.string.device_discovery_stopped, Snackbar.LENGTH_SHORT)
                        .show()
                    bluetooth.cancelDiscovery()
                }
            }


        }
    }
    fun ConstraintSet.match(view: View, parentView: View) {
        this.connect(view.id, ConstraintSet.TOP, parentView.id, ConstraintSet.TOP)
        this.connect(view.id, ConstraintSet.START, parentView.id, ConstraintSet.START)
        this.connect(view.id, ConstraintSet.END, parentView.id, ConstraintSet.END)
        this.connect(view.id, ConstraintSet.BOTTOM, parentView.id, ConstraintSet.BOTTOM)
    }

    override fun onItemClick(device : BluetoothDevice) {
        if (bluetooth.isAlreadyPaired(device)) {
            //Toast.makeText(this, R.string.device_already_paired, Toast.LENGTH_SHORT).show()
            Snackbar.make(this, R.string.device_already_paired, Snackbar.LENGTH_SHORT)
                .show()
        } else {
            val outcome = bluetooth.pair(device)

            // Prints a message to the user.
            val deviceName = BluetoothController.getDeviceName(device)
            if (outcome) {
                bondingProgressDialog = ProgressDialog.show(
                    context, "",
                    "Pairing with device $deviceName...", true, false
                )
            } else {
                /*Toast.makeText(
                    this,
                    "Error while pairing with device $deviceName!",
                    Toast.LENGTH_SHORT
                ).show()*/
                Snackbar.make(this,"Error while pairing with device $deviceName!", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }


    override fun startLoading() {
        recyclerView.startLoading()
    }

    override fun endLoading(partialResults: Boolean) {
        recyclerView.endLoading()
    }

    override fun endLoadingWithDialog(error: Boolean, device: BluetoothDevice) {
        if (bondingProgressDialog != null) {
            val view = findViewById<View>(R.id.layoutConstrain)
            val message: String
            val deviceName = BluetoothController.getDeviceName(device)

            // Gets the message to print.
            message = if (error) {
                "Failed pairing with device $deviceName!"
            } else {
                "Succesfully paired with device $deviceName!"
            }

            // Dismisses the progress dialog and prints a message to the user.
            bondingProgressDialog.dismiss()
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()

            // Cleans up state.
            bondingProgressDialog.equals(null)
        }
    }
}