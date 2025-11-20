package Todo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskDialog extends JDialog {

    private JTextField titleField;
    private JTextArea contentArea;
    private JTextField startDateField;
    private JTextField endDateField;
    private JCheckBox completedCheck;

    //라디오 버튼
    private JRadioButton priLowBtn;     
    private JRadioButton priMidBtn;     
    private JRadioButton priHighBtn;    

    private boolean editMode;
    private Task workingCopy;
    private Task resultTask = null;

    private void buildUI(LocalDate defaultDate, String dialogTitle) {
        setTitle(dialogTitle);
        setModal(true);
        setSize(450, 520);   // ★ 살짝 증가 (라디오 버튼 추가)
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(Color.WHITE); // 배경 흰색

        // 상단 폼 (GridBagLayout으로 변경하여 레이아웃 개선)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 제목
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        formPanel.add(new JLabel("일정 제목"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.8;
        titleField = new JTextField();
        Theme.styleTextField(titleField);
        formPanel.add(titleField, gbc);

        // String defaultDateStr = (defaultDate != null)
        //         ? defaultDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        //         : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        
                // 수정: 기본값을 "yyyy-MM-dd HH:mm"으로 만들기
        LocalDateTime baseDateTime = (defaultDate != null)
                ? defaultDate.atTime(0, 0)
                : LocalDateTime.now();

        String defaultDateTimeStr =
        baseDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        
        // 시작일
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        formPanel.add(new JLabel("시작일"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.8;
        startDateField = new JTextField(defaultDateTimeStr);   // 수정
        Theme.styleTextField(startDateField);
        formPanel.add(startDateField, gbc);

        // 종료일
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.2;
        formPanel.add(new JLabel("종료일"), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.8;
        endDateField = new JTextField(defaultDateTimeStr);     //  수정
        Theme.styleTextField(endDateField);
        formPanel.add(endDateField, gbc);


        // 중요도 섹션
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.2;
        formPanel.add(new JLabel("중요도"), gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.8;

        priLowBtn = new JRadioButton("높음");   // 1
        priMidBtn = new JRadioButton("보통");   // 2
        priHighBtn = new JRadioButton("낮음");  // 3

        priLowBtn.setBackground(Color.WHITE);
        priMidBtn.setBackground(Color.WHITE);
        priHighBtn.setBackground(Color.WHITE);

        ButtonGroup group = new ButtonGroup();
        group.add(priLowBtn);
        group.add(priMidBtn);
        group.add(priHighBtn);

        // 새 일정 추가 시 기본값은 "보통"
        priMidBtn.setSelected(true);

        JPanel priPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        priPanel.setBackground(Color.WHITE);
        priPanel.add(priLowBtn);
        priPanel.add(priMidBtn);
        priPanel.add(priHighBtn);

        formPanel.add(priPanel, gbc);

        add(formPanel, BorderLayout.NORTH);

        // 중앙: 내용
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(0, 20, 0, 20));
        
        contentPanel.add(new JLabel("일정 내용"), BorderLayout.NORTH);

        contentArea = new JTextArea(8, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(Theme.FONT_REGULAR_14);
        contentArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 내부 여백
        
        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER)); // 테두리 스타일
        contentPanel.add(scroll, BorderLayout.CENTER);

        completedCheck = new JCheckBox("완료 여부");
        completedCheck.setBackground(Color.WHITE);
        completedCheck.setSelected(false);
        contentPanel.add(completedCheck, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        // 하단 버튼
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveButton = new JButton("저장");
        JButton cancelButton = new JButton("취소");
        
        Theme.styleButton(saveButton);
        Theme.styleButton(cancelButton);
        cancelButton.setBackground(Theme.TEXT_SUB); // 취소 버튼은 회색으로

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 리스너
        saveButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            String start = startDateField.getText().trim();
            String end   = endDateField.getText().trim();
            boolean completed = completedCheck.isSelected();


            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "제목을 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
                return;
            }

            //  날짜/시간 비어 있으면 null 처리
            if (start.isEmpty()) start = null;
            if (end.isEmpty())   end   = null;

            // 날짜만 쓴 경우("yyyy-MM-dd")에는 00:00 붙여주기
            if (start != null && start.length() == 10) { // yyyy-MM-dd
                start = start + " 00:00";
            }
            if (end != null && end.length() == 10) {
                end = end + " 00:00";
            }

            //  priority 값 추출
            int priority = 2; // 기본값
            if (priLowBtn.isSelected()) priority = 1;
            if (priMidBtn.isSelected()) priority = 2;
            if (priHighBtn.isSelected()) priority = 3;

            if (editMode) {
                workingCopy.setTitle(title);
                workingCopy.setContent(content);
                workingCopy.setStartDate(start);
                workingCopy.setEndDate(end);
                workingCopy.setCompleted(completed);
                workingCopy.setPriority(priority); 
                resultTask = workingCopy;
            } else {
                Task newTask = new Task(title, content, start, end);
                newTask.setCompleted(completed);
                newTask.setPriority(priority); 
                resultTask = newTask;
            }
            dispose();
        });

        cancelButton.addActionListener(e -> {
            resultTask = null;
            dispose();
        });
    }

    public TaskDialog(Frame owner, LocalDate selectedDate) {
        super(owner, true);
        this.editMode = false;
        buildUI(selectedDate, "새 할 일 추가");
    }

    public TaskDialog(Frame owner, Task taskToEdit) {
        super(owner, true);
        this.editMode = true;
        this.workingCopy = new Task(taskToEdit);

        buildUI(LocalDate.now(), "할 일 수정");
        titleField.setText(workingCopy.getTitle());
        contentArea.setText(workingCopy.getContent());
        startDateField.setText(workingCopy.getStartDate() == null ? "" : workingCopy.getStartDate());
        endDateField.setText(workingCopy.getEndDate() == null ? "" : workingCopy.getEndDate());
        completedCheck.setSelected(workingCopy.isCompleted());

        //  기존 priority 값 선택 적용
        int pri = workingCopy.getPriority();
        if (pri == 1) priLowBtn.setSelected(true);
        else if (pri == 3) priHighBtn.setSelected(true);
        else priMidBtn.setSelected(true); // 나머지는 보통
    }

    public Task getTask() {
        return resultTask;
    }
}
