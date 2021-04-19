package it.polimi.tiw.playlist.beans;

public class Song {
	private int id;
	private String songTitle;
	private String albumImage;//String because it's the path of the image

	public Song(String songTitle , String albumImage , int id) {
		this.songTitle = songTitle;
		this.albumImage = albumImage;
		this.id = id;
	}
	
	/**
	 * 
	 * @return the title of the song
	 */
	public String getSongTitle() {
		return songTitle;
	}
	
	/**
	 * 
	 * @return the path where the image of the album is stored
	 */
	public String getAlbumImage() {
		return albumImage;
	}
	
	/**
	 * 
	 * @return the id of the song
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Change the title of the song
	 * @param newSongTitle is the new title of the song
	 */
	public void setSongTitle(String newSongTitle) {
		this.songTitle = newSongTitle;
	}
	
	
	/**
	 * Change the path of the image
	 * @param newAlbumImage is the new path
	 */
	public void setAlbumImage(String newAlbumImage) {
		this.albumImage = newAlbumImage;
	}
}
