package main.java.api.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * Checks whether specified student has attended today
 */
public class StudentAttendanceTodayQuery{
	private static Logger LOGGER = LogManager.getLogger("StudentAttendanceTodayQuery");

	private PreparedStatement stmt;
	private Connection conn;
	
	public StudentAttendanceTodayQuery(Connection conn) throws SQLException {
		this.conn = conn;
		String sql = "SELECT COUNT(u.login) AS attendancenum FROM lectureattendance a, undergrad u"
				+ " WHERE a.barcode = u.barcode AND u.login = ? AND a.year = ? AND month = ? AND day = ? AND a.course = ?";
		stmt = this.conn.prepareStatement(sql);
	}
	
	public boolean didStudentAttendToday(String studentLogin, String course) throws SQLException {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		//Months in calendar start at 0
		int month = c.get(Calendar.MONTH) + 1;
		int date = c.get(Calendar.DATE);
		LOGGER.info(String.format(
				"Executing query to check whether student has attended today. Student: %s Date: %d/%d/%d Course: %s", 
				studentLogin, 
				year, 
				month, 
				date,
				course));
		return checkAttendance(studentLogin, course, year, month, date);
	}
	
	public boolean checkAttendance(String studentLogin, String course, int year, int month, int date) throws SQLException{		
		stmt.setString(1, studentLogin);
		stmt.setInt(2, year);
		stmt.setInt(3, month);
		stmt.setInt(4, date);
		stmt.setString(5, course);
		stmt.execute();
		ResultSet rs = stmt.getResultSet();
		rs.next();
		return rs.getInt("attendancenum") > 0;
	}
}
