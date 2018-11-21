package main.java.api.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import main.java.api.AttendanceItem;

/**
 * Queries student attendance history for the current academic year(since September 1st)
 * @author Maria
 *
 */
public class StudentAttendanceHistoryQuery {
	private static Logger LOGGER = LogManager.getLogger("StudentAttendanceHistoryQuery");

	private String sql;
	private PreparedStatement stmt;
	private Connection conn;

	public StudentAttendanceHistoryQuery(Connection conn) throws SQLException {
		this.conn = conn;
		sql = "SELECT a.course AS course, a.year AS year, a.month AS month, a.day AS day FROM lectureattendance a, undergrad u"
				+ " WHERE a.barcode = u.barcode AND u.login = ? AND (a.year > ? "
				+ "OR (a.year = ? AND (month > ? OR (a.month = ? AND day >= ? ))))"
				+ " ORDER BY a.course, a.year DESC, a.month DESC, a.day DESC";
		stmt = this.conn.prepareStatement(sql);
	}

	public List<AttendanceItem> getAttendanceHistory(String studentLogin)
			throws SQLException {
		List<AttendanceItem> attendanceHistory = new ArrayList<>();
		
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = 9;
		int date = 1;
		if(c.get(Calendar.MONTH) < 8) {
			year = year - 1;
		}
		LOGGER.info(
				String.format(
						"Executing query to get student attendance history. Student: %s "
								+ "Start date: %4d/%2d/%2d",
						studentLogin, year, month, date));
		stmt.setString(1, studentLogin);
		stmt.setInt(2, year);
		stmt.setInt(3, year);
		stmt.setInt(4, month);
		stmt.setInt(5, month);
		stmt.setInt(6, date);
		stmt.execute();
		ResultSet rs = stmt.getResultSet();
		while(rs.next()) {
			AttendanceItem item = new AttendanceItem(rs.getString("course"), rs.getInt("year"), rs.getInt("month"), rs.getInt("day"));
			attendanceHistory.add(item);
		}
		
		return attendanceHistory;
	}
}
