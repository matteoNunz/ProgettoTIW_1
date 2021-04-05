package it.polimi.tiw.playlist.beans;

public class User {
	private String userName;
	private String password;
	
	public User(String userName , String password) {
		this.userName = userName;
		this.password = password;
	}
	
	/**
	 * 
	 * @return the userName 
	 */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * 
	 * @return the password of the user
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Change the userName of the user
	 * @param newUserName is the new userName
	 */
	public void setUserName(String newUserName) {
		this.userName = newUserName;
	}
	
	/**
	 * Change the password of the user
	 * @param newPassword is the new password
	 */
	public void setPassword(String newPassword) {
		this.password = newPassword;
	}
	
}
