package com.example.MusicServerCLI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.example.MucisServerCLI.DB.DBManager;
import com.example.MucisServerCLI.DB.JsonFile;
import com.example.MucisServerCLI.DB.JsonFileDAO;
import com.example.MucisServerCLI.DB.Song;
import com.example.MucisServerCLI.DB.SongDAO;

public class LibraryManager {
	
	DBManager db = null;
	SongDAO sdao = null;
	JsonFileDAO jdao = null;
	
	private String musicFolder = null;
	
	public LibraryManager(String musicFolder){
		this.musicFolder = musicFolder;
		db = new DBManager();
		sdao = db.getSongDAO();
		jdao = db.getJsonFileDAO();
	}
	
	public String getMusicFolder() {
		return musicFolder;
	}

	public void setMusicFolder(String musicFolder) {
		this.musicFolder = musicFolder;
	}

	public void makeLibrary(){
		
		try{
			
			db.wipeDatabase();
			
			JsonFile initialJsonFile = null;
			
			File path = new File(musicFolder);
			
			String[] extensions = new String[]{"mp3", "m4a", "flac", "wav", "ogg"};
			
			Collection<File> files = FileUtils.listFiles(path, extensions, true);
			Object[] fileArray = files.toArray();
			
			JSONArray list = new JSONArray();

			for(Integer i = 0; i < fileArray.length; i++){
				
				// convert from object into file
				File tempFile = (File) fileArray[i];
				
				// put ID3 information into string array
				String[] metaData = readID3(tempFile);

				if(metaData != null){	
					try {
						JSONObject jsonObj = new JSONObject();
						String pathUrl = metaData[0];
						pathUrl = pathUrl.replace(musicFolder, "");
						pathUrl = pathUrl.replace(" ", "%20");
						pathUrl = "http://" + InetAddress.getLocalHost().getHostName() + ":4231/" + pathUrl;
						
						System.out.println("URL         : " + pathUrl);
						
						jsonObj.put("Path", pathUrl);
						jsonObj.put("Artist", metaData[1]);
						jsonObj.put("Album", metaData[2]);
						jsonObj.put("Title", metaData[3]);
						jsonObj.put("AlbumArtist", metaData[4]);
						jsonObj.put("Composer", metaData[5]);
						jsonObj.put("Genre", metaData[6]);
						jsonObj.put("Track", metaData[7]);
						jsonObj.put("DiscNo", metaData[8]);
						jsonObj.put("Year", metaData[9]);
						jsonObj.put("Comment", metaData[10]);
						
						list.add(list.size(), jsonObj);
						
						int songId = sdao.getMaxId();
						
						sdao.insertSong(new Song(songId, pathUrl, metaData[1], metaData[2], 
								metaData[3], metaData[4], metaData[5], metaData[6], metaData[7], metaData[8], 
								metaData[9],metaData[10]));
						
						Song tempSong = sdao.getSong(songId);
						tempSong.printSong();
						
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SQLException e){
						e.printStackTrace();
					}
				}
			}
			
			JSONObject finalJSON = new JSONObject();
			finalJSON.put("Songs", list);
		//	System.out.println(finalJSON.toJSONString());
			
			
			try{
				String filePath = musicFolder + "/.LibraryIndex/MusicIndex.json";
				String jsonData = finalJSON.toJSONString();
				deleteJSONFile();
				FileWriter fileWrite = new FileWriter(filePath, false);
				fileWrite.write(jsonData);
				fileWrite.flush();
				fileWrite.close();
				
				int id = 0;
				int songId = -1;
				String songPath = "";
				String action = "C";
	// not put json data for MusicLibrary.json into DB, won't fit
	// try using a BLOB??
				
				initialJsonFile = new JsonFile(id, songId, songPath, filePath, action, "");
				
				jdao.insertJsonFile(initialJsonFile);
				
				JsonFile tempFile = jdao.getJsonFile(id);
				tempFile.printJsonFile();
				
			}catch(IOException e){
				e.printStackTrace();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		
	}
	
	public void addSongFromPath(String filePath){
		
		// check song doesnt exist
		if(!sdao.checkSongExistsByPath(filePath)){
			// convert from object into file
			File file = new File(filePath);
			
			// put ID3 information into string array
			String[] metaData = readID3(file);

			if(metaData != null){	
				try {
					JSONObject jsonObj = new JSONObject();
					String pathUrl = metaData[0];
					pathUrl = pathUrl.replace(musicFolder, "");
					pathUrl = pathUrl.replace(" ", "%20");
					pathUrl = "http://" + InetAddress.getLocalHost().getHostName() + ":4231/" + pathUrl;
					
					System.out.println("URL         : " + pathUrl);
					
					jsonObj.put("Action", "C");
					jsonObj.put("Path", pathUrl);
					jsonObj.put("Artist", metaData[1]);
					jsonObj.put("Album", metaData[2]);
					jsonObj.put("Title", metaData[3]);
					jsonObj.put("AlbumArtist", metaData[4]);
					jsonObj.put("Composer", metaData[5]);
					jsonObj.put("Genre", metaData[6]);
					jsonObj.put("Track", metaData[7]);
					jsonObj.put("DiscNo", metaData[8]);
					jsonObj.put("Year", metaData[9]);
					jsonObj.put("Comment", metaData[10]);
					
					int songId = sdao.getMaxId();
					
					sdao.insertSong(new Song(songId, pathUrl, metaData[1], metaData[2], 
							metaData[3], metaData[4], metaData[5], metaData[6], metaData[7], metaData[8], 
							metaData[9],metaData[10]));
					
					Song tempSong = sdao.getSong(songId);
					tempSong.printSong();
					
					// WRITE JSON FILE
					int jsonFileId = jdao.getMaxId();
					String jsonFilePath = musicFolder + "/.LibraryIndex/Update-" + jsonFileId + ".json";
					String action = "C";
					String jsonData = jsonObj.toJSONString();
					JsonFile jsonFile = new JsonFile(jsonFileId, songId, pathUrl, jsonFilePath, action, jsonData);
					
					FileWriter fileWrite = new FileWriter(jsonFilePath, false);
					fileWrite.write(jsonData);
					fileWrite.flush();
					fileWrite.close();
					
					System.out.println("saved json file.");
					
					jdao.insertJsonFile(jsonFile);
					
					JsonFile tempFile = jdao.getJsonFile(jsonFileId);
					tempFile.printJsonFile();
					
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e){
					e.printStackTrace();
				} catch (IOException e){
					e.printStackTrace();
				}
			}
		}

	}
	
	public void deleteSongFromPath(){
		System.out.println("DELETE SONG TO BE DONE");
	}
	
	public String[] readID3(File file){
		
		String[] tags = null;
		
		try{
			AudioFile f = AudioFileIO.read(file);
			Tag tag = f.getTag();
		
			
			// get all ID3 tags into individual strings
			if((tag != null) && (file != null)){
				System.out.println("\n");
				
				String path = file.getAbsolutePath().toString();
				
				String artist = tag.getFirst(FieldKey.ARTIST);
				System.out.println("Artist      : " + artist);
				
				String album = tag.getFirst(FieldKey.ALBUM);
				System.out.println("Album       : " + album);
				
				String title = tag.getFirst(FieldKey.TITLE);
				System.out.println("Title       : " + title);
				
				String albumArtist = tag.getFirst(FieldKey.ALBUM_ARTIST);
				System.out.println("Album Artist: " + albumArtist);
				
				String composer = tag.getFirst(FieldKey.COMPOSER);
				System.out.println("Composer    : " + composer);
				
				String genre = tag.getFirst(FieldKey.GENRE);
				System.out.println("Genre       : " + genre);
				
				String track = tag.getFirst(FieldKey.TRACK);
				System.out.println("Track       : " + track);
				
				String discNo = tag.getFirst(FieldKey.DISC_NO);
				System.out.println("Discn no.   : " + discNo);
				
				String year = tag.getFirst(FieldKey.YEAR);
				System.out.println("Year        : " + year);
				
				String comment = tag.getFirst(FieldKey.COMMENT);
				System.out.println("Comment     : " + comment);
				
				// put individual strings into array
				tags = new String[]{path, artist, album, title, 
									albumArtist, composer, genre, 
									track, discNo, year, comment};
			}
			
			return tags;
		
		} catch (CannotReadException e) {
			//e.printStackTrace();
		}catch(IOException e){
			//e.printStackTrace();
		}catch(TagException e){
			//e.printStackTrace();
		}catch(ReadOnlyFileException e){
			//e.printStackTrace();
		}catch(InvalidAudioFrameException e){
			//e.printStackTrace();
		}catch(StringIndexOutOfBoundsException e){
			//e.printStackTrace();
		}
		
		return tags;

	}
	
	public static boolean deleteJSONFile(){
		return (new File("JSONFilePath").delete());
	}
}