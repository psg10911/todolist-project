package Todo;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class WeeklyTimeTableDialog extends JDialog {

    private String currentUserId;
    private LocalDate currentMonday;
    
    // ★ [변경] 분리된 TimeTablePanel 사용
    private TimeTablePanel timeTablePanel;
    private JLabel weekLabel;
    
    // ★ [추가] 로직 처리는 Controller에게
    private TodoController todoController;

    public WeeklyTimeTableDialog(Window parent, String userId) {
        super(parent, "주간 시간표", ModalityType.APPLICATION_MODAL);
        this.currentUserId = userId;
        this.currentMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        this.todoController = new TodoController();

        setSize(1100, 800);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.CARD_BG);

        // 1. 상단 네비게이션 패널
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        topPanel.setBackground(Theme.CARD_BG);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        JButton prevBtn = new JButton("◀ 이전 주");
        JButton nextBtn = new JButton("다음 주 ▶");
        Theme.styleButton(prevBtn);
        Theme.styleButton(nextBtn);
        
        JButton todayBtn = new JButton("이번 주");
        Theme.styleButton(todayBtn); 
        todayBtn.setBackground(Theme.SECONDARY); 

        weekLabel = new JLabel("", SwingConstants.CENTER);
        weekLabel.setFont(Theme.FONT_BOLD_24);
        weekLabel.setForeground(Theme.TEXT_MAIN);
        
        prevBtn.addActionListener(e -> { currentMonday = currentMonday.minusWeeks(1); updateView(); });
        nextBtn.addActionListener(e -> { currentMonday = currentMonday.plusWeeks(1); updateView(); });
        todayBtn.addActionListener(e -> { currentMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)); updateView(); });

        topPanel.add(prevBtn); topPanel.add(todayBtn); topPanel.add(weekLabel); topPanel.add(nextBtn);
        add(topPanel, BorderLayout.NORTH);

        // 2. 중앙 시간표 패널 (스크롤)
        // ★ [변경] 분리된 패널 생성
        timeTablePanel = new TimeTablePanel();
        
        // ★ [추가] 패널에서 Task가 더블클릭되면 -> 수정창을 열도록 연결 (Callback)
        timeTablePanel.setOnTaskDoubleClicked(task -> openEditDialog(task));

        JScrollPane scrollPane = new JScrollPane(timeTablePanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.CARD_BG);
        add(scrollPane, BorderLayout.CENTER);

        // 3. 하단 닫기 버튼
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Theme.CARD_BG);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton closeBtn = new JButton("닫기");
        Theme.styleButton(closeBtn);
        closeBtn.setPreferredSize(new Dimension(100, 40));
        closeBtn.addActionListener(e -> dispose());
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        updateView();
        SwingUtilities.invokeLater(() -> scrollPane.getViewport().setViewPosition(new Point(0, 9 * 50)));
    }

    private void updateView() {
        LocalDate sunday = currentMonday.plusDays(6);
        weekLabel.setText(String.format("%s ~ %s", 
            currentMonday.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")), 
            sunday.format(DateTimeFormatter.ofPattern("MM.dd"))));
        
        // ★ [변경] Controller 사용
        List<Task> tasks = TodoDao.findByRange(currentUserId, currentMonday.atStartOfDay(), sunday.atTime(LocalTime.MAX));
        
        // 패널에게 데이터 전달
        timeTablePanel.setData(currentMonday, tasks);
    }

    private void openEditDialog(Task originalTask) {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        TaskDialog dialog = new TaskDialog(parentFrame, originalTask);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        Task updatedTask = dialog.getTask();
        if (updatedTask != null) {
            updatedTask.setId(originalTask.getId());
            updatedTask.setUserId(originalTask.getUserId());
            
            // ★ [변경] Controller 사용
            todoController.updateTask(updatedTask);
            updateView(); // 화면 갱신
        }
    }
    
    // (내부 클래스 TimeTablePanel은 삭제됨 -> 별도 파일로 이동)
}