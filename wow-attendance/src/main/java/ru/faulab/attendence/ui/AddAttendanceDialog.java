package ru.faulab.attendence.ui;

import ru.faulab.attendence.service.AttendanceService;

public interface AddAttendanceDialog {
    AttendanceService.AddAttendanceReport importAttendance();
}
