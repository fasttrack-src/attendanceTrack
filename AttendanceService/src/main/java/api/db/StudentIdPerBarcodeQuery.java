package main.java.api.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import main.java.api.Student;

//Query to return all students associated with a barcode
public class StudentIdPerBarcodeQuery {
	private static Logger LOGGER = LogManager.getLogger("StudentIdPerBarcodeQuery");
	
	private String sql;
	private PreparedStatement stmt;
	
	public StudentIdPerBarcodeQuery(Connection conn) throws SQLException {
		sql = "SELECT id, login FROM undergrad WHERE barcode = ?";
		stmt = conn.prepareStatement(sql);
	}
	
	public List<Student> getStudentPerBarcode(String barcode) throws SQLException {
		ArrayList<Student> studentIds = new ArrayList<>();
		stmt.setString(1, barcode);
		LOGGER.info("Executing query to get student ID for barcode: " + barcode);
		stmt.execute();
	
		ResultSet rs = stmt.getResultSet();
		while(rs.next()) {
			studentIds.add(new Student(rs.getString("login"), rs.getInt("id")));
		}
		return studentIds;
	}
}
