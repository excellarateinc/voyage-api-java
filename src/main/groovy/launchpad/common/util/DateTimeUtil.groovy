package launchpad.common.util

import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours

import java.sql.Timestamp
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class DateTimeUtil {
    public static final FMT_ISO_8601_DATE = "yyyy-MM-dd"
    public static final FMT_ISO_8601_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss"
    public static final FMT_ISO_8601_DATE_TIME_ZONE = "yyyy-MM-dd'T'HH:mm:ssZ"
    public static final FMT_EPIC_DATE_TIME = "M/d/yy h:mm a"
    public static final FMT_MONTH_DAY_YEAR = "MM/dd/yyyy"
    public static final DAY_MONTH_YEAR_TIME = "dd-MMM-yyyy hh:mm a"

    public static Calendar CALENDAR = Calendar.getInstance();

    def static supportedFormats = [
            "MM-dd-yyyy HH:mm:ss Z",        // 05-15-2015 18:45:33 +0000
            "MM-dd-yyyy hh:mm:ss a Z",      // 05-15-2015 6:45:33 PM +0000
            "MM-dd-yyyy hh:mm:ss a",        // 05-15-2015 6:45:33 PM
            "MM-dd-yyyy HH:mm:ss",          // 05-15-2015 18:45:33
            "MM-dd-yyyy",                   // 05-15-2015

            "MM/dd/yyyy HH:mm:ss Z",        // 05/15/2015 18:45:33 +0000
            "MM/dd/yyyy hh:mm:ss a Z",      // 05/15/2015 6:45:33 PM +0000
            "MM/dd/yyyy hh:mm:ss a",        // 05/15/2015 6:45:33 PM
            "MM/dd/yyyy HH:mm:ss",          // 05/15/2015 18:45:33
            "MM/dd/yyyy",                   // 05/15/2015

            "yyyy-MM-dd'T'HH:mm:ssZ",       // 2015-05-15T18:45:33+0000   ISO 8601
            "yyyy-MM-dd'T'HH:mm:ss",        // 2015-05-15T18:45:33
            "yyyy-MM-dd HH:mm:ss a",        // 2015-05-15 6:45:33 PM
            "yyyy-MM-dd HH:mm:ss",          // 2015-05-15 18:45:33
            "yyyy-MM-dd",                   // 2015-05-15

            "MMM dd yyyy  h:mma",           // Jan 10 2016  4:33PM
            "dd-MMM-yyyy"                   // 10-Jan-2016
    ]

    static Date parse(String dateString) {
        if (!dateString) return null

        for (format in supportedFormats) {
            try {
                def formatter = new SimpleDateFormat(format);
                formatter.setLenient(false)
                Date date = formatter.parse(dateString)
                return date
            } catch (ParseException ignored) { }
        }

        return null
    }

    public static String convertDateToString(Date date, String dateFormat) {
        SimpleDateFormat df = new SimpleDateFormat(dateFormat)
        String strDate = df.format(date)
        return strDate
    }

    public static Date convertStringToDate(String strDate, String dateFormat) {
        if (!strDate || !dateFormat) return null
        SimpleDateFormat sdfSource = new SimpleDateFormat(dateFormat)
        Date date = null
        try {
            date = sdfSource.parse(strDate)
        } catch (Exception ignored) { }
        return date
    }

    // this method converts the java.util.Date to Timestamp,
    // java.util.Date should be in the form of string like: "Fri Jun 11 15:10:11 UZT 2010"
    public static Timestamp convertISO8601DateStringToTimestamp(String strDate) {
        if (strDate == null || strDate.length() == 0) return null

        Date date = convertISO8601DateStringToDate(strDate)
        if (date == null) return null;

        long longDate = date.getTime()
        return (new Timestamp(longDate))
    }

    // this method converts the java.util.Date to Timestamp,
    // java.util.Date should be in the form of string like: "Fri Jun 11 15:10:11 UZT 2010"
    public static Date convertISO8601DateStringToDate(String strDate) {
        if (strDate == null) return null

        //DateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy")
        DateFormat format = new SimpleDateFormat(FMT_ISO_8601_DATE_TIME)
        return format.parse(strDate)
    }

    // converts the java.util.Date to Timestamp,
    // java.util.Date should be in the form of string like: "Fri Jun 11 15:10:11 UZT 2010"
    public static String convertDateToISO8601String(Date dateIn) {
        if (dateIn == null) return null

        DateFormat formatter = new SimpleDateFormat(FMT_ISO_8601_DATE_TIME)
        return formatter.format(dateIn)
    }

    public static int calcDaysDifference(Date beginDate, Date endDate) {
        if (!beginDate || !endDate) return 0
        Days d = Days.daysBetween(new DateTime(beginDate.time), new DateTime(endDate.time))
        return d.getDays()
    }

    public static float calcDaysDifferenceInFractions(Date beginDate, Date endDate) {
        if (!beginDate || !endDate) return 0
        Hours h = Hours.hoursBetween(new DateTime(beginDate.time), new DateTime(endDate.time))
        return h.hours/24
    }

    // Calculates the time difference between the current time and the given time(as ISO8601 Date)
    // and returns the difference hours
    public static int calculateTimeDifferenceInHours(Date date) {
        if (date == null) return 99999;
        long startTime = date.getTime()
        long currentTime = new Date().getTime()
        return getTimeDifference(currentTime, startTime, TimeUnit.HOURS)
    }

    // Calculates the time difference in the given unit
    public static int getTimeDifference(currentTime, startTime, unit) {
        if (unit == TimeUnit.MINUTES) {
            return TimeUnit.MILLISECONDS.toMinutes((currentTime - startTime))
        } else if (unit == TimeUnit.HOURS) {
            return TimeUnit.MILLISECONDS.toHours((currentTime - startTime))
        }
    }

    // Convert the java.sql.Timestamp to Month Day string like: "Jun21"
    public static String convertTimestampToMonthDay(timestamp) {
        if (timestamp == null) return null;
        SimpleDateFormat monthDayformatter = new SimpleDateFormat("MMMdd")
        return monthDayformatter.format((java.util.Date) timestamp)
    }

    public static boolean isValidFormat(String dateFormat, String dateString) {
        try {
            DateFormat formatter = new SimpleDateFormat(dateFormat)
            formatter.parse(dateString)
        } catch (ParseException e) {
            return false
        }
        return true
    }

    public static Timestamp endOfDaySql(Date date) {
        new Timestamp(endOfDay(date).time)
    }

    public static Date endOfDay(Date date) {
        Calendar calendar = CALENDAR;
        synchronized(calendar) {
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MILLISECOND, 999);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MINUTE, 59);
            return calendar.getTime();
        }
    }

    public static Timestamp startOfDaySql(Date date) {
        new Timestamp(startOfDay(date).time)
    }

    public static Date startOfDay(Date date) {
        Calendar calendar = CALENDAR;
        synchronized(calendar) {
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            return calendar.getTime();
        }
    }
    static format(Date date, String format) {
        return new SimpleDateFormat(format).format(date)
    }

    public static String getExecutedTime(start) {
        def now = System.currentTimeMillis()
        def time = now - start
        String executedTime = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(time), TimeUnit.MILLISECONDS.toSeconds(time) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
        )
        return executedTime
    }

    public static Date now() {
        new Date()
    }
}
