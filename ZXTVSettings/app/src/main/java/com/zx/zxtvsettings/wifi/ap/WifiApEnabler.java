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

package com.zx.zxtvsettings.wifi.ap;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.widget.Switch;
import android.widget.TextView;

import com.zx.zxtvsettings.R;


public class WifiApEnabler {
//    public static final String WIFI_AP_STATE_CHANGED_ACTION =
//            "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static final String ACTION_TETHER_STATE_CHANGED =
            "android.net.conn.TETHER_STATE_CHANGED";

//    public static final String EXTRA_WIFI_AP_STATE = "wifi_state";

//    public static final int WIFI_AP_STATE_DISABLING = 10;
//    public static final int WIFI_AP_STATE_DISABLED = 11;
//    public static final int WIFI_AP_STATE_ENABLING = 12;
//    public static final int WIFI_AP_STATE_ENABLED = 13;
//    public static final int WIFI_AP_STATE_FAILED = 14;

    private final Context mContext;
    private final Switch mSwitch;
    private final TextView mSummaryText;
    private final CharSequence mOriginalSummary;

    private WifiManager mWifiManager;
    private WifiApManager mWifiApManager;
    private final IntentFilter mIntentFilter;

    ConnectivityManager mCm;
    private String[] mWifiRegexs;
    private boolean mReveiving;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
                handleWifiApStateChanged(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_AP_STATE, WifiManager.WIFI_AP_STATE_FAILED));
            }
//            else if (ConnectivityManager.ACTION_TETHER_STATE_CHANGED.equals(action)) {
//                ArrayList<String> available = intent.getStringArrayListExtra(
//                        ConnectivityManager.EXTRA_AVAILABLE_TETHER);
//                ArrayList<String> active = intent.getStringArrayListExtra(
//                        ConnectivityManager.EXTRA_ACTIVE_TETHER);
//                ArrayList<String> errored = intent.getStringArrayListExtra(
//                        ConnectivityManager.EXTRA_ERRORED_TETHER);
//                updateTetherState(available.toArray(), active.toArray(), errored.toArray());
//            }
            else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                enableWifiSwitch();
            }
        }
    };
    
    public WifiApEnabler(Context context, Switch swi, TextView summaryText) {
        mContext = context;
        mSwitch = swi;
        mSummaryText = summaryText;
        mOriginalSummary = summaryText.getText();

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mCm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        mWifiApManager = new WifiApManager(context);

//        mWifiRegexs = mCm.getTetherableWifiRegexs();

        mIntentFilter = new IntentFilter(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(ACTION_TETHER_STATE_CHANGED);
        mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mReveiving=false;
    }

    public void resume() {
        if(!mReveiving){
            mContext.registerReceiver(mReceiver, mIntentFilter);
            enableWifiSwitch();
            mReveiving=true;
        }
    }

    public void pause() {
        if(mReveiving){
            mContext.unregisterReceiver(mReceiver);
            mReveiving=false;
        }
    }

    private void enableWifiSwitch() {
        boolean isAirplaneMode = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        if(!isAirplaneMode) {
            mSwitch.setEnabled(true);
        } else {
            //mSwitch.setSummary(mOriginalSummary);
            mSummaryText.setText(mOriginalSummary);
            mSwitch.setEnabled(false);
        }
    }

    public void setSoftapEnabled(boolean enable) {
        final ContentResolver cr = mContext.getContentResolver();
        /**
         * Disable Wifi if enabling tethering
         */
        int wifiState = mWifiManager.getWifiState();
        if (enable && ((wifiState == WifiManager.WIFI_STATE_ENABLING) ||
                    (wifiState == WifiManager.WIFI_STATE_ENABLED))) {
            mWifiManager.setWifiEnabled(false);
//            Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 1);
        }

        if (mWifiManager.setWifiApEnabled(null, enable)) {
//        if (mWifiApManager.setWifiApEnabled(null, enable)) {
            if (mSwitch != null) {

                /* Disable here, enabled on receiving success broadcast */
                mSwitch.setEnabled(false);
            }
        } else {
            if (mSwitch != null) {
                //mSwitch.setSummary(R.string.wifi_error);
                mSummaryText.setText(R.string.wifi_error);
            }
        }

        /**
         *  If needed, restore Wifi on tether disable
         */
        if (!enable) {
            int wifiSavedState = 0;
//            try {
////                wifiSavedState = Settings.Global.getInt(cr, Settings.Global.WIFI_SAVED_STATE);
//            } catch (Settings.SettingNotFoundException e) {
//                ;
//            }
            if (wifiSavedState == 1) {
                mWifiManager.setWifiEnabled(true);
//                mWifiManager.getWifiApState();
//                Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 0);
            }
        }
    }

    public void updateConfigSummary(WifiConfiguration wifiConfig) {
        String s = mContext.getString(R.string.wifi_tether_configure_ssid_default);
        /*
        mSwitch.setSummary(String.format(
                    mContext.getString(R.string.wifi_tether_enabled_subtext),
                    (wifiConfig == null) ? s : wifiConfig.SSID));
                    */
        mSummaryText.setText(String.format(
                    mContext.getString(R.string.wifi_tether_enabled_subtext),
                    (wifiConfig == null) ? s : wifiConfig.SSID));
    }

    private void updateTetherState(Object[] available, Object[] tethered, Object[] errored) {
        boolean wifiTethered = false;
        boolean wifiErrored = false;

        for (Object o : tethered) {
            String s = (String)o;
            for (String regex : mWifiRegexs) {
                if (s.matches(regex)) wifiTethered = true;
            }
        }
        for (Object o: errored) {
            String s = (String)o;
            for (String regex : mWifiRegexs) {
                if (s.matches(regex)) wifiErrored = true;
            }
        }

        if (wifiTethered) {
            WifiConfiguration wifiConfig = mWifiManager.getWifiApConfiguration();
//            WifiConfiguration wifiConfig = mWifiApManager.getWifiApConfiguration();
            updateConfigSummary(wifiConfig);
        } else if (wifiErrored) {
            //mSwitch.setSummary(R.string.wifi_error);
            mSummaryText.setText(R.string.wifi_error);
        }
    }

    private void handleWifiApStateChanged(int state) {
        switch (state) {
            case WifiManager.WIFI_AP_STATE_ENABLING:
                //mSwitch.setSummary(R.string.wifi_tether_starting);
                mSummaryText.setText(R.string.wifi_tether_starting);
                mSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_AP_STATE_ENABLED:
                /**
                 * Summary on enable is handled by tether
                 * broadcast notice
                 */
                mSwitch.setChecked(true);
                /* Doesnt need the airplane check */
                mSwitch.setEnabled(true);
                break;
            case WifiManager.WIFI_AP_STATE_DISABLING:
                //mSwitch.setSummary(R.string.wifi_tether_stopping);
                mSummaryText.setText(R.string.wifi_tether_stopping);
                mSwitch.setChecked(false);
                mSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_AP_STATE_DISABLED:
                mSwitch.setChecked(false);
                //mSwitch.setSummary(mOriginalSummary);
                mSummaryText.setText(mOriginalSummary);
                enableWifiSwitch();
                break;
            default:
                mSwitch.setChecked(false);
                //mSwitch.setSummary(R.string.wifi_error);
                mSummaryText.setText(R.string.wifi_error);
                enableWifiSwitch();
        }
    }
}
