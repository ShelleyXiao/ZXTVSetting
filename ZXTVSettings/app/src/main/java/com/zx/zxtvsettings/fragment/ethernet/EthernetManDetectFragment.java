package com.zx.zxtvsettings.fragment.ethernet;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.Logger;
import com.zx.zxtvsettings.activity.EthernetActvity;
import com.zx.zxtvsettings.fragment.BaseFragment;
import com.zx.zxtvsettings.fragment.ethernet.mode.NetData;


public class EthernetManDetectFragment extends BaseFragment {

    private static final String TAG = "EthernetManDetectFragment";
    private TextView mTextView = null;
    private EthernetActvity mNetworkActivity = null;
    private Callbacks mCallbacks;
    private Integer times = 0;

    String mIpAddress;
    String mMaskAddress;
    String mGatewayAddress;
    String mDns1Address;
    String mDns2Address;


    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (times >= 3) {
                if (mCallbacks.ethernetMandetectEthernet()) {
                    Logger.getLogger().i("ethernet_mandetect  success!");
                    detect_result();
                } else {
                    Logger.getLogger().i("ethernet_mandetect detect again");
                    times++;
                    if (times < 30) {
                        mHandler.postDelayed(mRunnable, 1000);
                    } else {
                        Logger.getLogger().i("detect fail!");
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
        setContentView(R.layout.fragment_ethernetmandetect);

        mNetworkActivity = (EthernetActvity) getActivity();
        mTextView = (TextView) findViewById(R.id.mandetect_text_id);
        times = 0;
        getFocus();
        mCallbacks.mandetectEthernet();
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
        boolean ethernetMandetectEthernet();

        void mandetectEthernet();
    }

    private void getFocus() {
        FragmentManager fm = getFragmentManager();
        if (fm.findFragmentByTag(NetData.EthernetManDetect_Tag) != null) {
            Logger.getLogger().i("  getArguments()   " + getArguments());
            if (getArguments() != null) {
                if (getArguments().getBoolean(NetData.EthernetManDetect_KeyId)) {
                    mTextView.setFocusable(true);
                    mTextView.requestFocus();
                }
            }
        }
    }


    private void detect_result() {
        mHandler.removeCallbacks(mRunnable);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = mNetworkActivity.getFragmentMap().get(NetData.EthernetDetectResult_KeyId);
        Bundle arguments = new Bundle();
        arguments.putBoolean(NetData.EthernetDetectResult_KeyId, true);
        fragment.setArguments(arguments);
        transaction.replace(R.id.network_framelayout, fragment, NetData.EthernetDetectResult_Tag).commit();
    }

    private void detect_fail() {
        mHandler.removeCallbacks(mRunnable);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = mNetworkActivity.getFragmentMap().get(NetData.EthernetConfigFail_KeyId);
        Bundle arguments = new Bundle();
        arguments.putBoolean(NetData.EthernetConfigFail_KeyId, true);
        fragment.setArguments(arguments);
        Logger.getLogger().i("fragment = " + fragment);
        Logger.getLogger().i("transaction = " + transaction);
        transaction.replace(R.id.network_framelayout, fragment, NetData.EthernetConfigFail_Tag).commit();
    }
}