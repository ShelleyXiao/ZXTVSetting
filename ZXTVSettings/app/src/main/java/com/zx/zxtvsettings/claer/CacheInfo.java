package com.zx.zxtvsettings.claer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;

import com.zx.zxtvsettings.Utils.Logger;
import com.zx.zxtvsettings.fragment.ClearHomeFragemnt;

import java.lang.reflect.Method;
import java.util.List;


/**
 * User: ShaudXiao
 * Date: 2016-08-25
 * Time: 14:57
 * Company: zx
 * Description:
 * FIXME
 */

public class CacheInfo {
    private final int CACHE_SIZE = 10 * 1024;
    private List<ClearInfo> cacheInfo = null;
    private Context mContext;
    private ClearHomeFragemnt.TraverseHandler mTraverseHandler;
    private int cache = 0;

    public CacheInfo(Context context) {
        this.mContext = context;
    }

    public void setParam(List<ClearInfo> cacheInfo, ClearHomeFragemnt.TraverseHandler handler) {
        this.cacheInfo = cacheInfo;
        this.mTraverseHandler = handler;
    }

    public void getCacheInfo() {
        if(null == cacheInfo || null == mTraverseHandler) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                PackageManager pm = mContext.getPackageManager();
                List<ApplicationInfo> applicationInfos = pm.getInstalledApplications(0);
                int size = applicationInfos.size();
                for(int i = 0; i < size; i++) {
                    ClearInfo info = new ClearInfo();
                    ApplicationInfo applicationInfo = applicationInfos.get(i);
                    Drawable drawable = applicationInfo.loadIcon(pm);
                    info.setIcon(drawableToBitmap(drawable));
                    info.setName(pm.getApplicationLabel(applicationInfo) + "");
                    info.setPath(applicationInfo.packageName);
                    cache++;
                    getpkginfo(applicationInfo.packageName, pm ,info);
                    Logger.getLogger().d("cache info: " + applicationInfo.packageName);
                }

                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        if (cache == 0) {
                            mTraverseHandler.sendEmptyMessage(Constant.CACHE_FINISHED);
                            break;
                        }
                    }
                }
            }
        }).start();
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        bitmap = Bitmap.createBitmap(w, h, config);
        //注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);

        return bitmap;
    }

    private void getpkginfo(String pkg, PackageManager pm, ClearInfo info) {
        try {
            Method getPackageSizeInfo = pm.getClass().getMethod("getPackageSizeInfo", String.class,
                    IPackageStatsObserver.class);
            getPackageSizeInfo.invoke(pm, pkg, new PkgSizeObserver(info));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class cacheObserver extends IPackageStatsObserver.Stub {
        private Callback callback = null;

        public cacheObserver(Callback callback) {
            this.callback = callback;
        }

        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                throws RemoteException {
            if (pStats != null && pStats.cacheSize <= CACHE_SIZE) {
                callback.onFinished(true);
            }
        }
    }

    private class PkgSizeObserver extends IPackageStatsObserver.Stub {
        private ClearInfo infos;

        public PkgSizeObserver(ClearInfo info) {
            this.infos = info;
        }

        @SuppressLint("NewApi")
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) {
            if (pStats != null) {
                if (infos != null && pStats.cacheSize > CACHE_SIZE) {
                    infos.setSize(pStats.cacheSize);
                    infos.setState(Constant.CACHE_STATE);
                    infos.setSelected(true);
                    cacheInfo.add(infos);
                }
            }
            cache--;
        }
    }
}
