package Todo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CalendarPanel extends JPanel {

    private JLabel monthYearLabel;
    private JPanel calendarGridPanel;
    private LocalDate currentDate;
    private TaskPanel taskPanel;
    
    // ★ [추가] Controller 사용
    private TodoController todoController;
    
    private static final DateTimeFormatter FULL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public CalendarPanel(TaskPanel taskPanel) {
        this.taskPanel = taskPanel;
        this.currentDate = LocalDate.now();
        this.todoController = new TodoController();
        
        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.CARD_BG); 
        setBorder(new EmptyBorder(20, 20, 20, 20)); 
        
        setMinimumSize(new Dimension(350, 400));
        setPreferredSize(new Dimension(400, 0)); 

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCalendarPanel(), BorderLayout.CENTER);

        updateCalendar();
    }

    public void clear() {
        currentDate = LocalDate.now();
        updateCalendar();
    }
    
    // ... (createTopPanel, styleNavButton, changeDate, createCalendarPanel은 기존과 동일) ...
    // (기존 코드를 그대로 유지해주세요)
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.CARD_BG);

        JButton todayButton = new JButton("Today");
        styleNavButton(todayButton);

        JPanel monthNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        monthNavPanel.setBackground(Theme.CARD_BG);

        JButton prevYearBtn = new JButton("<<");
        JButton prevMonthBtn = new JButton("<");
        JButton nextMonthBtn = new JButton(">");
        JButton nextYearBtn = new JButton(">>");
        
        styleNavButton(prevYearBtn);
        styleNavButton(prevMonthBtn);
        styleNavButton(nextMonthBtn);
        styleNavButton(nextYearBtn);

        monthYearLabel = new JLabel();
        monthYearLabel.setFont(Theme.FONT_BOLD_24);
        monthYearLabel.setForeground(Theme.TEXT_MAIN);
        
        monthNavPanel.add(prevYearBtn);
        monthNavPanel.add(prevMonthBtn);
        monthNavPanel.add(monthYearLabel);
        monthNavPanel.add(nextMonthBtn);
        monthNavPanel.add(nextYearBtn);
        panel.add(monthNavPanel, BorderLayout.CENTER);
        panel.add(todayButton, BorderLayout.WEST); 

        todayButton.addActionListener(e -> changeDate(LocalDate.now()));
        prevMonthBtn.addActionListener(e -> changeDate(currentDate.minusMonths(1)));
        nextMonthBtn.addActionListener(e -> changeDate(currentDate.plusMonths(1)));
        prevYearBtn.addActionListener(e -> changeDate(currentDate.minusYears(1)));
        nextYearBtn.addActionListener(e -> changeDate(currentDate.plusYears(1)));
        
        return panel;
    }

    private void styleNavButton(JButton btn) {
        btn.setFont(Theme.FONT_BOLD_16);
        btn.setForeground(Theme.PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setBorder(null); 
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void changeDate(LocalDate newDate) {
        currentDate = newDate;
        updateCalendar();
        taskPanel.loadTasksForDate(currentDate);
    }

    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.CARD_BG);

        JPanel dayOfWeekPanel = new JPanel(new GridLayout(1, 7));
        dayOfWeekPanel.setBackground(Theme.CARD_BG);
        
        String[] days = {"SUN", "MON", "TUE", "WED", "THR", "FRI", "SAT"};
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(Theme.FONT_BOLD_16);
            if (day.equals("SUN")) dayLabel.setForeground(Theme.ACCENT);
            else if (day.equals("SAT")) dayLabel.setForeground(Theme.PRIMARY);
            else dayLabel.setForeground(Theme.TEXT_SUB);
            dayOfWeekPanel.add(dayLabel);
        }
        panel.add(dayOfWeekPanel, BorderLayout.NORTH);

        calendarGridPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarGridPanel.setBackground(Theme.CARD_BG);
        calendarGridPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        panel.add(calendarGridPanel, BorderLayout.CENTER);
        return panel;
    }

    public void updateCalendar() {
        calendarGridPanel.removeAll();
        monthYearLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("yyyy. MM")));

        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < dayOfWeek; i++) {
            calendarGridPanel.add(new JLabel(""));
        }

        String currentUserId = UserSession.getInstance().getUserId();

        int daysInMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate thisDay = currentDate.withDayOfMonth(day);
            
            // ★ [변경] 분리된 DayButton 사용
            DayButton dayButton = new DayButton(String.valueOf(day));
            
            if (currentUserId != null) {
                // ★ [변경] Controller 사용
                List<Task> tasks = todoController.getTasksByDate(currentUserId, thisDay);
                List<Color> dotColors = new ArrayList<>();
                List<Color> barColors = new ArrayList<>();

                if (!tasks.isEmpty()) {
                    for (Task t : tasks) {
                        try {
                            String sStr = t.getStartDate();
                            String eStr = t.getEndDate();
                            if (sStr.length() > 16) sStr = sStr.substring(0, 16);
                            if (eStr.length() > 16) eStr = eStr.substring(0, 16);

                            LocalDate startData = LocalDateTime.parse(sStr, FULL_FMT).toLocalDate();
                            LocalDate endData = LocalDateTime.parse(eStr, FULL_FMT).toLocalDate();

                            Color taskColor = getHashColor(t.getTitle());

                            if (!startData.equals(endData)) {
                                barColors.add(taskColor);
                            } else {
                                dotColors.add(taskColor);
                            }
                        } catch (Exception e) {
                            dotColors.add(getHashColor(t.getTitle()));
                        }
                    }
                    dayButton.setTaskColors(dotColors, barColors);
                }
            }

            boolean isToday = thisDay.equals(LocalDate.now());
            boolean isSelected = thisDay.equals(currentDate);

            if (isToday) dayButton.setIsToday(true);
            else if (isSelected) dayButton.setIsSelected(true);

            final int currentDay = day;
            dayButton.addActionListener(e -> {
                currentDate = currentDate.withDayOfMonth(currentDay);
                taskPanel.loadTasksForDate(currentDate);
                updateCalendar(); 
            });
            
            calendarGridPanel.add(dayButton);
        }

        calendarGridPanel.revalidate();
        calendarGridPanel.repaint();
    }

    private Color getHashColor(String text) {
        int hash = text.hashCode();
        int r = ((hash & 0xFF0000) >> 16) % 127 + 128;
        int g = ((hash & 0x00FF00) >> 8) % 127 + 128;
        int b = (hash & 0x0000FF) % 127 + 128;
        return new Color(r, g, b);
    }

    // (내부 클래스 DayButton은 삭제됨 -> 별도 파일로 이동)
}