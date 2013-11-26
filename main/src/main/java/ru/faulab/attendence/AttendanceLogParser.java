package ru.faulab.attendence;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import ru.faulab.attendence.store.Attendance;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttendanceLogParser {
    public static final Pattern DATE = Pattern.compile("(?<date>\\d\\d/\\d\\d/\\d\\d\\d\\d)");
    public static final Pattern HUMANS = Pattern.compile("Участники[\\p{Graph}]+\\n(?<human>[а-я,А-Я ]+)");
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    public DayAttendance parse(String s) {
        Matcher humansRow = HUMANS.matcher(s);
        Matcher dateRow = DATE.matcher(s);

        String dateS = dateRow.find() ? dateRow.group("date") : null;
        Date date = null;
        try {
            date = DATE_FORMAT.parse(dateS);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ImmutableSet.Builder<String> attendances = ImmutableSet.builder();
        while (humansRow.find()) {
            String humanS = humansRow.group("human");
            Iterable<String> names = Splitter.on(", ").split(humanS);
            for (String name : names) {
                attendances.add(name);
            }
        }
        return new DayAttendance(date, attendances.build());
    }

    public static class DayAttendance {
        public final Date day;
        public final ImmutableSet<String> nicknames;

        public DayAttendance(Date day, ImmutableSet<String> nicknames) {
            this.day = day;
            this.nicknames = nicknames;
        }
    }
}
