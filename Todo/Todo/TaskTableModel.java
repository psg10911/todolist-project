package Todo;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TaskTableModel extends AbstractTableModel {

    private final String[] columns = {"완료", "일정 제목", "시작일", "종료일", "중요도"};
    private final List<Task> rows = new ArrayList<>();

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int c) { return columns[c]; }

    @Override
    public Class<?> getColumnClass(int c) {
        if (c == 0) return Boolean.class;
        return String.class;
    }

    @Override
    public boolean isCellEditable(int r, int c) {
        return c == 0;
    }

    @Override
    public Object getValueAt(int r, int c) {
        Task t = rows.get(r);
        switch (c) {
            case 0: return t.isCompleted();
            case 1: return t.getTitle();
            case 2: return t.getStartDate();
            case 3: return t.getEndDate();
            case 4:
                int p = t.getPriority();
                return (p == 1 ? "높음" : p == 2 ? "보통" : "낮음");
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int r, int c) {
        Task t = rows.get(r);
        if (c == 0 && aValue instanceof Boolean) {
            t.setCompleted((Boolean) aValue);
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

    // ★ 반드시 필요 — 내부 클래스 삭제 후 외부로 옮긴 moveRow
    public void moveRow(int from, int to) {
        if (from == to) return;
        Task t = rows.remove(from);
        rows.add(to, t);

        int a = Math.min(from, to);
        int b = Math.max(from, to);
        fireTableRowsUpdated(a, b);
    }
}
