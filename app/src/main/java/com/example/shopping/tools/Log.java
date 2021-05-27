package com.example.shopping.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Log {

    private static FileWriter fileWriter;


    public static boolean open(File file) {
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

    public static void close() {
        if (fileWriter != null) {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileWriter = null;
        }
    }

    private static String currentTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss S", Locale.getDefault(Locale.Category.FORMAT));
        return dateFormat.format(date);
    }

    private static String nullString(String str) {
        return str == null ? "null" : str;
    }

    private static void write(String level, String tag, String msg) {
        if (fileWriter == null)
            return;

        try {
            fileWriter.write("**");
            fileWriter.write(currentTime());
            fileWriter.write(' ');
            fileWriter.write(level);
            fileWriter.write(' ');

            fileWriter.write(nullString(tag));
            fileWriter.write(':');
            fileWriter.write(' ');
            fileWriter.write(nullString(msg));
            fileWriter.write('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int v(String tag, String msg) {
        write("VERBOSE", tag, msg);
        return android.util.Log.v(tag, msg);
    }

    public static int d(String tag, String msg) {
        write("DEBUG", tag, msg);
        return android.util.Log.d(tag, msg);
    }

    public static int i(String tag, String msg) {
        write("INFO", tag, msg);
        return android.util.Log.i(tag, msg);
    }

    public static int w(String tag, String msg) {
        write("WARN", tag, msg);
        return android.util.Log.w(tag, msg);
    }

    public static int e(String tag, String msg) {
        write("ERROR", tag, msg);
        return android.util.Log.e(tag, msg);
    }

}
