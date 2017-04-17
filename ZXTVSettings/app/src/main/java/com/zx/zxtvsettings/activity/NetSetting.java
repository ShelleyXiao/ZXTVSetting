package com.zx.zxtvsettings.activity;

import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.NetWorkUtil;

/**
 * User: ShaudXiao
 * Date: 2016-08-19
 * Time: 13:56
 * Company: zx
 * Description:
 * FIXME
 */

public class NetSetting extends BaseStatusBarActivity implements View.OnClickListener{

    TextView mSettingCustomWifi;
    TextView mSettingNetState;
    TextView mSettingCustomEthernet;
    TextView mSettingCustomNetDetection;
    TextView mSettingWifiSoftAp;
    RelativeLayout mSettingCustomRl;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting_net;
    }

    @Override
    protected void setupViews() {
        mSettingCustomWifi = (TextView) findViewById(R.id.setting_custom_wifi);
        mSettingNetState = (TextView) findViewById(R.id.net_state);
        mSettingCustomEthernet = (TextView) findViewById(R.id.setting_custom_ethernet);
        mSettingCustomNetDetection = (TextView) findViewById(R.id.setting_custom_net_detection);
        mSettingCustomRl = (RelativeLayout) findViewById(R.id.setting_custom_rl);
        mSettingWifiSoftAp = (TextView) findViewById(R.id.setting_custom_net_soft_ap);
        setupView();
    }

    @Override
    protected void initialized() {

    }

    @Override
    public void onResume() {
        super.onResume();
        if(NetWorkUtil.isNetWorkAvailable(this)) {
            mSettingNetState.setText(R.string.net_state_connected);
        } else {
            mSettingNetState.setText(R.string.net_state_disconnected);
        }
    }

    private void setupView() {
        findViewById(R.id.setting_custom_wifi).setOnClickListener(this);
        findViewById(R.id.setting_custom_ethernet).setOnClickListener(this);
        findViewById(R.id.setting_custom_net_detection).setOnClickListener(this);
        findViewById(R.id.setting_custom_net_soft_ap).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.setting_custom_wifi:
                startActivity(WifiActivity.class);
                break;

            case R.id.setting_custom_ethernet:
                startActivity(EthernetActvity.class);
                break;
            case R.id.setting_custom_net_soft_ap:
                startActivity(WIfiApActivity.class);
                break;
            case R.id.setting_custom_net_detection:
                if(NetWorkUtil.isNetWorkAvailable(this)) {
                    startActivity(SpeedTestActivity.class);
                } else {
                    showToastShort(getString(R.string.net_state_disconnected));
                }

                break;
        }

//        startActivity(intent);
    }
}
