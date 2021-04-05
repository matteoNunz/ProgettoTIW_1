package it.polimi.tiw.playlist.beans;

import java.sql.Date;//maybe java.util.Date;

public class Playlist {
	private String title;
	private Date creationDate;

	public Playlist(String title , Date creationDate) {
		this.title = title;
		this.creationDate = creationDate;
	}
	
	/**
	 * 
	 * @return the title of the Playlist
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * 
	 * @return the creation date of the Playlist
	 * 
	 * No set of creationDate because it's unique
	 */
	public Date getCreationDate() {
		return creationDate;
	}
	
	/**
	 * In case of new title 
	 * @param newTitle is the new title
	 */
	public void setTitle(String newTitle) {
		this.title = newTitle;
	}
}
