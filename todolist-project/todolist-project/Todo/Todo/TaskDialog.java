package Todo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import com.toedter.calendar.JDateChooser;
import javax.swing.text.*;

/**
 * 할 일을 추가하거나 수정/삭제하기 위한 모달 JDialog (팝업창)
 */
public class TaskDialog extends JDialog {

    private JTextField titleField;
    private JTextArea contentArea;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;

    private ButtonGroup importanceGroup;
    private JRadioButton prio1, prio2, prio3;

    private Task task = null;
    private boolean isEditMode; // 수정 모드 여부

    public TaskDialog(Frame owner, LocalDate selectedDate, boolean isEditMode) {
        super(owner, "할 일 관리", true);
        this.isEditMode = isEditMode;

        setSize(400, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // 상단 입력폼
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 10));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("일정 제목:"));
        titleField = new JTextField();
        formPanel.add(titleField);

        Date defaultDate = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        formPanel.add(new JLabel("시작일:"));
        startDateChooser = new JDateChooser(defaultDate);
        startDateChooser.setDateFormatString("yyyy-MM-dd");
        formPanel.add(startDateChooser);

        formPanel.add(new JLabel("종료일:"));
        endDateChooser = new JDateChooser(defaultDate);
        endDateChooser.setDateFormatString("yyyy-MM-dd");
        formPanel.add(endDateChooser);

        add(formPanel, BorderLayout.NORTH);

        // 중앙 내용
        JPanel contentPanel = new JPanel(new BorderLayout(0, 5));
        contentPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        contentPanel.add(new JLabel("일정 내용(최대 50자):"), BorderLayout.NORTH);

        contentArea = new JTextArea(3, 20);
        contentArea.setLineWrap(true);
        ((AbstractDocument) contentArea.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                int currentLength = fb.getDocument().getLength();
                int newLength = currentLength - length + (text == null ? 0 : text.length());
                if (newLength <= 50)
                    super.replace(fb, offset, length, text, attrs);
            }
        });
        contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        // 중요도
        JPanel importancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        importancePanel.setBorder(BorderFactory.createTitledBorder("중요도"));
        importanceGroup = new ButtonGroup();
        prio1 = new JRadioButton("1 (낮음)");
        prio2 = new JRadioButton("2 (보통)");
        prio3 = new JRadioButton("3 (높음)");
        prio2.setSelected(true);
        importanceGroup.add(prio1);
        importanceGroup.add(prio2);
        importanceGroup.add(prio3);
        importancePanel.add(prio1);
        importancePanel.add(prio2);
        importancePanel.add(prio3);
        contentPanel.add(importancePanel, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);

        // 하단 버튼 (저장 / 삭제 / 취소)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("저장");
        JButton deleteButton = new JButton("삭제");
        JButton cancelButton = new JButton("취소");

        buttonPanel.add(saveButton);
        if (isEditMode)
            buttonPanel.add(deleteButton); // ✅ 수정 모드일 때만 표시
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 저장
        saveButton.addActionListener(e -> {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "제목을 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Date utilStartDate = startDateChooser.getDate();
            Date utilEndDate = endDateChooser.getDate();
            if (utilStartDate == null || utilEndDate == null) {
                JOptionPane.showMessageDialog(this, "시작일과 종료일을 모두 선택해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate startDate = utilStartDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDate = utilEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (startDate.isAfter(endDate)) {
                JOptionPane.showMessageDialog(this, "시작일이 종료일보다 늦습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int selectedPriority = 2;
            if (prio1.isSelected())
                selectedPriority = 1;
            else if (prio3.isSelected())
                selectedPriority = 3;

            this.task = new Task(
                    titleField.getText(),
                    contentArea.getText(),
                    startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    endDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    selectedPriority);
            dispose();
        });

        // 삭제
        deleteButton.addActionListener(e -> {
            this.task = new Task("__DELETE__", "", "", "", 0);
            dispose();
        });

        // 취소
        cancelButton.addActionListener(e -> {
            this.task = null;
            dispose();
        });
    }

    public Task getTask() {
        return this.task;
    }

    public void fillFromTask(Task t) {
        titleField.setText(t.getTitle());
        contentArea.setText(t.getContent());

        startDateChooser.setDate(Date.from(LocalDate.parse(t.getStartDate())
                .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        endDateChooser.setDate(Date.from(LocalDate.parse(t.getEndDate())
                .atStartOfDay(ZoneId.systemDefault()).toInstant()));

        switch (t.getPriority()) {
            case 1 -> prio1.setSelected(true);
            case 2 -> prio2.setSelected(true);
            case 3 -> prio3.setSelected(true);
        }
    }
}
