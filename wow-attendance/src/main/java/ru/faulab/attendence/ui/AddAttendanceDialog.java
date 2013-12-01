package ru.faulab.attendence.ui;

import com.google.common.util.concurrent.ListenableFuture;
import ru.faulab.attendence.service.AttendanceService;

public interface AddAttendanceDialog {
    ListenableFuture<AttendanceService.AddAttendanceReport> importAttendance();
}
