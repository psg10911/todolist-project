package Todo;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TaskPanel extends JPanel {

    private JLabel selectedDateLabel;
    private JPanel taskListPanel;
    private LocalDate currentDate;
    private ArrayList<Task> allTasks = new ArrayList<>();
    private ArrayList<TaskCard> taskCards = new ArrayList<>();
    private JComboBox<String> sortComboBox;

    public TaskPanel() {
        setLayout(new BorderLayout(5, 10));
        setPreferredSize(new Dimension(400, 0));

        // 상단 (날짜 + 정렬 + 검색)
        JPanel topPanel = new JPanel(new BorderLayout());
        selectedDateLabel = new JLabel(" ", SwingConstants.CENTER);
        selectedDateLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        topPanel.add(selectedDateLabel, BorderLayout.CENTER);

        JPanel sortSearchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        String[] sortOptions = { "필터", "중요도순", "완료된순" };
        sortComboBox = new JComboBox<>(sortOptions);
        JButton searchBtn = new JButton("🔍 검색");
        sortSearchPanel.add(sortComboBox);
        sortSearchPanel.add(searchBtn);
        topPanel.add(sortSearchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 중앙 리스트
        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        taskListPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // 하단 버튼
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton addTaskBtn = new JButton("할 일 추가");
        JButton delTaskBtn = new JButton("전체 삭제");
        bottomPanel.add(addTaskBtn);
        bottomPanel.add(delTaskBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // 더미
        registerDummyTasks();

        // 이벤트
        addTaskBtn.addActionListener(e -> {
            TaskDialog dialog = new TaskDialog((JFrame) SwingUtilities.getWindowAncestor(this), currentDate);
            dialog.setVisible(true);
            if (dialog.getTask() != null) {
                allTasks.add(dialog.getTask());
                refreshTaskList();
            }
        });

        delTaskBtn.addActionListener(e -> {
            if (allTasks.isEmpty()) {
                JOptionPane.showMessageDialog(this, "삭제할 일정이 없습니다.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "모든 할 일을 삭제하시겠습니까?", "삭제 확인",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                allTasks.clear();
                refreshTaskList();
            }
        });

        sortComboBox.addActionListener(e -> refreshTaskList());
        searchBtn.addActionListener(e -> openSearchDialog());
    }

    // 검색
    private void openSearchDialog() {
        JDialog searchDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "할 일 검색", true);
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

        DefaultListModel<String> resultModel = new DefaultListModel<>();
        JList<String> resultList = new JList<>(resultModel);
        JScrollPane resultScroll = new JScrollPane(resultList);
        searchDialog.add(resultScroll, BorderLayout.CENTER);

        JButton closeBtn = new JButton("닫기");
        closeBtn.addActionListener(e -> searchDialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(closeBtn);
        searchDialog.add(bottom, BorderLayout.SOUTH);

        execBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            resultModel.clear();

            if (keyword.length() < 2 || keyword.contains(" ")) {
                JOptionPane.showMessageDialog(searchDialog, "키워드는 2글자 이상이며 공백을 포함할 수 없습니다.");
                return;
            }

            allTasks.stream()
                    .filter(t -> t.getTitle().contains(keyword)
                            || t.getStartDate().contains(keyword)
                            || t.getEndDate().contains(keyword))
                    .forEach(t -> resultModel.addElement("[" + t.getPriority() + "] " + t.getTitle() + " ("
                            + t.getStartDate() + " ~ " + t.getEndDate() + ")"));

            if (resultModel.isEmpty())
                JOptionPane.showMessageDialog(searchDialog, "검색 결과가 없습니다.");
        });

        searchDialog.setVisible(true);
    }

    // 날짜 변경
    public void loadTasksForDate(LocalDate date) {
        this.currentDate = date;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일(E)");
        selectedDateLabel.setText(date.format(fmt));
        refreshTaskList();
    }

    // 갱신
    private void refreshTaskList() {
        taskListPanel.removeAll();
        taskCards.clear();

        // 1) 필터링: currentDate가 start~end 사이인 일정만(또는 전체)
        ArrayList<Task> filtered = new ArrayList<>();
        for (Task t : allTasks) {
            if (currentDate == null) {
                filtered.add(t);
                continue;
            }
            LocalDate start = LocalDate.parse(t.getStartDate());
            LocalDate end = LocalDate.parse(t.getEndDate());
            if ((currentDate.isEqual(start) || currentDate.isAfter(start))
                    && (currentDate.isEqual(end) || currentDate.isBefore(end))) {
                filtered.add(t);
            }
        }

        // 2) 정렬
        String sortOption = (String) sortComboBox.getSelectedItem();
        switch (sortOption) {
            case "중요도순" -> filtered.sort(Comparator.comparing(Task::getPriority).reversed());
            case "완료된순" -> filtered.sort(Comparator.comparing(Task::isCompleted).reversed());
        }

        // 3) 카드 추가
        for (Task t : filtered) {
            TaskCard card = new TaskCard(t, t.getPriority());
            taskCards.add(card);
            taskListPanel.add(Box.createVerticalStrut(8));
            taskListPanel.add(card);
        }

        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    // 더미
    private void registerDummyTasks() {
        allTasks.add(new Task("자바 Swing 스터디", "스터디 내용", "2025-11-12", "2025-11-12", 1));
        allTasks.add(new Task("프로젝트 디자인 구상", "회의", "2025-11-12", "2025-11-12", 2));
        allTasks.add(new Task("하이 하이 테스트", "테스트 일정", "2025-11-12", "2025-11-13", 3));
        allTasks.add(new Task("하이 분석", "분석 작업", "2025-11-12", "2025-11-12", 1));
    }

    // 카드
    private class TaskCard extends JPanel {
        private final Task task;
        private final JCheckBox checkBox;

        public TaskCard(Task task, int priority) {
            this.task = task;

            setLayout(new BorderLayout(10, 0));
            setPreferredSize(new Dimension(360, 58));
            setMaximumSize(new Dimension(360, 58));
            setBorder(new CompoundBorder(
                    new LineBorder(new Color(180, 180, 180), 1, true),
                    new EmptyBorder(5, 10, 5, 10)));

            switch (priority) {
                case 1 -> setBackground(new Color(204, 226, 203)); // 초록
                case 2 -> setBackground(new Color(255, 204, 182)); // 주황
                case 3 -> setBackground(new Color(243, 176, 195)); // 빨강
            }

            JLabel priorityLabel = new JLabel(String.valueOf(priority));
            priorityLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
            priorityLabel.setHorizontalAlignment(SwingConstants.CENTER);
            priorityLabel.setPreferredSize(new Dimension(36, 50));

            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setOpaque(false);

            JLabel titleLabel = new JLabel(task.getTitle());
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

            JLabel periodLabel = new JLabel(task.getStartDate() + " ~ " + task.getEndDate());
            periodLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            periodLabel.setForeground(Color.DARK_GRAY);

            centerPanel.add(Box.createVerticalGlue());
            centerPanel.add(titleLabel);
            centerPanel.add(Box.createVerticalStrut(2));
            centerPanel.add(periodLabel);
            centerPanel.add(Box.createVerticalGlue());

            checkBox = new JCheckBox();
            checkBox.setPreferredSize(new Dimension(42, 42));
            checkBox.setOpaque(false);
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            checkBox.setSelected(task.isCompleted());
            checkBox.addActionListener(e -> task.setCompleted(checkBox.isSelected()));

            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setOpaque(false);
            rightPanel.add(checkBox, BorderLayout.CENTER);

            // 🔽 위/아래 이동 버튼 추가
            JPanel movePanel = new JPanel(new GridLayout(2, 1, 0, 2));
            movePanel.setOpaque(false);
            JButton upBtn = new JButton("▲");
            JButton downBtn = new JButton("▼");
            upBtn.setMargin(new Insets(0, 2, 0, 2));
            downBtn.setMargin(new Insets(0, 2, 0, 2));
            movePanel.add(upBtn);
            movePanel.add(downBtn);
            rightPanel.add(movePanel, BorderLayout.EAST);

            add(priorityLabel, BorderLayout.WEST);
            add(centerPanel, BorderLayout.CENTER);
            add(rightPanel, BorderLayout.EAST);
            // [추가] 카드 더블클릭 시 TaskDialog 열기
            // TaskCard 생성자 내부의 더블클릭 리스너
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(TaskPanel.this);
                        TaskDialog dialog = new TaskDialog(owner, LocalDate.parse(task.getStartDate()));

                        // ✅ 기존 값 채우기
                        dialog.fillFromTask(task);

                        dialog.setVisible(true);

                        Task updated = dialog.getTask();
                        if (updated != null) {
                            // ✅ 기존 Task 값 갱신 (완료여부는 다이얼로그에서 안 다루므로 유지)
                            task.setTitle(updated.getTitle());
                            task.setContent(updated.getContent());
                            task.setStartDate(updated.getStartDate());
                            task.setEndDate(updated.getEndDate());
                            task.setPriority(updated.getPriority());

                            TaskPanel.this.refreshTaskList();
                        }
                    }
                }
            });
            // [추가] ▲▼ 버튼 클릭 시 순서 변경
            upBtn.addActionListener(e -> moveTaskUp(task));
            downBtn.addActionListener(e -> moveTaskDown(task));

        }

        public Task getTask() {
            return task;
        }
    }

    // 미완료 개수
    public int getIncompleteTaskCount() {
        int count = 0;
        for (Task t : allTasks) {
            if (!t.isCompleted())
                count++;
        }
        return count;
    }

    private void taskPanelRefresh() {
        refreshTaskList();
    }

    // [추가] Task 순서를 위로 이동
    private void moveTaskUp(Task task) {
        int index = allTasks.indexOf(task);
        if (index > 0) {
            allTasks.remove(index);
            allTasks.add(index - 1, task);
            refreshTaskList();
        }
    }

    // [추가] Task 순서를 아래로 이동
    private void moveTaskDown(Task task) {
        int index = allTasks.indexOf(task);
        if (index >= 0 && index < allTasks.size() - 1) {
            allTasks.remove(index);
            allTasks.add(index + 1, task);
            refreshTaskList();
        }
    }

}
