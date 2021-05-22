package com.example.shopping.task;

import android.os.Bundle;
import android.util.Log;
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
        int t = event.getEventType();
        if ((t & listenType) > 0)
            post();
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


    //商品 立即预约 已预约  立即抢购
    //抢购 TEXT 京东 很遗憾
    //订单 填写订单 提交订单
    //收银台

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
            performSafe();
        } else if ("com.jd.lib.cashier.pay.view.CashierPayActivity".contentEquals(activity)) {
            performPay();
        } else {
            AccessibilityNodeInfo node = helper.findOneClickableNode("知道了");
            if (node != null)
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    private boolean clickedOrder = false;
    private boolean clickedBook = false;

    private void performSafe() {
        AccessibilityNodeInfo node = helper.findFocus();
        if (node == null)
            return;
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, pay);
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);

        node = helper.findOneClickableNode("确定");
        if (node == null)
            return;

        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        Log.i(TAG, "输入安全校验密码");
    }

    private void performProduct() {
        AccessibilityNodeInfo node = helper.findOneClickableNode("立即抢购");
        if (node != null) {
            if (node.isEnabled()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                clickedOrder = true;
                Log.i(TAG, "点击立即抢购");
            } else {
                if (clickedOrder) {
                    Log.i(TAG, "抢购失败");
                    notifyComplete(false);
                    isEnd = true;
                }
            }
            return;
        }

        node = helper.findOneClickableNode("立即预约");
        if (node != null) {
            if (node.isEnabled() && !clickedBook) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.i(TAG, "点击立即预约");
                clickedBook = true;
            }
        }
    }

    private void performOrder(boolean web) {
        AccessibilityNodeInfo node;
        node = helper.findOneClickableNodeInWeb("提交订单");
        if (node != null) {
            if (node.isEnabled()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.i(TAG, "点击提交订单");
            } else {
                helper.pressBack();
                Log.i(TAG, "无法提交订单，返回");
            }
            return;
        }
        if (!web)
            return;

        if (helper.findOneNodeInWeb("抢购失败") != null) {
            helper.pressBack();
            Log.i(TAG, "抢购失败");
        }

    }

    private void performPay() {
        notifyComplete(true);
        Log.i(TAG, "提交订单成功，请支付");
        isEnd = true;
    }

}
