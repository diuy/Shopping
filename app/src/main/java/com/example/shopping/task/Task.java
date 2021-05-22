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

    public static boolean compareString(CharSequence l, CharSequence r) {
        if (l == null)
            return r == null || r.length() == 0;
        return l.toString().contentEquals(r);
    }

    protected void notifyComplete(boolean success) {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null)
                        listener.onComplete(Task.this, success);
                }
            });
        }
    }

    public interface Listener {
        void onComplete(Task task, boolean success);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
