package ru.faulab.attendence.ui;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.awt.*;

public interface AddAttendanceDialogFactory {

    @Inject
    AddAttendanceDialog create(@Assisted Frame owner);
}
