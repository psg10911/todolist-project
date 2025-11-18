package Todo;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TodoDao {

    // 문자열 → Timestamp
    private static Timestamp toTs(String s){
        if (s == null || s.isBlank()) return null;
        if (s.length() == 10) s += " 00:00:00"; // yyyy-MM-dd만 온 경우 00:00:00 보정
        return Timestamp.valueOf(s);
    }

    // Timestamp → "yyyy-MM-dd HH:mm:ss"
    private static String tsToString(Timestamp ts) {
        if (ts == null) return null;
        return ts.toLocalDateTime().toString().replace('T', ' ');
    }

    /** INSERT: 생성된 PK(id) 반환 */
    public static int insert(Task t){
        String sql = "INSERT INTO todos (user_id, title, content, startDate, endDate, completed) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBUtill.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, t.getUserId());
            ps.setString(2, t.getTitle());
            ps.setString(3, t.getContent());
            ps.setTimestamp(4, toTs(t.getStartDate()));
            ps.setTimestamp(5, toTs(t.getEndDate()));
            ps.setBoolean(6, t.isCompleted());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** UPDATE: id + user_id로 안전하게 */
    public static void update(Task t){
        String sql = "UPDATE todos SET title=?, content=?, startDate=?, endDate=?, completed=? " +
                     "WHERE id=? AND user_id=?";
        try (Connection con = DBUtill.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, t.getTitle());
            ps.setString(2, t.getContent());
            ps.setTimestamp(3, toTs(t.getStartDate()));
            ps.setTimestamp(4, toTs(t.getEndDate()));
            ps.setBoolean(5, t.isCompleted());
            ps.setInt(6, t.getId());
            ps.setString(7, t.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** DELETE */
    public static void delete(int id, String userId){
        String sql = "DELETE FROM todos WHERE id=? AND user_id=?";
        try (Connection con = DBUtill.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** 날짜별 조회 예시: 해당 날짜의 시작~끝에 걸치는 일정 포함 */
    public static List<Task> findByDate(String userId, LocalDate date){
        LocalDateTime s = date.atStartOfDay();
        LocalDateTime e = date.atTime(LocalTime.MAX);

        String sql = "SELECT id, user_id, title, content, startDate, endDate, completed " +
                     "FROM todos " +
                     "WHERE user_id=? AND (" +
                     "      (startDate BETWEEN ? AND ?) OR " +
                     "      (endDate   BETWEEN ? AND ?) OR " +
                     "      (startDate IS NULL AND endDate IS NULL)" +
                     ") " +
                     "ORDER BY COALESCE(startDate, endDate)";

        List<Task> list = new ArrayList<>();
        try (Connection con = DBUtill.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(s));
            ps.setTimestamp(3, Timestamp.valueOf(e));
            ps.setTimestamp(4, Timestamp.valueOf(s));
            ps.setTimestamp(5, Timestamp.valueOf(e));

            try(ResultSet rs = ps.executeQuery()){
                while (rs.next()) {
                    Task t = new Task(
                        rs.getInt("id"),
                        rs.getString("user_id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        tsToString(rs.getTimestamp("startDate")),
                        tsToString(rs.getTimestamp("endDate")),
                        rs.getBoolean("completed")
                    );
                    list.add(t);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return list;
    }
}
