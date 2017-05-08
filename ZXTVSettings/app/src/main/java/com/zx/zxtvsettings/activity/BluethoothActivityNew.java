package com.zx.zxtvsettings.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceGroup;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.Logger;
import com.zx.zxtvsettings.Utils.Tools;
import com.zx.zxtvsettings.adapter.MyBluetoothAdapter;
import com.zx.zxtvsettings.bluetooth.BluetoothCallback;
import com.zx.zxtvsettings.bluetooth.BluetoothDeviceFilter;
import com.zx.zxtvsettings.bluetooth.BluetoothEnabler;
import com.zx.zxtvsettings.bluetooth.CachedBluetoothDevice;
import com.zx.zxtvsettings.bluetooth.LocalBluetoothAdapter;
import com.zx.zxtvsettings.bluetooth.LocalBluetoothManager;
import com.zx.zxtvsettings.bluetooth.SwitchBar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: ShaudXiao
 * Date: 2016-08-22
 * Time: 15:15
 * Company: zx
 * Description:
 * FIXME
 */

public class BluethoothActivityNew extends BaseStatusBarActivity implements View.OnClickListener
        , BluetoothCallback {

    ImageView mBluetoothIvOpen;
    RelativeLayout mBluetoothRlOpen;
    ImageView mBluetoothIvDetection;
    RelativeLayout mBluetoothRlDetection;
    ImageView mBluetoothIvSearch;
    RelativeLayout mBluetoothRlSearch;
    RelativeLayout mBluetoothRlSetting;
    TextView mBluetoothTvPair;
    TextView mBluetoothTvPairName;
    RelativeLayout mBluetoothRlPair1;
    RelativeLayout mBluetoothRlPair;
    TextView mBluetoothTvSearchDevice;
    ListView mBluetoothLvSearchDevice;
    RelativeLayout mBluetoothRlSearchDevice;

    TextView mEmptyView;

    private Set<BluetoothDevice> bondedDevices;
    private BluetoothDevice pairDevice;

    private boolean openFlag;
    private boolean detectionFlag;
    private int pairPosition = -1;

    private Context context;

    private BluetoothAdapter bluetoothAdapter;
    private MyBluetoothAdapter itemAdapter;

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

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting_bluetooth;
    }

    @Override
    protected void setupViews() {

        initView();

        setupView();

        mBluetoothLvSearchDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = (BluetoothDevice) list.get(i).get("device");
                device.createBond();
            }
        });
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
        mBluetoothRlPair1 = (RelativeLayout) findViewById(R.id.bluetooth_rl_pair1);
        mBluetoothRlPair = (RelativeLayout) findViewById(R.id.bluetooth_rl_pair);
        mBluetoothTvSearchDevice = (TextView) findViewById(R.id.bluetooth_tv_search_device);
        mBluetoothLvSearchDevice = (ListView) findViewById(R.id.bluetooth_lv_search_device);

        mBluetoothRlSearchDevice = (RelativeLayout) findViewById(R.id.bluetooth_rl_search_device);
    }

    @Override
    protected void initialized() {
        context = this;
        mInitiateDiscoverable = true;

        initBlutooth();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        list = new ArrayList<Map<String, Object>>();
        if (bluetoothAdapter.isEnabled()) {
            mBluetoothIvOpen.setBackgroundResource(R.drawable.switch_on);
            mBluetoothIvDetection.setBackgroundResource(R.drawable.switch_off);
            mBluetoothIvSearch.setVisibility(View.VISIBLE);
            mBluetoothTvPair.setVisibility(View.VISIBLE);
            mBluetoothRlPair.setVisibility(View.VISIBLE);
            mBluetoothLvSearchDevice.setVisibility(View.VISIBLE);
            mBluetoothRlSearchDevice.setVisibility(View.VISIBLE);

            bondedDevices = bluetoothAdapter.getBondedDevices();
            Iterator iterator = bondedDevices.iterator();
            if (iterator.hasNext()) {
                BluetoothDevice bond = (BluetoothDevice) iterator.next();
                pairDevice = bond;
                mBluetoothTvPairName.setText(bond.getName());
                pairPosition = -2;
            }
            openFlag = true;
        } else {
            mBluetoothIvOpen.setBackgroundResource(R.drawable.switch_off);
            mBluetoothIvDetection.setBackgroundResource(R.drawable.switch_off);
            mBluetoothIvSearch.setVisibility(View.GONE);
            mBluetoothTvPair.setVisibility(View.GONE);
            mBluetoothRlPair.setVisibility(View.GONE);
            mBluetoothTvSearchDevice.setVisibility(View.GONE);
            mBluetoothRlSearchDevice.setVisibility(View.GONE);
        }


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

        resisgerReciver();

        if (mBluetoothEnabler != null) {
            mBluetoothEnabler.resume(this);
        }

        if (mLocalManager == null) return;

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(searchReceiver);
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
//        findViewById(R.id.bluetooth_rl_open).setOnClickListener(this);
        findViewById(R.id.bluetooth_rl_detection).setOnClickListener(this);
        findViewById(R.id.bluetooth_rl_pair1).setOnClickListener(this);
//        findViewById(R.id.bluetooth_rl_search).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bluetooth_rl_open:
                if (!openFlag) {
                    bluetoothAdapter.disable();
                    mBluetoothIvOpen.setBackgroundResource(R.drawable.switch_off);
                    mBluetoothIvDetection.setBackgroundResource(R.drawable.switch_off);
                    mBluetoothIvSearch.setVisibility(View.GONE);
                    mBluetoothTvPair.setVisibility(View.GONE);
                    mBluetoothRlPair.setVisibility(View.GONE);
                    mBluetoothTvSearchDevice.setVisibility(View.GONE);
                    mBluetoothRlSearchDevice.setVisibility(View.GONE);

                    openFlag = !openFlag;
                } else {
                    if (bluetoothAdapter != null) {
                        if (!bluetoothAdapter.isEnabled()) {
                            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivity(intent);
                        }
                    } else {
                        showToastShort(getString(R.string.bluetooth_disabled));
                    }

                    mBluetoothIvOpen.setBackgroundResource(R.drawable.switch_on);
                    mBluetoothIvSearch.setVisibility(View.VISIBLE);
                    mBluetoothTvPair.setVisibility(View.VISIBLE);
                    mBluetoothRlPair.setVisibility(View.VISIBLE);
                    mBluetoothLvSearchDevice.setVisibility(View.VISIBLE);
                    mBluetoothRlSearchDevice.setVisibility(View.VISIBLE);

                    openFlag = !openFlag;
                }
                break;
            case R.id.bluetooth_rl_detection:
                if (detectionFlag) {
                    mBluetoothIvDetection.setBackgroundResource(R.drawable.switch_off);
                    detectionFlag = !detectionFlag;
                } else {
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivity(discoverableIntent);
                    mBluetoothIvDetection.setBackgroundResource(R.drawable.switch_on);
                    detectionFlag = !detectionFlag;
                }
                break;
            case R.id.bluetooth_rl_pair1:
                if (pairPosition > -1) {
                    BluetoothDevice device = (BluetoothDevice) list.get(pairPosition).get("device");
                    try {
                        boolean b = Tools.removeBond(device.getClass(), device);
                        if (b) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("name", device.getName());
                            map.put("type", device.getBluetoothClass().getDeviceClass());
                            map.put("device", device);
                            list.add(map);
                            itemAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ;
                    }
                } else if (pairPosition == -2) {
                    try {
                        showToastShort(getString(R.string.bluetooth_connect_canceling));
                        boolean b = Tools.removeBond(pairDevice.getClass(), pairDevice);
                        if (b) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("name", pairDevice.getName());
                            map.put("type", pairDevice.getBluetoothClass().getDeviceClass());
                            map.put("device", pairDevice);
                            mBluetoothTvPairName.setText(R.string.bluetooth_not_conected);
                            list.add(map);
                            itemAdapter.notifyDataSetChanged();
                        } else {
                            showToastShort(getString(R.string.bluetooth_connect_cancel_failed));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.bluetooth_rl_search:
                Animation rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate);
                mBluetoothIvSearch.startAnimation(rotateAnimation);
                bluetoothAdapter.startDiscovery();
                break;
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
                        itemAdapter = new MyBluetoothAdapter(context, list);
                        mBluetoothLvSearchDevice.setAdapter(itemAdapter);
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
                        itemAdapter.notifyDataSetChanged();
                    }
                }
                showToastShort(getString(R.string.bluetooth_connected));
            }
        }
    };

    private void setFilter(BluetoothDeviceFilter.Filter filter) {
        mFilter = filter;
    }

    private void setFilter(int filterType) {
        mFilter = BluetoothDeviceFilter.getFilter(filterType);
    }

    private void removeAllDevices() {
        mLocalAdapter.stopScanning();

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
        Logger.getLogger().i("********onDeviceAdded*******");
        if (mLocalAdapter.getBluetoothState() != BluetoothAdapter.STATE_ON) return;

        if (mFilter.matches(cachedDevice.getDevice())) {

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
//            getPreferenceScreen().addPreference(mAvailableDevicesCategory);
            mBluetoothRlSearchDevice.setVisibility(View.VISIBLE);
            mAvailableDevicesCategoryIsPresent = true;
        }

        removeAllDevices();

        mLocalManager.getCachedDeviceManager().clearNonBondedDevices();
//        mAvailableDevicesCategory.removeAll();
        if (null != itemAdapter) {
            itemAdapter.clearDevice();
        }

        mInitialScanStarted = true;
        mLocalAdapter.startScanning(true);
    }


    private void updateContent(int bluetoothState) {
        int messageId = 0;
        Logger.getLogger().i("**** updateContent **** bluetoothState = " + bluetoothState);
        switch (bluetoothState) {
            case BluetoothAdapter.STATE_ON:

                //已配对过的
                // Paired devices category
//                if (mPairedDevicesCategory == null) {
//                    mPairedDevicesCategory = new PreferenceCategory(getActivity());
//                } else {
//                    mPairedDevicesCategory.removeAll();
//                }
//                addDeviceCategory(mPairedDevicesCategory,
//                        R.string.bluetooth_preference_paired_devices,
//                        BluetoothDeviceFilter.BONDED_DEVICE_FILTER, true);
//                int numberOfPairedDevices = mPairedDevicesCategory.getPreferenceCount();

                mBluetoothTvPair.setVisibility(View.VISIBLE);
                mBluetoothRlPair.setVisibility(View.VISIBLE);
                addDeviceCategory(BluetoothDeviceFilter.BONDED_DEVICE_FILTER, true);

                //发现的可配对设备
                // Available devices category
//                if (mAvailableDevicesCategory == null) {
//                    mAvailableDevicesCategory = new BluetoothProgressCategory(getActivity());
//                    mAvailableDevicesCategory.setSelectable(false);
//                } else {
//                    mAvailableDevicesCategory.removeAll();
//                }
//                addDeviceCategory(mAvailableDevicesCategory,
//                        R.string.bluetooth_preference_found_devices,
//                        BluetoothDeviceFilter.UNBONDED_DEVICE_FILTER, mInitialScanStarted);
//                int numberOfAvailableDevices = mAvailableDevicesCategory.getPreferenceCount();

                addDeviceCategory(BluetoothDeviceFilter.UNBONDED_DEVICE_FILTER, mInitialScanStarted);

                mBluetoothIvOpen.setBackgroundResource(R.drawable.switch_on);
                mBluetoothIvSearch.setVisibility(View.VISIBLE);

                mBluetoothLvSearchDevice.setVisibility(View.VISIBLE);
                mBluetoothRlSearchDevice.setVisibility(View.VISIBLE);

                if (!mInitialScanStarted) {
                    startScanning();
                }

                //未打开蓝牙的提示
//                if (mMyDevicePreference == null) {
//                    mMyDevicePreference = new Preference(getActivity());
//                }
//
//                mMyDevicePreference.setSummary(getResources().getString(
//                        R.string.bluetooth_is_visible_message, mLocalAdapter.getName()));
//                mMyDevicePreference.setSelectable(false);
//                preferenceScreen.addPreference(mMyDevicePreference);

                // mLocalAdapter.setScanMode is internally synchronized so it is okay for multiple
                // threads to execute.

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
                break;

            case BluetoothAdapter.STATE_OFF:
                messageId = R.string.bluetooth_empty_list_bluetooth_off;
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

    private void addDeviceCategory(BluetoothDeviceFilter.Filter filter, boolean addCachedDevices) {
//        preferenceGroup.setTitle(titleId);
//        getPreferenceScreen().addPreference(preferenceGroup);
        setFilter(filter);
//        setDeviceListGroup(preferenceGroup);
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
}
