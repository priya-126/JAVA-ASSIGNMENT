import java.sql.*;
public class StudentDAO {
    public void addStudent(String roll, String name, String dept, int sem) throws SQLException {
        String sql = "INSERT INTO students(roll_no,name,department,current_semester) VALUES(?,?,?,?)";
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,roll);p.setString(2,name);p.setString(3,dept);p.setInt(4,sem);p.executeUpdate();
        }
    }
}
