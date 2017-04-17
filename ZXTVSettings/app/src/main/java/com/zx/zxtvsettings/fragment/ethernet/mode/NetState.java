package com.zx.zxtvsettings.fragment.ethernet.mode;



public class NetState {
	private String mWifiIpAddress_Str;
	private String mWifiNetMask_Str;
	private String mWifiGateWay_Str;
	private String mWifiDNS1_Str;
	private String mWifiDNS2_Str;
	private Integer mWifiIpAddress_Int;
	private Integer mWifiNetMask_Int;
	private Integer mWifiGateWay_Int;
	private Integer mWifiDNS1_Int;
	private Integer mWifiDNS2_Int;
	private String mEthernetIpAddress_Str;
	private String mEthernetNetMask_Str;
	private String mEthernetGateWay_Str;
	private String mEthernetDNS1_Str;
	private String mEthernetDNS2_Str;
	private Integer mEthernetIpAddress_Int;
	private Integer mEthernetNetMask_Int;
	private Integer mEthernetGateWay_Int;
	private Integer mEthernetDNS1_Int;
	private Integer mEthernetDNS2_Int;
	private String mPppoeIpAddress_Str;
	private String mPppoeNetMask_Str;
	private String mPppoeGateWay_Str;
	private String mPppoeDNS1_Str;
	private String mPppoeDNS2_Str;
	private String mWifiMacAddress;
	private String mEthernetMacAddress;
	private String mPppoeMacAddress;

	private NetType mNetType;
	private boolean mWifiConnected;
	private boolean mEthernetConnected;
	private boolean mPppoeConnected;
	private EthernetMode mEthernetMode;

	public NetState() {
		mWifiIpAddress_Str = "192.168.1.108";
		mWifiNetMask_Str = "255.255.255.0";
		mWifiGateWay_Str = "192.168.1.1";
		mWifiDNS1_Str = "192.168.1.1";
		mWifiDNS1_Str = "192.168.1.1";
		mEthernetIpAddress_Str = "192.168.1.104";
		mEthernetNetMask_Str = "255.255.255.0";
		mEthernetGateWay_Str = "192.168.1.1";
		mEthernetDNS1_Str = "192.168.1.1";
		mEthernetDNS2_Str = "192.168.1.1";
		mWifiMacAddress = "08:57:00:af:4a:90";
		mEthernetMacAddress = "00:1b:fc:eb:92:fa";
		mPppoeIpAddress_Str = "192.168.1.104";
		mPppoeNetMask_Str = "255.255.255.0";
		mPppoeGateWay_Str = "192.168.1.1";
		mPppoeDNS1_Str = "192.168.1.1";
		mPppoeDNS2_Str = "192.168.1.1";
		mPppoeMacAddress = "00:1b:fc:eb:92:fa";

		mWifiIpAddress_Int = 0x6C01A8C0;
		mWifiNetMask_Int = 0x00FFFFFF;
		mWifiGateWay_Int = 0x0101A8C0;
		mWifiDNS1_Int = 0x0101A8C0;
		mWifiDNS2_Int = 0x0101A8C0;
		mEthernetIpAddress_Int = 0x6801A8C0;
		mEthernetNetMask_Int = 0x00FFFFFF;
		mEthernetGateWay_Int = 0x0101A8C0;
		mEthernetDNS1_Int = 0x0101A8C0;
		mEthernetDNS2_Int = 0x0101A8C0;

		mNetType = NetType.TYPE_NONE;
		mWifiConnected = false;
		mEthernetConnected = false;
		mPppoeConnected = false;
		mEthernetMode = EthernetMode.MODE_AUTO;

	}

	public Boolean isConnected() {
		return mWifiConnected || mEthernetConnected || mPppoeConnected;
	}

	public Boolean isWifiConnected() {
		return mWifiConnected;
	}

	public Boolean isEthernetConnected() {
		return mEthernetConnected;
	}

	public Boolean isPppoeConnected() {
		return mPppoeConnected;
	}

	public void setWifiConnectState(Boolean state) {
		mWifiConnected = state;
	}

	public void setEthernetConnectState(Boolean state) {
		mEthernetConnected = state;
	}

	public void setPppoeConnectState(Boolean state) {
		mPppoeConnected = state;
	}

	public void setEthernetMode(EthernetMode mode) {
		mEthernetMode = mode;
	}

	public EthernetMode getEthernetMode() {
		return mEthernetMode;
	}

	public NetType getNetType() {
		return mNetType;
	}

	public void setNetType(NetType type) {
		mNetType = type;
	}

	public Integer getNetState(DataID id) {
		Integer retval = -1;
		switch (id) {
		case iWIFI_IP_ID:
			retval = mWifiIpAddress_Int;
			break;
		case iWIFI_MASK_ID:
			retval = mWifiNetMask_Int;
			break;
		case iWIFI_GATE_ID:
			retval = mWifiGateWay_Int;
			break;
		case iWIFI_DNS1_ID:
			retval = mWifiDNS1_Int;
			break;
		case iWIFI_DNS2_ID:
			retval = mWifiDNS2_Int;
			break;
		case iETHERNET_IP_ID:
			retval = mEthernetIpAddress_Int;
			break;
		case iETHERNET_MASK_ID:
			retval = mEthernetNetMask_Int;
			break;
		case iETHERNET_GATE_ID:
			retval = mEthernetGateWay_Int;
			break;
		case iETHERNET_DNS1_ID:
			retval = mEthernetDNS1_Int;
			break;
		case iETHERNET_DNS2_ID:
			retval = mEthernetDNS2_Int;
			break;
		}
		return retval;

	}

	public String getNetStateString(DataID id) {
		String retval = "error";
		switch (id) {
		case sWIFI_IP_ID:
			retval = mWifiIpAddress_Str;
			break;
		case sWIFI_MASK_ID:
			retval = mWifiNetMask_Str;
			break;
		case sWIFI_GATE_ID:
			retval = mWifiGateWay_Str;
			break;
		case sWIFI_DNS1_ID:
			retval = mWifiDNS1_Str;
			break;
		case sWIFI_DNS2_ID:
			retval = mWifiDNS2_Str;
			break;
		case sWIFI_MAC_ID:
			retval = mWifiMacAddress;
			break;
		case sETHERNET_IP_ID:
			retval = mEthernetIpAddress_Str;
			break;
		case sETHERNET_MASK_ID:
			retval = mEthernetNetMask_Str;
			break;
		case sETHERNET_GATE_ID:
			retval = mEthernetGateWay_Str;
			break;
		case sETHERNET_DNS1_ID:
			retval = mEthernetDNS1_Str;
			break;
		case sETHERNET_DNS2_ID:
			retval = mEthernetDNS2_Str;
			break;
		case sETHERNET_MAC_ID:
			retval = mEthernetMacAddress;
			break;
		case sPPPOE_IP_ID:
			retval = mPppoeIpAddress_Str;
			break;
		case sPPPOE_MASK_ID:
			retval = mPppoeNetMask_Str;
			break;
		case sPPPOE_GATE_ID:
			retval = mPppoeGateWay_Str;
			break;
		case sPPPOE_DNS1_ID:
			retval = mPppoeDNS1_Str;
			break;
		case sPPPOE_DNS2_ID:
			retval = mPppoeDNS2_Str;
			break;
		case sPPPOE_MAC_ID:
			retval = mPppoeMacAddress;
			break;
		}
		return retval;

	}

	public void setNetState(DataID id, Integer value) {
		switch (id) {
		case iWIFI_IP_ID:
			mWifiIpAddress_Int = value;
			break;
		case iWIFI_MASK_ID:
			mWifiNetMask_Int = value;
			break;
		case iWIFI_GATE_ID:
			mWifiGateWay_Int = value;
			break;
		case iWIFI_DNS1_ID:
			mWifiDNS1_Int = value;
			break;
		case iWIFI_DNS2_ID:
			mWifiDNS2_Int = value;
			break;
		case iETHERNET_IP_ID:
			mEthernetIpAddress_Int = value;
			break;
		case iETHERNET_MASK_ID:
			mEthernetNetMask_Int = value;
			break;
		case iETHERNET_GATE_ID:
			mEthernetGateWay_Int = value;
			break;
		case iETHERNET_DNS1_ID:
			mEthernetDNS1_Int = value;
			break;
		case iETHERNET_DNS2_ID:
			mEthernetDNS2_Int = value;
			break;
		}
	}

	public void setNetStateString(DataID id, String value) {
		switch (id) {
		case sWIFI_IP_ID:
			mWifiIpAddress_Str = value;
			break;
		case sWIFI_MASK_ID:
			mWifiNetMask_Str = value;
			break;
		case sWIFI_GATE_ID:
			mWifiGateWay_Str = value;
			break;
		case sWIFI_DNS1_ID:
			mWifiDNS1_Str = value;
			break;
		case sWIFI_DNS2_ID:
			mWifiDNS2_Str = value;
			break;
		case sWIFI_MAC_ID:
			mWifiMacAddress = value;
			break;
		case sETHERNET_IP_ID:
			mEthernetIpAddress_Str = value;
			break;
		case sETHERNET_MASK_ID:
			mEthernetNetMask_Str = value;
			break;
		case sETHERNET_GATE_ID:
			mEthernetGateWay_Str = value;
			break;
		case sETHERNET_DNS1_ID:
			mEthernetDNS1_Str = value;
			break;
		case sETHERNET_DNS2_ID:
			mEthernetDNS2_Str = value;
			break;
		case sETHERNET_MAC_ID:
			mEthernetMacAddress = value;
			break;
		case sPPPOE_IP_ID:
			mPppoeIpAddress_Str = value;
			break;
		case sPPPOE_MASK_ID:
			mPppoeNetMask_Str = value;
			break;
		case sPPPOE_GATE_ID:
			mPppoeGateWay_Str = value;
			break;
		case sPPPOE_DNS1_ID:
			mPppoeDNS1_Str = value;
			break;
		case sPPPOE_DNS2_ID:
			mPppoeDNS2_Str = value;
			break;
		case sPPPOE_MAC_ID:
			mPppoeMacAddress = value;
			break;
		}
	}

}
