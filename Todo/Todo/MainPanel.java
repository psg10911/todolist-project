package Todo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class MainPanel extends JPanel {

    private CalendarPanel calendarPanel;
    private TaskPanel taskPanel;
    private FriendListPanel friendListPanel;


    

    

    public MainPanel() {
        setLayout(new BorderLayout(15, 15)); 
        setBackground(Theme.BACKGROUND); 
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); 

        taskPanel = new TaskPanel();
        calendarPanel = new CalendarPanel(taskPanel);
        friendListPanel = new FriendListPanel(new FriendService(), null);
        // ★★★ [핵심 추가] 옵저버 패턴 연결 ★★★
        // TaskPanel에서 데이터가 변하면 -> CalendarPanel을 다시 그려라!
        taskPanel.addListener(() -> {
            calendarPanel.updateCalendar(); // 캘린더의 점/막대 갱신
        });
        // ★★★★★★★★★★★★★★★★★★★★★★★

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


        JButton logoutBtn = new JButton("로그아웃");
        Theme.styleButton(logoutBtn);
        bottomPanel.add(logoutBtn);

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this, 
                "정말 로그아웃하시겠습니까?", 
                "로그아웃 확인", 
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                ToDoListApp app = (ToDoListApp) SwingUtilities.getWindowAncestor(this);
                app.logout();
            }
        });

        add(calendarPanel, BorderLayout.CENTER);
        add(taskPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // 친구 목록
        friendBtn.addActionListener(e -> {
            JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "친구 목록", true);
            dialog.setSize(650, 500);
            dialog.setLocationRelativeTo(this);
            
            // ★ [변경] 싱글톤 ID 사용
            String userId = UserSession.getInstance().getUserId();
            if (userId != null) friendListPanel.setUser(userId);
            
            dialog.add(friendListPanel);
            dialog.setVisible(true);
        });

        // 오늘의 일정 (원형 시계)
        todayScheduleBtn.addActionListener(e -> {
            // ★ [변경] 싱글톤 ID 사용
            String userId = UserSession.getInstance().getUserId();
            if (userId == null) return;
            DailyScheduleDialog dialog = new DailyScheduleDialog(SwingUtilities.getWindowAncestor(this), userId);
            dialog.setVisible(true);
        });

        // 주간 시간표 (격자 테이블)
        weeklyTimeTableBtn.addActionListener(e -> {
            // ★ [변경] 싱글톤 ID 사용
            String userId = UserSession.getInstance().getUserId();
            if (userId == null) return;
            WeeklyTimeTableDialog dialog = new WeeklyTimeTableDialog(SwingUtilities.getWindowAncestor(this), userId);
            dialog.setVisible(true);
        });
    }

    public void initAfterLogin() {
        // 1. 오늘의 할 일 목록 로드 (오른쪽 화면)
        taskPanel.initAfterLogin();
        
        // 2. 캘린더의 점/막대 표시 갱신 (왼쪽 화면)
        //    -> 이 부분이 있어야 로그인 직후에 점이 보입니다!
        calendarPanel.updateCalendar();
    }
    
    public void clear() {
        
        // 각 패널 초기화
        taskPanel.clear();
        calendarPanel.clear();
        friendListPanel.clear();
    }

    

    public TaskPanel getTaskPanel() {
        return taskPanel;
    }

    public FriendListPanel getFriendListPanel() {
        return friendListPanel;
    }
}