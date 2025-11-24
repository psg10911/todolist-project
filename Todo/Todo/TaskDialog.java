package Todo;

import com.toedter.calendar.JDateChooser; // ★ JCalendar 라이브러리 import
import com.toedter.calendar.JTextFieldDateEditor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TaskDialog extends JDialog {

    private JTextField titleField;
    private JTextArea contentArea;
    
    // 커스텀 날짜+시간 선택기
    private DateTimeSelector startSelector;
    private DateTimeSelector endSelector;
    
    private JCheckBox completedCheck;

    private JRadioButton priLowBtn;     
    private JRadioButton priMidBtn;     
    private JRadioButton priHighBtn;    

    private boolean editMode;
    private Task workingCopy;
    private Task resultTask = null;

    // DB 저장용 포맷
    private static final DateTimeFormatter FULL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private void buildUI(LocalDate defaultDate, String dialogTitle) {
        setTitle(dialogTitle);
        setModal(true);
        setSize(500, 550); 
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(Color.WHITE);

        // 상단 폼
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5); 

        // 1. 제목
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.15;
        formPanel.add(new JLabel("일정 제목"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.85;
        titleField = new JTextField();
        Theme.styleTextField(titleField);
        formPanel.add(titleField, gbc);

        // 기본 시간 계산
        LocalDateTime baseTime = (defaultDate != null) 
                ? defaultDate.atTime(9, 0) 
                : LocalDateTime.now();
        
        // 2. 시작일 (JDateChooser + Spinner)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.15;
        formPanel.add(new JLabel("시작일"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.85;
        startSelector = new DateTimeSelector(baseTime);
        formPanel.add(startSelector, gbc);

        // 3. 종료일 (JDateChooser + Spinner)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.15;
        formPanel.add(new JLabel("종료일"), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.85;
        endSelector = new DateTimeSelector(baseTime.plusHours(1));
        formPanel.add(endSelector, gbc);

        // 4. 중요도
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.15;
        formPanel.add(new JLabel("중요도"), gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.85;
        
        JPanel priPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        priPanel.setBackground(Color.WHITE);
        
        priLowBtn = new JRadioButton("높음");
        priMidBtn = new JRadioButton("보통");
        priHighBtn = new JRadioButton("낮음");
        
        priLowBtn.setBackground(Color.WHITE);
        priMidBtn.setBackground(Color.WHITE);
        priHighBtn.setBackground(Color.WHITE);

        ButtonGroup group = new ButtonGroup();
        group.add(priLowBtn);
        group.add(priMidBtn);
        group.add(priHighBtn);

        priMidBtn.setSelected(true); // 기본

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
        contentArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
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
        cancelButton.setBackground(Theme.TEXT_SUB);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 리스너
        saveButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            boolean completed = completedCheck.isSelected();
            
            // Selector에서 완성된 문자열 가져오기
            String start = startSelector.getDateTimeString();
            String end = endSelector.getDateTimeString();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "제목을 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (start == null || end == null) {
                JOptionPane.showMessageDialog(this, "날짜를 올바르게 선택해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 날짜 선후 관계 체크
            if (start.compareTo(end) > 0) {
                 JOptionPane.showMessageDialog(this, "종료일이 시작일보다 빠를 수 없습니다.", "경고", JOptionPane.WARNING_MESSAGE);
                 return;
            }

            int priority = 2;
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

        buildUI(null, "할 일 수정");
        
        titleField.setText(workingCopy.getTitle());
        contentArea.setText(workingCopy.getContent());
        completedCheck.setSelected(workingCopy.isCompleted());
        
        // Selector에 값 채워넣기
        startSelector.setDateTimeString(workingCopy.getStartDate());
        endSelector.setDateTimeString(workingCopy.getEndDate());

        int pri = workingCopy.getPriority();
        if (pri == 1) priLowBtn.setSelected(true);
        else if (pri == 3) priHighBtn.setSelected(true);
        else priMidBtn.setSelected(true);
    }

    public Task getTask() {
        return resultTask;
    }

    // =================================================================================
    // ★ JCalendar(JDateChooser) + JSpinner(Time) 조합 컴포넌트
    // =================================================================================
    private class DateTimeSelector extends JPanel {
        
        // 라이브러리 컴포넌트 사용
        private JDateChooser dateChooser; 
        private JSpinner timeSpinner;

        public DateTimeSelector(LocalDateTime initDateTime) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
            setBackground(Color.WHITE);

            // 1. 날짜 선택기 (JDateChooser)
            dateChooser = new JDateChooser();
            dateChooser.setDateFormatString("yyyy-MM-dd"); // 표시 형식
            dateChooser.setPreferredSize(new Dimension(130, 30));
            
            // 초기값 설정 (LocalDateTime -> Date 변환)
            Date initDateVal = Date.from(initDateTime.atZone(ZoneId.systemDefault()).toInstant());
            dateChooser.setDate(initDateVal);
            
            // ★ 핵심: 날짜 텍스트 필드 수정 불가능하게 설정 (오타 방지) [cite: 10, 14]
            JTextFieldDateEditor dateEditor = (JTextFieldDateEditor) dateChooser.getDateEditor();
            dateEditor.setEditable(false); 
            // 테마 적용 (선택 사항)
            dateEditor.setBackground(new Color(245, 246, 250));
            dateEditor.setFont(Theme.FONT_REGULAR_14);

            // 2. 시간 선택기 (Spinner)
            SpinnerDateModel timeModel = new SpinnerDateModel();
            timeModel.setValue(initDateVal);
            
            timeSpinner = new JSpinner(timeModel);
            JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
            timeSpinner.setEditor(timeEditor);
            timeSpinner.setPreferredSize(new Dimension(70, 30));
            timeSpinner.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

            // ★ 핵심: 시간 텍스트 필드 수정 불가능하게 설정
            JFormattedTextField timeTextField = ((JSpinner.DefaultEditor) timeSpinner.getEditor()).getTextField();
            timeTextField.setEditable(false);
            timeTextField.setFont(Theme.FONT_REGULAR_14);
            timeTextField.setHorizontalAlignment(SwingConstants.CENTER);
            timeTextField.setBackground(new Color(245, 246, 250));

            add(dateChooser);
            add(new JLabel("  ")); // 간격
            add(timeSpinner);
        }

        // 선택된 날짜+시간을 합쳐서 "yyyy-MM-dd HH:mm" 문자열로 반환
        public String getDateTimeString() {
            Date d = dateChooser.getDate();
            if (d == null) return null;

            // Date -> LocalDate
            LocalDate ld = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // Spinner -> LocalTime
            Date t = (Date) timeSpinner.getValue();
            LocalTime lt = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault()).toLocalTime();

            return LocalDateTime.of(ld, lt).format(FULL_FMT);
        }

        // DB에서 불러온 문자열을 UI에 세팅
        public void setDateTimeString(String str) {
            if (str == null || str.isEmpty()) return;
            try {
                if (str.length() > 16) str = str.substring(0, 16);
                LocalDateTime ldt = LocalDateTime.parse(str, FULL_FMT);
                
                Date dateVal = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                
                dateChooser.setDate(dateVal);
                timeSpinner.setValue(dateVal);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}