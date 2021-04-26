package it.polimi.tiw.playlist.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpSession;

import com.mysql.cj.protocol.Resultset;

import it.polimi.tiw.playlist.beans.SongDetails;
import it.polimi.tiw.playlist.beans.User;

public class SongDAO {
	private Connection connection;
	
	public SongDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Method that verify if an album is already in the data base schema "album"
	 * @param albumTitle
	 * @param singer
	 * @param publicationYear
	 * @param outputPathImg
	 * @return the id of the album if that already exists, 0 otherwise
	 * @throws SQLException
	 */
	public int findAlbumId(String albumTitle , String singer , int publicationYear , String outputPathImg)throws SQLException {
		String query = "SELECT Id FROM album WHERE Title = ? AND Image = ? AND Singer = ? AND PublicationYear = ?";
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		int result = 0;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1, albumTitle);
			pStatement.setString(2, outputPathImg);
			pStatement.setString(3, singer);
			pStatement.setInt(4, publicationYear);
			
			resultSet = pStatement.executeQuery();
			if(resultSet.next())
				result = resultSet.getInt("Id");
		}catch(SQLException e) {
			throw e;
		}finally {
			try {
				if(resultSet != null) {
					resultSet.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}	
		return result;
	}
	
	/**
	 * Method that create the album and the song doing the commit if everything went okay, rollBack otherwise
	 * @param userId
	 * @param songTitle
	 * @param genre
	 * @param albumTitle
	 * @param singer
	 * @param publicationYear
	 * @param outputPathImg
	 * @param outputPathSong
	 * @return true if the method created correctly both album and song , false otherwise
	 * @throws SQLException
	 */
	public boolean createSongAndAlbum(int userId , String songTitle , String genre , String albumTitle , String singer , int publicationYear , String outputPathImg , String outputPathSong) 
			throws SQLException{
		
		boolean result = false;
		try {
			connection.setAutoCommit(false);
			
			int albumId = createAlbum(albumTitle , singer , publicationYear , outputPathImg);
			result = createSong(userId , songTitle , genre , albumId , outputPathSong);
			
			connection.commit();
		}catch(SQLException e){
			connection.rollback();
			throw e;
		}finally {
			connection.setAutoCommit(true);
			//And close the pStatement here , because now there are 3 different methods but the need to be together
		}
		return result;
	}
	
	/**
	 * Method that check if the same album is already in the dataBase and then, eventually, create a new album
	 * @param albumTitle
	 * @param singer
	 * @param publicationYear
	 * @param outputPathImg
	 * @return the id of the album created or already present
	 * @throws SQLException
	 */
	private int createAlbum(String albumTitle , String singer , int publicationYear , String outputPathImg) throws SQLException{
		
		int albumId = 0;
		albumId = findAlbumId(albumTitle , singer , publicationYear , outputPathImg);
		//If the album is already in the data base I reuse it
		if(albumId != 0)
			return albumId;
		
		String query = "INSERT INTO album (Title , Image , Singer , PublicationYear) VALUES (? , ? , ? , ?)";
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1, albumTitle);
			pStatement.setString(2, outputPathImg);
			pStatement.setString(3, singer);
			pStatement.setInt(4, publicationYear);
			
			int code = pStatement.executeUpdate();
			
			if(code > 0)
				return (findAlbumId(albumTitle , singer , publicationYear , outputPathImg));
		}catch(SQLException e) {
			throw e;
		}finally {
			try {
				if (pStatement != null) {
					pStatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
		}
		return albumId;
	}
	
	/**
	 * Method that create the song in the dataBase
	 * @param userId
	 * @param songTitle
	 * @param genre
	 * @param albumId
	 * @param outputPathSong
	 * @return true if the update of the DB went good , false otherwise
	 * @throws SQLException
	 */
	private boolean createSong(int userId , String songTitle , String genre , int albumId , String outputPathSong) throws SQLException{
		String query = "INSERT INTO song (IdUser , KindOf , MusicFile , SongTitle , IdAlbum) VALUES (? , ? , ? , ? , ?)";
		PreparedStatement pStatement = null;
		int code = 0;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, userId);
			pStatement.setString(2, genre);
			pStatement.setString(3, outputPathSong);
			pStatement.setString(4, songTitle);
			pStatement.setInt(5, albumId);
			
			code = pStatement.executeUpdate();
		}catch(SQLException e) {
			throw e;
		}finally {
			try {
				if (pStatement != null) {
					pStatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
		}
		return (code > 0); 
	}
	
	/**
	 * Method that take every song in a playList
	 * @param playlistId is the id of the playList the user wants the songs
	 * @return an array list filled for each song in the playList with id,title and image path
	 * @throws SQLException
	 */
	public ArrayList<SongDetails> getSongTiteAndImg(int playlistId) throws SQLException{
		String query = "SELECT * FROM contains JOIN song ON contains.IdSong = song.Id JOIN album ON song.IdAlbum = album.Id "
				+ "WHERE contains.IdPlaylist = ?";
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		ArrayList<SongDetails> songs = new ArrayList<SongDetails>();
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, playlistId);
			
			resultSet = pStatement.executeQuery();
			
			while(resultSet.next()) {
				SongDetails song = new SongDetails();
				song.setId(resultSet.getInt("song.Id"));
				song.setSongTitle(resultSet.getString("song.SongTitle"));
				song.setImgFile(resultSet.getString("album.Image"));
				songs.add(song);
			}
			
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(resultSet != null) {
					resultSet.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		return songs;
	}
	
	public ArrayList<SongDetails> getSongsNotInPlaylist(int playlistId) throws SQLException{
		String query = "SELECT * FROM song WHERE id NOT IN "
				+ "SELECT IdSong FROM contains WHERE IdPlaylisy = ?";
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		ArrayList<SongDetails> songs = new ArrayList<SongDetails>();
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, playlistId);
			
			resultSet = pStatement.executeQuery();
			
			while(resultSet.next()) {
				SongDetails song = new SongDetails();
				song.setId(resultSet.getInt("Id"));
				song.setSongTitle(resultSet.getString("SongTitle"));
				songs.add(song);
			}
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(resultSet != null) {
					resultSet.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		return songs;
	}
	
}










