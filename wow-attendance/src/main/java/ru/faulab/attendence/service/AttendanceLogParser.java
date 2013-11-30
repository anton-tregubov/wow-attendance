package ru.faulab.attendence.service;

import com.google.common.collect.ImmutableSet;

import java.util.Date;

public interface AttendanceLogParser {
    AttendanceLogParserImpl.DayAttendance parse(String s);

    public final static class DayAttendance {
        public final Date day;
        public final ImmutableSet<String> nicknames;

        public DayAttendance(Date day, ImmutableSet<String> nicknames) {
            this.day = day;
            this.nicknames = nicknames;
        }
    }
}
