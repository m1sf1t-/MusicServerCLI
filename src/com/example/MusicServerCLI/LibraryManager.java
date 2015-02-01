package com.example.MusicServerCLI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
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
	boolean publicServer = false;
	private String localHostname = null;
	private String publicHostname = null;
	
	private String musicFolder = null;
	
	public LibraryManager(String musicFolder, boolean publicServer){
		this.musicFolder = musicFolder;
		db = new DBManager();
		sdao = db.getSongDAO();
		jdao = db.getJsonFileDAO();
		this.publicServer = publicServer;
		localHostname = getLocalHostname();
		publicHostname = getPublicHostname();
	}
	
	public String getMusicFolder() {
		return musicFolder;
	}

	public void setMusicFolder(String musicFolder) {
		this.musicFolder = musicFolder;
	}

	public void makeLibrary(){
		
		db.wipeDatabase();
			
		File path = new File(musicFolder);
		String[] extensions = new String[]{"mp3", "m4a", "flac", "wav", "ogg"};
		Collection<File> files = FileUtils.listFiles(path, extensions, true);
		Object[] fileArray = files.toArray();
		JSONArray list = new JSONArray();

		for(Integer i = 0; i < fileArray.length; i++){
				
			File tempFile = (File) fileArray[i];
			String[] metaData = readID3(tempFile);

			if(metaData != null){	
				// stick a "/" before the url because there isn't one :S
				metaData[0] = "/" + metaData[0];
				
				// windows compatibility - doesn't work :\
				/*metaData[0].replace("\\", "\\/");
				metaData[0].replaceAll("\\\\", "\\/");
				
				System.out.println("WINDOWSCOMPAT: Replacing backslashes..." + metaData[0]);*/
				
				metaData[0] = pathToUrl(metaData[0]);
					
				JSONObject jsonObj = makeJsonObject(metaData[0], metaData[1], metaData[2], metaData[3], metaData[4],
						metaData[5], metaData[6], metaData[7], metaData[8], metaData[9], metaData[10]);
						
				list.add(list.size(), jsonObj);
						
				int songId = sdao.getMaxId();
						
				try{
					sdao.insertSong(new Song(songId, metaData[0], metaData[1], metaData[2], 
							metaData[3], metaData[4], metaData[5], metaData[6], metaData[7], metaData[8], 
							metaData[9],metaData[10]));
				}catch(SQLException e){
					e.printStackTrace();
				}
						
				Song tempSong = sdao.getSong(songId);
				tempSong.printSong();
			}
		}

		String filePath = musicFolder + "/.LibraryIndex/MusicIndex.json";
			
		try{
			// "true" passed to writeJsonFile() - DB insert overridden here
			jdao.insertJsonFile(new JsonFile(0, -1, "", filePath, "C", "No JSON data."));
		}catch (SQLException e){
			e.printStackTrace();
		}

		JSONObject finalJSON = new JSONObject();
		finalJSON.put("Songs", list);
		
		deleteJSONFile();
		writeJsonFile("C", finalJSON, -1, filePath, true);

	}
	
	public void addSongFromPath(String filePath){
		
		// check song doesnt exist
		if(!sdao.checkSongExistsByPath(filePath)){
			
			String[] metaData = readID3(new File(filePath));

			if(metaData != null){	
			
				metaData[0] = pathToUrl(metaData[0]);
				
				JSONObject jsonObj = makeJsonObject(metaData[0], metaData[1], metaData[2], metaData[3], metaData[4],
						metaData[5], metaData[6], metaData[7], metaData[8], metaData[9], metaData[10]);
				
				jsonObj.put("Action", "C");
				
				int songId = sdao.getMaxId();
				
				try {
					sdao.insertSong(new Song(songId, metaData[0], metaData[1], metaData[2], metaData[3], metaData[4], metaData[5], metaData[6], metaData[7], metaData[8], metaData[9], metaData[10]));
					writeJsonFile("C", jsonObj, songId, metaData[0], false);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void modifySongFromPath(String filePath){
		
		String[] metaData = readID3(new File(filePath));
		
		if(metaData != null){
			
			metaData[0] = pathToUrl(metaData[0]);
			
			JSONObject jsonObj = makeJsonObject(metaData[0], metaData[1], metaData[2], metaData[3], metaData[4],
					metaData[5], metaData[6], metaData[7], metaData[8], metaData[9], metaData[10]);

			jsonObj.put("Action", "U");
			int songId = sdao.getMaxId();
			
			try{
				if(sdao.checkSongExistsByPath(metaData[0])){
					
					Song song = sdao.getSongFromPath(metaData[0]);
					
					song.setArtist(metaData[1]);
					song.setAlbum(metaData[2]);
					song.setTitle(metaData[3]);
					song.setAlbumArtist(metaData[4]);
					song.setComposer(metaData[5]);
					song.setGenre(metaData[6]);
					song.setTrackNo(metaData[7]);
					song.setDiscNo(metaData[8]);
					song.setYear(metaData[9]);
					song.setComment(metaData[10]);
					
					sdao.updateSong(song);
					writeJsonFile("U", jsonObj, songId, metaData[0], false);
				}else{
					sdao.insertSong(new Song(songId, metaData[0], metaData[1], metaData[2], metaData[3], metaData[4], metaData[5], 
												metaData[6], metaData[7], metaData[8], metaData[9], metaData[10]));
					
					writeJsonFile("U", jsonObj, songId, metaData[0], false);
				}
			}catch(SQLIntegrityConstraintViolationException e){
				System.out.println("found duplicate.");
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
	}
	
	public void deleteSongFromPath(String filePath){
		
		filePath = pathToUrl(filePath);
		
		if(sdao.checkSongExistsByPath(filePath)){
			try {
				Song song = sdao.getSongFromPath(filePath);
				
				JSONObject jsonObj = makeJsonObject(song.getPath(), song.getArtist(), song.getAlbum(), song.getTitle(),
													song.getAlbumArtist(), song.getComposer(), song.getGenre(), song.getTrackNo(), 
													song.getDiscNo(), song.getYear(), song.getComment());
				
				jsonObj.put("Action", "D");
				
				sdao.deleteSong(filePath);
				
				writeJsonFile("D", jsonObj, song.getId(), song.getPath(), false);
				
				System.out.println("URL Deleted         : " + song.getPath());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else{
			System.out.println("Song not found in database: " + filePath);
		}
	}
	
	public void deleteSongsLikePath(String path){
		try{
			
			path = pathToUrl(path);

			// unique json object - not calling makeJsonObject()
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("Action", "DD");
			jsonObj.put("Path", path);
			
			int rows = DBManager.processStatement("DELETE FROM songs WHERE path LIKE '" + path.replace("'", "''") + "%'");
			
			// only write a file if something has been deleted
			if(rows > 0){
				writeJsonFile("DD", jsonObj, -1, "", false);
			}
			
			System.out.println(rows + " leftover song/s deleted under " + path);
			
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public void writeJsonFile(String action, JSONObject jsonObj, int songId, String pathUrl, boolean initialLib){
		int jsonFileId = jdao.getMaxId();
		String jsonFilePath = null;
		String jsonData = jsonObj.toJSONString();
		
		if(initialLib){
			jsonFilePath = musicFolder + "/.LibraryIndex/MusicIndex";
		}else{
			jsonFilePath = musicFolder + "/.LibraryIndex/" + jsonFileId;
		}

		JsonFile jsonFile = new JsonFile(jsonFileId, songId, pathUrl, jsonFilePath, action, jsonData);
		
		try {
			FileWriter fileWrite = new FileWriter(jsonFilePath, false);
			fileWrite.write(jsonData);
			fileWrite.flush();
			fileWrite.close();
			
			if(!initialLib){
				jdao.insertJsonFile(jsonFile);
			}

		}catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		System.out.println("saved json file.");
	}
	
	public String[] readID3(File file){
		
		String[] tags = null;
		
		try{
			AudioFile f = AudioFileIO.read(file);
			Tag tag = f.getTag();
		
			
			// get all ID3 tags into individual strings
			if((tag != null) && (file != null)){
				
				String path = file.getAbsolutePath().toString();
				String artist = tag.getFirst(FieldKey.ARTIST);
				String album = tag.getFirst(FieldKey.ALBUM);
				String title = tag.getFirst(FieldKey.TITLE);
				String albumArtist = tag.getFirst(FieldKey.ALBUM_ARTIST);
				String composer = tag.getFirst(FieldKey.COMPOSER);
				String genre = tag.getFirst(FieldKey.GENRE);
				String track = tag.getFirst(FieldKey.TRACK);
				String discNo = tag.getFirst(FieldKey.DISC_NO);
				String year = tag.getFirst(FieldKey.YEAR);
				String comment = tag.getFirst(FieldKey.COMMENT);
				
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
	
	public static String getPublicHostname(){
		
		try {
			URL whatIsMyIp = new URL("http://checkip.amazonaws.com/");
			
			BufferedReader in = new BufferedReader(new InputStreamReader(whatIsMyIp.openStream()));
			
			String ip = in.readLine(); 
			
			InetAddress inetAddr = InetAddress.getByName(ip);
			
			String hostname = inetAddr.getHostName();
			
			String canonicalHostname = inetAddr.getCanonicalHostName();
			
			return hostname;
			
		}catch(MalformedURLException e) {
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String getLocalHostname(){
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject makeJsonObject(String pathUrl, String artist, String album, String title,
										String albumArtist, String composer, String genre, String trackNo,
										String discNo, String year, String comment){
		
		JSONObject jsonObj = new JSONObject();
		
		jsonObj.put("Path", pathUrl);
		jsonObj.put("Artist", artist);
		jsonObj.put("Album", album);
		jsonObj.put("Title", title);
		jsonObj.put("AlbumArtist", albumArtist);
		jsonObj.put("Composer", composer);
		jsonObj.put("Genre", genre);
		jsonObj.put("Track", trackNo);
		jsonObj.put("DiscNo", discNo);
		jsonObj.put("Year", year);
		jsonObj.put("Comment", comment);
		
		return jsonObj;
	}
	
	public String pathToUrl(String path){
		
		String url = path.replace(musicFolder, "");
		
		url = url.replace(" ", "%20");
		
		if(publicServer){
			url = "http://" + publicHostname + ":4231" + url;
		}else{
			url = "http://" + localHostname + ":4231" + url;
		}
		
		return url;
	}
}