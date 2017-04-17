package com.zx.zxtvsettings.fragment.ethernet;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.Logger;
import com.zx.zxtvsettings.activity.EthernetActvity;
import com.zx.zxtvsettings.fragment.BaseFragment;
import com.zx.zxtvsettings.fragment.ethernet.mode.NetData;

public class EthernetAutoDetectFragment extends BaseFragment {
    private static final String TAG = "EthernetAutoDetectFragment";
    private View mView = null;
    private TextView mTextView = null;
    private EthernetActvity mEthernetActvity = null;
    private Callbacks mCallbacks;
    private Integer times = 0;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (times >= 3) {
                if (mCallbacks.ethernetAutodetectEthernet()) {
                    Logger.getLogger().i( "ethernet_autodetect  success!");
                    detect_result();

                } else {
                    Logger.getLogger().i( "ethernet_autodetect detect again");
                    times++;
                    if (times < 30) {
                        mHandler.postDelayed(mRunnable, 1000);
                    } else {
                        Logger.getLogger().i( "detect fail!");
                        detect_fail();
                    }
                }
            } else {
                times++;
                mHandler.postDelayed(mRunnable, 1000);
            }
        }
    };


    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_ethernetautodetect);

        mEthernetActvity = (EthernetActvity) getActivity();
        mTextView = (TextView) findViewById(R.id.audodetect_text_id);
        FragmentManager fm = getFragmentManager();
        times = 0;
        mCallbacks.autodetectEthernet();
        if (fm.findFragmentByTag(NetData.EthernetAutoDetect_Tag) != null) {
            Logger.getLogger().i( "  getArguments()   " + getArguments());
            if (getArguments() != null) {
                if (getArguments().getBoolean(NetData.EthernetAutoDetect_KeyId)) {
                    mTextView.setFocusable(true);
                    mTextView.requestFocus();
                }
            }
        }
        mHandler.post(mRunnable);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException(
                    "NetActivity must realize Callbacks!");
        }
        mCallbacks = (Callbacks) activity;

    }

    public interface Callbacks {
        boolean ethernetAutodetectEthernet();

        void autodetectEthernet();
    }

    private void detect_result() {
        mHandler.removeCallbacks(mRunnable);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = mEthernetActvity.getFragmentMap().get(NetData.EthernetDetectResult_KeyId);
        Bundle arguments = new Bundle();
        arguments.putBoolean(NetData.EthernetDetectResult_KeyId, true);
        fragment.setArguments(arguments);
        Logger.getLogger().i("fragment = " + fragment);
        Logger.getLogger().i( "transaction = " + transaction);
        transaction.replace(R.id.network_framelayout, fragment, NetData.EthernetDetectResult_Tag).commit();
    }

    private void detect_fail() {
        mHandler.removeCallbacks(mRunnable);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = mEthernetActvity.getFragmentMap().get(NetData.EthernetConfigFail_KeyId);
        Bundle arguments = new Bundle();
        arguments.putBoolean(NetData.EthernetConfigFail_KeyId, true);
        fragment.setArguments(arguments);
        Logger.getLogger().i( "fragment = " + fragment);
        Logger.getLogger().i( "transaction = " + transaction);
        transaction.replace(R.id.network_framelayout, fragment, NetData.EthernetConfigFail_Tag).commit();
    }
}