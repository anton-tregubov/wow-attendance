package ru.faulab.attendence.ui;

import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import com.google.inject.Inject;
import ru.faulab.attendence.dto.store.Attendance;
import ru.faulab.attendence.dto.store.Character;
import ru.faulab.attendence.service.AttendanceService;
import ru.faulab.attendence.service.CharacterService;
import ru.faulab.attendence.service.CharacterServiceImpl;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SortedSet;

public class MainFrame extends JFrame {
    private final JTable journal;
    private final JToolBar buttonBar;
    private final AttendanceService attendanceService;
    private final CharacterService characterService;
    private final JournalTableModel journalTableModel;

    @Inject
    public MainFrame(AttendanceService attendanceService, CharacterService characterService, final AddAttendanceDialogFactory attendanceDialogProvider) throws HeadlessException {
        this.attendanceService = attendanceService;
        this.characterService = characterService;

        journalTableModel = new JournalTableModel();
        journal = new JTable(journalTableModel);
        journal.setDefaultRenderer(Boolean.class, new BooleanRenderer());
        journal.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        buttonBar = new JToolBar();
        buttonBar.setFloatable(false);
        buttonBar.add(new AbstractAction("Импортировать посещение") {
            @Override
            public void actionPerformed(ActionEvent e) {
                AttendanceService.AddAttendanceReport addAttendanceReport = attendanceDialogProvider.create(MainFrame.this).importAttendance();
                updateTableAttendance();
            }
        });
        Container pane = getContentPane();
        pane.setLayout(new BorderLayout());
        pane.add(buttonBar, BorderLayout.NORTH);
        pane.add(new JScrollPane(journal), BorderLayout.CENTER);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void init() {
        CharacterServiceImpl.Report sync = characterService.sync();
        updateTableAttendance();
    }

    private void updateTableAttendance() {
        Calendar toDay = Calendar.getInstance();
        Date currentTime = new Date();
        toDay.setTime(currentTime);
        toDay.set(Calendar.SECOND, 0);
        toDay.set(Calendar.MINUTE, 0);
        toDay.set(Calendar.HOUR_OF_DAY, 0);
        toDay.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);

        Calendar fromDay = (Calendar) toDay.clone();
        fromDay.roll(Calendar.WEEK_OF_YEAR, -4);

        System.out.println();
        journalTableModel.setAttendancies(attendanceService.loadAttendanceByPeriod(fromDay.getTime(), toDay.getTime()), characterService.allRegisteredCharacters());
    }

    private static class JournalTableModel extends AbstractTableModel {

        private Date[] dates = new Date[0];
        private String[] nicknames = new String[0];
        private Boolean[][] attendance = new Boolean[0][0];
        private final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);

        public synchronized String getColumnName(int column) {
            return column == 0 ? "Герои/Даты" : DATE_FORMAT.format(dates[column - 1]);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? String.class : Boolean.class;
        }

        @Override
        public int getRowCount() {
            return nicknames.length;
        }

        @Override
        public int getColumnCount() {
            return dates.length + 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return columnIndex == 0 ? nicknames[rowIndex] : attendance[rowIndex][columnIndex - 1];
        }

        public void setAttendancies(Iterable<Attendance> attendancies, Iterable<Character> characters) {
            SortedSet<String> names = Sets.newTreeSet();
            SortedSet<Date> dates = Sets.newTreeSet();
            SetMultimap<String, Date> indexedAttendance = TreeMultimap.create();
            for (Attendance attendance : attendancies) {
                String nickname = attendance.getCharacter().getNickname();
                Date day = attendance.getDay();
                dates.add(day);
                indexedAttendance.put(nickname, day);
            }
            for (Character character : characters) {
                names.add(character.getNickname());
            }
            Boolean[][] attendencyB = new Boolean[names.size()][dates.size()];
            int nI = 0;
            for (String name : names) {
                int dI = 0;
                for (Date date : dates) {
                    attendencyB[nI][dI] = indexedAttendance.containsEntry(name, date);
                    dI++;
                }
                nI++;
            }
            this.nicknames = Iterables.toArray(names, String.class);
            this.dates = Iterables.toArray(dates, Date.class);
            this.attendance = attendencyB;
            fireTableStructureChanged();
        }
    }

    static class BooleanRenderer extends JCheckBox implements TableCellRenderer {
        private static final Border NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
        private static final Color GREEN = new Color(170, 187, 155);
        private static final Color RED = new Color(231, 158, 149);

        public BooleanRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER);
            setBorderPainted(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            boolean selected = value != null && (Boolean) value;
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(selected ? GREEN.darker() : RED.darker());
            } else {
                setForeground(table.getForeground());
                setBackground(selected ? GREEN : RED);
            }

            setSelected(selected);

            if (hasFocus) {
                setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            } else {
                setBorder(NO_FOCUS_BORDER);
            }

            return this;
        }
    }
}
