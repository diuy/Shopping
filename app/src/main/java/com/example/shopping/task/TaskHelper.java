package com.example.shopping.task;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.Nullable;

import com.example.shopping.R;

import java.lang.ref.WeakReference;
import java.util.List;


public class TaskHelper {
    private final Context context;
    private final AccessibilityService accessibilityService;
    private String currentActivity;

    public TaskHelper(AccessibilityService accessibilityService) {
        this.context = accessibilityService;
        this.accessibilityService = accessibilityService;
    }

    public AccessibilityNodeInfo getActiveNode() {
        return accessibilityService.getRootInActiveWindow();
    }

    public void pressBack(){
        accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    public AccessibilityNodeInfo findOneNode(String text) {
        AccessibilityNodeInfo info = getActiveNode();
        if (info == null)
            return null;
        List<AccessibilityNodeInfo> list = info.findAccessibilityNodeInfosByText(text);
        if (list == null || list.isEmpty())
            return null;

        return list.get(0);
    }
    public AccessibilityNodeInfo findFocus() {
        AccessibilityNodeInfo info = getActiveNode();
        if (info == null)
            return null;
        return info.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
    }



    public AccessibilityNodeInfo findOneClickableNode(String text) {
        AccessibilityNodeInfo p = findOneNode(text);
        while (p != null) {
            if (p.isClickable())
                return p;
            p = p.getParent();
        }
        return null;
    }

    public AccessibilityNodeInfo findContainString(String text){
        AccessibilityNodeInfo info = getActiveNode();
        if (info == null)
            return null;
        return findContainString(info,text);
    }

    private static AccessibilityNodeInfo findContainString(AccessibilityNodeInfo info,String text){
        if(info==null)
            return null;
        CharSequence cs = info.getText();
        if(cs !=null && cs.length()>=text.length()){
            if( cs.toString().contains(text)){
                return info;
            }
        }
        for (int i = 0 ; i < info.getChildCount() ; i ++){
            AccessibilityNodeInfo n = findContainString(info.getChild(i),text);
            if(n!=null)
                return n;
        }

        return null;
    }

    public String getResourceString(int resId) {
        return context.getString(resId);
    }

    public void startActivity(String url) {
        Uri uri = Uri.parse(url);
        context.startActivity(new Intent("android.intent.action.VIEW", uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public boolean launchPackage(String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            context.startActivity(packageManager.getLaunchIntentForPackage(packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean checkPackageName(String name) {
        AccessibilityNodeInfo info = getActiveNode();
        if (info == null)
            return false;
        return name.contentEquals(info.getPackageName());
    }

    public boolean launchApp(String appName) {
        String pkg = getPackageName(appName);
        if (pkg == null)
            return false;
        return launchPackage(pkg);
    }

    public String getPackageName(String appName) {
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo applicationInfo : installedApplications) {
            if (packageManager.getApplicationLabel(applicationInfo).toString().equals(appName)) {
                return applicationInfo.packageName;
            }
        }
        return null;
    }

    public String getAppName(String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            CharSequence appName = packageManager.getApplicationLabel(applicationInfo);
            return appName == null ? null : appName.toString();
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }


    public void uninstall(String packageName) {
        context.startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }


    public void openUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        context.startActivity(new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }


    public String getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(String currentActivity) {
        this.currentActivity = currentActivity;
    }

    public boolean checkActivity(String activity) {
        if(currentActivity==null)
            return false;
        return activity.contentEquals(currentActivity);
    }
}
