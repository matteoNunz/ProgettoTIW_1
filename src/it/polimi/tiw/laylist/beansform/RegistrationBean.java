package it.polimi.tiw.laylist.beansform;

import java.sql.Connection;
import java.sql.SQLException;

import it.polimi.tiw.playlist.dao.UserDAO;

/**
 * This bean is used to maintain the userName or the password if something got wrong during the registration 
 * to avoid user remember what he wrote as userName or password, saving them if they are right
 *
 */
public class RegistrationBean {
	
	private String userName;
	private String password;
	private Connection connection;
	
	/**
	 * The constructor will set only the right parameters
	 * @param userName is the userName inserted by the user
	 * @param passowrd is the password inserted by the user
	 * @param connection is the connection for the DB
	 */
	public RegistrationBean(String userName , String password , Connection connection) {
		this.connection = connection;
		setUserName(userName);
		setPassword(password);
	}
	
	/**
	 * This method checks if the userName is unique and, if it is, set the userName.Otherwise userName will be null
	 * @param userName 
	 */
	private void setUserName(String userName) {
		UserDAO user = new UserDAO(connection);
		
		try {
			if(user.findUser(userName) == false)
				this.userName = userName;
			else 
				this.userName = null;
		}catch(SQLException e) {
			this.userName = null;
		}
	}
	
	/**
	 * This method checks if the password respect all the constraints before save it in a temporary bean
	 * @param password 
	 */
	private void setPassword(String password) {
		//Check if the password isn't null or empty
		if(password == null || password.isEmpty()) {
			this.password = null;
			return;
		}
		
		//Check if the password contain at least one number and one special character and if it has a size bigger than 4
		if(!(password.contains("0") || password.contains("1") || password.contains("2") || password.contains("3") || password.contains("4") || password.contains("5") || password.contains("6") || password.contains("7") || password.contains("8") || password.contains("9")) 
				|| !(password.contains("#") || password.contains("@") || password.contains("_")) || password.length() < 4) {
			this.password = null;
			return;
		}
		
		//Check if the password is too long
		if(password.length() > 25) {
			this.password = null;
			return;
		}
		
		this.password = password;
	}

}
