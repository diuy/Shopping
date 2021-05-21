package com.example.shopping.tools;

import android.os.Handler;
import android.os.Looper;

public class HandlerTimer {
    private final long period;
    private final Runnable runnable;
    private Handler handler;

    public HandlerTimer(long period, Runnable runnable) {
        this.period = period;
        this.runnable = runnable;
    }

    private final Runnable innerRunnable = new Runnable() {
        @Override
        public void run() {
            runnable.run();
            handler.postDelayed(innerRunnable, period);
        }
    };

    public void start() {
        if (handler != null)
            return;
        handler = new Handler(Looper.myLooper());
        handler.postDelayed(innerRunnable, period);
    }

    public void stop() {
        handler.removeCallbacks(innerRunnable);
        handler = null;
    }
}
