package com.example.shopping;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

public class ShoppingService extends AccessibilityService {
    private final String TAG = getClass().getName();
    private static ShoppingService service;
    private JDTask task;
    private HandlerTimer timer;
    private NodeInfoWriter writer;


    public ShoppingService() {

    }


    private final Runnable onTime = () -> {
        if (task == null) {
            Uri uri = Uri.parse(getString(R.string.jd_mt_url));
            //startActivity(new Intent("android.intent.action.VIEW", uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            task = new JDTask();
            task.start();
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        timer = new HandlerTimer(1000, onTime);
        timer.start();

        toast("Shopping started!");
        service = this;
    }

    private void toast(String str) {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
    }


    //实现辅助功能
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (task != null)
                task.setCurrentActivity(event.getClassName().toString());
        }
        if (task != null)
            task.setRootInfo(getRootInActiveWindow());
        if(writer!=null){
            writer.writeEvent(event);
            writer.writeRoot(getRootInActiveWindow());
        }
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        openFile();
        return super.onStartCommand(intent, flags, startId);
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
            Log.e(TAG,"failed to create dir:"+dataDir.getPath());
            return;
        }

        File logFile = new File(dataDir, "node-info.txt");
        Log.e(TAG,"data file:"+logFile.getAbsolutePath());

        writer = new NodeInfoWriter(logFile);
    }

    @Override
    public void onDestroy() {
        closeFile();
        super.onDestroy();
        toast("助手关闭");
        service = null;
        timer.stop();

    }


    /**
     * 辅助功能是否启动
     */
    public static boolean isStart() {
        return service != null;
    }


}