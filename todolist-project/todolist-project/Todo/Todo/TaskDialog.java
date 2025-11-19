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

    // 중요도 버튼 필드
    private ButtonGroup importanceGroup;
    private JRadioButton prio1, prio2, prio3;

    private Task task = null; // 저장 버튼을 누르면 이 객체가 생성됨

    public TaskDialog(Frame owner, LocalDate selectedDate) {
        super(owner, "할일 추가하기", true); // true = Modal 팝업

        // 새 컴포넌트가 들어갈 수 있도록 높이 살짝 증가 (350 -> 390)
        setSize(400, 390);// 팝업창 크기 설정
        setLocationRelativeTo(owner);// 화면 중앙에 팝업창 배치
        setLayout(new BorderLayout(10, 10));// 패널 간격 설정

        // 1. 상단 입력 폼 패널 (GridLayout) - 변경 없음
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 10));// 3행 2열, 가로 5, 세로 10 간격
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));// 패널 여백 설정

        formPanel.add(new JLabel("일정 제목:"));// 제목 라벨
        titleField = new JTextField();// 제목 입력 필드
        formPanel.add(titleField);// 제목 입력 필드 추가

        Date defaultDate = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());// 선택된 날짜를 Date 객체로 변환

        formPanel.add(new JLabel("시작일:"));// 시작일 라벨
        startDateChooser = new JDateChooser(defaultDate);// 시작일 선택기
        startDateChooser.setDateFormatString("yyyy-MM-dd");// 날짜 형식 설정
        formPanel.add(startDateChooser);// 시작일 선택기 추가

        formPanel.add(new JLabel("종료일:"));// 종료일 라벨
        endDateChooser = new JDateChooser(defaultDate);// 종료일 선택기
        endDateChooser.setDateFormatString("yyyy-MM-dd");// 날짜 형식 설정
        formPanel.add(endDateChooser);// 종료일 선택기 추가

        add(formPanel, BorderLayout.NORTH);// 상단 폼 패널 추가

        // 2. 중앙 내용(Content) 패널
        JPanel contentPanel = new JPanel(new BorderLayout(0, 5));// 내용 패널 (세로 5 간격)
        contentPanel.setBorder(new EmptyBorder(0, 10, 0, 10));// 패널 여백 설정

         // 내용 라벨
        contentPanel.add(new JLabel("일정 내용(최대 50자):"), BorderLayout.NORTH);// 내용 라벨 추가

        // JTextArea 크기 축소 (5줄 -> 3줄)
        contentArea = new JTextArea(3, 20);// 내용 입력 영역 (3줄)
        contentArea.setLineWrap(true); // 자동 줄바꿈

        // 50자 제한 DocumentFilter 적용
        ((AbstractDocument) contentArea.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                // (현재 문서 길이) - (삭제될 글자 수) + (삽입될 글자 수)
                int currentLength = fb.getDocument().getLength();// 현재 문서 길이
                int newLength = currentLength - length + (text == null ? 0 : text.length());// 변경 후 길이 계산

                if (newLength <= 50) {
                    // 50자 이내일 경우에만 변경 허용
                    super.replace(fb, offset, length, text, attrs);// 실제 변경 수행
                }
                // 50자를 넘으면 아무것도 하지 않음 (입력/붙여넣기 무시)
            }
        });

        contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);// 내용 입력 영역 추가

        // [수정 3] 중요도 라디오버튼 패널 생성
        JPanel importancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));// 중요도 패널
        // TitledBorder를 사용해 그룹 이름 표시
        importancePanel.setBorder(BorderFactory.createTitledBorder("중요도"));// 중요도 그룹 테두리

        importanceGroup = new ButtonGroup(); // 하나만 선택되도록 그룹화
        prio1 = new JRadioButton("1 (낮음)");// 낮음
        prio2 = new JRadioButton("2 (보통)");// 보통
        prio3 = new JRadioButton("3 (높음)");// 높음

         // '2 (보통)'을 기본 선택값으로 설정

        prio2.setSelected(true); // '2 (보통)'을 기본값으로 선택

        importanceGroup.add(prio1);// 그룹에 라디오버튼 추가
        importanceGroup.add(prio2);// 그룹에 라디오버튼 추가
        importanceGroup.add(prio3);// 그룹에 라디오버튼 추가

        importancePanel.add(prio1);// 중요도 1
        importancePanel.add(prio2);// 중요도 2
        importancePanel.add(prio3);// 중요도 3

        // [수정 3] 중요도 패널을 contentPanel의 아래쪽(SOUTH)에 추가
        contentPanel.add(importancePanel, BorderLayout.SOUTH);// 중요도 패널 추가

        add(contentPanel, BorderLayout.CENTER);// 중앙 내용 패널 추가

        // 3. 하단 버튼 패널 - 변경 없음
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));// 버튼 패널 (오른쪽 정렬)
        JButton saveButton = new JButton("Save");// 저장 버튼
        JButton cancelButton = new JButton("Cancel");// 취소 버튼
        buttonPanel.add(saveButton);// 저장 버튼 추가
        buttonPanel.add(cancelButton);// 취소 버튼 추가
        add(buttonPanel, BorderLayout.SOUTH);// 하단 버튼 패널 추가

        // 'Save' 버튼 리스너 - 변경 없음 (유효성 검사 등)
        saveButton.addActionListener(e -> {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "제목을 입력해주세요.", "Warning", JOptionPane.WARNING_MESSAGE);// 경고창 표시
                return;
            }

            Date utilStartDate = startDateChooser.getDate();// 시작일 Date 객체
            Date utilEndDate = endDateChooser.getDate();// 종료일 Date 객체

            if (utilStartDate == null || utilEndDate == null) {
                JOptionPane.showMessageDialog(this, "시작일과 종료일을 모두 선택해주세요.", "Warning", JOptionPane.WARNING_MESSAGE);// 경고창 표시
                return;
            }

            LocalDate startDate = utilStartDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();// 시작일 LocalDate 객체
            LocalDate endDate = utilEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();// 종료일 LocalDate 객체

             // 시작일이 종료일보다 늦은 경우 오류 메시지 표시

            if (startDate.isAfter(endDate)) {
                JOptionPane.showMessageDialog(this,
                        "오류: 시작일이 종료일보다 늦습니다.",
                        "날짜 오류",
                        JOptionPane.ERROR_MESSAGE);// 오류 메시지 표시
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
