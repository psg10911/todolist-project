package Todo;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;

public class SignupPanel extends JPanel {
    private ToDoListApp mainApp;

    public SignupPanel(ToDoListApp mainApp) {
        this.mainApp = mainApp;

        setLayout(new GridBagLayout());
        setBackground(Theme.BACKGROUND);

        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(400, 450));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        card.setLayout(null);

        JLabel title = new JLabel("회원가입", SwingConstants.CENTER);
        title.setFont(Theme.FONT_BOLD_24);
        title.setForeground(Theme.TEXT_MAIN);
        title.setBounds(100, 40, 200, 40);
        card.add(title);

        JTextField idField = new JTextField("  아이디 입력");
        Theme.styleTextField(idField);
        idField.setForeground(Theme.TEXT_SUB);
        idField.setBounds(50, 110, 300, 45);
        addPlaceholderBehavior(idField, "  아이디 입력");
        card.add(idField);

        JPasswordField pwField = new JPasswordField("  비밀번호 입력");
        Theme.styleTextField(pwField);
        pwField.setForeground(Theme.TEXT_SUB);
        pwField.setEchoChar((char) 0);
        pwField.setBounds(50, 170, 300, 45);
        addPlaceholderBehavior(pwField, "  비밀번호 입력");
        card.add(pwField);

        JButton signupButton = new JButton("회원가입 완료");
        Theme.styleButton(signupButton);
        signupButton.setBounds(50, 240, 300, 45);
        card.add(signupButton);

        JLabel backLabel = new JLabel("로그인으로 돌아가기", SwingConstants.CENTER);
        backLabel.setFont(Theme.FONT_REGULAR_12);
        backLabel.setForeground(Theme.PRIMARY);
        backLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLabel.setBounds(100, 300, 200, 30);
        card.add(backLabel);

        signupButton.addActionListener(e -> {
            String id = idField.getText();
            String pw = new String(pwField.getPassword());
            // 플레이스홀더 처리 로직이 필요하다면 추가 (생략 시 그대로 전송될 수 있음)
            if(id.equals("아이디 입력")) id = "";
            if(pw.equals("비밀번호 입력")) pw = "";
            
            if(id.trim().isEmpty() || pw.trim().isEmpty()){
                JOptionPane.showMessageDialog(this, "정보를 입력해주세요.");
                return;
            }

            UserSql.insertUser(id, pw);
            JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다!");
            mainApp.showPanel("LOGIN");
        });

        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainApp.showPanel("LOGIN");
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