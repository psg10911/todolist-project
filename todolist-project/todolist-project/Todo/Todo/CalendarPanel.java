package Todo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
// [수정됨] import 추가
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * 좌측 캘린더 UI를 담당하는 패널 (JPanel).
 */
public class CalendarPanel extends JPanel {

    private JLabel monthYearLabel;
    private JPanel calendarGridPanel;
    private LocalDate currentDate;
    private TaskPanel taskPanel; // 할 일 목록 패널 참조
    private LocalDate selectedDate; // 선택된 날짜 저장 변수

    // [수정됨] 정사각형 크기를 저장할 변수
    private Dimension squareCellSize = new Dimension(60, 60);

    public CalendarPanel(TaskPanel taskPanel) {
        this.taskPanel = taskPanel; // MainPanel로부터 참조를 받음
        this.currentDate = LocalDate.now();
        this.selectedDate = LocalDate.now(); // ✅ [추가] selectedDate도 오늘로 초기화

        setLayout(new BorderLayout(10, 10));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCalendarPanel(), BorderLayout.CENTER);

        updateCalendar();
        taskPanel.loadTasksForDate(selectedDate); // 초기 로드 시 오늘 날짜의 할 일 목록 표시
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
        // ✅ [수정]
        // currentDate는 달력의 '월'을 제어하므로 newDate의 1일로 설정
        currentDate = newDate.withDayOfMonth(1);
        // selectedDate는 사용자가 클릭한 정확한 날짜
        selectedDate = newDate;

        updateCalendar(); // 달력 다시 그리기

        // ✅ [수정] TaskPanel에는 항상 selectedDate를 전달
        taskPanel.loadTasksForDate(selectedDate);
    }

    // [수정됨] createCalendarPanel 메서드 전체 (요일 표시 + 정사각형)
    private JPanel createCalendarPanel() {
        // 1. 메인 패널 (BorderLayout)
        JPanel panel = new JPanel(new BorderLayout());

        // 2. 요일 패널 (GridLayout)
        JPanel dayOfWeekPanel = new JPanel(new GridLayout(1, 7));
        String[] days = { "SUN", "MON", "TUE", "WED", "THR", "FRI", "SAT" };
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            if (day.equals("SUN"))
                dayLabel.setForeground(Color.RED);
            if (day.equals("SAT"))
                dayLabel.setForeground(Color.BLUE);
            dayOfWeekPanel.add(dayLabel);
        }

        // 3. 요일 패널을 메인 패널의 NORTH에 추가
        panel.add(dayOfWeekPanel, BorderLayout.NORTH);

        // 4. 날짜 그리드 패널 (GridLayout)
        calendarGridPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarGridPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        // 5. 정사각형 계산을 위한 리스너
        calendarGridPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int panelWidth = calendarGridPanel.getWidth();
                if (panelWidth > 0) {
                    GridLayout layout = (GridLayout) calendarGridPanel.getLayout();
                    int hgap = layout.getHgap();
                    int cellWidth = (panelWidth - (6 * hgap)) / 7;

                    if (cellWidth < 10)
                        cellWidth = 10;

                    squareCellSize = new Dimension(cellWidth, cellWidth);

                    for (Component comp : calendarGridPanel.getComponents()) {
                        comp.setPreferredSize(squareCellSize);
                    }
                    calendarGridPanel.revalidate();
                }
            }
        });

        // 6. calendarGridPanel을 감싸서 세로로 늘어나지 않게 할 wrapper 패널 생성
        JPanel gridWrapperPanel = new JPanel(new BorderLayout());

        // 7. wrapper의 NORTH에 그리드 패널을 추가 (정사각형 유지)
        gridWrapperPanel.add(calendarGridPanel, BorderLayout.NORTH);

        // 8. wrapper 패널을 메인 패널(panel)의 CENTER에 추가
        panel.add(gridWrapperPanel, BorderLayout.CENTER);

        return panel;
    }

    // [수정됨] updateCalendar 메서드 (정사각형 크기 적용)
    private void updateCalendar() {
        calendarGridPanel.removeAll();
        monthYearLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("YYYY / MM")));

        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7; // 0(일)~6(토)

        for (int i = 0; i < dayOfWeek; i++) {
            // [수정됨] 빈 라벨에도 크기 적용
            JLabel blank = new JLabel("");
            blank.setPreferredSize(squareCellSize);
            calendarGridPanel.add(blank);
        }

        int daysInMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
            dayButton.setFocusPainted(false);

            // [수정됨] 버튼에도 크기 적용
            dayButton.setPreferredSize(squareCellSize);

            // ✅ --- [ 이 부분을 통째로 교체 ] ---

            // 이 버튼의 날짜
            LocalDate buttonDate = currentDate.withDayOfMonth(day);

            if (buttonDate.equals(LocalDate.now())) {
                // 1. "오늘" 날짜 (파란색) - 최우선
                dayButton.setBackground(new Color(200, 220, 255));
                dayButton.setOpaque(true);
            } else if (buttonDate.equals(selectedDate)) {
                // 2. "선택된" 날짜 (노란색 계열) - 차선
                dayButton.setBackground(new Color(255, 250, 200)); // 예: 밝은 노랑
                dayButton.setOpaque(true);
            } else {
                // 3. 그 외 (기본값)
                dayButton.setBackground(null);
                dayButton.setOpaque(false);
            }

            // ✅ --- [ 교체 끝 ] ---

            final int currentDay = day;
            dayButton.addActionListener(e -> {
                // 날짜 버튼 클릭 시
                LocalDate clickedDate = currentDate.withDayOfMonth(currentDay);
                changeDate(clickedDate); // 'changeDate' 메서드 호출
            });
            calendarGridPanel.add(dayButton);
        }

        calendarGridPanel.revalidate();
        calendarGridPanel.repaint();
    }
}