package Todo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 할 일 추가/수정 다이얼로그.
 * - 추가 모드: 기본 날짜로 빈 폼을 보여주고 Save 시 새 Task 반환
 * - 수정 모드: 전달받은 Task의 "복사본(working copy)"로 폼을 채워 편집, Save 시 복사본 반환
 *   (Cancel 시 원본은 절대 바뀌지 않음)
 */
public class TaskDialog extends JDialog {

    private JTextField titleField;
    private JTextArea contentArea;
    private JTextField startDateField;
    private JTextField endDateField;
    private JCheckBox completedCheck;

    private boolean editMode;
    private Task workingCopy;       // 수정 모드에서 편집하는 복사본
    private Task resultTask = null; // Save면 반환할 Task, Cancel이면 null

    // ===== 공통 UI 빌더 =====
    private void buildUI(LocalDate defaultDate, String dialogTitle) {
        setTitle(dialogTitle);
        setModal(true);
        setSize(420, 400);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(getOwner());

        // 상단 폼
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 6, 10));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("일정 제목 (Title):"));
        titleField = new JTextField();
        formPanel.add(titleField);

        String defaultDateStr = (defaultDate != null)
                ? defaultDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        formPanel.add(new JLabel("시작일 (Start Date):"));
        startDateField = new JTextField(defaultDateStr);
        formPanel.add(startDateField);

        formPanel.add(new JLabel("종료일 (End Date):"));
        endDateField = new JTextField(defaultDateStr);
        formPanel.add(endDateField);

        add(formPanel, BorderLayout.NORTH);

        // 중앙: 내용 + 완료 체크
        JPanel contentPanel = new JPanel(new BorderLayout(0, 6));
        contentPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        contentPanel.add(new JLabel("일정 내용 (Content):"), BorderLayout.NORTH);

        contentArea = new JTextArea(6, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        completedCheck = new JCheckBox("완료");
        completedCheck.setSelected(false);
        contentPanel.add(completedCheck, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        // 하단 버튼
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Save 리스너 (추가/수정 분기)
        saveButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            String start = startDateField.getText().trim();
            String end   = endDateField.getText().trim();
            boolean completed = completedCheck.isSelected();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "제목을 입력해주세요.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (editMode) {
                // 수정 모드: 복사본(workingCopy)에 값 반영하고 그 복사본을 결과로 반환
                workingCopy.setTitle(title);
                workingCopy.setContent(content);
                workingCopy.setStartDate(start.isEmpty() ? null : start);
                workingCopy.setEndDate(end.isEmpty() ? null : end);
                workingCopy.setCompleted(completed);
                resultTask = workingCopy;
            } else {
                // 추가 모드: 새 Task 생성(사용자/ID는 호출 측에서 세팅)
                Task newTask = new Task(title, content, start, end);
                newTask.setCompleted(completed);
                resultTask = newTask;
            }

            dispose();
        });

        // Cancel 리스너
        cancelButton.addActionListener(e -> {
            resultTask = null;
            dispose();
        });
    }

    // ===== 생성자들 =====

    /** ▶ 추가 모드: 캘린더에서 받은 날짜로 기본값 채워서 띄움 */
    public TaskDialog(Frame owner, LocalDate selectedDate) {
        super(owner, true);
        this.editMode = false;
        buildUI(selectedDate, "Add Task");
        // (추가 모드는 폼 기본값만 사용하므로 따로 채울 값 없음)
    }

    /** ▶ 수정 모드: 원본 Task로부터 복사본을 만들어 편집 */
    public TaskDialog(Frame owner, Task taskToEdit) {
        super(owner, true);
        this.editMode = true;
        // 원본을 보호하기 위해 복사본(워킹카피) 생성
        this.workingCopy = new Task(taskToEdit);

        buildUI(LocalDate.now(), "할 일 수정"); // UI 먼저 만들고…
        // …복사본 값으로 폼을 채움 (원본은 오염되지 않음)
        titleField.setText(workingCopy.getTitle());
        contentArea.setText(workingCopy.getContent());
        startDateField.setText(workingCopy.getStartDate() == null ? "" : workingCopy.getStartDate());
        endDateField.setText(workingCopy.getEndDate() == null ? "" : workingCopy.getEndDate());
        completedCheck.setSelected(workingCopy.isCompleted());
    }

    /** 호출자가 Save/Cancel 결과를 가져가는 메서드: Save면 Task, Cancel이면 null */
    public Task getTask() {
        return resultTask;
    }
}