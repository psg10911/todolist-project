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

    private final TaskRepository repository;
    private final DefaultListModel<Task> resultModel;
    private final JList<Task> resultList;

    // TaskPanel의 refreshTaskList()를 실행하기 위한 콜백
    private final Runnable refreshCallback;

    public SearchDialog(Frame owner, TaskRepository repository, Runnable refreshCallback) {
        super(owner, "할 일 검색", true);
        this.repository = repository;
        this.refreshCallback = refreshCallback;

        setLayout(new BorderLayout(10, 10));
        setSize(420, 350);
        setLocationRelativeTo(owner);

        // 1. (North) 검색바
        JPanel searchTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JLabel searchLabel = new JLabel("키워드:");
        JTextField searchField = new JTextField(15);
        JButton execBtn = new JButton("검색");
        searchTop.add(searchLabel);
        searchTop.add(searchField);
        searchTop.add(execBtn);
        add(searchTop, BorderLayout.NORTH);

        // 2. (Center) 검색 결과 리스트
        resultModel = new DefaultListModel<>();
        resultList = new JList<>(resultModel);
        // Task 객체를 JList에 표시하기 위한 렌더러 (TaskPanel 코드 재사용)
        resultList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Task t) {
                    setText("[" + t.getPriority() + "] " + t.getTitle() + " (" +
                            t.getStartDate() + " ~ " + t.getEndDate() + ")");
                }
                return this;
            }
        });

        JScrollPane resultScroll = new JScrollPane(resultList);
        add(resultScroll, BorderLayout.CENTER);

        // 3. (South) 닫기 버튼
        JButton closeBtn = new JButton("닫기");
        closeBtn.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        // 4. '검색' 버튼 리스너
        execBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            resultModel.clear();

            if (keyword.length() < 2 || keyword.contains(" ")) {
                JOptionPane.showMessageDialog(this, "키워드는 2글자 이상이며 공백을 포함할 수 없습니다.");
                return;
            }
            // Repository에서 검색
            ArrayList<Task> results = repository.searchTasks(keyword);

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "검색 결과가 없습니다.");
            } else {
                results.forEach(resultModel::addElement);
            }
        });

        // 5. '더블클릭' 리스너 (수정)
        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Task selected = resultList.getSelectedValue();
                    if (selected != null) {
                        // 1. TaskDialog 열기 (TaskPanel 코드 재사용)
                        TaskDialog dialog = new TaskDialog(owner, LocalDate.parse(selected.getStartDate()), true);
                        dialog.fillFromTask(selected);
                        dialog.setVisible(true);

                        Task updated = dialog.getTask();
                        if (updated != null) {
                            if ("__DELETE__".equals(updated.getTitle())) {
                                // 2. Repository에서 삭제
                                repository.deleteTask(selected);
                            } else {
                                // 3. Repository의 Task 객체 직접 수정 (TaskPanel 방식)
                                selected.setTitle(updated.getTitle());
                                selected.setContent(updated.getContent());
                                selected.setStartDate(updated.getStartDate());
                                selected.setEndDate(updated.getEndDate());
                                selected.setPriority(updated.getPriority());
                            }
                            // 4. TaskPanel의 refreshTaskList() 호출
                            refreshCallback.run();
                            // 5. 검색창 닫기
                            dispose();
                        }
                    }
                }
            }
        });
    }
}