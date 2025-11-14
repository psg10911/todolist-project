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
 * [수업 자료] JTable 예제를 활용합니다. [cite: 53, 55]
 */
public class TaskPanel extends JPanel {

    private JLabel selectedDateLabel;
    private JTable table;
    private DefaultTableModel tableModel; // [수업 자료] [cite: 127]
    private LocalDate currentDate; // CalendarPanel에서 받아온 현재 날짜

    // [수업 자료] PDF의 columnNames [] = { ... } [cite: 129]
    private String[] columnNames = {"완료", "일정 제목", "시작일", "종료일"};

    public TaskPanel() {
        setLayout(new BorderLayout(5, 10));
        setPreferredSize(new Dimension(400, 0)); // 너비 고정

        // 1. 상단 날짜 레이블
        selectedDateLabel = new JLabel(" ", SwingConstants.CENTER);
        selectedDateLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(selectedDateLabel, BorderLayout.NORTH);

        // 2. 중앙 JTable
        // [수업 자료] tableModel = new DefaultTableModel(columnNames, 0) [cite: 132-133]
        tableModel = new DefaultTableModel(columnNames, 0) {
            // "완료" 컬럼(0번 인덱스)은 Boolean 타입(체크박스)으로 설정
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                return String.class;
            }
            // "완료" 컬럼 외에는 편집 불가능하게 설정
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // 0번 컬럼(체크박스)만 수정 가능
            }
        };
        
        // [수업 자료] table = new JTable(tableModel) [cite: 135]
        table = new JTable(tableModel);
        
        // 컬럼 너비 간단하게 설정
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);

        // [수업 자료] JScrollPane(table) [cite: 157-158]
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 3. 하단 버튼 패널 (PDF의 bottom 과 유사)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton addTaskBtn = new JButton("할 일 추가");
        JButton delTaskBtn = new JButton("할 일 삭제");
        
        buttonPanel.add(addTaskBtn);
        buttonPanel.add(delTaskBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // '할 일 추가' 버튼 리스너
        addTaskBtn.addActionListener(e -> {
            TaskDialog dialog = new TaskDialog((JFrame) SwingUtilities.getWindowAncestor(this), currentDate);
            dialog.setVisible(true);

            if (dialog.getTask() != null) {
                Task newTask = dialog.getTask();
                // (실제로는 DB 저장)
                
                // [수업 자료] tableModel.addRow(String[]) 
                tableModel.addRow(newTask.toObjectArray());
            }
        });

        // '할 일 삭제' 버튼 리스너
        delTaskBtn.addActionListener(e -> {
            // [수업 자료] PDF의 selectedIndex [cite: 128]와 유사
            int selectedRow = table.getSelectedRow();
            
            if (selectedRow >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "이 할 일을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    // (실제로는 DB 삭제)
                    
                    // [수업 자료] tableModel.removeRow(selectedIndex) 
                    tableModel.removeRow(selectedRow);
                }
            } else {
                JOptionPane.showMessageDialog(this, "삭제할 할 일을 선택해주세요.");
            }
        });
    }

    /**
     * CalendarPanel에서 호출하는 메서드.
     * 날짜가 변경되면 해당 날짜의 할 일을 (가상으로) 로드합니다.
     */
    public void loadTasksForDate(LocalDate date) {
        this.currentDate = date;
        String formattedDate = date.format(DateTimeFormatter.ofPattern("MM/dd/YYYY (E)"));
        selectedDateLabel.setText(formattedDate);

        // (실제로는 여기서 DB에서 'date'에 해당하는 할 일을 조회해야 합니다)
        
        // [수업 자료] PDF의 loadData() [cite: 144]와 유사한 역할
        // [수업 자료] tableModel.setRowCount(0) [cite: 146] (목록 초기화)
        tableModel.setRowCount(0); 

        // 프론트엔드 데모용 가상 데이터
        if (date.getDayOfMonth() == 10) {
            tableModel.addRow(new Object[]{false, "자바 Swing 스터디", "2025-11-10", "2025-11-10"});
            tableModel.addRow(new Object[]{true, "프로젝트 디자인 구상", "2025-11-10", "2025-11-10"});
        } else if (date.getDayOfMonth() == 22) {
            tableModel.addRow(new Object[]{false, "페르소나 데이터 만들기", "2025-11-22", "2025-11-25"});
        }
    }
}