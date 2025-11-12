package Todo;

import javax.swing.*;
import java.awt.*;

/**
 * 메인 JFrame 클래스.
 * [수업 자료] CardLayout을 사용해 로그인 화면과 메인 화면을 전환합니다.
 */
public class ToDoListApp extends JFrame {
    private JTextField iField;
    private JPasswordField passwordField;
    private JTextField namField;
    private CardLayout cardLayout;
    private JPanel cardPanel; // 카드 레이아웃을 적용할 패널 (PDF의 cardPanel)

    // --- [ 수정된 부분 1: 패널 필드 선언 ] ---
    // 패널들을 클래스 필드로 선언하여 showPanel에서 접근할 수 있도록 함
    private LoginPanel loginPanel;
    private SignupPanel signupPanel;
    private MainPanel mainPanel;
    // --- [ 수정 완료 ] ---

    public ToDoListApp() {
        setTitle("투두리스트 프로그램");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // [수업 자료] PDF의 cards = new CardLayout()
        cardLayout = new CardLayout();
        // [수업 자료] PDF의 cardPanel = new JPanel(cards)
        cardPanel = new JPanel(cardLayout);

        // --- [ 수정된 부분 2: 패널 생성 및 저장 ] ---
        // 1. 로그인 화면 패널 생성
        loginPanel = new LoginPanel(this); // 필드에 저장

        // 2. 회원가입 화면
        signupPanel = new SignupPanel(this); // 필드에 저장

        // 3. 메인 화면 패널 생성 (this를 전달)
        mainPanel = new MainPanel(this); // 필드에 저장
        // --- [ 수정 완료 ] ---

        // [수업 자료] PDF의 cardPanel.add(..., "이름")
        cardPanel.add(loginPanel, "LOGIN");
        cardPanel.add(signupPanel, "SIGNUP");
        cardPanel.add(mainPanel, "MAIN");

        // 프레임에 cardPanel 추가
        add(cardPanel);

        // 처음 보여줄 화면 설정
        cardLayout.show(cardPanel, "LOGIN");
    }

    // [수업 자료] PDF의 cards.show() 원리를 이용한 화면 전환 메서드 @param panelName "LOGIN" 또는 "MAIN"

    public void showPanel(String panelName) {

        // "LOGIN" 패널을 보여주기 직전에, LoginPanel의 필드를 초기화하는 메서드를 호출합니다.
        if ("LOGIN".equals(panelName)) {
            // (loginPanel이 null이 아닌지 확인)
            if (loginPanel != null) {
                loginPanel.clearFields();
            }
        }

        cardLayout.show(cardPanel, panelName);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ToDoListApp app = new ToDoListApp();
            app.setVisible(true);
        });
    }
}