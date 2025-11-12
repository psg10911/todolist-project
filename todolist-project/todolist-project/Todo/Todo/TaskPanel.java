package Todo;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
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

    // 🔽 필터 콤보박스 (정렬)
    private JComboBox<String> sortComboBox;

    public TaskPanel() {
        setLayout(new BorderLayout(5, 10));
        setPreferredSize(new Dimension(400, 0));

        // -------------------------------
        // 1️⃣ 상단 (날짜 + 정렬 + 검색)
        // -------------------------------
        JPanel topPanel = new JPanel(new BorderLayout());

        selectedDateLabel = new JLabel(" ", SwingConstants.CENTER);
        selectedDateLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        topPanel.add(selectedDateLabel, BorderLayout.CENTER);

        JPanel sortSearchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        String[] sortOptions = { "최신순", "중요도순", "완료된순" };
        sortComboBox = new JComboBox<>(sortOptions);
        JButton searchBtn = new JButton("🔍 검색");
        sortSearchPanel.add(sortComboBox);
        sortSearchPanel.add(searchBtn);
        topPanel.add(sortSearchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // -------------------------------
        // 2️⃣ 중앙: 일정 카드 리스트
        // -------------------------------
        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        taskListPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // -------------------------------
        // 3️⃣ 하단 버튼
        // -------------------------------
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton addTaskBtn = new JButton("할 일 추가");
        JButton delTaskBtn = new JButton("전체 삭제");
        bottomPanel.add(addTaskBtn);
        bottomPanel.add(delTaskBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // -------------------------------
        // 📦 더미 데이터 등록 (테스트용)
        // -------------------------------
        registerDummyTasks();

        // -------------------------------
        // 🔘 이벤트 처리
        // -------------------------------
        addTaskBtn.addActionListener(e -> {
            TaskDialog dialog = new TaskDialog((JFrame) SwingUtilities.getWindowAncestor(this), currentDate);
            dialog.setVisible(true);
            if (dialog.getTask() != null) {
                Task newTask = dialog.getTask();
                allTasks.add(newTask);
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

    // ---------------------------------------------
    // 🔍 검색 다이얼로그
    // ---------------------------------------------
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

    // ---------------------------------------------
    // 📅 날짜 변경 시 호출
    // ---------------------------------------------
    public void loadTasksForDate(LocalDate date) {
        this.currentDate = date;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일(E)");
        selectedDateLabel.setText(date.format(fmt));
        refreshTaskList();
    }

    // ---------------------------------------------
    // ♻️ 정렬 & 필터링 후 화면 갱신
    // ---------------------------------------------
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

            // ✅ 현재 날짜(currentDate)가 시작일~종료일 사이에 포함되는 일정만 표시
            if ((currentDate.isEqual(start) || currentDate.isAfter(start))
                    && (currentDate.isEqual(end) || currentDate.isBefore(end))) {
                filtered.add(t);
            }
        }

        // 🔽 정렬 옵션 적용
        String sortOption = (String) sortComboBox.getSelectedItem();
        switch (sortOption) {
            case "최신순" -> filtered.sort(Comparator.comparing(Task::getStartDate).reversed());
            case "중요도순" -> filtered.sort(Comparator.comparing(Task::getPriority).reversed());
            case "완료된순" -> filtered.sort(Comparator.comparing(Task::isCompleted).reversed());
        }

        // 카드 다시 추가
        for (Task t : filtered) {
            TaskCard card = new TaskCard(t, t.getPriority());
            taskCards.add(card);
            taskListPanel.add(Box.createVerticalStrut(8));
            taskListPanel.add(card);
        }

        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    // ---------------------------------------------
    // 📦 더미 일정 등록
    // ---------------------------------------------
    private void registerDummyTasks() {
        allTasks.add(new Task("자바 Swing 스터디", "스터디 내용", "2025-11-10", "2025-11-10", 1));
        allTasks.add(new Task("프로젝트 디자인 구상", "회의", "2025-11-10", "2025-11-10", 2));
        allTasks.add(new Task("하이 하이 테스트", "테스트 일정", "2025-08-10", "2025-08-12", 3));
        allTasks.add(new Task("하이 분석", "분석 작업", "2025-08-10", "2025-08-15", 1));
    }

    // ---------------------------------------------
    // ✅ TaskCard 내부 클래스
    // ---------------------------------------------
    private static class TaskCard extends JPanel {
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
            checkBox.setPreferredSize(new Dimension(42, 42)); // 🔹 크기 키움
            checkBox.setFont(new Font("SansSerif", Font.PLAIN, 32)); // 🔹 내부 체크마크 크기도 키움
            checkBox.setOpaque(false);
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            checkBox.setSelected(task.isCompleted());
            checkBox.addActionListener(e -> task.setCompleted(checkBox.isSelected()));

            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setOpaque(false);
            rightPanel.add(checkBox, BorderLayout.CENTER);

            add(priorityLabel, BorderLayout.WEST);
            add(centerPanel, BorderLayout.CENTER);
            add(rightPanel, BorderLayout.EAST);
        }

    }

    // ✅ 미완료(Task.completed == false) 할 일 개수를 반환
    public int getIncompleteTaskCount() {
        int count = 0;
        for (Task t : allTasks) {
            if (!t.isCompleted()) {
                count++;
            }
        }
        return count;
    }

}
