package com.example.shopping;

import android.app.usage.UsageEvents;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityNodeInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class JDTask {

    private EventBus bus = new EventBus();
    private AccessibilityNodeInfo rootInfo;
    private String currentActivity;
    private StartState state;

    private final Handler handler = new Handler(Looper.myLooper());

    public JDTask() {
        bus.register(this);
    }

    public void setRootInfo(AccessibilityNodeInfo rootInfo) {
        this.rootInfo = rootInfo;
    }

    public void setCurrentActivity(String currentActivity) {
        this.currentActivity = currentActivity;
    }

    public void start() {
        state = new StartState();
        state.perform();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onTime(UsageEvents.Event event) {

    }

    public boolean checkPackageName() {
        if (rootInfo == null)
            return false;
        if (rootInfo.getPackageName() == null)
            return false;
        return "com.jingdong.app.mall".contentEquals(rootInfo.getPackageName());
    }

    public boolean checkActivity(String name) {
        return name.contentEquals(currentActivity);
    }


    private class StartState implements TaskState {

        @Override
        public void perform() {


        }
    }


}
