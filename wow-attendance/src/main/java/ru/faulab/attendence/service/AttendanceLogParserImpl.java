package ru.faulab.attendence.service;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttendanceLogParserImpl implements AttendanceLogParser {
    private static final Pattern DATE = Pattern.compile("(?<date>\\d\\d/\\d\\d/\\d\\d\\d\\d)");
    private static final Pattern HUMANS = Pattern.compile("Участники[\\p{Graph}]+\\n(?<human>[а-я,А-Я ]+)");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public synchronized DayAttendance parse(String s) {
        Matcher humansRow = HUMANS.matcher(s);
        Matcher dateRow = DATE.matcher(s);

        String dateS = dateRow.find() ? dateRow.group("date") : null;
        Date date = null;
        try {
            date = DATE_FORMAT.parse(dateS);
        } catch (ParseException e) {
            //todo - make exception more User Friendly
            Throwables.propagate(e);
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


}
