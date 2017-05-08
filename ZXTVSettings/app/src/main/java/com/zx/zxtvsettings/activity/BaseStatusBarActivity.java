package com.zx.zxtvsettings.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.SystemUtils;

/**
 * User: ShaudXiao
 * Date: 2017-04-14
 * Time: 17:20
 * Company: zx
 * Description:
 * FIXME
 */


public class BaseStatusBarActivity extends BaseActivityNew{

    private ImageView netStatu, setStatu;
    private LinearLayout net, set;

    private TextView timeTextView, statuTextView;

    private AppReceiver mAppReceiver;
    private TimeReceiver mTimeReceiver;
    private NetWorkChangeReceiver mNetWorkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        set = (LinearLayout) findViewById(R.id.set);
        net = (LinearLayout) findViewById(R.id.net);

        netStatu = (ImageView) findViewById(R.id.net_statu);
        timeTextView = (TextView) findViewById(R.id.time);
        statuTextView = (TextView) findViewById(R.id.time_statu);
        setStatu = (ImageView) findViewById(R.id.set_statu);

        timeTextView.setText(SystemUtils.getTime(this));
        if (!DateFormat.is24HourFormat(this)) {
            statuTextView.setText(SystemUtils.getStatu());
        } else {
            statuTextView.setVisibility(View.INVISIBLE);
        }

        setListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        register();
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected void setupViews() {

    }

    @Override
    protected void initialized() {

    }

    @Override
    protected void onStop() {
        super.onStop();

        unRegister();
    }


    private void setListener() {
        set.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    set.getChildAt(1).setVisibility(View.VISIBLE);
                } else {
                    set.getChildAt(1).setVisibility(View.GONE);
                }
            }
        });

        net.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    net.getChildAt(1).setVisibility(View.VISIBLE);
                } else {
                    net.getChildAt(1).setVisibility(View.GONE);
                }
            }
        });
        set.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_DOWN:
//                            Logger.getLogger().i("currentView " + currentView.getId());
                            if (currentView != null) {
                                currentView.requestFocus();
                            }
                            return true;
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            return true;
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            Intent intent = new Intent(BaseStatusBarActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            break;
                    }
                }
                return false;
            }
        });

        net.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            if (currentView != null) {
                                currentView.requestFocus();
                            }
                            return true;
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            Intent intent = new Intent(BaseStatusBarActivity.this, NetSetting.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            break;
                    }
                }
                return false;
            }
        });
    }


    private void register() {
        mNetWorkChangeReceiver = new NetWorkChangeReceiver();
        IntentFilter filterNECT = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        filterNECT.addAction("android.net.wifi.STATE_CHANGE");
        filterNECT.addAction("android.net.ethernet.STATE_CHANGE");
        registerReceiver(mNetWorkChangeReceiver, filterNECT);

        mTimeReceiver = new TimeReceiver();
        IntentFilter filterTime = new IntentFilter(Intent.ACTION_TIME_TICK);
        registerReceiver(mTimeReceiver, filterTime);

        mAppReceiver = new AppReceiver();
        IntentFilter filterAPP = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filterAPP.addDataScheme("package");
        filterAPP.addAction(Intent.ACTION_PACKAGE_REMOVED);
        registerReceiver(mAppReceiver, filterAPP);
    }

    private void unRegister() {
        try {
            if (mTimeReceiver != null) {
                unregisterReceiver(mTimeReceiver);
            }

            if (mNetWorkChangeReceiver != null) {
                unregisterReceiver(mNetWorkChangeReceiver);
            }


            if (mAppReceiver != null) {
                unregisterReceiver(mAppReceiver);
            }
        } catch (Exception e) {

        }
    }

    private void timeUpdate(String time) {
        timeTextView.setText(time);
    }

    private void timeStatuUpdate(String timeStu) {
        statuTextView.setText(timeStu);
    }

    private void netStatuUpdate(int flag) {
        switch (flag) {
            case ConnectivityManager.TYPE_WIFI:
                netStatu.setImageResource(R.drawable.wlan);
                break;
            case ConnectivityManager.TYPE_ETHERNET:
                netStatu.setImageResource(R.drawable.eth);
                break;
            default:
                netStatu.setImageResource(R.drawable.un_eth);
                break;
        }
    }

    public class NetWorkChangeReceiver extends BroadcastReceiver {
        private ConnectivityManager connectivityManager;
        private NetworkInfo info;

        @SuppressWarnings("deprecation")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                connectivityManager = (ConnectivityManager) BaseStatusBarActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                    switch (mNetworkInfo.getType()) {
                        case ConnectivityManager.TYPE_WIFI:
                            netStatuUpdate(ConnectivityManager.TYPE_WIFI);
                            break;
                        case ConnectivityManager.TYPE_ETHERNET:
                            netStatuUpdate(ConnectivityManager.TYPE_ETHERNET);
                            break;
                    }

                } else {
                    netStatuUpdate(-1);
                }
            }
        }
    }

    public class AppReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {


            updateApp(intent);

        }
    }

    public class TimeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIME_TICK)) {
                timeUpdate(SystemUtils.getTime(BaseStatusBarActivity.this));
                if (!DateFormat.is24HourFormat(BaseStatusBarActivity.this)) {
                    timeStatuUpdate(SystemUtils.getStatu());
                } else {
                    timeStatuUpdate("");
                }
            }
        }
    }

    public  void updateApp(Intent intent) {

    }
}
