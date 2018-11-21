package main.java.api;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GetGroupsRequest {
	private String guid;
	
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}
}
