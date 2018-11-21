package main.java.api.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import main.java.api.DeleteAttendanceRequest;

/*
 * Used to delete an item of attendance from the lectureattendance table
 */
public class DeleteAttendanceQuery {
	private static Logger LOGGER = LogManager.getLogger("DeleteAttendanceQuery");
	
	private String sql;
	private PreparedStatement stmt;
	
	public DeleteAttendanceQuery(Connection conn) throws SQLException {
		sql = "DELETE FROM lectureattendance" +
				" WHERE barcode = ? AND course = ? AND year = ? AND month = ? AND day = ?";
		stmt = conn.prepareStatement(sql);
	}

	public void executeQuery(DeleteAttendanceRequest request) throws SQLException {
		stmt.setString(1, request.getBarcode());
		stmt.setString(2, request.getCourse());
		stmt.setInt(3, request.getYear());
		stmt.setInt(4, request.getMonth());
		stmt.setInt(5, request.getDate());
		LOGGER.info(String.format(
				"Executing query to delete student attendance. Student barcode: %s Date: %4d/%2d/%2d Course: %s",
				request.getBarcode(),
				request.getYear(),
				request.getMonth(),
				request.getDate(),
				request.getCourse()));
		stmt.executeUpdate();
	}
}
