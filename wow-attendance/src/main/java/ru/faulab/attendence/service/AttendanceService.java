package ru.faulab.attendence.service;

import com.google.common.collect.ImmutableSet;
import ru.faulab.attendence.dto.store.Attendance;

import java.util.Date;

public interface AttendanceService {
    ImmutableSet<Attendance> loadAttendanceByPeriod(Date from, Date to);

    AddAttendanceReport addAttendancies(Date day, ImmutableSet<String> nicknames);

    public final static class AddAttendanceReport {
        public final ImmutableSet<String> unknownPersons;
        public final ImmutableSet<Attendance> alreadyExistedAttendance;
        public final ImmutableSet<Attendance> addedExistedAttendance;

        public AddAttendanceReport(ImmutableSet<String> unknownPersons, ImmutableSet<Attendance> alreadyExistedAttendance, ImmutableSet<Attendance> addedExistedAttendance) {
            this.unknownPersons = unknownPersons;
            this.alreadyExistedAttendance = alreadyExistedAttendance;
            this.addedExistedAttendance = addedExistedAttendance;
        }
    }
}
