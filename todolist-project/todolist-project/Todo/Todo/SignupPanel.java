package Todo;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;

public class SignupPanel extends JPanel {
    private ToDoListApp mainApp;

    public SignupPanel(ToDoListApp mainApp) {
        this.mainApp = mainApp;// MainApp 참조 저장

        // 중앙 정렬
        setLayout(new GridBagLayout());// 가운데 정렬
        setBackground(new Color(240, 240, 240));// 배경색 설정

        // 카드 영역 (흰색 네모)
        JPanel card = new JPanel();// 회원가입 카드 패널
        card.setPreferredSize(new Dimension(400, 450));// 카드 크기 설정
        card.setBackground(Color.WHITE);// 카드 배경색
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));// 테두리 설정
        card.setLayout(null);// 절대 위치 레이아웃

        // 제목
        JLabel title = new JLabel("회원가입", SwingConstants.CENTER);// 제목 라벨
        title.setFont(new Font("맑은 고딕", Font.BOLD, 26));// 폰트 설정
        title.setBounds(100, 40, 200, 40);// 위치 및 크기 설정
        card.add(title);// 카드에 제목 추가

        // 아이디 입력
        JTextField idField = new JTextField("아이디 입력");// 아이디 입력 필드
        idField.setForeground(Color.GRAY);// 플레이스홀더 색상
        idField.setBounds(100, 120, 200, 40);// 위치 및 크기 설정
        addPlaceholderBehavior(idField, "아이디 입력");// 플레이스홀더 동작 추가
        card.add(idField);//    카드에 아이디 필드 추가

        // 비밀번호 입력
        JPasswordField pwField = new JPasswordField("비밀번호 입력");// 비밀번호 입력 필드
        pwField.setForeground(Color.GRAY);// 플레이스홀더 색상
        pwField.setEchoChar((char) 0);// 에코 문자 설정 (플레이스홀더 표시용)
        pwField.setBounds(100, 180, 200, 40);// 위치 및 크기 설정
        addPlaceholderBehavior(pwField, "비밀번호 입력");// 플레이스홀더 동작 추가
        card.add(pwField);// 카드에 비밀번호 필드 추가

        // 회원가입 완료 버튼
        JButton signupButton = new JButton("회원가입 완료");// 회원가입 완료 버튼
        signupButton.setBounds(100, 250, 200, 40);// 위치 및 크기 설정
        card.add(signupButton);// 카드에 회원가입 완료 버튼 추가

        // 로그인으로 돌아가기 라벨
        JLabel backLabel = new JLabel("로그인으로 돌아가기", SwingConstants.CENTER);// 로그인으로 돌아가기 라벨
        backLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));// 폰트 설정
        backLabel.setForeground(Color.BLUE);// 글자색 설정
        backLabel.setBounds(100, 310, 200, 30);// 위치 및 크기 설정
        card.add(backLabel);// 카드에 로그인으로 돌아가기 라벨 추가

        // ✅ 회원가입 완료 클릭 시 바로 로그인 화면으로 이동
        signupButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다!");// 알림창 표시
            mainApp.showPanel("LOGIN");// 로그인 화면으로 이동
        });

        // 로그인으로 돌아가기 클릭 시
        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainApp.showPanel("LOGIN");// 로그인 화면으로 이동
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
                    field.setText("");// 텍스트 초기화
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
                        ((JPasswordField) field).setEchoChar((char) 0);// 비밀번호 필드일 경우 에코 문자 해제
                }
            }
        });
    }
}
