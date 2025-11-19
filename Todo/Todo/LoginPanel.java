package Todo;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends JPanel {
    private ToDoListApp mainApp;

    public LoginPanel(ToDoListApp mainApp) {
        this.mainApp = mainApp;
        
        setLayout(new GridBagLayout());
        setBackground(Theme.BACKGROUND); // 전체 배경색

        // 카드(박스) 영역
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(400, 500));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        card.setLayout(null);

        // 제목
        JLabel title = new JLabel("투두캘린더", SwingConstants.CENTER);
        title.setFont(Theme.FONT_BOLD_24);
        title.setForeground(Theme.TEXT_MAIN);
        title.setBounds(100, 50, 200, 40);
        card.add(title);

        // 아이디 필드
        JTextField idField = new JTextField("  아이디 입력");
        Theme.styleTextField(idField);
        idField.setForeground(Theme.TEXT_SUB);
        idField.setBounds(50, 130, 300, 45);
        addPlaceholderBehavior(idField, "  아이디 입력");
        card.add(idField);

        // 비밀번호 필드
        JPasswordField pwField = new JPasswordField("  비밀번호 입력");
        Theme.styleTextField(pwField);
        pwField.setForeground(Theme.TEXT_SUB);
        pwField.setEchoChar((char) 0);
        pwField.setBounds(50, 190, 300, 45);
        addPlaceholderBehavior(pwField, " 비밀번호 입력");
        card.add(pwField);

        // 로그인 버튼
        JButton loginButton = new JButton("로그인하기");
        Theme.styleButton(loginButton);
        loginButton.setBounds(50, 270, 300, 45);
        card.add(loginButton);

        // 회원가입 링크
        JLabel signupLabel = new JLabel("회원가입", SwingConstants.CENTER);
        signupLabel.setFont(Theme.FONT_REGULAR_12);
        signupLabel.setForeground(Theme.PRIMARY);
        signupLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signupLabel.setBounds(100, 330, 200, 30);
        card.add(signupLabel);

        // 이벤트 리스너
        signupLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainApp.showPanel("SIGNUP");
            }
        });

        loginButton.addActionListener(e -> {
            String rawId = idField.getText();
            String rawPw = new String(pwField.getPassword());

            if ("아이디 입력".equals(rawId)) rawId = "";
            if ("비밀번호 입력".equals(rawPw)) rawPw = "";

            String id = rawId.trim();
            String pw = rawPw.trim();

            if (id.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 입력하세요.");
                return;
            }

            boolean success = UserSql.loginUser(id, pw);
            if (success) {
                mainApp.initAfterLogin(id);
            } else {
                JOptionPane.showMessageDialog(this, "아이디 또는 비밀번호가 올바르지 않습니다.");
            }
        });

        add(card);
    }

    private void addPlaceholderBehavior(JTextComponent field, String placeholder) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // 텍스트 내용 대신 "색상"으로 판단 (더 정확함)
                // 글자색이 '연한 회색(TEXT_SUB)'이면 플레이스홀더 상태임
                if (field.getForeground().equals(Theme.TEXT_SUB)) {
                    field.setText("");
                    field.setForeground(Theme.TEXT_MAIN); // 입력 시 진한 색으로 변경
                    if (field instanceof JPasswordField) {
                        ((JPasswordField) field).setEchoChar('●'); // 비밀번호 마스킹 켜기
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // 비어있으면 다시 플레이스홀더로 복구
                if (field.getText().isEmpty()) {
                    field.setForeground(Theme.TEXT_SUB); // 다시 연한 회색으로
                    field.setText(placeholder);
                    if (field instanceof JPasswordField) {
                        ((JPasswordField) field).setEchoChar((char) 0); // 마스킹 끄기 (글자 보이게)
                    }
                }
            }
        });
    }
}