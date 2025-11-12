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

    private JTextField titleField;// 일정 제목
    private JTextArea contentArea;// 일정 내용
    private JDateChooser startDateChooser;// 시작일
    private JDateChooser endDateChooser;// 종료일

    private ButtonGroup importanceGroup;// 중요도 라디오 버튼 그룹
    private JRadioButton prio1, prio2, prio3;// 중요도 라디오 버튼들

    private Task task = null;// 생성되거나 수정된 할 일 객체
    private boolean isEditMode; // 수정 모드 여부

    public TaskDialog(Frame owner, LocalDate selectedDate, boolean isEditMode) {
        super(owner, "할 일 관리", true);// 모달 다이얼로그 설정
        this.isEditMode = isEditMode;// 수정 모드 설정

        setSize(400, 400);// 다이얼로그 크기 설정
        setLocationRelativeTo(owner);// 소유자 프레임 중앙에 위치
        setLayout(new BorderLayout(10, 10));// 레이아웃 설정

        // 상단 입력폼
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 10));// 3행 2열 그리드 레이아웃
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));// 여백 설정

        formPanel.add(new JLabel("일정 제목:"));// 제목 라벨
        titleField = new JTextField();// 제목 입력 필드
        formPanel.add(titleField);// 제목 필드 추가

        Date defaultDate = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());// 선택된 날짜를 기본값으로 설정

        formPanel.add(new JLabel("시작일:"));// 시작일 라벨
        startDateChooser = new JDateChooser(defaultDate);// 시작일 선택기
        startDateChooser.setDateFormatString("yyyy-MM-dd");// 날짜 형식 설정
        formPanel.add(startDateChooser);// 시작일 선택기 추가

        formPanel.add(new JLabel("종료일:"));// 종료일 라벨
        endDateChooser = new JDateChooser(defaultDate);// 종료일 선택기
        endDateChooser.setDateFormatString("yyyy-MM-dd");// 날짜 형식 설정
        formPanel.add(endDateChooser);// 종료일 선택기 추가

        add(formPanel, BorderLayout.NORTH);// 상단에 입력폼 추가

        // 중앙 내용
        JPanel contentPanel = new JPanel(new BorderLayout(0, 5));// 내용 패널
        contentPanel.setBorder(new EmptyBorder(0, 10, 0, 10));// 여백 설정
        contentPanel.add(new JLabel("일정 내용(최대 50자):"), BorderLayout.NORTH);// 내용 라벨

        contentArea = new JTextArea(3, 20);// 내용 입력 영역
        contentArea.setLineWrap(true);// 자동 줄바꿈
        ((AbstractDocument) contentArea.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                int currentLength = fb.getDocument().getLength();// 현재 길이
                int newLength = currentLength - length + (text == null ? 0 : text.length());// 변경 후 길이 계산
                if (newLength <= 50)// 최대 50자 제한
                    super.replace(fb, offset, length, text, attrs);// 허용된 경우에만 교체 수행
            }
        });
        contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);// 스크롤 가능한 내용 영역 추가

        // 중요도
        JPanel importancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));// 중요도 패널
        importancePanel.setBorder(BorderFactory.createTitledBorder("중요도"));// 테두리 제목 설정
        importanceGroup = new ButtonGroup();// 중요도 버튼 그룹
        prio1 = new JRadioButton("1 (낮음)");// 낮음
        prio2 = new JRadioButton("2 (보통)");// 보통
        prio3 = new JRadioButton("3 (높음)");// 높음
        prio2.setSelected(true);// 기본값: 보통

        importanceGroup.add(prio1);// 그룹에 버튼 추가
        importanceGroup.add(prio2);// 그룹에 버튼 추가
        importanceGroup.add(prio3);// 그룹에 버튼 추가

        importancePanel.add(prio1);// 낮음
        importancePanel.add(prio2);// 기본값: 보통
        importancePanel.add(prio3);// 높음
        contentPanel.add(importancePanel, BorderLayout.SOUTH);// 중요도 패널 추가
        add(contentPanel, BorderLayout.CENTER);// 중앙에 내용 패널 추가

        // 하단 버튼 (저장 / 삭제 / 취소)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));// 버튼 패널
        JButton saveButton = new JButton("저장");// 저장
        JButton deleteButton = new JButton("삭제");// 삭제
        JButton cancelButton = new JButton("취소");// 취소

        buttonPanel.add(saveButton);// 항상 "저장" 표시
        if (isEditMode)
            buttonPanel.add(deleteButton); // 수정 모드일 때만 "삭제" 표시
        buttonPanel.add(cancelButton);// 항상 "취소" 표시
        add(buttonPanel, BorderLayout.SOUTH);// 하단에 버튼 패널 추가

        // 저장
        saveButton.addActionListener(e -> {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "제목을 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Date utilStartDate = startDateChooser.getDate();
            Date utilEndDate = endDateChooser.getDate();// 선택된 날짜 가져오기
            if (utilStartDate == null || utilEndDate == null) {
                JOptionPane.showMessageDialog(this, "시작일과 종료일을 모두 선택해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate startDate = utilStartDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();// Date ->
                                                                                                         // LocalDate 변환
            LocalDate endDate = utilEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();// Date ->
                                                                                                     // LocalDate 변환
            if (startDate.isAfter(endDate)) {
                JOptionPane.showMessageDialog(this, "시작일이 종료일보다 늦습니다.", "오류", JOptionPane.ERROR_MESSAGE);// 오류 메시지
                return;
            }

            int selectedPriority = 2;// 기본값: 보통
            if (prio1.isSelected())
                selectedPriority = 1;// 낮음
            else if (prio3.isSelected())
                selectedPriority = 3;// 높음

            this.task = new Task(
                    titleField.getText(), // 일정 제목
                    contentArea.getText(), // 일정 내용
                    startDate.format(DateTimeFormatter.ISO_LOCAL_DATE), // 시작일
                    endDate.format(DateTimeFormatter.ISO_LOCAL_DATE), // 종료일
                    selectedPriority);// 중요도
            dispose();// 다이얼로그 닫기
        });

        // 삭제
        deleteButton.addActionListener(e -> {
            this.task = new Task("__DELETE__", "", "", "", 0);// 삭제 신호용 특수 Task 객체
            dispose();// 다이얼로그 닫기
        });

        // 취소
        cancelButton.addActionListener(e -> {
            this.task = null;//
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