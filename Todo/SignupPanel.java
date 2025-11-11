package Todo;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;

public class SignupPanel extends JPanel {
    private ToDoListApp mainApp;

    public SignupPanel(ToDoListApp mainApp) {
        this.mainApp = mainApp;

        // 중앙 정렬
        setLayout(new GridBagLayout());
        setBackground(new Color(240, 240, 240));

        // 카드 영역 (흰색 네모)
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(400, 450));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        card.setLayout(null);

        // 제목
        JLabel title = new JLabel("회원가입", SwingConstants.CENTER);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        title.setBounds(100, 40, 200, 40);
        card.add(title);

        // 아이디 입력
        JTextField idField = new JTextField("아이디 입력");
        idField.setForeground(Color.GRAY);
        idField.setBounds(100, 120, 200, 40);
        addPlaceholderBehavior(idField, "아이디 입력");
        card.add(idField);

        // 비밀번호 입력
        JPasswordField pwField = new JPasswordField("비밀번호 입력");
        pwField.setForeground(Color.GRAY);
        pwField.setEchoChar((char) 0);
        pwField.setBounds(100, 180, 200, 40);
        addPlaceholderBehavior(pwField, "비밀번호 입력");
        card.add(pwField);

        // 회원가입 완료 버튼
        JButton signupButton = new JButton("회원가입 완료");
        signupButton.setBounds(100, 250, 200, 40);
        card.add(signupButton);

        // 로그인으로 돌아가기 라벨
        JLabel backLabel = new JLabel("로그인으로 돌아가기", SwingConstants.CENTER);
        backLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        backLabel.setForeground(Color.BLUE);
        backLabel.setBounds(100, 310, 200, 30);
        card.add(backLabel);

        // ✅ 회원가입 완료 클릭 시 바로 로그인 화면으로 이동
        signupButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다!");
            mainApp.showPanel("LOGIN");
        });

        // ← 로그인으로 돌아가기 클릭 시
        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainApp.showPanel("LOGIN");
            }
        });

        add(card);
    }

    // placeholder 동작 (아이디/비밀번호 입력창 공용)
    private void addPlaceholderBehavior(JTextComponent field, String placeholder) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                    if (field instanceof JPasswordField)
                        ((JPasswordField) field).setEchoChar('●');
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                    if (field instanceof JPasswordField)
                        ((JPasswordField) field).setEchoChar((char) 0);
                }
            }
        });
    }
}