package main.java.api;

public class ConfigManager {

	private String databaseUrl;
	private String databaseUser;
	private String databasePassword;
	private String allowedOrigin;
	private String ldapUrl;
	private int ldapPort;
	private String adminPswdHash;
	
	public String getDatabaseUrl() {
		return databaseUrl;
	}
	
	public String getDatabaseUser() {
		return databaseUser;
	}
	
	public String getDatabasePassword() {
		return databasePassword;
	}
	
	public String getAllowedOrigin() {
		return allowedOrigin;
	}
	
	public String getLdapUrl() {
		return ldapUrl;
	}
	
	public int getLdapPort() {
		return ldapPort;
	}
	
	public String getAdminPswdHash() {
		return adminPswdHash;
	}
	
	public void setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
	}
	
	public void setDatabaseUser(String databaseUser) {
		this.databaseUser = databaseUser;
	}
	
	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}
	
	public void setAllowedOrigin(String allowedOrigin) {
		this.allowedOrigin = allowedOrigin;
	}
	
	public void setLdapUrl(String ldapUrl) {
		this.ldapUrl = ldapUrl;
	}
	
	public void setLdapPort(int ldapPort) {
		this.ldapPort = ldapPort;
	}
	
	public void setAdminPswdHash(String adminPswdHash) {
		this.adminPswdHash = adminPswdHash;
	}
}
