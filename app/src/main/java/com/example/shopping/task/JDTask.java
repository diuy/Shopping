package com.example.shopping.task;

import android.os.Bundle;
//import android.util.Log;
import com.example.shopping.tools.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.shopping.R;


public class JDTask extends Task {
    private static final String TAG = "JDTask";

    private static final int listenType = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED |
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED;


    public JDTask(TaskHelper helper) {
        super(helper);
    }

    @Override
    public String name() {
        return TAG;
    }

    public void onEvent(AccessibilityEvent event) {
        if (isEnd)
            return;
        if ("com.jingdong.app.mall".contentEquals(event.getPackageName())) {
            int t = event.getEventType();
            if ((t & listenType) > 0)
                post();
        }
    }

    private Runnable runnable;

    private void post(long time) {
        if (runnable == null) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    perform();
                }
            };
        }
        handler.postDelayed(runnable, time);
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

    private String pay;

    public void start() {
        Log.i(TAG, "start");
        if (!helper.startActivity(helper.getResourceString(R.string.jd_mt_url))) {
            Log.e(TAG, "startActivity failed");
            notifyComplete(false);
            return;
        }
        pay = helper.readConfig("pay");
        post();
    }

    public void stop() {
        Log.i(TAG, "stop");

        if (runnable != null)
            handler.removeCallbacks(runnable);
    }


    //?????? ???????????? ?????????  ????????????
    //?????? TEXT ?????? ?????????
    //?????? ???????????? ????????????
    //?????????

    private void perform() {
        String activity = helper.getCurrentActivity();
        if (activity == null)
            return;

        if ("com.jd.lib.productdetail.ProductDetailActivity".contentEquals(activity)) {
            performProduct();
        } else if ("com.jingdong.app.mall.WebActivity".contentEquals(activity)) {
            performOrder(true);
        } else if ("com.jd.lib.settlement.fillorder.activity.NewFillOrderActivity".contentEquals(activity)) {
            performOrder(false);
        } else if ("com.jingdong.common.ui.JDDialog".contentEquals(activity)) {
            performDialog();
        } else if ("com.jd.lib.cashier.pay.view.CashierPayActivity".contentEquals(activity)) {
            performPay();
        }
    }

    private boolean clickedOrder = false;
    private boolean clickedBook = false;

    private void performDialog() {

        AccessibilityNodeInfo node;
        node = helper.findOneClickableNode("??????");
        if (node != null) {
            AccessibilityNodeInfo n = helper.findFocus();
            if (n != null) {
                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, pay);
                n.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                Log.i(TAG, "????????????????????????");
            }
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            Log.i(TAG, "????????????");
            return;
        }

        node = helper.findOneClickableNode("?????????");
        if (node != null) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            Log.i(TAG, "???????????????");
        }
    }

    private void performProduct() {
        AccessibilityNodeInfo node = helper.findOneClickableNode("????????????");
        if (node != null) {
            if (node.isEnabled()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                clickedOrder = true;
                Log.i(TAG, "??????????????????");
            } else {
                if (clickedOrder) {
                    Log.i(TAG, "????????????");
                    notifyComplete(false);
                    isEnd = true;
                }
            }
            return;
        }

        node = helper.findOneClickableNode("????????????");
        if (node != null) {
            if (node.isEnabled() && !clickedBook) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.i(TAG, "??????????????????");
                clickedBook = true;
            }
        }
    }

    private void performOrder(boolean web) {
        AccessibilityNodeInfo node;
        node = helper.findOneClickableNodeInWeb("????????????");
        if (node != null) {
            if (node.isEnabled()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.i(TAG, "??????????????????");
            } else {
                helper.pressBack();
                Log.i(TAG, "???????????????????????????");
            }
            return;
        }
        if (!web)
            return;

        if (helper.findOneNodeInWeb("????????????") != null) {
            helper.pressBack();
            Log.i(TAG, "????????????");
        }

    }

    private void performPay() {
        notifyComplete(true);
        Log.i(TAG, "??????????????????????????????");
        isEnd = true;
    }

}
