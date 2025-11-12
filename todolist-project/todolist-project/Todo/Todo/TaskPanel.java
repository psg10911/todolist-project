package Todo;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;

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

        // 더미 데이터
        registerDummyTasks();

        // ✅ 추가 버튼 클릭 시 (삭제 버튼 없는 Dialog)
        addTaskBtn.addActionListener(e -> {
            TaskDialog dialog = new TaskDialog((JFrame) SwingUtilities.getWindowAncestor(this), currentDate, false);
            dialog.setVisible(true);
            Task newTask = dialog.getTask();
            if (newTask != null && !"__DELETE__".equals(newTask.getTitle())) {
                allTasks.add(newTask);
                refreshTaskList();
            }
        });

        // 전체 삭제
        delTaskBtn.addActionListener(e -> {
            if (allTasks.isEmpty()) {
                JOptionPane.showMessageDialog(this, "삭제할 일정이 없습니다.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "현재 날짜(" + (currentDate != null ? currentDate.toString() : "전체") + ")의 할 일을 모두 삭제하시겠습니까?",
                    "삭제 확인", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (currentDate == null) {
                    allTasks.clear();
                } else {
                    allTasks.removeIf(t -> {
                        LocalDate start = LocalDate.parse(t.getStartDate());
                        LocalDate end = LocalDate.parse(t.getEndDate());
                        return (currentDate.isEqual(start) || currentDate.isAfter(start))
                                && (currentDate.isEqual(end) || currentDate.isBefore(end));
                    });
                }
                refreshTaskList();
            }
        });

        sortComboBox.addActionListener(e -> refreshTaskList());
        searchBtn.addActionListener(e -> openSearchDialog());
    }

    // ✅ 검색
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

        DefaultListModel<Task> resultModel = new DefaultListModel<>();
        JList<Task> resultList = new JList<>(resultModel);
        resultList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Task t) {
                    setText("[" + t.getPriority() + "] " + t.getTitle() + " (" +
                            t.getStartDate() + " ~ " + t.getEndDate() + ")");
                }
                return this;
            }
        });

        JScrollPane resultScroll = new JScrollPane(resultList);
        searchDialog.add(resultScroll, BorderLayout.CENTER);

        JButton closeBtn = new JButton("닫기");
        closeBtn.addActionListener(e -> searchDialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(closeBtn);
        searchDialog.add(bottom, BorderLayout.SOUTH);

        // ✅ 검색 버튼 클릭 시
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
                    .forEach(resultModel::addElement);

            if (resultModel.isEmpty())
                JOptionPane.showMessageDialog(searchDialog, "검색 결과가 없습니다.");
        });

        // ✅ 검색 결과 더블클릭 → 수정창 열기
        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Task selected = resultList.getSelectedValue();
                    if (selected != null) {
                        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(TaskPanel.this);
                        TaskDialog dialog = new TaskDialog(owner, LocalDate.parse(selected.getStartDate()), true);
                        dialog.fillFromTask(selected);
                        dialog.setVisible(true);

                        Task updated = dialog.getTask();
                        if (updated != null) {
                            if ("__DELETE__".equals(updated.getTitle())) {
                                allTasks.remove(selected);
                            } else {
                                selected.setTitle(updated.getTitle());
                                selected.setContent(updated.getContent());
                                selected.setStartDate(updated.getStartDate());
                                selected.setEndDate(updated.getEndDate());
                                selected.setPriority(updated.getPriority());
                            }
                            refreshTaskList();
                            searchDialog.dispose(); // 닫고 갱신 반영
                        }
                    }
                }
            }
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

        String sortOption = (String) sortComboBox.getSelectedItem();
        switch (sortOption) {
            case "중요도순" -> filtered.sort(Comparator.comparing(Task::getPriority).reversed());
            case "완료된순" -> filtered.sort(Comparator.comparing(Task::isCompleted).reversed());
        }

        for (Task t : filtered) {
            TaskCard card = new TaskCard(t, t.getPriority());
            taskCards.add(card);
            taskListPanel.add(Box.createVerticalStrut(8));
            taskListPanel.add(card);
        }

        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    // 더미 데이터
    private void registerDummyTasks() {
        allTasks.add(new Task("자바 Swing 스터디", "스터디 내용", "2025-11-12", "2025-11-12", 1));
        allTasks.add(new Task("프로젝트 디자인 구상", "회의", "2025-11-12", "2025-11-12", 2));
        allTasks.add(new Task("하이 하이 테스트", "테스트 일정", "2025-11-12", "2025-11-13", 3));
        allTasks.add(new Task("하이 분석", "분석 작업", "2025-11-12", "2025-11-12", 1));
        allTasks.add(new Task("ㄴㅇㄴㅇ", "ㅁㄴㅇㅁㄴㅇ", "2025-11-14", "2025-11-14", 1));
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

            // 위/아래 이동 버튼
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

            // 더블클릭 리스너 (메인 목록용)
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(TaskPanel.this);
                        TaskDialog dialog = new TaskDialog(owner, LocalDate.parse(task.getStartDate()), true);
                        dialog.fillFromTask(task);
                        dialog.setVisible(true);

                        Task updated = dialog.getTask();
                        if (updated != null) {
                            if ("__DELETE__".equals(updated.getTitle())) {
                                allTasks.remove(task);
                            } else {
                                task.setTitle(updated.getTitle());
                                task.setContent(updated.getContent());
                                task.setStartDate(updated.getStartDate());
                                task.setEndDate(updated.getEndDate());
                                task.setPriority(updated.getPriority());
                            }
                            TaskPanel.this.refreshTaskList();
                        }
                    }
                }
            });

            upBtn.addActionListener(e -> moveTaskUp(task));
            downBtn.addActionListener(e -> moveTaskDown(task));
        }
    }

    // 미완료 개수
    public int getIncompleteTaskCount() {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Task t : allTasks) {
            LocalDate start = LocalDate.parse(t.getStartDate());
            LocalDate end = LocalDate.parse(t.getEndDate());
            if (!t.isCompleted()
                    && ((today.isEqual(start) || today.isAfter(start))
                            && (today.isEqual(end) || today.isBefore(end)))) {
                count++;
            }
        }
        return count;
    }

    // 순서 이동
    private void moveTaskUp(Task task) {
        int index = allTasks.indexOf(task);
        if (index > 0) {
            allTasks.remove(index);
            allTasks.add(index - 1, task);
            refreshTaskList();
        }
    }

    private void moveTaskDown(Task task) {
        int index = allTasks.indexOf(task);
        if (index >= 0 && index < allTasks.size() - 1) {
            allTasks.remove(index);
            allTasks.add(index + 1, task);
            refreshTaskList();
        }
    }
}
