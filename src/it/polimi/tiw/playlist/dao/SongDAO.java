package it.polimi.tiw.playlist.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpSession;

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
	
	private boolean createSong(int userId , String songTitle , String genre , int albumId , String outputPathSong) throws SQLException{
		String query = "INSERT INTO song (IdUser , KindOf , MusifFile , SongTitle , IdAlbum) VALUES (? , ? , ? , ? , ?)";
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

}










