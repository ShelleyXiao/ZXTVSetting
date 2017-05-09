package com.zx.zxtvsettings.adapter;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.Logger;
import com.zx.zxtvsettings.bluetooth.A2dpProfile;
import com.zx.zxtvsettings.bluetooth.CachedBluetoothDevice;
import com.zx.zxtvsettings.bluetooth.HeadsetProfile;
import com.zx.zxtvsettings.bluetooth.HidProfile;
import com.zx.zxtvsettings.bluetooth.LocalBluetoothProfile;
import com.zx.zxtvsettings.bluetooth.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ShaudXiao
 * Date: 2016-08-22
 * Time: 15:17
 * Company: zx
 * Description:
 * FIXME
 */

public class MyBluetoothAdapterNew extends BaseAdapter {

    private List<CachedBluetoothDevice> list = new ArrayList<>();
    private Context context;
    private  Holder holder;

    public MyBluetoothAdapterNew(Context context) {
        this.context = context;
    }

    public void addDats(List<CachedBluetoothDevice> datas) {
        list.clear();
        list.addAll(datas);
        notifyDataSetChanged();
    }

    public void addItem(CachedBluetoothDevice data) {
        list.add(data);
        notifyDataSetChanged();
    }

    public void clearDevice() {
        list.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.item_bluetooth, null);
            holder.name = (TextView) convertView
                    .findViewById(R.id.item_bluetooth_name);
            holder.icon = (ImageView) convertView
                    .findViewById(R.id.item_bluetooth_iv);
            holder.paied = (TextView) convertView.findViewById(R.id.item_bluetooth_pired);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        CachedBluetoothDevice device = list.get(position);
        holder.name.setText(device.getName());
        int iconResId = getBtClassDrawable(device);
        if (iconResId != 0) {
            holder.icon.setBackgroundResource(iconResId);
        }


        int sumaryResId = getConnectionSummary(device);
        if(sumaryResId != 0) {
            holder.paied.setText(getConnectionSummary(device));
        } else {
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                holder.paied.setVisibility(View.VISIBLE);
                holder.paied.setText(getConnectionSummary(device));
            } else {
                holder.paied.setVisibility(View.INVISIBLE);
            }
        }


        return convertView;
    }

    private int getBtClassDrawable(CachedBluetoothDevice device ) {
        BluetoothClass btClass = device.getBtClass();
        if (btClass != null) {
            switch (btClass.getMajorDeviceClass()) {
                case BluetoothClass.Device.Major.COMPUTER:
                    return R.drawable.ic_bt_laptop;

                case BluetoothClass.Device.Major.PHONE:
                    return R.drawable.ic_bt_cellphone;

                case BluetoothClass.Device.Major.PERIPHERAL:
                    return HidProfile.getHidClassDrawable(btClass);

                case BluetoothClass.Device.Major.IMAGING:
                    return R.drawable.ic_bt_imaging;

                default:
                    // unrecognized device class; continue
            }
        } else {
            Logger.getLogger().i( "mBtClass is null");
        }

        List<LocalBluetoothProfile> profiles = device.getProfiles();
        for (LocalBluetoothProfile profile : profiles) {
            int resId = profile.getDrawableResource(btClass);
            if (resId != 0) {
                return resId;
            }
        }
        if (btClass != null) {
            if (btClass.doesClassMatch(BluetoothClass.PROFILE_A2DP)) {
                return R.drawable.ic_bt_headphones_a2dp;

            }
            if (btClass.doesClassMatch(BluetoothClass.PROFILE_HEADSET)) {
                return R.drawable.ic_bt_headset_hfp;
            }
        }

        return R.drawable.ic_settings_bluetooth2;
    }

    private int getConnectionSummary(CachedBluetoothDevice device) {
        final CachedBluetoothDevice cachedDevice = device;

        boolean profileConnected = false;       // at least one profile is connected
        boolean a2dpNotConnected = false;       // A2DP is preferred but not connected
        boolean headsetNotConnected = false;    // Headset is preferred but not connected

        for (LocalBluetoothProfile profile : cachedDevice.getProfiles()) {
            int connectionStatus = cachedDevice.getProfileConnectionState(profile);

            switch (connectionStatus) {
                case BluetoothProfile.STATE_CONNECTING:
                case BluetoothProfile.STATE_DISCONNECTING:
                    return Utils.getConnectionStateSummary(connectionStatus);

                case BluetoothProfile.STATE_CONNECTED:
                    profileConnected = true;
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    if (profile.isProfileReady()) {
                        if (profile instanceof A2dpProfile) {
                            a2dpNotConnected = true;
                        } else if (profile instanceof HeadsetProfile) {
                            headsetNotConnected = true;
                        }
                    }
                    break;
            }
        }

        if (profileConnected) {
            if (a2dpNotConnected && headsetNotConnected) {
                return R.string.bluetooth_connected_no_headset_no_a2dp;
            } else if (a2dpNotConnected) {
                return R.string.bluetooth_connected_no_a2dp;
            } else if (headsetNotConnected) {
                return R.string.bluetooth_connected_no_headset;
            } else {
                return R.string.bluetooth_connected;
            }
        }

        switch (cachedDevice.getBondState()) {
            case BluetoothDevice.BOND_BONDING:
                return R.string.bluetooth_pairing;

            case BluetoothDevice.BOND_BONDED:
            case BluetoothDevice.BOND_NONE:
            default:
                return 0;
        }
    }


    private class Holder {
        private TextView name;
        private ImageView icon;
        private TextView paied;
    }
}
