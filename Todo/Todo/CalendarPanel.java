package Todo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class CalendarPanel extends JPanel {

    private JLabel monthYearLabel;
    private JPanel calendarGridPanel;
    private LocalDate currentDate;
    private TaskPanel taskPanel;

    public CalendarPanel(TaskPanel taskPanel) {
        this.taskPanel = taskPanel;
        this.currentDate = LocalDate.now();
        
        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.CARD_BG); 
        setBorder(new EmptyBorder(20, 20, 20, 20)); 

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCalendarPanel(), BorderLayout.CENTER);

        updateCalendar();
        taskPanel.loadTasksForDate(currentDate);
    }

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

        // 리스너
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

   // CalendarPanel.java 내부의 updateCalendar 메서드 전체를 아래 코드로 교체하세요.
    private void updateCalendar() {
        calendarGridPanel.removeAll();
        monthYearLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("yyyy. MM")));

        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;

        // 1. 빈칸 채우기
        for (int i = 0; i < dayOfWeek; i++) {
            calendarGridPanel.add(new JLabel(""));
        }

        // 2. 날짜 버튼 생성 및 추가
        int daysInMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate thisDay = currentDate.withDayOfMonth(day);
            
            // 상태 확인
            boolean isToday = thisDay.equals(LocalDate.now());
            boolean isSelected = thisDay.equals(currentDate);

            // 커스텀 버튼 생성 (paintComponent 오버라이드)
            JButton dayButton = new JButton(String.valueOf(day)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // 원 그리기 (오늘이거나 선택된 경우)
                    if (isSelected || isToday) {
                        if (isToday) {
                            g2.setColor(Theme.PRIMARY); // 오늘: 진한 파랑
                        } else {
                            g2.setColor(new Color(225, 240, 255)); // 선택됨: 아주 연한 하늘색
                        }
                        
                        int diameter = Math.min(getWidth(), getHeight()) - 4;
                        int x = (getWidth() - diameter) / 2;
                        int y = (getHeight() - diameter) / 2;
                        g2.fillOval(x, y, diameter, diameter);
                    }

                    super.paintComponent(g); // 숫자 그리기
                }
            };

            // 버튼 스타일
            dayButton.setFont(Theme.FONT_REGULAR_14);
            dayButton.setFocusPainted(false);
            dayButton.setBorder(null);
            dayButton.setContentAreaFilled(false); // 배경 투명화 (원만 보이게)
            dayButton.setOpaque(false);

            // 글자 색상 설정
            if (isToday) {
                dayButton.setForeground(Color.WHITE);
                dayButton.setFont(Theme.FONT_BOLD_16);
            } else if (isSelected) {
                dayButton.setForeground(Theme.PRIMARY); // 선택된 날짜: 하늘색 글씨
                dayButton.setFont(Theme.FONT_BOLD_16);
            } else {
                dayButton.setForeground(Theme.TEXT_MAIN);
            }

            // ★ [수정된 부분] 클릭 시 동작
            final int currentDay = day;
            dayButton.addActionListener(e -> {
                // 1. 현재 날짜 변수(currentDate)를 클릭한 날짜로 업데이트합니다. (이 부분이 빠져 있었음)
                currentDate = currentDate.withDayOfMonth(currentDay);
                
                // 2. 할 일 목록 갱신
                taskPanel.loadTasksForDate(currentDate);
                
                // 3. 캘린더 화면 갱신 (업데이트된 currentDate를 기준으로 다시 그림)
                updateCalendar(); 
            });
            
            calendarGridPanel.add(dayButton);
        }

        calendarGridPanel.revalidate();
        calendarGridPanel.repaint();
    }
}