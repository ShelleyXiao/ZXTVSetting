package com.zx.zxtvsettings.fragment.ethernet;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.activity.EthernetActvity;
import com.zx.zxtvsettings.fragment.BaseFragment;
import com.zx.zxtvsettings.fragment.ethernet.mode.EthernetMode;
import com.zx.zxtvsettings.fragment.ethernet.mode.NetData;

public class EthernetFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "EthernetFragment";
    private RadioGroup mRadioGroup = null;
    private RadioButton mRadioAuto = null;
    private RadioButton mRadioMan = null;
    private RadioButton mRadioPppoe = null;
    private Button mBtn_ConfigStart = null;
    private View mView = null;
    private Boolean mAutochecked = true;
    private Boolean mManchecked = false;
    private Boolean mPppoechecked = false;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    private Callbacks mCallbacks;

    private EthernetActvity mEthernetActvity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("EthernetActivity must realize Callbacks!");
        }
        mCallbacks = (Callbacks) activity;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_ethernet);

        mEthernetActvity = (EthernetActvity) getActivity();

        mRadioGroup = (RadioGroup) findViewById(R.id.radiogroup_id);
        mRadioAuto = (RadioButton) findViewById(R.id.auto_radio_id);
        mRadioMan = (RadioButton) findViewById(R.id.man_radio_id);
        mRadioPppoe = (RadioButton) findViewById(R.id.pppoe_radio_id);
        mBtn_ConfigStart = (Button) findViewById(R.id.ethernet_configstart_id);
        mBtn_ConfigStart.setOnClickListener(this);
        mPreferences = ((EthernetActvity)mCallbacks).getSharedPreferences("EthernetActvity", Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();

        initView();
        registerfragment();

        restoremode();

    }

    private void restoremode() {
        String mode;
        if (mPreferences == null)
            return;
        mode = mPreferences.getString("MODE", null);
        Log.i(TAG, "restore mode = " + mode);
        if (mode == null)
            return;

        if (mode.equals("auto")) {
            Log.i(TAG, "restore mode auto");
            mAutochecked = true;
            mManchecked = false;
            mPppoechecked = false;
            mRadioAuto.setChecked(true);
            mRadioMan.setChecked(false);
            mRadioPppoe.setChecked(false);
        } else if (mode.equals("man")) {
            Log.i(TAG, "restore mode man");
            mAutochecked = false;
            mManchecked = true;
            mPppoechecked = false;
            mRadioAuto.setChecked(false);
            mRadioMan.setChecked(true);
            mRadioPppoe.setChecked(false);
        }  else {
            Log.e(TAG, "restore mode error! please check!");
        }

    }

    private void storemode() {
        if (mAutochecked) {
            mEditor.putString("MODE", "auto");
        } else if (mManchecked) {
            mEditor.putString("MODE", "man");
        } else if (mPppoechecked) {
            mEditor.putString("MODE", "pppoe");
        }
        mEditor.commit();
    }

    public void initView() {
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                switch (checkedId) {
                    case R.id.auto_radio_id:
                        mAutochecked = true;
                        mManchecked = false;
//                        mPppoechecked = false;
                        Toast.makeText(getActivity(), "Network Auto Connect ", Toast.LENGTH_LONG).show();
                        break;
                    case R.id.man_radio_id:
                        mAutochecked = false;
                        mManchecked = true;
//                        mPppoechecked = false;
                        Toast.makeText(getActivity(), "Network Manul Connect", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ethernet_configstart_id:
                Log.i(TAG, "enter onClick ethernet_configstart_id ");
                storemode();
                detect_start();
                break;
            default:
                break;
        }
    }

    public void registerfragment() {
//        mCallbacks.registerNetworkFragments(NetData.Netstate_KeyId, new NetstateFragment());
//        mCallbacks.registerNetworkFragments(NetData.NetstateDetect_KeyId, new NetstateDetectFragment());

//        mCallbacks.registerNetworkFragments(NetData.Ethernet_KeyId, new EthernetFragment());
        mCallbacks.registerNetworkFragments(NetData.EthernetAutoDetect_KeyId, new EthernetAutoDetectFragment());
        mCallbacks.registerNetworkFragments(NetData.EthernetManDetect_KeyId, new EthernetManDetectFragment());
        mCallbacks.registerNetworkFragments(NetData.EthernetManConfig_KeyId, new EthernetManConfigFragment());
        mCallbacks.registerNetworkFragments(NetData.EthernetDetectResult_KeyId, new EthernetDetectResultFragment());
        mCallbacks.registerNetworkFragments(NetData.EthernetConfigFail_KeyId, new EthernetConfigFailFragment());

    }

    private void detect_start() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (mAutochecked) {
            mEthernetActvity.getNetState().setEthernetMode(EthernetMode.MODE_AUTO);
            Fragment fragment = mEthernetActvity.getFragmentMap().get(NetData.EthernetAutoDetect_KeyId);
            Bundle arguments = new Bundle();
            arguments.putBoolean(NetData.EthernetAutoDetect_KeyId, true);
            fragment.setArguments(arguments);
            transaction.replace(R.id.network_framelayout, fragment, NetData.EthernetAutoDetect_Tag).commit();

        }

        if (mManchecked) {
            mEthernetActvity.getNetState().setEthernetMode(EthernetMode.MODE_MAN);
            Fragment fragment = mEthernetActvity.getFragmentMap().get(NetData.EthernetManConfig_KeyId);
            Bundle arguments = new Bundle();
            arguments.putBoolean(NetData.EthernetManConfig_KeyId, true);
            fragment.setArguments(arguments);
            transaction.replace(R.id.network_framelayout, fragment, NetData.EthernetManConfig_Tag).commit();

        }
    }

    public interface Callbacks {
        void registerNetworkFragments(String str, Fragment frag);
    }

}
