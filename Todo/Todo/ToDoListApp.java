package Todo;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;

public class ToDoListApp extends JFrame {

    private CardLayout cardLayout;// 카드 레이아웃
    private JPanel cardPanel;// 카드 패널
    private LoginPanel loginPanel;// 로그인 패널
    private SignupPanel signupPanel;// 회원가입 패널
    private MainPanel mainPanel;// 메인 패널

    private static boolean seedLoaded = false; // 시드 데이터 로드 여부

    public ToDoListApp() {
        setTitle("Todo 리스트 프로그램");// 창 제목
        setSize(1000, 700);// 창 크기
        // ★ 최소 크기 설정 (너비 800, 높이 600 이하로 축소 불가)
        setMinimumSize(new Dimension(800, 600)); 
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 창 닫기 버튼 클릭 시 종료
        setLocationRelativeTo(null);// 화면 중앙에 창 띄우기

        // 카드 패널 설정
        cardLayout = new CardLayout();// 카드 레이아웃 생성
        cardPanel = new JPanel(cardLayout);// 카드 패널 생성
        
        // 패널 생성
        loginPanel = new LoginPanel(this); // 로그인 패널
        signupPanel = new SignupPanel(this); // 회원가입 패널
        mainPanel = new MainPanel(); // 메인 패널

        cardPanel.add(loginPanel, "LOGIN");// 카드 패널에 로그인 패널 추가
        cardPanel.add(signupPanel, "SIGNUP");// 카드 패널에 회원가입 패널 추가
        cardPanel.add(mainPanel, "MAIN");// 카드 패널에 메인 패널 추가

        add(cardPanel);// 프레임에 카드 패널 추가
        cardLayout.show(cardPanel, "LOGIN");// 처음에는 로그인 패널 보이기
    }

    public void showPanel(String panelName) {
        cardLayout.show(cardPanel, panelName);// 지정한 패널 보이기
    }

    // LoginPanel에서 성공하면 호출
    public void initAfterLogin(String userId) {
        
        // ★ [핵심 수정 로직] ★
        // 1. 로그인한 ID가 'user'이고 (특정 ID에만 시드 데이터 주입),
        // 2. 'user' 계정에 할 일이 하나도 없을 때만 로드합니다. (딱 한 번만 로드)
        if ("user".equals(userId) && !TodoDao.hasAnyTodo(userId)) {            
            // TodoFile.readAllAndInsert는 파일에 적힌 ID(user)를 그대로 사용하기 때문에, 
            // 파일 내용을 로드하면 DB에 user의 할 일이 생깁니다.
            TodoFile.readAllAndInsert("Todo\\Todo\\Todo_persona.txt");
            
            System.out.println("초기 데이터가 'user' 계정에 로드되었습니다.");
        }
        // 'pko'와 같은 다른 ID가 로그인하면, if 조건에 걸리지 않아 로드 자체가 되지 않습니다.

        mainPanel.getTaskPanel().initAfterLogin(userId);
        mainPanel.setCurrentUserId(userId);
        mainPanel.getFriendListPanel().setUser(userId);
        showPanel("MAIN");
    }
    // 로그아웃 메서드
    public void logout() {
        // MainPanel 초기화
        mainPanel.clear();
        
        // LoginPanel 초기화
        loginPanel.clearFields();
        
        // 로그인 패널 표시
        cardLayout.show(cardPanel, "LOGIN");
    }

    public static void main(String[] args) {
        // [중요] 한글 깨짐 방지 및 전체 폰트 통일 (맑은 고딕)
        setUIFont(new javax.swing.plaf.FontUIResource("맑은 고딕", Font.PLAIN, 13));// 기본 폰트 설정

        SwingUtilities.invokeLater(() -> { 
            ToDoListApp app = new ToDoListApp();// 앱 생성
            app.setVisible(true);// 앱 보이기
        });
    }

    // UI 매니저의 모든 기본 폰트를 변경하는 헬퍼 메서드
    private static void setUIFont(javax.swing.plaf.FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();// UI 매니저 기본 폰트 키 열거
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();// 다음 키 가져오기
            Object value = UIManager.get(key);// 해당 키의 값 가져오기
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(key, f);// 기본 폰트로 설정
        }
    }
}