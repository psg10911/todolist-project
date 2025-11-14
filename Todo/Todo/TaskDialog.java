package Todo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * 할 일을 추가하기 위한 모달 JDialog (팝업창).
 * (간단한 GridLayout과 BorderLayout 사용)
 */
public class TaskDialog extends JDialog {
    
    private JTextField titleField;
    private JTextArea contentArea;
    private JTextField startDateField;
    private JTextField endDateField;
    
    private ButtonGroup importanceGroup; // 중요도 그룹
    private JRadioButton prio1, prio2, prio3; // 중요도 버튼
    
    private Task task = null; // 저장 버튼을 누르면 이 객체가 생성됨

    public TaskDialog(Frame owner, LocalDate selectedDate) {
        super(owner, "Add Task", true); // true = Modal 팝업
        setSize(400, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        
        // 1. 상단 입력 폼 패널 (GridLayout)
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 10));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("일정 제목 (Title):"));
        titleField = new JTextField();
        formPanel.add(titleField);
        
        String defaultDate = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        formPanel.add(new JLabel("시작일 (Start Date):"));
        startDateField = new JTextField(defaultDate);
        formPanel.add(startDateField);

        formPanel.add(new JLabel("종료일 (End Date):"));
        endDateField = new JTextField(defaultDate);
        formPanel.add(endDateField);

        add(formPanel, BorderLayout.NORTH);

        // 2. 중앙 내용(Content) 패널
        JPanel contentPanel = new JPanel(new BorderLayout(0, 5));
        contentPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        contentPanel.add(new JLabel("일정 내용 (Content):"), BorderLayout.NORTH);
        contentArea = new JTextArea(5, 20);
        contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        // ✅ 중요도 선택 라디오버튼 추가
        JPanel importancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        importancePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("중요도"));

        importanceGroup = new ButtonGroup();

        prio1 = new JRadioButton("1 (낮음)");
        prio1.setActionCommand("1");

        prio2 = new JRadioButton("2 (보통)");
        prio2.setActionCommand("2");
        prio2.setSelected(true); // 기본 선택값

        prio3 = new JRadioButton("3 (높음)");
        prio3.setActionCommand("3");

        importanceGroup.add(prio1);
        importanceGroup.add(prio2);
        importanceGroup.add(prio3);

        importancePanel.add(prio1);
        importancePanel.add(prio2);
        importancePanel.add(prio3);

        contentPanel.add(importancePanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        // 3. 하단 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 'Save' 버튼 리스너
        saveButton.addActionListener(e -> {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "제목을 입력해주세요.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 선택된 중요도 값 가져오기
            int priority = Integer.parseInt(importanceGroup.getSelection().getActionCommand());
            
            // Task 객체 생성 (int priority 추가됨)
            this.task = new Task(
                titleField.getText(),
                contentArea.getText(),
                startDateField.getText(),
                endDateField.getText(),
                priority
            );
            dispose(); // 팝업창 닫기
        });

        // 'Cancel' 버튼 리스너
        cancelButton.addActionListener(e -> {
            this.task = null;
            dispose();
        });
    }

    // 메인 패널에서 이 다이얼로그의 결과(저장된 Task)를 가져가는 메소드
    public Task getTask() {
        return this.task;
    }
}
