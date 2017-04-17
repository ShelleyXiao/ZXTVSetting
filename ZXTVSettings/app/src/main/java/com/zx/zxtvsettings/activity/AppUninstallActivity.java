package com.zx.zxtvsettings.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.Tools;
import com.zx.zxtvsettings.adapter.AppUninstallAdapter;
import com.zx.zxtvsettings.app.AppBean;
import com.zx.zxtvsettings.app.GetAppList;

import java.util.List;



/**
 * User: ShaudXiao
 * Date: 2016-08-23
 * Time: 16:16
 * Company: zx
 * Description:
 * FIXME
 */

public class AppUninstallActivity extends BaseStatusBarActivity implements AdapterView.OnItemClickListener{

    ListView mAppUninstallLv;

    private List<AppBean> mAppList;
    private Receiver receiver;
    private AppUninstallAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_app_uninstall;
    }

    @Override
    protected void setupViews() {

        mAppUninstallLv = (ListView)findViewById(R.id.app_uninstall_lv);

        mAppUninstallLv.setOnItemClickListener(this);

        adapter = new AppUninstallAdapter(this, mAppList);
        mAppUninstallLv.setAdapter(adapter);
    }

    @Override
    protected void initialized() {
        GetAppList getAppInstance = new GetAppList(this);
        mAppList = getAppInstance.getUninstallAppList();
        adapter = new AppUninstallAdapter(this, mAppList);
        mAppUninstallLv.setAdapter(adapter);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "============onRestart========");
    }

    @Override
    protected void onStart() {
        super.onStart();
        receiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        this.registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent){
            //接收安装广播
            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {

                String packageName = intent.getDataString();
                List<ResolveInfo> list = Tools.findActivitiesForPackage(context, packageName);
                ResolveInfo info = list.get(0);
                PackageManager localPackageManager = context.getPackageManager();
                AppBean localAppBean = new AppBean();
                localAppBean.setIcon(info.activityInfo.loadIcon(localPackageManager));
                localAppBean.setName(info.activityInfo.loadLabel(localPackageManager).toString());
                localAppBean.setPackageName(info.activityInfo.packageName);
                localAppBean.setDataDir(info.activityInfo.applicationInfo.publicSourceDir);

                mAppList.add(localAppBean);
            }
            //接收卸载广播
            if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
                String receiverName = intent.getDataString();
                receiverName = receiverName.substring(8);
                AppBean appBean;
                for(int i = 0;i < mAppList.size();i++){
                    appBean = mAppList.get(i);
                    String packageName = appBean.getPackageName();
                    if(packageName.equals(receiverName)){
                        mAppList.remove(i);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Uri packageURI = Uri.parse("package:" + mAppList.get(position).getPackageName());
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
                packageURI);
        uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(uninstallIntent);
    }
}
