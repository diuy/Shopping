package com.example.shopping;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.shopping.task.Task;
import com.example.shopping.task.TaskFactory;
import com.example.shopping.task.TaskHelper;
import com.example.shopping.tools.HandlerSchedule;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
//        toast("onCreate");
        // 获取服务通知
        Notification notification = createForegroundNotification();
        //将服务置于启动状态 ,NOTIFICATION_ID指的是创建的通知的ID
        startForeground(1, notification);
    }

    private void onSchedule(String name) {
        startTask(name);
    }

    public boolean startTask(String name) {
        if (task != null) {
            task.stop();
            task = null;
        }
        taskHelper.wakeUpAndUnlock();

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
            return true;
        }
        return false;
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
        schedule.addSchedule(TaskFactory.NAME_YPTask, dayTime(9, 57, 0));
//        schedule.addSchedule(TaskFactory.NAME_YPTask, getTodayTimeMillisecond()+30*1000);

        schedule.start();
        //        startTask(TaskFactory.NAME_NotifyTask); //TODO test

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

    private static final int HOUR_MILLISECOND = 3600000;
    private static final int MINUTE_MILLISECOND = 60000;
    private static final int SECOND_MILLISECOND = 1000;

    private int getTodayTimeMillisecond() {
        Calendar calendar = Calendar.getInstance();
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        int s = calendar.get(Calendar.SECOND);
        return h * HOUR_MILLISECOND + m * MINUTE_MILLISECOND + s * SECOND_MILLISECOND;
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

        if (!"true".equals(taskHelper.readConfig("log"))) {
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
        stopForeground(true);
        super.onDestroy();
    }


    /**
     * 辅助功能是否启动
     */
    public static boolean isStart() {
        return service != null;
    }

    private Notification createForegroundNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 唯一的通知通道的id.
        String notificationChannelId = "notification_channel_id_01";

        // Android8.0以上的系统，新建消息通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //用户可见的通道名称
            String channelName = "Shopping Notification";
            //通道的重要程度
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, channelName, importance);
            notificationChannel.setDescription("Shopping Channel");
            //LED灯
//            notificationChannel.enableLights(true);
//            notificationChannel.setLightColor(Color.RED);
            //震动
//            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
//            notificationChannel.enableVibration(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId);
        //通知小图标
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        //通知标题
        builder.setContentTitle("Shopping");
        //通知内容
        builder.setContentText("运行中");
        //设定通知显示的时间
        builder.setWhen(System.currentTimeMillis());
        //设定启动的内容
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        //创建通知并返回
        return builder.build();
    }
}