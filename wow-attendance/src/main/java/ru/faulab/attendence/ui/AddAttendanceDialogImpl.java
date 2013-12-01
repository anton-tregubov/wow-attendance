package ru.faulab.attendence.ui;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import ru.faulab.attendence.service.AttendanceLogParser;
import ru.faulab.attendence.service.AttendanceLogParserImpl;
import ru.faulab.attendence.service.AttendanceService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.DateFormat;

public class AddAttendanceDialogImpl extends JDialog implements AddAttendanceDialog {
    private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private final MembersListModel parsedMembers;
    private final JTextArea importField;
    private final JList<String> parsedMembersList;
    private final JLabel when;
    private final JButton okButton;
    private final JButton cancelButton;
    private final AttendanceLogParser attendanceLogParser;
    private final AttendanceService attendanceService;
    private AttendanceLogParserImpl.DayAttendance lastParsedData;
    private ListenableFuture<AttendanceService.AddAttendanceReport> addAttendanceReport;

    @Inject
    public AddAttendanceDialogImpl(Frame owner, AttendanceLogParser attendanceLogParser, AttendanceService attendanceService) {
        super(owner);
        this.attendanceLogParser = attendanceLogParser;
        this.attendanceService = attendanceService;
        this.addAttendanceReport = Futures.immediateFuture(null);
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        importField = new JTextArea();
        importField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                parse(importField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                parse(importField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                parse(importField.getText());
            }
        });
        when = new JLabel("42");
        parsedMembers = new MembersListModel();
        parsedMembersList = new JList<String>(parsedMembers);
        okButton = new JButton(new AbstractAction("Импортировать") {
            @Override
            public void actionPerformed(ActionEvent e) {
                addAttendanceReport = AddAttendanceDialogImpl.this.attendanceService.addAttendances(lastParsedData.day, lastParsedData.nicknames);
                AddAttendanceDialogImpl.this.dispose();
            }
        });
        getRootPane().setDefaultButton(okButton);
        cancelButton = new JButton(new AbstractAction("Отменить") {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddAttendanceDialogImpl.this.dispose();
            }
        });

        JScrollPane scrollableList = new JScrollPane(parsedMembersList);
        JScrollPane scrollableText = new JScrollPane(importField);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addComponent(scrollableText)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(when)
                                .addComponent(scrollableList, 100, 100, 200)
                        )
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(okButton)
                        .addComponent(cancelButton))

        );

        layout.linkSize(SwingConstants.HORIZONTAL, okButton, cancelButton);

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(scrollableText)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(when)
                                .addComponent(scrollableList)
                        )
                ).addGroup(layout.createParallelGroup()
                        .addComponent(okButton)
                        .addComponent(cancelButton))

        );


    }

    @Override
    public ListenableFuture<AttendanceService.AddAttendanceReport> importAttendance() {
        initFromClipboard();
        setSize(500, 300);
        setLocationRelativeTo(getParent());
        setVisible(true);
        return addAttendanceReport;
    }

    private void initFromClipboard() {
        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            try {
                String data = (String) systemClipboard.getData(DataFlavor.stringFlavor);
                importField.setText(data);
            } catch (UnsupportedFlavorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private synchronized void parse(String text) {
        AttendanceLogParserImpl.DayAttendance parse = null;
        try {
            parse = attendanceLogParser.parse(text);
        } catch (Exception e) {
            e.printStackTrace();
            lastParsedData = null;
            invalidateButtons();
            return;
        }
        parsedMembers.setMembers(Ordering.natural().immutableSortedCopy(parse.nicknames));
        when.setText(DATE_FORMAT.format(parse.day));
        lastParsedData = parse;
        invalidateButtons();
    }

    private void invalidateButtons() {
        okButton.setEnabled(lastParsedData != null && lastParsedData.day != null && lastParsedData.nicknames.size() > 0);
    }

    private static class MembersListModel extends AbstractListModel<String> {

        private ImmutableList<String> members = ImmutableList.of();

        private void setMembers(ImmutableList<String> members) {
            this.members = members;
            fireContentsChanged(this, 0, members.size());
        }

        @Override
        public int getSize() {
            return members.size();
        }

        @Override
        public String getElementAt(int index) {
            return members.get(index);
        }
    }
}
