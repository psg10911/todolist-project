package Todo;

import javax.swing.*;
import java.awt.*;

//메인화면
public class MainPanel extends JPanel {

    private CalendarPanel calendarPanel;
    private TaskPanel taskPanel;

    public MainPanel() {
        setLayout(new BorderLayout(10, 10));// 패널 간격 설정
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));// 패널 여백 설정

        // 1. TaskPanel (할 일 목록) 생성 (EAST)
        taskPanel = new TaskPanel();

        // 2. CalendarPanel (달력) 생성 (CENTER)
        calendarPanel = new CalendarPanel(taskPanel);// TaskPanel 참조 전달

         // 3. 패널 배치
        add(calendarPanel, BorderLayout.CENTER);// CalendarPanel을 가운데에 배치
        add(taskPanel, BorderLayout.EAST);// TaskPanel을 오른쪽에 배치
    }
}
