package Todo;

import javax.swing.*;
import java.awt.*;

//메인 JFrame 클래스. CardLayout으로 로그인 화면->메인 화면 전환

public class ToDoListApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardPanel; // 카드 레이아웃을 적용

    private LoginPanel loginPanel;
    private SignupPanel signupPanel;
    private MainPanel mainPanel;

    public ToDoListApp() {
        setTitle("Todo 리스트 프로그램");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        //  로그인,회원가입,main 화면 
        loginPanel = new LoginPanel(this); 
        signupPanel = new SignupPanel(this);
        mainPanel = new MainPanel();

        cardPanel.add(loginPanel, "LOGIN");
        cardPanel.add(signupPanel, "SIGNUP");
        cardPanel.add(mainPanel, "MAIN");

        // 프레임에 cardPanel 추가
        add(cardPanel);

        // 처음 보여줄 화면 설정
        cardLayout.show(cardPanel, "LOGIN");
    }

    
     
     //panelName "LOGIN", "MAIN" ,"SIGNUP"
     
    
    public void showPanel(String panelName) {
        cardLayout.show(cardPanel, panelName);
    }

    // LoginPanel에서 성공하면 호출
    public void initAfterLogin(String userId) {
        
        mainPanel.getTaskPanel().initAfterLogin(userId); // MainPanel 안의 TaskPanel 초기화: userId 주입 + 오늘 일정 로드
        showPanel("MAIN"); // 메인 화면으로 전환
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { //EDT(스레드) 실행
            ToDoListApp app = new ToDoListApp();
            app.setVisible(true);
        });
    }
}