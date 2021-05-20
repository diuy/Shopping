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
    private FileWriter fileWriter;


    public ShoppingService() {

    }


    private final Runnable onTime = () -> {
        if (task == null) {
            Uri uri = Uri.parse(getString(R.string.jd_mt_url));
            startActivity(new Intent("android.intent.action.VIEW", uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            task = new JDTask();
            task.start();
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        timer = new HandlerTimer(1000, onTime);
        timer.start();

        toast("助手锁定中");
//        AccessibilityServiceInfo info = getServiceInfo();
//        info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;
//        setServiceInfo(info);
        service = this;
    }

    private void toast(String str) {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
    }


    //实现辅助功能
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        Log.d(TAG, "onAccessibilityEvent:event:" + event.toString());
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (task != null)
                task.setCurrentActivity(event.getClassName().toString());
        }
        if (task != null)
            task.setRootInfo(getRootInActiveWindow());
        printEvent(event);
        printRoot();
//        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
//            if (event.getSource() != null) {
//                //  Log.d(TAG, "onAccessibilityEvent:source:" + event.getSource().getChildCount());
//                print(event.getSource());
//
//
//                List<AccessibilityNodeInfo> nodeInfos = event.getSource().findAccessibilityNodeInfosByText("口碑好货");
//                if (nodeInfos != null && nodeInfos.size() > 0) {
//                    Log.i(TAG, "****" + nodeInfos.toString());
//                    nodeInfos.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                }
//            }
//        }
    }

    private final StringBuilder builder = new StringBuilder(10 * 1024);

    private void printEvent(AccessibilityEvent event) {
        builder.setLength(0);
        builder.append("event->\n");
        builder.append(event.toString()).append("\n");
        AccessibilityNodeInfo info = event.getSource();
        if (info != null) {
            builder.append("source->\n");
            builder.append("window->\n");
            if (info.getWindow() != null)
                builder.append(info.getWindow()).append("\n");
            printNodeInfo(info, 0);
        }
        Log.e(TAG, builder.toString());
    }

    private void printRoot(){
        builder.setLength(0);
        builder.append("root->\n");
        printNodeInfo(getRootInActiveWindow(), 0);
        Log.e(TAG, builder.toString());
    }

    private void printNodeInfo(AccessibilityNodeInfo info, int depth) {
        if (info == null)
            return;
        for (int i = 0; i < depth; i++) {
            builder.append(' ');
        }
        printNodeInfo(info);
        for (int i = 0; i < info.getChildCount(); i++) {
            printNodeInfo(info.getChild(i), depth + 1);
        }
    }

    private void printNodeInfo(AccessibilityNodeInfo info) {
        builder.append(info.getClassName()).append(":").append(info.getText()).append(' ');

        if (info.isAccessibilityFocused()) {
            builder.append("AccessibilityFocused").append(',');
        }
        if (info.isCheckable()) {
            builder.append("Checkable").append(',');
        }
        if (info.isChecked()) {
            builder.append("Checked").append(',');
        }
        if (info.isClickable()) {
            builder.append("Clickable").append(',');
        }
        if (info.isContentInvalid()) {
            builder.append("ContentInvalid").append(',');
        }
        if (info.isContextClickable()) {
            builder.append("ContextClickable").append(',');
        }
        if (info.isDismissable()) {
            builder.append("Dismissable").append(',');
        }
        if (info.isEditable()) {
            builder.append("Editable").append(',');
        }
        if (info.isEnabled()) {
            builder.append("Enabled").append(',');
        }
        if (info.isFocusable()) {
            builder.append("Focusable").append(',');
        }
        if (info.isFocused()) {
            builder.append("Focused").append(',');
        }
        if (info.isImportantForAccessibility()) {
            builder.append("ImportantForAccessibility").append(',');
        }
        if (info.isLongClickable()) {
            builder.append("LongClickable").append(',');
        }
        if (info.isMultiLine()) {
            builder.append("MultiLine").append(',');
        }
        if (info.isPassword()) {
            builder.append("Password").append(',');
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (info.isScreenReaderFocusable()) {
                builder.append("ScreenReaderFocusable").append(',');
            }
        }
        if (info.isScrollable()) {
            builder.append("Scrollable").append(',');
        }
        if (info.isSelected()) {
            builder.append("Selected").append(',');
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (info.isShowingHintText()) {
                builder.append("ShowingHintText").append(',');
            }
        }
//        if (info.isTextEntryKey()) {
//            builder.append("TextEntryKey").append(',');
//        }
        if (info.isVisibleToUser()) {
            builder.append("VisibleToUser").append(',');
        }
        builder.append('\n');
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
        toast("onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onInterrupt() {

        toast("助手中断");
        service = null;
    }

    private void closeFile(){
        if(fileWriter!=null) {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileWriter = null;
        }
    }

    private void openFile() {
        if(fileWriter!=null)
            return;
        File root = getExternalFilesDir(null);
        File dataDir = new File(root,"shopping");
        if(!dataDir.exists() && !dataDir.mkdir()){
            return;
        }

        File logFile = new File(dataDir,"node-info.txt");
        try {
            fileWriter = new FileWriter(logFile,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
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