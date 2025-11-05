package Todo;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * 좌측 캘린더 UI를 담당하는 패널 (JPanel).
 */
public class CalendarPanel extends JPanel {

    private JLabel monthYearLabel;
    private JPanel calendarGridPanel;
    private LocalDate currentDate;
    private TaskPanel taskPanel; // 할 일 목록 패널 참조

    public CalendarPanel(TaskPanel taskPanel) {
        this.taskPanel = taskPanel; // MainPanel로부터 참조를 받음
        this.currentDate = LocalDate.now();
        
        setLayout(new BorderLayout(10, 10));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCalendarPanel(), BorderLayout.CENTER);

        updateCalendar();
        taskPanel.loadTasksForDate(currentDate); // 프로그램 시작 시 오늘 날짜의 할 일 로드
    }

    // 상단 (월 이동) 패널 생성 (이전 코드와 동일)
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton todayButton = new JButton("Today");
        panel.add(todayButton, BorderLayout.WEST);

        JPanel monthNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton prevYearBtn = new JButton("<<");
        JButton prevMonthBtn = new JButton("<");
        monthYearLabel = new JLabel();
        monthYearLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        JButton nextMonthBtn = new JButton(">");
        JButton nextYearBtn = new JButton(">>");
        
        monthNavPanel.add(prevYearBtn);
        monthNavPanel.add(prevMonthBtn);
        monthNavPanel.add(monthYearLabel);
        monthNavPanel.add(nextMonthBtn);
        monthNavPanel.add(nextYearBtn);
        panel.add(monthNavPanel, BorderLayout.CENTER);

        // 리스너 설정
        todayButton.addActionListener(e -> changeDate(LocalDate.now()));
        prevMonthBtn.addActionListener(e -> changeDate(currentDate.minusMonths(1)));
        nextMonthBtn.addActionListener(e -> changeDate(currentDate.plusMonths(1)));
        prevYearBtn.addActionListener(e -> changeDate(currentDate.minusYears(1)));
        nextYearBtn.addActionListener(e -> changeDate(currentDate.plusYears(1)));
        
        return panel;
    }
    
    // 날짜 변경 시 캘린더 업데이트 및 TaskPanel에 알림
    private void changeDate(LocalDate newDate) {
        currentDate = newDate;
        updateCalendar();
        taskPanel.loadTasksForDate(currentDate); // TaskPanel의 목록 업데이트
    }

    // 중앙 (날짜 그리드) 패널 생성 (이전 코드와 동일)
    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel dayOfWeekPanel = new JPanel(new GridLayout(1, 7));
        String[] days = {"SUN", "MON", "TUE", "WED", "THR", "FRI", "SAT"};
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            if (day.equals("SUN")) dayLabel.setForeground(Color.RED);
            if (day.equals("SAT")) dayLabel.setForeground(Color.BLUE);
            dayOfWeekPanel.add(dayLabel);
        }
        panel.add(dayOfWeekPanel, BorderLayout.NORTH);

        calendarGridPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarGridPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        panel.add(calendarGridPanel, BorderLayout.CENTER);
        return panel;
    }

    // 캘린더 날짜 업데이트 (이전 코드와 동일)
    private void updateCalendar() {
        calendarGridPanel.removeAll();
        monthYearLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("YYYY / MM")));

        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7; // 0(일)~6(토)

        for (int i = 0; i < dayOfWeek; i++) {
            calendarGridPanel.add(new JLabel(""));
        }

        int daysInMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
            dayButton.setFocusPainted(false);
            
            if (currentDate.withDayOfMonth(day).equals(LocalDate.now())) {
                dayButton.setBackground(new Color(200, 220, 255));
                dayButton.setOpaque(true);
            }
            
            final int currentDay = day;
            dayButton.addActionListener(e -> {
                // 날짜 버튼 클릭 시
                LocalDate selectedDate = currentDate.withDayOfMonth(currentDay);
                taskPanel.loadTasksForDate(selectedDate); // TaskPanel에 날짜 전달
            });
            calendarGridPanel.add(dayButton);
        }

        calendarGridPanel.revalidate();
        calendarGridPanel.repaint();
    }
}