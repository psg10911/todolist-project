package Todo;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * [리팩토링된 클래스]
 * Task 목록 UI를 총괄하는 메인 패널 (View/Controller).
 * TaskRepository(Model)와 TaskCard(View)를 중재합니다.
 */
public class TaskPanel extends JPanel {

    private JLabel selectedDateLabel;
    private JPanel taskListPanel;
    private LocalDate currentDate;
    private JComboBox<String> sortComboBox;

    // [수정] 데이터 관리를 Repository에 위임
    private TaskRepository repository;

    public TaskPanel() {
        this.repository = new TaskRepository(); // Repository 생성

        setLayout(new BorderLayout(5, 10));
        setPreferredSize(new Dimension(400, 0));

        // 1. 상단 (날짜 + 정렬 + 검색)
        JPanel topPanel = new JPanel(new BorderLayout());
        selectedDateLabel = new JLabel(" ", SwingConstants.CENTER);
        selectedDateLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        topPanel.add(selectedDateLabel, BorderLayout.CENTER);

        JPanel sortSearchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        String[] sortOptions = { "필터", "중요도순", "완료된순" }; // "필터"가 기본값
        sortComboBox = new JComboBox<>(sortOptions);
        JButton searchBtn = new JButton("🔍 검색");
        sortSearchPanel.add(sortComboBox);
        sortSearchPanel.add(searchBtn);
        topPanel.add(sortSearchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2. 중앙 리스트
        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        taskListPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // 3. 하단 버튼
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton addTaskBtn = new JButton("할 일 추가");
        JButton delTaskBtn = new JButton("전체 삭제");
        bottomPanel.add(addTaskBtn);
        bottomPanel.add(delTaskBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // 4. 이벤트 리스너
        addTaskBtn.addActionListener(e -> openAddTaskDialog());
        delTaskBtn.addActionListener(e -> deleteCurrentTasks());
        searchBtn.addActionListener(e -> openSearchDialog());
        sortComboBox.addActionListener(e -> refreshTaskList());
    }

    /**
     * '할 일 추가' 버튼 로직 (TaskDialog 호출)
     */
    private void openAddTaskDialog() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        TaskDialog dialog = new TaskDialog(owner, currentDate, false);
        dialog.setVisible(true);

        Task newTask = dialog.getTask();
        if (newTask != null && !"__DELETE__".equals(newTask.getTitle())) {
            repository.addTask(newTask); // Repository에 추가
            refreshTaskList();
        }
    }

    /**
     * '전체 삭제' 버튼 로직
     */
    private void deleteCurrentTasks() {
        // (Repository에 데이터가 있는지 확인하는 것이 더 좋음)
        int confirm = JOptionPane.showConfirmDialog(this,
                "현재 날짜(" + (currentDate != null ? currentDate.toString() : "전체") + ")의 할 일을 모두 삭제하시겠습니까?",
                "삭제 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            repository.deleteAllTasks(currentDate); // Repository에서 삭제
            refreshTaskList();
        }
    }

    /**
     * '검색' 버튼 로직 (SearchDialog 호출)
     */
    private void openSearchDialog() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        // [수정] SearchDialog 생성. 'refreshCallback'으로 refreshTaskList() 전달
        SearchDialog dialog = new SearchDialog(owner, repository, () -> refreshTaskList());
        dialog.setVisible(true);
    }

    /**
     * CalendarPanel에서 호출하는 날짜 변경 메서드
     */
    public void loadTasksForDate(LocalDate date) {
        this.currentDate = date;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일(E)");
        selectedDateLabel.setText(date.format(fmt));
        refreshTaskList();
    }

    /**
     * [리팩토링] 화면 갱신 (Model -> View)
     * Repository에서 데이터를 가져와 TaskCard를 다시 그립니다.
     */
    private void refreshTaskList() {
        taskListPanel.removeAll();

        // 1. Repository에서 필터링/정렬된 데이터 가져오기
        String sortOption = (String) sortComboBox.getSelectedItem();
        ArrayList<Task> tasks = repository.getFilteredAndSortedTasks(currentDate, sortOption);

        // 2. TaskCard(View) 생성 및 이벤트 바인딩
        for (Task task : tasks) {
            TaskCard card = new TaskCard(task, task.getPriority());

            // 2-1. 더블클릭(수정) 이벤트 바인딩
            card.addEditListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        openEditDialog(task); // 수정 다이얼로그 열기
                    }
                }
            });

            // 2-2. 체크박스 이벤트 바인딩
            card.addCheckListener(e -> {
                task.setCompleted(((JCheckBox) e.getSource()).isSelected());
                // '완료된순' 정렬일 때만 즉시 새로고침 (기존 로직 유지)
                if ("완료된순".equals(sortComboBox.getSelectedItem())) {
                    refreshTaskList();
                }
            });

            // 2-3. 순서 이동 이벤트 바인딩
            card.addMoveUpListener(e -> {
                repository.moveTaskUp(task);
                refreshTaskList();
            });
            card.addMoveDownListener(e -> {
                repository.moveTaskDown(task);
                refreshTaskList();
            });

            // 3. 패널에 카드 추가
            taskListPanel.add(Box.createVerticalStrut(8));
            taskListPanel.add(card);
        }

        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    /**
     * TaskCard 더블클릭 시 '수정' 다이얼로그 열기
     * (TaskPanel의 내부 클래스 TaskCard에서 이동)
     */
    private void openEditDialog(Task task) {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(TaskPanel.this);
        TaskDialog dialog = new TaskDialog(owner, LocalDate.parse(task.getStartDate()), true);
        dialog.fillFromTask(task);
        dialog.setVisible(true);

        Task updated = dialog.getTask();
        if (updated != null) {
            if ("__DELETE__".equals(updated.getTitle())) {
                repository.deleteTask(task); // Repository에서 삭제
            } else {
                // Repository의 Task 객체 직접 수정 (기존 방식 유지)
                task.setTitle(updated.getTitle());
                task.setContent(updated.getContent());
                task.setStartDate(updated.getStartDate());
                task.setEndDate(updated.getEndDate());
                task.setPriority(updated.getPriority());
            }
            refreshTaskList(); // 변경 사항 반영
        }
    }

    /**
     * NotificationPopup이 호출하는 메서드
     * (Repository의 메서드를 대신 호출)
     */
    public int getIncompleteTaskCount() {
        return repository.getIncompleteTaskCount();
    }
}