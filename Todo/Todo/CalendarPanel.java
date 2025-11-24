package Todo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

//앱의 좌측 UI


public class CalendarPanel extends JPanel {

    private JLabel monthYearLabel;// "2025. 11" 형식으로 표시
    private JPanel calendarGridPanel;// 달력 날짜 그리드 패널
    private LocalDate currentDate;// 현재 보고 있는 달의 날짜 (년, 월 정보 포함)
    private TaskPanel taskPanel;// 연결된 할 일 패널

    public CalendarPanel(TaskPanel taskPanel) {
        this.taskPanel = taskPanel;// 할 일 패널 참조 저장
        this.currentDate = LocalDate.now();// 초기값: 오늘 날짜
        
        setLayout(new BorderLayout(0, 20));// 위아래 20픽셀 간격
        setBackground(Theme.CARD_BG); // 배경색 설정
        setBorder(new EmptyBorder(20, 20, 20, 20)); // 패딩 설정

        add(createTopPanel(), BorderLayout.NORTH);// 상단 네비게이션 패널
        add(createCalendarPanel(), BorderLayout.CENTER);// 달력 패널

        updateCalendar();// 달력 초기화
        taskPanel.loadTasksForDate(currentDate);// 오늘 날짜의 할 일 불러오기
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());// 상단 네비게이션 패널
        panel.setBackground(Theme.CARD_BG);// 배경색 설정

        JButton todayButton = new JButton("Today");// 오늘 버튼
        styleNavButton(todayButton);// 버튼 스타일 적용

        JPanel monthNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));// 월 네비게이션 패널
        monthNavPanel.setBackground(Theme.CARD_BG);// 배경색 설정

        JButton prevYearBtn = new JButton("<<");// 이전 해 버튼
        JButton prevMonthBtn = new JButton("<");// 이전 달 버튼
        JButton nextMonthBtn = new JButton(">");// 다음 달 버튼
        JButton nextYearBtn = new JButton(">>");// 다음 해 버튼
        
        styleNavButton(prevYearBtn);// 버튼 스타일 적용
        styleNavButton(prevMonthBtn);// 버튼 스타일 적용
        styleNavButton(nextMonthBtn);// 버튼 스타일 적용
        styleNavButton(nextYearBtn);// 버튼 스타일 적용

        monthYearLabel = new JLabel();// 년월 표시 라벨
        monthYearLabel.setFont(Theme.FONT_BOLD_24);// 폰트 설정
        monthYearLabel.setForeground(Theme.TEXT_MAIN);// 글자색 설정
        
        monthNavPanel.add(prevYearBtn);// 이전 해 버튼 추가
        monthNavPanel.add(prevMonthBtn);// 이전 달 버튼 추가
        monthNavPanel.add(monthYearLabel);// 년월 라벨 추가
        monthNavPanel.add(nextMonthBtn);// 다음 달 버튼 추가
        monthNavPanel.add(nextYearBtn);// 다음 해 버튼 추가
        panel.add(monthNavPanel, BorderLayout.CENTER);// 중앙에 월 네비게이션 패널 추가
        panel.add(todayButton, BorderLayout.WEST); // 왼쪽에 오늘 버튼 추가

        // 리스너
        todayButton.addActionListener(e -> changeDate(LocalDate.now()));// 오늘 버튼 클릭 시 오늘 날짜로 변경
        prevMonthBtn.addActionListener(e -> changeDate(currentDate.minusMonths(1)));// 이전 달 버튼 클릭 시 한 달 전으로 변경
        nextMonthBtn.addActionListener(e -> changeDate(currentDate.plusMonths(1)));// 다음 달 버튼 클릭 시 한 달 후로 변경
        prevYearBtn.addActionListener(e -> changeDate(currentDate.minusYears(1)));// 이전 해 버튼 클릭 시 1년 전으로 변경
        nextYearBtn.addActionListener(e -> changeDate(currentDate.plusYears(1)));// 다음 해 버튼 클릭 시 1년 후로 변경
        
        return panel;
    }

    private void styleNavButton(JButton btn) {
        btn.setFont(Theme.FONT_BOLD_16);// 폰트 설정
        btn.setForeground(Theme.PRIMARY);// 글자색 설정
        btn.setBackground(Color.WHITE);// 배경색 설정
        btn.setBorder(null); // 테두리 제거
        btn.setFocusPainted(false);// 포커스 테두리 제거
        btn.setContentAreaFilled(false);// 배경 투명화
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));// 마우스 커서 변경
    }
    
    private void changeDate(LocalDate newDate) {
        currentDate = newDate;// 현재 날짜 변경
        updateCalendar();// 달력 갱신
        taskPanel.loadTasksForDate(currentDate);// 해당 날짜의 할 일 불러오기
    }

    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout());// 달력 패널
        panel.setBackground(Theme.CARD_BG);// 배경색 설정

        JPanel dayOfWeekPanel = new JPanel(new GridLayout(1, 7));// 요일 헤더 패널
        dayOfWeekPanel.setBackground(Theme.CARD_BG);// 배경색 설정
        
        String[] days = {"SUN", "MON", "TUE", "WED", "THR", "FRI", "SAT"};// 요일 배열
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);// 요일 라벨
            dayLabel.setFont(Theme.FONT_BOLD_16);// 폰트 설정
            if (day.equals("SUN")) dayLabel.setForeground(Theme.ACCENT);// 일요일: 강조색
            else if (day.equals("SAT")) dayLabel.setForeground(Theme.PRIMARY);// 토요일: 기본색
            else dayLabel.setForeground(Theme.TEXT_SUB);// 평일: 보조 텍스트 색
            dayOfWeekPanel.add(dayLabel);
        }
        panel.add(dayOfWeekPanel, BorderLayout.NORTH);// 상단에 요일 헤더 패널 추가

        calendarGridPanel = new JPanel(new GridLayout(0, 7, 5, 5));// 날짜 그리드 패널
        calendarGridPanel.setBackground(Theme.CARD_BG);// 배경색 설정
        calendarGridPanel.setBorder(new EmptyBorder(10, 0, 0, 0));// 여백 설정
        panel.add(calendarGridPanel, BorderLayout.CENTER);// 중앙에 날짜 그리드 패널 추가
        return panel;
    }

    private void updateCalendar() {
        calendarGridPanel.removeAll();// 기존 날짜 버튼 제거
        monthYearLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("yyyy. MM")));// 년월 라벨 갱신

        YearMonth yearMonth = YearMonth.from(currentDate);// 현재 연월 정보
        LocalDate firstDayOfMonth = yearMonth.atDay(1);// 해당 월의 첫째 날
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;// 첫째 날의 요일 (일=0, 월=1, ..., 토=6)

        // 1. 빈칸 채우기
        for (int i = 0; i < dayOfWeek; i++) {
            calendarGridPanel.add(new JLabel("")); // 빈칸 추가
        }

        // 2. 날짜 버튼 생성 및 추가
        int daysInMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate thisDay = currentDate.withDayOfMonth(day);// 해당 날짜
            
            // 상태 확인
            boolean isToday = thisDay.equals(LocalDate.now());// 오늘인지 여부
            boolean isSelected = thisDay.equals(currentDate);// 선택된 날짜인지 여부

            // 커스텀 버튼 생성 (paintComponent 오버라이드)
            JButton dayButton = new JButton(String.valueOf(day)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;// 그래픽스2D로 변환
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);// 안티앨리어싱 설정

                    // 원 그리기 (오늘이거나 선택된 경우)
                    if (isSelected || isToday) {
                        if (isToday) {
                            g2.setColor(Theme.PRIMARY); // 오늘: 진한 파랑
                        } else {
                            g2.setColor(new Color(225, 240, 255)); // 선택됨: 아주 연한 하늘색
                        }
                        
                        int diameter = Math.min(getWidth(), getHeight()) - 4;// 원의 지름 (버튼 크기에서 약간 여백을 뺌)
                        int x = (getWidth() - diameter) / 2;// 원의 x 좌표
                        int y = (getHeight() - diameter) / 2;// 원의 y 좌표
                        g2.fillOval(x, y, diameter, diameter);// 원 채우기
                    }

                    super.paintComponent(g); // 숫자 그리기
                }
            };

            // 버튼 스타일
            dayButton.setFont(Theme.FONT_REGULAR_14);// 폰트 설정
            dayButton.setFocusPainted(false);// 포커스 테두리 제거
            dayButton.setBorder(null);// 테두리 제거
            dayButton.setContentAreaFilled(false); // 배경 투명화 (원만 보이게)
            dayButton.setOpaque(false);// 불투명도 설정

            // 글자 색상 설정
            if (isToday) {
                dayButton.setForeground(Color.WHITE);// 오늘: 흰색 글씨
                dayButton.setFont(Theme.FONT_BOLD_16);// 오늘: 굵은 글씨
            } else if (isSelected) {
                dayButton.setForeground(Theme.PRIMARY); // 선택된 날짜: 하늘색 글씨
                dayButton.setFont(Theme.FONT_BOLD_16);// 선택된 날짜: 굵은 글씨
            } else {
                dayButton.setForeground(Theme.TEXT_MAIN);// 기본 날짜: 기본 텍스트 색
            }

            // ★ [수정된 부분] 클릭 시 동작
            final int currentDay = day;// 현재 날짜 저장 (람다식에서 사용하기 위해 final 또는 effectively final이어야 함)
            dayButton.addActionListener(e -> {
                // 1. 현재 날짜 변수(currentDate)를 클릭한 날짜로 업데이트합니다. (이 부분이 빠져 있었음)
                currentDate = currentDate.withDayOfMonth(currentDay);// 클릭한 날짜로 변경
                
                // 2. 할 일 목록 갱신
                taskPanel.loadTasksForDate(currentDate);// 해당 날짜의 할 일 불러오기
                
                // 3. 캘린더 화면 갱신 (업데이트된 currentDate를 기준으로 다시 그림)
                updateCalendar(); // 달력 갱신
            });
            
            calendarGridPanel.add(dayButton);// 버튼 추가
        }

        calendarGridPanel.revalidate();// 레이아웃 재검토
        calendarGridPanel.repaint();// 캘린더 패널 다시 그리기
    }
}