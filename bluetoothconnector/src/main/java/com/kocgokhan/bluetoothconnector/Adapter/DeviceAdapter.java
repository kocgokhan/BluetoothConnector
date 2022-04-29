package com.kocgokhan.bluetoothconnector.Adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kocgokhan.bluetoothconnector.BlueTooth.BluetoothController;
import com.kocgokhan.bluetoothconnector.BlueTooth.BluetoothDiscoveryDeviceListener;
import com.kocgokhan.bluetoothconnector.BlueTooth.View.ListInteractionListener;
import com.kocgokhan.bluetoothconnector.BlueTooth.View.RecyclerViewClickListener;
import com.kocgokhan.bluetoothconnector.R;
import com.kocgokhan.bluetoothconnector.callAdapterProccess;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter
        extends RecyclerView.Adapter<DeviceAdapter.ViewHolder>
        implements BluetoothDiscoveryDeviceListener {

    /**
     * The devices shown in this {@link RecyclerView}.
     */
    private final List<BluetoothDevice> devices;

    /**
     * Callback for handling interaction events.
     */
    private final ListInteractionListener<BluetoothDevice> listener;

    /**
     * Controller for Bluetooth functionalities.
     */
    private BluetoothController bluetooth;

    private Context context;



    /**
     * Instantiates a new DeviceRecyclerViewAdapter.
     *
     * @param listener an handler for interaction events.
     */
    public DeviceAdapter(ListInteractionListener<BluetoothDevice> listener ) {
        this.devices = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_device_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = devices.get(position);
        holder.mDeviceNameView.setText(devices.get(position).getName());
        holder.mDeviceAddressView.setText(devices.get(position).getAddress());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    listener.onItemClick(holder.mItem);

                }
            }
        });
    }





    /**
     * Returns the icon shown on the left of the device inside the list.
     *
     * @param device the device for the icon to get.
     * @return a resource drawable id for the device icon.
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return devices.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceDiscovered(BluetoothDevice device) {
        listener.endLoading(true);
        devices.add(device);
        notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceDiscoveryStarted() {
        cleanView();
        listener.startLoading();
    }

    /**
     * Cleans the view.
     */
    public void cleanView() {
        devices.clear();
        notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBluetoothController(BluetoothController bluetooth) {
        this.bluetooth = bluetooth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceDiscoveryEnd() {
        listener.endLoading(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBluetoothStatusChanged() {
        // Notifies the Bluetooth controller.
        bluetooth.onBluetoothStatusChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBluetoothTurningOn() {
        listener.startLoading();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDevicePairingEnded() {
        if (bluetooth.isPairingInProgress()) {
            BluetoothDevice device = bluetooth.getBoundingDevice();
            switch (bluetooth.getPairingDeviceStatus()) {
                case BluetoothDevice.BOND_BONDING:
                    // Still pairing, do nothing.
                    break;
                case BluetoothDevice.BOND_BONDED:
                    // Successfully paired.
                    listener.endLoadingWithDialog(false, device);

                    // Updates the icon for this element.
                    notifyDataSetChanged();
                    break;
                case BluetoothDevice.BOND_NONE:
                    // Failed pairing.
                    listener.endLoadingWithDialog(true, device);
                    break;
            }
        }
    }

    /**
     * ViewHolder for a BluetoothDevice.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * The inflated view of this ViewHolder.
         */
        final View mView;

        /**
         * The icon of the device.
         */

        /**
         * The name of the device.
         */
        final TextView mDeviceNameView;

        /**
         * The MAC address of the BluetoothDevice.
         */
        final TextView mDeviceAddressView;


        /**
         * The item of this ViewHolder.
         */
        BluetoothDevice mItem;

        /**
         * Instantiates a new ViewHolder.
         *
         * @param view the inflated view of this ViewHolder.
         */
        ViewHolder(View view) {
            super(view);
            mView = view;
            mDeviceNameView = (TextView) view.findViewById(R.id.name_of_device);
            mDeviceAddressView = (TextView) view.findViewById(R.id.ssiID_of_device);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return super.toString() + " '" + BluetoothController.deviceToString(mItem) + "'";
        }
    }
}

