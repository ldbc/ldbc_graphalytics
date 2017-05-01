/*
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package science.atlarge.graphalytics.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**

 */
public class TimeUtil {
    public static String getCurrentTimeString() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
    }

    public static long getEpochSecond() {
       return System.currentTimeMillis() / 1000L;
    }

    public static long getTimeElapsed(long startTime) {
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    public static String epoch2Date(long epoch) {
        return (new Date(epoch * 1000)).toString();
    }


    public static void waitFor(long seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Continues if current time is larger than start time + wait time, otherwise wait for an interval.
     * @param startTime The timestamp when the waiting starts (in Epoch)
     * @param waitTime The duration of the waiting (in seconds)
     * @param interval The duration of the interval (in seconds).
     * @return true if current time > start time + wait time, otherwise false.
     */
    public static boolean waitFor(long startTime, long waitTime, long interval) {
        if(System.currentTimeMillis() - startTime > waitTime * 1000) {
            return true;
        } else {
            TimeUtil.waitFor(interval);
            return false;
        }
    }

}
