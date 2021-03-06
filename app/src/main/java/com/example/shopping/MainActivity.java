package com.example.shopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
//import android.util.Log;
import com.example.shopping.tools.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //Manifest.permission.CAMERA

    public void saveConfig(String key, String value) {
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String readConfig(String key) {
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupPermission();
        setupServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EditText text = findViewById(R.id.textQQ);
        text.setText(readConfig("qq"));

        text = findViewById(R.id.textPay);
        text.setText(readConfig("pay"));
        Switch btn = findViewById(R.id.btnLog);

        if (readConfig("log").equals("true")) {
            btn.setChecked(true);
        } else {
            btn.setChecked(false);
        }
    }

    private boolean checkPermission() {
        for (String str : PERMISSIONS) {
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(str)) {
                return false;
            }
        }
        return true;
    }

    private void setupPermission() {
        List<String> list = new ArrayList<>();
        for (String str : PERMISSIONS) {
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(str)) {
                list.add(str);
            }
        }
        if (list.isEmpty())
            return;
        requestPermissions(list.toArray(new String[0]), 0);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("??????????????????");

        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                setupPermission();
            }
        });

        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (!checkPermission()) {
                showDialog();
            }
        }
    }

    public void setupServer() {
        if (!isAccessibilitySettingsOn(this, ShoppingService.class)) {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }
    }

    public static boolean isAccessibilitySettingsOn(Context mContext, Class<? extends AccessibilityService> clazz) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + clazz.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void toast(String str) {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
    }


    public void onLayout(View view) {
        toast("onLayout");
    }


    public void onSetQQ(View view) {
        EditText text = findViewById(R.id.textQQ);
        saveConfig("qq", text.getText().toString());

    }

    public void onSetPay(View view) {
        EditText text = findViewById(R.id.textPay);
        saveConfig("pay", text.getText().toString());
    }

    public void onLogSwitch(View view) {
        Switch btn = findViewById(R.id.btnLog);
        if (btn.isChecked()) {
            saveConfig("log", "true");
            if (ShoppingService.getInstance() != null) {
                ShoppingService.getInstance().openRecord();
            }
        } else {
            saveConfig("log", "false");
            if (ShoppingService.getInstance() != null) {
                ShoppingService.getInstance().closeFile();
            }
        }
    }

    private final Handler handler = new Handler();
    private boolean taskIsRun = false;

    public void onTask(View view) {
        if (taskIsRun) {
            toast("wait for other task");
            return;
        }
        Button button = (Button) view;
        if (button.getText() != null && button.getText().length() > 0) {
            String text = button.getText().toString();
            if (ShoppingService.getInstance() != null) {
                taskIsRun = true;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (ShoppingService.getInstance() != null) {
                            if (ShoppingService.getInstance().startTask(text)) {
                                toast(String.format("%s: started", text));
                            } else {
                                toast(String.format("%s: failed", text));
                            }
                        }
                        taskIsRun = false;
                    }
                }, 5000);
            } else {
                toast("Shopping server is not started!");
            }

        }
    }
}