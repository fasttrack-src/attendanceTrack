package main.java.api;

public class GetStudentHistoryRequest {
	private String guid;
	private String studentLogin;
	
	public String getGuid() {
		return guid;
	}
	
	public String getStudentLogin() {
		return studentLogin;
	}
	
	public void setGuid(String guid) {
		this.guid = guid;
	}
	
	public void setStudentLogin(String studentLogin) {
		this.studentLogin = studentLogin;
	}
}
