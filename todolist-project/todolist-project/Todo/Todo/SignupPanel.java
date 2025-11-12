package Todo;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;

public class SignupPanel extends JPanel {
    private ToDoListApp mainApp;// 메인 애플리케이션 참조
    private JTextField idField;// 아이디 입력 필드
    private JPasswordField pwField;// 비밀번호 입력 필드

    public SignupPanel(ToDoListApp mainApp) {
        this.mainApp = mainApp;// 메인 애플리케이션 참조 저장

        // 중앙 정렬
        setLayout(new GridBagLayout());// 가운데 정렬
        setBackground(new Color(240, 240, 240));// 연한 회색 배경

        // 카드 영역 (흰색 네모)
        JPanel card = new JPanel();// 카드 패널
        card.setPreferredSize(new Dimension(400, 450));// 카드 크기
        card.setBackground(Color.WHITE);// 흰색 배경
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2)); // 회색 테두리
        card.setLayout(null); // 절대 위치 지정

        // 제목
        JLabel title = new JLabel("회원가입", SwingConstants.CENTER); // 제목 라벨
        title.setFont(new Font("맑은 고딕", Font.BOLD, 26));// 큰 글씨체
        title.setBounds(100, 40, 200, 40);// 위치 및 크기
        card.add(title);// 카드에 추가

        // 아이디 입력
        idField = new JTextField("아이디 입력"); // [수정] 필드 선언에서 'JTextField' 제거
        idField.setForeground(Color.GRAY);// 회색 글씨
        idField.setBounds(100, 120, 200, 40);// 위치 및 크기
        addPlaceholderBehavior(idField, "아이디 입력");// 플레이스홀더 동작 추가
        card.add(idField);// 카드에 추가

        // 비밀번호 입력
        pwField = new JPasswordField("비밀번호 입력"); // [수정] 필드 선언에서 'JPasswordField' 제거
        pwField.setForeground(Color.GRAY);// 회색 글씨
        pwField.setEchoChar((char) 0);// 플레이스홀더용 (글자 안보이게)
        pwField.setBounds(100, 180, 200, 40);// 위치 및 크기
        addPlaceholderBehavior(pwField, "비밀번호 입력");// 플레이스홀더 동작 추가
        card.add(pwField);// 카드에 추가

        // 회원가입 완료 버튼
        JButton signupButton = new JButton("회원가입 완료");// 버튼 생성
        signupButton.setBounds(100, 250, 200, 40);// 위치 및 크기
        card.add(signupButton);// 카드에 추가

        // 로그인으로 돌아가기 라벨
        JLabel backLabel = new JLabel("로그인으로 돌아가기", SwingConstants.CENTER);// 라벨 생성
        backLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));// 작은 글씨체
        backLabel.setForeground(Color.BLUE);// 파란색 글씨
        backLabel.setBounds(100, 310, 200, 30);// 위치 및 크기
        card.add(backLabel);// 카드에 추가

        // 회원가입 완료 버튼 리스너
        signupButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다!");// 완료 메시지
            mainApp.showPanel("LOGIN");// 로그인 패널로 전환
            clearFields(); // 회원가입 후 필드 초기화
        });

        // ← 로그인으로 돌아가기 클릭 시
        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainApp.showPanel("LOGIN");// 로그인 패널로 전환
                clearFields(); // 돌아갈 때도 필드 초기화
            }
        });

        add(card);// 메인 패널에 카드 추가
    }

    // 아이디와 비밀번호 필드를 placeholder 상태로 초기화

    public void clearFields() {
        idField.setText("아이디 입력");// 플레이스홀더 텍스트
        idField.setForeground(Color.GRAY);// 회색 글씨

        pwField.setText("비밀번호 입력");// 플레이스홀더 텍스트
        pwField.setForeground(Color.GRAY);// 회색 글씨
        pwField.setEchoChar((char) 0);// 플레이스홀더용 (글자 안보이게)
    }

    // placeholder 동작 (아이디/비밀번호 입력창 공용)
    private void addPlaceholderBehavior(JTextComponent field, String placeholder) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");// 텍스트 비우기
                    field.setForeground(Color.BLACK);// 검은색 글씨
                    if (field instanceof JPasswordField)// 비밀번호 필드일 때
                        ((JPasswordField) field).setEchoChar('●');// 글자 보이게
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);// 회색 글씨
                    field.setText(placeholder);// 플레이스홀더 텍스트
                    if (field instanceof JPasswordField)// 비밀번호 필드일 때
                        ((JPasswordField) field).setEchoChar((char) 0);// 글자 안보이게
                }
            }
        });
    }
}