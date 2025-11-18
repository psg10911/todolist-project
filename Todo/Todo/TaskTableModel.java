package Todo;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TaskTableModel extends AbstractTableModel {

    private final String[] columns = {"완료", "일정 제목", "시작일", "종료일"}; // 화면에 보일 컬럼만
    private final List<Task> rows = new ArrayList<>();

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int c) { return columns[c]; }

    @Override
    public Class<?> getColumnClass(int c) {
        return (c == 0) ? Boolean.class : String.class;
    }

    @Override
    public boolean isCellEditable(int r, int c) {
        return c == 0; // 체크박스만 직접 수정 가능(완료 토글)
    }

    @Override
    public Object getValueAt(int r, int c) {
        Task t = rows.get(r);
        switch (c) {
            case 0: return t.isCompleted();
            case 1: return t.getTitle();
            case 2: return t.getStartDate();
            case 3: return t.getEndDate();
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int r, int c) {
        Task t = rows.get(r);
        if (c == 0 && aValue instanceof Boolean) {
            t.setCompleted((Boolean) aValue);
            // TODO: 필요하면 여기서 즉시 DB UPDATE(t) 호출 가능
        }
        fireTableCellUpdated(r, c);
    }

    
    public void addTask(Task t) {
        rows.add(t);
        int idx = rows.size() - 1;
        fireTableRowsInserted(idx, idx);
    }

    public void updateTask(int row, Task updated) {
        rows.set(row, updated);
        fireTableRowsUpdated(row, row);
    }

    public void removeAt(int row) {
        rows.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public Task getTaskAt(int row) {
        return rows.get(row);
    }

    public List<Task> getAll() { return rows; }
}
