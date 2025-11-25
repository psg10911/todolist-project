package Todo;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.table.DefaultTableModel;
// ★ List를 사용하기 위해 임포트
import java.util.ArrayList;

public class TaskPanel extends JPanel {

    private JLabel selectedDateLabel;
    private JTable table;
    private TaskTableModel model;
    private TableRowSorter<TaskTableModel> sorter;
    private LocalDate currentDate;
    private String currentUserId;

    private FriendService friendService;

    public TaskPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BACKGROUND);
        setPreferredSize(new Dimension(450, 0)); 

        this.friendService = new FriendService();

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Theme.BACKGROUND);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // ★ [수정됨] 상단 날짜 라벨 가운데 정렬
        selectedDateLabel = new JLabel(" ", SwingConstants.CENTER);
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

        // ★ [추가됨] 테이블 셀 내용 가운데 정렬 설정
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // 0번(체크박스)을 제외한 나머지 컬럼(1~4)에 가운데 정렬 적용
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        table.getTableHeader().setReorderingAllowed(true);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { 
                    int viewRow = table.getSelectedRow();
                    if (viewRow != -1) {
                        openEditDialog(viewRow);
                    }
                }
            }
        });

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
        JButton shareBtn = new JButton("공유"); // ★ 추가된 버튼
        JButton delBtn = new JButton("삭제");

        Theme.styleButton(addBtn);
        Theme.styleButton(editBtn);
        Theme.styleButton(shareBtn);
        Theme.styleDangerButton(delBtn);

        bottom.add(addBtn);
        bottom.add(editBtn);
        bottom.add(shareBtn);
        bottom.add(delBtn);
        add(bottom, BorderLayout.SOUTH);

        sortComboBox.addActionListener(e -> {
            String sel = (String) sortComboBox.getSelectedItem();
            sorter.setSortKeys(null);

            if ("최신순".equals(sel)) {
                    sortByLatestIdDesc();
                    return; 
                }

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
            
            TaskDialog dialog = new TaskDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), 
                currentDate, 
                currentUserId
            );
            dialog.setVisible(true);

            Task t = dialog.getTask();
            if (t == null) return;

            t.setUserId(currentUserId);
            
            // INSERT도 백그라운드 처리 권장 (간단히 스레드로 처리)
            new Thread(() -> {
                int newId = TodoDao.insert(t);
                t.setId(newId);
                SwingUtilities.invokeLater(() -> model.addTask(t));
            }).start();
        });

        editBtn.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "수정할 할 일을 선택해주세요.");
                return;
            }
            openEditDialog(viewRow); 
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
            
            // DELETE 백그라운드 처리
            new Thread(() -> {
                TodoDao.delete(t.getId(), t.getUserId());
                SwingUtilities.invokeLater(() -> model.removeAt(row));
            }).start();
        });

        shareBtn.addActionListener(e -> {
            // 1. 선택된 일정 확인
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "공유할 일정을 선택해주세요.");
                return;
            }
            
            // 2. 현재 로그인 여부 확인
            if (!ensureUserBound()) return;

            // 3. Task 객체 가져오기
            int row = table.convertRowIndexToModel(viewRow);
            Task task = model.getTaskAt(row);

            // 4. 친구 목록 불러오기 (팝업에 띄우기 위해)
            List<String> friends = friendService.getFriends(currentUserId);
            if (friends.isEmpty()) {
                JOptionPane.showMessageDialog(this, "공유할 친구가 없습니다. 먼저 친구를 추가해주세요.");
                return;
            }

            // 5. 친구 선택 팝업 띄우기
            String selectedFriend = (String) JOptionPane.showInputDialog(
                    this,
                    "'" + task.getTitle() + "' 일정을 누구에게 공유하시겠습니까?",
                    "일정 공유",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    friends.toArray(), // 친구 리스트 배열
                    friends.get(0)     // 기본 선택값
            );

            // 6. 선택 후 공유 로직 실행
            if (selectedFriend != null) {
                // FriendService에 shareTodo 메서드가 있다고 가정 (또는 DAO 직접 호출)
                // 만약 Service에 메서드가 없다면 아래 참고 코드를 Service에 추가해야 함
                boolean success = friendService.shareTodo(task.getId(), currentUserId, selectedFriend);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, selectedFriend + "님에게 일정을 공유했습니다!");
                } else {
                    JOptionPane.showMessageDialog(this, "공유에 실패했습니다. (이미 공유된 일정이거나 오류 발생)");
                }
            }
        });
    }

    private void openEditDialog(int viewRow) {
        int row = table.convertRowIndexToModel(viewRow);
        Task original = model.getTaskAt(row);

        TaskDialog dialog = new TaskDialog((JFrame) SwingUtilities.getWindowAncestor(this), original);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        Task updated = dialog.getTask();
        if (updated == null) return;

        // UPDATE 백그라운드 처리
        new Thread(() -> {
            updated.setId(original.getId());
            updated.setUserId(original.getUserId());
            TodoDao.update(updated);
            SwingUtilities.invokeLater(() -> model.updateTask(row, updated));
        }).start();
    }

    private void sortByLatestIdDesc() {
        model.getAll().sort((t1, t2) -> {
            int a = t1.getId();
            int b = t2.getId();
            if (a == 0 && b == 0) return 0;
            if (a == 0) return 1;
            if (b == 0) return -1;
            return Integer.compare(b, a); 
        });
        model.fireTableDataChanged();
    }

    private int priorityTextToInt(String s) {
        if ("높음".equals(s)) return 1;
        if ("보통".equals(s)) return 2;
        if ("낮음".equals(s)) return 3;
        return 2;
    }

    public void setCurrentUserId(String userId) { this.currentUserId = userId; }

    // 로그아웃 시 TaskPanel 초기화
    public void clear() {
        currentUserId = null;
        currentDate = null;
        selectedDateLabel.setText(" ");
        model.getAll().clear();
        model.fireTableDataChanged();
    }

    public void initAfterLogin(String userId) {
        setCurrentUserId(userId);
        loadTasksForDate(LocalDate.now());
    }

    // ★ [핵심 수정] DB 조회를 백그라운드 스레드(SwingWorker)에서 실행
    public void loadTasksForDate(LocalDate date) {
        this.currentDate = date;
        selectedDateLabel.setText(date.format(DateTimeFormatter.ofPattern("MM월 dd일 (E)")));
        
        // 로딩 중 표시 (선택 사항: 커서 변경 등)
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // SwingWorker: <결과 타입, 중간 처리 타입>
        SwingWorker<List<Task>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Task> doInBackground() throws Exception {
                // 백그라운드 스레드에서 DB 조회 수행
                if (currentUserId != null && !currentUserId.isBlank()) {
                    return TodoDao.findByDate(currentUserId, date);
                }
                return new ArrayList<>();
            }

            @Override
            protected void done() {
                // 작업 완료 후 UI 갱신 (EDT에서 실행됨)
                try {
                    List<Task> tasks = get(); // doInBackground의 리턴값 받기
                    model.getAll().clear();
                    model.getAll().addAll(tasks);
                    model.fireTableDataChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(TaskPanel.this, "데이터 로드 중 오류 발생");
                } finally {
                    setCursor(Cursor.getDefaultCursor()); // 커서 복구
                }
            }
        };
        
        worker.execute(); // 작업 시작
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
        
        // 검색 결과창 가운데 정렬 추가
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i=1; i<resultTable.getColumnCount(); i++) {
            resultTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        resultTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = resultTable.getSelectedRow();
                    if (row != -1) {
                        JOptionPane.showMessageDialog(searchDialog, "검색된 일정의 상세 내용은 메인 화면에서 수정해주세요.");
                    }
                }
            }
        });

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

            // 검색도 백그라운드 처리 (선택 사항)
            new Thread(() -> {
                // 여기서는 현재 메모리에 있는 model.getAll()을 뒤지는 것이므로 빠르지만,
                // 만약 DB 전체 검색이라면 SwingWorker를 써야 함.
                // 현재 코드는 메모리 검색이므로 그대로 둠.
                SwingUtilities.invokeLater(() -> {
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
            }).start();
        });

        searchDialog.setVisible(true);
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