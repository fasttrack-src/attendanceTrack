package main.java.api.db;

import java.sql.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to establish a connection to the database Database URL, username and
 * password specified in config file
 * 
 * @author Maria
 */
public class DbConnection {
	private static Logger LOGGER = LogManager.getLogger("DbConnection");
	private Connection con;

	public DbConnection(String url, String username, String password) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		con = DriverManager.getConnection(url, username, password);
		LOGGER.info("Attendance service connected to database successfully.");
	}

	public Connection getConnection() {
		return con;
	}
}