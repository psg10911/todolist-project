package Todo;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.awt.event.ActionListener; // Timer용

// 로그인 직후 미완료 할일을 알려주는 반투명 팝업 윈도우. JWindow는 프레임이나 타이틀바가 없는 가벼운 창

public class NotificationPopup extends JWindow {

    private Timer fadeOutTimer;

    public NotificationPopup(Frame owner, int incompleteCount) {
        super(owner); // 부모 프레임(ToDoListApp)

        // 1. 윈도우 기본 설정
        setSize(300, 160); // 팝업 크기
        setLocationRelativeTo(null); // 화면 중앙에 배치

        // 2. 반투명 설정
        setOpacity(0.9f); // 90% 불투명도

        // 3. 메인 콘텐츠 패널
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245)); // 약간 밝은 회색
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1)); // 얇은 테두리

        // 4. X 버튼
        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        closeButton.setForeground(Color.RED);
        closeButton.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        closeButton.setContentAreaFilled(false); // 버튼 배경 투명
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 'X' 버튼 누르면 즉시 창 닫기
        closeButton.addActionListener(e -> dispose());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topPanel.setOpaque(false); // 패널 배경 투명
        topPanel.add(closeButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 5. 중앙 정렬될 텍스트 콘텐츠
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false); // 패널 배경 투명

        // 5. 오늘 날짜
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd (E)");
        JLabel dateLabel = new JLabel(today.format(formatter));
        dateLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 6. 미완료 할일 텍스트
        JLabel line1Label = new JLabel("오늘의 미완료 할일");
        line1Label.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        line1Label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel line2Label = new JLabel(incompleteCount + " 가지 남았습니다.");
        line2Label.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        line2Label.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 패널에 라벨들 추가 (간격 포함)
        contentPanel.add(Box.createVerticalStrut(5)); // 위쪽 여백
        contentPanel.add(dateLabel);
        contentPanel.add(Box.createVerticalStrut(15)); // 라벨 사이 여백
        contentPanel.add(line1Label);
        contentPanel.add(line2Label);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 윈도우에 메인 패널 추가
        add(mainPanel);

    }

    /**
     * 윈도우를 서서히 투명하게 만들며 닫는 메서드
     */

}