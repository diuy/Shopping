package com.example.shopping.task;

import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;

public abstract class ShoppingTask {
    protected final TaskHelper helper;

    protected final Handler handler = new Handler(Looper.myLooper());

    private Runnable successListener;

    protected boolean isEnd;

    public ShoppingTask(TaskHelper helper) {
        this.helper = helper;
    }

    public abstract void onEvent(AccessibilityEvent event);

    public abstract void start();

    public abstract void stop();

    protected void notifySuccess() {
        if (successListener != null)
            successListener.run();
    }

    public void setSuccessListener(Runnable successListener) {
        this.successListener = successListener;
    }
}
