package Todo;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;          
import javax.swing.table.TableRowSorter; 
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;                          
import java.util.List;    

public class TaskPanel extends JPanel {

    private JLabel selectedDateLabel;
    private JTable table;
    private TaskTableModel model;   // 커스텀 모델
    private LocalDate currentDate;
    private String currentUserId;   // 로그인 사용자 주입

    public void setCurrentUserId(String userId) { this.currentUserId = userId; }
    
    public void initAfterLogin(String userId) {
        setCurrentUserId(userId);
        loadTasksForDate(LocalDate.now());
    }
    
    public TaskPanel() {
        setLayout(new BorderLayout(5, 10));
        setPreferredSize(new Dimension(400, 0));

        selectedDateLabel = new JLabel(" ", SwingConstants.CENTER);
        selectedDateLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(selectedDateLabel, BorderLayout.NORTH);

        model = new TaskTableModel();
        table = new JTable(model);

        table.setRowSorter(new TableRowSorter<>(model));
        // 컬럼 너비
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton addTaskBtn  = new JButton("할 일 추가");
        JButton editTaskBtn = new JButton("할 일 수정");
        JButton delTaskBtn  = new JButton("할 일 삭제");
        buttonPanel.add(addTaskBtn);
        buttonPanel.add(editTaskBtn);
        buttonPanel.add(delTaskBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // 추가
        addTaskBtn.addActionListener(e -> {
            if (currentUserId == null || currentUserId.isBlank()) {                 // ✅ ADDED: 방어
                JOptionPane.showMessageDialog(this, "로그인 사용자 정보를 먼저 설정하세요.");
                return;
            }

            TaskDialog dialog = new TaskDialog((JFrame) SwingUtilities.getWindowAncestor(this), currentDate);
            dialog.setVisible(true);
            Task t = dialog.getTask();
            if (t == null) return;

            //  CHANGED: DB INSERT 추가
            t.setUserId(currentUserId);
            int newId = TodoDao.insert(t); 
            t.setId(newId);

            model.addTask(t); // 화면 갱신
        });

        // 수정
        editTaskBtn.addActionListener(e -> {
            int viewRow = table.getSelectedRow();                                   // ✅ CHANGED: viewRow로
            if (viewRow < 0) { 
                JOptionPane.showMessageDialog(this, "수정할 할 일을 선택해주세요."); 
                return; 
            }
            int row = table.convertRowIndexToModel(viewRow);                        // ✅ ADDED: 정렬/필터 안전 변환

            Task original = model.getTaskAt(row);

            TaskDialog dialog = new TaskDialog((JFrame) SwingUtilities.getWindowAncestor(this), original);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            Task updated = dialog.getTask();
            if (updated == null) return;

            // id/userId 유지 보장
            updated.setId(original.getId());
            updated.setUserId(original.getUserId());

            // CHANGED: DB UPDATE 추가
            TodoDao.update(updated);

            model.updateTask(row, updated); // 화면 갱신
        });

        // 삭제
        delTaskBtn.addActionListener(e -> {
            int viewRow = table.getSelectedRow();                                   // ✅ CHANGED: viewRow로
            if (viewRow < 0) { 
                JOptionPane.showMessageDialog(this, "삭제할 할 일을 선택해주세요."); 
                return; 
            }
            int confirm = JOptionPane.showConfirmDialog(this, "이 할 일을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            int row = table.convertRowIndexToModel(viewRow);                        // ✅ ADDED: 정렬/필터 안전 변환
            Task t = model.getTaskAt(row);

            // CHANGED: DB DELETE 추가
            TodoDao.delete(t.getId(), t.getUserId());

            model.removeAt(row); // 화면 갱신
        });
    }

    // 날짜 변경 시 목록 로드
    public void loadTasksForDate(LocalDate date) {
        this.currentDate = date;
        selectedDateLabel.setText(date.format(DateTimeFormatter.ofPattern("MM/dd/YYYY (E)")));

        // DB에서 읽어오는 경우:
        model.getAll().clear();
        if (currentUserId != null && !currentUserId.isBlank()) {
            model.getAll().addAll(TodoDao.findByDate(currentUserId, date));
        }
        model.fireTableDataChanged();

        
    }
}
