package main.java.api.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//Query to check whether student is enrolled in course
public class StudentTakesCourseQuery {
private static Logger LOGGER = LogManager.getLogger("StudentTakesCourseQuery");
	
	private String sql;
	private PreparedStatement stmt;
	
	public StudentTakesCourseQuery(Connection conn) throws SQLException {
		sql = "SELECT COUNT(id) as takes FROM takescourse WHERE undergradid = ? AND courseid = ?";
		stmt = conn.prepareStatement(sql);
	}
	
	public boolean doesStudentTakeCourse(int studentId, int courseId) throws SQLException {
		stmt.setInt(1, studentId);
		stmt.setInt(2, courseId);
		LOGGER.info(String.format("Executing query to check if student takes course. Student ID: %s Course ID: %s", studentId, courseId));
		stmt.execute();
	
		ResultSet rs = stmt.getResultSet();
		rs.next();
		return rs.getInt("takes") > 0;
	}
}
