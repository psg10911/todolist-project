package Todo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * 우측 할 일 목록(JTable)과 버튼을 담당하는 패널 (JPanel).
 * - 전체 일정 검색 기능
 * - 행 드래그로 순서 변경 가능
 * - 더미 일정 자동 등록 및 중복 방지
 */
public class TaskPanel extends JPanel {

    private JLabel selectedDateLabel;
    private JTable table;
    private DefaultTableModel tableModel;
    private LocalDate currentDate;

    // ✅ 모든 날짜의 일정 저장 (검색, 표시 공용)
    private final ArrayList<Task> allTasks = new ArrayList<>();

    private final String[] columnNames = { "완료", "일정 제목", "시작일", "종료일" };

    public TaskPanel() {
        setLayout(new BorderLayout(5, 10));
        setPreferredSize(new Dimension(400, 0));

        // -------------------------------
        // 1️⃣ 상단 영역 (날짜 + 정렬 + 검색)
        // -------------------------------
        JPanel topPanel = new JPanel(new BorderLayout());
        selectedDateLabel = new JLabel(" ", SwingConstants.CENTER);
        selectedDateLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        topPanel.add(selectedDateLabel, BorderLayout.CENTER);

        JPanel sortSearchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JComboBox<String> sortComboBox = new JComboBox<>(new String[] { "최신순", "중요도순", "완료된순" });
        JButton searchBtn = new JButton("🔍 검색");
        sortSearchPanel.add(sortComboBox);
        sortSearchPanel.add(searchBtn);
        topPanel.add(sortSearchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // -------------------------------
        // 2️⃣ 중앙 JTable 영역
        // -------------------------------
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // 체크박스만 수정 가능
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);

        // ✅ 드래그로 순서 변경 가능
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new TableRowTransferHandler(table));

        add(new JScrollPane(table), BorderLayout.CENTER);

        // -------------------------------
        // 3️⃣ 하단 버튼
        // -------------------------------
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton addTaskBtn = new JButton("할 일 추가");
        JButton delTaskBtn = new JButton("할 일 삭제");
        buttonPanel.add(addTaskBtn);
        buttonPanel.add(delTaskBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // -------------------------------
        // 4️⃣ 더미 일정 미리 등록 (검색 가능)
        // -------------------------------
        registerDummyTasks();

        // -------------------------------
        // 5️⃣ 버튼 이벤트 처리
        // -------------------------------
        addTaskBtn.addActionListener(e -> {
            TaskDialog dialog = new TaskDialog((JFrame) SwingUtilities.getWindowAncestor(this), currentDate);
            dialog.setVisible(true);
            if (dialog.getTask() != null) {
                Task newTask = dialog.getTask();
                addIfNotExists(newTask);

                // 현재 날짜면 바로 화면에 표시
                if (currentDate != null && newTask.getStartDate().equals(currentDate.toString())) {
                    tableModel.addRow(newTask.toObjectArray());
                }
            }
        });

        delTaskBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "이 할 일을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    String title = tableModel.getValueAt(selectedRow, 1).toString();
                    String start = tableModel.getValueAt(selectedRow, 2).toString();
                    tableModel.removeRow(selectedRow);
                    allTasks.removeIf(t -> t.getTitle().equals(title) && t.getStartDate().equals(start));
                }
            } else {
                JOptionPane.showMessageDialog(this, "삭제할 할 일을 선택해주세요.");
            }
        });

        searchBtn.addActionListener(e -> openSearchDialog());
    }

    // ✅ 더미 일정 초기 등록 (검색 시 항상 포함됨)
    private void registerDummyTasks() {
        Task t1 = new Task("자바 Swing 스터디", "스터디 내용", "2025-11-10", "2025-11-10");
        Task t2 = new Task("프로젝트 디자인 구상", "프로젝트 회의", "2025-11-10", "2025-11-10");
        Task t3 = new Task("하이 하이 테스트", "테스트 일정", "2025-08-10", "2025-08-12");
        Task t4 = new Task("하이 분석", "분석 작업", "2025-08-10", "2025-08-15");

        addIfNotExists(t1);
        addIfNotExists(t2);
        addIfNotExists(t3);
        addIfNotExists(t4);
    }

    /**
     * 🔍 전체 일정 검색 모달창
     */
    private void openSearchDialog() {
        JDialog searchDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "전체 일정 검색", true);
        searchDialog.setLayout(new BorderLayout(10, 10));
        searchDialog.setSize(420, 350);
        searchDialog.setLocationRelativeTo(this);

        JPanel searchTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JLabel searchLabel = new JLabel("키워드:");
        JTextField searchField = new JTextField(15);
        JButton execBtn = new JButton("검색");
        searchTop.add(searchLabel);
        searchTop.add(searchField);
        searchTop.add(execBtn);
        searchDialog.add(searchTop, BorderLayout.NORTH);

        DefaultTableModel resultModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable resultTable = new JTable(resultModel);
        resultTable.setRowHeight(28);
        searchDialog.add(new JScrollPane(resultTable), BorderLayout.CENTER);

        JButton closeBtn = new JButton("닫기");
        closeBtn.addActionListener(e -> searchDialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(closeBtn);
        searchDialog.add(bottom, BorderLayout.SOUTH);

        execBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.length() < 2 || keyword.contains(" ")) {
                JOptionPane.showMessageDialog(searchDialog, "키워드는 2글자 이상이며 공백을 포함할 수 없습니다.");
                return;
            }

            resultModel.setRowCount(0);
            for (Task t : allTasks) {
                if (t.getTitle().contains(keyword)
                        || t.getStartDate().contains(keyword)
                        || t.getEndDate().contains(keyword)) {
                    resultModel
                            .addRow(new Object[] { t.isCompleted(), t.getTitle(), t.getStartDate(), t.getEndDate() });
                }
            }

            if (resultModel.getRowCount() == 0)
                JOptionPane.showMessageDialog(searchDialog, "검색 결과가 없습니다.");
        });

        searchDialog.setVisible(true);
    }

    /**
     * 📅 날짜 변경 시 호출
     */
    public void loadTasksForDate(LocalDate date) {
        this.currentDate = date;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일(E)");
        selectedDateLabel.setText(date.format(formatter));
        tableModel.setRowCount(0);

        for (Task t : allTasks) {
            if (t.getStartDate().equals(date.toString())) {
                tableModel.addRow(t.toObjectArray());
            }
        }
    }

    /**
     * ✅ 중복 방지 후 allTasks 추가
     */
    private void addIfNotExists(Task task) {
        boolean exists = allTasks.stream()
                .anyMatch(t -> t.getTitle().equals(task.getTitle())
                        && t.getStartDate().equals(task.getStartDate()));
        if (!exists)
            allTasks.add(task);
    }

    /**
     * ✅ JTable 행 드래그 순서 변경용 TransferHandler
     */
    static class TableRowTransferHandler extends TransferHandler {
        private final JTable table;

        public TableRowTransferHandler(JTable table) {
            this.table = table;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            int row = table.getSelectedRow();
            return new StringSelection(String.valueOf(row));
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDrop();
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!support.isDrop())
                return false;
            JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
            int dropRow = dl.getRow();
            int dragRow;
            try {
                dragRow = Integer.parseInt((String) support.getTransferable()
                        .getTransferData(DataFlavor.stringFlavor));
            } catch (UnsupportedFlavorException | IOException e) {
                return false;
            }

            if (dragRow == dropRow)
                return false;
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            Object[] rowData = new Object[model.getColumnCount()];
            for (int i = 0; i < model.getColumnCount(); i++)
                rowData[i] = model.getValueAt(dragRow, i);
            model.removeRow(dragRow);
            if (dropRow > dragRow)
                dropRow--;
            model.insertRow(dropRow, rowData);
            table.setRowSelectionInterval(dropRow, dropRow);
            return true;
        }
    }
}
