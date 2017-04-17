/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zx.zxtvsettings.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.wifi.ap.WIFI_AP_STATE;
import com.zx.zxtvsettings.wifi.ap.WifiApManager;

import java.util.concurrent.atomic.AtomicBoolean;

public class WifiEnabler implements OnCheckedChangeListener {
    private Context mContext;
    private Switch mSwitch;
    private boolean mListeningToOnSwitchChange = false;
    private AtomicBoolean mConnected = new AtomicBoolean(false);

    private final WifiManager mWifiManager;
    private final WifiApManager mWifiApManager;
    private boolean mStateMachineEvent;
    private final IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                handleWifiStateChanged(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                if (!mConnected.get()) {
                    handleStateChanged(WifiInfo.getDetailedStateOf((SupplicantState)
                            intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                        WifiManager.EXTRA_NETWORK_INFO);
                mConnected.set(info.isConnected());
                handleStateChanged(info.getDetailedState());
            }
        }
    };

    private static final String EVENT_DATA_IS_WIFI_ON = "is_wifi_on";
    private static final int EVENT_UPDATE_INDEX = 0;

    public WifiEnabler(Context context, Switch switc) {
        mContext = context;
        mSwitch = switc;

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiApManager = new WifiApManager(context);

        mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        // The order matters! We really should not depend on this. :(
        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        setupSwitch();
    }

    public void setupSwitch() {
        final int state = mWifiManager.getWifiState();
        handleWifiStateChanged(state);
        if (!mListeningToOnSwitchChange) {
            mSwitch.setOnCheckedChangeListener(this);
            mListeningToOnSwitchChange = true;
        }
    }

    public void teardownSwitch() {
        if (mListeningToOnSwitchChange) {
            mSwitch.setOnCheckedChangeListener(null);
            mListeningToOnSwitchChange = false;
        }
    }

    public void resume(Context context) {
        mContext = context;
        // Wi-Fi state is sticky, so just let the receiver update UI
        mContext.registerReceiver(mReceiver, mIntentFilter);
        if (!mListeningToOnSwitchChange) {
            mSwitch.setOnCheckedChangeListener(this);
            mListeningToOnSwitchChange = true;
        }
    }

    public void pause() {
        mContext.unregisterReceiver(mReceiver);
        if (mListeningToOnSwitchChange) {
            mSwitch.setOnCheckedChangeListener(null);
            mListeningToOnSwitchChange = false;
        }
    }

    private void handleWifiStateChanged(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                mSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                setSwitchChecked(true);
                mSwitch.setEnabled(true);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                mSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                setSwitchChecked(false);
                mSwitch.setEnabled(true);
                break;
            default:
                setSwitchChecked(false);
                mSwitch.setEnabled(true);
        }
    }

    private void setSwitchChecked(boolean checked) {
        mStateMachineEvent = true;
        mSwitch.setChecked(checked);
        mStateMachineEvent = false;
    }

    private void handleStateChanged(@SuppressWarnings("unused") NetworkInfo.DetailedState state) {
        // After the refactoring from a CheckBoxPreference to a Switch, this method is useless since
        // there is nowhere to display a summary.
        // This code is kept in case a future change re-introduces an associated text.
        /*
        // WifiInfo is valid if and only if Wi-Fi is enabled.
        // Here we use the state of the switch as an optimization.
        if (state != null && mSwitch.isChecked()) {
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (info != null) {
                //setSummary(Summary.get(mContext, info.getSSID(), state));
            }
        }
        */
    }

    @Override
    public void onCheckedChanged(CompoundButton  switchView, boolean isChecked) {
        //Do nothing if called as a result of a state machine event
        if (mStateMachineEvent) {
            return;
        }
        // Show toast message if Wi-Fi is not allowed in airplane mode
	/*
        if (isChecked && !WirelessSettings.isRadioAllowed(mContext, Settings.Global.RADIO_WIFI)) {
            Toast.makeText(mContext, R.string.wifi_in_airplane_mode, Toast.LENGTH_SHORT).show();
            // Reset switch to off. No infinite check/listenenr loop.
            mSwitchBar.setChecked(false);
            return;
        }
	*/


        // Disable tethering if enabling Wifi
        WIFI_AP_STATE wifiApState = mWifiApManager.getWifiApState();
        if (isChecked && ((wifiApState == WIFI_AP_STATE.WIFI_AP_STATE_ENABLING) ||
                (wifiApState == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED))) {
           mWifiApManager.setWifiApEnabled(null, false);
        }

        if (!mWifiManager.setWifiEnabled(isChecked)) {
            // Error
            mSwitch.setEnabled(true);
            Toast.makeText(mContext, R.string.wifi_error, Toast.LENGTH_SHORT).show();
        }
    }





}