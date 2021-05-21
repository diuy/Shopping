package com.example.shopping.task;

import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.shopping.R;


public class JDTestTask extends ShoppingTask {
    private static final String TAG = "JDTestTask";

    private static final int listenType = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED |
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED;


    public JDTestTask(TaskHelper helper) {
        super(helper);
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

    public void start() {
        Log.i(TAG, "start");
        helper.startActivity(helper.getResourceString(R.string.jd_test_url));
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
        }
    }

    private void performSafe() {
        AccessibilityNodeInfo node = helper.findFocus();
        if(node==null)
            return;
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "zf31415926");
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,arguments);

        node = helper.findOneClickableNode("确定");
        if(node==null)
            return;

        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        Log.i(TAG, "输入安全校验密码");
    }

    private void performProduct() {
        AccessibilityNodeInfo node = helper.findOneClickableNode("立即购买");
        if (node != null) {
            if (node.isEnabled()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.i(TAG, "点击立即购买");
            }
        }
    }

    private void performOrder(boolean web) {
        AccessibilityNodeInfo node;
        node = helper.findOneClickableNode("提交订单");
        if (node != null) {
            if (node.isEnabled()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.i(TAG, "点击提交订单");
            } else {
                helper.pressBack();
                Log.i(TAG, "无法提交订单，返回");
            }
        }

    }

    private void performPay() {
        notifySuccess();
        Log.i(TAG, "提交订单成功，请支付");
        isEnd = true;
    }

}
