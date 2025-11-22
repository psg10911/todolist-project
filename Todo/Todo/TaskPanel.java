package Todo;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
        setPreferredSize(new Dimension(450, 0)); 

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Theme.BACKGROUND);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        selectedDateLabel = new JLabel(" ", SwingConstants.LEFT);
        selectedDateLabel.setFont(Theme.FONT_BOLD_24);
        selectedDateLabel.setForeground(Theme.TEXT_MAIN);
        topPanel.add(selectedDateLabel, BorderLayout.CENTER);

        JPanel sortSearchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        sortSearchPanel.setBackground(Theme.BACKGROUND);

        JComboBox<String> sortComboBox = new JComboBox<>(new String[]{
                "최신순", "제목순", "완료된순", "중요도순"
        });
        sortComboBox.setFont(Theme.FONT_REGULAR_12);
        sortComboBox.setBackground(Color.WHITE);

        JButton searchBtn = new JButton("검색");
        Theme.styleButton(searchBtn);
        searchBtn.setPreferredSize(new Dimension(80, 30));

        sortSearchPanel.add(sortComboBox);
        sortSearchPanel.add(searchBtn);
        topPanel.add(sortSearchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        model = new TaskTableModel();
        table = new JTable(model);
        Theme.styleTable(table);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new TableRowReorderTransferHandler(table, model));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setBackground(Theme.BACKGROUND);

        JButton addBtn = new JButton("추가");
        JButton editBtn = new JButton("수정");
        JButton delBtn = new JButton("삭제");

        Theme.styleButton(addBtn);
        Theme.styleButton(editBtn);
        Theme.styleDangerButton(delBtn);

        bottom.add(addBtn);
        bottom.add(editBtn);
        bottom.add(delBtn);
        add(bottom, BorderLayout.SOUTH);

        sortComboBox.addActionListener(e -> {
            String sel = (String) sortComboBox.getSelectedItem();
            sorter.setSortKeys(null);

            // if ("최신순".equals(sel)) {
            //     sorter.setComparator(2, (a, b) -> nullSafeStringCompare((String) b, (String) a));
            //     sorter.toggleSortOrder(2);

            // } else 
            if ("최신순".equals(sel)) {
                    sortByLatestIdDesc();
                    return; // 아래 기본 sorter 로직은 건너뜀
                }

                // ▼ 나머지(제목순/완료된순)는 기존 sorter 사용
                sorter.setSortKeys(null); // 초기화

            if ("제목순".equals(sel)) {
                sorter.toggleSortOrder(1);

            } else if ("완료된순".equals(sel)) {
                sorter.setComparator(0, (a, b) -> Boolean.compare((Boolean) b, (Boolean) a));
                sorter.toggleSortOrder(0);

            } else if ("중요도순".equals(sel)) {
                sorter.setComparator(4, (a, b) -> {
                    int pa = priorityTextToInt((String) a);
                    int pb = priorityTextToInt((String) b);
                    return Integer.compare(pa, pb);
                });
                sorter.toggleSortOrder(4);
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

    private void sortByLatestIdDesc() {
    // id가 0(아직 DB 미삽입)인 경우를 가장 뒤로 보내도록 정렬
    model.getAll().sort((t1, t2) -> {
        int a = t1.getId();
        int b = t2.getId();
        // 0은 가장 오래된 것으로 간주
        if (a == 0 && b == 0) return 0;
        if (a == 0) return 1;
        if (b == 0) return -1;
        return Integer.compare(b, a); // 큰 id 먼저
    });
    model.fireTableDataChanged();
}

    // private static int nullSafeStringCompare(String a, String b) {
    //     if (a == null && b == null) return 0;
    //     if (a == null) return 1;
    //     if (b == null) return -1;
    //     return a.compareTo(b);
    // }

    private int priorityTextToInt(String s) {
        if ("높음".equals(s)) return 1;
        if ("보통".equals(s)) return 2;
        if ("낮음".equals(s)) return 3;
        return 2;
    }

    public void setCurrentUserId(String userId) { this.currentUserId = userId; }

    public void initAfterLogin(String userId) {
        setCurrentUserId(userId);
        loadTasksForDate(LocalDate.now());
    }

    public void loadTasksForDate(LocalDate date) {
        this.currentDate = date;
        selectedDateLabel.setText(date.format(DateTimeFormatter.ofPattern("MM월 dd일 (E)")));
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
        Theme.styleTextField(searchField);
        JButton execBtn = new JButton("검색");
        Theme.styleButton(execBtn);

        searchTop.add(searchLabel);
        searchTop.add(searchField);
        searchTop.add(execBtn);
        searchDialog.add(searchTop, BorderLayout.NORTH);

        String[] cols = {"완료", "일정 제목", "시작일", "종료일", "중요도"};
        DefaultTableModel resultModel = new DefaultTableModel(cols, 0) {
            @Override public Class<?> getColumnClass(int i) { return i == 0 ? Boolean.class : String.class; }
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable resultTable = new JTable(resultModel);
        Theme.styleTable(resultTable);

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

                    String priorityText = (t.getPriority() == 1 ? "높음" :
                                           t.getPriority() == 2 ? "보통" : "낮음");

                    resultModel.addRow(new Object[]{
                            t.isCompleted(), t.getTitle(), t.getStartDate(), t.getEndDate(), priorityText
                    });
                }
            }

            if (resultModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(searchDialog, "검색 결과가 없습니다.");
            }
        });

        searchDialog.setVisible(true);
    }

    // ★ 내부 TaskTableModel은 완전히 삭제됨 (요청사항)

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
