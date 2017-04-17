/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *                          www.pekall.com
 *
 * ----------------------------------------------------------------------------------------------
 */
package com.zx.zxtvsettings.claer;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.FileUtil;
import com.zx.zxtvsettings.Utils.SDInfoUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * @author haoanbang
 */
public class FileSDCardHelper {

    private StorageManager mStorageManager;
    private ArrayList<SDCardInfo> mSDCardInfos = new ArrayList<SDCardInfo>();
    public boolean isDoubleCardPhone;
    private static FileSDCardHelper mInstance;
    private Context mContext;

    private FileSDCardHelper(Context context) {
        try {
            mContext = context;
            mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            initSDcardInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FileSDCardHelper getInstance(Context context) {
        if (mInstance == null)
            mInstance = new FileSDCardHelper(context);

        return mInstance;
    }

    /**
     * 获取存储根路径*/
    private void initSDcardInfo() {
        mSDCardInfos.clear();
        Object[] storgerVolumes = InnerClass.StorageManager_getVolumeList(mStorageManager);
        String[] storgerPaths = InnerClass.StorageManager_getVolumePaths(mStorageManager);
        for (int i = 0; i < storgerVolumes.length; i++) {
            SDCardInfo sdCardInfo = new SDCardInfo();
            sdCardInfo.sdcard_id = InnerClass.StorageVolume_getStorageId(storgerVolumes[i]);// SD鍗D
            boolean isCanRemove = InnerClass.StorageVolume_isRemovable(storgerVolumes[i]);
            if (isCanRemove) {
                sdCardInfo.type = SDCardInfo.EXTERNAL_SD;
            } else {
                sdCardInfo.type = SDCardInfo.INTERNAL_SD;
            }
            sdCardInfo.path = storgerPaths[i];
            sdCardInfo.title = mContext.getString(R.string.root_path_text);
            sdCardInfo.state = InnerClass.StorageManager_getVolumeState(mStorageManager,
                    sdCardInfo.path);
            if (mSDCardInfos.indexOf(sdCardInfo) == -1
                    && !sdCardInfo.state.equals(Environment.MEDIA_REMOVED))
                mSDCardInfos.add(sdCardInfo);

            if (sdCardInfo.state.equals(Environment.MEDIA_MOUNTED)) {
//                mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
//                        + sdCardInfo.path)));
            }
        }
        if (mSDCardInfos.size() > 1) {
            isDoubleCardPhone = true;// 鍙孲D鍗�       
            }
        Collections.sort(mSDCardInfos, new FileComparator());
    }

    private class FileComparator implements Comparator<SDCardInfo> {
        @Override
        public int compare(SDCardInfo object1, SDCardInfo object2) {
            return object1.type - object2.type;
        }
    }

    private String getSDCardState(String path) {
        return InnerClass.StorageManager_getVolumeState(mStorageManager, path);
    }

    public boolean isAllSDCardReady() {
        for (int i = 0; i < mSDCardInfos.size(); i++) {
            if (isSDCardReady(mSDCardInfos.get(i).path)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSDCardReady(String path) {
        return Environment.MEDIA_MOUNTED.equals(getSDCardState(path));
    }

    private FilenameFilter hideFileFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
            return !filename.startsWith(".");
        }
    };

    private ArrayList<FileInfo> mInternalDatas = new ArrayList<FileInfo>();
    private ArrayList<FileInfo> mExternalDatas = new ArrayList<FileInfo>();

    public ArrayList<FileInfo> getDatas(int sdCardType) {
        switch (sdCardType) {
            case SDCardInfo.INTERNAL_SD:
                return mInternalDatas;
            case SDCardInfo.EXTERNAL_SD:
                return mExternalDatas;
        }
        return null;
    }

    public static final int FINDFILES_ROOTPATH_IS_NOT_DIRECTORY = 1;
    public static final int FINDFILES_ROOTPATH_IS_NOT_EXISTS = 2;
    public static final int FINDFILES_FILES_IS_EMPTY = 3;
    public static final int FINDFILES_SETUP_DATAS_SUCCESS = 4;
    public static final int FINDFILES_SHOWPROGRESSDIALOG = 5;
    public static final int FINDFILES_INTERRUPT = 6;

    // private static final int LARGECOUNT = 10;

    public void getFiles(String rootPath, Handler handler, int sdCardType) {
        isCancel = false;
        switch (sdCardType) {
            case SDCardInfo.INTERNAL_SD:
                mInternalDatas.clear();
                break;
            case SDCardInfo.EXTERNAL_SD:
                mExternalDatas.clear();
                break;
        }
        File rootFile = new File(rootPath);
        if (!rootFile.exists()) {
            handler.sendEmptyMessage(FINDFILES_ROOTPATH_IS_NOT_EXISTS);
            return;
        }
        if (!rootFile.isDirectory()) {
            handler.sendEmptyMessage(FINDFILES_ROOTPATH_IS_NOT_DIRECTORY);
            return;
        }
        /*
         * String[] files = rootFile
         * .list(mFileSettingsHelper.getBoolean(FileSettingsHelper
         * .KEY_SHOW_HIDEFILE, false) ? null : hideFileFilter); if (files ==
         * null || files.length <= 0) {
         * handler.sendEmptyMessage(FINDFILES_FILES_IS_EMPTY); return; }
         */
        setDatas(rootPath, rootFile, handler, sdCardType);
    }

    private void addDatas(FileInfo fileInfo, int sdCardType) {
        switch (sdCardType) {
            case SDCardInfo.INTERNAL_SD:
                if (!mInternalDatas.contains(fileInfo))
                    mInternalDatas.add(fileInfo);
                break;
            case SDCardInfo.EXTERNAL_SD:
                if (!mExternalDatas.contains(fileInfo))
                    mExternalDatas.add(fileInfo);
                break;
        }
    }

    public void cancelFindFiles() {
        isCancel = true;
    }

    private boolean isCancel;

    private void setDatas(final String rootPath, final File rootFile, final Handler handler,
            final int sdCardType) {
        handler.sendEmptyMessage(FINDFILES_SHOWPROGRESSDIALOG);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] files = rootFile.list(hideFileFilter);
                if (files == null || files.length <= 0) {
                    handler.sendEmptyMessage(FINDFILES_FILES_IS_EMPTY);
                    return;
                }
                if (isCancel) {
                    handler.sendEmptyMessage(FINDFILES_INTERRUPT);
                    return;
                }
                for (int i = 0; i < files.length; i++) {
                    FileInfo info = null;
                    FileInfo fileInfo = FileUtil.getFileInfo(
                            rootPath.endsWith("/") ? (rootPath + files[i]) : (rootPath
                                    + File.separator + files[i]),
                                        false);
                    if (fileInfo != null) {
                        addDatas(fileInfo, sdCardType);
                    }
                }
                handler.sendEmptyMessage(FINDFILES_SETUP_DATAS_SUCCESS);
            }
        }).start();
    }

    public boolean isShowChooseView() {
        if (isDoubleCardPhone)
            if (mSDCardInfos.size() > 1)
                return true;
        return false;
    }

    public boolean isHasSDcard() {
        if (mSDCardInfos.size() > 0)
            return true;
        else
            return false;
    }

    public ArrayList<SDCardInfo> getAllRoot() {
        return mSDCardInfos;
    }

    public SDCardInfo getRoot(int sdcardType) {
        for (int i = 0; i < mSDCardInfos.size(); i++) {
            if (mSDCardInfos.get(i).type == sdcardType)
                return mSDCardInfos.get(i);
        }
        return null;
    }

    public static List<FileInfo> getSDCardFiles(SDCardInfo info, Context mContext) {
        List<FileInfo> fileInfos = new ArrayList<FileInfo>();
        long largeValue = Constant.BIG_FILE_SIZE;
        File rootPath = new File(info.path);
        String ingor = rootPath + File.separator + "tencent";
        File[] files = rootPath.listFiles();
        if (files.length == 0) {
            return null;
        } else {
            for (File f : files) {
                if (f.getAbsolutePath().equals(ingor))
                    continue;
                if (FileUtil.getNameFromFilepath(f.getAbsolutePath()).startsWith("."))
                    continue;
                long size = SDInfoUtil.getSizeOfPath(f);
                if (size < largeValue) {
                    continue;
                }
                FileInfo fileInfo = new FileInfo();
                fileInfo.fileName = FileUtil.getNameFromFilepath(f.getAbsolutePath());
                fileInfo.fileSize = SDInfoUtil.getSizeOfPath(f);
                fileInfo.filePath = f.getAbsolutePath();
                fileInfo.isDir = f.isDirectory() ? true : false;
                fileInfos.add(fileInfo);
            }
//            Comparator<FileInfo> comparator = FileSortHelper.getInstance(FileSettingsHelper
//                    .getInstance(mContext)).sizeComparator;
//            Collections.sort(fileInfos, comparator);
            return fileInfos;
        }
    }
}
