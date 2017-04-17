package com.zx.zxtvsettings.Utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore.Files.FileColumns;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.claer.AppPackageDatabaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


public class SDInfoUtil {
    private Context context = null;

    public SDInfoUtil(Context context) {
        this.context = context;
    }

    public long[] getDataStorage() {
        long[] result = new long[2];
        File sdcardDir = Environment.getDataDirectory();
        StatFs statFs = new StatFs(sdcardDir.getPath());
        long blockSize = statFs.getBlockSize();
        long totalBlocks = statFs.getBlockCount();
        long availableBlocks = statFs.getAvailableBlocks();
        result[0] = availableBlocks * blockSize;
        result[1] = totalBlocks * blockSize;
        return result;
    }

    public long[] getDirectoryStorage(String path) {
        long[] result = new long[2];
        StatFs statFs = new StatFs(path);
        long blockSize = statFs.getBlockSize();
        long totalBlocks = statFs.getBlockCount();
        long availableBlocks = statFs.getAvailableBlocks();
        result[0] = availableBlocks * blockSize;
        result[1] = totalBlocks * blockSize;
        return result;
    }

    public static long getSizeOfPath(File f) {
        long size = 0;
        if (!f.isDirectory() && f.isFile()) {
            return f.length();
        }
        File flist[] = f.listFiles();

        if (flist.length > 0) {
            for (int i = 0; i < flist.length; i++) {
                if (flist[i].isDirectory()) {
                    size = size + getSizeOfPath(flist[i]);
                } else {
                    size = size + flist[i].length();
                }
            }
        }
        return size;

    }



    private String getSDCardSelection(String path) {
        String selection = FileColumns.DATA + " LIKE \'" + path + "/%' ";
        return selection;
    }

    // 筛选压缩包
    private String[] Compression = new String[] {
            "zip", "rar", "jar", "tar", "gz", "gzip", "ar", "cbr", "cbz", "tar.gz", "tar.bz2", "tar.xz", "tar.lzma"
    };

    public String getCompressionSelection() {
        StringBuilder selection = new StringBuilder();
        for (String s : Compression) {
            selection.append("(" + FileColumns.DATA + " LIKE \'%." + s + "\'" + ") OR ");
        }
        return selection.substring(0, selection.lastIndexOf(")") + 1);
    }

    // 筛选文档
    private String getDocSelection() {
        StringBuilder selection = new StringBuilder();
        Iterator<String> iter = sDocMimeTypesSet.iterator();
        while (iter.hasNext()) {
            selection.append("(" + FileColumns.MIME_TYPE + "=='" + iter.next() + "') OR ");
        }
        return selection.substring(0, selection.lastIndexOf(")") + 1);
    }

    private HashSet<String> sDocMimeTypesSet = new HashSet<String>() {
        private static final long serialVersionUID = 1L;
        {// 文档类型
            add("text/plain");
            add("application/pdf");
            add("application/msword");
            add("application/vnd.ms-excel");
        }
    };

    // 筛选图片
    private String getPictureSelection() {
        StringBuilder selection = new StringBuilder();
        selection.append("(" + FileColumns.MIME_TYPE + " LIKE \'image/%' ) ");

        return selection.substring(0, selection.lastIndexOf(")") + 1);
    }

    // 筛选音乐
    private String getMusicSelection() {
        StringBuilder selection = new StringBuilder();
        selection.append("(" + FileColumns.MIME_TYPE + "=='application/ogg' OR "
                + FileColumns.MIME_TYPE
                + " LIKE \'audio/%' ) ");

        return selection.substring(0, selection.lastIndexOf(")") + 1);
    }

    // 筛选视频
    private String getVideoSelection() {
        StringBuilder selection = new StringBuilder();
        selection.append("(" + FileColumns.MIME_TYPE + " LIKE \'video/%' ) ");
        return selection.substring(0, selection.lastIndexOf(")") + 1);
    }

    public ArrayList<String> getSoftwareResidues() {
        ArrayList<String> softwareName = new ArrayList<String>();
        AppPackageDatabaseHelper appDatabaseHelper = new AppPackageDatabaseHelper(context);
        File[] files = Environment.getExternalStorageDirectory().listFiles();
        List<PackageInfo> packageinfos = context.getPackageManager().getInstalledPackages(0);
        HashSet<String> hashet = new HashSet<String>();
        for (int i = 0; i < packageinfos.size(); i++) {
            // if ((packageinfos.get(i).applicationInfo.flags &
            // ApplicationInfo.FLAG_SYSTEM) == 0) {// 非系统应用
            String s = packageinfos.get(i).packageName;
            hashet.add(s);
        }
        copyDatabaseFile(context);
        for (int index = 0; index < files.length; index++) {
            String package_name = FileUtil.getNameFromFilepath(files[index].getAbsolutePath());
            if (package_name.equals("baidu") || package_name.equals("tencent")) {
                File[] fs = files[index].listFiles();
                String parent = FileUtil.getNameFromFilepath(files[index].getAbsolutePath());
                for (File f : fs) {
                    boolean is = isTrashes(
                            hashet,
                            parent + File.separator + FileUtil.getNameFromFilepath(f.getAbsolutePath()),
                            appDatabaseHelper);
                    if (is)
                        softwareName.add(f.getName());
                }
            } else {
                boolean is = isTrashes(hashet, package_name, appDatabaseHelper);
                if (is) {
                    softwareName.add(files[index].getName());
                }
            }
        }
        // for (String p : softwareName)
        // Util.Tlog("软件残留名：" + p);
        return softwareName;
    }

    private boolean isTrashes(HashSet<String> packages, String package_name,
            AppPackageDatabaseHelper appDatabaseHelper) {
        Cursor c = appDatabaseHelper.getPackageName(package_name);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                String name = c.getString(c
                        .getColumnIndexOrThrow(AppPackageDatabaseHelper.APP_PACKAGE_NAME));
                boolean is = packages.contains(name);
                if (!is) {
                    return true;
                }
            }
        }
        return false;
    }

    private void copyDatabaseFile(Context mContext) {
        String database_name = "app";
        String dirs = "/data/data/" + mContext.getPackageName() + "/databases/";
        File dir = new File(dirs);
        if (!dir.exists()) {
            try {
                dir.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File dest = new File(dir, database_name);
        if (dest.exists()) {
            return;
        }

        try {
            InputStream in = mContext.getResources().openRawResource(R.raw.app);
            int size = in.available();
            byte buf[] = new byte[size];
            in.read(buf);
            in.close();
            FileOutputStream out = new FileOutputStream(dest);
            out.write(buf);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
