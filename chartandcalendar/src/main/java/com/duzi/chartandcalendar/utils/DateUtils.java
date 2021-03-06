/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.duzi.chartandcalendar.utils;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.SHORT;

public abstract class DateUtils
{
    private static Long fixedLocalTime = null;

    private static TimeZone fixedTimeZone = null;

    /**
     * 하루를 밀리세컨드로
     */
    public static long millisecondsInOneDay = 24 * 60 * 60 * 1000;

    public static long applyTimezone(long localTimestamp)
    {
        TimeZone tz = getTimezone();
        return localTimestamp - tz.getOffset(localTimestamp - tz.getOffset(localTimestamp));
    }

    public static String formatHeaderDate(GregorianCalendar day)
    {
        Locale locale = Locale.getDefault();
        String dayOfMonth = Integer.toString(day.get(DAY_OF_MONTH));
        String dayOfWeek = day.getDisplayName(DAY_OF_WEEK, SHORT, locale);
        return dayOfWeek + "\n" + dayOfMonth;
    }

    public static String formatTime(Context context, int hours, int minutes)
    {
        int reminderMilliseconds = (hours * 60 + minutes) * 60 * 1000;

        Date date = new Date(reminderMilliseconds);
        java.text.DateFormat df = DateFormat.getTimeFormat(context);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        return df.format(date);
    }

    public static String formatWeekdayList(Context context, boolean weekday[])
    {
        String shortDayNames[] = getShortDayNames();
        String longDayNames[] = getLongDayNames();
        StringBuilder buffer = new StringBuilder();

        int count = 0;
        int first = 0;
        boolean isFirst = true;
        for (int i = 0; i < 7; i++)
        {
            if (weekday[i])
            {
                if (isFirst) first = i;
                else buffer.append(", ");

                buffer.append(shortDayNames[i]);
                isFirst = false;
                count++;
            }
        }

        if (count == 1) return longDayNames[first];
        if (count == 2 && weekday[0] && weekday[1])
            return "주말";
        if (count == 5 && !weekday[0] && !weekday[1])
            return "월 ~ 금";
        if (count == 7) return "한 주의 어떤 날";
        return buffer.toString();
    }

    public static GregorianCalendar getCalendar(long timestamp)
    {
        GregorianCalendar day =
            new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        day.setTimeInMillis(timestamp);
        return day;
    }

    public static String[] getDayNames(int format)
    {
        String[] wdays = new String[7];

        Calendar day = new GregorianCalendar();
        day.set(DAY_OF_WEEK, Calendar.SATURDAY);

        for (int i = 0; i < wdays.length; i++)
        {
            wdays[i] =
                day.getDisplayName(DAY_OF_WEEK, format, Locale.getDefault());
            day.add(DAY_OF_MONTH, 1);
        }

        return wdays;
    }

    public static long getLocalTime()
    {
        if (fixedLocalTime != null) return fixedLocalTime;

        TimeZone tz = getTimezone();
        long now = new Date().getTime();
        return now + tz.getOffset(now);
    }

    /**
     * locale 세팅에 따라서 [월,화,수,목,금,토,일] 로 표현
     */
    public static String[] getLocaleDayNames(int format)
    {
        String[] days = new String[7];

        Calendar calendar = new GregorianCalendar();
        calendar.set(DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        for (int i = 0; i < days.length; i++)
        {
            days[i] = calendar.getDisplayName(DAY_OF_WEEK, format,
                Locale.getDefault());
            calendar.add(DAY_OF_MONTH, 1);
        }

        return days;
    }

    /**
     * locale 세팅에 따라서 [2,3,4,5,6,7,1] 로 표현
     */
    public static Integer[] getLocaleWeekdayList()
    {
        Integer[] dayNumbers = new Integer[7];
        Calendar calendar = new GregorianCalendar();
        calendar.set(DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        for (int i = 0; i < dayNumbers.length; i++)
        {
            dayNumbers[i] = calendar.get(DAY_OF_WEEK);
            calendar.add(DAY_OF_MONTH, 1);
        }
        return dayNumbers;
    }

    public static String[] getLongDayNames()
    {
        return getDayNames(GregorianCalendar.LONG);
    }

    public static String[] getShortDayNames()
    {
        return getDayNames(SHORT);
    }

    public static long getStartOfDay(long timestamp)
    {
        return (timestamp / millisecondsInOneDay) * millisecondsInOneDay;
    }

    public static long getStartOfToday()
    {
        return getStartOfDay(DateUtils.getLocalTime());
    }

    public static long millisecondsUntilTomorrow()
    {
        return getStartOfToday() + millisecondsInOneDay - getLocalTime();
    }

    public static GregorianCalendar getStartOfTodayCalendar()
    {
        return getCalendar(getStartOfToday());
    }

    public static TimeZone getTimezone()
    {
        if(fixedTimeZone != null) return fixedTimeZone;
        return TimeZone.getDefault();
    }

    public static void setFixedTimeZone(TimeZone tz)
    {
        fixedTimeZone = tz;
    }

    public static int getWeekday(long timestamp)
    {
        GregorianCalendar day = getCalendar(timestamp);
        return javaWeekdayToLoopWeekday(day.get(DAY_OF_WEEK));
    }

    /**
     * Throughout the code, it is assumed that the weekdays are numbered from 0
     * (Saturday) to 6 (Friday). In the Java Calendar they are numbered from 1
     * (Sunday) to 7 (Saturday). This function converts from Java to our
     * internal representation.
     *
     * @return weekday number in the internal interpretation
     */
    public static int javaWeekdayToLoopWeekday(int number)
    {
        return number % 7;
    }

    public static long removeTimezone(long timestamp)
    {
        TimeZone tz = getTimezone();
        return timestamp + tz.getOffset(timestamp);
    }

    public static void setFixedLocalTime(Long timestamp)
    {
        fixedLocalTime = timestamp;
    }

    public static Long truncate(TruncateField field, long timestamp)
    {
        GregorianCalendar cal = DateUtils.getCalendar(timestamp);

        switch (field)
        {
            case MONTH:
                cal.set(DAY_OF_MONTH, 1);
                return cal.getTimeInMillis();

            case WEEK_NUMBER:
                int firstWeekday = cal.getFirstDayOfWeek();
                int weekday = cal.get(DAY_OF_WEEK);
                int delta = weekday - firstWeekday;
                if (delta < 0) delta += 7;
                cal.add(Calendar.DAY_OF_YEAR, -delta);
                return cal.getTimeInMillis();

            case QUARTER:
                int quarter = cal.get(Calendar.MONTH) / 3;
                cal.set(DAY_OF_MONTH, 1);
                cal.set(Calendar.MONTH, quarter * 3);
                return cal.getTimeInMillis();

            case YEAR:
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(DAY_OF_MONTH, 1);
                return cal.getTimeInMillis();

            default:
                throw new IllegalArgumentException();
        }
    }

    public enum TruncateField
    {
        MONTH, WEEK_NUMBER, YEAR, QUARTER
    }

    /**
     * Gets the number of days between two timestamps (exclusively).
     *
     * @param t1 the first timestamp to use in milliseconds
     * @param t2 the second timestamp to use in milliseconds
     * @return the number of days between the two timestamps
     */
    public static int getDaysBetween(long t1, long t2)
    {
        Date d1 = new Date(t1);
        Date d2 = new Date(t2);
        return (int) (Math.abs((d2.getTime() - d1.getTime()) / millisecondsInOneDay));
    }
}
