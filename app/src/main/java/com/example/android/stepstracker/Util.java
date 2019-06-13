package com.example.android.stepstracker;

import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public  abstract class Util {

    /**
     * @return milliseconds since 1.1.1970 for today 0:00:00 local timezone
     */
    public static int getDate(int delta) {


        Calendar cal  = Calendar.getInstance();

        cal.add(Calendar.DATE, delta);

        SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd");
        String result = s.format(new Date(cal.getTimeInMillis()));
        return Integer.parseInt(result);
    }


}

