package Todo;

import javax.swing.*;
import java.awt.*;


public class ToDoListApp extends JFrame {
    private JTextField iField;
    private JPasswordField passwordField;
    private JTextField namField;
    private CardLayout cardLayout;
    private JPanel cardPanel; // 카드 레이아웃을 적용할 패널

    public ToDoListApp() {
        setTitle("투두리스트 프로그램");// 창 제목 설정
        setSize(1000, 700);// 창 크기 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 종료 시 프로그램 종료
        setLocationRelativeTo(null);// 화면 중앙에 창 배치

        
        cardLayout = new CardLayout();// 카드 레이아웃 생성

        cardPanel = new JPanel(cardLayout);// 카드 레이아웃 패널 생성

        // 1. 로그인 화면 패널 생성
        LoginPanel loginPanel = new LoginPanel(this);

        // 회원가입 화면
        SignupPanel signupPanel = new SignupPanel(this);

        // 2. 메인 화면 패널 생성
        MainPanel mainPanel = new MainPanel();

        // [수업 자료] PDF의 cardPanel.add(..., "이름") [cite: 35]
        cardPanel.add(loginPanel, "LOGIN");
        cardPanel.add(signupPanel, "SIGNUP");
        cardPanel.add(mainPanel, "MAIN");

        // 프레임에 cardPanel 추가
        add(cardPanel);

        // 처음 보여줄 화면 설정
        cardLayout.show(cardPanel, "LOGIN");
    }

   
     //@param panelName "LOGIN" 또는 "MAIN"
     
    public void showPanel(String panelName) {
        cardLayout.show(cardPanel, panelName);// 지정한 이름의 패널로 전환
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ToDoListApp app = new ToDoListApp();// 앱 인스턴스 생성
            app.setVisible(true);
        });
    }
}
