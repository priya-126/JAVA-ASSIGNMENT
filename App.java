package com.college;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/kec_academic";
        String username = "root";
        String password = "Kec@2026Demo!";

        String query = "UPDATE students SET name = ?, "
                + "department = ?, current_semester = ? "
                + "WHERE roll_no = ?";

        try (
            Connection con = DriverManager.getConnection(
                url, username, password);
            Scanner sc = new Scanner(System.in)
        ) {
            System.out.print("Enter Roll Number to Update: ");
            String rollNo = sc.nextLine();

            System.out.print("Enter New Name: ");
            String name = sc.nextLine();

            System.out.print("Enter New Department: ");
            String department = sc.nextLine();

            System.out.print("Enter New Semester: ");
            int semester = Integer.parseInt(sc.nextLine());

            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, name);
                ps.setString(2, department);
                ps.setInt(3, semester);
                ps.setString(4, rollNo);

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    System.out.println("Student Updated Successfully!");
                } else {
                    System.out.println("Roll Number Not Found!");
                }
            }

        } catch (SQLException | NumberFormatException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
