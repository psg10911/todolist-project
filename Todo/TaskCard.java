package Todo;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;

/**
 * [신규 클래스]
 * Task 1개를 표시하는 커스텀 UI 컴포넌트 (View).
 * TaskPanel의 내부 클래스에서 분리됨.
 */
public class TaskCard extends JPanel {

    private final Task task;
    private final JCheckBox checkBox;
    private final JButton upBtn;
    private final JButton downBtn;

    public TaskCard(Task task, int priority) {
        this.task = task;

        setLayout(new BorderLayout(10, 0));
        setPreferredSize(new Dimension(360, 58));
        setMaximumSize(new Dimension(360, 58));
        setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(5, 10, 5, 10)));

        switch (priority) {
            case 1 -> setBackground(new Color(204, 226, 203)); // 초록
            case 2 -> setBackground(new Color(255, 204, 182)); // 주황
            case 3 -> setBackground(new Color(243, 176, 195)); // 빨강
        }

        // 1. (West) 중요도
        JLabel priorityLabel = new JLabel(String.valueOf(priority));
        priorityLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        priorityLabel.setHorizontalAlignment(SwingConstants.CENTER);
        priorityLabel.setPreferredSize(new Dimension(36, 50));

        // 2. (Center) 제목, 날짜
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(task.getTitle());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel periodLabel = new JLabel(task.getStartDate() + " ~ " + task.getEndDate());
        periodLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        periodLabel.setForeground(Color.DARK_GRAY);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(2));
        centerPanel.add(periodLabel);
        centerPanel.add(Box.createVerticalGlue());

        // 3. (East) 체크박스, 이동 버튼
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        checkBox = new JCheckBox();
        checkBox.setPreferredSize(new Dimension(42, 42));
        checkBox.setOpaque(false);
        checkBox.setHorizontalAlignment(SwingConstants.CENTER);
        checkBox.setSelected(task.isCompleted());
        rightPanel.add(checkBox, BorderLayout.CENTER);

        JPanel movePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        movePanel.setOpaque(false);
        upBtn = new JButton("▲");
        downBtn = new JButton("▼");
        upBtn.setMargin(new Insets(0, 2, 0, 2));
        downBtn.setMargin(new Insets(0, 2, 0, 2));
        movePanel.add(upBtn);
        movePanel.add(downBtn);
        rightPanel.add(movePanel, BorderLayout.EAST);

        add(priorityLabel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    // 4. 이벤트 리스너 연결 (TaskPanel이 호출)
    public void addEditListener(MouseAdapter listener) {
        this.addMouseListener(listener);
    }

    public void addCheckListener(ActionListener listener) {
        checkBox.addActionListener(listener);
    }

    public void addMoveUpListener(ActionListener listener) {
        upBtn.addActionListener(listener);
    }

    public void addMoveDownListener(ActionListener listener) {
        downBtn.addActionListener(listener);
    }
}