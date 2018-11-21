package main.java.api;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a request to validate login details
 * Used in AttendanceService.login() method
 * @author Maria
 *
 */
@XmlRootElement
public class LoginRequest {
	private String guid;
	private String password;

	public String getGuid() {
		return guid;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setGuid(String guid) {
		this.guid = guid;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
}
