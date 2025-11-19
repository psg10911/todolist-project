package Todo;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class MainPanel extends JPanel {

    private CalendarPanel calendarPanel;
    private TaskPanel taskPanel;

    public MainPanel() {
        setLayout(new BorderLayout(15, 15)); // 패널 간 간격 넓힘
        setBackground(Theme.BACKGROUND); // 배경색 지정
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // 전체 여백

        taskPanel = new TaskPanel();
        calendarPanel = new CalendarPanel(taskPanel);

        add(calendarPanel, BorderLayout.CENTER);
        add(taskPanel, BorderLayout.EAST);
    }
    
    public TaskPanel getTaskPanel() {
        return taskPanel;
    }
}