package main.java.api;

public class LoginResponse {
	private boolean isTutor;
	
	public LoginResponse(boolean isTutor) {
		this.isTutor = isTutor;
	}
	
	public boolean getIsTutor() {
		return isTutor;
	}
	
	public void setIsTutor(boolean isTutor) {
		this.isTutor = isTutor;
	}
}
