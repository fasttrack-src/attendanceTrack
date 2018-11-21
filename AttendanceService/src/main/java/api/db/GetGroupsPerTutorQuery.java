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
 * Queries all tutorial groups assigned to a tutor
 * Adapted from sp_tutorialGroupsForTutor
 * @author Maria
 *
 */
public class GetGroupsPerTutorQuery {
	private static Logger LOGGER = LogManager.getLogger("GetGroupsPerTutorQuery");
	
	private String sql;
	private PreparedStatement stmt;
	private Connection conn;
	
	public GetGroupsPerTutorQuery(Connection conn) throws SQLException {
		this.conn = conn;
		sql = "SELECT c.name AS Course, c.id as CourseId, g.groupname AS GroupName, g.id AS GroupID" + 
				" FROM tutor t" + 
				" JOIN tutorassign a ON t.id = a.tutorid" + 
				" JOIN tutorialgroup g ON g.id = a.tutorialgroupid" + 
				" JOIN course c ON c.id = g.courseid" + 
				" WHERE EXISTS (SELECT DISTINCT courseid FROM assign t WHERE sessionNumber = " +
				"(SELECT DISTINCT sessionNumber FROM assign ORDER BY sessionNumber desc LIMIT 1) AND t.courseid = c.id)" + 
				" AND t.id = ?" + 
				" UNION" + 
				" SELECT " +
				" c.name AS Course, c.id as CourseId, g.groupname AS GroupName, g.id AS GroupID" + 
				" FROM academics t" + 
				" JOIN tutorassign a ON t.id = a.academicid" + 
				" JOIN tutorialgroup g ON g.id = a.tutorialgroupid" + 
				" JOIN course c ON c.id = g.courseid" + 
				" WHERE EXISTS (SELECT DISTINCT courseid FROM assign t WHERE sessionNumber = " +
				"(SELECT DISTINCT sessionNumber FROM assign ORDER BY sessionNumber desc LIMIT 1) AND t.courseid = c.id) AND t.id <> 39" + 
				" AND t.id = ?" + 
				" ORDER BY course, groupname;";
		stmt = this.conn.prepareStatement(sql);
	}
	
	public List<TutorialGroup> getGroupsPerTutor(int tutorId) throws SQLException {
		ArrayList<TutorialGroup> groups = new ArrayList<>();

		stmt.setInt(1, tutorId);
		stmt.setInt(2, tutorId);
		LOGGER.info("Executing query to get tutorial groups per tutor. Tutor ID: " + tutorId);
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
