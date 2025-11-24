package Todo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class MainPanel extends JPanel {

    private CalendarPanel calendarPanel;
    private TaskPanel taskPanel;
    private FriendListPanel friendListPanel;
    private String currentUserId;

    public MainPanel() {
        setLayout(new BorderLayout(15, 15)); 
        setBackground(Theme.BACKGROUND); 
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); 

        taskPanel = new TaskPanel();
        calendarPanel = new CalendarPanel(taskPanel);
        friendListPanel = new FriendListPanel(new FriendService(), null);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Theme.BACKGROUND); 

        // ★ 버튼 3개 생성
        JButton todayScheduleBtn = new JButton("오늘의 일정"); 
        JButton weeklyTimeTableBtn = new JButton("주간 시간표"); 
        JButton friendBtn = new JButton("친구 목록");
        
        Theme.styleButton(todayScheduleBtn);
        Theme.styleButton(weeklyTimeTableBtn);
        Theme.styleButton(friendBtn);

        bottomPanel.add(todayScheduleBtn);
        bottomPanel.add(weeklyTimeTableBtn);
        bottomPanel.add(friendBtn);

        add(calendarPanel, BorderLayout.CENTER);
        add(taskPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // 친구 목록
        friendBtn.addActionListener(e -> {
            JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "친구 목록", true);
            dialog.setSize(650, 500);
            dialog.setLocationRelativeTo(this);
            if (currentUserId != null) friendListPanel.setUser(currentUserId);
            dialog.add(friendListPanel);
            dialog.setVisible(true);
        });

        // 오늘의 일정 (원형 시계)
        todayScheduleBtn.addActionListener(e -> {
            if (currentUserId == null) return;
            DailyScheduleDialog dialog = new DailyScheduleDialog(SwingUtilities.getWindowAncestor(this), currentUserId);
            dialog.setVisible(true);
        });

        // 주간 시간표 (격자 테이블)
        weeklyTimeTableBtn.addActionListener(e -> {
            if (currentUserId == null) return;
            WeeklyTimeTableDialog dialog = new WeeklyTimeTableDialog(SwingUtilities.getWindowAncestor(this), currentUserId);
            dialog.setVisible(true);
        });
    }
    
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    public TaskPanel getTaskPanel() {
        return taskPanel;
    }

    public FriendListPanel getFriendListPanel() {
        return friendListPanel;
    }
}