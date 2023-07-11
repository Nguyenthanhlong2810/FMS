package org.opentcs.kernel.periodreport;

import java.util.Calendar;

public enum Period {
    NONE(0),
    DAILY(1),
    WEEKLY(7),
    MONTHLY(30),
    YEARLY(365);

    private int represent;

    Period(int represent) {
        this.represent = represent;
    }

    public int getRepresent() {
        return represent;
    }

    public static Period getPeriod(int represent) {
        if (represent == 0) {
            return NONE;
        }
        if (represent == 1) {
            return DAILY;
        }
        if (represent == 7) {
            return WEEKLY;
        }
        if (represent == 30) {
            return MONTHLY;
        }
        if (represent == 365) {
            return YEARLY;
        }
        return WEEKLY;
    }

    public long getTime() {
        Calendar calendar = Calendar.getInstance();
        long diff = calendar.getTimeInMillis();
        switch (this) {
            case NONE:
                return 0;
            case DAILY:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case WEEKLY:
                calendar.add(Calendar.WEEK_OF_MONTH, 1);
                break;
            case MONTHLY:
                calendar.add(Calendar.MONTH, 1);
                break;
            case YEARLY:
                calendar.add(Calendar.YEAR, 1);
                break;
        }
        diff = calendar.getTimeInMillis() - diff;

        return diff;
    }

    public long getPeriodDelay() {
        Calendar calendar = Calendar.getInstance();
        long diff = calendar.getTimeInMillis();
        switch (this) {
            case DAILY:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case WEEKLY:
                calendar.set(Calendar.DAY_OF_WEEK, 1);
                calendar.add(Calendar.WEEK_OF_MONTH, 1);
                break;
            case MONTHLY:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.MONTH, 1);
                break;
            case YEARLY:
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                calendar.add(Calendar.YEAR, 1);
                break;
        }
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        diff = calendar.getTimeInMillis() - diff;

        return diff;
    }

    public long getLastTimeInMilis(long milis) {
        Calendar calendar = Calendar.getInstance();
        switch (this) {
            case DAILY:
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                break;
            case WEEKLY:
                calendar.add(Calendar.WEEK_OF_MONTH, -1);
                break;
            case MONTHLY:
                calendar.add(Calendar.MONTH, -1);
                break;
            case YEARLY:
                calendar.add(Calendar.YEAR, -1);
                break;
        }
        return calendar.getTimeInMillis();
    }
}
