package Todo;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DailyScheduleDialog extends JDialog {

    private static final DateTimeFormatter FULL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private String currentUserId;
    private List<Task> todayTasks;
    
    private DefaultListModel<String> taskListModel; 
    private DefaultListModel<String> freeTimeListModel;
    
    // ★ [변경 1] 분리된 ClockPanel 사용
    private ClockPanel clockPanel; 
    // ★ [변경 2] 로직 처리를 위한 Controller 사용
    private TodoController todoController;

    public DailyScheduleDialog(Window parent, String userId) {
        super(parent, "오늘의 일정 상세", ModalityType.APPLICATION_MODAL);
        this.currentUserId = userId;
        this.todayTasks = new ArrayList<>();
        this.todoController = new TodoController(); // 컨트롤러 초기화

        setSize(1000, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.CARD_BG); 

        taskListModel = new DefaultListModel<>();
        freeTimeListModel = new DefaultListModel<>();
        
        // --- UI 구성 ---
        JLabel titleLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일의 하루")), SwingConstants.CENTER);
        titleLabel.setFont(Theme.FONT_BOLD_24);
        titleLabel.setForeground(Theme.TEXT_MAIN);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0)); 
        centerPanel.setBackground(Theme.CARD_BG);
        centerPanel.setBorder(new EmptyBorder(0, 20, 10, 20));

        // ★ [변경 3] 분리된 ClockPanel 생성
        clockPanel = new ClockPanel();
        centerPanel.add(clockPanel);

        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setResizeWeight(0.5);
        rightSplitPane.setBorder(null);
        rightSplitPane.setDividerSize(8); 
        rightSplitPane.setBackground(Theme.BACKGROUND);

        Border outerBorder = BorderFactory.createLineBorder(Theme.BORDER);
        
        JPanel taskListPanel = new JPanel(new BorderLayout());
        taskListPanel.setBackground(Theme.CARD_BG);
        
        TitledBorder taskTitle = BorderFactory.createTitledBorder(outerBorder, " 📅 오늘의 할 일 (더블클릭 수정) ");
        taskTitle.setTitleFont(Theme.FONT_BOLD_16);
        taskTitle.setTitleColor(Theme.PRIMARY);
        taskListPanel.setBorder(taskTitle);
        
        JList<String> taskList = new JList<>(taskListModel);
        taskList.setFont(Theme.FONT_REGULAR_14);
        taskList.setFixedCellHeight(35); 
        taskList.setSelectionBackground(new Color(230, 240, 255));
        taskList.setSelectionForeground(Theme.TEXT_MAIN);
        
        taskList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBorder(new EmptyBorder(0, 10, 0, 0));
                if (value.toString().startsWith("●")) {
                    l.setForeground(Theme.TEXT_MAIN);
                }
                return l;
            }
        });

        taskList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = taskList.locationToIndex(e.getPoint());
                    if (index >= 0 && index < todayTasks.size()) {
                        openEditDialog(todayTasks.get(index));
                    }
                }
            }
        });
        taskListPanel.add(new JScrollPane(taskList), BorderLayout.CENTER);

        JPanel freeListPanel = new JPanel(new BorderLayout());
        freeListPanel.setBackground(Theme.CARD_BG);
        
        TitledBorder freeTitle = BorderFactory.createTitledBorder(outerBorder, " 🌿 쉴 수 있는 빈 시간 ");
        freeTitle.setTitleFont(Theme.FONT_BOLD_16);
        freeTitle.setTitleColor(new Color(39, 174, 96)); 
        freeListPanel.setBorder(freeTitle);
        
        JList<String> freeTimeList = new JList<>(freeTimeListModel);
        freeTimeList.setFont(Theme.FONT_REGULAR_14);
        freeTimeList.setFixedCellHeight(35);
        freeTimeList.setSelectionBackground(new Color(235, 250, 235));
        
        freeTimeList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBorder(new EmptyBorder(0, 10, 0, 0));
                if (value.toString().contains("[현재]")) {
                    l.setForeground(new Color(39, 174, 96));
                    l.setFont(Theme.FONT_BOLD_16);
                } else {
                    l.setForeground(Theme.TEXT_MAIN);
                }
                return l;
            }
        });
        freeListPanel.add(new JScrollPane(freeTimeList), BorderLayout.CENTER);

        rightSplitPane.setTopComponent(taskListPanel);
        rightSplitPane.setBottomComponent(freeListPanel);

        centerPanel.add(rightSplitPane);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Theme.CARD_BG);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        JButton closeBtn = new JButton("닫기");
        Theme.styleButton(closeBtn); 
        closeBtn.setPreferredSize(new Dimension(100, 40));
        closeBtn.addActionListener(e -> dispose());
        
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // 초기 데이터 로드
        refreshData();
    }

    private void openEditDialog(Task originalTask) {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        TaskDialog dialog = new TaskDialog(parentFrame, originalTask);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        Task updatedTask = dialog.getTask();
        if (updatedTask != null) {
            // Controller 사용
            updatedTask.setId(originalTask.getId());
            updatedTask.setUserId(originalTask.getUserId());
            todoController.updateTask(updatedTask); 
            
            refreshData();
        }
    }
    
    // ★ [변경 4] 데이터 갱신 로직 통합
    private void refreshData() {
        loadTodayTasks();
        updateFreeTimeList();
        
        // ★ [변경 5] 계산은 Controller가 했고, 결과만 시계 패널에 전달
        List<int[]> freeSlots = todoController.calculateFreeIntervals(todayTasks);
        clockPanel.setTasks(todayTasks, freeSlots);
    }

    private void loadTodayTasks() {
        LocalDate today = LocalDate.now();
        // Controller를 통해 데이터 가져옴
        List<Task> allTasks = todoController.getTasksByDate(currentUserId, today);
        taskListModel.clear();
        todayTasks.clear();

        // 오늘 날짜의 시간 단위 일정만 필터링 (PeriodTask 등은 시간표에 표시 애매함으로 제외하거나 정책 결정)
        // 여기서는 간단히 기존 로직(시간 파싱) 유지
        for (Task t : allTasks) {
            try {
                String startStr = t.getStartDate();
                String endStr = t.getEndDate();
                if (startStr.length() > 16) startStr = startStr.substring(0, 16);
                if (endStr.length() > 16) endStr = endStr.substring(0, 16);

                LocalDateTime startDT = LocalDateTime.parse(startStr, FULL_FMT);
                LocalDateTime endDT = LocalDateTime.parse(endStr, FULL_FMT);

                if (!startDT.toLocalDate().equals(endDT.toLocalDate())) continue;
                if (startDT.toLocalDate().equals(today)) todayTasks.add(t);
            } catch (Exception e) { }
        }
        
        Collections.sort(todayTasks, Comparator.comparing(Task::getStartDate));

        if (todayTasks.isEmpty()) {
            taskListModel.addElement("오늘 예정된 일정이 없습니다.");
        } else {
            for (Task t : todayTasks) {
                // 다형성 메서드 사용
                String timeStr = t.getScheduleString();
                String status = t.isCompleted() ? "(완료)" : "";
                taskListModel.addElement(String.format("●  [%s]  %s  %s", timeStr, t.getTitle(), status));
            }
        }
    }

    private void updateFreeTimeList() {
        freeTimeListModel.clear();
        // ★ [변경 6] 복잡한 계산 로직은 Controller에게 위임!
        List<int[]> freeSlots = todoController.calculateFreeIntervals(todayTasks);
        
        if (freeSlots.isEmpty()) {
            freeTimeListModel.addElement("오늘은 꽉 찬 하루네요! 화이팅 🔥");
            return;
        }

        int currentMin = LocalTime.now().getHour() * 60 + LocalTime.now().getMinute();
        for (int[] slot : freeSlots) {
            int start = slot[0];
            int end = slot[1];
            int duration = end - start;
            String sTime = String.format("%02d:%02d", start / 60, start % 60);
            String eTime = String.format("%02d:%02d", end / 60, end % 60);
            
            String durationStr = "";
            if (duration >= 60) durationStr += (duration / 60) + "시간 ";
            if (duration % 60 > 0) durationStr += (duration % 60) + "분";
            
            String itemText = String.format("%s ~ %s   (%s)", sTime, eTime, durationStr);
            if (currentMin >= start && currentMin < end) itemText = "▶ " + itemText + "  [현재]";
            freeTimeListModel.addElement(itemText);
        }
    }
    
    // (calculateFreeIntervals 메서드는 Controller로 이동했으므로 삭제됨)
    // (ClockPanel 내부 클래스도 삭제됨)
}