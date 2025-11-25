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
    private String currentUserId; 
    
    private static final DateTimeFormatter FULL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public CalendarPanel(TaskPanel taskPanel) {
        this.taskPanel = taskPanel;
        this.currentDate = LocalDate.now();
        
        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.CARD_BG); 
        setBorder(new EmptyBorder(20, 20, 20, 20)); 
        
        setMinimumSize(new Dimension(350, 400));
        setPreferredSize(new Dimension(400, 0)); 

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCalendarPanel(), BorderLayout.CENTER);

        updateCalendar();
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
        updateCalendar(); 
    }


    // 로그아웃 시 CalendarPanel 초기화
    public void clear() {
        currentUserId = null;
        currentDate = LocalDate.now();
        updateCalendar();
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

    private void updateCalendar() {
        calendarGridPanel.removeAll();
        monthYearLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("yyyy. MM")));

        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < dayOfWeek; i++) {
            calendarGridPanel.add(new JLabel(""));
        }

        int daysInMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate thisDay = currentDate.withDayOfMonth(day);
            
            DayButton dayButton = new DayButton(String.valueOf(day));
            
            if (currentUserId != null) {
                List<Task> tasks = TodoDao.findByDate(currentUserId, thisDay);
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

                            // 하루 이상이면 막대, 당일이면 점
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

            dayButton.setFont(Theme.FONT_REGULAR_14);
            dayButton.setFocusPainted(false);
            dayButton.setBorder(null);
            dayButton.setContentAreaFilled(false);
            dayButton.setOpaque(false);

            if (isToday) {
                dayButton.setForeground(Color.WHITE);
                dayButton.setFont(Theme.FONT_BOLD_16);
                dayButton.setIsToday(true); 
            } else if (isSelected) {
                dayButton.setForeground(Theme.PRIMARY);
                dayButton.setFont(Theme.FONT_BOLD_16);
                dayButton.setIsSelected(true); 
            } else {
                dayButton.setForeground(Theme.TEXT_MAIN);
            }

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

    // =========================================================
    // ★ 커스텀 날짜 버튼 (막대 + 점 + 초과 표시)
    // =========================================================
    private class DayButton extends JButton {
        private List<Color> dotColors = new ArrayList<>();
        private List<Color> barColors = new ArrayList<>();
        
        private boolean isToday = false;
        private boolean isSelected = false;

        public DayButton(String text) {
            super(text);
        }

        public void setTaskColors(List<Color> dots, List<Color> bars) { 
            this.dotColors = dots;
            this.barColors = bars;
        }
        
        public void setIsToday(boolean b) { this.isToday = b; }
        public void setIsSelected(boolean b) { this.isSelected = b; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 1. 배경 그리기
            if (isToday) {
                g2.setColor(Theme.PRIMARY);
                int diameter = Math.min(w, h) - 4;
                g2.fillOval((w-diameter)/2, (h-diameter)/2, diameter, diameter);
            } else if (isSelected) {
                g2.setColor(new Color(225, 240, 255));
                int diameter = Math.min(w, h) - 4;
                g2.fillOval((w-diameter)/2, (h-diameter)/2, diameter, diameter);
            }

            // 2. ★ 상단 막대 그리기 (최대 3개)
            if (!barColors.isEmpty()) {
                int barHeight = 5; 
                int barGap = 2;    
                int startY = 4;    
                int maxBars = 3; // 최대 3개까지만 그림

                int count = Math.min(barColors.size(), maxBars);
                
                for (int i = 0; i < count; i++) {
                    g2.setColor(barColors.get(i));
                    g2.fillRoundRect(6, startY + (i * (barHeight + barGap)), w - 12, barHeight, 2, 2);
                }
                
                // ★ 3개 초과시 우측 하단에 '+' 표시
                if (barColors.size() > maxBars) {
                    g2.setColor(Theme.TEXT_SUB);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                    // 3번째 막대 높이 쯤 오른쪽에 표시
                    g2.drawString("+", w - 10, startY + (count * (barHeight + barGap)) + 2); 
                }
            }

            // 3. 텍스트 그리기 (기본)
            super.paintComponent(g);

            // 4. ★ 하단 점 그리기 (최대 3개)
            if (!dotColors.isEmpty()) {
                int dotSize = 6;
                int gap = 3;
                int maxDots = 3;
                
                int count = dotColors.size();
                int displayCount = Math.min(count, maxDots);
                boolean hasMore = count > maxDots;

                int totalWidth = (displayCount * dotSize) + ((displayCount - 1) * gap);
                if (hasMore) totalWidth += (gap + 6);

                int startX = (w - totalWidth) / 2;
                int y = h - 12; 

                for (int i = 0; i < displayCount; i++) {
                    g2.setColor(dotColors.get(i));
                    g2.fillOval(startX + (i * (dotSize + gap)), y, dotSize, dotSize);
                }

                if (hasMore) {
                    g2.setColor(Theme.TEXT_SUB);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                    g2.drawString("+", startX + (displayCount * (dotSize + gap)), y + dotSize); 
                }
            }
        }
    }
}