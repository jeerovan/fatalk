package com.kaarss.fatalk;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by jeevansingh on 01/01/18.
 */

public class TimeUtils {
    private static String TAG = TimeUtils.class.getSimpleName();

    /**
     * convert time in milliseconds into a display string of the form [h]h:mm
     * [am|pm] (traditional) or hh:mm (24 hour format) if using traditional
     * format, the leading 'h' & 'm' will be padded with a space to ensure
     * constant length if less than 10 24 hour format
     *
     * @param msecs a millisecond time
     * @return TimeString the formatted time string
     */
    public static String toAMPM(long msecs) {
        long time = new Date(msecs).getTime();
        SimpleDateFormat df = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
        return df.format(time);
    }

    public static String toDashAMPM(long msecs) {
        long time = new Date(msecs).getTime();
        SimpleDateFormat df = new SimpleDateFormat("h-mm-a", Locale.ENGLISH);
        return df.format(time);
    }

    public static String toReadable(long msecs) {
        Date today = new Date();
        String todayDate = getDateOnlyString(today.getTime());
        String dateDate = getDateOnlyString(msecs);
        String timeString = "";
        if (todayDate.equals(dateDate)) {
            timeString = toAMPM(msecs);
        } else {
            timeString = dateDate + " " + toAMPM(msecs);
        }
        return timeString;
    }

    public static String getDateOnlyString(long msecs) {
        Date date = new Date(msecs);
        long time = date.getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        return df.format(time);
    }

    public static long getMillisFromDate(String date) {
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        long millis = 0;
        try {
            Date dDate = df.parse(date);
            millis = dDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return millis;
    }
}
