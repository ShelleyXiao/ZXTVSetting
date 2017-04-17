package com.zx.zxtvsettings.activity;

import android.content.Intent;
import android.provider.Settings;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.Logger;


public class MainActivity extends BaseStatusBarActivity implements View.OnClickListener {


    ImageButton mSettingNet;
    ImageButton mSettingDisplay;
    ImageButton mSettingBluethee;
    ImageButton mSettingUninstall;
    ImageButton mSettingMore;
    ImageButton mSettingAbout;
    ImageButton mSettingClear;

    private boolean isFirstEnter = false;

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

        mSettingNet.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View view, View view1) {
//                Logger.getLogger().i(view.toString() + ", " + view1.toString());
                Logger.getLogger().e("fffffffffffffff");
                if(isFirstEnter && view != null) {

                }
            }

        });

        mSettingNet.requestFocus();

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
                startActivity(BluethoothActivity.class);
                break;
            case R.id.setting_uninstall:
                startActivity(AppUninstallActivity.class);
                break;
            case R.id.setting_more:
                Intent intent =  new Intent(Settings.ACTION_SETTINGS);
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
