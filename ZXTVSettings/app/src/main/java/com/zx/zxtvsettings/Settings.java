package com.zx.zxtvsettings;

import android.app.Application;
import android.content.Context;

/**
 * User: ShaudXiao
 * Date: 2016-08-19
 * Time: 10:06
 * Company: zx
 * Description:
 * FIXME
 */

public class Settings extends Application {

    private static Settings Settings;

    public static Settings getInstance() {
        return Settings;
    }

    public static Context getContext() {
        return Settings;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Settings = this;

//        if(BuildConfig.LOG_DEBUG == true) {
//            MLog.init(true);
//        } else {
//            MLog.init(false);
//        }

    }


}
