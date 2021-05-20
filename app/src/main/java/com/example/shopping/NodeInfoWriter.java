package com.example.shopping;

import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NodeInfoWriter {
    private final File file;
    private FileWriter fileWriter;

    public NodeInfoWriter(File file) {
        this.file = file;
    }

    public void writeEvent(AccessibilityEvent event) {
        if(!openFile())
            return;
        write(currentTime(), ":event->\n");
        write(event.toString(), "\n");

        AccessibilityNodeInfo info = event.getSource();
        if (info != null) {
            if (info.getWindow() != null)
                write("window->\n", info.getWindow().toString(), "\n");
            write("source->\n");
            writeNodeInfo(info, 0);
        }
        try {
            fileWriter.flush();
        } catch (IOException e) {
        }
    }


    public void writeRoot(AccessibilityNodeInfo info) {
        if(!openFile())
            return;
        write(currentTime(), ":root->\n");
        writeNodeInfo(info, 0);

        try {
            fileWriter.flush();
        } catch (IOException e) {
        }
    }

    private void writeNodeInfo(AccessibilityNodeInfo info, int depth) {
        if (info == null)
            return;
        for (int i = 0; i < depth; i++) {
            write(" ");
        }
        writeNodeInfo(info);
        for (int i = 0; i < info.getChildCount(); i++) {
            writeNodeInfo(info.getChild(i), depth + 1);
        }
    }

    public void close() {
        if (fileWriter != null) {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileWriter = null;
        }
    }

    private boolean openFile() {
        if (fileWriter != null)
            return true;
        try {
            fileWriter = new FileWriter(file, true);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String currentTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss S", Locale.getDefault(Locale.Category.FORMAT));
        return dateFormat.format(date);
    }

    private void write(Object... objects) {
        try {
            for (Object o : objects) {
                if (o != null)
                    fileWriter.write(o.toString());
                else
                    fileWriter.write("null");
            }
        } catch (IOException e) {
        }
    }

    private void writeNodeInfo(AccessibilityNodeInfo info) {
        write(info.getClassName(), ":", info.getText(), " ");

        if (info.isAccessibilityFocused()) {
            write("AccessibilityFocused", ",");
        }
        if (info.isCheckable()) {
            write("Checkable", ",");
        }
        if (info.isChecked()) {
            write("Checked", ",");
        }
        if (info.isClickable()) {
            write("Clickable", ",");
        }
        if (info.isContentInvalid()) {
            write("ContentInvalid", ",");
        }
        if (info.isContextClickable()) {
            write("ContextClickable", ",");
        }
        if (info.isDismissable()) {
            write("Dismissable", ",");
        }
        if (info.isEditable()) {
            write("Editable", ",");
        }
        if (info.isEnabled()) {
            write("Enabled", ",");
        }
        if (info.isFocusable()) {
            write("Focusable", ",");
        }
        if (info.isFocused()) {
            write("Focused", ",");
        }
        if (info.isImportantForAccessibility()) {
            write("ImportantForAccessibility", ",");
        }
        if (info.isLongClickable()) {
            write("LongClickable", ",");
        }
        if (info.isMultiLine()) {
            write("MultiLine", ",");
        }
        if (info.isPassword()) {
            write("Password", ",");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (info.isScreenReaderFocusable()) {
                write("ScreenReaderFocusable", ",");
            }
        }
        if (info.isScrollable()) {
            write("Scrollable", ",");
        }
        if (info.isSelected()) {
            write("Selected", ",");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (info.isShowingHintText()) {
                write("ShowingHintText", ",");
            }
        }
        if (info.isVisibleToUser()) {
            write("VisibleToUser", ",");
        }
        write("\n");
    }

}
