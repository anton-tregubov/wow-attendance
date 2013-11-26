package ru.faulab.attendence;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class AddAttendanceDialog extends JDialog {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");
    private final MembersListModel parsedMembers;
    private final JTextArea importField;
    private final JList<String> parsedMembersList;
    private final JLabel when;
    private final JButton okButton;
    private final JButton cancelButton;
    private final AttendanceLogParser attendanceLogParser;
    private final AttendanceService attendanceService;
    private AttendanceLogParser.DayAttendance lastParsedData;

    public AddAttendanceDialog(Frame owner, AttendanceLogParser attendanceLogParser, AttendanceService attendanceService) {
        super(owner);
        this.attendanceLogParser = attendanceLogParser;
        this.attendanceService = attendanceService;
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
                AttendanceService.AddAttendanceReport addAttendanceReport = AddAttendanceDialog.this.attendanceService.addAttendancies(lastParsedData.day, lastParsedData.nicknames);
                AddAttendanceDialog.this.dispose();
            }
        });
        getRootPane().setDefaultButton(okButton);
        cancelButton = new JButton(new AbstractAction("Отменить") {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddAttendanceDialog.this.dispose();
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

        initFromClipboard();
        setSize(500, 300);
        setLocationRelativeTo(owner);
        setVisible(true);
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

    private void parse(String text) {
        AttendanceLogParser.DayAttendance parse = null;
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
