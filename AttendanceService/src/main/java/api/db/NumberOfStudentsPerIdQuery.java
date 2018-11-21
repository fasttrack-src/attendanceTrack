package main.java.api.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NumberOfStudentsPerIdQuery {
private static Logger LOGGER = LogManager.getLogger("NumberOfStudentsPerIdQuery");
	
	private String sql;
	private PreparedStatement stmt;
	
	public NumberOfStudentsPerIdQuery(Connection conn) throws SQLException {
		sql = "SELECT COUNT(id) AS numstudents FROM undergrad WHERE login = ?";
		stmt = conn.prepareStatement(sql);
	}
	
	public int getNumStudents(String login) throws SQLException {
		stmt.setString(1, login);
		LOGGER.info("Executing query to get number of students for login: " + login);
		stmt.execute();
	
		ResultSet rs = stmt.getResultSet();
		rs.next();
		return rs.getInt("numstudents");
	}
}
