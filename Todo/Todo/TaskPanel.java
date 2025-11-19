package Todo;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 우측 할 일 목록 패널 (List<Task> + 커스텀 TableModel 버전)
 * - 검색/드래그
 * - DB 연동: 추가/수정/삭제/조회
 */
public class TaskPanel extends JPanel {

    private JLabel selectedDateLabel;
    private JTable table;
    private TaskTableModel model;       // 커스텀 모델(List<Task> 보관)
    private TableRowSorter<TaskTableModel> sorter; // 정렬/필터
    private LocalDate currentDate;
    private String currentUserId;       // 로그인 사용자

    public TaskPanel() {
        setLayout(new BorderLayout(5, 10));
        setPreferredSize(new Dimension(420, 0));

        // 1) 상단: 날짜 + 정렬 콤보 + 검색 버튼
        JPanel topPanel = new JPanel(new BorderLayout());
        selectedDateLabel = new JLabel(" ", SwingConstants.CENTER);
        selectedDateLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        topPanel.add(selectedDateLabel, BorderLayout.CENTER);

        JPanel sortSearchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JComboBox<String> sortComboBox = new JComboBox<>(new String[]{"최신순", "제목순", "완료된순"});
        JButton searchBtn = new JButton("검색");
        sortSearchPanel.add(sortComboBox);
        sortSearchPanel.add(searchBtn);
        topPanel.add(sortSearchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2) 중앙: JTable (커스텀 모델)
        model = new TaskTableModel();
        table = new JTable(model);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);

        // 정렬/필터 (뷰 ↔ 모델 인덱스 변환 필요)
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // 드래그로 순서 변경 가능 (커스텀 TransferHandler: 모델의 List<Task>를 재배열)
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new TableRowReorderTransferHandler(table, model));

        add(new JScrollPane(table), BorderLayout.CENTER);

        // 3) 하단: 버튼(추가/수정/삭제)
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton addBtn = new JButton("할 일 추가");
        JButton editBtn = new JButton("할 일 수정");
        JButton delBtn = new JButton("할 일 삭제");
        bottom.add(addBtn);
        bottom.add(editBtn);
        bottom.add(delBtn);
        add(bottom, BorderLayout.SOUTH);

        // ===== 버튼 리스너 =====

        // 정렬 콤보
        sortComboBox.addActionListener(e -> {
            String sel = (String) sortComboBox.getSelectedItem();
            sorter.setSortKeys(null); // 초기화
            if ("최신순".equals(sel)) {
                // 시작일 내림차순 정렬(빈 값이 뒤로 가도록)
                sorter.setComparator(2, (a, b) -> nullSafeStringCompare((String) b, (String) a));
                sorter.toggleSortOrder(2);
            } else if ("제목순".equals(sel)) {
                //sorter.setComparator(1, TaskPanel::nullSafeStringCompare);
                sorter.toggleSortOrder(1);
            } else if ("완료된순".equals(sel)) {
                sorter.setComparator(0, (a, b) -> Boolean.compare((Boolean) b, (Boolean) a)); // true 먼저
                sorter.toggleSortOrder(0);
            }
        });

        // 검색
        searchBtn.addActionListener(e -> openSearchDialog());

        // 추가
        addBtn.addActionListener(e -> {
            if (!ensureUserBound()) return;

            TaskDialog dialog = new TaskDialog((JFrame) SwingUtilities.getWindowAncestor(this), currentDate);
            dialog.setVisible(true);

            Task t = dialog.getTask();
            if (t == null) return;

            // DB INSERT
            t.setUserId(currentUserId);
            int newId = TodoDao.insert(t);
            t.setId(newId);

            // 모델에 추가 (List<Task>에 append)
            model.addTask(t);
        });

        // 수정
        editBtn.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "수정할 할 일을 선택해주세요.");
                return;
            }
            int row = table.convertRowIndexToModel(viewRow);
            Task original = model.getTaskAt(row);

            TaskDialog dialog = new TaskDialog((JFrame) SwingUtilities.getWindowAncestor(this), original); // 복사본 편집
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            Task updated = dialog.getTask();
            if (updated == null) return; // cancel

            // id/userId 유지
            updated.setId(original.getId());
            updated.setUserId(original.getUserId());

            // DB UPDATE
            TodoDao.update(updated);

            // 모델 교체
            model.updateTask(row, updated);
        });

        // 삭제
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

            // DB DELETE
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

    /** 로그인 성공 후 주입 */
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    /** 로그인 직후 초기화: userId 주입 + 오늘 일정 로드 */
    public void initAfterLogin(String userId) {
        setCurrentUserId(userId);
        loadTasksForDate(LocalDate.now());
    }

    /** 날짜 변경 시 DB에서 해당 날짜 일정 로드 */
    public void loadTasksForDate(LocalDate date) {
        this.currentDate = date;
        selectedDateLabel.setText(date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일(E)")));

        // DB SELECT → 모델 교체
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

    // ========================== 검색 모달 ==========================
    private void openSearchDialog() {
        JDialog searchDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "전체 일정 검색", true);
        searchDialog.setLayout(new BorderLayout(10, 10));
        searchDialog.setSize(460, 360);
        searchDialog.setLocationRelativeTo(this);

        JPanel searchTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JLabel searchLabel = new JLabel("키워드:");
        JTextField searchField = new JTextField(18);
        JButton execBtn = new JButton("검색");
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
        resultTable.setRowHeight(26);
        searchDialog.add(new JScrollPane(resultTable), BorderLayout.CENTER);

        JButton closeBtn = new JButton("닫기");
        closeBtn.addActionListener(e -> searchDialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(closeBtn);
        searchDialog.add(bottom, BorderLayout.SOUTH);

        // 검색 실행
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

    // ======================= 커스텀 TableModel =======================
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
                // (옵션) 즉시 DB 반영하려면 아래 주석 해제
                // try { TodoDao.update(t); } catch (Exception ignore) {}
            }
        }

        // 편의 메서드
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
        public Task getTaskAt(int row) {
            return rows.get(row);
        }
        public List<Task> getAll() {
            return rows;
        }

        /** 드래그로 순서 재배열 */
        public void moveRow(int fromIndex, int toIndex) {
            if (fromIndex == toIndex) return;
            Task t = rows.remove(fromIndex);
            rows.add(toIndex, t);
            int a = Math.min(fromIndex, toIndex);
            int b = Math.max(fromIndex, toIndex);
            fireTableRowsUpdated(a, b);
        }
    }

    // ================== 드래그-드랍 순서 변경 TransferHandler ==================
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
            // 드래그 시작 시 "모델 인덱스"를 텍스트로 태워 보냄
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
            // 드랍 지점이 뷰 인덱스이므로 모델 인덱스로 변환
            int modelDropIndex = (viewDropRow < 0) ? model.getRowCount() : table.convertRowIndexToModel(viewDropRow);

            try {
                String str = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                int modelDragIndex = Integer.parseInt(str);

                if (modelDragIndex == modelDropIndex) return false;
                // 드래그 행을 드랍 위치로 이동
                model.moveRow(modelDragIndex, modelDropIndex);

                // 드랍 후 해당 행 선택
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