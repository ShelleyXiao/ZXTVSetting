package com.zx.zxtvsettings.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.NetWorkUtil;

import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * Created by WXT on 2016/7/8.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final int DEFAULT_TRAN_DUR_ANIM = 300;

    public static final String TAG = BaseActivity.class.getSimpleName();
    private boolean isNetWork = true;
    public Context context;

    protected Unbinder mUnbinder;

    protected String category , genre;

    protected View moveView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setFlags(flag,flag);
        context = this.getApplicationContext();
        int layoutId = getLayoutId();
        if (layoutId != 0) {
            setContentView(layoutId);
            // 删除窗口背景
            getWindow().setBackgroundDrawable(null);

            mUnbinder = ButterKnife.bind(this);
        }

        Bundle bundle = getIntent().getExtras();


        //向用户展示信息前的准备工作在这个方法里处理
        preliminary();
    }

    public void onResume() {
        super.onResume();

        if(!NetWorkUtil.isNetWorkAvailable(this)) {
            netWorkNo();
        }
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    /**
     * 向用户展示信息前的准备工作在这个方法里处理
     */
    protected void preliminary() {
        // 初始化组件
        setupViews();

        // 初始化数据
        initialized();
    }

    /**
     * 获取全局的Context
     *
     * @return {@link #context = this.getApplicationContext();}
     */
    public Context getContext() {
        return context;
    }

    /**
     * 布局文件ID
     *
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * 初始化组件
     */
    protected abstract void setupViews();

    /**
     * 初始化数据
     */
    protected abstract void initialized();

    protected void setFocusMoveView(int id) {
        moveView = findViewById(id);
    }

    /**
     * 通过Class跳转界面
     **/
    public void startActivity(Class<?> cls) {
        startActivity(cls, null);
    }

    /**
     * 含有Bundle通过Class跳转界面
     **/
    public void startActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }


    /**
     * 通过Action跳转界面
     **/
    public void startActivity(String action) {
        startActivity(action, null);
    }

    /**
     * 含有Bundle通过Action跳转界面
     **/
    public void startActivity(String action, Bundle bundle) {
        Intent intent = new Intent();
        intent.setAction(action);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    /**
     * 含有Bundle通过Class打开编辑界面
     **/
    public void startActivityForResult(Class<?> cls, Bundle bundle, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void showToastLong(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public void showToastShort(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 监听广播
     */
    class MyConnectionChanngeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
                showToastShort("网络断开连接");
                netWorkNo();
                isNetWork = false;
            } else {
                netWorkYew();
            }
        }
    }

    /**
     * 带有右进右出动画的退出
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    /**
     * 默认退出
     */
    public void defaultFinish() {
        super.finish();
    }

    public void netWorkNo() {
//        ConfirmDialog.Builder dialog = new ConfirmDialog.Builder(this);
//        dialog.setMessage(R.string.no_network_promat);
//        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        }).setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                gotoNetWorkSetting();
//                dialog.dismiss();
//            }
//        });
//        dialog.create().show();
    }

    public void netWorkYew() {
        if (!isNetWork) {
            showToastShort("网络连接成功");
            isNetWork = true;
        }
    }

    public void gotoNetWorkSetting() {
        Intent intent = null;
        //判断手机系统的版本  即API大于10 就是3.0或以上版本
        if (android.os.Build.VERSION.SDK_INT > 10) {
            intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
        } else {
            intent = new Intent("android.settings.WIFI_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        BaseActivity.this.startActivity(intent);
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        //低内存运行
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch(level){
            //UI组件不可见时
            case TRIM_MEMORY_UI_HIDDEN:
                break;
        }

    }

    public float getDimension(int id) {
        return getResources().getDimension(id);
    }

    /**
     * 提供选中放大的效果
     */
    public View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {

//            int focus = 0;
//            if (hasFocus) {
//                focus = R.anim.enlarge;
//            } else {
//                focus = R.anim.decrease;
//            }
//            //如果有焦点就放大，没有焦点就缩小
//            Animation mAnimation = AnimationUtils.loadAnimation(
//                    getContext(), focus);
//            mAnimation.setBackgroundColor(Color.TRANSPARENT);
//            mAnimation.setFillAfter(hasFocus);
//            v.startAnimation(mAnimation);
//            mAnimation.start();
//            v.bringToFront();

            if(hasFocus) {
                setFocusView(v);
                moveView.setVisibility(View.VISIBLE);
                flyWhiteBorder(v, moveView, 1.1f, 1.1f);
            } else {
                cancleFocusView(v);
                moveView.setVisibility(View.GONE);
            }
        }
    };


    protected void setFocusView(final View v) {
        v.bringToFront();

        if(v instanceof ViewGroup) {
            // 处理其他动作
            ViewGroup group = (ViewGroup)v;
        } else {

        }

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, View.SCALE_X, 1.10F);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, View.SCALE_Y, 1.10F);

        AnimatorSet scale = new AnimatorSet();
        scale.setDuration(DEFAULT_TRAN_DUR_ANIM);
        scale.setInterpolator(new AccelerateDecelerateInterpolator());
        scale.playTogether(scaleX, scaleY);
        scale.start();
        scale.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //可以处理内部子控件动画逻辑
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });

    }

    protected void cancleFocusView(final View v) {
        if(v instanceof ViewGroup) {
            // 处理其他动作
            ViewGroup group = (ViewGroup)v;
        } else {

        }

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, View.SCALE_X, 1.0F);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, View.SCALE_X, 1.0F);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(DEFAULT_TRAN_DUR_ANIM);
        set.setInterpolator(new AccelerateInterpolator());
        set.playTogether(scaleX, scaleY);
        set.start();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
    }

    public void flyWhiteBorder(final View focusView,  View moveView, float scaleX, float scaleY) {
        int newWidth = 0;
        int newHeight = 0;
        int oldWidth = 0;
        int oldHeight = 0;

        int newX = 0;
        int newY = 0;

        if (focusView != null) {
            // 有一点偏差,需要进行四舍五入.
            newWidth = (int) (Math.rint(focusView.getMeasuredWidth() * scaleX));
            newHeight = (int) (Math.rint(focusView.getMeasuredHeight() * scaleY));
            oldWidth = moveView.getMeasuredWidth();
            oldHeight = moveView.getMeasuredHeight();
            Rect fromRect = findLocationWithView(moveView);
            Rect toRect = findLocationWithView(focusView);
            int x = toRect.left - fromRect.left;
            int y = toRect.top - fromRect.top;
            newX = x - Math.abs(focusView.getMeasuredWidth() - newWidth) / 2;
            newY = y - Math.abs(focusView.getMeasuredHeight() - newHeight) / 2;
        }

        ObjectAnimator transAnimatorX = ObjectAnimator.ofFloat(moveView, "translationX", newX);
        ObjectAnimator transAnimatorY = ObjectAnimator.ofFloat(moveView, "translationY", newY);
        // BUG，因为缩放会造成图片失真(拉伸).
        // hailong.qiu 2016.02.26 修复 :)
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofInt(new ScaleView(moveView), "width", oldWidth,
                (int) newWidth);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofInt(new ScaleView(moveView), "height", oldHeight,
                (int) newHeight);
        //
        AnimatorSet mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(transAnimatorX, transAnimatorY, scaleXAnimator, scaleYAnimator);
        mAnimatorSet.setInterpolator(new DecelerateInterpolator(1));
        mAnimatorSet.setDuration(DEFAULT_TRAN_DUR_ANIM);
        mAnimatorSet.start();
    }

    public Rect findLocationWithView(View view) {
        if(null == moveView) {
            throw new IllegalArgumentException("moveview is null!!");
        }
        ViewGroup root = (ViewGroup) moveView.getParent();
        Rect rect = new Rect();
        root.offsetDescendantRectToMyCoords(view, rect);
        return rect;
    }

    public class ScaleView {
        private View view;
        private int width;
        private int height;

        public ScaleView(View view) {
            this.view = view;
        }

        public int getWidth() {
            return view.getLayoutParams().width;
        }

        public void setWidth(int width) {
            this.width = width;
            view.getLayoutParams().width = width;
            view.requestLayout();
        }

        public int getHeight() {
            return view.getLayoutParams().height;
        }

        public void setHeight(int height) {
            this.height = height;
            view.getLayoutParams().height = height;
            view.requestLayout();
        }
    }


}
