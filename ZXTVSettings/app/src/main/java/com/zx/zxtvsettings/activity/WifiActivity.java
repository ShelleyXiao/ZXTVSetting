package com.zx.zxtvsettings.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.zx.zxtvsettings.GlobalConstants;
import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.Logger;
import com.zx.zxtvsettings.adapter.WifiListAdpter;
import com.zx.zxtvsettings.wifi.Wifi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * User: ShaudXiao
 * Date: 2016-08-19
 * Time: 15:17
 * Company: zx
 * Description:
 * FIXME
 */

public class WifiActivity extends BaseStatusBarActivity implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemClickListener {


    TextView mWifisetingTittle;
    Switch mWifiSwitch;
    TextView mWifiStatedispaly;
    ImageView mWifiArrowtop;
    ListView mWifiListview;
    ImageView mWifiArrowbottom;
    ProgressBar mWifiScanProgress;
    TextView mWifiConnectID;

    private final int WIFI_OPEN_FINISH = 1;//开启完成
    private final int WIFI_FOUND_FINISH = 0;//查找完成
    private final int WIFI_SCAN = 2;//wifi扫描
    private final int WIFI_CLOSE = 3;//关闭wifi
    private final int WIFI_INFO = 4;
    private final int WIFI_STATE_INIT = 5;//加载页面


    private WifiManager mWifiManager;
    private List<ScanResult> mScanResults = new ArrayList<ScanResult>();

    private WifiListAdpter mWififListAdapter;
    private  IntentFilter mIntentFilter;

    private boolean mStateMachineEvent;
    private boolean mListeningToOnSwitchChange = false;
    private AtomicBoolean mConnected = new AtomicBoolean(false);

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WIFI_STATE_INIT:
                    int wifiState = mWifiManager.getWifiState();
                    if (wifiState == WifiManager.WIFI_STATE_DISABLED) {  //wifi不可用啊
                        mWifiStatedispaly.setText(R.string.wifi_state_closed);
                        mWifiScanProgress.setVisibility(View.GONE);
                        mWifiListview.setVisibility(View.GONE);
                    } else if (wifiState == WifiManager.WIFI_STATE_UNKNOWN) {//wifi 状态未知
                        mWifiStatedispaly.setText(R.string.wifi_state_unknowed);
                    } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {//OK 可用
                        mWifiSwitch.setChecked(true);

                        if (mWifiManager.isWifiEnabled()) {
                            showToastLong(getString(R.string.wifi_state_opened));

                        } else {
                            showToastLong(getString(R.string.wifi_open_msg));
                        }
                    }
                    break;
                case WIFI_SCAN:
                    Wifi.startScan(mWifiManager);
                    List<ScanResult> results = mWifiManager.getScanResults();
                    mWifiStatedispaly.setText(R.string.wifi_scan_msg);
                    if(results == null || results.size() == 0) {
                        mHandler.sendEmptyMessageDelayed(WIFI_SCAN, 1000);
                    } else {
                        mScanResults.clear();
                        mScanResults.addAll(results);
                        Wifi.sortByLevel(results);
                        mWififListAdapter.notifyDataSetChanged();

                        mWifiStatedispaly.setText(R.string.wifi_nearby);
                        mWifiScanProgress.setProgress(View.GONE);
                        mWifiListview.setVisibility(View.VISIBLE);
                    }

                    break;
            }

            super.handleMessage(msg);
        }
    };


    @Override
    protected int getLayoutId() {
        return R.layout.activity_wifi_setting;
    }

    @Override
    protected void setupViews() {

        mWifisetingTittle = (TextView) findViewById(R.id.wifiseting_tittle);
        mWifiSwitch = (Switch) findViewById(R.id.wifi_switch);
        mWifiStatedispaly = (TextView) findViewById(R.id.wifi_statedispaly);
        mWifiArrowtop = (ImageView) findViewById(R.id.wifi_arrowtop);
        mWifiListview = (ListView) findViewById(R.id.wifi_listview);
        mWifiArrowbottom = (ImageView) findViewById(R.id.wifi_arrowbottom);
        mWifiScanProgress = (ProgressBar) findViewById(R.id.wifi_scan_progress);
        mWifiConnectID = (TextView) findViewById(R.id.wifiseting_connect_name);

        mWifiSwitch.setOnCheckedChangeListener(this);

        mWififListAdapter = new WifiListAdpter(this, mScanResults);
        mWifiListview.setAdapter(mWififListAdapter);
        mWifiListview.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int postion, long l) {
                if (postion == 0) {
                    mWifiArrowtop.setVisibility(View.GONE);
                } else {
                    mWifiArrowtop.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mWifiListview.setOnItemClickListener(this);


    }

    @Override
    protected void initialized() {

        mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiver, mIntentFilter);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        setupSwitch();
        if(Wifi.isWifiEnabled(this)) {
            String name = Wifi.getConnectWifiName(mWifiManager);
            if(null != name) {
//                mWifiConnectID.setText(getString(R.string.wifi_connect_name, name));
            } else {
                mWifiConnectID.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        mHandler.sendEmptyMessageDelayed(WIFI_STATE_INIT, 200);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        Logger.getLogger().d("onCheckedChanged " + checked);
        if (compoundButton instanceof Switch) {
            if (checked) {
                mWifiStatedispaly.setText(R.string.wifi_scan_msg);
                mWifiScanProgress.setVisibility(View.VISIBLE);
//                mWifiListview.setVisibility(View.GONE);

                Wifi.openWifi(mWifiManager);
                Wifi.startScan(mWifiManager);

                mHandler.sendEmptyMessageDelayed(WIFI_SCAN, 1000);

            } else {
                Wifi.closeWifi(mWifiManager);

                mWifiListview.setVisibility(View.GONE);
                mWifiScanProgress.setVisibility(View.GONE);
                mWifiArrowtop.setVisibility(View.GONE);

                mWifiStatedispaly.setText(R.string.wifi_close_msg);

                mWifiConnectID.setVisibility(View.GONE);

            }
        }
    }

    public void setupSwitch() {
        final int state = mWifiManager.getWifiState();
        handleWifiStateChanged(state);
        if (!mListeningToOnSwitchChange) {
            mWifiSwitch.setOnCheckedChangeListener(this);
            mListeningToOnSwitchChange = true;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                Logger.getLogger().d("SCAN_RESULTS_AVAILABLE_ACTION ");
                List<ScanResult> temp = mWifiManager.getScanResults();
//                mScanResults = mWifiManager.getScanResults();
                mScanResults.clear();
                mScanResults.addAll(temp);

                mWifiScanProgress.setVisibility(View.GONE);
                mWifiStatedispaly.setText(R.string.wifi_nearby);
                mWifiListview.setVisibility(View.VISIBLE);

                mWififListAdapter.notifyDataSetChanged();

                if(mScanResults == null || mScanResults.size() == 0) {
                    //再次扫描
                    mWifiManager.startScan();
                }

                Logger.getLogger().d(mScanResults.size() + " " + mScanResults.get(0).SSID);
            } else  if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                handleWifiStateChanged(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                Logger.getLogger().d("SUPPLICANT_STATE_CHANGED_ACTION ");
                if (!mConnected.get()) {
                    handleStateChanged(WifiInfo.getDetailedStateOf((SupplicantState)
                            intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                Logger.getLogger().d("NETWORK_STATE_CHANGED_ACTION ");
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                        WifiManager.EXTRA_NETWORK_INFO);
                mConnected.set(info.isConnected());
                handleStateChanged(info.getDetailedState());
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        final ScanResult result = mScanResults.get(position);
        launchWifiConnecter(WifiActivity.this, result);
    }

    // 处理链接信息
    private void handleStateChanged(@SuppressWarnings("unused") NetworkInfo.DetailedState state) {
        // After the refactoring from a CheckBoxPreference to a Switch, this method is useless since
        // there is nowhere to display a summary.
        // This code is kept in case a future change re-introduces an associated text.

        // WifiInfo is valid if and only if Wi-Fi is enabled.
        // Here we use the state of the switch as an optimization.
        if (state != null && mWifiSwitch.isChecked()) {
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (info != null) {
                //setSummary(Summary.get(mContext, info.getSSID(), state));

                mWifiConnectID.setText(getString(R.string.wifi_connect_name, info.getSSID()));
                mWifiConnectID.setVisibility(View.VISIBLE);
            } else {
                Logger.getLogger().e(" dddddddddddddddddd  disconect******");
            }
        } else {

        }

    }

    private void handleWifiStateChanged(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                mWifiSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                setSwitchChecked(true);
                mWifiSwitch.setEnabled(true);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                mWifiSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                setSwitchChecked(false);
                mWifiSwitch.setEnabled(true);
                break;
            default:
                setSwitchChecked(false);
                mWifiSwitch.setEnabled(true);
        }
    }

    private void setSwitchChecked(boolean checked) {
        mStateMachineEvent = true;
        mWifiSwitch.setChecked(checked);
        mStateMachineEvent = false;
    }

    private static void launchWifiConnecter(final Activity activity, final ScanResult hotspot) {
        final Intent intent = new Intent(GlobalConstants.WIFI_CONECT_ACTION);
        intent.putExtra(GlobalConstants.WIFI_CONECT_ACTION_EXTRA, hotspot);
        try {
            activity.startActivity(intent);
        } catch(ActivityNotFoundException e) {
            Toast.makeText(activity, "Wifi Connecter is not installed.", Toast.LENGTH_LONG).show();
        }
    }


}
