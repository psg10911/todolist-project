package Todo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import com.toedter.calendar.JDateChooser;

// [추가] DocumentFilter (글자 수 제한)
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

// [추가] RadioButton (중요도)
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

/**
 * 할 일을 추가하기 위한 모달 JDialog (팝업창).
 * (간단한 GridLayout과 BorderLayout 사용)
 */
public class TaskDialog extends JDialog {

    private JTextField titleField;
    private JTextArea contentArea;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;

    // [추가] 중요도 버튼 필드
    private ButtonGroup importanceGroup;
    private JRadioButton prio1, prio2, prio3;

    private Task task = null; // 저장 버튼을 누르면 이 객체가 생성됨

    public TaskDialog(Frame owner, LocalDate selectedDate) {
        super(owner, "할일 추가하기", true); // true = Modal 팝업

        // [수정] 새 컴포넌트가 들어갈 수 있도록 높이 살짝 증가 (350 -> 390)
        setSize(400, 390);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // 1. 상단 입력 폼 패널 (GridLayout) - 변경 없음
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

        // 2. 중앙 내용(Content) 패널 [대폭 수정]
        JPanel contentPanel = new JPanel(new BorderLayout(0, 5));
        contentPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        contentPanel.add(new JLabel("일정 내용(최대 50자):"), BorderLayout.NORTH);

        // [수정 1] JTextArea 크기 축소 (5줄 -> 3줄)
        contentArea = new JTextArea(3, 20);
        contentArea.setLineWrap(true); // 자동 줄바꿈

        // [수정 2] 50자 제한 DocumentFilter 적용
        ((AbstractDocument) contentArea.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                // (현재 문서 길이) - (삭제될 글자 수) + (삽입될 글자 수)
                int currentLength = fb.getDocument().getLength();
                int newLength = currentLength - length + (text == null ? 0 : text.length());

                if (newLength <= 50) {
                    // 50자 이내일 경우에만 변경 허용
                    super.replace(fb, offset, length, text, attrs);
                }
                // 50자를 넘으면 아무것도 하지 않음 (입력/붙여넣기 무시)
            }
        });

        contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        // [수정 3] 중요도 라디오버튼 패널 생성
        JPanel importancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        // TitledBorder를 사용해 그룹 이름 표시
        importancePanel.setBorder(BorderFactory.createTitledBorder("중요도"));

        importanceGroup = new ButtonGroup(); // 하나만 선택되도록 그룹화
        prio1 = new JRadioButton("1 (낮음)");
        prio2 = new JRadioButton("2 (보통)");
        prio3 = new JRadioButton("3 (높음)");

        prio2.setSelected(true); // '2 (보통)'을 기본값으로 선택

        importanceGroup.add(prio1);
        importanceGroup.add(prio2);
        importanceGroup.add(prio3);

        importancePanel.add(prio1);
        importancePanel.add(prio2);
        importancePanel.add(prio3);

        // [수정 3] 중요도 패널을 contentPanel의 아래쪽(SOUTH)에 추가
        contentPanel.add(importancePanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        // 3. 하단 버튼 패널 - 변경 없음
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 'Save' 버튼 리스너 - 변경 없음 (유효성 검사 등)
        saveButton.addActionListener(e -> {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "제목을 입력해주세요.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Date utilStartDate = startDateChooser.getDate();
            Date utilEndDate = endDateChooser.getDate();

            if (utilStartDate == null || utilEndDate == null) {
                JOptionPane.showMessageDialog(this, "시작일과 종료일을 모두 선택해주세요.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate startDate = utilStartDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDate = utilEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (startDate.isAfter(endDate)) {
                JOptionPane.showMessageDialog(this,
                        "오류: 시작일이 종료일보다 늦습니다.",
                        "날짜 오류",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // (참고: 현재 '중요도' 값은 Task 객체에 저장되지 않습니다.)

            this.task = new Task(
                    titleField.getText(),
                    contentArea.getText(), // 50자로 제한된 내용이 저장됨
                    startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
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