package com.zx.zxtvsettings.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;

import com.zx.zxtvsettings.R;


public class MainActivity extends BaseStatusBarActivity implements View.OnClickListener {


    ImageButton mSettingNet;
    ImageButton mSettingDisplay;
    ImageButton mSettingBluethee;
    ImageButton mSettingUninstall;
    ImageButton mSettingMore;
    ImageButton mSettingAbout;
    ImageButton mSettingClear;



    @Override
    protected int getLayoutId() {
        return R.layout.activity_main2;
    }

    @Override
    protected void setupViews() {

        setFocusMoveView(R.id.mainUpView);

        mSettingNet = (ImageButton) findViewById(R.id.setting_net);
        mSettingDisplay = (ImageButton) findViewById(R.id.setting_display);
        mSettingBluethee = (ImageButton) findViewById(R.id.setting_bluethee);
        mSettingUninstall = (ImageButton) findViewById(R.id.setting_uninstall);
        mSettingMore = (ImageButton) findViewById(R.id.setting_more);
        mSettingAbout = (ImageButton) findViewById(R.id.setting_about);
        mSettingClear = (ImageButton) findViewById(R.id.setting_clear);

        mSettingNet.setOnFocusChangeListener(mFocusChangeListener);
        mSettingDisplay.setOnFocusChangeListener(mFocusChangeListener);
        mSettingBluethee.setOnFocusChangeListener(mFocusChangeListener);
        mSettingMore.setOnFocusChangeListener(mFocusChangeListener);
        mSettingUninstall.setOnFocusChangeListener(mFocusChangeListener);
        mSettingAbout.setOnFocusChangeListener(mFocusChangeListener);
        mSettingClear.setOnFocusChangeListener(mFocusChangeListener);

        setupView();
    }

    @Override
    protected void initialized() {

    }

    private void setupView() {
        findViewById(R.id.setting_net).setOnClickListener(this);
        findViewById(R.id.setting_display).setOnClickListener(this);
        findViewById(R.id.setting_bluethee).setOnClickListener(this);
        findViewById(R.id.setting_uninstall).setOnClickListener(this);
        findViewById(R.id.setting_more).setOnClickListener(this);
        findViewById(R.id.setting_about).setOnClickListener(this);
        findViewById(R.id.setting_clear).setOnClickListener(this);

//        mSettingNet.getViewTreeObserver().addOnWindowFocusChangeListener(new ViewTreeObserver.OnWindowFocusChangeListener() {
//            @Override
//            public void onWindowFocusChanged(boolean b) {
//                if(b) {
//                    mSettingNet.requestFocus();
//                }
//            }
//        });


    }

    @Override
    protected View getFirstViewFocusRequest() {
        return findViewById(R.id.setting_net);
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.setting_net:
                startActivity(NetSetting.class);
                break;
            case R.id.setting_display:
                startActivity(DisplayModeActivity.class);
                break;
            case R.id.setting_bluethee:
//                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
//                BluetoothManager mBluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
//                BluetoothAdapter adapter = mBluetoothManager.getAdapter();
                if (getPackageManager().hasSystemFeature(
                        PackageManager.FEATURE_BLUETOOTH_LE))  {
                    startActivity(BluethoothActivityNew.class);
                } else {
                    showToastLong(getString(R.string.not_support_bluethooth));
                }
                break;
            case R.id.setting_uninstall:
                startActivity(AppUninstallActivity.class);
                break;
            case R.id.setting_more:
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
                break;
            case R.id.setting_about:
                startActivity(AboutActivity.class);
                break;
            case R.id.setting_clear:
                startActivity(ClearGarbageActivity.class);
                break;
        }

    }

}
