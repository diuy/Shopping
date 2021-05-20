package com.example.shopping;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

public class TaskHelper {
    private final Context context;
    private volatile WeakReference<Activity> currentActivity = new WeakReference<>(null);

    public TaskHelper(Context context){
        this.context = context;
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



    @Nullable
    public Activity getCurrentActivity() {
        Log.d("App", "getCurrentActivity: " + currentActivity.get());
        return currentActivity.get();
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

    public void setCurrentActivity(Activity currentActivity) {
        this.currentActivity = new WeakReference<Activity>(currentActivity);
//        Log.d("App", "setCurrentActivity: " + currentActivity);
    }
}
