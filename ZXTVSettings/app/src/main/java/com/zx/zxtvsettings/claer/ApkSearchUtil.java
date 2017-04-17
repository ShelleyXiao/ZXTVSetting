package com.zx.zxtvsettings.claer;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import com.zx.zxtvsettings.R;

import java.io.File;
import java.lang.reflect.Method;


/**
 * User: ShaudXiao
 * Date: 2016-08-24
 * Time: 14:41
 * Company: zx
 * Description:
 * FIXME
 */

public class ApkSearchUtil {

    private Context mContext;

    public ApkSearchUtil(Context context) {
        this.mContext = context;
    }

    public String getApKName(PackageInfo localPackageInfo, String paramString) {
        CharSequence localCharSequence = null;
        if(null != localPackageInfo ) {
            try {
                Class<?> localClass = Class.forName("android.content.res.AssetManager");
                Object localObject = localClass.getConstructor((Class[]) null).newInstance(
                        (Object[]) null);
                Class<?>[] arrayOfClass = new Class<?>[1];
                arrayOfClass[0] = String.class;
                Method localMethod = localClass.getDeclaredMethod("addAssetPath", arrayOfClass);
                Object[] arrayOfObject = new Object[1];
                arrayOfObject[0] = paramString;
                localMethod.invoke(localObject, arrayOfObject);
                Resources localResources1 = mContext.getResources();
                Resources localResources2 = new Resources((AssetManager) localObject,
                        localResources1.getDisplayMetrics(), localResources1.getConfiguration());
                if (localPackageInfo.applicationInfo.labelRes != 0)
                    localCharSequence = localResources2
                            .getText(localPackageInfo.applicationInfo.labelRes);
            } catch (Exception e) {
                e.printStackTrace();
                localCharSequence = null;
            }

        }

        return localCharSequence.toString();
    }

    public ClearInfo getApkInfo(File file) {
        ClearInfo info = new ClearInfo();
        PackageManager pm = mContext.getPackageManager();
        String apkPath = null;
        info.setSize(file.length());
        apkPath = file.getAbsolutePath();
        info.setPath(apkPath);

        PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if(null == packageInfo) {
            info.setIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.catalog_apk));
            info.setName(apkPath.substring(apkPath.lastIndexOf("/") + 1, apkPath.lastIndexOf(".")));
            info.setState(Constant.UNKNOWN);
            info.setVersion(mContext.getString(R.string.unknown));
            return info;
        }

        ApplicationInfo appInfo = packageInfo.applicationInfo;
        CharSequence appName = null;
        if(appInfo.labelRes != 0) {
            appName = getApKName(packageInfo, apkPath);
        } else {
            appName = pm.getApplicationLabel(appInfo).toString();
        }

        if(null == appName || appName.equals("")) {
            appName = file.getName().substring(0, file.getName().lastIndexOf("."));
        }
        info.setName(appName.toString());
        appInfo.sourceDir = apkPath;
        appInfo.publicSourceDir = apkPath;
        BitmapDrawable apkIcon = (BitmapDrawable)appInfo.loadIcon(pm);
        info.setIcon(apkIcon.getBitmap());

        String packageName = packageInfo.packageName;
        info.setPackageName(packageName);

        String versionName = packageInfo.versionName;
        if (!(versionName.startsWith("v") || versionName.startsWith("V"))) {
            versionName = "V" + versionName;
        }
        info.setVersion(versionName);

        info.setState(isApkInstalled(packageName));

        return info;
    }

    private int isApkInstalled(String pkgName) {
        try {
            mContext.getPackageManager().getPackageInfo(pkgName, 0);
            return Constant.INSTALLED;
        } catch (PackageManager.NameNotFoundException e) {
            return Constant.UNINSTALLED;
        }
    }

}
