package main.java.api.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import main.java.api.GetStudentsInGroupRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import main.java.api.Student;

/*
 * Queries students in a specified tutorial group
 */
public class GetStudentsInGroupQuery {
	private static Logger LOGGER = LogManager.getLogger("GetStudentsInGroupQuery");
	
	private String sql;
	private PreparedStatement stmt;
	private Connection conn;
	
	public GetStudentsInGroupQuery(Connection conn) throws SQLException {
		this.conn = conn;
		sql = "SELECT u.login,u.name,u.barcode FROM undergrad u JOIN takescourse t ON t.undergradid=u.id AND u.active=1 AND t.checkbit=1" + 
				" WHERE t.session=(SELECT DISTINCT session FROM takescourse ORDER BY session desc LIMIT 1) AND t.tutorialgroupid=?"
				+ " ORDER BY u.name";
		//Sequence of queries executed in a transaction
		this.conn.setAutoCommit(false);
		stmt = this.conn.prepareStatement(sql);
	}

	//This query also creates a StudentAttendanceTodayQuery to check if the students returned have attended today
	//This will be displayed in the Ionic app
	public List<Student> getStudentsInGroup(GetStudentsInGroupRequest request, Connection conn) throws SQLException {
		ArrayList<Student> students = new ArrayList<>();
		
		StudentAttendanceTodayQuery query = new StudentAttendanceTodayQuery(conn);
		
		stmt.setInt(1, request.getGroupId());
		LOGGER.info("Executing query to get students in tutorial group. Group ID: " + request.getGroupId());
		stmt.execute();
	
		ResultSet rs = stmt.getResultSet();
		while(rs.next()) {
			String studentLogin = rs.getString("login");
			Student s = new Student(studentLogin, rs.getString("name"), rs.getString("barcode"));
			boolean attendedToday = query.didStudentAttendToday(studentLogin, request.getCourse());
			s.setAttendedToday(attendedToday);
			if(attendedToday) {
				s.setAttendedPastTwoWeeks(true);
			}else {
				StudentAttendancePastTwoWeeksQuery pastWeeksQuery = new StudentAttendancePastTwoWeeksQuery(conn);
				s.setAttendedPastTwoWeeks(pastWeeksQuery.didStudentAttendInThePastTwoWeeks(studentLogin, request.getCourse()));
			}
			//s.setAttendedToday(query.didStudentAttendToday(studentLogin, request.getCourse(), cal));
			students.add(s);
		}
		return students;
	}
}
