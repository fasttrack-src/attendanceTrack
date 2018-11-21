package main.java.api.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import main.java.api.TutorialGroup;

/**
 * Queries all active tutorial groups
 * Adapted from sp_tutorialGroupsForTutor
 * @author Maria
 *
 */
public class GetAllGroupsQuery {

private static Logger LOGGER = LogManager.getLogger("GetAllGroupsQuery");
	
	private String sql;
	private PreparedStatement stmt;
	private Connection conn;
	
	public GetAllGroupsQuery(Connection conn) throws SQLException {
		this.conn = conn;
		sql = "SELECT c.name AS Course, c.id as CourseId, g.groupname AS GroupName, g.id AS GroupID" + 
				" FROM tutorialgroup g " + 
				" JOIN course c ON c.id = g.courseid" + 
				" WHERE EXISTS (SELECT DISTINCT courseid FROM assign t WHERE sessionNumber = " +
				"(SELECT DISTINCT sessionNumber FROM assign ORDER BY sessionNumber desc LIMIT 1) AND t.courseid = c.id)" + 
				" ORDER BY course, groupname;";
		stmt = this.conn.prepareStatement(sql);
	}
	
	public List<TutorialGroup> getGroups() throws SQLException {
		ArrayList<TutorialGroup> groups = new ArrayList<>();

		LOGGER.info("Executing query to get all tutorial groups");
		stmt.execute();
	
		ResultSet rs = stmt.getResultSet();
		while(rs.next()) {
			TutorialGroup group = 
					new TutorialGroup(rs.getString("GroupName"), rs.getInt("GroupID"), rs.getString("Course"), rs.getInt("CourseId"));
			groups.add(group);
		}
		return groups;
	}
}
