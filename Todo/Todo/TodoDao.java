package Todo;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class TodoDao {

    private static final DateTimeFormatter DT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static Timestamp toTs(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        if (s.length() == 10) s = s + " 00:00:00";
        else if (s.length() == 16) s = s + ":00";
        try { return Timestamp.valueOf(s); } 
        catch (IllegalArgumentException ex) { return null; }
    }

    public static String tsToString(Timestamp ts) {
        if (ts == null) return null;
        return ts.toLocalDateTime().format(DT_FORMAT);
    }

    public static Task findById(int id) {
        String sql = "SELECT id, user_id, title, content, startDate, endDate, completed, priority, dtype FROM todos WHERE id=?";
        try (Connection con = DBUtill.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToTask(rs); // ★ 헬퍼 메서드로 분리
                }
                return null;
            }
        } catch (SQLException ex) { throw new RuntimeException(ex); }
    }

    public static int upsert(Task t) {
        // dtype 컬럼 추가
        String sql = "INSERT INTO todos (user_id, title, content, startDate, endDate, completed, priority, dtype) VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE content=VALUES(content), endDate=VALUES(endDate), completed=VALUES(completed), priority=VALUES(priority), dtype=VALUES(dtype), id=LAST_INSERT_ID(id)";
        try (Connection con = DBUtill.getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getUserId()); ps.setString(2, t.getTitle()); ps.setString(3, t.getContent());
            ps.setTimestamp(4, toTs(t.getStartDate())); ps.setTimestamp(5, toTs(t.getEndDate()));
            ps.setBoolean(6, t.isCompleted()); ps.setInt(7, t.getPriority().getDbValue());
            
            // ★ 다형성: 객체 타입에 따라 dtype 문자열 결정
            String dtype = (t instanceof PeriodTask) ? "PERIOD" : "TIME";
            ps.setString(8, dtype);

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) { int id = rs.getInt(1); t.setId(id); return id; }
            }
            return (t.getId() > 0) ? t.getId() : -1;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public static int insert(Task t){ return upsert(t); }

    public static void update(Task t){
        // update는 upsert로 처리되거나, 별도 update문에도 dtype을 추가해줄 수 있습니다.
        // 여기서는 간단히 upsert 로직을 따르거나 기존 update문에 dtype만 추가하면 됩니다.
        String sql = "UPDATE todos SET title=?, content=?, startDate=?, endDate=?, completed=?, priority=?, dtype=? WHERE id=? AND user_id=?";
        try (Connection con = DBUtill.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getTitle()); ps.setString(2, t.getContent());
            ps.setTimestamp(3, toTs(t.getStartDate())); ps.setTimestamp(4, toTs(t.getEndDate()));
            ps.setBoolean(5, t.isCompleted()); ps.setInt(6, t.getPriority().getDbValue());
            
            String dtype = (t instanceof PeriodTask) ? "PERIOD" : "TIME";
            ps.setString(7, dtype);

            ps.setInt(8, t.getId()); ps.setString(9, t.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public static void delete(int id, String userId){
        String sql = "DELETE FROM todos WHERE id=? AND user_id=?";
        try (Connection con = DBUtill.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id); ps.setString(2, userId); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
    

    // 변경
    // 특정 사용자가 할 일이 하나라도 있는지 확인하는 메서드
    // 이미 데이터가 로드되어있대면 다시 로드 방지
    public static boolean hasAnyTodo(String userId) {
        String sql = "SELECT 1 FROM todos WHERE user_id = ? LIMIT 1";
        try (Connection con = DBUtill.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // 레코드가 하나라도 있으면 true
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static List<Task> findByDate(String userId, LocalDate date){
        LocalDateTime s = date.atStartOfDay();
        LocalDateTime e = date.atTime(LocalTime.MAX);
        return findByRange(userId, s, e);
    }

    

    // ★ [주간 시간표용] 특정 기간의 일정을 모두 가져오는 메서드
    public static List<Task> findByRange(String userId, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT id, user_id, title, content, startDate, endDate, completed, priority, dtype " +
                     "FROM todos " +
                     "WHERE user_id=? AND (" +
                     "     (startDate <= ? AND endDate >= ?) OR " + 
                     "     (startDate IS NULL AND endDate IS NULL)" + 
                     ") ORDER BY startDate";

        List<Task> list = new ArrayList<>();
        try (Connection con = DBUtill.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(end));   
            ps.setTimestamp(3, Timestamp.valueOf(start)); 

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToTask(rs)); // ★ 헬퍼 메서드 사용
                }
            }
        } catch (SQLException ex) { throw new RuntimeException(ex); }
        return list;
    }

    private static Task mapRowToTask(ResultSet rs) throws SQLException {
        String type = rs.getString("dtype");
        Task t;
        
        int id = rs.getInt("id");
        String uid = rs.getString("user_id");
        String title = rs.getString("title");
        String content = rs.getString("content");
        String sDate = tsToString(rs.getTimestamp("startDate"));
        String eDate = tsToString(rs.getTimestamp("endDate"));
        boolean comp = rs.getBoolean("completed");
        int priInt = rs.getInt("priority");
    Priority priority = Priority.fromDbValue(priInt); 

        // ★ [수정] 생성자에 int 대신 priority(Enum) 전달
        if ("PERIOD".equals(type)) {
            t = new PeriodTask(id, uid, title, content, sDate, eDate, comp, priority);
        } else {
            t = new TimeTask(id, uid, title, content, sDate, eDate, comp, priority);
        }
        return t;
    }

    
    
}