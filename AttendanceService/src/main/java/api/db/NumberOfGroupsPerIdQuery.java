package main.java.api.db;

/**
 * Returns number of tutorial groups that correspond to given ID
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NumberOfGroupsPerIdQuery {
	private static Logger LOGGER = LogManager.getLogger("NumberOfGroupsPerIdQuery");
	
	private String sql;
	private PreparedStatement stmt;
	
	public NumberOfGroupsPerIdQuery(Connection conn) throws SQLException {
		sql = "SELECT COUNT(id) AS numgroups FROM tutorialgroup WHERE id = ?";
		stmt = conn.prepareStatement(sql);
	}
	
	public int getNumGroups(int id) throws SQLException {
		stmt.setInt(1, id);
		LOGGER.info("Executing query to get number of tutorial groups for ID: " + id);
		stmt.execute();
	
		ResultSet rs = stmt.getResultSet();
		rs.next();
		return rs.getInt("numgroups");
	}
}
