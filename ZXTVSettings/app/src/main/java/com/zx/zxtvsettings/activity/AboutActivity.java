package com.zx.zxtvsettings.activity;

import android.os.Build;
import android.widget.TextView;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.NetWorkUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


/**
 * User: ShaudXiao
 * Date: 2016-08-23
 * Time: 16:36
 * Company: zx
 * Description:
 * FIXME
 */

public class AboutActivity extends BaseStatusBarActivity {

    TextView mDeviceName;
    TextView mDeviceAndroid;
    TextView mDeviceVersion;
    TextView mDeviceMAC;
    TextView mDeviceip;

    private static final String ETH0_MAC_ADDR = "/sys/class/net/eth0/address" ;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void setupViews() {
        mDeviceName = (TextView) findViewById(R.id.device_name);
        mDeviceAndroid = (TextView) findViewById(R.id.device_android);
        mDeviceVersion = (TextView) findViewById(R.id.device_version);
        mDeviceMAC = (TextView) findViewById(R.id.device_mac);
        mDeviceip = (TextView) findViewById(R.id.device_ip);
    }

    @Override
    protected void initialized() {
//        Logger.getLogger().d("DEVICE: " + Build.DEVICE + " DISPLAY: " + Build.DISPLAY + " MODEL:" + Build.MODEL + " PRODUCT:" + Build.PRODUCT);
        mDeviceAndroid.setText(Build.VERSION.RELEASE);
        mDeviceName.setText(Build.MODEL);
        mDeviceVersion.setText(Build.DISPLAY);
        mDeviceMAC.setText(getWireMacAddr());
        mDeviceip.setText(NetWorkUtil.getLocalNetState(this).ipAddress);
    }

    private String getWireMacAddr() {
        try {
            return readLine(ETH0_MAC_ADDR);
        } catch (IOException e) {
            e.printStackTrace();
            return "unavailable";
        }
        //return "00:22:7E:0B:53:26";
    }

    private static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

}
