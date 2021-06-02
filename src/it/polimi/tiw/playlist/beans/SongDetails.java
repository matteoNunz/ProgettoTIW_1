package it.polimi.tiw.playlist.beans;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Blob;
import java.util.Base64;

public class SongDetails{
	private int id;
	private String songTitle;
	private String albumTitle;
	private String singer;
	private String kindOf;
	private String songFile;//where is the music file
	private String imgFile;
	private int publicationYear;
	
	
	private InputStream image;
	private byte[] imageBytes;
	private File imageFile;
	private FileInputStream fileStream;
	private Blob imageBlob;
	
	
	
	/**
	 * 
	 * @return the id of the song
	 */
	public int getId() {
		return id;
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
	public String getKindOf() {
		return kindOf;
	}
	
	/**
	 * 
	 * @return the path of the music file
	 */
	public String getSongFile() {
		return songFile;
	}
	
	/**
	 * 
	 * @return the path of the image
	 */
	public String getImgFile() {
		return imgFile;
	}
	
	/**
	 * 
	 * @return the publication year of the album that contains the song
	 */
	public int getPublicationYear() {
		return publicationYear;
	}
	
	/**
	 * 
	 * @return the inputStream of the image
	 */
	public InputStream getImage() {
		return image;
	}
	
	/**
	 * Set the song id
	 * @param id is the unique id of the song
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Set the SongTitle
	 * @param newSongTitle is the new title of the song
	 */
	public void setSongTitle(String newSongTitle) {
		this.songTitle = newSongTitle;
	}
	
	/**
	 * Set the title of the album that contains the song
	 * @param newAlbumTitle
	 */
	public void setAlbumTitle(String newAlbumTitle) {
		this.albumTitle = newAlbumTitle;
	}
	
	
	/**
	 * Set the name of singer
	 * @param newSinger is the new name
	 */
	public void setSinger(String newSinger) {
		this.singer = newSinger;
	}
	
	/**
	 * Set the kind of the song
	 * @param newKindOf is the new type
	 */
	public void setKindOf(String newKindOf) {
		this.kindOf = newKindOf;
	}
	
	/**
	 * Set the position of the file
	 * @param newFile is the position where the file is stored
	 */
	public void setFile(String newFile) {
		this.songFile = newFile;
	}
	
	/**
	 * Set the position of the image
	 * @param newFile is the position where the song is stored
	 */
	public void setImgFile(String newFile) {
		this.imgFile = newFile;
		
		/*File oldImgPath = new File(imgFile);
		//String finalPath = "D:\\Eclipse\\TIW-PlayList-HTML-Pure\\WebContent\\WEB-INF\\Resources\\Images\\" + oldImgPath.getName();
		String finalPath = "/../../../WebContent/Resources/Images/" + oldImgPath.getName();
		//File newImgPath = new File("/TIW-PlayList-HTML-Pure/WebContent/WEB-INF/Resources/Images/" + oldImgPath.getName());//open the folder where save the image
		//File newImgPath = new File("/WEB-INF/Resources/Images/" + oldImgPath.getName());
		File newImgPath = new File(finalPath);
		

		try {
			FileInputStream fileInputStream = new FileInputStream(oldImgPath);
			//InputStream targetStream = FileUtils.openInputStream(oldImgPath);
			Files.copy(fileInputStream, newImgPath.toPath() , StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image saved on server correctly");
			imgFile = finalPath;
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("Old path: " + oldImgPath.toPath());
		System.out.println("New path: " + newImgPath.toPath());*/
	}
	
	/**
	 * Set the publication year
	 * @param date is the year 
	 */
	public void setPublicationYear(int date) {
		this.publicationYear = date;
	}
	
	/**
	 * Set the inputStream image
	 * @param image is the input stream read from the db
	 */
	public void setImage(InputStream image) {
		this.image = image;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}

	public void setImageBytes(byte[] imageBytes) {
		System.out.println("Image bytes saved");
		this.imageBytes = imageBytes;
		//Code the bytes array in base64
		//imageBytes = Base64.getEncoder().encode(imageBytes);
		//System.out.println("base64 is: " + imageBytes);
	}

	public File getImageFile() {
		return imageFile;
	}

	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
		imageFile.setReadable(true);
	}

	public FileInputStream getFileStream() {
		return fileStream;
	}

	public void setFileStream(){
		try {
			//Create the fileStream
			fileStream = new FileInputStream(imgFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Blob getImageBlob() {
		return imageBlob;
	}

	public void setImageBlob(Blob imageBlob) {
		this.imageBlob = imageBlob;
	}
}


