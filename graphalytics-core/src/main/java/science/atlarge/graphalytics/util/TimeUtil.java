/*
 * Copyright 2015 - 2017 Atlarge Research Team,
 * operating at Technische Universiteit Delft
 * and Vrije Universiteit Amsterdam, the Netherlands.
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
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * @author Wing Lung Ngai
 */
public class TimeUtil {

    public static long getTimeElapsed(long startTime) {
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    public static String epoch2Date(long epoch) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MMMM/dd HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(epoch));
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
