package Todo;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TaskTableModel extends AbstractTableModel {

    // ★ 컬럼 분리 확인: "일시" -> "시작", "종료"
    private final String[] columns = {"완료", "일정 제목", "시작", "종료", "중요도"};
    private final List<Task> rows = new ArrayList<>();

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int c) { return columns[c]; }
    
    @Override
    public Class<?> getColumnClass(int c) {
        if (c == 0) return Boolean.class;
        return String.class;
    }

    @Override public boolean isCellEditable(int r, int c) { return c == 0; }

    @Override
    public Object getValueAt(int r, int c) {
        Task t = rows.get(r);
        
        switch (c) {
            case 0: return t.isCompleted();
            case 1: return t.getTitle();
            
            // ★ [핵심] 다형성 적용
            // t가 TimeTask면 "14:00", PeriodTask면 "11-26"이 나옴
            case 2: return t.getFormattedStart(); 
            
            // ★ [핵심] 다형성 적용
            // t가 TimeTask면 "16:00", PeriodTask면 "11-28"이 나옴
            case 3: return t.getFormattedEnd();   
            
            case 4:
                Priority p = t.getPriority();
                return (p == Priority.HIGH ? "높음" : p == Priority.MEDIUM ? "보통" : "낮음");
        }
        return null;
    }

    // ... (나머지 코드는 기존과 동일하게 유지) ...
    @Override
    public void setValueAt(Object aValue, int r, int c) {
        Task t = rows.get(r);
        if (c == 0 && aValue instanceof Boolean) {
            t.setCompleted((Boolean) aValue);
        }
        fireTableCellUpdated(r, c);
    }
    public void addTask(Task t) { rows.add(t); int idx = rows.size() - 1; fireTableRowsInserted(idx, idx); }
    public void updateTask(int row, Task updated) { rows.set(row, updated); fireTableRowsUpdated(row, row); }
    public void removeAt(int row) { rows.remove(row); fireTableRowsDeleted(row, row); }
    public Task getTaskAt(int row) { return rows.get(row); }
    public List<Task> getAll() { return rows; }
    public void moveRow(int from, int to) {
        if (from == to) return;
        Task t = rows.remove(from);
        rows.add(to, t);
        fireTableRowsUpdated(Math.min(from, to), Math.max(from, to));
    }
}