package com.example.shopping.task;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.shopping.R;

import java.util.ArrayList;
import java.util.List;


public class NotifyTask extends Task {
    private static final String TAG = "NotifyTask";
    private Runnable runnable;

    public NotifyTask(TaskHelper helper) {
        super(helper);
    }

    @Override
    public String name() {
        return TAG;
    }

    @Override
    public void onEvent(AccessibilityEvent event) {

    }

    //com.tencent.mobileqq
    @Override
    public void start() {
        Log.i(TAG, "start");
        String qq = helper.readConfig("qq");
        Log.i(TAG, "qq:" + qq);
        if (!helper.startActivity("mqqwpa://im/chat?chat_type=wpa&uin=" + qq + "&version=1")) {
            Log.e(TAG, "startActivity failed");
            notifyComplete(false);
            return;
        }
        runnable = new StartRunner();
        handler.postDelayed(runnable, 4000);
    }

    private void findImageView(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> nodes) {
        if (node == null)
            return;
        if ("android.widget.AbsListView".contentEquals(node.getClassName()))
            return;

        if (node.getChildCount() > 0) {
            for (int i = 0; i < node.getChildCount(); i++) {
                findImageView(node.getChild(i), nodes);
            }
        } else {
            if (node.isClickable() && "android.widget.ImageView".contentEquals(node.getClassName())) {
                nodes.add(node);
            }
        }
    }

    private class StartRunner implements Runnable {
        private int time;

        @Override
        public void run() {

            boolean r = false;

            if (helper.checkActivity("com.tencent.mobileqq.activity.SplashActivity")) {
                List<AccessibilityNodeInfo> nodes = new ArrayList<>();
                findImageView(helper.getActiveNode(), nodes);
                if (nodes.size() <= 7) {
                    Log.e(TAG, "cannot find call image");
                    //notifyComplete(false);
                } else {
                    Log.i(TAG, "click call image");
                    nodes.get(7).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    runnable = new CallRunner();
                    handler.postDelayed(runnable, 2000);
                    r = true;
                }
            }
            if (!r) {
                time += 500;
                if (time < 10000) {
                    handler.postDelayed(runnable, 500);
                } else {
                    notifyComplete(false);
                }
            }
        }
    }

    private class CallRunner implements Runnable {

        @Override
        public void run() {
            boolean b = false;
            if (helper.checkActivity("android.app.Dialog")) {
                AccessibilityNodeInfo nodeInfo = helper.findOneClickableNode("语音通话");
                if (nodeInfo != null) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.i(TAG, "点击语音通话");
                    b = true;
                    notifyComplete(true);
                }
            }
            if (!b) {
                Log.e(TAG, "没有找到语音通话");
                notifyComplete(false);
            }
        }
    }


    @Override
    public void stop() {
        Log.i(TAG, "stop");

        if (runnable != null)
            handler.removeCallbacks(runnable);
    }
}
