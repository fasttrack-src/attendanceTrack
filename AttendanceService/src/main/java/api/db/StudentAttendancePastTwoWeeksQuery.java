package main.java.api.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Checks whether a specified student has attended at least once in the past two weeks
 *  Only executed if student hasn't attended today
 * @author Maria
 *
 */
public class StudentAttendancePastTwoWeeksQuery {
	private static Logger LOGGER = LogManager.getLogger("StudentAttendancePastTwoWeeksQuery");

	private String sql;
	private PreparedStatement stmt;
	private Connection conn;

	public StudentAttendancePastTwoWeeksQuery(Connection conn) throws SQLException {
		this.conn = conn;
		sql = "SELECT COUNT(u.login) AS attendancenum FROM lectureattendance a, undergrad u"
				+ " WHERE a.barcode = u.barcode AND u.login = ? AND (a.year > ? "
				+ "OR (a.year = ? AND (month > ? OR (a.month = ? AND day >= ? )))) AND a.course = ?";
		stmt = this.conn.prepareStatement(sql);
	}

	public boolean didStudentAttendInThePastTwoWeeks(String studentLogin, String course)
			throws SQLException {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, -14);
		int year = c.get(Calendar.YEAR);
		// Months in calendar start at 0
		int month = c.get(Calendar.MONTH) + 1;
		int date = c.get(Calendar.DATE);
		LOGGER.info(
				String.format(
						"Executing query to check whether student has attended the past two weeks. Student: %s "
								+ "Date two weeks ago: %4d/%2d/%2d Course: %s",
						studentLogin, year, month, date, course));
		return checkAttendance(studentLogin, course, year, month, date);
	}

	public boolean checkAttendance(String studentLogin, String course, int year, int month, int date)
			throws SQLException {
		stmt.setString(1, studentLogin);
		stmt.setInt(2, year);
		stmt.setInt(3, year);
		stmt.setInt(4, month);
		stmt.setInt(5, month);
		stmt.setInt(6, date);
		stmt.setString(7, course);
		stmt.execute();
		ResultSet rs = stmt.getResultSet();
		rs.next();
		return rs.getInt("attendancenum") > 0;
	}
}
