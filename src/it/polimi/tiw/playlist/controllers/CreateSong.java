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

import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.SongDAO;

@WebServlet("/CreateSong")
@MultipartConfig 
public class CreateSong extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private String imgFolderPath = "";
	private String mp3FolderPath = "";
	private boolean isReplaced = false;
	
	public void init() {
		ServletContext context = getServletContext();
		
		//Initializing the folder where images and mp3 files will be uploaded
		imgFolderPath = context.getInitParameter("albumImgPath");
		mp3FolderPath = context.getInitParameter("songFilePath");
		isReplaced = false;
		
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
		
		int publicationYear = 0;
		
		HttpSession s = request.getSession();
		//Check if the session is still valid
		if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect("/TIW-PlayList-HTML-Pure/login.html");
			return;
		}
		
		User user = (User) s.getAttribute("user");

		String error = "";
		
		//Check if the user missed some parameters
		if(songTitle == null || songTitle.isEmpty() || genre == null || genre.isEmpty() || albumTitle == null || albumTitle.isEmpty()
				|| singer == null || singer.isEmpty() || date == null || date.isEmpty() 
				|| albumImg == null || albumImg.getSize() <= 0 || songFile == null || songFile.getSize() <= 0) {
			error += "Missin parameters;";
		}	
		
		try {
			publicationYear = Integer.parseInt(date);
			
			//Take the current year
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			
			//Check if the publicationYear is not bigger than the current year
			if(publicationYear > currentYear)
				error += "Invalid date;";
		}catch(NumberFormatException e) {
			error += "Date not valid;";
		}
		
		//Check if the genre is valid
		if(!(genre.equals("Dance") || genre.equals("Pop") || genre.equals("Rock") || genre.equals("Rap"))) {
			error += "Invalid genre;";
		}
		
		//Check if some input are too long
		if(songTitle.length() > 45)
			error += "Song title too long;";
		if(genre.length() > 45)
			error += "Genre name too long;";
		if(albumTitle.length() > 45)
			error += "Album title too long;";
		if(singer.length() > 45)
			error += "Singer name too long;";
		
		//Take the type of the image file uploaded
		String contentTypeImg = albumImg.getContentType();

		//Check if the type is an image
		if(!contentTypeImg.startsWith("image"))
			error += "Image file not valid;";
		else {
			//If it's an image, check id the size is bigger than 1024KB (1MB)
			if(albumImg.getSize() > 1024000) {
				error += "Image size is too big;";
			}	
		}
		
		//Take the type of the music file uploaded
		String contentTypeMusic = songFile.getContentType();
		
		//Check the type of the music file uploaded
		if(!contentTypeMusic.startsWith("audio"))
			error += "Music file not valid";
		else {
			//If it's a song, check if the size is bigger than 10240KB (about 10MB)
			if(songFile.getSize() > 10240000) {
				error += "Song size is too big;";
			}	
		}
		
		//If an error occurred, redirect with errorMsg1 to the template engine  
		if(!error.equals("")) {
			request.setAttribute("error1", error);
			String path = "/GoToHomePage";

			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;
		}
		
		//Take the name of the image uploaded
		String fileNameImg = Path.of(albumImg.getSubmittedFileName()).getFileName().toString();
		
		//Take the name of the song uploaded
		String fileNameSong = Paths.get(songFile.getSubmittedFileName()).getFileName().toString();
		
		//Create the final path for images adding the user id in the start to avoid error in case of duplicate name;
		fileNameImg = user.getId() + "_" + fileNameImg;
		String outputPathImg = imgFolderPath + fileNameImg;
		
		//Create the final part for music files adding the user id in the start to avoid error in case of duplicate name;
		fileNameSong = user.getId()  + "_" + fileNameSong;
		String outputPathSong = mp3FolderPath + fileNameSong;
		
		//Check if the final path are not too long
		if(fileNameSong.length() > 255)
			error += "Song name too long;";
		if(fileNameImg.length() > 255) {
			error += "Image name too long;";
		}
		
		//TODO to link more songs to the same album this if need to be deleted
		//		it's very difficult that the same singer create 2 album with the same name and publication year
		//Check if a song or an image with the same name already exist
		/*File tempFile = new File(outputPathImg);
		if(tempFile.exists())
			error += "Image name already exists;";*/
		
		//In case of future error the software will be delete the new song only if it is completely new
		File tempFile = new File(outputPathImg);
		if(tempFile.exists())
			isReplaced = true;
		
		tempFile = new File(outputPathSong);
		if(tempFile.exists())
			error += "Song name already exists; ";
		
		//If an error occurred, redirect with errorMsg1 to the template engine  
		if(!error.equals("")) {
			request.setAttribute("error1", error);
			String path = "/GoToHomePage";

			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;
		}
		
		//Save the image if it's not present
		if(!isReplaced) {
			File fileImg = new File(outputPathImg);
			
			try (InputStream fileContent = albumImg.getInputStream()) {
				Files.copy(fileContent, fileImg.toPath());
			} catch (Exception e) {
				error += "Error in uploading the image;";
			}
		}
		
		//If an error occurred, redirect with errorMsg1 to the template engine  
		if(!error.equals("")) {
			request.setAttribute("error1", error);
			String path = "/GoToHomePage";

			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;
		}
		
		//Save the mp3 file
		File fileSong = new File(outputPathSong);
		
		try (InputStream fileContent = songFile.getInputStream()) {
			Files.copy(fileContent, fileSong.toPath());
		} catch (Exception e) {
			error += "Error in uploading the music file;\n";
		}
		
		//If an error occurred, redirect with errorMsg1 to the template engine  
		if(!error.equals("")) {
			request.setAttribute("error1", error);
			String path = "/GoToHomePage";

			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;
		}
		
		//Now the data base can be updated
		
		SongDAO sDao = new SongDAO(connection);
		
		try {
			boolean result = sDao.createSongAndAlbum(user.getId() , songTitle, genre, albumTitle, singer, publicationYear, fileNameImg , fileNameSong);
			
			if(result == true) {
				String path = getServletContext().getContextPath() + "/GoToHomePage";
				response.sendRedirect(path);
			}
			else {
				//Delete uploaded file if something got wrong during the updating of the dataBase
				File file; 
				if(!isReplaced) {
					file = new File(outputPathSong);
					file.delete();
				}
				file = new File(outputPathImg);
				file.delete();
				
				error += "Impossible upload file in the database , try later";
				request.setAttribute("error1", error);
				String path = getServletContext().getContextPath() + "/GoToHomePage";

				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
				dispatcher.forward(request,response);
			}
			
		}catch(SQLException e) {
			//Delete uploaded file if something got wrong with the data base
			File file; 
			if(!isReplaced) {
				file = new File(outputPathSong);
				file.delete();
			}
			file = new File(outputPathImg);
			file.delete();
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An arror occurred uploading the db, retry later");
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








