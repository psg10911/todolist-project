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
        String sql = "SELECT id, user_id, title, content, startDate, endDate, completed, priority FROM todos WHERE id=?";
        try (Connection con = DBUtill.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Task(
                        rs.getInt("id"), rs.getString("user_id"), rs.getString("title"), rs.getString("content"),
                        tsToString(rs.getTimestamp("startDate")), tsToString(rs.getTimestamp("endDate")),
                        rs.getBoolean("completed"), rs.getInt("priority")
                    );
                }
                return null;
            }
        } catch (SQLException ex) { throw new RuntimeException(ex); }
    }

    public static int insert(Task t){ return upsert(t); }

    public static void update(Task t){
        String sql = "UPDATE todos SET title=?, content=?, startDate=?, endDate=?, completed=?, priority=? WHERE id=? AND user_id=?";
        try (Connection con = DBUtill.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getTitle()); ps.setString(2, t.getContent());
            ps.setTimestamp(3, toTs(t.getStartDate())); ps.setTimestamp(4, toTs(t.getEndDate()));
            ps.setBoolean(5, t.isCompleted()); ps.setInt(6, t.getPriority());
            ps.setInt(7, t.getId()); ps.setString(8, t.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public static int upsert(Task t){
        String sql = "INSERT INTO todos (user_id, title, content, startDate, endDate, completed, priority) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE content=VALUES(content), endDate=VALUES(endDate), completed=VALUES(completed), priority=VALUES(priority), id=LAST_INSERT_ID(id)";
        try (Connection con = DBUtill.getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getUserId()); ps.setString(2, t.getTitle()); ps.setString(3, t.getContent());
            ps.setTimestamp(4, toTs(t.getStartDate())); ps.setTimestamp(5, toTs(t.getEndDate()));
            ps.setBoolean(6, t.isCompleted()); ps.setInt(7, t.getPriority());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) { int id = rs.getInt(1); t.setId(id); return id; }
            }
            return (t.getId() > 0) ? t.getId() : -1;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public static void delete(int id, String userId){
        String sql = "DELETE FROM todos WHERE id=? AND user_id=?";
        try (Connection con = DBUtill.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id); ps.setString(2, userId); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
    
    public static boolean hasAnyTodo(String userId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM todos WHERE user_id=? LIMIT 1)";
        try (var con = DBUtill.getConnection(); var ps  = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (var rs = ps.executeQuery()) { return rs.next() && rs.getInt(1) == 1; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public static List<Task> findByDate(String userId, LocalDate date){
        LocalDateTime s = date.atStartOfDay();
        LocalDateTime e = date.atTime(LocalTime.MAX);
        return findByRange(userId, s, e);
    }

    // ★ [주간 시간표용] 특정 기간의 일정을 모두 가져오는 메서드
    public static List<Task> findByRange(String userId, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT id, user_id, title, content, startDate, endDate, completed, priority " +
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
                    list.add(new Task(
                        rs.getInt("id"), rs.getString("user_id"), rs.getString("title"), rs.getString("content"),
                        tsToString(rs.getTimestamp("startDate")), tsToString(rs.getTimestamp("endDate")),
                        rs.getBoolean("completed"), rs.getInt("priority")
                    ));
                }
            }
        } catch (SQLException ex) { throw new RuntimeException(ex); }
        return list;
    }
}