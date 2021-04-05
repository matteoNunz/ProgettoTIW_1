package it.polimi.tiw.playlist.beans;

import java.sql.Date;//maybe java.util.Date;

public class SongDetails{
	private String songTitle;
	private String albumTitle;
	private String singer;
	private String kindOf;
	private String file;//where is the music file
	private Date publicationYear;

	public SongDetails(String songTitle , String albumTitle , String singer , String kindOf , String file , Date publicationYear) {
		this.songTitle = songTitle;
		this.albumTitle = albumTitle;
		this.singer = singer;
		this.kindOf = kindOf;
		this.file = file;
		this.publicationYear = publicationYear;
	}
	
	/**
	 * 
	 * @return the song title
	 */
	public String getSongTitle() {
		return songTitle;
	}
	
	/**
	 * 
	 * @return the title of the album that contains the song
	 */
	public String getAlbumTitle() {
		return albumTitle;
	}
	
	/**
	 * 
	 * @return the singer of the song
	 */
	public String getSinger() {
		return singer;
	}
	
	/**
	 * 
	 * @return the kind of song
	 */
	public String getKinfOf() {
		return kindOf;
	}
	
	/**
	 * 
	 * @return the path of the music file
	 */
	public String getFile() {
		return file;
	}
	
	/**
	 * 
	 * @return the publication year of the album that contains the song
	 */
	public Date getPublicationYear() {
		return publicationYear;
	}
	
	/**
	 * Change the SongTitle
	 * @param newSongTitle is the new title of the song
	 */
	public void setSongTitle(String newSongTitle) {
		this.songTitle = newSongTitle;
	}
	
	/**
	 * Change the title of the album that contains the song
	 * @param newAlbumTitle
	 */
	public void setAlbumTitle(String newAlbumTitle) {
		this.albumTitle = newAlbumTitle;
	}
	
	
	/**
	 * Change the name of singer
	 * @param newSinger is the new name
	 */
	public void setSinger(String newSinger) {
		this.singer = newSinger;
	}
	
	/**
	 * Change the kinf of the song
	 * @param newKindOf is the new type
	 */
	public void setKindOf(String newKindOf) {
		this.kindOf = newKindOf;
	}
	
	/**
	 * Change the position of the file
	 * @param newFile is the position where the file is stored
	 */
	public void setFile(String newFile) {
		this.file = newFile;
	}
}


