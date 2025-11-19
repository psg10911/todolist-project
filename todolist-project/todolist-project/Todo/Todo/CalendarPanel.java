package Todo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;


public class CalendarPanel extends JPanel {

    private JLabel monthYearLabel; // 월/년 표시 라벨
    private JPanel calendarGridPanel; // 캘린더 날짜 그리드 패널
    private LocalDate currentDate;// 현재 표시 중인 날짜
    private TaskPanel taskPanel; // 할 일 목록 패널 참조

    // [수정됨] 정사각형 크기를 저장할 변수
    private Dimension squareCellSize = new Dimension(60, 60);

    public CalendarPanel(TaskPanel taskPanel) {
        this.taskPanel = taskPanel; // MainPanel로부터 참조를 받음
        this.currentDate = LocalDate.now();// 초기값은 오늘 날짜

        setLayout(new BorderLayout(10, 10));// 패널 간격 설정

        add(createTopPanel(), BorderLayout.NORTH);// 상단 패널 추가
        add(createCalendarPanel(), BorderLayout.CENTER);// 캘린더 패널 추가

         // 초기 캘린더 표시

        updateCalendar();// 캘린더 초기화
        taskPanel.loadTasksForDate(currentDate); // 프로그램 시작 시 오늘 날짜의 할 일 로드
    }

    // 상단 (월 이동) 패널 생성 (이전 코드와 동일)
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());// 상단 패널
        JButton todayButton = new JButton("Today");// 오늘 버튼
        panel.add(todayButton, BorderLayout.WEST);// 왼쪽에 오늘 버튼 추가

        JPanel monthNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));// 가운데 월 이동 패널
        JButton prevYearBtn = new JButton("<<");// 이전 년도 버튼
        JButton prevMonthBtn = new JButton("<");// 이전 달 버튼
        monthYearLabel = new JLabel();// 월/년 표시 라벨
        monthYearLabel.setFont(new Font("SansSerif", Font.BOLD, 22));// 폰트 설정
        JButton nextMonthBtn = new JButton(">");// 다음 달 버튼
        JButton nextYearBtn = new JButton(">>");// 다음 년도 버튼

        monthNavPanel.add(prevYearBtn);// 이전 년도 버튼 추가
        monthNavPanel.add(prevMonthBtn);// 이전 달 버튼 추가
        monthNavPanel.add(monthYearLabel);// 월/년 라벨 추가
        monthNavPanel.add(nextMonthBtn);// 다음 달 버튼 추가
        monthNavPanel.add(nextYearBtn);// 다음 년도 버튼 추가
        panel.add(monthNavPanel, BorderLayout.CENTER);// 가운데에 월 이동 패널 추가

        // 리스너 설정
        todayButton.addActionListener(e -> changeDate(LocalDate.now()));// 오늘 버튼 클릭 시 오늘 날짜로 변경
        prevMonthBtn.addActionListener(e -> changeDate(currentDate.minusMonths(1)));// 이전 달 버튼 클릭 시
        nextMonthBtn.addActionListener(e -> changeDate(currentDate.plusMonths(1)));// 다음 달 버튼 클릭 시
        prevYearBtn.addActionListener(e -> changeDate(currentDate.minusYears(1)));// 이전 년도 버튼 클릭 시
        nextYearBtn.addActionListener(e -> changeDate(currentDate.plusYears(1)));// 다음 년도 버튼 클릭 시

        return panel;// 상단 패널 반환
    }

    // 날짜 변경 시 캘린더 업데이트 및 TaskPanel에 알림
    private void changeDate(LocalDate newDate) {
        currentDate = newDate;// 현재 날짜 변경
        updateCalendar();// 캘린더 업데이트
        taskPanel.loadTasksForDate(currentDate); // TaskPanel의 목록 업데이트
    }

    // [수정됨] createCalendarPanel 메서드 전체 (요일 표시 + 정사각형)
    private JPanel createCalendarPanel() {
        // 1. 메인 패널 (BorderLayout)
        JPanel panel = new JPanel(new BorderLayout());// 캘린더 메인 패널

        // 2. 요일 패널 (GridLayout)
        JPanel dayOfWeekPanel = new JPanel(new GridLayout(1, 7));// 요일 표시 패널
        String[] days = { "SUN", "MON", "TUE", "WED", "THR", "FRI", "SAT" };// 요일 배열
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);// 요일 라벨
            if (day.equals("SUN"))
                dayLabel.setForeground(Color.RED);// 일요일은 빨간색
            if (day.equals("SAT"))
                dayLabel.setForeground(Color.BLUE);// 토요일은 파란색
            dayOfWeekPanel.add(dayLabel);// 요일 라벨 추가
        }

        // 3. 요일 패널을 메인 패널의 NORTH에 추가
        panel.add(dayOfWeekPanel, BorderLayout.NORTH);// 요일 패널 추가

        // 4. 날짜 그리드 패널 (GridLayout)
        calendarGridPanel = new JPanel(new GridLayout(0, 7, 5, 5));// 날짜 그리드 패널
        calendarGridPanel.setBorder(new EmptyBorder(5, 0, 0, 0));// 위쪽 여백 설정

        // 5. 정사각형 계산을 위한 리스너
        calendarGridPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int panelWidth = calendarGridPanel.getWidth();// 그리드 패널의 현재 너비
                if (panelWidth > 0) {
                    GridLayout layout = (GridLayout) calendarGridPanel.getLayout();// 그리드 레이아웃 가져오기
                    int hgap = layout.getHgap();// 가로 간격
                    int cellWidth = (panelWidth - (6 * hgap)) / 7;// 셀 너비 계산

                    if (cellWidth < 10)
                        cellWidth = 10;// 최소 크기 제한

                    squareCellSize = new Dimension(cellWidth, cellWidth);// 정사각형 크기 설정

                    for (Component comp : calendarGridPanel.getComponents()) {
                        comp.setPreferredSize(squareCellSize);// 각 컴포넌트에 정사각형 크기 적용
                    }
                    calendarGridPanel.revalidate();// 레이아웃 재계산
                }
            }
        });

        // 6. calendarGridPanel을 감싸서 세로로 늘어나지 않게 할 wrapper 패널 생성
        JPanel gridWrapperPanel = new JPanel(new BorderLayout());// 그리드 래퍼 패널

        // 7. wrapper의 NORTH에 그리드 패널을 추가 (정사각형 유지)
        gridWrapperPanel.add(calendarGridPanel, BorderLayout.NORTH);// 그리드 패널 추가

        // 8. wrapper 패널을 메인 패널(panel)의 CENTER에 추가
        panel.add(gridWrapperPanel, BorderLayout.CENTER);// 그리드 래퍼 패널 추가

        return panel;// 캘린더 메인 패널 반환
    }

    // [수정됨] updateCalendar 메서드 (정사각형 크기 적용)
    private void updateCalendar() {
        calendarGridPanel.removeAll();// 기존 날짜 버튼 제거
        monthYearLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("YYYY / MM")));// 월/년 라벨 업데이트

        YearMonth yearMonth = YearMonth.from(currentDate);// 현재 연월 정보
        LocalDate firstDayOfMonth = yearMonth.atDay(1);// 해당 월의 첫 번째 날짜
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7; // 0(일)~6(토)

        for (int i = 0; i < dayOfWeek; i++) {
            // [수정됨] 빈 라벨에도 크기 적용
            JLabel blank = new JLabel("");// 빈 라벨
            blank.setPreferredSize(squareCellSize);// 정사각형 크기 적용
            calendarGridPanel.add(blank);// 빈 칸 추가
        }

        int daysInMonth = yearMonth.lengthOfMonth();// 해당 월의 총 일수
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));// 날짜 버튼
            dayButton.setFont(new Font("SansSerif", Font.PLAIN, 14));// 폰트 설정
            dayButton.setFocusPainted(false);// 포커스 페인팅 비활성화

            // [수정됨] 버튼에도 크기 적용
            dayButton.setPreferredSize(squareCellSize);// 정사각형 크기 적용

            if (currentDate.withDayOfMonth(day).equals(LocalDate.now())) {
                dayButton.setBackground(new Color(200, 220, 255));// 오늘 날짜 강조 색상
                dayButton.setOpaque(true);// 배경색 적용을 위해 불투명 설정
            }

            final int currentDay = day;// 클릭 리스너에서 사용할 현재 날짜 변수
            dayButton.addActionListener(e -> {
                // 날짜 버튼 클릭 시
                LocalDate selectedDate = currentDate.withDayOfMonth(currentDay);// 선택된 날짜
                taskPanel.loadTasksForDate(selectedDate); // TaskPanel에 날짜 전달
            });
            calendarGridPanel.add(dayButton);// 날짜 버튼 추가
        }

        calendarGridPanel.revalidate();// 레이아웃 재계산
        calendarGridPanel.repaint();// 패널 다시 그리기
    }
}
