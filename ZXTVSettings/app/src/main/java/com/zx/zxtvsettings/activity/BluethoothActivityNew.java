package com.zx.zxtvsettings.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.Logger;
import com.zx.zxtvsettings.adapter.MyBluetoothAdapterNew;
import com.zx.zxtvsettings.bluetooth.BluetoothCallback;
import com.zx.zxtvsettings.bluetooth.BluetoothDeviceFilter;
import com.zx.zxtvsettings.bluetooth.BluetoothEnabler;
import com.zx.zxtvsettings.bluetooth.CachedBluetoothDevice;
import com.zx.zxtvsettings.bluetooth.LocalBluetoothAdapter;
import com.zx.zxtvsettings.bluetooth.LocalBluetoothManager;
import com.zx.zxtvsettings.bluetooth.SwitchBar;
import com.zx.zxtvsettings.bluetooth.Utils;
import com.zx.zxtvsettings.bluetooth.view.BluetoothDeviceView;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * User: ShaudXiao
 * Date: 2016-08-22
 * Time: 15:15
 * Company: zx
 * Description:
 * FIXME
 */

public class BluethoothActivityNew extends BaseStatusBarActivity implements View.OnClickListener,
         BluetoothCallback, BluetoothDeviceView.IDeviceStateChangeListener {

    ImageView mBluetoothIvOpen;
    RelativeLayout mBluetoothRlOpen;
    ImageView mBluetoothIvDetection;
    RelativeLayout mBluetoothRlDetection;
    ImageView mBluetoothIvSearch;
    RelativeLayout mBluetoothRlSearch;
    RelativeLayout mBluetoothRlSetting;
    TextView mBluetoothTvPair;
    TextView mBluetoothTvPairName;
    TextView mBluetoothTvPairState;
    RelativeLayout mBluetoothRlPair1;
    RelativeLayout mBluetoothRlPair;
    TextView mBluetoothTvSearchDevice;
    ListView mBluetoothLvSearchDevice;
    RelativeLayout mBluetoothRlSearchDevice;

    LinearLayout mBluetoothLlSearchDevice;
    LinearLayout mBluetoothLlSearchDevicePired;

    LinearLayout mDeviceListGroup;

    TextView mEmptyView;

    private int pairPosition = -1;

    private Context context;

    private BluetoothAdapter bluetoothAdapter;
    private MyBluetoothAdapterNew mDeviceListAdapter;

    private List<Map<String, Object>> list;

    private BluetoothEnabler mBluetoothEnabler;
    private SwitchBar mSwitchBar;

    private static final String KEY_BT_DEVICE_LIST = "bt_device_list";
    private static final String KEY_BT_SCAN = "bt_scan";

    private BluetoothDeviceFilter.Filter mFilter;

    private BluetoothDevice mSelectedDevice;

    private LocalBluetoothAdapter mLocalAdapter;
    private LocalBluetoothManager mLocalManager;

    private boolean mInitiateDiscoverable;
    private boolean mInitialScanStarted;
    private boolean mAvailableDevicesCategoryIsPresent;

    private final WeakHashMap<CachedBluetoothDevice, BluetoothDeviceView> mDeviceViewMap =
            new WeakHashMap<CachedBluetoothDevice, BluetoothDeviceView>();


    private CachedBluetoothDevice mPiredDevice;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting_bluetooth;
    }

    @Override
    protected void setupViews() {

        initView();

        setupView();

    }

    @Override
    protected View getFirstViewFocusRequest() {
        return findViewById(R.id.bluetooth_rl_open);
    }

    private void initView() {

        mEmptyView = (TextView) findViewById(R.id.empty_view);

        mSwitchBar = (SwitchBar) findViewById(R.id.open_switchbar);

        mBluetoothIvOpen = (ImageView) findViewById(R.id.bluetooth_iv_open);
        mBluetoothRlOpen = (RelativeLayout) findViewById(R.id.bluetooth_rl_open);

        mBluetoothIvDetection = (ImageView) findViewById(R.id.bluetooth_iv_detection);
        mBluetoothRlDetection = (RelativeLayout) findViewById(R.id.bluetooth_rl_detection);
        mBluetoothIvSearch = (ImageView) findViewById(R.id.bluetooth_iv_search);
        mBluetoothRlSearch = (RelativeLayout) findViewById(R.id.bluetooth_rl_search);
        mBluetoothRlSetting = (RelativeLayout) findViewById(R.id.bluetooth_rl_setting);

        mBluetoothTvPair = (TextView) findViewById(R.id.bluetooth_tv_pair);
        mBluetoothTvPairName = (TextView) findViewById(R.id.bluetooth_tv_pair_name);
        mBluetoothTvPairState = (TextView) findViewById(R.id.bluetooth_tv_pair_state);
        mBluetoothRlPair1 = (RelativeLayout) findViewById(R.id.bluetooth_rl_pair1);
        mBluetoothRlPair = (RelativeLayout) findViewById(R.id.bluetooth_rl_pair);

        mBluetoothTvSearchDevice = (TextView) findViewById(R.id.bluetooth_tv_search_device);
        mBluetoothLvSearchDevice = (ListView) findViewById(R.id.bluetooth_lv_search_device);

        mBluetoothRlSearchDevice = (RelativeLayout) findViewById(R.id.bluetooth_rl_search_device);

        mBluetoothLlSearchDevice = (LinearLayout) findViewById(R.id.bluetooth_ll_search_device);
        mBluetoothLlSearchDevicePired = (LinearLayout) findViewById(R.id.bluetooth_ll_search_device_pired);

        mDeviceListGroup = mBluetoothLlSearchDevicePired;
    }

    @Override
    protected void initialized() {
        context = this;
        mInitiateDiscoverable = true;

        initBlutooth();

        Logger.getLogger().e("mLocalAdapter.enable() " + mLocalAdapter.enable());
//        if(mLocalAdapter.enable()) {
//            mBluetoothIvSearch.setVisibility(View.VISIBLE);
//            mBluetoothTvPair.setVisibility(View.VISIBLE);
//            mBluetoothRlPair.setVisibility(View.VISIBLE);
//            mBluetoothLvSearchDevice.setVisibility(View.VISIBLE);
//            mBluetoothRlSearchDevice.setVisibility(View.VISIBLE);
//        } else {
//            mBluetoothIvSearch.setVisibility(View.GONE);
//            mBluetoothTvPair.setVisibility(View.GONE);
//            mBluetoothRlPair.setVisibility(View.GONE);
//            mBluetoothTvSearchDevice.setVisibility(View.GONE);
//            mBluetoothRlSearchDevice.setVisibility(View.GONE);
//        }
    }

    private void initBlutooth() {
        mLocalManager = LocalBluetoothManager.getInstance(this);
        if (mLocalManager == null) {
            Logger.getLogger().e(TAG, "Bluetooth is not supported on this device");
            return;
        }
        mLocalAdapter = mLocalManager.getBluetoothAdapter();

        mBluetoothEnabler = new BluetoothEnabler(this, mSwitchBar);

        setFilter(BluetoothDeviceFilter.ALL_FILTER);
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mBluetoothEnabler != null) {
            mBluetoothEnabler.resume(this);
        }

        if (mLocalManager == null) return;

        mInitiateDiscoverable = true;

        mLocalManager.setForegroundActivity(this);
        mLocalManager.getEventManager().registerCallback(this);

        if (mLocalAdapter != null) {
            updateContent(mLocalAdapter.getBluetoothState());
        }

        updateProgressUi(mLocalAdapter.isDiscovering());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLocalManager == null) {
            return;
        }

        removeAllDevices();
        mLocalManager.setForegroundActivity(null);
        mLocalManager.getEventManager().unregisterCallback(this);

        mBluetoothEnabler.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(searchReceiver);
    }

    private void resisgerReciver() {
        IntentFilter intent = new IntentFilter();
        // 用BroadcastReceiver来取得搜索结果
        intent.addAction(BluetoothDevice.ACTION_FOUND);
        //每当扫描模式变化的时候，应用程序可以为通过ACTION_SCAN_MODE_CHANGED值来监听全局的消息通知。
        // 比如，当设备停止被搜寻以后，该消息可以被系统通知給应用程序。
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        //每当蓝牙模块被打开或者关闭，应用程序可以为通过ACTION_STATE_CHANGED值来监听全局的消息通知。
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(searchReceiver, intent);
    }

    private void setupView() {
        findViewById(R.id.bluetooth_rl_open).setOnClickListener(this);
        findViewById(R.id.bluetooth_rl_pair1).setOnClickListener(this);
        findViewById(R.id.bluetooth_rl_search).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bluetooth_rl_open:
                if(mBluetoothEnabler.isChecked()) {
                    mBluetoothEnabler.setChecked(false);
                } else {
                    mBluetoothEnabler.setChecked(true);
                }
                break;
            case R.id.bluetooth_rl_pair1:

                break;
            case R.id.bluetooth_rl_search:
                if (mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON) {
                    startScanning();
                }

                break;
        }

        if (view instanceof BluetoothDeviceView) {
            BluetoothDeviceView deviceView = (BluetoothDeviceView) view;
            deviceView.onClicked();
        }
    }

    private BroadcastReceiver searchReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = null;
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null) {
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("name", device.getName());
                    map.put("type", device.getBluetoothClass().getDeviceClass());
                    map.put("device", device);
                    if (list.indexOf(map) == -1) {// 防止重复添加
                        list.add(map);
//                        mDeviceListAdapter = new MyBluetoothAdapter(context, list);
//                        mBluetoothLvSearchDevice.setAdapter(mDeviceListAdapter);
                    }
                }
            } else if (device != null && device.getBondState() == BluetoothDevice.BOND_BONDING) {
                showToastShort(getString(R.string.bluetooth_connectting));
            } else if (device != null && device.getBondState() == BluetoothDevice.BOND_BONDED) {
                mBluetoothTvPairName.setText(device.getName());
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).get("device").equals(device)) {
                        pairPosition = i;
                        list.remove(i);
                        mDeviceListAdapter.notifyDataSetChanged();
                    }
                }
                showToastShort(getString(R.string.bluetooth_connected));
            }
        }
    };

    private void setFilter(BluetoothDeviceFilter.Filter filter) {
        mFilter = filter;
    }


    private void removeAllDevices() {
        mLocalAdapter.stopScanning();
        mDeviceViewMap.clear();
        mDeviceListGroup.removeAllViews();
    }

    private void addCachedDevices() {
        Collection<CachedBluetoothDevice> cachedDevices =
                mLocalManager.getCachedDeviceManager().getCachedDevicesCopy();
        for (CachedBluetoothDevice cachedDevice : cachedDevices) {
            onDeviceAdded(cachedDevice);
            Logger.getLogger().i(cachedDevice.getName());
        }
    }

    @Override
    public void onBluetoothStateChanged(int bluetoothState) {
        if (bluetoothState == BluetoothAdapter.STATE_OFF) {
            updateProgressUi(false);
        }

        updateContent(bluetoothState);
    }

    @Override
    public void onScanningStateChanged(boolean started) {
        updateProgressUi(started);
    }

    @Override
    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        // Prevent updates while the list shows one of the state messages
        if (mDeviceViewMap.get(cachedDevice) != null) {
            return;
        }

        if (mLocalAdapter.getBluetoothState() != BluetoothAdapter.STATE_ON) return;

        Logger.getLogger().i(mFilter.matches(cachedDevice.getDevice()) + " " + cachedDevice.getName());
        if (mFilter.matches(cachedDevice.getDevice())) {
            createDeviceView(cachedDevice);
        }
    }

    @Override
    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        Logger.getLogger().i("********onDeviceDeleted*******");
    }

    @Override
    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        Logger.getLogger().i("********onDeviceBondStateChanged*******");
        removeAllDevices();
        updateContent(mLocalAdapter.getBluetoothState());
    }

    private void startScanning() {
        if (!mAvailableDevicesCategoryIsPresent) {
            mBluetoothRlSearchDevice.setVisibility(View.VISIBLE);
            mAvailableDevicesCategoryIsPresent = true;
        }

        if (mBluetoothLlSearchDevice != null) {
            setDeviceListGroup(mBluetoothLlSearchDevice);
            removeAllDevices();
        }
        mLocalManager.getCachedDeviceManager().clearNonBondedDevices();

        mInitialScanStarted = true;
        mLocalAdapter.startScanning(true);
    }


    private void updateContent(int bluetoothState) {
        int messageId = 0;
        Logger.getLogger().e("**** updateContent **** bluetoothState = " + bluetoothState);
        switch (bluetoothState) {
            case BluetoothAdapter.STATE_ON:
                removeAllDevices();
                mDeviceViewMap.clear();
                //已配对过的
                // Paired devices category
//                mBluetoothTvPair.setVisibility(View.VISIBLE);
//                mBluetoothRlPair.setVisibility(View.VISIBLE);
                Logger.getLogger().e("********** BluetoothDeviceFilter.BONDED_DEVICE_FILTER ");

                mBluetoothLlSearchDevicePired.removeAllViews();
                addDeviceCategory(mBluetoothLlSearchDevicePired,
                        BluetoothDeviceFilter.BONDED_DEVICE_FILTER, true);

                mBluetoothLlSearchDevice.removeAllViews();
                addDeviceCategory(mBluetoothLlSearchDevice,
                        BluetoothDeviceFilter.UNBONDED_DEVICE_FILTER, mInitialScanStarted);
                Logger.getLogger().i("size + " + mBluetoothLlSearchDevicePired.getChildCount());
                mBluetoothIvOpen.setBackgroundResource(R.drawable.switch_on);

                mBluetoothLvSearchDevice.setVisibility(View.VISIBLE);
                mBluetoothRlSearchDevice.setVisibility(View.VISIBLE);
                mBluetoothTvSearchDevice.setVisibility(View.VISIBLE);

                mBluetoothRlSearch.setVisibility(View.VISIBLE);
                mBluetoothIvSearch.setVisibility(View.VISIBLE);

                if (!mInitialScanStarted) {
                    Logger.getLogger().e("start scan");
                    startScanning();
                }

                //未打开蓝牙的提示
                mEmptyView.setText(getString(
                        R.string.bluetooth_is_visible_message, mLocalAdapter.getName()));

                if (mInitiateDiscoverable) {
                    // Make the device visible to other devices.
                    mLocalAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
                    mInitiateDiscoverable = false;
                }
                return; // not break

            case BluetoothAdapter.STATE_TURNING_OFF:
                messageId = R.string.bluetooth_turning_off;
                mBluetoothLlSearchDevice.removeAllViews();
                mBluetoothLlSearchDevicePired.removeAllViews();
                break;

            case BluetoothAdapter.STATE_OFF:
                messageId = R.string.bluetooth_empty_list_bluetooth_off;
                mBluetoothRlSearch.setVisibility(View.GONE);
                mBluetoothIvSearch.setVisibility(View.GONE);
                mBluetoothTvPair.setVisibility(View.GONE);
                mBluetoothRlPair.setVisibility(View.GONE);
                mBluetoothTvSearchDevice.setVisibility(View.GONE);
                mBluetoothRlSearchDevice.setVisibility(View.GONE);
                break;

            case BluetoothAdapter.STATE_TURNING_ON:
                messageId = R.string.bluetooth_turning_on;
                mInitialScanStarted = false;
                break;
        }

//        setDeviceListGroup(preferenceScreen);
        removeAllDevices();
        mEmptyView.setText(messageId);
    }

    private void addDeviceCategory(LinearLayout group, BluetoothDeviceFilter.Filter filter, boolean addCachedDevices) {
        Logger.getLogger().i("********* addDeviceCategory " + addCachedDevices);
        setFilter(filter);
        setDeviceListGroup(group);
        if (addCachedDevices) {
            addCachedDevices();
        }
    }

    private void updateProgressUi(boolean state) {
        Animation rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate);
        if (state) {
            mBluetoothIvSearch.startAnimation(rotateAnimation);
        } else {
            mBluetoothIvSearch.clearAnimation();
        }
    }

    void createDeviceView(CachedBluetoothDevice cachedDevice) {
        if (mDeviceListGroup == null) {
            Logger.getLogger().i("Trying to create a device view before the list group/category "
                    + "exists!");
            return;
        }

        BluetoothDeviceView deviceView = new BluetoothDeviceView(this, cachedDevice);
        deviceView.setOnClickListener(this);
        mDeviceListGroup.addView(deviceView);
        Logger.getLogger().e("createDeviceView mDeviceListGroup = " + mDeviceListGroup);
        mDeviceViewMap.put(cachedDevice, deviceView);

        deviceView.setDeviceStateChangeListener(this);
    }

    private void setDeviceListGroup(LinearLayout group) {
        Logger.getLogger().e("setDeviceListGroup mDeviceListGroup = " + mDeviceListGroup);
        mDeviceListGroup = group;

    }


    @Override
    public void onDeviceStateChange(CachedBluetoothDevice device) {
        mPiredDevice = device;
        int sumaryResId = Utils.getConnectionSummary(device);
        if(sumaryResId != 0) {
            mBluetoothRlPair.setVisibility(View.VISIBLE);
            mBluetoothTvPair.setVisibility(View.VISIBLE);
            mBluetoothTvPairName.setText(device.getName());
            mBluetoothTvPairState.setText(sumaryResId);
        } else {
            mBluetoothRlPair.setVisibility(View.GONE);
            mBluetoothTvPair.setVisibility(View.GONE);
        }
    }
}
