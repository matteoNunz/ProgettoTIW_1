package it.polimi.tiw.playlist.dao;

import java.io.File;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

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
	public int findAlbumId(String albumTitle , String singer , int publicationYear , String outputPathImg )throws SQLException {
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
	public boolean createSongAndAlbum(int userId , String songTitle , String genre , String albumTitle , String singer , int publicationYear , String outputPathImg , String outputPathSong , InputStream image , InputStream song) 
			throws SQLException{
		
		boolean result = false;
		try {
			connection.setAutoCommit(false);
			
			int albumId = createAlbum(albumTitle , singer , publicationYear , outputPathImg , image);
			result = createSong(userId , songTitle , genre , albumId , outputPathSong , song);
			
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
	private int createAlbum(String albumTitle , String singer , int publicationYear , String outputPathImg , InputStream image) throws SQLException{
		
		int albumId = 0;
		albumId = findAlbumId(albumTitle , singer , publicationYear , outputPathImg);
		//If the album is already in the data base I reuse it
		if(albumId != 0)
			return albumId;
		
		String query = "INSERT INTO album (Title , Image , Singer , PublicationYear , ImageBlob) VALUES (? , ? , ? , ? , ?)";
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1, albumTitle);
			pStatement.setString(2, outputPathImg);
			pStatement.setString(3, singer);
			pStatement.setInt(4, publicationYear);
			if(image != null)
				pStatement.setBlob(5 , image);
			
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
	private boolean createSong(int userId , String songTitle , String genre , int albumId , String outputPathSong , InputStream song) throws SQLException{
		String query = "INSERT INTO song (IdUser , KindOf , MusicFile , SongTitle , IdAlbum , SongBlob) VALUES (? , ? , ? , ? , ? , ?)";
		PreparedStatement pStatement = null;
		int code = 0;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, userId);
			pStatement.setString(2, genre);
			pStatement.setString(3, outputPathSong);
			pStatement.setString(4, songTitle);
			pStatement.setInt(5, albumId);
			if(song != null)
				pStatement.setBlob(6 ,  song);
			
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
	public ArrayList<SongDetails> getSongTitleAndImg(int playlistId) throws SQLException{
		String query = "SELECT * FROM contains JOIN song ON contains.IdSong = song.Id JOIN album ON song.IdAlbum = album.Id "
				+ "WHERE contains.IdPlaylist = ? ORDER BY album.PublicationYear DESC";
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		ArrayList<SongDetails> songs = new ArrayList<SongDetails>();
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, playlistId);
			
			resultSet = pStatement.executeQuery();
			
			while(resultSet.next()) {
				System.out.println("Dentro al while");
				SongDetails song = new SongDetails();
				
				Blob blob = resultSet.getBlob("ImageBlob");
				song.setImageBlob(blob);
				System.out.println("Blob letto");
				InputStream image = blob.getBinaryStream();
				System.out.println("InputStream letto");
				File imageFile = new File("image");
				System.out.println("File aperto");
				
				//song.setImageBytes(blob.getBytes(1, (int) blob.length()));
				song.setImageFile(imageFile);
				//song.setImage(image);
				//song.setFileStream();
				songs.add(song);
				
				song.setId(resultSet.getInt("song.Id"));
				song.setSongTitle(resultSet.getString("song.SongTitle"));
				song.setImgFile(resultSet.getString("album.Image"));//Set the name of the image file
				//Read the image from the data base
				System.out.println("Finito la lettura base");

				System.out.println("Fine while");
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
	
	public ArrayList<SongDetails> getSongsNotInPlaylist(int playlistId , int userId) throws SQLException{
		String query = "SELECT * FROM song WHERE IdUser = ? AND Id NOT IN ("
				+ "SELECT IdSong FROM contains WHERE IdPlaylist = ?)";
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		ArrayList<SongDetails> songs = new ArrayList<SongDetails>();
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, userId);
			pStatement.setInt(2, playlistId);
			
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
	
	/**
	 * Method that verify if a song belongs to a specific user
	 * @param sId is the song id
	 * @param userId is the user id
	 * @return true if the song belongs, false otherwise
	 * @throws SQLException
	 */
	public boolean findSongByUser(int sId , int userId) throws SQLException{
		String query = "SELECT * FROM song WHERE Id = ? AND IdUser = ?";
		boolean result = false;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, sId);
			pStatement.setInt(2, userId);
			
			resultSet = pStatement.executeQuery();
			
			if(resultSet.next())
				result = true;
			
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
		return result;
	}
	
	/**
	 * Method that take from the data base details of a specific song
	 * @param songId is the song id the user wants the details
	 * @return a SongDetails object
	 * @throws SQLException
	 */
	public SongDetails getSongDetails(int songId) throws SQLException{
		String query = "SELECT * FROM song JOIN album on song.IdAlbum = album.Id WHERE song.Id = ?";
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		SongDetails song = new SongDetails();
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, songId);
			
			resultSet = pStatement.executeQuery();
			
			if(resultSet.next()) {//theoretically always true -> I've just controlled there is a song
				song.setSongTitle(resultSet.getString("song.SongTitle"));
				song.setAlbumTitle(resultSet.getString("album.Title"));
				song.setSinger(resultSet.getString("album.Singer"));
				song.setKindOf(resultSet.getString("song.KindOf"));
				song.setPublicationYear(resultSet.getInt("album.PublicationYear"));
				song.setFile(resultSet.getString("song.MusicFile"));
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
		return song;
	}
	
}










