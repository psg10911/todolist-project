package Todo;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;

// Task 1개를 표시하는 커스텀 UI 컴포넌트 (View).TaskPanel의 내부 클래스에서 분리됨.
public class TaskCard extends JPanel {

    private final Task task;// Task 데이터 모델
    private final JCheckBox checkBox;// 완료 체크박스
    private final JButton upBtn;// 위로 이동 버튼
    private final JButton downBtn;// 아래로 이동 버튼

    public TaskCard(Task task, int priority) {
        this.task = task;// Task 데이터 모델

        setLayout(new BorderLayout(10, 0));// 가로 간격 10
        setPreferredSize(new Dimension(360, 58));// 고정 크기
        setMaximumSize(new Dimension(360, 58));// 고정 크기
        setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(5, 10, 5, 10)));// 테두리 + 안쪽 여백

        switch (priority) {
            case 1 -> setBackground(new Color(204, 226, 203)); // 초록
            case 2 -> setBackground(new Color(255, 204, 182)); // 주황
            case 3 -> setBackground(new Color(243, 176, 195)); // 빨강
        }

        // 1. (West) 중요도
        JLabel priorityLabel = new JLabel(String.valueOf(priority));// 중요도 숫자
        priorityLabel.setFont(new Font("SansSerif", Font.BOLD, 20));// 굵은 글씨
        priorityLabel.setHorizontalAlignment(SwingConstants.CENTER);// 가운데 정렬
        priorityLabel.setPreferredSize(new Dimension(36, 50));// 고정 크기

        // 2. (Center) 제목, 날짜
        JPanel centerPanel = new JPanel();// 제목 + 기간 패널
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));// 수직 정렬
        centerPanel.setOpaque(false);// 투명하게

        JLabel titleLabel = new JLabel(task.getTitle());// 제목 라벨
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));// 굵은 글씨

        JLabel periodLabel = new JLabel(task.getStartDate() + " ~ " + task.getEndDate());// 기간 라벨
        periodLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));// 일반 글씨
        periodLabel.setForeground(Color.DARK_GRAY);// 어두운 회색 글씨

        centerPanel.add(Box.createVerticalGlue());// 위쪽 여백
        centerPanel.add(titleLabel);// 제목
        centerPanel.add(Box.createVerticalStrut(2));// 제목-기간 간격
        centerPanel.add(periodLabel);// 기간
        centerPanel.add(Box.createVerticalGlue());// 아래쪽 여백

        // 3. (East) 체크박스, 이동 버튼
        JPanel rightPanel = new JPanel(new BorderLayout());// 체크박스 + 이동 버튼 패널
        rightPanel.setOpaque(false);// 투명하게

        checkBox = new JCheckBox();// 완료 체크박스
        checkBox.setPreferredSize(new Dimension(42, 42));// 고정 크기
        checkBox.setOpaque(false);// 투명하게
        checkBox.setHorizontalAlignment(SwingConstants.CENTER);// 가운데 정렬
        checkBox.setSelected(task.isCompleted());// 완료 여부 반영
        rightPanel.add(checkBox, BorderLayout.CENTER);// 가운데에 체크박스

        JPanel movePanel = new JPanel(new GridLayout(2, 1, 0, 2));// 위/아래 이동 버튼 패널
        movePanel.setOpaque(false);// 투명하게
        upBtn = new JButton("▲");// 위로 이동 버튼
        downBtn = new JButton("▼");// 아래로 이동 버튼
        upBtn.setMargin(new Insets(0, 2, 0, 2));// 버튼 안쪽 여백
        downBtn.setMargin(new Insets(0, 2, 0, 2));// 버튼 안쪽 여백
        movePanel.add(upBtn);// 위로 이동 버튼
        movePanel.add(downBtn);// 아래로 이동 버튼
        rightPanel.add(movePanel, BorderLayout.EAST);// 오른쪽에 이동 버튼 패널

        add(priorityLabel, BorderLayout.WEST);// 왼쪽에 중요도
        add(centerPanel, BorderLayout.CENTER);// 가운데에 제목 + 기간
        add(rightPanel, BorderLayout.EAST);// 오른쪽에 체크박스 + 이동 버튼
    }

    // 4. 이벤트 리스너 연결 (TaskPanel이 호출)
    public void addEditListener(MouseAdapter listener) {
        this.addMouseListener(listener);// 카드 전체에 마우스 리스너 추가
    }

    public void addCheckListener(ActionListener listener) {
        checkBox.addActionListener(listener);// 체크박스에 액션 리스너 추가
    }

    public void addMoveUpListener(ActionListener listener) {
        upBtn.addActionListener(listener);// 위로 이동 버튼에 액션 리스너 추가
    }

    public void addMoveDownListener(ActionListener listener) {
        downBtn.addActionListener(listener);// 아래로 이동 버튼에 액션 리스너 추가
    }
}