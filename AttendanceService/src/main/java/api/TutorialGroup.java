package main.java.api;

public class TutorialGroup {
	private String name;
	private int id;
	private String course;
	private int courseId;
	
	public TutorialGroup(String name, int id, String course, int courseId) {
		this.name = name;
		this.id = id;
		this.course = course;
		this.courseId = courseId;
	}
	
	public String getName() {
		return name;
	}
	
	public int getId() {
		return id;
	}
	
	public String getCourse() {
		return course;
	}
	
	public int getCourseId() {
		return courseId;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public void setCourse(String course) {
		this.course = course;
	}
	
	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}
}
