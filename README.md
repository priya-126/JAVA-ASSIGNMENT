
package com.studentacademic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/students")
public class StudentManagementServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try (Connection con = DBConnection.getConnection();
             PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html><html><head>");
            out.println("<title>Student Management</title>");
            out.println("<style>");
            out.println("body{font-family:Arial;margin:35px;background:#f2f5f9}");
            out.println("h1{color:#17365d}");
            out.println("table{border-collapse:collapse;width:100%;background:white}");
            out.println("th,td{border:1px solid #ccc;padding:12px;text-align:left}");
            out.println("th{background:#17365d;color:white}");
            out.println("</style></head><body>");

            out.println("<h1>Student Academic Management System</h1>");
            out.println("<h2>Student List</h2>");
            out.println("<table><tr><th>Roll Number</th>");
            out.println("<th>Name</th><th>Department</th><th>Semester</th></tr>");

            String sql = "SELECT roll_no, name, department, current_semester "
                       + "FROM students ORDER BY roll_no";

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    out.println("<tr>");
                    out.println("<td>" + escape(rs.getString("roll_no")) + "</td>");
                    out.println("<td>" + escape(rs.getString("name")) + "</td>");
                    out.println("<td>" + escape(rs.getString("department")) + "</td>");
                    out.println("<td>" + rs.getInt("current_semester") + "</td>");
                    out.println("</tr>");
                }
            }

            out.println("</table></body></html>");

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database error: "
                    + escape(e.getMessage()));
        }
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
