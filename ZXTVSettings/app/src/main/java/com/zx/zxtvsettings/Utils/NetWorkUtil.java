package com.zx.zxtvsettings.Utils;

import android.annotation.NonNull;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.os.SystemProperties;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


public class NetWorkUtil {

    private NetWorkUtil() {
    }

    /**
     * 判断网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetWorkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * 检测wifi是否连接
     *
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 检测3G是否连接
     *
     * @return
     */
    public static boolean is3gConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public static boolean isEthernetConnected(Context mContext) {

        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State mEthernetState = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET).getState();
        if (mEthernetState == NetworkInfo.State.CONNECTED) {
            Logger.getLogger().i("Ethernet connect state = true");
            return true;
        }

        return false;
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }

    public static NetState getLocalNetState(Context context) {
        NetState netState = new NetState();
        if (isEthernetConnected(context)) {

            netState.ipAddress = SystemProperties.get("dhcp.eth0.ipaddress", "error");
            netState.netmask = SystemProperties.get("dhcp.eth0.mask", "error");
            netState.gateway = SystemProperties.get("dhcp.eth0.gateway", "error");
            netState.dns1 = SystemProperties.get("dhcp.eth0.dns1", "error");
            netState.dns2 = SystemProperties.get("dhcp.eth0.dns2", "0.0.0.0");


        }

        if (isWifiConnected(context)) {
            netState.ipAddress = SystemProperties.get("dhcp.wlan0.ipaddress", "error");
            netState.netmask = SystemProperties.get("dhcp.wlan0.mask", "error");
            netState.gateway = SystemProperties.get("dhcp.wlan0.gateway", "error");
            netState.dns1 = SystemProperties.get("dhcp.wlan0.dns1", "error");
            netState.dns2 = SystemProperties.get("dhcp.wlan0.dns2", "0.0.0.0");
        }

        return netState;

    }

    public static Inet4Address getIPv4Address(@NonNull String text) {
        try {
            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
        } catch (IllegalArgumentException | ClassCastException e) {
            return null;
        }
    }


    public static class NetState {
        public String ipAddress;
        public String netmask;
        public String gateway;
        public String dns1;
        public String dns2;
    }
}
