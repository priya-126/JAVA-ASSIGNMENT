import java.sql.*;
public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/kec_academic";
    private static final String USER = "root";
    private static final String PASSWORD = "Kec@2026Demo!";
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
