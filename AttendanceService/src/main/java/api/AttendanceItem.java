package main.java.api;

public class AttendanceItem {
	private String course;
	private int year;
	private int month;
	private int date;

	public AttendanceItem(String course, int year, int month, int date) {
		this.course = course;
		this.year = year;
		this.month = month;
		this.date = date;
	}
	
	public String getCourse() {
		return course;
	}
	
	public int getYear() {
		return year;
	}
	
	public int getMonth() {
		return month;
	}
	
	public int getDate() {
		return date;
	}
	
	public void setCourse(String course) {
		this.course = course;
	}
	
	public void setYear(int year) {
		this.year = year;
	}
	
	public void setMonth(int month) {
		this.month = month;
	}
	
	public void setDate(int date) {
		this.date = date;
	}
}
