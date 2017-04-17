package com.zx.zxtvsettings.activity;

import android.annotation.NonNull;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.EthernetManager;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.os.INetworkManagementService;
import android.os.SystemProperties;
import android.widget.TextView;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.Logger;
import com.zx.zxtvsettings.fragment.ethernet.EthernetAutoDetectFragment;
import com.zx.zxtvsettings.fragment.ethernet.EthernetFragment;
import com.zx.zxtvsettings.fragment.ethernet.EthernetManDetectFragment;
import com.zx.zxtvsettings.fragment.ethernet.mode.DataID;
import com.zx.zxtvsettings.fragment.ethernet.mode.EthernetMode;
import com.zx.zxtvsettings.fragment.ethernet.mode.NetData;
import com.zx.zxtvsettings.fragment.ethernet.mode.NetState;
import com.zx.zxtvsettings.fragment.ethernet.mode.NetType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: ShaudXiao
 * Date: 2017-03-23
 * Time: 10:35
 * Company: zx
 * Description:
 * FIXME
 */

public class EthernetActvity extends BaseStatusBarActivity implements EthernetFragment.Callbacks, EthernetAutoDetectFragment.Callbacks,
        EthernetManDetectFragment.Callbacks{

    TextView mEthernetTitle;


    private NetState mNetState = null;
    private Context mContext;
    private NetworkInfo.State mEthernetState;
    private NetworkInfo.State mWifiState;
    private boolean mFastSetFlag = false;
    private SharedPreferences mPreferences;
    ConnectivityManager cm;
    private static final String ETH0_MAC_ADDR = "/sys/class/net/eth0/address";
    private static final String WLAN0_MAC_ADDR = "/sys/class/net/wlan0/address";

    // Ethernet ===============================================================
    private IpConfiguration staticIpConfig = new IpConfiguration();
    private IpConfiguration mIpConfiguration;
    private EthernetManager mEthernetManager;
    // private final EthernetConfigStore mEthernetConfigStore;
    /** To set link state and configure IP addresses. */
    private INetworkManagementService mNMService;

    // update setting values on IP address, Gateway, Netmask, DNS
    private StaticIpConfiguration staticIpConfiguration = null;
    private Inet4Address inetAddr = null;
    private InetAddress gatewayAddr = null;
    private InetAddress dnsAddr1 = null;
    private InetAddress dnsAddr2 = null;
    private int networkPrefixLength = -1;
    private static final String IP_ADDRESS = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})";
    private static final String SLASH_FORMAT = IP_ADDRESS + "/(\\d{1,3})";
    private static final Pattern addressPattern = Pattern.compile(IP_ADDRESS);
    private static final Pattern cidrPattern = Pattern.compile(SLASH_FORMAT);
    private HashMap<String, Fragment> mFragmentMap = new HashMap<String, Fragment>();


    @Override
    protected int getLayoutId() {
        return R.layout.activity_ethernet;
    }

    @Override
    protected void setupViews() {

    }

    @Override
    protected void initialized() {
        initFragments();
        mContext = this;
        mNetState = new NetState();

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment fragment = mFragmentMap.get(NetData.Ethernet_KeyId);
        if(null != fragment) {
            ft.add( R.id.network_framelayout,fragment, NetData.Ethernet_Tag);
            ft.commit();
        }
    }

    private void initFragments() {
        mFragmentMap.put(NetData.Ethernet_KeyId, new EthernetFragment());

    }

    public NetState getNetState() {
        return mNetState;
    }

    public HashMap<String, Fragment> getFragmentMap() {
        return mFragmentMap;
    }

    @Override
    public void registerNetworkFragments(@NonNull String key, Fragment frag) {
        if (null != mFragmentMap && mFragmentMap.get(key) == null ) {
           mFragmentMap.put(key, frag);
        }
    }

    @Override
    public boolean ethernetAutodetectEthernet() {
        mFastSetFlag = false;
        updateNetState();

        return isEthernetConnected();
    }

    @Override
    public void autodetectEthernet() {
        autoDetect();
    }

    @Override
    public boolean ethernetMandetectEthernet() {
        mFastSetFlag = false;
        if (isEthernetConnected()) {
            getNetState().setNetType(NetType.TYPE_ETHERNET);
            getNetState().setEthernetConnectState(true);
        } else  {
            getNetState().setNetType(NetType.TYPE_NONE);
            getNetState().setEthernetConnectState(false);

        }

        return isEthernetConnected();
    }

    @Override
    public void mandetectEthernet() {
        mIpConfiguration = new IpConfiguration();
        mEthernetManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
        staticIpConfiguration = new StaticIpConfiguration();
        try {
            mNMService.setInterfaceDown("eth0");
        } catch (Exception e) {
            Logger.getLogger().e(TAG, "Failed to setInterfaceDown");
        }

        try {
            mNMService.clearInterfaceAddresses("eth0");
        } catch (Exception e) {
            Logger.getLogger().e(TAG, "Failed to clear address or disable ipv6" + e);
        }

        try {
            inetAddr = getIPv4Address(getNetState().getNetStateString(DataID.sETHERNET_IP_ID));
            gatewayAddr = getIPv4Address(getNetState().getNetStateString(DataID.sETHERNET_GATE_ID));
            networkPrefixLength = (int) toInteger(getNetState().getNetStateString(DataID.sETHERNET_MAC_ID));
            dnsAddr1 = getIPv4Address(getNetState().getNetStateString(DataID.sETHERNET_DNS1_ID));
            dnsAddr2 = getIPv4Address(getNetState().getNetStateString(DataID.sETHERNET_DNS2_ID));
        } catch (Exception e) {
            return;
        }

        if(inetAddr != null && networkPrefixLength != -1) {
            staticIpConfiguration.ipAddress = new LinkAddress(inetAddr, networkPrefixLength);

        } else {
            try {
                mNMService.setInterfaceDown("eth0");
            } catch (Exception e) {
                Logger.getLogger().e(TAG, "Failed to setInterfaceDown" );
            }
        }

        if(gatewayAddr != null) {
            staticIpConfiguration.gateway = gatewayAddr;
        }
        if(dnsAddr1 != null) {
            staticIpConfiguration.dnsServers.add(dnsAddr1);
        }
        if(dnsAddr2 != null) {
            staticIpConfiguration.dnsServers.add(dnsAddr1);
        }

        mIpConfiguration.setStaticIpConfiguration(staticIpConfiguration);
        mIpConfiguration.setIpAssignment(IpConfiguration.IpAssignment.STATIC);
        mEthernetManager.setConfiguration(mIpConfiguration);
        try {
            mNMService.setInterfaceDown("eth0");
        } catch (Exception e) {
            Logger.getLogger().e(TAG, "Failed to setInterfaceDown" );
        }

    }

    private void updateNetState() {
        String ipAddress = null;
        String netmask = null;
        String gateway = null;
        String dns1 = null;
        String dns2 = null;
        String mac = null;
        EthernetMode ethernetMode = getNetState().getEthernetMode();
        if(isEthernetConnected()) {
            mac = getEth0MacAddr();
            getNetState().setNetStateString(DataID.sETHERNET_MAC_ID, mac);
            if(ethernetMode == EthernetMode.MODE_AUTO || mFastSetFlag) {
                ipAddress = SystemProperties.get("dhcp.eth0.ipaddress", "error");
                netmask = SystemProperties.get("dhcp.eth0.mask", "error");
                gateway = SystemProperties.get("dhcp.eth0.gateway", "error");
                dns1 = SystemProperties.get("dhcp.eth0.dns1", "error");
                dns2 = SystemProperties.get("dhcp.eth0.dns2", "0.0.0.0");

                if(ipAddress.equals("error") || netmask.equals("error")
                        || dns1.equals("error")) {
                    Logger.getLogger().e(TAG, "get Etherent info error");
                    Logger.getLogger().e(TAG, "dhcp.eth0.ipaddress " + ipAddress);
                    Logger.getLogger().e(TAG, "dhcp.eth0.mask " + netmask);
                    Logger.getLogger().e(TAG, "dhcp.eth0.gateway " + gateway);
                    Logger.getLogger().e(TAG, "dhcp.eth0.dns1 " + dns1);
                    return ;
                }
                getNetState().setNetStateString(DataID.sETHERNET_IP_ID, ipAddress);
                getNetState().setNetStateString(DataID.sETHERNET_MASK_ID, netmask);
                getNetState().setNetStateString(DataID.sETHERNET_GATE_ID, gateway);
                getNetState().setNetStateString(DataID.sETHERNET_DNS1_ID, dns1);
                getNetState().setNetStateString(DataID.sETHERNET_DNS2_ID, dns2);
            }
            getNetState().setEthernetConnectState(true);
        } else {
            getNetState().setEthernetConnectState(false);
        }

        if(isWifiConnected()) {
            mac = getWlan0MacAddr();

            ipAddress = SystemProperties.get("dhcp.wlan0.ipaddress", "error");
            netmask = SystemProperties.get("dhcp.wlan0.mask", "error");
            gateway = SystemProperties.get("dhcp.wlan0.gateway", "error");
            dns1 = SystemProperties.get("dhcp.wlan0.dns1", "error");
            dns2 = SystemProperties.get("dhcp.wlan0.dns2", "0.0.0.0");

            if(ipAddress.equals("error") || netmask.equals("error")
                    || dns1.equals("error")) {
                Logger.getLogger().e(TAG, "get Wifi info error");
                Logger.getLogger().e(TAG, "dhcp.wlan0.ipaddress " + ipAddress);
                Logger.getLogger().e(TAG, "dhcp.wlan0.mask " + netmask);
                Logger.getLogger().e(TAG, "dhcp.wlan0.gateway " + gateway);
                Logger.getLogger().e(TAG, "dhcp.wlan0.dns1 " + dns1);
                return ;
            }
            getNetState().setNetStateString(DataID.sWIFI_MAC_ID, mac);
            getNetState().setNetStateString(DataID.sWIFI_IP_ID, ipAddress);
            getNetState().setNetStateString(DataID.sWIFI_MASK_ID, netmask);
            getNetState().setNetStateString(DataID.sWIFI_GATE_ID, gateway);
            getNetState().setNetStateString(DataID.sWIFI_DNS1_ID, dns1);
            getNetState().setNetStateString(DataID.sWIFI_DNS2_ID, dns2);

            getNetState().setWifiConnectState(true);

        } else {
            getNetState().setWifiConnectState(false);
        }

		/* sort network priority */
        if (isEthernetConnected() && mFastSetFlag) {
            Logger.getLogger().i(TAG, "enter case 1");
            getNetState().setNetType(NetType.TYPE_ETHERNET);
            return;
        } else if (isEthernetConnected() && (ethernetMode != EthernetMode.MODE_PPPOE)) {
            Logger.getLogger().i(TAG, "enter case 2");
            getNetState().setNetType(NetType.TYPE_ETHERNET);
            return;
        } else if (isWifiConnected()) {
            Logger.getLogger().i(TAG, "enter case 4");
            getNetState().setNetType(NetType.TYPE_WIFI);
            return;
        } else {
            Logger.getLogger().i(TAG, "enter case 5");
            getNetState().setNetType(NetType.TYPE_NONE);
        }
    }

    private void autoDetect() {
        mIpConfiguration = new IpConfiguration();
        mEthernetManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
        try {
            mNMService.setInterfaceDown("eth0");
        } catch (Exception e) {
            Logger.getLogger().e(TAG, "Failed to setInterfaceDown");
        }

        try {
            mNMService.clearInterfaceAddresses("eth0");
        } catch (Exception e) {
            Logger.getLogger().e(TAG, "Failed to clear addr or disable IPv6");

        }

        mIpConfiguration.setStaticIpConfiguration(null);
        mIpConfiguration.setIpAssignment(IpConfiguration.IpAssignment.DHCP);
        mEthernetManager.setConfiguration(mIpConfiguration);

        try {
            mNMService.setInterfaceDown("eth0");
        } catch (Exception e) {
            Logger.getLogger().e(TAG, "Failed to setInterfaceDown");
        }
    }


    private void restoreData() {
        String mode = null;
        String netmask = null;
        String gateway = null;
        String dns1 = null;
        String dns2 = null;

        mPreferences = getSharedPreferences("EthernetActvity", MODE_PRIVATE);
        mode = mPreferences.getString("MODE", null);
        if(null != mode) {
            if (mode.equals("auto")) {
                getNetState().setEthernetMode(EthernetMode.MODE_AUTO);
            } else if(mode.equals("man")) {
                getNetState().setEthernetMode(EthernetMode.MODE_MAN);
            }
        }
    }

    private boolean isEthernetConnected() {

        cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mEthernetState = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET).getState();
        if(mEthernetState == NetworkInfo.State.CONNECTED) {
            Logger.getLogger().i("Ethernet connect state = true");
            return true;
        }

        return false;
    }

    private boolean isWifiConnected() {

        cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mEthernetState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if(mEthernetState == NetworkInfo.State.CONNECTED) {
            Logger.getLogger().i("Wifi connect state = true");
            return true;
        }

        return false;
    }

    private String getEth0MacAddr() {
        try {
            return readLine(ETH0_MAC_ADDR);
        } catch (IOException e) {
            Logger.getLogger().i(TAG, "IO Exception when getting eth0 mac address " + e);
            e.printStackTrace();
            return "unavailable";
        }
    }

    private String getWlan0MacAddr() {
        try {
            return readLine(WLAN0_MAC_ADDR);
        } catch (IOException e) {
            Logger.getLogger().i(TAG, "IO Exception when getting eth0 mac address " + e);
            e.printStackTrace();
            return "unavailable";
        }
    }

    private String readLine(@NonNull String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    private Inet4Address getIPv4Address(@NonNull String text) {
        try {
            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
        } catch (IllegalArgumentException | ClassCastException e) {
            return null;
        }
    }

    private int toInteger(String address) {
        Matcher matcher = addressPattern.matcher(address);
        if (matcher.matches()) {
            return matchAddress(matcher);
        } else {
            throw new IllegalArgumentException("Could not parse [" + address + "]");
        }
    }

    private int matchAddress(Matcher matcher) {
        int addr = 0;
        for(int  i = 0; i <= 4; i++) {
            int n = (rangeCheck(Integer.parseInt(matcher.group(i)), 0, 255));
            while( (n & 128) == 128) {
                addr = addr + 1;
                i = i << 1;
            }
        }

        return addr;
    }

    private int rangeCheck(int value, int begin, int end) {
        if (value >= begin && value <= end)
            return value;
        throw new IllegalArgumentException("Value out of range: [" + value + "]");
    }


}
