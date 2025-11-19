package Todo;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends JPanel {
    private ToDoListApp mainApp;

    public LoginPanel(ToDoListApp mainApp) {
        this.mainApp = mainApp;// MainApp 참조 저장

        setLayout(new GridBagLayout());// 가운데 정렬
        setBackground(new Color(240, 240, 240));// 배경색 설정

        JPanel card = new JPanel();// 로그인 카드 패널
        card.setPreferredSize(new Dimension(400, 500));// 카드 크기 설정
        card.setBackground(Color.WHITE);// 카드 배경색
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));// 테두리 설정
        card.setLayout(null);// 절대 위치 레이아웃

        JLabel title = new JLabel("투두캘린더", SwingConstants.CENTER);// 제목 라벨
        title.setFont(new Font("맑은 고딕", Font.BOLD, 26));// 폰트 설정
        title.setBounds(100, 40, 200, 40);// 위치 및 크기 설정
        card.add(title);// 카드에 제목 추가

        JTextField idField = new JTextField("아이디 입력");// 아이디 입력 필드
        idField.setForeground(Color.GRAY);// 플레이스홀더 색상
        idField.setBounds(100, 120, 200, 40);// 위치 및 크기 설정
        addPlaceholderBehavior(idField, "아이디 입력");// 플레이스홀더 동작 추가
        card.add(idField);// 카드에 아이디 필드 추가

        JPasswordField pwField = new JPasswordField("비밀번호 입력");// 비밀번호 입력 필드
        pwField.setForeground(Color.GRAY);// 플레이스홀더 색상
        pwField.setEchoChar((char) 0);// 에코 문자 설정 (플레이스홀더 표시용)
        pwField.setBounds(100, 180, 200, 40);// 위치 및 크기 설정
        addPlaceholderBehavior(pwField, "비밀번호 입력");// 플레이스홀더 동작 추가
        card.add(pwField);// 카드에 비밀번호 필드 추가

        JButton loginButton = new JButton("로그인하기");// 로그인 버튼
        loginButton.setBounds(100, 250, 200, 40);// 위치 및 크기 설정
        card.add(loginButton);// 카드에 로그인 버튼 추가

        JLabel signupLabel = new JLabel("회원가입", SwingConstants.CENTER);// 회원가입 라벨
        signupLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));// 폰트 설정
        signupLabel.setForeground(Color.BLUE);// 글자색 설정
        signupLabel.setBounds(100, 310, 200, 30);// 위치 및 크기 설정
        card.add(signupLabel);// 카드에 회원가입 라벨 추가

        signupLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainApp.showPanel("SIGNUP");// 회원가입 패널로 전환
            }
        });

        loginButton.addActionListener(e -> mainApp.showPanel("MAIN"));// 로그인 버튼 클릭 시 메인 패널로 전환
        add(card);// 로그인 카드 패널 추가
    }

    private void addPlaceholderBehavior(JTextComponent field, String placeholder) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");// 플레이스홀더 텍스트 제거
                    field.setForeground(Color.BLACK);// 일반 텍스트 색상으로 변경
                    if (field instanceof JPasswordField)
                        ((JPasswordField) field).setEchoChar('●');// 비밀번호 필드일 경우 에코 문자 설정
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);// 플레이스홀더 색상으로 변경
                    field.setText(placeholder);// 플레이스홀더 텍스트 복원
                    if (field instanceof JPasswordField)
                        ((JPasswordField) field).setEchoChar((char) 0);// 에코 문자 제거
                }
            }
        });
    }
}
