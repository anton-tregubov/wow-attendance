package ru.faulab.attendence.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import ru.faulab.attendence.dto.store.Attendance;

import java.util.Date;

public interface AttendanceService {
    ListenableFuture<ImmutableSet<Attendance>> loadAttendanceFromPeriod(Date from, Date to);

    ListenableFuture<AddAttendanceReport> addAttendances(Date day, ImmutableSet<String> nicknames);

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
