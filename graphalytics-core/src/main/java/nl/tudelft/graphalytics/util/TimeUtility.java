package nl.tudelft.graphalytics.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**

 */
public class TimeUtility {
    public static String getCurrentTimeString() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
    }

    public static long getEpochSecond() {
       return System.currentTimeMillis() / 1000L;
    }

    public static long getTimeElapsed(long startTime) {
        return getEpochSecond() - startTime;
    }

    public static String epoch2Date(long epoch) {
        return (new Date(epoch * 1000)).toString();
    }

    public static void waitFor(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
