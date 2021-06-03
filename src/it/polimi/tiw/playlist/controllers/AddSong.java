package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.dao.SongDAO;

@WebServlet("/AddSong")
public class AddSong extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private Connection connection;
	
	public void init() {
		try {
			//Initializing the connection
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			
			Class.forName(driver);
			connection = DriverManager.getConnection(url , user , password);
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void doPost(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		String playlistId = request.getParameter("playlistId");
		String songId = request.getParameter("song");//I'm not sure
		String error = "";
		int pId = -1;
		int sId = -1;
		
		//I should do some controls about the user session
		HttpSession s = request.getSession();
		
		if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect("/TIW-PlayList-HTML-Pure/login.html");
			return;
		}
		//Take the user
	    User user = (User) s.getAttribute("user");
		
	    //Check id the parameters are present
		if(playlistId == null || playlistId.isEmpty() || songId == null || songId.isEmpty()) {
			error += "Missing parameter;";
		}
		if(error.equals("")) {
			try {
				//Create the DAO to check if the playList id belongs to the user 
				PlaylistDAO pDao = new PlaylistDAO(connection);
				SongDAO sDao = new SongDAO(connection);
				
				//Check if the playlistId and songId are numbers
				pId = Integer.parseInt(playlistId);
				sId = Integer.parseInt(songId);
				
				//Check if the player can access at this playList --> Check if the playList exists
				if(!pDao.findPlayListById(pId, user.getId()))
					error += "PlayList doesn't exist";
				//Check if the player has created the song with sId as id -->Check if the song exists
				if(!sDao.findSongByUser(sId, user.getId()))
					error += "Song dowsnt exist;";
			}catch(NumberFormatException e) {
				error += "Playlist not defined;";
			}catch(SQLException e) {
				error += "Impossible comunicate with the data base;";
			}
		}
		
		//if an error occurred
		if(!error.equals("")) {
			request.setAttribute("error", error);
			String path = "/GoToPlayListPage";

			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
		}
		
		//The user can add the song at the playList
		
		//To add the song in the playList
		PlaylistDAO pDao = new PlaylistDAO(connection);
		
		try {
			boolean result = pDao.addSong(pId, sId);
			
			if(result == true) {
				String path = getServletContext().getContextPath() + ("/GoToPlayListPage?playlistId=" + playlistId + "&section=0");
				response.sendRedirect(path);
			}
			else {
				error += "An arror occurred with the db, retry later;";
				request.setAttribute("error", error);
				//Forward to GoToPlaylistPage
				String path = getServletContext().getContextPath() + "/GoToPlayListPage";
				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
				dispatcher.forward(request,response);
			}
		}catch(SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An arror occurred with the db, retry later");
		}
		
	}

	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
