package Todo;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

public class TaskPanel extends JPanel {

    private JLabel selectedDateLabel;
    private JTable table;
    private TaskTableModel model;
    private TableRowSorter<TaskTableModel> sorter;
    private LocalDate currentDate;
    private String currentUserId;

    public TaskPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BACKGROUND);
        setPreferredSize(new Dimension(450, 0)); // 너비 약간 증가

        // 1) 상단: 날짜 + 검색/정렬
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Theme.BACKGROUND);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        selectedDateLabel = new JLabel(" ", SwingConstants.LEFT);
        selectedDateLabel.setFont(Theme.FONT_BOLD_24);
        selectedDateLabel.setForeground(Theme.TEXT_MAIN);
        topPanel.add(selectedDateLabel, BorderLayout.CENTER);

        JPanel sortSearchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        sortSearchPanel.setBackground(Theme.BACKGROUND);
        
        JComboBox<String> sortComboBox = new JComboBox<>(new String[]{"최신순", "제목순", "완료된순"});
        sortComboBox.setFont(Theme.FONT_REGULAR_12);
        sortComboBox.setBackground(Color.WHITE);
        
        JButton searchBtn = new JButton("검색");
        Theme.styleButton(searchBtn);
        searchBtn.setPreferredSize(new Dimension(80, 30));
        
        sortSearchPanel.add(sortComboBox);
        sortSearchPanel.add(searchBtn);
        topPanel.add(sortSearchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2) 중앙: 리스트
        model = new TaskTableModel();
        table = new JTable(model);
        Theme.styleTable(table); // 테이블 스타일 적용

        table.getColumnModel().getColumn(0).setPreferredWidth(50); // 완료 체크박스
        table.getColumnModel().getColumn(1).setPreferredWidth(200); // 제목

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new TableRowReorderTransferHandler(table, model));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // 스크롤판 외곽선 제거
        add(scrollPane, BorderLayout.CENTER);

        // 3) 하단 버튼
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setBackground(Theme.BACKGROUND);
        
        JButton addBtn = new JButton("추가");
        JButton editBtn = new JButton("수정");
        JButton delBtn = new JButton("삭제");
        
        Theme.styleButton(addBtn);
        Theme.styleButton(editBtn);
        Theme.styleDangerButton(delBtn); // 삭제 버튼만 빨간색

        bottom.add(addBtn);
        bottom.add(editBtn);
        bottom.add(delBtn);
        add(bottom, BorderLayout.SOUTH);

        // ===== 리스너 (기존 로직 유지) =====
        sortComboBox.addActionListener(e -> {
            String sel = (String) sortComboBox.getSelectedItem();
            sorter.setSortKeys(null);
            if ("최신순".equals(sel)) {
                sorter.setComparator(2, (a, b) -> nullSafeStringCompare((String) b, (String) a));
                sorter.toggleSortOrder(2);
            } else if ("제목순".equals(sel)) {
                sorter.toggleSortOrder(1);
            } else if ("완료된순".equals(sel)) {
                sorter.setComparator(0, (a, b) -> Boolean.compare((Boolean) b, (Boolean) a));
                sorter.toggleSortOrder(0);
            }
        });

        searchBtn.addActionListener(e -> openSearchDialog());

        addBtn.addActionListener(e -> {
            if (!ensureUserBound()) return;
            TaskDialog dialog = new TaskDialog((JFrame) SwingUtilities.getWindowAncestor(this), currentDate);
            dialog.setVisible(true);
            Task t = dialog.getTask();
            if (t == null) return;
            t.setUserId(currentUserId);
            int newId = TodoDao.insert(t);
            t.setId(newId);
            model.addTask(t);
        });

        editBtn.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "수정할 할 일을 선택해주세요.");
                return;
            }
            int row = table.convertRowIndexToModel(viewRow);
            Task original = model.getTaskAt(row);
            TaskDialog dialog = new TaskDialog((JFrame) SwingUtilities.getWindowAncestor(this), original);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            Task updated = dialog.getTask();
            if (updated == null) return;
            updated.setId(original.getId());
            updated.setUserId(original.getUserId());
            TodoDao.update(updated);
            model.updateTask(row, updated);
        });

        delBtn.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "삭제할 할 일을 선택해주세요.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(
                    this, "이 할 일을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            int row = table.convertRowIndexToModel(viewRow);
            Task t = model.getTaskAt(row);
            TodoDao.delete(t.getId(), t.getUserId());
            model.removeAt(row);
        });
    }

    private static int nullSafeStringCompare(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return a.compareTo(b);
    }

    public void setCurrentUserId(String userId) { this.currentUserId = userId; }

    public void initAfterLogin(String userId) {
        setCurrentUserId(userId);
        loadTasksForDate(LocalDate.now());
    }

    public void loadTasksForDate(LocalDate date) {
        this.currentDate = date;
        selectedDateLabel.setText(date.format(DateTimeFormatter.ofPattern("MM월 dd일 (E)"))); // 포맷 간소화
        model.getAll().clear();
        if (currentUserId != null && !currentUserId.isBlank()) {
            model.getAll().addAll(TodoDao.findByDate(currentUserId, date));
        }
        model.fireTableDataChanged();
    }

    private boolean ensureUserBound() {
        if (currentUserId == null || currentUserId.isBlank()) {
            JOptionPane.showMessageDialog(this, "로그인 사용자 정보를 먼저 설정하세요.");
            return false;
        }
        return true;
    }

    private void openSearchDialog() {
        JDialog searchDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "전체 일정 검색", true);
        searchDialog.setLayout(new BorderLayout(10, 10));
        searchDialog.setSize(500, 400);
        searchDialog.setLocationRelativeTo(this);
        searchDialog.getContentPane().setBackground(Color.WHITE);

        JPanel searchTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        searchTop.setBackground(Color.WHITE);
        JLabel searchLabel = new JLabel("키워드:");
        JTextField searchField = new JTextField(18);
        Theme.styleTextField(searchField); // 스타일 적용
        JButton execBtn = new JButton("검색");
        Theme.styleButton(execBtn); // 스타일 적용
        
        searchTop.add(searchLabel);
        searchTop.add(searchField);
        searchTop.add(execBtn);
        searchDialog.add(searchTop, BorderLayout.NORTH);

        String[] cols = {"완료", "일정 제목", "시작일", "종료일"};
        DefaultTableModel resultModel = new DefaultTableModel(cols, 0) {
            @Override public Class<?> getColumnClass(int i) { return i == 0 ? Boolean.class : String.class; }
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable resultTable = new JTable(resultModel);
        Theme.styleTable(resultTable); // 스타일 적용
        
        searchDialog.add(new JScrollPane(resultTable), BorderLayout.CENTER);

        JButton closeBtn = new JButton("닫기");
        Theme.styleButton(closeBtn);
        closeBtn.addActionListener(e -> searchDialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setBackground(Color.WHITE);
        bottom.add(closeBtn);
        searchDialog.add(bottom, BorderLayout.SOUTH);

        execBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.length() < 2 || keyword.contains(" ")) {
                JOptionPane.showMessageDialog(searchDialog, "키워드는 2글자 이상이며 공백을 포함할 수 없습니다.");
                return;
            }
            resultModel.setRowCount(0);
            for (Task t : model.getAll()) {
                if ((t.getTitle() != null && t.getTitle().contains(keyword)) ||
                    (t.getStartDate() != null && t.getStartDate().contains(keyword)) ||
                    (t.getEndDate() != null && t.getEndDate().contains(keyword))) {
                    resultModel.addRow(new Object[]{
                            t.isCompleted(), t.getTitle(), t.getStartDate(), t.getEndDate()
                    });
                }
            }
            if (resultModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(searchDialog, "검색 결과가 없습니다.");
            }
        });
        searchDialog.setVisible(true);
    }

    // 내부 클래스 TaskTableModel, TableRowReorderTransferHandler는 기존과 동일하게 유지 (생략하지 않고 포함)
    public static class TaskTableModel extends AbstractTableModel {
        private final String[] columns = {"완료", "일정 제목", "시작일", "종료일"};
        private final List<Task> rows = new ArrayList<>();

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int c) { return columns[c]; }
        @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
        @Override public boolean isCellEditable(int r, int c) { return c == 0; }

        @Override
        public Object getValueAt(int r, int c) {
            Task t = rows.get(r);
            switch (c) {
                case 0: return t.isCompleted();
                case 1: return t.getTitle();
                case 2: return t.getStartDate();
                case 3: return t.getEndDate();
            }
            return null;
        }

        @Override
        public void setValueAt(Object v, int r, int c) {
            Task t = rows.get(r);
            if (c == 0 && v instanceof Boolean) {
                t.setCompleted((Boolean) v);
                fireTableCellUpdated(r, c);
            }
        }

        public void addTask(Task t) {
            rows.add(t);
            int idx = rows.size() - 1;
            fireTableRowsInserted(idx, idx);
        }
        public void updateTask(int row, Task updated) {
            rows.set(row, updated);
            fireTableRowsUpdated(row, row);
        }
        public void removeAt(int row) {
            rows.remove(row);
            fireTableRowsDeleted(row, row);
        }
        public Task getTaskAt(int row) { return rows.get(row); }
        public List<Task> getAll() { return rows; }

        public void moveRow(int fromIndex, int toIndex) {
            if (fromIndex == toIndex) return;
            Task t = rows.remove(fromIndex);
            rows.add(toIndex, t);
            int a = Math.min(fromIndex, toIndex);
            int b = Math.max(fromIndex, toIndex);
            fireTableRowsUpdated(a, b);
        }
    }

    static class TableRowReorderTransferHandler extends TransferHandler {
        private final JTable table;
        private final TaskTableModel model;

        TableRowReorderTransferHandler(JTable table, TaskTableModel model) {
            this.table = table;
            this.model = model;
        }
        @Override
        protected Transferable createTransferable(JComponent c) {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) return null;
            int modelRow = table.convertRowIndexToModel(viewRow);
            return new StringSelection(String.valueOf(modelRow));
        }
        @Override
        public int getSourceActions(JComponent c) { return MOVE; }
        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDrop() && support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }
        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) return false;
            JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
            int viewDropRow = dl.getRow();
            int modelDropIndex = (viewDropRow < 0) ? model.getRowCount() : table.convertRowIndexToModel(viewDropRow);
            try {
                String str = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                int modelDragIndex = Integer.parseInt(str);
                if (modelDragIndex == modelDropIndex) return false;
                model.moveRow(modelDragIndex, modelDropIndex);
                int newViewIndex = table.convertRowIndexToView(modelDropIndex);
                table.getSelectionModel().setSelectionInterval(newViewIndex, newViewIndex);
                return true;
            } catch (UnsupportedFlavorException | IOException ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }
}