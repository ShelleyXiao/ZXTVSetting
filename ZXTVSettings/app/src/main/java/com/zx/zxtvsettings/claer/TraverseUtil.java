package com.zx.zxtvsettings.claer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.FileUtil;
import com.zx.zxtvsettings.Utils.Logger;
import com.zx.zxtvsettings.Utils.SDInfoUtil;
import com.zx.zxtvsettings.fragment.ClearHomeFragemnt;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * User: ShaudXiao
 * Date: 2016-08-24
 * Time: 17:24
 * Company: zx
 * Description:
 * FIXME
 */

public class TraverseUtil {
    // 临时文件
    private final String[] tempFile = new String[]{
            "log", "temp", "tmp", "??", "??~", "~", "_mp"
    };
    // 缩略图文件夹
    private final String[] thumbFolder = new String[]{
            ".thumbnails", "thumb", "thumbnails", ".thumb"
    };
    // 音乐文件
    private final String[] musicFile = {
            "mp3", "ape", "flac", "wav", "m4a", "cd", "aac+", "md", "asf", "ra", "vqf", "mid", "ogg", "aiff", "au",
            "amr", "wma"
    };
    // 视频文件
    private final String[] videoFile = new String[]{
            "3gp", "mp4", "rmvb", "mpeg", "mpg", "avi", "flv", "f4v", "wmv", "mkv", "dat", "navi", "asf", "mov", "webm"
    };

    // 压缩包
    private final String[] zipFile = new String[]{
            "zip", "rar", "jar", "tar", "gz", "gzip", "ar", "cbr", "cbz", "tar.gz", "tar.bz2", "tar.xz", "tar.lzma"
    };

    private final int THREAD_FILE = 0;
    private final int THREAD_EMPTY_FOLDER = 0;
    private final int THREAD_TRAVERSE = 2;
    private final int THREAD_SOFTWARE = 3;

    private final int THREAD_NUM = 5; // 5个线程

    private ApkSearchUtil mApkSearchUtil;
    private ClearHomeFragemnt.TraverseHandler mTraverseHandler;
    private Context mContext;
    private ArrayList<String> allSoftwareList = null;

    private ThreadGroup mThreadGroup = new ThreadGroup("traverse");
    private TraverseThread mTraverseThread = null;
    private Callback mTraverseCallback = null;
    private String path = null;

    private ArrayList<ClearInfo> emptyFolderList;
    private ArrayList<ClearInfo> bigFileList;
    private ArrayList<ClearInfo> apkList;
    private ArrayList<ClearInfo> thumbFolderList;
    private ArrayList<ClearInfo> tempFileList;
    private ArrayList<ClearInfo> softwareList; // 软件残留


    public TraverseUtil(Context context) {
        this.mContext = context;
        this.mApkSearchUtil = new ApkSearchUtil(mContext);
    }

    public void setHandler(ClearHomeFragemnt.TraverseHandler handler) {
        this.mTraverseHandler = handler;
    }

    public void stopTraverse() {
        if (null != mThreadGroup) {
            mThreadGroup.interrupt();
            mThreadGroup = null;
        }
    }

    public void setTraverse(Callback traverse) {
        this.mTraverseCallback = traverse;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void startTraverse() {
        if (null == path || null == mTraverseCallback || null == mTraverseHandler) {
            return;
        }

        File file = new File(path);
        File[] files = file.listFiles();
        if (null != files && files.length > 0) {
            List<File> fileList = new ArrayList<File>();
            List<File> folderList = new ArrayList<File>();
            for (File f : files) {
                if (f.isDirectory()) {
                    folderList.add(f);
                } else {
                    fileList.add(f);
                }
            }

            mTraverseThread = new TraverseThread(mThreadGroup, "software", folderList, THREAD_SOFTWARE);
            mTraverseThread.start();

            traverseFolder(folderList);

            mTraverseThread = new TraverseThread(mThreadGroup, "file", fileList, THREAD_FILE);
            mTraverseThread.start();

            mTraverseThread = new TraverseThread(mThreadGroup, "empeyFolder", folderList, THREAD_EMPTY_FOLDER);
            mTraverseThread.start();
        } else {
            return;
        }

        //扫描 ThreadGroup
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {

                    try {
                        Thread.sleep(Constant.UPDATE_CLEAR_DATA);
                        if (null == mThreadGroup) {
                            break;
                        }

                        if (mThreadGroup.activeCount() == 0) {
                            mTraverseCallback.onFinished(true);
                            stopTraverse();
                            break;
                        }

                        mTraverseHandler.sendEmptyMessage(Constant.FILE_SEARCH_UPDATE);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    public void setEmptyFolder(ArrayList<ClearInfo> emptyFolderList) {
        this.emptyFolderList = emptyFolderList;
    }

    public void setBigFile(ArrayList<ClearInfo> bigFileList) {
        this.bigFileList = bigFileList;
        Collections.synchronizedList(this.bigFileList);
    }

    public void setAPK(ArrayList<ClearInfo> apkList) {
        this.apkList = apkList;
        Collections.synchronizedList(this.apkList);
    }

    public void setThumbFolder(ArrayList<ClearInfo> thumbFolderList) {
        this.thumbFolderList = thumbFolderList;
        Collections.synchronizedList(this.thumbFolderList);
    }

    public void setTempFile(ArrayList<ClearInfo> tempFileList) {
        this.tempFileList = tempFileList;
        Collections.synchronizedList(this.tempFileList);
    }

    public void setSoftware(ArrayList<ClearInfo> softwareList) {
        this.softwareList = softwareList;
    }

    private boolean isMusic(String name) {
        for (String str : musicFile) {
            if (name.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean isVideo(String name) {
        for (String str : videoFile) {
            if (name.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean isZip(String name) {
        for (String str : zipFile) {
            if (name.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTempFile(File file) {
        String suffixName = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        for (String str : tempFile) {
            if (str.equalsIgnoreCase(suffixName)) {
                return true;
            }
        }

        return false;
    }

    private boolean isThumbFolder(File file) {
        String name = file.getName();
        for (String str : thumbFolder) {
            if (name.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAPK(String name) {
        return name.substring(name.lastIndexOf(".") + 1).equalsIgnoreCase("apk");
    }

    private boolean isBigFile(File file) {
        return file.length() >= Constant.BIG_FILE_SIZE;
    }

    private boolean isSoftware(String name) {
        return allSoftwareList.contains(name);
    }

    private String[] filterEmptyFolder = new String[]{
            ".android_secure"
    };

    private boolean isEmptyFolder(File file) {
        for (int i = 0; i < filterEmptyFolder.length; i++) {
            if (filterEmptyFolder[i].equalsIgnoreCase(file.getName().toString())) {
                return false;
            }
        }

        boolean isEmpty = false;
        if (file.isFile() || file.length() == 0) {
            return false;
        }

        File[] temp = file.listFiles();
        if (null == temp || temp.length == 0) {
            return true;
        } else {
            int size = temp.length;
            for (int i = 0; i < size; i++) {
                if (temp[i].isDirectory()) {
                    File[] temp1 = temp[i].listFiles();
                    if (null == temp1 || temp1.length == 0) {
                        isEmpty &= true;
                    } else {
                        for (int j = 0; j < temp[i].length(); j++) {
                            File tempFile1 = temp1[i];
                            if (tempFile1.isDirectory()) {
                                File[] temp2 = tempFile1.listFiles();
                                if (null == temp2 || temp2.length == 0) {
                                    isEmpty &= true;
                                } else {
                                    for (int k = 0; k < temp2.length; k++) {
                                        File tempFile2 = temp2[i];
                                        File[] temp3 = tempFile2.listFiles();
                                        if (null == temp3 || temp3.length == 0) {
                                            isEmpty &= true;
                                        } else {
                                            return false;
                                        }
                                    }
                                }
                            } else {
                                return false;
                            }
                        }
                    }
                } else {
                    return false;
                }

            }
        }

        return isEmpty;
    }


    private Bitmap getFileIcon() {
        return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.file_icon_default);
    }

    private Bitmap getFolderIcon() {
        return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.folder);
    }

    private Bitmap getBigFileIcon(String name) {
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ico_file);
        String suffixName = name.substring(name.lastIndexOf(".") + 1);
        if (isAPK(suffixName)) {
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ico_apk);
        } else if (suffixName.equalsIgnoreCase("ptp")) {
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ico_theme);
        } else if (isVideo(suffixName)) {
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ico_video);
        } else if (isMusic(suffixName)) {
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ico_music);
        } else if (isZip(suffixName)) {
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ico_zip);
        }
        return bmp;
    }

    private void traverseFile(File file) {
        ClearInfo clearInfo = null;
        if (isTempFile(file)) {
            clearInfo = new ClearInfo();
            clearInfo.setName(file.getName());
            clearInfo.setPath((file.getAbsolutePath()));
            clearInfo.setSize(file.length());
            clearInfo.setIcon(getFileIcon());
            clearInfo.setSelected(true);
            tempFileList.add(clearInfo);
        } else if (isAPK(file.getName())) {
            apkList.add(mApkSearchUtil.getApkInfo(file));
        } else if (isBigFile(file)) {
            clearInfo = new ClearInfo();
            clearInfo.setName(file.getName());
            clearInfo.setPath((file.getAbsolutePath()));
            clearInfo.setSize(file.length());
            clearInfo.setIcon(getBigFileIcon(file.getName()));
            clearInfo.setSelected(true);
        }
    }

    private void traverseFolder(List<File> folderList) {
        Collections.shuffle(folderList);
        int folderSize = folderList.size();
        if (folderSize < THREAD_NUM) {
            for (int i = 0; i < folderSize; i++) {
                List<File> temp = folderList.subList(i, i + 1);
                mTraverseThread = new TraverseThread(mThreadGroup, "thread" + i, temp, THREAD_TRAVERSE);
                mTraverseThread.start();
            }

        }  else {
            int length = folderSize / THREAD_NUM;
            int start = 0;
            int end = length;
            for (int i = 0; i < THREAD_NUM; i++) {
                try {
                    List<File> temp = folderList.subList(start, end);
                    mTraverseThread = new TraverseThread(mThreadGroup, "thread" + i, temp, THREAD_TRAVERSE);
                    mTraverseThread.start();

                    start = end;
                    end += length;

                    if(start < folderSize && end > folderSize) {
                        end = folderSize;
                    } else if(start >= folderSize) {
                        break;
                    }

                    if(i == THREAD_NUM - 2) {
                        end = folderSize;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void traverse(File file) {
        if (FileUtil.isFilterFolder(file.getName())) { // 不扫描的目录
            return;
        }
        // System.out.println(file.getAbsolutePath());
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        int size = files.length;
        ClearInfo clearInfo = null;
        for (int i = 0; i < size; i++) {
            if (files[i].isDirectory()) {
                if (isThumbFolder(files[i])) {
                    clearInfo = new ClearInfo();
                    clearInfo.setName(files[i].getName());
                    clearInfo.setPath(files[i].getAbsolutePath());
                    clearInfo.setSelected(true);
                    clearInfo.setIcon(getFolderIcon());
                    clearInfo.setSize(getFolderSize(files[i]));
                    thumbFolderList.add(clearInfo);
                } else {
                    traverse(files[i]);
                }
            } else {
                if (isTempFile(files[i])) {
                    clearInfo = new ClearInfo();
                    clearInfo.setName(files[i].getName());
                    clearInfo.setPath(files[i].getAbsolutePath());
                    clearInfo.setIcon(getFileIcon());
                    clearInfo.setSize(files[i].length());
                    clearInfo.setSelected(true);
                    tempFileList.add(clearInfo);
                } else if (isAPK(files[i].getName())) {
                    apkList.add(mApkSearchUtil.getApkInfo(files[i]));
                } else if (isBigFile(files[i])) {
                    clearInfo = new ClearInfo();
                    clearInfo.setName(files[i].getName());
                    clearInfo.setPath(files[i].getAbsolutePath());
                    clearInfo.setIcon(getBigFileIcon(files[i].getName()));
                    clearInfo.setSize(files[i].length());
                    bigFileList.add(clearInfo);
                }
            }
        }
    }

    private void getEmptyFolder(File folder) {
        if (isEmptyFolder(folder)) {
            ClearInfo clearInfo = new ClearInfo();
            clearInfo.setName(folder.getName());
            clearInfo.setPath(folder.getAbsolutePath());
            clearInfo.setIcon(getFolderIcon());
            clearInfo.setSize(getEmptyFolderSize(folder));
            clearInfo.setSelected(true);

            emptyFolderList.add(clearInfo);
        }
    }

    private void getSofeware(File file) {

        if (isSoftware(file.getName())) {
            ClearInfo clearInfo = new ClearInfo();
            clearInfo.setName(file.getName());
            clearInfo.setPath(file.getAbsolutePath());
            clearInfo.setIcon(getFolderIcon());
            clearInfo.setSize(getFolderSize(file));
            clearInfo.setSelected(true);

            softwareList.add(clearInfo);
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null == files || files.length == 0) {
                return;
            } else {
                int size = files.length;

                for (int i = 0; i < size; i++) {
                    if (isSoftware(files[i].getName())) {
                        ClearInfo clearInfo = new ClearInfo();
                        clearInfo.setName(files[i].getName());
                        clearInfo.setPath(files[i].getAbsolutePath());
                        clearInfo.setIcon(getFolderIcon());
                        clearInfo.setSize(getFolderSize(files[i]));
                        clearInfo.setSelected(true);
                        softwareList.add(clearInfo);
                    }
                }
            }
        }
    }

    private long getEmptyFolderSize(File folder) {
        long size = 0;
        File[] files = folder.listFiles();
        if (null != files) {
            if (files.length == 0) {
                return folder.length();
            } else {
                for (File f : files) {
                    if (f.isFile()) {
                        size += f.length();
                    } else {
                        size += f.length();
                        size += getFolderSize(f);
                    }
                }
            }
        } else {
            size = folder.length();
        }
        return size;
    }

    private long getFolderSize(File file) {
        long size = 0;
        File[] files = file.listFiles();
        if (null != files) {
            if (files.length == 0) {
                return file.length();
            } else {
                for (File f : files) {
                    if (f.isFile()) {
                        size += f.length();
                    } else {
                        size += getFolderSize(f);
                    }
                }
            }
        }
        return size;
    }

    private class TraverseThread extends Thread {

        private List<File> mFileList;
        private int type = -1;

        public TraverseThread(ThreadGroup group, String name, List<File> fileList, int type) {
            super(group, name);

            this.mFileList = fileList;
            this.type = type;
        }

        @Override
        public void run() {
            if (type == THREAD_SOFTWARE) {
                allSoftwareList = new SDInfoUtil(mContext).getSoftwareResidues();
                if (null == allSoftwareList || allSoftwareList.size() == 0) {
                    return;
                }
            }

            File folder = null;
            for (int i = 0; i < mFileList.size(); i++) {
                folder = mFileList.get(i);
                Logger.getLogger().d("scan File: " + folder.getAbsolutePath());
                if (type == THREAD_FILE) {
                    traverseFile(folder);
                } else if (type == THREAD_EMPTY_FOLDER) {
                    getEmptyFolder(folder);
                } else if (type == THREAD_SOFTWARE) {
                    getSofeware(folder);
                }  else if (type == THREAD_TRAVERSE) {
                    traverse(folder);
                }
            }

            if (type == THREAD_EMPTY_FOLDER) {
                mTraverseHandler.sendEmptyMessage(Constant.EMPTY_FOLDER_SEARCH);
            } else if (type == THREAD_SOFTWARE) {
                mTraverseHandler.sendEmptyMessage(Constant.SOFTWARE_SEARCH);
            }
        }


    }
}
