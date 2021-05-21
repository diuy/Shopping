package com.example.shopping.tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HandlerSchedule {
    private HandlerTimer timer;
    private Listener listener;
    private List<Item> items = new ArrayList<>();
    private long lastTime;

    public HandlerSchedule(Listener listener) {
        this.listener = listener;
        timer = new HandlerTimer(1000, new Runnable() {
            @Override
            public void run() {
                onTimer();
            }
        });
    }

    public void start() {
        lastTime = System.currentTimeMillis();
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    private static final long HOUR_MILLISECOND = 3600000;
    private static final long MINUTE_MILLISECOND = 60000;
    private static final long SECOND_MILLISECOND = 1000;

    private long getDateMillis(long t) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(t);
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        int s = calendar.get(Calendar.SECOND);
        int mm = calendar.get(Calendar.MILLISECOND);
        return t - h * HOUR_MILLISECOND - m * MINUTE_MILLISECOND - s * SECOND_MILLISECOND - mm;
    }

    private boolean testInTime(long start, long end, int t) {
        long st = getDateMillis(start);
        long et = getDateMillis(end);
        long nt = t + st;
        if (nt >= start && nt < end) {
            return true;
        }
        nt = t + et;
        if (nt >= start && nt < end) {
            return true;
        }
        return false;
    }

    private void onTimer() {
        long nowTime = System.currentTimeMillis();
        if (nowTime < lastTime) {
            lastTime = nowTime;
            return;
        }

        for (Item item : items) {
            if (testInTime(lastTime, nowTime, item.getTime())) {
                if (listener != null) {
                    listener.onSchedule(item.getName());
                }
            }
        }
        lastTime = nowTime;
    }

    public interface Listener {
        public void onSchedule(String name);
    }

    public void addSchedule(String name, int time) {
        items.add(new Item(name, time));
    }

    public void clearSchedule() {
        items.clear();
    }

    private static class Item {
        private final String name;
        private final int time;

        public Item(String name, int time) {

            this.name = name;
            this.time = time;
        }

        public String getName() {
            return name;
        }

        public int getTime() {
            return time;
        }
    }
}
