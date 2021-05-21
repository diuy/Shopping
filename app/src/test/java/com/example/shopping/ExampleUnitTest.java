package com.example.shopping;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    private static final long HOUR_MILLISECOND = 3600000;
    private static final long MINUTE_MILLISECOND = 60000;
    private static final long SECOND_MILLISECOND = 1000;

    private long getDateMillis(long t) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(t);
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        int s = calendar.get(Calendar.SECOND);
        int mm = calendar.get(Calendar.MILLISECOND);
        return t - h * HOUR_MILLISECOND - m * MINUTE_MILLISECOND - s * SECOND_MILLISECOND - mm;
    }

    private boolean testInTime(long start,long end,int t){
        long st = getDateMillis(start);
        long et = getDateMillis(end);
        long nt = t+st;
        if(nt>=start&&nt<end){
            return true;
        }
        nt=t+et;
        if(nt>=start&&nt<end){
            return true;
        }
        return false;
    }

    private long parse(String str) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(str);
        return date.getTime();
    }

    @Test
    public void mytest() throws ParseException {
        long t1= parse("2020-10-11 23:59:00");
        long t2= parse("2020-10-12 00:00:59");
        boolean b1 = testInTime(t1,t2,24*3600*1000);
        System.out.printf(""+b1);

        boolean b2 = testInTime(t1,t2,24*3600*1000-10*1000);
        System.out.printf(""+b2);

        boolean b3 = testInTime(t1,t2,24*3600*1000+10*1000);
        System.out.printf(""+b3);

        boolean b4 = testInTime(t1,t2,23*3600*1000);
        System.out.printf(""+b4);

        boolean b5 = testInTime(t1,t2,60*1000);
        System.out.printf(""+b5);

        boolean b6 = testInTime(t1,t2,0*1000);
        System.out.printf(""+b6);
    }
}