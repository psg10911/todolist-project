package Todo;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TaskTableModel extends AbstractTableModel {

    private final String[] columns = {"완료", "일정 제목", "시작", "종료", "중요도"};
    private final List<Task> rows = new ArrayList<>();

    // 날짜 파싱 및 포맷팅을 위한 포매터 정의
    private static final DateTimeFormatter DB_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TIME_ONLY_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FMT = DateTimeFormatter.ofPattern("MM-dd");

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
        
        // ★ [핵심 수정] 이 일정이 '하루짜리(시간 단위)'인지 '기간(하루 이상)'인지 판단
        boolean isSingleDay = isSameDay(t.getStartDate(), t.getEndDate());

        switch (c) {
            case 0: return t.isCompleted();
            case 1: return t.getTitle();
            // 하루짜리 일정이면 '시간'을, 기간 일정이면 '날짜'를 표시
            case 2: return formatDisplay(t.getStartDate(), isSingleDay); 
            case 3: return formatDisplay(t.getEndDate(), isSingleDay);   
            case 4:
                int p = t.getPriority();
                return (p == 1 ? "높음" : p == 2 ? "보통" : "낮음");
        }
        return null;
    }

    // 시작일과 종료일이 같은 날짜인지 확인하는 헬퍼 메서드
    private boolean isSameDay(String startStr, String endStr) {
        if (startStr == null || endStr == null) return false;
        try {
            if (startStr.length() > 16) startStr = startStr.substring(0, 16);
            if (endStr.length() > 16) endStr = endStr.substring(0, 16);
            
            LocalDate sDate = LocalDateTime.parse(startStr, DB_FMT).toLocalDate();
            LocalDate eDate = LocalDateTime.parse(endStr, DB_FMT).toLocalDate();
            
            return sDate.equals(eDate);
        } catch (Exception e) {
            return false;
        }
    }

    // 날짜/시간 표시 포맷팅
    private String formatDisplay(String dateStr, boolean showTime) {
        if (dateStr == null || dateStr.isEmpty()) return "";
        
        try {
            if (dateStr.length() > 16) dateStr = dateStr.substring(0, 16);
            LocalDateTime dateTime = LocalDateTime.parse(dateStr, DB_FMT);
            
            if (showTime) {
                return dateTime.format(TIME_ONLY_FMT); // 예: 14:00
            } else {
                return dateTime.format(DATE_ONLY_FMT); // 예: 11-28
            }
        } catch (Exception e) {
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