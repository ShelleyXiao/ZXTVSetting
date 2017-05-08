package com.zx.zxtvsettings.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.claer.ClearInfo;
import com.zx.zxtvsettings.fragment.ClearHomeFragemnt;

import java.util.ArrayList;


/**
 * User: ShaudXiao
 * Date: 2016-08-24
 * Time: 10:50
 * Company: zx
 * Description:
 * FIXME
 */

public class ClearGarbageActivity extends BaseStatusBarActivity {


    ImageView mCleanProgressBig;
    ImageView mCleanProgressSmall;
    FrameLayout mContent;
    TextView mDisplayText;
    Button mClearBtn;


    private ArrayList<ClearInfo> cacheInfo = null;
    private ArrayList<ClearInfo> tempFileInfo = null;
    private ArrayList<ClearInfo> emptyFolderInfo = null;
    private ArrayList<ClearInfo> thumbInfo = null;
    private ArrayList<ClearInfo> softwareInfo = null;
    private ArrayList<ClearInfo> apkInfo = null;
    private ArrayList<ClearInfo> bigFileInfo = null;

    private ClearHomeFragemnt mClearHomeFragemnt;
    private FragmentManager fm;

    private boolean bClearing = false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_garbage_clear;
    }

    @Override
    protected void setupViews() {

        mCleanProgressBig = (ImageView) findViewById(R.id.clean_progress_big);
        mCleanProgressSmall = (ImageView) findViewById(R.id.clean_progress_small);
        mContent = (FrameLayout) findViewById(R.id.content);
        mDisplayText = (TextView) findViewById(R.id.scan_state_display);
        mClearBtn = (Button) findViewById(R.id.btn_clear);


        mClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(view instanceof  Button) {
                    Button btn = (Button) view;
                    if(btn.getText().equals(getString(R.string.clearing))) {
//                        mClearHomeFragemnt.clearData();
                    } else if(btn.getText().equals(getString(R.string.clear_start))) {
                        mClearHomeFragemnt.clearStart();
                    } else if(btn.getText().equals(getString(R.string.clear_scan_again))) {
                        mClearHomeFragemnt.startScan();
                    }
                }
            }
        });
    }

    @Override
    protected void initialized() {

        fm = getFragmentManager();
        mClearHomeFragemnt = new ClearHomeFragemnt();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.content, mClearHomeFragemnt, "Home");
        ft.commit();

        startAnim();
    }

    @Override
    public void onBackPressed() {

        if(mClearHomeFragemnt.onBackPressed()) {
            return;
        }

        mClearHomeFragemnt.clearData();

        super.onBackPressed();
    }

    public void startAnim() {
        Animation smallAnim = AnimationUtils.loadAnimation(this, R.anim.clear_scan_small);
        Animation bigAnim = AnimationUtils.loadAnimation(this, R.anim.clear_scan_big);
        smallAnim.setInterpolator(new LinearInterpolator());
        bigAnim.setInterpolator(new LinearInterpolator());

        mCleanProgressBig.setAnimation(bigAnim);
        mCleanProgressSmall.setAnimation(smallAnim);

    }

    public void stopAnim() {
        mCleanProgressBig.clearAnimation();
        mCleanProgressSmall.clearAnimation();

    }

    public void setStateDisplay(int id) {
        mDisplayText.setText(id);
    }

    public void setBtnDisplay(int id) {
        mClearBtn.setText(id);
        if(id == R.string.clear_over
                || id == R.string.clearing) {
            mClearBtn.setEnabled(false);
        } else {
            mClearBtn.setEnabled(true);
        }
    }

    public void setClearBtnVisibility(boolean show) {
        mClearBtn.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public ArrayList<ClearInfo> getCacheInfo() {
        return cacheInfo;
    }

    public void setCacheInfo(ArrayList<ClearInfo> cacheInfo) {
        this.cacheInfo = cacheInfo;
    }

    public ArrayList<ClearInfo> getTempFileInfo() {
        return tempFileInfo;
    }

    public void setTempFileInfo(ArrayList<ClearInfo> tempFileInfo) {
        this.tempFileInfo = tempFileInfo;
    }

    public ArrayList<ClearInfo> getEmptyFolderInfo() {
        return emptyFolderInfo;
    }

    public void setEmptyFolderInfo(ArrayList<ClearInfo> emptyFolderInfo) {
        this.emptyFolderInfo = emptyFolderInfo;
    }

    public ArrayList<ClearInfo> getThumbInfo() {
        return thumbInfo;
    }

    public void setThumbInfo(ArrayList<ClearInfo> thumbInfo) {
        this.thumbInfo = thumbInfo;
    }

    public ArrayList<ClearInfo> getSoftwareInfo() {
        return softwareInfo;
    }

    public void setSoftwareInfo(ArrayList<ClearInfo> softwareInfo) {
        this.softwareInfo = softwareInfo;
    }

    public ArrayList<ClearInfo> getApkInfo() {
        return apkInfo;
    }

    public void setApkInfo(ArrayList<ClearInfo> apkInfo) {
        this.apkInfo = apkInfo;
    }

    public ArrayList<ClearInfo> getBigFileInfo() {
        return bigFileInfo;
    }

    public void setBigFileInfo(ArrayList<ClearInfo> bigFileInfo) {
        this.bigFileInfo = bigFileInfo;
    }
}
