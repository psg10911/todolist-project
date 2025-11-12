package Todo;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 할 일을 추가하기 위한 모달 JDialog (팝업창).
 * (간단한 GridLayout과 BorderLayout 사용)
 */
public class TaskDialog extends JDialog {
    
    private JTextField titleField;
    private JTextArea contentArea;
    private JTextField startDateField;
    private JTextField endDateField;
    
    private Task task = null; // 저장 버튼을 누르면 이 객체가 생성됨

    public TaskDialog(Frame owner, LocalDate selectedDate) {
        super(owner, "Add Task", true); // true = Modal 팝업
        setSize(400, 350);
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
            
            this.task = new Task(
                
                titleField.getText(),
                contentArea.getText(),
                startDateField.getText(),
                endDateField.getText()
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
