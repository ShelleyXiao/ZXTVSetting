package com.zx.zxtvsettings.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.FileUtil;
import com.zx.zxtvsettings.Utils.Logger;
import com.zx.zxtvsettings.activity.ClearGarbageActivity;
import com.zx.zxtvsettings.adapter.ClearDetailAdapter;
import com.zx.zxtvsettings.claer.CacheInfo;
import com.zx.zxtvsettings.claer.Callback;
import com.zx.zxtvsettings.claer.ClearCache;
import com.zx.zxtvsettings.claer.ClearInfo;
import com.zx.zxtvsettings.claer.Constant;
import com.zx.zxtvsettings.claer.FileSDCardHelper;
import com.zx.zxtvsettings.claer.SDCardInfo;
import com.zx.zxtvsettings.claer.TraverseUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * User: ShaudXiao
 * Date: 2016-08-24
 * Time: 16:33
 * Company: zx
 * Description:
 * FIXME
 */

public class ClearHomeFragemnt extends BaseFragment implements AdapterView.OnItemClickListener, ClearDetailAdapter.itemCheckedChangedListener, View.OnClickListener{

    TextView mClearTotalCount;
    TextView mClearTotalSize;
    TextView mSystemCacheLable;
    TextView mSystemCacheCount;
    ProgressBar mSystemCacheProgress;
    TextView mSystemCacheSize;
    RelativeLayout mSystemCache;

    TextView mTempFileLable;
    TextView mTempFileCount;
    ProgressBar mTempFileProgress;
    TextView mTempFileSize;
    RelativeLayout mTempFile;

    TextView mApkZipLable;
    TextView mApkZipCount;
    ProgressBar mApkZipProgress;
    TextView mApkZipSize;
    RelativeLayout mApkZip;

    TextView mBigFileLable;
    TextView mBigFileCount;
    ProgressBar mBigFileProgress;
    TextView mBigFileSize;
    RelativeLayout mBigFile;

    TextView mSoftwareLastLable;
    TextView mSoftwareLastCount;
    ProgressBar mSoftwareLastProgress;
    TextView mSoftwareLastSize;
    RelativeLayout mSoftwareLast;

    TextView mEmptyFolderLable;
    TextView mEmptyFolderCount;
    ProgressBar mEmptyFolderProgress;
    TextView mEmptyFolderSize;
    RelativeLayout mEmptyFolder;

    TextView mThumbLable;
    TextView mThumbCount;
    ProgressBar mThumbProgress;
    TextView mThumbSize;
    RelativeLayout mThumb;

    ScrollView mContentS;

    ListView mDetailLv;

    private ClearDetailAdapter mCacheAdatper;
    private ClearDetailAdapter mEmptyFolderAdatper;
    private ClearDetailAdapter mBigFileAdatper;
    private ClearDetailAdapter mApkAdatper;
    private ClearDetailAdapter mThumbAdatper;
    private ClearDetailAdapter mTempAdatper;
    private ClearDetailAdapter mSoftwareAdatper;



    private ArrayList<ClearInfo> cacheList = new ArrayList<ClearInfo>();
    private ArrayList<ClearInfo> emptyFolderList = new ArrayList<ClearInfo>();
    private ArrayList<ClearInfo> bigFileList = new ArrayList<ClearInfo>();
    private ArrayList<ClearInfo> apkList = new ArrayList<ClearInfo>();
    private ArrayList<ClearInfo> thumbFolderList = new ArrayList<ClearInfo>();
    private ArrayList<ClearInfo> tempFileList = new ArrayList<ClearInfo>();
    private ArrayList<ClearInfo> softwareList = new ArrayList<ClearInfo>(); // 软件残留

    private TraverseHandler handler = new TraverseHandler();
    private TraverseUtil traverseUtil;
    private int clearThread = 0;

    private final int CLEAR_CACHE = 0;
    private final int CLEAR_TEMP_FILE = 1;
    private final int CLEAR_EMPTY_FOLDER = 2;
    private final int CLEAR_THUMB_FOLDER = 3;
    private final int CLEAR_SOFTWARE = 4;
    private final int CLEAR_APK = 5;
    private final int CLEAR_BIG_FILE = 6;
    private final int CLEAR_FINISHED = 10;

    private boolean isTraversaling = true;  //判断是否还在遍历文件夹（扫描中？）

    private FileSDCardHelper mFileSDCardHelper;

    private int detailType = -1;

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_clear_home);
        initView();

        setupView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        clickAble(false);

        mClearTotalCount.setText(getString(R.string.clear_total_count, 0));

        startScan();

        initAdpter();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        mClearTotalCount = (TextView)findViewById(R.id.clear_total_count);
        mSystemCacheLable = (TextView)findViewById(R.id.system_cache_lable);
        mClearTotalSize = (TextView)findViewById(R.id.clear_total_size);
        mSystemCacheCount = (TextView)findViewById(R.id.system_cache_count);
        mSystemCacheProgress = (ProgressBar) findViewById(R.id.system_cache_progress);
        mSystemCacheSize = (TextView) findViewById(R.id.system_cache_size);
        mSystemCache = (RelativeLayout) findViewById(R.id.system_cache);
        mTempFileLable = (TextView) findViewById(R.id.temp_file_lable);
        mTempFileCount = (TextView) findViewById(R.id.temp_file_count);
        mTempFileProgress = (ProgressBar) findViewById(R.id.temp_file_progress);
        mTempFileSize = (TextView) findViewById(R.id.temp_file_size);
        mTempFile = (RelativeLayout) findViewById(R.id.temp_file);
        mApkZipLable = (TextView) findViewById(R.id.apk_zip_lable);
        mApkZipCount = (TextView) findViewById(R.id.apk_zip_count);
        mApkZipProgress = (ProgressBar) findViewById(R.id.apk_zip_progress);
        mApkZipSize = (TextView) findViewById(R.id.apk_zip_size);
        mApkZip = (RelativeLayout) findViewById(R.id.apk_zip);

        mBigFileLable = (TextView) findViewById(R.id.big_file_lable);
        mBigFileCount = (TextView) findViewById(R.id.big_file_count);
        mBigFileProgress = (ProgressBar) findViewById(R.id.big_file_progress);
        mBigFileSize = (TextView) findViewById(R.id.big_file_size);
        mBigFile = (RelativeLayout) findViewById(R.id.big_file);

        mSoftwareLastLable = (TextView) findViewById(R.id.software_last_lable);
        mSoftwareLastCount = (TextView) findViewById(R.id.software_last_count);
        mSoftwareLastProgress = (ProgressBar) findViewById(R.id.software_last_progress);
        mSoftwareLastSize = (TextView) findViewById(R.id.software_last_size);
        mSoftwareLast = (RelativeLayout) findViewById(R.id.software_last);

        mEmptyFolderLable = (TextView) findViewById(R.id.empty_folder_lable);
        mEmptyFolderCount = (TextView) findViewById(R.id.empty_folder_count);
        mEmptyFolderProgress = (ProgressBar) findViewById(R.id.empty_folder_progress);
        mEmptyFolderSize = (TextView) findViewById(R.id.empty_folder_size);
        mEmptyFolder = (RelativeLayout) findViewById(R.id.empty_folder);

        mThumbLable = (TextView) findViewById(R.id.thumb_lable);
        mThumbCount = (TextView) findViewById(R.id.thumb_count);
        mThumbProgress = (ProgressBar) findViewById(R.id.thumb_progress);
        mThumbCount = (TextView) findViewById(R.id.thumb_size);
        mThumb = (RelativeLayout) findViewById(R.id.thumb);

        mContentS = (ScrollView) findViewById(R.id.content_S);
        mDetailLv = (ListView) findViewById(R.id.content_l);
    }

    public void startScan() {

        cacheList.clear();
        tempFileList.clear();
        apkList.clear();
        bigFileList.clear();
        softwareList.clear();
        tempFileList.clear();
        thumbFolderList.clear();

        getCacheInfo();
        getClearInfo();
    }

    public boolean onBackPressed() {
        if(mDetailLv.getVisibility() == View.VISIBLE) {
            showDetailList(false);
            return true;
        } else if(mContentS.getVisibility() == View.VISIBLE) {
            return false;
        }

        return true;
    }

    private void initAdpter() {

        mCacheAdatper = new ClearDetailAdapter(getActivity(), cacheList, this);
        mEmptyFolderAdatper = new ClearDetailAdapter(getActivity(), emptyFolderList, this);
        mBigFileAdatper = new ClearDetailAdapter(getActivity(), bigFileList, this);
        mApkAdatper = new ClearDetailAdapter(getActivity(), apkList, this);
        mThumbAdatper = new ClearDetailAdapter(getActivity(), thumbFolderList, this);
        mTempAdatper = new ClearDetailAdapter(getActivity(), tempFileList, this);
        mSoftwareAdatper = new ClearDetailAdapter(getActivity(), softwareList, this);

        mDetailLv.setOnItemClickListener(this);
    }

    private void getClearInfo() {
        traverseUtil = new TraverseUtil(getActivity());
        traverseUtil.setEmptyFolder(emptyFolderList);
        traverseUtil.setSoftware(softwareList);
        traverseUtil.setThumbFolder(thumbFolderList);
        traverseUtil.setTempFile(tempFileList);
        traverseUtil.setAPK(apkList);
        traverseUtil.setBigFile(bigFileList);

        FileSDCardHelper mFileSDCardHelper = FileSDCardHelper.getInstance(getActivity());

        List<String> sdcardPath = new ArrayList<String>();
        List<SDCardInfo> cardInfos = mFileSDCardHelper.getAllRoot();
        for(SDCardInfo info : cardInfos) {
            sdcardPath.add(info.path);
            Logger.getLogger().e("path : " + info.path);
        }

        traverseUtil.setHandler(handler);
        traverseUtil.setTraverse(new Callback() {
            @Override
            public void onFinished(boolean finished) {
                handler.sendEmptyMessage(Constant.ALL_SEARCH_FINISHED);
            }
        });

        for(int i =0; i < sdcardPath.size(); i++) {
            traverseUtil.setPath(sdcardPath.get(i));
            traverseUtil.startTraverse();
        }
    }

    private void getCacheInfo() {
        CacheInfo cacheInfo = new CacheInfo(getActivity());
        cacheInfo.setParam(cacheList, handler);
        cacheInfo.getCacheInfo();
    }

    private void clickAble(boolean isAble) {

        enableItem(isAble, mSystemCacheProgress, mSystemCache, mSystemCacheCount, mSystemCacheSize);
        enableItem(isAble, mTempFileProgress, mTempFile, mTempFileCount, mTempFileSize);
        enableItem(isAble, mApkZipProgress, mApkZip, mApkZipCount, mApkZipSize);
        enableItem(isAble, mBigFileProgress, mBigFile, mBigFileCount, mBigFileSize);
        enableItem(isAble, mSoftwareLastProgress, mSoftwareLast, mSoftwareLastCount, mSoftwareLastSize);
        enableItem(isAble, mEmptyFolderProgress, mEmptyFolder, mEmptyFolderCount, mEmptyFolderSize);
        enableItem(isAble, mThumbProgress, mThumb, mThumbCount, mThumbSize);

    }

    public void enableItem(boolean isAble, ProgressBar progressBar, RelativeLayout layout, TextView count, TextView size) {
        if(isAble) {
            progressBar.setVisibility(View.GONE);
            layout.setClickable(true);
            count.setVisibility(View.VISIBLE);
            size.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            layout.setClickable(false);
            count.setVisibility(View.GONE);
            size.setVisibility(View.GONE);
        }
    }

    private void setupView() {
        findViewById(R.id.system_cache).setOnClickListener(this);
        findViewById(R.id.temp_file).setOnClickListener(this);
        findViewById(R.id.apk_zip).setOnClickListener(this);
        findViewById(R.id.big_file).setOnClickListener(this);
        findViewById(R.id.empty_folder).setOnClickListener(this);
        findViewById(R.id.thumb).setOnClickListener(this);
        findViewById(R.id.software_last).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.system_cache:
                mDetailLv.setAdapter(mCacheAdatper);
                detailType = Constant.TYPE_CACHE;

                updateSingleCountAndSize(cacheList);
                break;
            case R.id.temp_file:
                mDetailLv.setAdapter(mTempAdatper);
                detailType = Constant.TYPE_TEMP_FILE;

                updateSingleCountAndSize(tempFileList);
                break;
            case R.id.apk_zip:
                mDetailLv.setAdapter(mApkAdatper);
                detailType = Constant.TYPE_APK;
                updateSingleCountAndSize(apkList);
                break;
            case R.id.big_file:
                mDetailLv.setAdapter(mBigFileAdatper);
                detailType = Constant.TYPE_BIG_FILE;
                updateSingleCountAndSize(bigFileList);
                break;
            case R.id.software_last:
                mDetailLv.setAdapter(mSoftwareAdatper);
                detailType = Constant.TYPE_SOFTWARE;
                updateSingleCountAndSize(softwareList);
                break;
            case R.id.empty_folder:
                mDetailLv.setAdapter(mEmptyFolderAdatper);
                detailType = Constant.TYPE_EMPTY_FOLDER;
                updateSingleCountAndSize(emptyFolderList);
                break;
            case R.id.thumb:
                mDetailLv.setAdapter(mThumbAdatper);
                detailType = Constant.TYPE_THUMB;
                updateSingleCountAndSize(thumbFolderList);
                break;
        }

        showDetailList(true);
    }

    private void showDetailList(boolean show) {
        mContentS.setVisibility(show ? View.GONE : View.VISIBLE);
        mDetailLv.setVisibility(show ? View.VISIBLE : View.GONE);

        mContentS.setFocusable(show ? false : true);
        mDetailLv.setFocusable(show ? true : false);

        if(show) {
            mDetailLv.requestFocus();
        } else {
            mContentS.requestFocus();
            detailType = -1;
        }

    }

    private long updateCacheSize() {
        int listSize = cacheList.size();
        long size = 0;
        for(int i = 0 ; i < size; i++ ) {
            size += cacheList.get(i).getSize();
        }

        mSystemCacheCount.setText("(" + listSize + ")");
        mSystemCacheSize.setText(FileUtil.convertStorage(size));

        return size;
    }

    private void updateClearSize() {
        long size = updateCacheSize();
        long totalSize = size;
        long totalCount = cacheList.size();
        if(size == 0 && !isTraversaling) {
            mSystemCacheProgress.setVisibility(View.GONE);
        }

        size = 0;
        long listSize = tempFileList.size();
        for(int i = 0; i < listSize; i++) {
            size += tempFileList.get(i).getSize();
        }

        totalSize += size;
        totalCount += listSize;
        mTempFileCount.setText("(" + listSize + ")");
        mTempFileSize.setText(FileUtil.convertStorage(size));
        if(listSize == 0 && !isTraversaling) {
            mTempFileProgress.setVisibility(View.GONE);
        }

        size = 0;
        listSize = emptyFolderList.size();
        for (int i = 0; i < listSize; i++) {
            size += emptyFolderList.get(i).getSize();
        }
        totalSize += size;
        totalCount += listSize;
        mEmptyFolderCount.setText("(" + listSize + ")");
        mEmptyFolderSize.setText( FileUtil.convertStorage(size));
        if (listSize == 0 && !isTraversaling) {
            mEmptyFolderProgress.setVisibility(View.GONE);
        }

        size = 0;
        listSize = thumbFolderList.size();
        for (int i = 0; i < listSize; i++) {
            size += thumbFolderList.get(i).getSize();
        }
        totalSize += size;
        totalCount += listSize;
        mThumbCount.setText("(" + listSize + ")");
        mThumbSize.setText( FileUtil.convertStorage(size));
        if (listSize == 0 && !isTraversaling) {
            mThumbProgress.setVisibility(View.GONE);
        }

        size = 0;
        listSize = softwareList.size();
        for (int i = 0; i < listSize; i++) {
            size += softwareList.get(i).getSize();
        }
        totalSize += size;
        totalCount += listSize;
        mSoftwareLastCount.setText("(" + listSize + ")");
        mSoftwareLastSize.setText( FileUtil.convertStorage(size));
        if (listSize == 0 && !isTraversaling) {
            mSoftwareLastProgress.setVisibility(View.GONE);
        }

        size = 0;
        listSize = apkList.size();
        for (int i = 0; i < listSize; i++) {
            size += apkList.get(i).getSize();
        }
        totalSize += size;
        totalCount += listSize;
        mApkZipCount.setText("(" + listSize + ")");
        mApkZipSize.setText( FileUtil.convertStorage(size));
        if (listSize == 0 && !isTraversaling) {
            mApkZipProgress.setVisibility(View.GONE);
        }

        size = 0;
        listSize = bigFileList.size();
        for (int i = 0; i < listSize; i++) {
            size += bigFileList.get(i).getSize();
        }
        totalSize += size;
        totalCount += listSize;
        mBigFileCount.setText("(" + listSize + ")");
        mBigFileSize.setText( FileUtil.convertStorage(size));
        if (listSize == 0 && !isTraversaling) {
            mBigFileProgress.setVisibility(View.GONE);
        }

        mClearTotalCount.setText(getString(R.string.clear_total_count, totalCount));
        mClearTotalSize.setText(FileUtil.convertStorage(totalSize));
    }

    private void updateSingleCountAndSize(List<ClearInfo> datas) {
        long size = 0;
        long count = 0;

        long listSize = datas.size();
        for(int i = 0; i < listSize; i++) {
            ClearInfo info = datas.get(i);
            if(null != info && info.isSelected()) {
                count++;
                size += info.getSize();
            }
        }

        mClearTotalCount.setText(getString(R.string.clear_total_count, count));
        mClearTotalSize.setText(FileUtil.convertStorage(size));
    }

    private void saveData() {
        ClearGarbageActivity activity = (ClearGarbageActivity) getActivity();
        activity.setCacheInfo(cacheList);
        activity.setTempFileInfo(tempFileList);
        activity.setThumbInfo(thumbFolderList);
        activity.setEmptyFolderInfo(emptyFolderList);
        activity.setSoftwareInfo(softwareList);
        activity.setBigFileInfo(bigFileList);
        activity.setApkInfo(apkList);
    }

    private boolean checkItemFinished(ProgressBar bar) {
        return bar.getVisibility() == View.GONE;
    }

    private boolean checkAllFinished() {
        boolean isAllFinished =false;
        isAllFinished &= checkItemFinished(mSystemCacheProgress);
        isAllFinished &= checkItemFinished(mApkZipProgress);
        isAllFinished &= checkItemFinished(mBigFileProgress);
        isAllFinished &= checkItemFinished(mEmptyFolderProgress);
        isAllFinished &= checkItemFinished(mSoftwareLastProgress);
        isAllFinished &= checkItemFinished(mTempFileProgress);
        isAllFinished &= checkItemFinished(mThumbProgress);

        return isAllFinished;
    }



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        LinearLayout layout = (LinearLayout) adapterView.getChildAt(i);
        if(null != layout) {
            CheckBox checkBox = (CheckBox) layout.findViewById(R.id.detial_item_selected);
            if(null != checkBox) {
                if(checkBox.isChecked()) {
                    checkBox.setChecked(false);
                } else {
                    checkBox.setChecked(true);
                }
            }
        }
    }

    @Override
    public void itemCheckedChanged(int position) {

        if(mDetailLv.getVisibility() == View.VISIBLE) {
            switch (detailType) {
                case Constant.TYPE_CACHE:
                    updateSingleCountAndSize(cacheList);
                    break;
                case Constant.TYPE_TEMP_FILE:
                    updateSingleCountAndSize(tempFileList);
                    break;
                case Constant.TYPE_APK:
                    updateSingleCountAndSize(apkList);
                    break;
                case Constant.TYPE_BIG_FILE:
                    updateSingleCountAndSize(bigFileList);
                    break;
                case Constant.TYPE_SOFTWARE:
                    updateSingleCountAndSize(softwareList);
                    break;
                case Constant.TYPE_THUMB:
                    updateSingleCountAndSize(thumbFolderList);
                    break;
                case Constant.TYPE_EMPTY_FOLDER:
                    updateSingleCountAndSize(emptyFolderList);
                    break;
            }
        }

    }

    public void clearStart() {
        if ( cacheList != null && cacheList.size() > 0) {
            clearThread++;
            new ClearCache().clearCache(getActivity(), new Callback() {
                @Override
                public void onFinished(boolean successed) {
                    cacheList.clear(); // 清空缓存
                    clearThread--;
                    clearHandler.sendEmptyMessage(CLEAR_CACHE);
                }
            });
        }
        if ( tempFileList != null && tempFileList.size() > 0) {
            clearThread++;
            FileUtil.delFiles(tempFileList, new Callback() {
                @Override
                public void onFinished(boolean successed) {
                    tempFileList.clear();
                    clearThread--;
                    clearHandler.sendEmptyMessage(CLEAR_TEMP_FILE);
                }
            });
        }
        if (emptyFolderList != null && emptyFolderList.size() > 0) {
            clearThread++;
            FileUtil.delFiles(emptyFolderList, new Callback() {

                @Override
                public void onFinished(boolean successed) {
                    emptyFolderList.clear();
                    clearThread--;
                    clearHandler.sendEmptyMessage(CLEAR_EMPTY_FOLDER);
                }
            });
        }

        if (thumbFolderList != null && thumbFolderList.size() > 0) {
            clearThread++;
            FileUtil.delFiles(thumbFolderList, new Callback() {

                @Override
                public void onFinished(boolean successed) {
                    thumbFolderList.clear();
                    clearThread--;
                    clearHandler.sendEmptyMessage(CLEAR_THUMB_FOLDER);
                }
            });
        }

        if (softwareList != null && softwareList.size() > 0) {
            clearThread++;
            FileUtil.delFiles(softwareList, new Callback() {

                @Override
                public void onFinished(boolean successed) {
                    softwareList.clear();
                    clearThread--;
                    clearHandler.sendEmptyMessage(CLEAR_SOFTWARE);
                }
            });
        }

        if ( apkList != null && apkList.size() > 0) {
            clearThread++;
            FileUtil.delFiles(apkList, new Callback() {

                @Override
                public void onFinished(boolean successed) {
                    apkList.clear();
                    clearThread--;
                    clearHandler.sendEmptyMessage(CLEAR_APK);
                }
            });
        }
        if (bigFileList != null && bigFileList.size() > 0) {
            clearThread++;
            FileUtil.delFiles(bigFileList, new Callback() {

                @Override
                public void onFinished(boolean successed) {
                    bigFileList.clear();
                    clearThread--;
                    clearHandler.sendEmptyMessage(CLEAR_BIG_FILE);
                }
            });
        }

        if (clearThread == 0) {
            Toast.makeText(getActivity(), getString(R.string.clear_no_data), Toast.LENGTH_SHORT).show();
            return;
        }

        ((ClearGarbageActivity)getActivity()).setStateDisplay(R.string.clearing);
        ((ClearGarbageActivity)getActivity()).setBtnDisplay(R.string.clearing);
        ((ClearGarbageActivity)getActivity()).setBtnDisplay(R.string.clearing);

        clickAble(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (clearThread == 0) {
                        clearHandler.sendEmptyMessage(CLEAR_FINISHED);
                        break;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    public void clearData() {
        cacheList = null;
        tempFileList = null;
        emptyFolderList = null;
        thumbFolderList = null;
        apkList = null;
        bigFileList = null;
        softwareList = null;

        if (traverseUtil != null) {
            traverseUtil.stopTraverse();
            traverseUtil = null;
        }

//        ((ClearGarbageActivity)getActivity()).setBtnDisplay(R.string.clear_start);
    }

    private void clearFinished() {
        boolean isClearAll = true;
        isClearAll &= (cacheList == null || cacheList.size() == 0);
        isClearAll &= (tempFileList == null || tempFileList.size() == 0);
        isClearAll &= (thumbFolderList == null || thumbFolderList.size() == 0);
        isClearAll &= (emptyFolderList == null || emptyFolderList.size() == 0);
        isClearAll &= (softwareList == null || softwareList.size() == 0);
        isClearAll &= (apkList == null || apkList.size() == 0);
        isClearAll &= (bigFileList == null || bigFileList.size() == 0);
        if (isClearAll) {
            ((ClearGarbageActivity)getActivity()).stopAnim();
            ((ClearGarbageActivity)getActivity()).setStateDisplay(R.string.clear_over);
//            ((ClearGarbageActivity)getActivity()).setBtnDisplay(R.string.clear_over);
            ((ClearGarbageActivity)getActivity()).setBtnDisplay(R.string.clear_scan_again);

        }
        updateClearSize();
        clickAble(true);
        Toast.makeText(getActivity(), getString(R.string.clear_success), Toast.LENGTH_SHORT).show();
    }

    public class TraverseHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Logger.getLogger().e("msg: " + msg.what);
            switch (msg.what) {
                case Constant.CACHE_FINISHED:
                    mSystemCacheProgress.setVisibility(View.GONE);
                    break;
                case Constant.EMPTY_FOLDER_SEARCH:
                    mEmptyFolderProgress.setVisibility(View.GONE);
                    break;
                case Constant.SOFTWARE_SEARCH:
                    mSoftwareLastProgress.setVisibility(View.GONE);
                    break;
                case Constant.FILE_SEARCH_UPDATE:

                    break;
                case Constant.ALL_SEARCH_FINISHED:
                    isTraversaling = false;

                    clickAble(true);

                    saveData();

                    ((ClearGarbageActivity)getActivity()).stopAnim();
                    ((ClearGarbageActivity)getActivity()).setStateDisplay(R.string.garbage_clear_scaned_text);
                    ((ClearGarbageActivity)getActivity()).setClearBtnVisibility(true);

                    break;
            }

            updateClearSize();

            super.handleMessage(msg);
        }
    }

    private Handler clearHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CLEAR_CACHE:
                    mSystemCacheProgress.setVisibility(View.GONE);
                    mSystemCacheSize.setVisibility(View.VISIBLE);
                    mSystemCacheCount.setText("( 0 )");
                    mSystemCacheSize.setText("0 B");
                    break;
                case CLEAR_TEMP_FILE:
                    mTempFileProgress.setVisibility(View.GONE);
                    mTempFileSize.setVisibility(View.VISIBLE);
                    mTempFileCount.setText("( 0 )");
                    mTempFileSize.setText("0 B");
                    break;
                case CLEAR_EMPTY_FOLDER:

                    mEmptyFolderProgress.setVisibility(View.GONE);
                    mEmptyFolderSize.setVisibility(View.VISIBLE);
                    mEmptyFolderCount.setText("( 0 )");
                    mEmptyFolderSize.setText("0 B");

                    break;
                case CLEAR_THUMB_FOLDER:
                    mThumbProgress.setVisibility(View.GONE);
                    mThumbSize.setVisibility(View.VISIBLE);
                    mThumbCount.setText("( 0 )");
                    mThumbSize.setText("0 B");
                    break;
                case CLEAR_SOFTWARE:
                    mSoftwareLastProgress.setVisibility(View.GONE);
                    mSoftwareLastSize.setVisibility(View.VISIBLE);
                    mSoftwareLastCount.setText("( 0 )");
                    mSoftwareLastSize.setText("0 B");
                    break;
                case CLEAR_APK:
                    mApkZipProgress.setVisibility(View.GONE);
                    mApkZipCount.setText("( 0 )");
                    mApkZipSize.setText("0 B");
                    break;
                case CLEAR_BIG_FILE:
                    mBigFileProgress.setVisibility(View.GONE);
                    mBigFileSize.setVisibility(View.VISIBLE);
                    mBigFileCount.setText("( 0 )");
                    mBigFileSize.setText("0 B");
                    break;
                case CLEAR_FINISHED:
                    clearFinished();
                    break;
            }
        };
    };

}
