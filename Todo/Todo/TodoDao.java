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

    // ★ 문자열 → Timestamp 변환 유틸리티
    public static Timestamp toTs(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;

        if (s.length() == 10) {
            s = s + " 00:00:00";
        } else if (s.length() == 16) {
            s = s + ":00";
        }

        try {
            return Timestamp.valueOf(s);
        } catch (IllegalArgumentException ex) {
            System.out.println("[TodoDao.toTs] invalid datetime string: \"" + s + "\"");
            return null;
        }
    }

    // ★ Timestamp → 문자열 변환 유틸리티
    public static String tsToString(Timestamp ts) {
        if (ts == null) return null;
        return ts.toLocalDateTime().format(DT_FORMAT);
    }

    /** ID 기반 단일 조회 (수정됨) */
    public static Task findById(int id) {
        String sql =
                "SELECT id, user_id, title, content, startDate, endDate, completed, priority " +
                "FROM todos " +
                "WHERE id=?";

        try (Connection con = DBUtill.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Task t = new Task(
                        rs.getInt("id"),
                        rs.getString("user_id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        tsToString(rs.getTimestamp("startDate")),
                        tsToString(rs.getTimestamp("endDate")),
                        rs.getBoolean("completed"),
                        rs.getInt("priority") // ★ 수정: 생성자에 priority를 바로 전달
                    );
                    // t.setPriority(rs.getInt("priority")); // ★ 제거됨
                    return t;
                }
                return null;
            }

        } catch (SQLException ex) {
            throw new RuntimeException("ID로 Task 조회 중 오류 발생: " + id, ex);
        }
    }


    /** INSERT: 생성된 PK(id) 반환 */
    public static int insert(Task t){
        String sql =
                "INSERT INTO todos (user_id, title, content, startDate, endDate, completed, priority) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBUtill.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, t.getUserId());
            ps.setString(2, t.getTitle());
            ps.setString(3, t.getContent());
            ps.setTimestamp(4, toTs(t.getStartDate()));
            ps.setTimestamp(5, toTs(t.getEndDate()));
            ps.setBoolean(6, t.isCompleted());
            ps.setInt(7, t.getPriority());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** UPDATE */
    public static void update(Task t){
        String sql =
                "UPDATE todos SET title=?, content=?, startDate=?, endDate=?, completed=?, priority=? " +
                "WHERE id=? AND user_id=?";

        try (Connection con = DBUtill.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, t.getTitle());
            ps.setString(2, t.getContent());
            ps.setTimestamp(3, toTs(t.getStartDate()));
            ps.setTimestamp(4, toTs(t.getEndDate()));
            ps.setBoolean(5, t.isCompleted());
            ps.setInt(6, t.getPriority());
            ps.setInt(7, t.getId());
            ps.setString(8, t.getUserId());

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

    /** 날짜별 조회 (수정됨) */
    public static List<Task> findByDate(String userId, LocalDate date){
        LocalDateTime s = date.atStartOfDay();
        LocalDateTime e = date.atTime(LocalTime.MAX);

        String sql =
                "SELECT id, user_id, title, content, startDate, endDate, completed, priority " +
                "FROM todos " +
                "WHERE user_id=? AND (" +
                "     (startDate <= ? AND endDate >= ?) OR " +
                "     (startDate IS NULL AND endDate IS NULL)" +
                ") " +
                "ORDER BY COALESCE(startDate, endDate)";

        List<Task> list = new ArrayList<>();

        try (Connection con = DBUtill.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(e));
            ps.setTimestamp(3, Timestamp.valueOf(s));


            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    Task t = new Task(
                        rs.getInt("id"),
                        rs.getString("user_id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        tsToString(rs.getTimestamp("startDate")),
                        tsToString(rs.getTimestamp("endDate")),
                        rs.getBoolean("completed"),
                        rs.getInt("priority") // ★ 수정: 생성자에 priority를 바로 전달
                    );

                    // t.setPriority(rs.getInt("priority")); // ★ 제거됨

                    list.add(t);
                }
            }

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        return list;
    }
}