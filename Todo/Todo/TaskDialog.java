package Todo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TaskDialog extends JDialog {

    private JTextField titleField;
    private JTextArea contentArea;
    private JTextField startDateField;
    private JTextField endDateField;
    private JCheckBox completedCheck;

    private boolean editMode;
    private Task workingCopy;
    private Task resultTask = null;

    private void buildUI(LocalDate defaultDate, String dialogTitle) {
        setTitle(dialogTitle);
        setModal(true);
        setSize(450, 450);
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

        String defaultDateStr = (defaultDate != null)
                ? defaultDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        // 시작일
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        formPanel.add(new JLabel("시작일"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.8;
        startDateField = new JTextField(defaultDateStr);
        Theme.styleTextField(startDateField);
        formPanel.add(startDateField, gbc);

        // 종료일
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.2;
        formPanel.add(new JLabel("종료일"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.8;
        endDateField = new JTextField(defaultDateStr);
        Theme.styleTextField(endDateField);
        formPanel.add(endDateField, gbc);

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

            if (editMode) {
                workingCopy.setTitle(title);
                workingCopy.setContent(content);
                workingCopy.setStartDate(start.isEmpty() ? null : start);
                workingCopy.setEndDate(end.isEmpty() ? null : end);
                workingCopy.setCompleted(completed);
                resultTask = workingCopy;
            } else {
                Task newTask = new Task(title, content, start, end);
                newTask.setCompleted(completed);
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
    }

    public Task getTask() {
        return resultTask;
    }
}