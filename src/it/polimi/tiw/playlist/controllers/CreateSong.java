package it.polimi.tiw.playlist.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
 
import java.util.Calendar;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.SongDAO;

@WebServlet("/CreateSong")
@MultipartConfig 
public class CreateSong extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
	private String imgFolderPath = "";
	private String mp3FolderPath = "";
	
	public void init() {
		ServletContext context = getServletContext();
		
		//Initializing the template engine
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		
		//Initializing the folder where images and mp3 files will be uploaded
		imgFolderPath = context.getInitParameter("albumImgPath");
		mp3FolderPath = context.getInitParameter("songFilePath");
		
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
	
	public void doPost(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		//Take the request parameters
		String songTitle = request.getParameter("title");
		String genre = request.getParameter("genre");
		String albumTitle = request.getParameter("albumTitle");
		String singer = request.getParameter("singer");
		String date = request.getParameter("date");
		
		Part albumImg = request.getPart("albumImg");
		Part songFile = request.getPart("songFile");
		
		//I should do some controls about the user session
		HttpSession s = request.getSession();
		User user = (User) s.getAttribute("user");

		String error = "";
		
		//Check if the user missed some parameters
		if(songTitle == null || songTitle.isEmpty() || genre == null || genre.isEmpty() || albumTitle == null || albumTitle.isEmpty()
				|| singer == null || singer.isEmpty() || date == null || date.isEmpty() 
				|| albumImg == null || albumImg.getSize() <= 0 || songFile == null || songFile.getSize() <= 0) {
			error += "Missin parameters;";
		}	
		
		int publicationYear = Integer.parseInt(date);
		
		//Take the current year
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		
		//Check if the publicationYear is not bigger than the current year
		if(publicationYear > currentYear)
			error += "Invalid date;";
		
		//Check if some input are too long
		if(songTitle.length() > 45)
			error += "Song title too long;";
		if(genre.length() > 45)
			error += "Genre name too long;";
		if(albumTitle.length() > 45)
			error += "Album title too long;";
		if(singer.length() > 45)
			error += "Singer name too long;";
		
		//Take the type of the image file uploaded : image/png
		String contentTypeImg = albumImg.getContentType();
		System.out.println("Image type is: " + contentTypeImg);

		//Check if the type is an image
		if(!contentTypeImg.startsWith("image"))
			error += "Image file not valid;";
		
		//Take the type of the music file uploaded : audio/mpeg
		String contentTypeMusic = songFile.getContentType();
		System.out.println("Music type is: " + contentTypeMusic);
		
		//Check the type of the music file uploaded
		if(!contentTypeMusic.startsWith("audio"))
			error += "Music file not valid";
		
		//If an error occurred, redirect with errorMsg1 to the template engine  
		if(!error.equals("")) {
			request.setAttribute("error1", error);
			String path = "/GoToHomePage";

			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
		}
		
		//Take the name of the image uploaded
		String fileNameImg = Path.of(albumImg.getSubmittedFileName()).getFileName().toString();
		System.out.println("Test image: " + albumImg.getSubmittedFileName());
		System.out.println("Test 1: "+ Paths.get(albumImg.getSubmittedFileName()));
		System.out.println("Filename image: " + fileNameImg);
		
		//Take the name of the song uploaded
		String fileNameSong = Paths.get(songFile.getSubmittedFileName()).getFileName().toString();
		System.out.println("Filename music: " + fileNameSong);
		
		//Create the final path for images adding the user id in the start to avoid error in case of duplicate name;
		String outputPathImg = imgFolderPath + user.getId() + "_" + fileNameImg;
		System.out.println("Output path img: " + outputPathImg);
		
		//Create the final part for music files adding the user id in the start to avoid error in case of duplicate name;
		String outputPathSong = mp3FolderPath + user.getId()  + "_" + fileNameSong;
		System.out.println("Output path song: " + outputPathSong);
		
		//Check if the final path are not too long
		if(fileNameSong.length() > 255)
			error += "Song name too long;";
		if(fileNameImg.length() > 255) {
			error += "Image name too long;";
		}
		//Check if a song or an image with the same name already exist
		File tempFile = new File(outputPathImg);
		if(tempFile.exists())
			error += "Image name already exists;";
		tempFile = new File(outputPathSong);
		if(tempFile.exists())
			error += "Song name already exists;";
		System.out.println("Error is: " + error);
		
		//If an error occurred, redirect with errorMsg1 to the template engine  
		if(!error.equals("")) {
			request.setAttribute("error1", error);
			String path = "/GoToHomePage";

			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;
		}
		
		//Save the image
		File fileImg = new File(outputPathImg);

		try (InputStream fileContent = albumImg.getInputStream()) {
			
			Files.copy(fileContent, fileImg.toPath());
			System.out.println("File saved correctly!");

		} catch (Exception e) {
			error += "Error in uploading the image;";
		}
		
		//Save the mp3 file
		File fileSong = new File(outputPathSong);
		
		try (InputStream fileContent = albumImg.getInputStream()) {
			
			Files.copy(fileContent, fileSong.toPath());
			System.out.println("File saved correctly!");

		} catch (Exception e) {
			error += "Error in uploading the music file;";
		}
		
		//If an error occurred, redirect with errorMsg1 to the template engine  
		if(!error.equals("")) {
			/*String path = "/WEB-INF/HomePage.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("user", user); 
			ctx.setVariable("errorMsg1", error);
			templateEngine.process(path, ctx, response.getWriter());*/
			request.setAttribute("error1", error);
			String path = "/GoToHomePage";

			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
		}
		
		//Now it's possible update the data base
		
		SongDAO sDao = new SongDAO(connection);
		
		try {
			boolean result = sDao.createSongAndAlbum(user.getId() , songTitle, genre, albumTitle, singer, publicationYear, outputPathImg, outputPathSong);
			
			if(result == true) {
				String path = getServletContext().getContextPath() + "/GoToHomePage";
				response.sendRedirect(path);
			}
			else {
				error += "Impossible upload file in the database , try later";
				request.setAttribute("error1", error);
				String path = "/GoToHomePage";

				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
				dispatcher.forward(request,response);
			}
			
		}catch(SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An arror occurred uploading the db, retry later");
		}
	}

}








