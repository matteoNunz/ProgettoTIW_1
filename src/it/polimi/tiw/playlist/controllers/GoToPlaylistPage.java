package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.playlist.beans.Playlist;
import it.polimi.tiw.playlist.beans.SongDetails;
import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.dao.SongDAO;

@WebServlet("/GoToPlayListPage")
public class GoToPlaylistPage extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
	
	public void init() {
		ServletContext context = getServletContext();
		
		//Initializing the template engine
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		
		try {
			
			//Initializing the connection
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
	
	public void doGet(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		//Take the playList id
		String playlistId = request.getParameter("playlistId");
		String error = "";
		String error1 = "";
		int id = -1;
		
		//I should do some controls about the user session
		HttpSession s = request.getSession();
		
		if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect("/TIW-PlayList-HTML-Pure/login.html");
			return;
		}
		
		//Take the user
	    User user = (User) s.getAttribute("user");
	    
		//Check if playlistId is valid
		if(playlistId.isEmpty() || playlistId == null)
			error += "Playlist not defined;";
		
		//Check the follow only if the id is valid
		if(error.equals("")) {
			//Create the DAO to check if the playList id belongs to the user 
			PlaylistDAO pDao = new PlaylistDAO(connection);
			
			try {
				//Check if the playlistId is a number
				id = Integer.parseInt(playlistId);
				//Check if the player can access at this playList --> Check if the playList exists
				if(!pDao.findPlayListById(id, user.getId())) {
						error += "PlayList doesn't exist";
				}
			}catch(NumberFormatException e) {
				error += "Playlist not defined;";
			}catch(SQLException e) {
				error += "Impossible comunicate with the data base;";
			}
		}	
		
		if(!error.equals("")){
			request.setAttribute("error2", error);
			String path = "/GoToHomePage";

			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;
		}
		
		//The user created this playList
		
		//Take the error in case of forward from AddSong
		if(request.getAttribute("error") != null)
			error = (String) request.getAttribute("error");
		//Take the error in case of forward from GoToSongPage
		if(request.getAttribute("error1") != null)
			error1 = (String) request.getAttribute("error1");
		
		SongDAO sDao = new SongDAO(connection);
		
		//To take the title of the playList
		PlaylistDAO pDao = new PlaylistDAO(connection);
		
		//Take the titles and the image paths
		try {
			
			ArrayList<SongDetails> songsInPlaylist = sDao.getSongTiteAndImg(id);
			ArrayList<SongDetails> songsNotInPlaylist = sDao.getSongsNotInPlaylist(id , user.getId());
			String title = pDao.findPlayListTitleById(id);
			
			Playlist p = new Playlist();
			p.setId(id);
			p.setTitle(title);
			
			String path = "/WEB-INF/PlaylistPage.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request , response , servletContext , request.getLocale());
			ctx.setVariable("user" , user);
			ctx.setVariable("songsInPlaylist", songsInPlaylist);
			ctx.setVariable("songsNotInPlaylist", songsNotInPlaylist);
			ctx.setVariable("playlist", p);
			ctx.setVariable("errorMsg", error);
			ctx.setVariable("errorMsg1", error1);
			templateEngine.process(path , ctx , response.getWriter());
			
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
