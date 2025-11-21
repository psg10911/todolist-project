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
        setLayout(new BorderLayout(15, 15)); // 패널 간 간격 넓힘
        setBackground(Theme.BACKGROUND); // 배경색 지정
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // 전체 여백

        taskPanel = new TaskPanel();
        calendarPanel = new CalendarPanel(taskPanel);
        friendListPanel = new FriendListPanel(new FriendService(), null);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton friendBtn = new JButton("친구 목록");
        bottomPanel.add(friendBtn);

        add(calendarPanel, BorderLayout.CENTER);
        add(taskPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
        
        friendBtn.addActionListener(e -> {
            // 팝업 생성
            JDialog dialog = new JDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    "친구 목록",
                    true
            );

            dialog.setSize(650, 500);
            dialog.setLocationRelativeTo(this);

            // 현재 로그인한 사용자 정보 전달
            if (currentUserId != null) {
                friendListPanel.setUser(currentUserId);
            }

            dialog.add(friendListPanel);
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