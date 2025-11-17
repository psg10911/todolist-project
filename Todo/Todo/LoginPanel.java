package Todo;
import javax.swing.*;
import java.awt.*;

/**
 * 로그인 화면 (JPanel).
 * ID, PW, Name 입력을 받습니다.
 */
public class LoginPanel extends JPanel {

    private ToDoListApp mainApp; // 화면 전환을 위한 메인 앱 참조
    private JTextField idField;
    private JPasswordField passField;
    private JTextField nameField;

    public LoginPanel(ToDoListApp mainApp) {
        this.mainApp = mainApp;
        
        // 간단한 배치를 위해 GridLayout 사용
        setLayout(new GridLayout(4, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(150, 200, 150, 200)); // 여백

        add(new JLabel("ID:"));
        idField = new JTextField();
        add(idField);

        add(new JLabel("Password:"));
        passField = new JPasswordField();
        add(passField);
        
        add(new JLabel("Name:"));
        nameField = new JTextField();
        add(nameField);

        // 빈 레이블과 버튼으로 4x2 그리드 맞추기
        add(new JLabel("")); // 빈 칸
        JButton loginButton = new JButton("로그인");
        add(loginButton);

        // 로그인 버튼 리스너
        loginButton.addActionListener(e -> {
            // (실제로는 여기서 ID/PW/Name을 검증하고 저장해야 합니다)
            
            // 프론트엔드 데모: 메인 화면으로 전환
            // [수업 자료] 버튼 클릭으로 화면을 전환하는 원리 적용 
            mainApp.showPanel("MAIN");
        });
    }
}