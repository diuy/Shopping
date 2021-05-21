package com.example.shopping.task;

import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;

public abstract class Task {
    protected final TaskHelper helper;

    protected final Handler handler = new Handler(Looper.myLooper());

    private Listener listener;

    protected boolean isEnd;

    public Task(TaskHelper helper) {
        this.helper = helper;
    }

    public abstract void onEvent(AccessibilityEvent event);

    public abstract void start();

    public abstract void stop();

    public abstract String name();

    protected void notifyComplete(boolean success) {
        if (listener != null)
            listener.onComplete(success);
    }

    public interface Listener {
        void onComplete(boolean success);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
