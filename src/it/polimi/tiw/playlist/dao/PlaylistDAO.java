package it.polimi.tiw.playlist.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.playlist.beans.Playlist;

public class PlaylistDAO {
	private Connection connection;
	
	public PlaylistDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Method that create a list of playlist of the user
	 * @param userId is the id of the user
	 * @return an ArrayList of playlist created by the user
	 * @throws SQLException 
	 */
	public ArrayList<Playlist> findPlaylist(int userId) throws SQLException{
		String query = "SELECT * FROM playlist WHERE IdUserName = ? ORDER BY CreationDate DESC";
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		ArrayList<Playlist> playlists = new ArrayList<Playlist>();
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1 , userId);
			
			resultSet = pStatement.executeQuery();
			
			while(resultSet.next()) {
				Playlist playlist = new Playlist();
				playlist.setTitle(resultSet.getString("Title"));
				playlist.setId(resultSet.getInt("Id"));
				playlist.setCreationDate(resultSet.getDate("CreationDate"));
				playlists.add(playlist);
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
		return playlists;
	}
	
	/**
	 * Method that verify if a title is already in the data base 
	 * @param title is the title to search
	 * @return true if the title is already present, false otherwise
	 * @throws SQLException
	 */
	public boolean findPlaylistByTitle(String title) throws SQLException{
		String query = "SELECT * FROM playlist WHERE Title = ?";
		boolean result = false;
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1, title);
			resultSet = pStatement.executeQuery();
			
			if(resultSet.next()) result = true;
			
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
	 * Method that create a new playList with an unique title 
	 * @param title 
	 * @param creationDate
	 * @return true if the playList was created correctly, false otherwise
	 * @throws SQLException
	 */
	public boolean createPlaylist(String title , Date creationDate , int userId) throws SQLException{
		String query = "INSERT INTO playlist (IdUserName , Title , CreationDate) VALUES (? , ? , ?)";
		int code = 0;
		PreparedStatement pStatement = null;
		
		if(findPlaylistByTitle(title) == true)//And if findPlaylistByTitle thorw an exception?????
			return false;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, userId);
			pStatement.setString(2 , title);
			pStatement.setDate(3 , creationDate);
			code = pStatement.executeUpdate();
		}catch(SQLException e) {
			throw new SQLException();
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
	 * Method that find if a playList and a user are connected
	 * @param playlistId is the id of the playList
	 * @param userId is the id of the user
	 * @return true if the user created this playList, false otherwise
	 * @throws SQLException
	 */
	public boolean findPlayListById(int playlistId , int userId) throws SQLException{
		String query = "SELECT * FROM playlist WHERE Id = ? AND IdUserName = ?";
		boolean result = false;
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1 , playlistId);
			pStatement.setInt(2 , userId);
			
			resultSet = pStatement.executeQuery();
			
			if(resultSet.next()) {
				result = true;
			}
		}catch(SQLException e) {
			throw new SQLException();
		}finally{
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
}





