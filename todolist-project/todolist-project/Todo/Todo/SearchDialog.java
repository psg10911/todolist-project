package Todo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.function.Consumer; // 콜백용

/**
 * [신규 클래스]
 * 할 일 검색 팝업 (View/Controller).
 * TaskPanel의 openSearchDialog() 메서드에서 분리됨.
 */
public class SearchDialog extends JDialog {

    private final TaskRepository repository; // 할 일 저장소
    private final DefaultListModel<Task> resultModel; // 검색 결과 모델
    private final JList<Task> resultList; // 검색 결과 리스트

    // TaskPanel의 refreshTaskList()를 실행하기 위한 콜백
    private final Runnable refreshCallback;

    public SearchDialog(Frame owner, TaskRepository repository, Runnable refreshCallback) {
        super(owner, "할 일 검색", true); // 모달 다이얼로그
        this.repository = repository; // 저장소 주입
        this.refreshCallback = refreshCallback; // 콜백 주입

        setLayout(new BorderLayout(10, 10)); // 레이아웃 설정
        setSize(420, 350); // 크기 설정
        setLocationRelativeTo(owner); // 소유자 중앙에 위치

        // 1. (North) 검색바
        JPanel searchTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));// 검색 패널
        JLabel searchLabel = new JLabel("키워드:"); // 레이블
        JTextField searchField = new JTextField(15); // 검색 입력 필드
        JButton execBtn = new JButton("검색");// 실행 버튼
        searchTop.add(searchLabel);// 구성요소 추가
        searchTop.add(searchField);// 구성요소 추가
        searchTop.add(execBtn);// 구성요소 추가
        add(searchTop, BorderLayout.NORTH);// 다이얼로그 상단에 추가

        // 2. (Center) 검색 결과 리스트
        resultModel = new DefaultListModel<>();// 결과 모델
        resultList = new JList<>(resultModel);// 결과 리스트
        // Task 객체를 JList에 표시하기 위한 렌더러 (TaskPanel 코드 재사용)
        resultList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {// 기본 설정
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);// Task 객체 표시
                if (value instanceof Task t) { // Task 객체인 경우
                    setText("[" + t.getPriority() + "] " + t.getTitle() + " (" +
                            t.getStartDate() + " ~ " + t.getEndDate() + ")"); // 텍스트 설정
                }
                return this;
            }
        });

        JScrollPane resultScroll = new JScrollPane(resultList);// 스크롤 패널
        add(resultScroll, BorderLayout.CENTER);// 다이얼로그 중앙에 추가

        // 3. (South) 닫기 버튼
        JButton closeBtn = new JButton("닫기");// 닫기 버튼
        closeBtn.addActionListener(e -> dispose());// 닫기 리스너
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));// 하단 패널
        bottom.add(closeBtn);// 닫기 버튼 추가
        add(bottom, BorderLayout.SOUTH);// 다이얼로그 하단에 추가

        // 4. '검색' 버튼 리스너
        execBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();// 입력 키워드
            resultModel.clear();// 이전 결과 초기화

            if (keyword.length() < 2 || keyword.contains(" ")) {
                JOptionPane.showMessageDialog(this, "키워드는 2글자 이상이며 공백을 포함할 수 없습니다."); // 경고
                return;
            }
            // Repository에서 검색
            ArrayList<Task> results = repository.searchTasks(keyword);

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "검색 결과가 없습니다."); // 결과 없음
            } else {
                results.forEach(resultModel::addElement); // 결과 모델에 추가
            }
        });

        // 5. '더블클릭' 리스너 (수정)
        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // 더블클릭 시
                    Task selected = resultList.getSelectedValue(); // 선택된 Task
                    if (selected != null) { // null 체크
                        // 1. TaskDialog 열기 (TaskPanel 코드 재사용)
                        TaskDialog dialog = new TaskDialog(owner, LocalDate.parse(selected.getStartDate()), true);// 읽기전용
                        dialog.fillFromTask(selected);// 선택된 Task 정보 채우기
                        dialog.setVisible(true);// 다이얼로그 표시

                        Task updated = dialog.getTask();// 수정된 Task 정보 가져오기
                        if (updated != null) {
                            if ("__DELETE__".equals(updated.getTitle())) {
                                // 2. Repository에서 삭제
                                repository.deleteTask(selected);// 선택된 Task 삭제
                            } else {
                                // 3. Repository의 Task 객체 직접 수정 (TaskPanel 방식)
                                selected.setTitle(updated.getTitle());// 제목 수정
                                selected.setContent(updated.getContent());// 내용 수정
                                selected.setStartDate(updated.getStartDate());// 시작일 수정
                                selected.setEndDate(updated.getEndDate());// 종료일 수정
                                selected.setPriority(updated.getPriority());// 중요도 수정
                            }
                            // 4. TaskPanel의 refreshTaskList() 호출
                            refreshCallback.run();// 갱신 콜백 실행
                            // 5. 검색창 닫기
                            dispose();
                        }
                    }
                }
            }
        });
    }
}