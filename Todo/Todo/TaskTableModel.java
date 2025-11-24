package Todo;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TaskTableModel extends AbstractTableModel {

    private final String[] columns = {"완료", "일정 제목", "시작일", "종료일", "중요도"};
    private final List<Task> rows = new ArrayList<>();

    // 날짜 파싱 및 포맷팅을 위한 포매터 정의
    private static final DateTimeFormatter DB_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TIME_ONLY_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FMT = DateTimeFormatter.ofPattern("MM-dd"); // 또는 "yyyy-MM-dd"

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
        return c == 0; // 체크박스만 수정 가능
    }

    @Override
    public Object getValueAt(int r, int c) {
        Task t = rows.get(r);
        switch (c) {
            case 0: return t.isCompleted();
            case 1: return t.getTitle();
            case 2: return formatDisplayDate(t.getStartDate()); // ★ 수정됨
            case 3: return formatDisplayDate(t.getEndDate());   // ★ 수정됨
            case 4:
                int p = t.getPriority();
                return (p == 1 ? "높음" : p == 2 ? "보통" : "낮음");
        }
        return null;
    }

    // ★ 날짜 표시 로직 메서드
    private String formatDisplayDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";
        
        try {
            // 1. 문자열을 날짜 객체로 변환
            // (초 단위가 붙어있을 경우 잘라내기)
            if (dateStr.length() > 16) dateStr = dateStr.substring(0, 16);
            LocalDateTime dateTime = LocalDateTime.parse(dateStr, DB_FMT);
            
            LocalDate taskDate = dateTime.toLocalDate();
            LocalDate today = LocalDate.now();

            // 2. 날짜 비교
            if (taskDate.equals(today)) {
                // 오늘이면 -> 시간만 표시 (예: 14:00)
                return dateTime.format(TIME_ONLY_FMT);
            } else {
                // 오늘이 아니면 -> 날짜만 표시 (예: 11-25)
                return dateTime.format(DATE_ONLY_FMT);
            }
        } catch (Exception e) {
            // 파싱 실패 시 원본 그대로 표시
            return dateStr;
        }
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

    public void moveRow(int from, int to) {
        if (from == to) return;
        Task t = rows.remove(from);
        rows.add(to, t);

        int a = Math.min(from, to);
        int b = Math.max(from, to);
        fireTableRowsUpdated(a, b);
    }
}