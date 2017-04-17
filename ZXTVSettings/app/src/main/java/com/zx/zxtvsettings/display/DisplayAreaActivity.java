package com.zx.zxtvsettings.display;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.com.android.displayd.DisplayBoundInfo;
import com.com.android.displayd.DisplayDAgent;
import com.com.android.displayd.DisplayModeInfo;
import com.zx.zxtvsettings.R;


public class DisplayAreaActivity extends Activity {
    private static final String TAG = "DisplayAreaActivity";

    private DisplayDAgent mDisplayd;
    private DisplayModeInfo curDisplayMode;
    private DisplayBoundInfo curDisplayBound;

    private TextView switcher;
    private ImageView areaUp, areaDown, areaLeft, areaRight;
    private boolean isLegalSetBound = true;
    private int limitX, limitY;

    private int stateCount;
    private final int MANAGE_SHRINK = 0;
    private final int MANAGE_EXPAND = 1;

    private final int ADJUST_TOP = 0;
    private final int ADJUST_BOTTOM = 1;
    private final int ADJUST_LEFT = 2;
    private final int ADJUST_RIGHT = 3;
    private final Object mLock = new Object();

    private int currentState() {
        return stateCount % 2;
    }

    private void switchStateLocked() {
        synchronized (mLock) {
            stateCount++;
            switch (currentState()) {
                case MANAGE_EXPAND:
                    switcher.setText(R.string.area_switch_button_expand);
                    break;
                case MANAGE_SHRINK:
                    switcher.setText(R.string.area_switch_button_shrink);
                    break;
                default:
                    break;
            }
        }
    }

    private int nextBoundX(int bound, int adjust_xx) {
        int limit = 0;
        if (adjust_xx == ADJUST_TOP || adjust_xx == ADJUST_BOTTOM)
            limit = limitY;
        else if (adjust_xx == ADJUST_LEFT || adjust_xx == ADJUST_RIGHT)
            limit = limitX;

        int next = 0;
        switch (currentState()) {
            case MANAGE_EXPAND:
                next = bound - 10;
                if (next < 0)
                    next = 0;
                break;
            case MANAGE_SHRINK:
                next = bound + 10;
                if (next > limit)
                    next = limit;
                break;
            default:
                break;
        }
        return next;
    }

    private void adjustBoundLocked(int border) {
        synchronized (mLock) {
            if (!isLegalSetBound)
                return;
            DisplayBoundInfo bound = new DisplayBoundInfo(-1);
            if (border == ADJUST_TOP) {
                bound.Top = nextBoundX(curDisplayBound.Top, border);
                if (bound.Top == curDisplayBound.Top)
                    return;
                curDisplayBound.Top = bound.Top;
            } else if (border == ADJUST_BOTTOM) {
                bound.Bottom = nextBoundX(curDisplayBound.Bottom, border);
                if (bound.Bottom == curDisplayBound.Bottom)
                    return;
                curDisplayBound.Bottom = bound.Bottom;
            } else if (border == ADJUST_LEFT) {
                bound.Left = nextBoundX(curDisplayBound.Left, border);
                if (bound.Left == curDisplayBound.Left)
                    return;
                curDisplayBound.Left = bound.Left;
            } else if (border == ADJUST_RIGHT) {
                bound.Right = nextBoundX(curDisplayBound.Right, border);
                if (bound.Right == curDisplayBound.Right)
                    return;
                curDisplayBound.Right = bound.Right;
            } else
                return;
            Log.i(TAG, "set bounds");
            bound.dump(TAG);
            try {
                mDisplayd.setBounds(bound);
            } catch (RemoteException e) {
            }
        }
    }

    View.OnClickListener areaAdjustListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ImageView iv = (ImageView) v;
            int border = -1;
            if (iv == areaUp)
                border = ADJUST_TOP;
            else if (iv == areaDown)
                border = ADJUST_BOTTOM;
            else if (iv == areaLeft)
                border = ADJUST_LEFT;
            else if (iv == areaRight)
                border = ADJUST_RIGHT;
            else
                return;
            adjustBoundLocked(border);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_displayarea);
        stateCount = 0;
        switcher = (TextView) findViewById(R.id.area_switch_on_click);
        switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchStateLocked();
            }
        });

        areaUp = (ImageView) findViewById(R.id.area_up);
        areaDown = (ImageView) findViewById(R.id.area_down);
        areaLeft = (ImageView) findViewById(R.id.area_left);
        areaRight = (ImageView) findViewById(R.id.area_right);
        areaUp.setOnClickListener(areaAdjustListener);
        areaDown.setOnClickListener(areaAdjustListener);
        areaLeft.setOnClickListener(areaAdjustListener);
        areaRight.setOnClickListener(areaAdjustListener);

        mDisplayd = new DisplayDAgent();
        try {
            curDisplayMode = mDisplayd.getMode();
        } catch (RemoteException e) {
            curDisplayMode = new DisplayModeInfo(0);
        }
        curDisplayMode.dump(TAG);
        limitX = curDisplayMode.Width / 2;
        limitY = curDisplayMode.Height / 2;
        if ("4k@60Hz".equals(curDisplayMode.signature))
            isLegalSetBound = false;
        if (!isLegalSetBound) {
            Toast toast = Toast.makeText(this, "4K@60Hz not allow to set",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
        try {
            curDisplayBound = mDisplayd.getBounds();
        } catch (RemoteException e) {
            curDisplayBound = new DisplayBoundInfo(0);
        }
        curDisplayBound.dump(TAG);
    }

    /* 
     * Activity.onKeyDown()
     * Called when a key was pressed down and not handled by any of the views
     * inside of the activity
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Log.d(TAG, "keycode:"+keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                adjustBoundLocked(ADJUST_TOP);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                adjustBoundLocked(ADJUST_BOTTOM);
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                adjustBoundLocked(ADJUST_LEFT);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                adjustBoundLocked(ADJUST_RIGHT);
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                switchStateLocked();
                break;
            default:
                return super.onKeyDown(keyCode, event);
        }
        return true;
    }
}

