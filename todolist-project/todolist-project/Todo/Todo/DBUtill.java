package Todo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtill {
    private static final String URL  = "jdbc:mysql://localhost:3307/appdb?serverTimezone=UTC";
    private static final String USER = "appuser";
    private static final String PASS = "apppw";

    // 필요할 때마다 호출해서 커넥션 얻기
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

