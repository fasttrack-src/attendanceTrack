package main.java.api;

public class Student {
	private String login;
	private String name;
	private boolean attendedToday;
	private String barcode;
	private int id;
	private boolean attendedPastTwoWeeks;

	public Student(String login, String name, String barcode) {
		this.login = login;
		this.name = name;
		this.barcode = barcode;
	}
	
	public Student(String login, int id) {
		this.login = login;
		this.id = id;
	}
	
	public String getLogin() {
		return login;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean getAttendedToday() {
		return attendedToday;
	}
	
	public String getBarcode() {
		return barcode;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean getAttendedPastTwoWeeks() {
		return attendedPastTwoWeeks;
	}
	
	public void setLogin(String login) {
		this.login = login;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setAttendedToday(boolean attendedToday) {
		this.attendedToday = attendedToday;
	}
	
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setAttendedPastTwoWeeks(boolean attendedPastTwoWeeks) {
		this.attendedPastTwoWeeks = attendedPastTwoWeeks;
	}
	
	@Override
	public String toString() {
		return login + " " + name;
	}
}
