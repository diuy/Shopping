package com.example.shopping;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.example.shopping.task.JDTask;
import com.example.shopping.task.JDTestTask;
import com.example.shopping.task.ShoppingTask;
import com.example.shopping.task.TaskHelper;
import com.example.shopping.tools.HandlerSchedule;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShoppingService extends AccessibilityService {
    private static final String TAG = "ShoppingService";
    public static ShoppingService service;
    private ShoppingTask task;
    private NodeInfoWriter writer;
    private TaskHelper taskHelper;
    private HandlerSchedule schedule;

    public static ShoppingService getInstance() {
        return service;
    }

    public ShoppingService() {

    }

    private final Runnable onTime = () -> {
        if (task == null) {

            task = new JDTask(taskHelper);
            task.start();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        openFile();
    }

    private void onSchedule(String name) {
        if (task != null) {
            task.stop();
            task = null;
        }

        if ("jd".contentEquals(name)) {
            task = new JDTask(taskHelper);
            task.setSuccessListener(new Runnable() {
                @Override
                public void run() {
                    onSuccess();
                }
            });
            task.start();
        }
    }

    private void onSuccess() {
        if (task != null) {
            task.stop();
            task = null;
        }
    }

    private int dayTime(int h, int m, int s) {
        return (h * 3600 + m * 60 + s) * 1000;
    }

    private void createTestTask() {
        task = new JDTestTask(taskHelper);
        task.setSuccessListener(new Runnable() {
            @Override
            public void run() {
                onSuccess();
            }
        });
        task.start();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        taskHelper = new TaskHelper(this);
        schedule = new HandlerSchedule(new HandlerSchedule.Listener() {
            @Override
            public void onSchedule(String name) {
                ShoppingService.this.onSchedule(name);
            }
        });
        schedule.start();
        schedule.addSchedule("jd", dayTime(11, 0, 0));
        schedule.addSchedule("jd", dayTime(11, 55, 0));
        createTestTask();//TODO test
        service = this;
        openFile();
        toast("Shopping started!");
    }

    private void toast(String str) {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
    }


    //实现辅助功能
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (taskHelper != null)
                taskHelper.setCurrentActivity(event.getClassName().toString());
        }
        if (task != null)
            task.onEvent(event);
        if (writer != null) {
            //writer.writeEvent(event);
            //writer.writeRoot(getRootInActiveWindow());
        }

        //Log.d(TAG, "event:" + event.toString());
    }

    void gestureOnScreen(Path path, Long startTime, Long duration, AccessibilityService.GestureResultCallback callback, Handler handler) {
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, startTime, duration));
        GestureDescription gesture = builder.build();
        dispatchGesture(gesture, callback, handler);
    }

    void clickOnScreen(Float x, Float y, AccessibilityService.GestureResultCallback callback, Handler handler) {
        Path p = new Path();
        p.moveTo(x, y);
        gestureOnScreen(p, 0L, 0L, null, null);

    }

    @Override
    public void onInterrupt() {
        toast("助手中断");
        service = null;
    }

    private void closeFile() {
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }

    private void openFile() {
        if (writer != null)
            return;
        File root = getExternalFilesDir(null);
        File dataDir = new File(root, "shopping");
        if (!dataDir.exists() && !dataDir.mkdir()) {
            Log.e(TAG, "failed to create dir:" + dataDir.getPath());
            return;
        }
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault(Locale.Category.FORMAT));
        String str = dateFormat.format(date);
        File logFile = new File(dataDir, "record." + str + ".txt");
        Log.i(TAG, "record file:" + logFile.getAbsolutePath());
        //toast(logFile.getAbsolutePath());
        writer = new NodeInfoWriter(logFile);
        if (!writer.open()) {
            Log.e(TAG, "failed to open record file:" + logFile.getAbsolutePath());
        }

    }

    public void openRecord() {
        closeFile();
        openFile();
    }

    @Override
    public void onDestroy() {
        closeFile();
        toast("助手关闭");
        service = null;
        schedule.stop();
        super.onDestroy();
    }


    /**
     * 辅助功能是否启动
     */
    public static boolean isStart() {
        return service != null;
    }


}