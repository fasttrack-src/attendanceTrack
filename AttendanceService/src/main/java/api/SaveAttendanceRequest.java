package main.java.api;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents an item of attendance
 * @author Maria
 *
 */
@XmlRootElement
public class SaveAttendanceRequest {
	private String barcode;
	private int year;
	private int month;
	private int date;
	private int hour;
	private int minute;
	private String course;
	private int courseId;
	private String guid;
	
	public String getBarcode() {
		return barcode;
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
	
	public int getHour() {
		return hour;
	}
	
	public int getMinute() {
		return minute;
	}
	
	public String getCourse() {
		return course;
	}
	
	public int getCourseId() {
		return courseId;
	}
	
	public String getGuid() {
		return guid;
	}
	
	public void setBarcode(String barcode) {
		this.barcode = barcode;
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
	
	public void setHour(int hour) {
		this.hour = hour;
	}
	
	public void setMinute(int minute) {
		this.minute = minute;
	}
	
	public void setCourse(String course) {
		this.course = course;
	}
	
	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}
	
	public void setGuid(String guid) {
		this.guid = guid;
	}
	
	public boolean isValid() {
		return barcode.matches("[0-9]+") &&
				year > 0 &&
				month > 0 && month <= 12 &&
				date > 0 && date <= 31 &&
				hour >= 0 && hour <= 24 &&
				minute >= 0 && minute <= 59;
	}
	
	@Override
	public String toString() {
		return String.format(
				"Student barcode: %s Date: %4d/%2d/%2d %2d:%2d Course: %s", 
				barcode, 
				year, 
				month, 
				date, 
				hour, 
				minute, 
				course);
	}
}
