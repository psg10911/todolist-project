package Todo;
import javax.swing.*;
import java.awt.*;

/**
 * 메인 화면 (로그인 후 보여지는 화면).
 * [수업 자료] BorderLayout을 사용해  캘린더와 할 일 목록을 배치합니다.
 */
public class MainPanel extends JPanel {

    private CalendarPanel calendarPanel;
    private TaskPanel taskPanel;

    public MainPanel() {
        // [수업 자료] PDF의 new JPanel(new BorderLayout()) 
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. TaskPanel (할 일 목록) 생성 (EAST)
        taskPanel = new TaskPanel();

        // 2. CalendarPanel (달력) 생성 (CENTER)
        // *중요*: 캘린더가 TaskPanel을 제어할 수 있도록 참조를 넘겨줍니다.
        calendarPanel = new CalendarPanel(taskPanel);

        // [수업 자료] PDF의 add(..., BorderLayout.CENTER) 
        add(calendarPanel, BorderLayout.CENTER);
        add(taskPanel, BorderLayout.EAST);
    }
}