package org.test.giraph.utils;

/*
    Creates Job name based on dataset and String, both passed.
 */
public class JobName {
    public static String createJobName(String job, String data) {
        String result = new String(job);

        int last = data.lastIndexOf("/");
        result += "_"+data.substring(++last);

        return result;
    }
}
