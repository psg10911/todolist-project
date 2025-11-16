package Todo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * 우측 할 일 목록(JTable)과 버튼을 담당하는 패널 (JPanel).
 * [수업 자료] JTable 예제를 활용합니다.
 */
public class TaskPanel extends JPanel {

    private JLabel selectedDateLabel;
    private JTable table;
    private DefaultTableModel tableModel;
    private LocalDate currentDate;

    private String[] columnNames = {"완료", "일정 제목", "시작일", "종료일", "중요도"};

    public TaskPanel() {
        setLayout(new BorderLayout(5, 10));
        setPreferredSize(new Dimension(400, 0));

        // 1. 상단 날짜 표시
        selectedDateLabel = new JLabel(" ", SwingConstants.CENTER);
        selectedDateLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(selectedDateLabel, BorderLayout.NORTH);

        // 2. 중앙 테이블
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // 완료 여부만 수정 가능
            }
        };

        table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 3. 하단 버튼
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton addTaskBtn = new JButton("할 일 추가");
        JButton editTaskBtn = new JButton("할 일 수정"); // ✅ 새로 추가됨
        JButton delTaskBtn = new JButton("할 일 삭제");

        buttonPanel.add(addTaskBtn);
        buttonPanel.add(editTaskBtn); // ✅ 중간에 배치
        buttonPanel.add(delTaskBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // [할 일 추가]
        addTaskBtn.addActionListener(e -> {
            TaskDialog dialog = new TaskDialog((JFrame) SwingUtilities.getWindowAncestor(this), currentDate);
            dialog.setVisible(true);

            if (dialog.getTask() != null) {
                Task newTask = dialog.getTask();
                tableModel.addRow(newTask.toObjectArray());
            }
        });

        // ✅ [할 일 수정]
        editTaskBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "수정할 할 일을 선택해주세요.");
                return;
            }

            // 기존 값 가져오기
            String title = (String) tableModel.getValueAt(selectedRow, 1);
            String startDate = (String) tableModel.getValueAt(selectedRow, 2);
            String endDate = (String) tableModel.getValueAt(selectedRow, 3);
            String priorityText = (String) tableModel.getValueAt(selectedRow, 4);

            // String → int 변환
            int priority = switch (priorityText) {
                case "높음" -> 3;
                case "보통" -> 2;
                case "낮음" -> 1;
                default -> 2;
            };

            // 다이얼로그 열기
            TaskDialog dialog = new TaskDialog((JFrame) SwingUtilities.getWindowAncestor(this), currentDate);
            dialog.setVisible(true);

            if (dialog.getTask() != null) {
                Task editedTask = dialog.getTask();

                // 테이블에 반영
                tableModel.setValueAt(false, selectedRow, 0);
                tableModel.setValueAt(editedTask.getTitle(), selectedRow, 1);
                tableModel.setValueAt(editedTask.getStartDate(), selectedRow, 2);
                tableModel.setValueAt(editedTask.getEndDate(), selectedRow, 3);

                // priority 숫자 → 텍스트 변환
                String updatedPriority = switch (editedTask.getPriority()) {
                    case 3 -> "높음";
                    case 2 -> "보통";
                    case 1 -> "낮음";
                    default -> "-";
                };
                tableModel.setValueAt(updatedPriority, selectedRow, 4);
            }
        });

        // [할 일 삭제]
        delTaskBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this, "이 할 일을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    tableModel.removeRow(selectedRow);
                }
            } else {
                JOptionPane.showMessageDialog(this, "삭제할 할 일을 선택해주세요.");
            }
        });
    }

    /**
     * 날짜 변경 시 할 일 로드
     */
    public void loadTasksForDate(LocalDate date) {
        this.currentDate = date;
        String formattedDate = date.format(DateTimeFormatter.ofPattern("MM/dd/YYYY (E)"));
        selectedDateLabel.setText(formattedDate);

        tableModel.setRowCount(0);

        if (date.getDayOfMonth() == 10) {
            tableModel.addRow(new Object[]{false, "자바 Swing 스터디", "2025-11-10", "2025-11-10", "높음"});
            tableModel.addRow(new Object[]{true, "프로젝트 디자인 구상", "2025-11-10", "2025-11-10", "보통"});
        } else if (date.getDayOfMonth() == 22) {
            tableModel.addRow(new Object[]{false, "페르소나 데이터 만들기", "2025-11-22", "2025-11-25", "낮음"});
        }
    }
}
