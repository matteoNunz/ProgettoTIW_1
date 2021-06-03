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

import org.apache.tomcat.util.codec.binary.Base64;
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
		String section = request.getParameter("section");//Which songs need to be shown
		String error = "";
		String error1 = "";
		int id = -1;
		int block = 0;
		
		//I should do some controls about the user session
		HttpSession s = request.getSession();
		
		if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect("/TIW-PlayList-HTML-Pure/login.html");
			return;
		}
		
		//Take the user
	    User user = (User) s.getAttribute("user");
	    
		//Check if playlistId is valid
		if(playlistId == null || playlistId.isEmpty())
			error += "Playlist not defined;";
		
		if(section == null || section.isEmpty()) {
			section = "0";
		}
		
		//Check the follow only if the id is valid
		if(error.equals("")) {
			//Create the DAO to check if the playList id belongs to the user 
			PlaylistDAO pDao = new PlaylistDAO(connection);
			
			try {
				//Check if the playlistId is a number
				id = Integer.parseInt(playlistId);
				//Check if section is a number
				block = Integer.parseInt(section);
				//Check if the player can access at this playList --> Check if the playList exists
				if(!pDao.findPlayListById(id, user.getId())) {
						error += "PlayList doesn't exist";
				}
				if(block < 0)
					block = 0;
			}catch(NumberFormatException e) {
				error += "Playlist e/o section not defined;";
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
			
			ArrayList<SongDetails> songsInPlaylist = sDao.getSongTitleAndImg(id);
			ArrayList<SongDetails> songsNotInPlaylist = sDao.getSongsNotInPlaylist(id , user.getId());
			String title = pDao.findPlayListTitleById(id);
			
			//TODO check the functioning of block -> problem with before link in the html
			//TODO it doesn't take songs in order of date -> with the first section it works
			
			boolean next = false;
			
			if(block * 5 + 5 > songsInPlaylist.size()) {
				block = (songsInPlaylist.size() / 5);
			}
			//if(block == (songsInPlaylist.size() / 5))
			//	next = false;
			if((block * 5 + 5) < songsInPlaylist.size()) {
				next = true;
			}
			
			ArrayList<SongDetails> songs = new ArrayList<SongDetails>();
			
			ServletContext servletContext = getServletContext();
			String imagePathHelp = servletContext.getInitParameter("albumImgPathHelp");
			
			if(songsInPlaylist.size() > 0) {
				for(int i = (block * 5) ; i < (block * 5 + 5) && i < songsInPlaylist.size(); i++){
					SongDetails song = songsInPlaylist.get(i);
					
					
					//song.setImgFile(Base64.encodeBase64String(song.getImageBytes()));
					//System.out.println("Bytes converted in string:" + song.getImgFile());
					
					//File file = new File(song.getImgFile());
					//String imgName = file.getName();
					
					//song.setImgFile(imagePathHelp + imgName);//Update the position 
					//song.setImgFile("/images/1_CatturaFireworks");
					
					songs.add(song);
				}	
			}
			
			
			
			Playlist p = new Playlist();
			p.setId(id);
			p.setTitle(title);
			
			String path = "/WEB-INF/PlaylistPage.html";
			//ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request , response , servletContext , request.getLocale());
			ctx.setVariable("user" , user);
			ctx.setVariable("songsInPlaylist", songs);
			ctx.setVariable("songsNotInPlaylist", songsNotInPlaylist);
			ctx.setVariable("playlist", p);
			ctx.setVariable("block", block);
			ctx.setVariable("next", next);
			
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
