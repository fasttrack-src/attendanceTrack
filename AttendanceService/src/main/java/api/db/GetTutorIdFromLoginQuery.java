package main.java.api.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetTutorIdFromLoginQuery {
	private static Logger LOGGER = LogManager.getLogger("GetTutorIdFromLoginQuery");
	
	private String sql;
	private PreparedStatement stmt;
	
	public GetTutorIdFromLoginQuery(Connection conn) throws SQLException {
		sql = "SELECT id FROM tutor WHERE login = ? "
				+ "UNION "
				+ "SELECT id FROM academics WHERE left(email,locate(\"@\",email)-1) = ?";
		stmt = conn.prepareStatement(sql);
	}
	
	public List<Integer> getTutorId(String login) throws SQLException {
		ArrayList<Integer> tutorIds = new ArrayList<>();
		stmt.setString(1, login);
		stmt.setString(2, login);
		LOGGER.info("Executing query to get tutor ID for login: " + login);
		stmt.execute();
	
		ResultSet rs = stmt.getResultSet();
		while(rs.next()) {
			tutorIds.add(rs.getInt("id"));
		}
		return tutorIds;
	}
}
