/*
 * Wifi Connecter
 * 
 * Copyright (c) 2011 Kevin Yuan (farproc@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 **/

package com.zx.zxtvsettings.wifi;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.zx.zxtvsettings.R.string.security;


public class Wifi {

    public static final ConfigurationSecurities ConfigSec = ConfigurationSecurities.newInstance();

    private static final String TAG = "Wifi Connecter";

    private static final int[] STATE_SECURED = {
            R.attr.state_encrypted
    };
    private static final int[] STATE_NONE = {};

    private static int[] wifi_signal_attributes = { R.attr.wifi_signal };

    /**
     * These values are matched in string arrays -- changes must be kept in sync
     */
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;

    enum PskType {
        UNKNOWN,
        WPA,
        WPA2,
        WPA_WPA2
    }


    private WifiConfiguration mWifiConfiguration;
    // 打开WIFI
    public static void openWifi(final WifiManager wifiMgr) {
        if (!wifiMgr.isWifiEnabled()) {
            wifiMgr.setWifiEnabled(true);
        }
    }

    // 关闭WIFI
    public static void closeWifi(final WifiManager wifiMgr) {
        if (wifiMgr.isWifiEnabled()) {
            wifiMgr.setWifiEnabled(false);
        }
    }

    public static void startScan(final WifiManager wifiMgr) {
        wifiMgr.startScan();
    }

    public static String getConnectWifiName(final WifiManager wifiMgr) {
        WifiInfo info = wifiMgr.getConnectionInfo();

        return info != null ? info.getSSID() : null;
    }

    public static int getSecurity(WifiConfiguration configuration) {
        if(configuration.allowedAuthAlgorithms.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if(configuration.allowedAuthAlgorithms.get(WifiConfiguration.KeyMgmt.WPA_EAP)) {
            return SECURITY_EAP;
        }

        return (configuration.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    public static  int getSecurity(final ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        }
        if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        }
        if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }

        return SECURITY_NONE;
    }

    public static String getSecurityString(Context context, final ScanResult result, boolean concise) {
        PskType pskType = getPskType(result);
        switch(security) {
            case SECURITY_EAP:
                return concise ? context.getString(R.string.wifi_security_short_eap) :
                        context.getString(R.string.wifi_security_eap);
            case SECURITY_PSK:
                switch (pskType) {
                    case WPA:
                        return concise ? context.getString(R.string.wifi_security_short_wpa) :
                                context.getString(R.string.wifi_security_wpa);
                    case WPA2:
                        return concise ? context.getString(R.string.wifi_security_short_wpa2) :
                                context.getString(R.string.wifi_security_wpa2);
                    case WPA_WPA2:
                        return concise ? context.getString(R.string.wifi_security_short_wpa_wpa2) :
                                context.getString(R.string.wifi_security_wpa_wpa2);
                    case UNKNOWN:
                    default:
                        return concise ? context.getString(R.string.wifi_security_short_psk_generic)
                                : context.getString(R.string.wifi_security_psk_generic);
                }
            case SECURITY_WEP:
                return concise ? context.getString(R.string.wifi_security_short_wep) :
                        context.getString(R.string.wifi_security_wep);
            case SECURITY_NONE:
            default:
                return concise ? "" : context.getString(R.string.wifi_security_none);
        }
    }

    private static PskType getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        } else if (wpa2) {
            return PskType.WPA2;
        } else if (wpa) {
            return PskType.WPA;
        } else {
            Logger.getLogger().w("Received abnormal flag string: " + result.capabilities);
            return PskType.UNKNOWN;
        }
    }

    public static int getLevel(int rssi) {
        if (rssi == Integer.MAX_VALUE) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(rssi, 4);
    }

    /**
     *
     * 更新wifi信号指示图
     *
     * */
    public static  void updateIcon(ImageView view, ScanResult result, Context context) {

        int security = getSecurity(result);
        int level = getLevel(result.level);

        if (level == -1) {
            view.setBackground(null);
        } else {
            Drawable drawable = view.getDrawable();
            Logger.getLogger().d(" **************** updateIcon drawable =  " + drawable  + " security = " + security);

                // To avoid a drawing race condition, we first set the state (SECURE/NONE) and then
                // set the icon (drawable) to that state's drawable.
                StateListDrawable sld = (StateListDrawable) context.getTheme()
                        .obtainStyledAttributes(wifi_signal_attributes).getDrawable(0);
                // If sld is null then we are indexing and therefore do not have access to
                // (nor need to display) the drawable.
            Logger.getLogger().d(" " + sld.toString());
            if (sld != null) {
                sld.setState((security != SECURITY_NONE) ? STATE_SECURED : STATE_NONE);
                drawable = sld.getCurrent();
            }

            if (drawable != null) {
                drawable.setLevel(level);
                view.setBackground(drawable);
            }


        }
    }

    public static boolean isWifiEnabled(final Context context) {

        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            ConnectivityManager connManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiInfo.isConnected();
        } else {
            return false;
        }
    }

    /**
     * Change the password of an existing configured network and connect to it
     *
     * @param wifiMgr
     * @param config
     * @param newPassword
     * @return
     */
    public static boolean changePasswordAndConnect(final Context ctx, final WifiManager wifiMgr, final WifiConfiguration config, final String newPassword, final int numOpenNetworksKept) {
        ConfigSec.setupSecurity(config, ConfigSec.getWifiConfigurationSecurity(config), newPassword);
        final int networkId = wifiMgr.updateNetwork(config);
        if (networkId == -1) {
            // Update failed.
            return false;
        }
        // Force the change to apply.
        wifiMgr.disconnect();
        return connectToConfiguredNetwork(ctx, wifiMgr, config, true);
    }

    /**
     * Configure a network, and connect to it.
     *
     * @param wifiMgr
     * @param scanResult
     * @param password   Password for secure network or is ignored.
     * @return
     */
    public static boolean connectToNewNetwork(final Context ctx, final WifiManager wifiMgr, final ScanResult scanResult, final String password, final int numOpenNetworksKept) {
        final String security = ConfigSec.getScanResultSecurity(scanResult);

        if (ConfigSec.isOpenNetwork(security)) {
            checkForExcessOpenNetworkAndSave(wifiMgr, numOpenNetworksKept);
        }

        WifiConfiguration config = new WifiConfiguration();
        config.SSID = convertToQuotedString(scanResult.SSID);
        config.BSSID = scanResult.BSSID;
        ConfigSec.setupSecurity(config, security, password);

        int id = -1;
        try {
            id = wifiMgr.addNetwork(config);
        } catch (NullPointerException e) {
            Log.e(TAG, "Weird!! Really!! What's wrong??", e);
            // Weird!! Really!!
            // This exception is reported by user to Android Developer Console(https://market.android.com/publish/Home)
        }
        if (id == -1) {
            return false;
        }

        if (!wifiMgr.saveConfiguration()) {
            return false;
        }

        config = getWifiConfiguration(wifiMgr, config, security);
        if (config == null) {
            return false;
        }

        return connectToConfiguredNetwork(ctx, wifiMgr, config, true);
    }

    /**
     * Connect to a configured network.
     *
     * @param wifiManager
     * @param config
     * @param numOpenNetworksKept Settings.Secure.WIFI_NUM_OPEN_NETWORKS_KEPT
     * @return
     */
    public static boolean connectToConfiguredNetwork(final Context ctx, final WifiManager wifiMgr, WifiConfiguration config, boolean reassociate) {
        final String security = ConfigSec.getWifiConfigurationSecurity(config);

        int oldPri = config.priority;
        // Make it the highest priority.
        int newPri = getMaxPriority(wifiMgr) + 1;
        if (newPri > MAX_PRIORITY) {
            newPri = shiftPriorityAndSave(wifiMgr);
            config = getWifiConfiguration(wifiMgr, config, security);
            if (config == null) {
                return false;
            }
        }

        // Set highest priority to this configured network
        config.priority = newPri;
        int networkId = wifiMgr.updateNetwork(config);
        if (networkId == -1) {
            return false;
        }

        // Do not disable others
        if (!wifiMgr.enableNetwork(networkId, false)) {
            config.priority = oldPri;
            return false;
        }

        if (!wifiMgr.saveConfiguration()) {
            config.priority = oldPri;
            return false;
        }

        // We have to retrieve the WifiConfiguration after save.
        config = getWifiConfiguration(wifiMgr, config, security);
        if (config == null) {
            return false;
        }

        ReenableAllApsWhenNetworkStateChanged.schedule(ctx);

        // Disable others, but do not save.
        // Just to force the WifiManager to connect to it.
        if (!wifiMgr.enableNetwork(config.networkId, true)) {
            return false;
        }

        final boolean connect = reassociate ? wifiMgr.reassociate() : wifiMgr.reconnect();
        if (!connect) {
            return false;
        }

        return true;
    }

    public static void sortByPriority(final List<WifiConfiguration> configurations) {
        java.util.Collections.sort(configurations, new Comparator<WifiConfiguration>() {

            @Override
            public int compare(WifiConfiguration object1,
                               WifiConfiguration object2) {
                return object1.priority - object2.priority;
            }
        });
    }

    public static void sortByLevel(final List<ScanResult> results) {
        Collections.sort(results, new Comparator<ScanResult>() {

            @Override
            public int compare(ScanResult o1, ScanResult o2) {
                int o1Level = getLevel(o1.level);
                int o2Level = getLevel(o2.level);

                return o1Level - o2Level;
            }
        });
    }

    /**
     * Ensure no more than numOpenNetworksKept open networks in configuration list.
     *
     * @param wifiMgr
     * @param numOpenNetworksKept
     * @return Operation succeed or not.
     */
    private static boolean checkForExcessOpenNetworkAndSave(final WifiManager wifiMgr, final int numOpenNetworksKept) {
        final List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        sortByPriority(configurations);

        boolean modified = false;
        int tempCount = 0;
        for (int i = configurations.size() - 1; i >= 0; i--) {
            final WifiConfiguration config = configurations.get(i);
            if (ConfigSec.isOpenNetwork(ConfigSec.getWifiConfigurationSecurity(config))) {
                tempCount++;
                if (tempCount >= numOpenNetworksKept) {
                    modified = true;
                    wifiMgr.removeNetwork(config.networkId);
                }
            }
        }
        if (modified) {
            return wifiMgr.saveConfiguration();
        }

        return true;
    }

    private static final int MAX_PRIORITY = 99999;

    private static int shiftPriorityAndSave(final WifiManager wifiMgr) {
        final List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        sortByPriority(configurations);
        final int size = configurations.size();
        for (int i = 0; i < size; i++) {
            final WifiConfiguration config = configurations.get(i);
            config.priority = i;
            wifiMgr.updateNetwork(config);
        }
        wifiMgr.saveConfiguration();
        return size;
    }

    private static int getMaxPriority(final WifiManager wifiManager) {
        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        int pri = 0;
        for (final WifiConfiguration config : configurations) {
            if (config.priority > pri) {
                pri = config.priority;
            }
        }
        return pri;
    }

    private static final String BSSID_ANY = "any";

    public static WifiConfiguration getWifiConfiguration(final WifiManager wifiMgr, final ScanResult hotsopt, String hotspotSecurity) {
        final String ssid = convertToQuotedString(hotsopt.SSID);
        if (ssid.length() == 0) {
            return null;
        }

        final String bssid = hotsopt.BSSID;
        if (bssid == null) {
            return null;
        }

        if (hotspotSecurity == null) {
            hotspotSecurity = ConfigSec.getScanResultSecurity(hotsopt);
        }

        final List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        if (configurations == null) {
            return null;
        }

        for (final WifiConfiguration config : configurations) {
            if (config.SSID == null || !ssid.equals(config.SSID)) {
                continue;
            }
            if (config.BSSID == null || BSSID_ANY.equals(config.BSSID) || bssid.equals(config.BSSID)) {
                final String configSecurity = ConfigSec.getWifiConfigurationSecurity(config);
                if (hotspotSecurity.equals(configSecurity)) {
                    return config;
                }
            }
        }
        return null;
    }

    public static WifiConfiguration getWifiConfiguration(final WifiManager wifiMgr, final WifiConfiguration configToFind, String security) {
        final String ssid = configToFind.SSID;
        if (ssid.length() == 0) {
            return null;
        }

        final String bssid = configToFind.BSSID;


        if (security == null) {
            security = ConfigSec.getWifiConfigurationSecurity(configToFind);
        }

        final List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();

        for (final WifiConfiguration config : configurations) {
            if (config.SSID == null || !ssid.equals(config.SSID)) {
                continue;
            }
            if (config.BSSID == null || BSSID_ANY.equals(config.BSSID) || bssid == null || bssid.equals(config.BSSID)) {
                final String configSecurity = ConfigSec.getWifiConfigurationSecurity(config);
                if (security.equals(configSecurity)) {
                    return config;
                }
            }
        }
        return null;
    }

    public static String convertToQuotedString(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }

        final int lastPos = string.length() - 1;
        if (lastPos > 0 && (string.charAt(0) == '"' && string.charAt(lastPos) == '"')) {
            return string;
        }

        return "\"" + string + "\"";
    }

}
