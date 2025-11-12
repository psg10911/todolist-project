package Todo;

import javax.swing.*;
import java.awt.*;
// ... (기존 import)
import java.awt.Frame; // (이전 단계에서 추가됨)
import javax.swing.JFrame; // (이전 단계에서 추가됨)
import javax.swing.SwingUtilities; // (이전 단계에서 추가됨)

// [추가] 
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * 메인 화면 (로그인 후 보여지는 화면).
 * [수업 자료] BorderLayout을 사용해 캘린더와 할 일 목록을 배치합니다.
 */
public class MainPanel extends JPanel {

    private CalendarPanel calendarPanel;
    private TaskPanel taskPanel;

    // --- [ 수정된 부분 1: 필드 추가 ] ---
    private ToDoListApp mainApp; // 화면 전환(로그아웃)을 위한 ToDoListApp 참조
    // --- [ 수정 완료 ] ---

    // --- [ 수정된 부분 2: 생성자 변경 ] ---
    public MainPanel(ToDoListApp mainApp) { // (ToDoListApp mainApp) 추가
        this.mainApp = mainApp; // 참조 저장

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

        // --- [ 수정된 부분 3: 로그아웃 버튼 추가 ] ---

        // 3. 하단 로그아웃 버튼 패널 (SOUTH)
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); // 왼쪽 정렬
        JButton logoutButton = new JButton("로그아웃");

        // 로그아웃 버튼 클릭 시 LOGIN 패널로 전환
        logoutButton.addActionListener(e -> {
            // mainApp 참조를 사용하여 showPanel 호출
            this.mainApp.showPanel("LOGIN");
        });

        bottomBar.add(logoutButton);
        add(bottomBar, BorderLayout.SOUTH); // 메인 패널의 SOUTH에 추가

        // --- [ 수정 완료 ] ---

        // [추가] 이 코드로 교체합니다.
        // MainPanel이 화면에 표시될 때(componentShown)를 감지하는 리스너 추가
        addComponentListener(new ComponentAdapter() {
            private boolean isFirstTime = true; // 팝업이 한 번만 뜨도록 제어

            @Override
            public void componentShown(ComponentEvent e) {
                // 1. 패널이 화면에 보일 때
                // 2. 'isFirstTime'이 true일 때 (즉, 로그인 직후 처음 한 번)
                if (isFirstTime) {

                    // 팝업 로직 실행
                    SwingUtilities.invokeLater(() -> {
                        try {
                            // 1. TaskPanel에서 (오늘의) 미완료 할일 개수 가져오기
                            int incompleteCount = taskPanel.getIncompleteTaskCount();

                            // 2. 팝업을 띄울 부모 프레임(ToDoListApp) 찾기
                            Frame owner = (Frame) SwingUtilities.getWindowAncestor(MainPanel.this);

                            // 3. 팝업 생성 및 표시
                            if (owner != null) {
                                NotificationPopup popup = new NotificationPopup(owner, incompleteCount);
                                popup.setVisible(true);
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });

                    isFirstTime = false; // 플래그를 false로 바꿔 다시는 팝업이 뜨지 않게 함
                }
            }

            // [추가] MainPanel이 다시 숨겨질 때 (로그아웃 시)
            @Override
            public void componentHidden(ComponentEvent e) {
                // 팝업이 한 번만 뜨도록 제어하는 플래그를
                // true로 리셋합니다. (다음에 로그인할 때 다시 팝업이 뜨도록)
                isFirstTime = true;
            }
        });

    }
}