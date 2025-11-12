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
        setBackground(new Color(240, 240, 240));

        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(400, 450));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        card.setLayout(null);

        JLabel title = new JLabel("투두캘린더", SwingConstants.CENTER);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        title.setBounds(100, 40, 200, 40);
        card.add(title);

        JTextField idField = new JTextField("아이디 입력");
        idField.setForeground(Color.GRAY);
        idField.setBounds(100, 120, 200, 40);
        addPlaceholderBehavior(idField, "아이디 입력");
        card.add(idField);

        JPasswordField pwField = new JPasswordField("비밀번호 입력");
        pwField.setForeground(Color.GRAY);
        pwField.setEchoChar((char) 0);
        pwField.setBounds(100, 180, 200, 40);
        addPlaceholderBehavior(pwField, "비밀번호 입력");
        card.add(pwField);

        JButton loginButton = new JButton("로그인하기");
        loginButton.setBounds(100, 250, 200, 40);
        card.add(loginButton);

        JLabel signupLabel = new JLabel("회원가입", SwingConstants.CENTER);
        signupLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        signupLabel.setForeground(Color.BLUE);
        signupLabel.setBounds(100, 310, 200, 30);
        card.add(signupLabel);

        // ✅ 회원가입 클릭 시 화면 전환
        signupLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainApp.showPanel("SIGNUP");
            }
        });

        loginButton.addActionListener(e -> mainApp.showPanel("MAIN"));
        add(card);
    }

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
