package main.java.api.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import main.java.api.SaveAttendanceRequest;

/**
 * Query to insert an item of attendance into the lectureattendance table
 * @author Maria
 *
 */
public class SubmitAttendanceQuery {
	private static Logger LOGGER = LogManager.getLogger("SubmitAttendanceQuery");
	
	private String sql;
	private PreparedStatement stmt;
	private Connection conn;
	
	public SubmitAttendanceQuery(Connection conn) throws SQLException {
		this.conn = conn;
		sql = "INSERT INTO lectureattendance" +
				"(barcode, day, month, year, hour, minute, course) VALUES" +
				"(?,?,?,?,?,?,?)";
		stmt = this.conn.prepareStatement(sql);
	}
	
	public void executeQuery(SaveAttendanceRequest request) throws SQLException {
		stmt.setString(1, request.getBarcode());
		stmt.setInt(2, request.getDate());
		stmt.setInt(3, request.getMonth());
		stmt.setInt(4, request.getYear());
		stmt.setInt(5, request.getHour());
		stmt.setInt(6, request.getMinute());
		stmt.setString(7, request.getCourse());
		LOGGER.info("Executing query to save student attendance. " + request);
		stmt.executeUpdate();
	}
}
