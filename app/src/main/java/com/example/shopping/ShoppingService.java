package com.example.shopping;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.example.shopping.task.Task;
import com.example.shopping.task.TaskFactory;
import com.example.shopping.task.TaskHelper;
import com.example.shopping.tools.HandlerSchedule;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShoppingService extends AccessibilityService {
    private static final String TAG = "ShoppingService";
    public static ShoppingService service;
    private Task task;
    private NodeInfoWriter writer;
    private TaskHelper taskHelper;
    private HandlerSchedule schedule;

    public static ShoppingService getInstance() {
        return service;
    }

    public ShoppingService() {

    }


    @Override
    public void onCreate() {
        super.onCreate();
        toast("onCreate");

    }

    private void onSchedule(String name) {
        if (task != null) {
            task.stop();
            task = null;
        }
        taskHelper.wakeUpAndUnlock();
        startTask(name);
    }

    private void startTask(String name) {
        task = TaskFactory.createTask(name, taskHelper);

        if (task != null) {
            task.setListener(new Task.Listener() {
                @Override
                public void onComplete(Task t, boolean success) {
                    Log.i(TAG, "Task completed:" + t.name() + "->" + success);
                    if (t == task) {
                        task = null;
                        if (success)
                            startNotifyTask();
                    }
                }
            });
            task.start();
            Log.i(TAG, "Task started:" + task.name());
        }
    }

    private void startNotifyTask() {
        task = TaskFactory.createNotifyTask(taskHelper);
        task.setListener(new Task.Listener() {
            @Override
            public void onComplete(Task t, boolean success) {
                Log.i(TAG, "Task completed:" + t.name() + "->" + success);
                if (t == task)
                    task = null;
            }
        });
        task.start();
        Log.i(TAG, "Task started:" + task.name());
    }

    private int dayTime(int h, int m, int s) {
        return (h * 3600 + m * 60 + s) * 1000;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        taskHelper = new TaskHelper(this);
        openFile();

        schedule = new HandlerSchedule(new HandlerSchedule.Listener() {
            @Override
            public void onSchedule(String name) {
                ShoppingService.this.onSchedule(name);
            }
        });
        schedule.addSchedule(TaskFactory.NAME_JDTask, dayTime(11, 54, 0));
        schedule.addSchedule(TaskFactory.NAME_YPTask, dayTime(9, 0, 0));
        schedule.addSchedule(TaskFactory.NAME_YPTask, dayTime(9, 56, 0));

        schedule.start();
//         startTask(TaskFactory.NAME_NotifyTask); //TODO test

//        startTask(TaskFactory.NAME_JDTestTask); //TODO test
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                taskHelper.wakeUpAndUnlock();
//                startTask("JDTestTask");//TODO test
//
//            }
//        }, 5 * 1000);

        service = this;
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
            writer.writeEvent(event);
            //writer.writeRoot(getRootInActiveWindow());
             Log.d(TAG, "event:" + event.toString());
        }
        // Log.d(TAG, "event:" + event.toString());
    }

    public void writeRoot() {
        writer.writeRoot(getRootInActiveWindow());
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
        toast("Shopping is interrupted");
        service = null;
    }

    public void closeFile() {
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }

    public void openFile() {
        if (writer != null)
            return;

        if(!"true".equals(taskHelper.readConfig("log"))){
            return;
        }

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
        toast("Shopping closed");
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