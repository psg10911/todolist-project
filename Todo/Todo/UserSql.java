package Todo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

//Connection (SQL연결하는세션), PreparedStatement(컴파일된 바인딩 SQL코드),
//executeUpdate(INSERT, UPDATE, DELETE 같은 SQL문 실행), executeQuery(SELECT 같은 SQL문 실행)

public class UserSql {
    // 회원가입
    public static boolean insertUser(String id, String pw) {
        String sql = "INSERT INTO users (user_id, user_pw) VALUES (?, ?)";

        try (Connection conn = DBUtill.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 첫번째 ?에 id, 두번째 ?에 pw
            pstmt.setString(1, id);
            pstmt.setString(2, pw);

            
            return pstmt.executeUpdate() > 0; //반환하는행의 수 >0 이면 성공

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // 회원탈퇴
    public static boolean deleteUser(String id) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DBUtill.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            // executeUpdate()의 반환값이 1 이상이면 성공
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // 로그인
    public static boolean loginUser(String id, String pw) {
        final String sql = "SELECT 1 FROM users WHERE user_id = ? AND user_pw = ? LIMIT 1";
        try (Connection conn = DBUtill.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id.trim());
            ps.setString(2, pw.trim());

            try (var rs = ps.executeQuery()) {
                return rs.next(); // 있으면 true
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}

