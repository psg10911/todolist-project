package Todo;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;

public class ToDoListApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardPanel;

    private LoginPanel loginPanel;
    private SignupPanel signupPanel;
    private MainPanel mainPanel;

    public ToDoListApp() {
        setTitle("Todo 리스트 프로그램");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 카드 패널 설정
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        // 패널 생성
        loginPanel = new LoginPanel(this); 
        signupPanel = new SignupPanel(this);
        mainPanel = new MainPanel();

        cardPanel.add(loginPanel, "LOGIN");
        cardPanel.add(signupPanel, "SIGNUP");
        cardPanel.add(mainPanel, "MAIN");

        add(cardPanel);
        cardLayout.show(cardPanel, "LOGIN");
    }

    public void showPanel(String panelName) {
        cardLayout.show(cardPanel, panelName);
    }

    // LoginPanel에서 성공하면 호출
    public void initAfterLogin(String userId) {
        mainPanel.getTaskPanel().initAfterLogin(userId);
        mainPanel.setCurrentUserId(userId);
        mainPanel.getFriendListPanel().setUser(userId);
        showPanel("MAIN"); 
    }

    public static void main(String[] args) {
        // [중요] 한글 깨짐 방지 및 전체 폰트 통일 (맑은 고딕)
        setUIFont(new javax.swing.plaf.FontUIResource("맑은 고딕", Font.PLAIN, 13));

        SwingUtilities.invokeLater(() -> { 
            ToDoListApp app = new ToDoListApp();
            app.setVisible(true);
        });
    }

    // UI 매니저의 모든 기본 폰트를 변경하는 헬퍼 메서드
    private static void setUIFont(javax.swing.plaf.FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(key, f);
        }
    }
}