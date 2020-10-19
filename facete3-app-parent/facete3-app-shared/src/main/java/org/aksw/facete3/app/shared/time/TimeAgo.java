package org.aksw.facete3.app.shared.time;

import java.time.temporal.ChronoUnit;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;


public class TimeAgo {

    public static final List<Long> times = Arrays.asList(
            TimeUnit.DAYS.toMillis(365),
            TimeUnit.DAYS.toMillis(30),
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.HOURS.toMillis(1),
            TimeUnit.MINUTES.toMillis(1),
            TimeUnit.SECONDS.toMillis(1));

    public static final List<ChronoUnit> timesString = Arrays.asList(
            ChronoUnit.YEARS,
            ChronoUnit.MONTHS,
            ChronoUnit.DAYS,
            ChronoUnit.HOURS,
            ChronoUnit.MINUTES,
            ChronoUnit.SECONDS);


    public static String fmtLong(long value, ChronoUnit chronoUnit) {
        return (value == 0 ? "moments" : value) + " " + chronoUnit.toString().toLowerCase() + " ago";
    }

    public static String fmtCompact(long value, ChronoUnit chronoUnit) {
        return value == 0
                ? "now"
                : Long.toString(value) +
                    (ChronoUnit.MONTHS.equals(chronoUnit)
                        ? "M"
                        : Character.toString(chronoUnit.toString().toLowerCase().charAt(0)));
    }

    public static String toDuration(long duration, BiFunction<Long, ChronoUnit, String> formatter) {
        Entry<Long, ChronoUnit> pair = toDuration(duration);
        String result = formatter.apply(pair.getKey(), pair.getValue());
        return result;
    }

    public static Entry<Long, ChronoUnit> toDuration(long duration) {

        long value = 0;
        ChronoUnit unit = null;
        for (int i = 0; i < TimeAgo.times.size(); i++) {
            Long current = TimeAgo.times.get(i);
            long temp = duration / current;
            if (temp > 0) {
                value = temp;
                unit = timesString.get(i);
                break;
            }
        }
        if (unit == null) {
            unit = ChronoUnit.SECONDS;
        }


        return new SimpleEntry<>(value, unit);
    }

    public static void main(String args[]) {
        Locale.setDefault(Locale.GERMAN);

        BiFunction<Long, ChronoUnit, String> labelizer = TimeAgo::fmtCompact;
        System.out.println(toDuration(123, labelizer));
        System.out.println(toDuration(1230, labelizer));
        System.out.println(toDuration(12300, labelizer));
        System.out.println(toDuration(123000, labelizer));
        System.out.println(toDuration(1230000, labelizer));
        System.out.println(toDuration(12300000, labelizer));
        System.out.println(toDuration(123000000, labelizer));
        System.out.println(toDuration(1230000000, labelizer));
        System.out.println(toDuration(12300000000L, labelizer));
        System.out.println(toDuration(123000000000L, labelizer));
    }
}