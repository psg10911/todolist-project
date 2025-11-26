package Todo;

import com.toedter.calendar.JDateChooser; 
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
import java.util.List;

public class TaskDialog extends JDialog {

    private JTextField titleField;
    private JTextArea contentArea;
    
    private JPanel datePanel;
    private DateTimeSelector startSelector;
    private DateTimeSelector endSelector;
    
    private JCheckBox multiDayCheck; 
    private JCheckBox completedCheck;
    private JRadioButton priLowBtn, priMidBtn, priHighBtn;    

    private boolean editMode;
    private Task workingCopy;
    private Task resultTask = null;
    private String currentUserId; 
    private LocalDate baseDate; 

    private static final DateTimeFormatter FULL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TaskDialog(Frame owner, LocalDate selectedDate, String userId) {
        super(owner, true);
        this.editMode = false;
        this.currentUserId = userId;
        this.baseDate = selectedDate != null ? selectedDate : LocalDate.now();
        buildUI("새 할 일 추가");
    }

    public TaskDialog(Frame owner, Task taskToEdit) {
        super(owner, true);
        this.editMode = true;
        this.workingCopy = taskToEdit.copy();
        this.currentUserId = taskToEdit.getUserId();
        
        try {
            String s = taskToEdit.getStartDate();
            if (s.length() > 10) s = s.substring(0, 10);
            this.baseDate = LocalDate.parse(s);
        } catch (Exception e) {
            this.baseDate = LocalDate.now();
        }

        buildUI("할 일 수정");
        
        titleField.setText(workingCopy.getTitle());
        contentArea.setText(workingCopy.getContent());
        completedCheck.setSelected(workingCopy.isCompleted());
        
        LocalDateTime start = parseDateTime(workingCopy.getStartDate());
        LocalDateTime end = parseDateTime(workingCopy.getEndDate());
        
        boolean isMultiDay = !start.toLocalDate().equals(end.toLocalDate());
        multiDayCheck.setSelected(isMultiDay);
        toggleDateMode(isMultiDay);

        startSelector.setDateTime(start);
        endSelector.setDateTime(end);

        Priority pri = workingCopy.getPriority();
        if (pri == Priority.LOW) priLowBtn.setSelected(true);
        else if (pri == Priority.HIGH) priHighBtn.setSelected(true);
        else priMidBtn.setSelected(true);
    }

    private void buildUI(String dialogTitle) {
        setTitle(dialogTitle);
        setModal(true);
        setSize(500, 600); 
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(Color.WHITE);

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

        // 2. 기간 설정 체크박스
        gbc.gridx = 1; gbc.gridy = 1;
        multiDayCheck = new JCheckBox("하루 이상 (기간 설정)");
        multiDayCheck.setBackground(Color.WHITE);
        multiDayCheck.setFont(Theme.FONT_REGULAR_12);
        multiDayCheck.addActionListener(e -> toggleDateMode(multiDayCheck.isSelected()));
        formPanel.add(multiDayCheck, gbc);

        // 3. 시작/종료 선택기
        LocalDateTime baseStart = baseDate.atTime(9, 0);
        LocalDateTime baseEnd = baseDate.atTime(10, 0);

        startSelector = new DateTimeSelector(baseStart);
        endSelector = new DateTimeSelector(baseEnd);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.15;
        formPanel.add(new JLabel("시작"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.85;
        formPanel.add(startSelector, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.15;
        formPanel.add(new JLabel("종료"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.85;
        formPanel.add(endSelector, gbc);

        toggleDateMode(false); // 초기 모드 설정

        // 4. 중요도
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.15;
        formPanel.add(new JLabel("중요도"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.85;
        
        JPanel priPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        priPanel.setBackground(Color.WHITE);
        priLowBtn = new JRadioButton("높음");
        priMidBtn = new JRadioButton("보통");
        priHighBtn = new JRadioButton("낮음");
        priLowBtn.setBackground(Color.WHITE);
        priMidBtn.setBackground(Color.WHITE);
        priHighBtn.setBackground(Color.WHITE);

        ButtonGroup group = new ButtonGroup();
        group.add(priLowBtn); group.add(priMidBtn); group.add(priHighBtn);
        priMidBtn.setSelected(true);

        priPanel.add(priLowBtn); priPanel.add(priMidBtn); priPanel.add(priHighBtn);
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

        // === 저장 로직 ===
        saveButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "제목을 입력해주세요.");
                return;
            }

            LocalDateTime startDT, endDT;
            boolean isMulti = multiDayCheck.isSelected();

            if (isMulti) {
                LocalDate d1 = startSelector.getSelectedDate();
                LocalDate d2 = endSelector.getSelectedDate();
                if (d1 == null || d2 == null) {
                     JOptionPane.showMessageDialog(this, "날짜를 선택해주세요.");
                     return;
                }
                startDT = d1.atTime(12,0,0); // 시작 기간이 -1일 되는 문제 방지
                endDT = d2.atTime(23, 59, 59); 
            } else {
                LocalTime t1 = startSelector.getSelectedTime();
                LocalTime t2 = endSelector.getSelectedTime();
                startDT = baseDate.atTime(t1);
                endDT = baseDate.atTime(t2);
            }

            if (startDT.isAfter(endDT)) {
                JOptionPane.showMessageDialog(this, "종료 시간이 시작 시간보다 빠를 수 없습니다.");
                return;
            }

            // 중복 검사
            int currentId = (editMode && workingCopy != null) ? workingCopy.getId() : 0;
            
            // 겹침 검사
            if (isTimeOverlapping(currentUserId, startDT, endDT, currentId)) {
                JOptionPane.showMessageDialog(this, "해당 시간에 이미 일정이 있습니다. (기간 일정은 겹칠 수 있습니다.)");
                return;
            }

            String startStr = startDT.format(FULL_FMT);
            String endStr = endDT.format(FULL_FMT);
            Priority priority = priLowBtn.isSelected() ? Priority.LOW : (priHighBtn.isSelected() ? Priority.HIGH : Priority.MEDIUM);
            boolean completed = completedCheck.isSelected();
            String content = contentArea.getText().trim();

            if (editMode) {
                workingCopy.setTitle(title);
                workingCopy.setContent(content);
                workingCopy.setStartDate(startStr);
                workingCopy.setEndDate(endStr);
                workingCopy.setCompleted(completed);
                workingCopy.setPriority(priority); 
                resultTask = workingCopy;
            } else {
                if (isMulti) {
                    // 기간 일정이면 PeriodTask
                    resultTask = new PeriodTask(title, content, startStr, endStr);
                } else {
                    // 시간 일정이면 TimeTask
                    resultTask = new TimeTask(title, content, startStr, endStr);
                }
                
                resultTask.setCompleted(completed);
                resultTask.setPriority(priority); 
            }
            dispose();
        });

        cancelButton.addActionListener(e -> {
            resultTask = null;
            dispose();
        });
    }

    private void toggleDateMode(boolean isMultiDay) {
        startSelector.setMode(isMultiDay);
        endSelector.setMode(isMultiDay);
    }

    private boolean isTimeOverlapping(String userId, LocalDateTime newStart, LocalDateTime newEnd, int excludeId) {
        boolean isNewMultiDay = !newStart.toLocalDate().equals(newEnd.toLocalDate());
        if (isNewMultiDay) {
            return false; 
        }

        List<Task> tasks = TodoDao.findByRange(userId, newStart.minusDays(1), newEnd.plusDays(1));
        
        for (Task t : tasks) {
            if (t.getId() == excludeId) continue;
            
            try {
                LocalDateTime tStart = parseDateTime(t.getStartDate());
                LocalDateTime tEnd = parseDateTime(t.getEndDate());
                
                boolean isExistingMultiDay = !tStart.toLocalDate().equals(tEnd.toLocalDate());
                if (isExistingMultiDay) {
                    continue;
                }

                if (newStart.isBefore(tEnd) && newEnd.isAfter(tStart)) {
                    return true; 
                }
            } catch (Exception ex) {}
        }
        return false;
    }

    private LocalDateTime parseDateTime(String str) {
        if (str.length() > 16) str = str.substring(0, 16);
        return LocalDateTime.parse(str, FULL_FMT);
    }

    public Task getTask() { return resultTask; }

    private class DateTimeSelector extends JPanel {
        private JDateChooser dateChooser; 
        private JSpinner timeSpinner;

        public DateTimeSelector(LocalDateTime initDateTime) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setBackground(Color.WHITE);

            dateChooser = new JDateChooser();
            dateChooser.setDateFormatString("yyyy-MM-dd");
            dateChooser.setPreferredSize(new Dimension(130, 30));
            
            JTextFieldDateEditor dateEditor = (JTextFieldDateEditor) dateChooser.getDateEditor();
            dateEditor.setEditable(false); 
            dateEditor.setBackground(new Color(245, 246, 250));
            dateEditor.setFont(Theme.FONT_REGULAR_14);

            SpinnerDateModel timeModel = new SpinnerDateModel();
            timeSpinner = new JSpinner(timeModel);
            JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
            timeSpinner.setEditor(timeEditor);
            timeSpinner.setPreferredSize(new Dimension(80, 30));
            timeSpinner.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
            
            JFormattedTextField timeTextField = ((JSpinner.DefaultEditor) timeSpinner.getEditor()).getTextField();
            timeTextField.setEditable(false);
            timeTextField.setFont(Theme.FONT_REGULAR_14);
            timeTextField.setHorizontalAlignment(SwingConstants.CENTER);
            timeTextField.setBackground(new Color(245, 246, 250));

            setDateTime(initDateTime);

            add(dateChooser);
            add(Box.createHorizontalStrut(5));
            add(timeSpinner);
        }

        public void setMode(boolean isMultiDay) {
            dateChooser.setVisible(isMultiDay);
            timeSpinner.setVisible(!isMultiDay);
            revalidate();
            repaint();
        }

        public void setDateTime(LocalDateTime ldt) {
            Date dateVal = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
            dateChooser.setDate(dateVal);
            timeSpinner.setValue(dateVal);
        }

        public LocalDate getSelectedDate() {
            Date d = dateChooser.getDate();
            if (d == null) return null;
            return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        public LocalTime getSelectedTime() {
            Date t = (Date) timeSpinner.getValue();
            return LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault()).toLocalTime();
        }
    }
}