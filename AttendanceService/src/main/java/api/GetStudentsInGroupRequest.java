package main.java.api;

import javax.xml.bind.annotation.XmlRootElement;

//Represents a request to the getStudentsInGroups method in AttendanceService
@XmlRootElement
public class GetStudentsInGroupRequest {
	private int groupId;
	private String course;
	private String guid;
	
	public int getGroupId() {
		return groupId;
	}
	
	public String getCourse() {
		return course;
	}
	
	public String getGuid() {
		return guid;
	}
	
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
	public void setCourse(String course) {
		this.course = course;
	}
	
	public void setGuid(String guid) {
		this.guid = guid;
	}
}
