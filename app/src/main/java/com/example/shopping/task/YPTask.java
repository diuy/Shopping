package com.example.shopping.task;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.shopping.R;

//com.xiaomi.youpin
public class YPTask extends Task {
    private static final String TAG = "YPTask";
    private static final String URL = "youpin://app.youpin.mi.com/app/shop/ugg/subscribeBuy.html?" +
            "actId=60a65349cff47e00014257b7&spmref=YouPin_A.share.share_pop_copy.4.66988930&share=1";
    private static final int listenType = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED |
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED;
    private Runnable runnable;

    public YPTask(TaskHelper helper) {
        super(helper);
    }

    @Override
    public String name() {
        return TAG;
    }

    public void onEvent(AccessibilityEvent event) {
        if (isEnd)
            return;
        if ("com.xiaomi.youpin".contentEquals(event.getPackageName())) {
            int t = event.getEventType();
            if ((t & listenType) > 0)
                post();
        }
    }


    private void post() {
        if (runnable == null) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    perform();
                }
            };
        } else {
            handler.removeCallbacks(runnable);
        }
        handler.post(runnable);
    }

    public void start() {
        Log.i(TAG, "start");
        if (!helper.startActivity(URL)) {
            Log.e(TAG, "startActivity failed");
            notifyComplete(false);
            return;
        }
        post();
    }

    public void stop() {
        Log.i(TAG, "stop");

        if (runnable != null)
            handler.removeCallbacks(runnable);
    }

    private String lastClick = "";

    private void perform() {
        AccessibilityNodeInfo node = helper.findOneClickableNodeInWeb("立即抢购");
        if (node != null) {
            if (node.isEnabled() && !compareString(lastClick, "立即抢购")) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                lastClick = "立即抢购";
                Log.i(TAG, "点击立即抢购");
            }
            return;
        }
        node = helper.findOneClickableNodeInWeb("立即预约");
        if (node != null) {
            if (node.isEnabled() && !compareString(lastClick, "立即预约")) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                lastClick = "立即预约";
                Log.i(TAG, "点击立即预约");
            }
            return;
        }
        node = helper.findOneNodeInWeb("恭喜您预约成功");
        if (node != null ) {
            Log.i(TAG, "恭喜您预约成功");
            return;
        }

        node = helper.findOneNodeInWeb("查看我的购买资格");
        if (node != null) {
            Log.i(TAG, "获得购买资格");
            notifyComplete(true);
        }

    }

}
