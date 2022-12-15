/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Date object is used to represent time(1970,1,1), which need to consider DST
 * as web.xml sets DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE E.g.
 * planned service's time is stored in winter time always in database but
 * scheduled service's time is stored in winter/summer time according to the
 * date in database. In any case, UI shows the same time in winter and summer;
 *
 * @author Jia Li
 */
public class TimeHelper {

    public static Integer getDayFor(boolean isUtc, Date date, Integer seconds, String zone) {
        if (seconds != null) {
            if (isUtc) {
                //return (seconds + getOffset(date, zone) / 1000) / 3600 / 24;
                return (seconds + TimeZone.getTimeZone(zone).getRawOffset() / 1000) / 3600 / 24;
            } else {
                return seconds / 3600 / 24;
            }
        } else {
            return 0;
        }
    }

    /**
     *
     * @param time
     * @param zone
     * @return the offset in milliseconds, DST is considered. For example, the
     * utc offset during DST is typically obtained by adding one hour to
     * standard time.
     */
    private static int getOffset(Date time, String zone) {
        int offset = 0;

        if (time != null) {
            offset = TimeZone.getTimeZone(zone).getOffset(time.getTime());
        }

        return offset;
    }

//    private static int getDSTSavings(Date date, String zone) {
//        int dst = 0;
//        if (date != null && TimeZone.getTimeZone(zone).inDaylightTime(date)) {
//            dst = TimeZone.getTimeZone(zone).getDSTSavings();
//        }
//
//        return dst;
//    }
    /**
     *
     * @param seconds
     * @param zone
     * @return the Date using system time zone.
     */
    public static Date getUtcTimeFrom(boolean isUtc, Date date, Integer seconds, String zone) {
        if(seconds == null)
            return null;
        
        if (isUtc) {
            seconds += getOffset(date, zone) / 1000;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(1970, 1, 1, seconds / 3600, seconds / 60 % 60, seconds % 60);
        //Date date1 = new Date(1970, 1, 1, seconds / 3600, seconds / 60 % 60, seconds % 60);
        //return date1;
        return calendar.getTime();

//        if (isUtc) {
//
//            if (seconds != null) {
//                //return new Date(seconds * 1000L + getDSTSavings(date, zone));
//                return new Date(seconds * 1000L);
//            }
//        } else if (seconds != null && zone != null) {
//            //seconds -= TimeZone.getTimeZone(zone).getRawOffset() / 1000;
//            //seconds -= getOffset(new Date(), zone)/1000;
//            //seconds -= getOffset(echoDate, zone)/1000;
//        }
//        return null;
    }

    public static Long getUTCSecsFrom(boolean isUtc, Date date, Date time, String zone) {
        
        long secs = getLocalSecsFrom(isUtc,  time, zone);
        secs-= getOffset(date, zone) / 1000;
//        Logger.getLogger("TimeHelper").log(Level.INFO, "time: {0}", time);
//        Logger.getLogger("TimeHelper").log(Level.INFO, "Secs: {0}", secs);

//            if (isUtc) {
//                int dst = getDSTSavings(time, zone);
//                secs = (time.getTime() + dst) / 1000;//date to secs
//                //secs = time.getTime() / 1000;
//            } else {
//                secs = time.getTime() / 1000;
//                //secs += TimeZone.getTimeZone(zone).getRawOffset() / 1000;
//                //secs += TimeZone.getTimeZone(zone).getOffset(new Date().getTime()) / 1000;
//                secs -= getOffset(time, zone) / 1000;
//            }

        return secs;
    }

    public static Long getLocalSecsFrom(boolean isUtc, Date time, String zone) {
        long secs = 0;
        if (time != null) {
//            if (isUtc) {
//                secs = time.getTime() / 1000;               
//                secs += TimeZone.getTimeZone(zone).getRawOffset() / 1000;
//            } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);
            secs = calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND);
            // time= 1970.1.1 but one hour difference from Linux and Windows if we use secs = time.getTime() /1000;

//                secs = time.getTime() / 1000;
//                System.out.println(new Date());
//                
//                Logger.getLogger("TimeHelper").log(Level.INFO, "Secs: {0}", secs);
//                
//                int offset = getOffset(new Date(), zone) / 1000;
//                secs += offset;
//                Logger.getLogger("TimeHelper").log(Level.INFO, "Offset: {0}", offset);
//                Logger.getLogger("TimeHelper").log(Level.INFO, TimeZone.getDefault().getDisplayName());
//                String str = "getLocalSecsFrom utc=false zone=" + zone + " time=" + time.toString() + ": " + time.getTime() / 1000;
//                Logger.getLogger("TimeHelper").log(Level.INFO, str);
//            }
        }
        return secs;
    }

    //Consider Day Light Saving
    public static Integer getUtcSecsFromLocalSecs(Date date, Integer localSecs, String zone) {
        return localSecs - getOffset(date, zone) / 1000;
    }
}
