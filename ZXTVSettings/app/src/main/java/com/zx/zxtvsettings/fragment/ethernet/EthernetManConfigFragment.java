package com.zx.zxtvsettings.fragment.ethernet;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.Logger;
import com.zx.zxtvsettings.activity.EthernetActvity;
import com.zx.zxtvsettings.fragment.BaseFragment;
import com.zx.zxtvsettings.fragment.ethernet.mode.DataID;
import com.zx.zxtvsettings.fragment.ethernet.mode.NetData;

import java.util.HashMap;

public class EthernetManConfigFragment extends BaseFragment implements View.OnClickListener, View.OnKeyListener {
	private static final String TAG = "EthernetManConfigFragment";
	private EthernetActvity mEthernetActvity = null;
	private Button mBtn_Finish = null;
	private Button mBtn_LastStep = null;
	private SharedPreferences mPreferences;
	private SharedPreferences.Editor mEditor;

	String mIpAddress;
	String mMaskAddress;
	String mGatewayAddress;
	String mDns1Address;
	String mDns2Address;

	enum EditTextID {
		ethernet_ipconfig1_id, ethernet_ipconfig2_id,
        ethernet_ipconfig3_id, ethernet_ipconfig4_id,
        ethernet_maskconfig1_id, ethernet_maskconfig2_id,
        ethernet_maskconfig3_id, ethernet_maskconfig4_id,
        ethernet_gateconfig1_id, ethernet_gateconfig2_id,
        ethernet_gateconfig3_id, ethernet_gateconfig4_id,
        ethernet_dns1config1_id, ethernet_dns1config2_id,
        ethernet_dns1config3_id, ethernet_dns1config4_id,
        ethernet_dns2config1_id, ethernet_dns2config2_id,
        ethernet_dns2config3_id, ethernet_dns2config4_id,
	};

	private HashMap<EditTextID, EditText> mHashMap = new HashMap<EditTextID, EditText>(20);


	@Override
	protected void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_ethernetmanconfig);

		mEthernetActvity = (EthernetActvity) getActivity();
		mBtn_Finish = (Button) findViewById(R.id.ethernetconfig_finish_id);
		mBtn_LastStep = (Button) findViewById(R.id.ethernetconfig_back_id);
		mBtn_Finish.setOnClickListener(this);
		mBtn_LastStep.setOnClickListener(this);
		mBtn_Finish.setOnKeyListener(this);
		mBtn_LastStep.setOnKeyListener(this);

		getFocus();
		initHashMap();
		initView();
	}

	private void initHashMap() {
		mHashMap.clear();
		if (mHashMap.isEmpty()) {
			mHashMap.put(EditTextID.ethernet_ipconfig1_id, (EditText) findViewById(R.id.ethernet_ipconfig1_id));
			mHashMap.put(EditTextID.ethernet_ipconfig2_id, (EditText) findViewById(R.id.ethernet_ipconfig2_id));
			mHashMap.put(EditTextID.ethernet_ipconfig3_id, (EditText) findViewById(R.id.ethernet_ipconfig3_id));
			mHashMap.put(EditTextID.ethernet_ipconfig4_id, (EditText) findViewById(R.id.ethernet_ipconfig4_id));
			mHashMap.put(EditTextID.ethernet_maskconfig1_id,
					(EditText) findViewById(R.id.ethernet_maskconfig1_id));
			mHashMap.put(EditTextID.ethernet_maskconfig2_id,
					(EditText) findViewById(R.id.ethernet_maskconfig2_id));
			mHashMap.put(EditTextID.ethernet_maskconfig3_id,
					(EditText) findViewById(R.id.ethernet_maskconfig3_id));
			mHashMap.put(EditTextID.ethernet_maskconfig4_id,
					(EditText) findViewById(R.id.ethernet_maskconfig4_id));
			mHashMap.put(EditTextID.ethernet_gateconfig1_id,
					(EditText) findViewById(R.id.ethernet_gateconfig1_id));
			mHashMap.put(EditTextID.ethernet_gateconfig2_id,
					(EditText) findViewById(R.id.ethernet_gateconfig2_id));
			mHashMap.put(EditTextID.ethernet_gateconfig3_id,
					(EditText) findViewById(R.id.ethernet_gateconfig3_id));
			mHashMap.put(EditTextID.ethernet_gateconfig4_id,
					(EditText) findViewById(R.id.ethernet_gateconfig4_id));
			mHashMap.put(EditTextID.ethernet_dns1config1_id,
					(EditText) findViewById(R.id.ethernet_dns1config1_id));
			mHashMap.put(EditTextID.ethernet_dns1config2_id,
					(EditText) findViewById(R.id.ethernet_dns1config2_id));
			mHashMap.put(EditTextID.ethernet_dns1config3_id,
					(EditText) findViewById(R.id.ethernet_dns1config3_id));
			mHashMap.put(EditTextID.ethernet_dns1config4_id,
					(EditText) findViewById(R.id.ethernet_dns1config4_id));
			mHashMap.put(EditTextID.ethernet_dns2config1_id,
					(EditText) findViewById(R.id.ethernet_dns2config1_id));
			mHashMap.put(EditTextID.ethernet_dns2config2_id,
					(EditText) findViewById(R.id.ethernet_dns2config2_id));
			mHashMap.put(EditTextID.ethernet_dns2config3_id,
					(EditText) findViewById(R.id.ethernet_dns2config3_id));
			mHashMap.put(EditTextID.ethernet_dns2config4_id,
					(EditText) findViewById(R.id.ethernet_dns2config4_id));
			mHashMap.get(EditTextID.ethernet_ipconfig1_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_ipconfig2_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_ipconfig3_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_ipconfig4_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_gateconfig1_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_gateconfig2_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_gateconfig3_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_gateconfig4_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_maskconfig1_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_maskconfig2_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_maskconfig3_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_maskconfig4_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_dns1config1_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_dns1config2_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_dns1config3_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_dns1config4_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_dns2config1_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_dns2config2_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_dns2config3_id).setOnKeyListener(this);
			mHashMap.get(EditTextID.ethernet_dns2config4_id).setOnKeyListener(this);

		}
	}

	private void initView() {
		String[] iparray = mEthernetActvity.getNetState().getNetStateString(DataID.sETHERNET_IP_ID).split("\\.");
		String[] maskarray = mEthernetActvity.getNetState().getNetStateString(DataID.sETHERNET_MASK_ID).split("\\.");
		String[] gatewayarray = mEthernetActvity.getNetState().getNetStateString(DataID.sETHERNET_GATE_ID)
				.split("\\.");
		String[] dns1array = mEthernetActvity.getNetState().getNetStateString(DataID.sETHERNET_DNS1_ID).split("\\.");
		String[] dns2array = mEthernetActvity.getNetState().getNetStateString(DataID.sETHERNET_DNS2_ID).split("\\.");
		mHashMap.get(EditTextID.ethernet_ipconfig1_id).setText(iparray[0]);
		mHashMap.get(EditTextID.ethernet_ipconfig2_id).setText(iparray[1]);
		mHashMap.get(EditTextID.ethernet_ipconfig3_id).setText(iparray[2]);
		mHashMap.get(EditTextID.ethernet_ipconfig4_id).setText(iparray[3]);
		mHashMap.get(EditTextID.ethernet_maskconfig1_id).setText(maskarray[0]);
		mHashMap.get(EditTextID.ethernet_maskconfig2_id).setText(maskarray[1]);
		mHashMap.get(EditTextID.ethernet_maskconfig3_id).setText(maskarray[2]);
		mHashMap.get(EditTextID.ethernet_maskconfig4_id).setText(maskarray[3]);
		mHashMap.get(EditTextID.ethernet_gateconfig1_id).setText(gatewayarray[0]);
		mHashMap.get(EditTextID.ethernet_gateconfig2_id).setText(gatewayarray[1]);
		mHashMap.get(EditTextID.ethernet_gateconfig3_id).setText(gatewayarray[2]);
		mHashMap.get(EditTextID.ethernet_gateconfig4_id).setText(gatewayarray[3]);
		mHashMap.get(EditTextID.ethernet_dns1config1_id).setText(dns1array[0]);
		mHashMap.get(EditTextID.ethernet_dns1config2_id).setText(dns1array[1]);
		mHashMap.get(EditTextID.ethernet_dns1config3_id).setText(dns1array[2]);
		mHashMap.get(EditTextID.ethernet_dns1config4_id).setText(dns1array[3]);
		mHashMap.get(EditTextID.ethernet_dns2config1_id).setText(dns2array[0]);
		mHashMap.get(EditTextID.ethernet_dns2config2_id).setText(dns2array[1]);
		mHashMap.get(EditTextID.ethernet_dns2config3_id).setText(dns2array[2]);
		mHashMap.get(EditTextID.ethernet_dns2config4_id).setText(dns2array[3]);
	}

	private void getconfigdate() {
		String[] iparray = new String[4];
		String[] maskarray = new String[4];
		String[] gatewayarray = new String[4];
		String[] dns1array = new String[4];
		String[] dns2array = new String[4];
		iparray[0] = mHashMap.get(EditTextID.ethernet_ipconfig1_id).getText().toString();
		iparray[1] = mHashMap.get(EditTextID.ethernet_ipconfig2_id).getText().toString();
		iparray[2] = mHashMap.get(EditTextID.ethernet_ipconfig3_id).getText().toString();
		iparray[3] = mHashMap.get(EditTextID.ethernet_ipconfig4_id).getText().toString();
		maskarray[0] = mHashMap.get(EditTextID.ethernet_maskconfig1_id).getText().toString();
		maskarray[1] = mHashMap.get(EditTextID.ethernet_maskconfig2_id).getText().toString();
		maskarray[2] = mHashMap.get(EditTextID.ethernet_maskconfig3_id).getText().toString();
		maskarray[3] = mHashMap.get(EditTextID.ethernet_maskconfig4_id).getText().toString();
		gatewayarray[0] = mHashMap.get(EditTextID.ethernet_gateconfig1_id).getText().toString();
		gatewayarray[1] = mHashMap.get(EditTextID.ethernet_gateconfig2_id).getText().toString();
		gatewayarray[2] = mHashMap.get(EditTextID.ethernet_gateconfig3_id).getText().toString();
		gatewayarray[3] = mHashMap.get(EditTextID.ethernet_gateconfig4_id).getText().toString();
		dns1array[0] = mHashMap.get(EditTextID.ethernet_dns1config1_id).getText().toString();
		dns1array[1] = mHashMap.get(EditTextID.ethernet_dns1config2_id).getText().toString();
		dns1array[2] = mHashMap.get(EditTextID.ethernet_dns1config3_id).getText().toString();
		dns1array[3] = mHashMap.get(EditTextID.ethernet_dns1config4_id).getText().toString();
		dns2array[0] = mHashMap.get(EditTextID.ethernet_dns2config1_id).getText().toString();
		dns2array[1] = mHashMap.get(EditTextID.ethernet_dns2config2_id).getText().toString();
		dns2array[2] = mHashMap.get(EditTextID.ethernet_dns2config3_id).getText().toString();
		dns2array[3] = mHashMap.get(EditTextID.ethernet_dns2config4_id).getText().toString();

		mIpAddress = iparray[0] + "." + iparray[1] + "." + iparray[2] + "." + iparray[3];
		mMaskAddress = maskarray[0] + "." + maskarray[1] + "." + maskarray[2] + "." + maskarray[3];
		mGatewayAddress = gatewayarray[0] + "." + gatewayarray[1] + "." + gatewayarray[2] + "." + gatewayarray[3];
		mDns1Address = dns1array[0] + "." + dns1array[1] + "." + dns1array[2] + "." + dns1array[3];
		mDns2Address = dns2array[0] + "." + dns2array[1] + "." + dns2array[2] + "." + dns2array[3];

	}

	private boolean checkconfigdata() {
		boolean result = true;
		if (!isIpAddress(mIpAddress)) {
			Toast.makeText(getActivity(), "IP Address Wrong, please check! ", Toast.LENGTH_LONG).show();
			result = false;
		}
		if (!isIpAddress(mMaskAddress)) {
			Toast.makeText(getActivity(), "MASK Address Wrong, please check! ", Toast.LENGTH_LONG).show();
			result = false;
		}
		if (!isIpAddress(mGatewayAddress)) {
			Toast.makeText(getActivity(), "GateWay Address Wrong, please check! ", Toast.LENGTH_LONG).show();
			result = false;
		}
		if (!isIpAddress(mDns1Address)) {
			Toast.makeText(getActivity(), "DNS1 Address Wrong, please check! ", Toast.LENGTH_LONG).show();
			result = false;
		}
		if (!isIpAddress(mDns2Address)) {
			Toast.makeText(getActivity(), "DNS2 Address Wrong, please check! ", Toast.LENGTH_LONG).show();
			result = false;
		}
		return result;

	}

	private boolean isIpAddress(String value) {

		int start = 0;
		int end = value.indexOf('.');
		int numBlocks = 0;

		while (start < value.length()) {

			if (end == -1) {
				end = value.length();
			}

			try {
				int block = Integer.parseInt(value.substring(start, end));
				if ((block > 255) || (block < 0)) {
					return false;
				}
			} catch (NumberFormatException e) {
				return false;
			}

			numBlocks++;

			start = end + 1;
			end = value.indexOf('.', start);
		}

		return numBlocks == 4;
	}

	private void getFocus() {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(NetData.EthernetManConfig_Tag) != null) {
			Logger.getLogger().i(TAG, "  getArguments()   " + getArguments());
			if (getArguments() != null) {
				if (getArguments().getBoolean(NetData.EthernetManConfig_KeyId)) {
					mBtn_Finish.setFocusable(true);
					mBtn_Finish.requestFocus();
				}
			}
		}
	}

	/**
	 * Now only handles KEYCODE_ENTER and KEYCODE_NUMPAD_ENTER key event.
	 */
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (v == null) {
			return false;
		}
		boolean handled = false;
		Logger.getLogger().i(TAG, "view = " + v);
		String str;
		switch (keyCode) {
		case KeyEvent.KEYCODE_POUND:
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				if (v instanceof EditText) {
					Integer index = ((EditText) v).getSelectionStart();
					Editable editable = ((EditText) v).getText();
					if (index > 0) {
						editable.delete(index - 1, index);
					}
					handled = true;
				}
			}
			break;
		case KeyEvent.KEYCODE_BACK:
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				detect_back();
				handled = true;
			}
			break;
		default:
			break;

		}
		return handled;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ethernetconfig_back_id:
			Logger.getLogger().i(TAG, "enter onClick ethernetconfig_back_id ");
			detect_back();
			break;
		case R.id.ethernetconfig_finish_id:
			Logger.getLogger().i(TAG, "enter onClick ethernetconfig_finish_id ");
			onClick_finish_button();
			break;
		default:
			break;
		}
	}

	private void storemaninfo() {
		mPreferences = mEthernetActvity.getSharedPreferences("networkactivity", mEthernetActvity.MODE_PRIVATE);
		mEditor = mPreferences.edit();
		mEditor.putString("man_ip", mIpAddress);
		mEditor.putString("man_mask", mMaskAddress);
		mEditor.putString("man_gate", mGatewayAddress);
		mEditor.putString("man_dns1", mDns1Address);
		mEditor.putString("man_dns2", mDns2Address);
		mEditor.commit();
	}

	private void onClick_finish_button() {
		getconfigdate();
		if (checkconfigdata()) {
			mEthernetActvity.getNetState().setNetStateString(DataID.sETHERNET_IP_ID, mIpAddress);
			mEthernetActvity.getNetState().setNetStateString(DataID.sETHERNET_MASK_ID, mMaskAddress);
			mEthernetActvity.getNetState().setNetStateString(DataID.sETHERNET_GATE_ID, mGatewayAddress);
			mEthernetActvity.getNetState().setNetStateString(DataID.sETHERNET_DNS1_ID, mDns1Address);
			mEthernetActvity.getNetState().setNetStateString(DataID.sETHERNET_DNS2_ID, mDns2Address);
			storemaninfo();
			jump_mandetect_fragment();
		} else {
			Logger.getLogger().e(TAG, "please check config data!!!");
		}
	}

	private void detect_back() {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		Fragment fragment = mEthernetActvity.getFragmentMap().get(NetData.Ethernet_KeyId);
		Bundle arguments = new Bundle();
		arguments.putBoolean(NetData.Ethernet_KeyId, true);
		fragment.setArguments(arguments);
		transaction.replace(R.id.network_framelayout, fragment, NetData.Ethernet_Tag).commit();
	}

	private void jump_mandetect_fragment() {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		Fragment fragment = mEthernetActvity.getFragmentMap().get(NetData.EthernetManDetect_KeyId);
		Bundle arguments = new Bundle();
		arguments.putBoolean(NetData.EthernetManDetect_KeyId, true);
		fragment.setArguments(arguments);
		transaction.replace(R.id.network_framelayout, fragment, NetData.EthernetManDetect_Tag).commit();
	}

}
