package Todo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 우측 할 일 목록(JTable)과 버튼을 담당하는 패널 (JPanel).
 */
public class TaskPanel extends JPanel {

    private JLabel selectedDateLabel;
    private JTable table;
    private DefaultTableModel tableModel;
    private LocalDate currentDate;

    private String[] columnNames = { "완료", "일정 제목", "시작일", "종료일" };

    public TaskPanel() {
        setLayout(new BorderLayout(5, 10));
        setPreferredSize(new Dimension(400, 0)); // 오른쪽 패널 너비 고정

        // -------------------------------
        // 1️⃣ 상단 영역 (날짜 + 정렬 + 검색)
        // -------------------------------
        JPanel topPanel = new JPanel(new BorderLayout());

        // 날짜 라벨 (가운데)
        selectedDateLabel = new JLabel(" ", SwingConstants.CENTER);
        selectedDateLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        topPanel.add(selectedDateLabel, BorderLayout.CENTER);

        // 오른쪽 정렬 영역 (정렬 + 검색 버튼)
        JPanel sortSearchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        // 🔽 정렬 콤보박스
        String[] sortOptions = { "최신순", "중요도순", "완료된순" };
        JComboBox<String> sortComboBox = new JComboBox<>(sortOptions);
        sortComboBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sortSearchPanel.add(sortComboBox);

        // 🔍 검색 버튼
        JButton searchBtn = new JButton("🔍");
        searchBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sortSearchPanel.add(searchBtn);

        // topPanel의 오른쪽에 배치
        topPanel.add(sortSearchPanel, BorderLayout.EAST);

        // 최종적으로 TaskPanel의 NORTH에 추가
        add(topPanel, BorderLayout.NORTH);

        // -------------------------------
        // 2️⃣ 중앙 JTable 영역
        // -------------------------------
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0)
                    return Boolean.class;
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // 체크박스만 수정 가능
            }
        };

        table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // -------------------------------
        // 3️⃣ 하단 버튼 영역
        // -------------------------------
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton addTaskBtn = new JButton("할 일 추가");
        JButton delTaskBtn = new JButton("할 일 삭제");
        buttonPanel.add(addTaskBtn);
        buttonPanel.add(delTaskBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // -------------------------------
        // 🔘 버튼 이벤트 처리
        // -------------------------------
        addTaskBtn.addActionListener(e -> {
            TaskDialog dialog = new TaskDialog((JFrame) SwingUtilities.getWindowAncestor(this), currentDate);
            dialog.setVisible(true);

            if (dialog.getTask() != null) {
                Task newTask = dialog.getTask();
                tableModel.addRow(newTask.toObjectArray());
            }
        });

        delTaskBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "이 할 일을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    tableModel.removeRow(selectedRow);
                }
            } else {
                JOptionPane.showMessageDialog(this, "삭제할 할 일을 선택해주세요.");
            }
        });
    }

    /**
     * 날짜 변경 시 호출되는 메서드
     */
    public void loadTasksForDate(LocalDate date) {
        this.currentDate = date;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일(E)");
        String formattedDate = date.format(formatter);
        selectedDateLabel.setText(formattedDate);

        tableModel.setRowCount(0); // 기존 목록 초기화

        // 데모용 가상 데이터
        if (date.getDayOfMonth() == 10) {
            tableModel.addRow(new Object[] { false, "자바 Swing 스터디", "2025-11-10", "2025-11-10" });
            tableModel.addRow(new Object[] { true, "프로젝트 디자인 구상", "2025-11-10", "2025-11-10" });
        } else if (date.getDayOfMonth() == 22) {
            tableModel.addRow(new Object[] { false, "페르소나 데이터 만들기", "2025-11-22", "2025-11-25" });
        }
    }
}
