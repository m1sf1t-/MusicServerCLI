package com.example.MucisServerCLI.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.example.MusicServerCLI.LibraryManager;

public class SongDAO {

	Connection con = null;
	PreparedStatement pstmt = null;
	
	private int maxId = 0;
	
	private static final String selectSongSQL = 
			"SELECT path, artist, album, title, albumArtist, " +
			       "composer, genre, trackNo, discNo, songYear, comment " +
			"FROM songs WHERE id = ?";
	
	private static final String selectSongByPathSQL = 
			"SELECT id, artist, album, title, albumArtist, " +
			       "composer, genre, trackNo, discNo, songYear, comment " +
			"FROM songs WHERE path = ?";
	
	private static final String insertSongSQL = 
			"INSERT INTO songs " +
			"(id, path, artist, album, title, albumArtist, composer, " +
			"genre, trackNo, discNo, songYear, comment) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private static final String updateSongSQL = 
			"UPDATE songs " +
			"SET path = ?, artist = ?, album = ?, " +
			"title = ?, albumArtist = ?, composer = ?, " +
			"genre = ?, trackNo = ?, discNo = ?, songYear = ?, comment = ? " +
			"WHERE id = ?";
	
	private static final String deleteSongSQL = 
			"DELETE FROM songs WHERE path = ?";
	
	public SongDAO(Connection con){
		this.con = con;

		updateMaxId();
	}
	
	private void updateMaxId(){
		try{
			
			Statement stmt = con.createStatement();
			
			ResultSet rs = 
					stmt.executeQuery("SELECT MAX(id) FROM songs");
			
			if(rs.next()){
				maxId = rs.getInt(1);
			}
			
			rs.close();
			stmt.close();
		}catch(SQLException se){
			se.printStackTrace();
		}
	}
	
	public int getMaxId(){
		updateMaxId();
		return this.maxId + 1;
	}
	
	public Song getSong(int targetId){
		Song song = null;
		
		try{
			pstmt = con.prepareStatement(selectSongSQL);
			pstmt.clearParameters();
			pstmt.setInt(1, targetId);
			
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next()){
				String path = rs.getString("path");
				String artist = rs.getString("artist");
				String album = rs.getString("album");
				String title = rs.getString("title");
				String albumArtist = rs.getString("albumArtist");
				String composer = rs.getString("composer");
				String genre = rs.getString("genre");
				String trackNo  = rs.getString("trackNo");
				String discNo = rs.getString("discNo");
				String year = rs.getString("songYear");
				String comment = rs.getString("comment");
				
				song = new Song(targetId, path, artist, album, title, albumArtist, 
						composer, genre, trackNo, discNo, year, comment);
				
			}
		}catch(SQLException se){
			se.printStackTrace();
		}
		
		return song;
	}
	
    public Song getSongFromPath(String path){
		Song song = null;
		
		try{
			pstmt = con.prepareStatement(selectSongByPathSQL);
			pstmt.clearParameters();
			pstmt.setString(1, path);
			
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next()){
				int id = rs.getInt("id");
				String artist = rs.getString("artist");
				String album = rs.getString("album");
				String title = rs.getString("title");
				String albumArtist = rs.getString("albumArtist");
				String composer = rs.getString("composer");
				String genre = rs.getString("genre");
				String trackNo  = rs.getString("trackNo");
				String discNo = rs.getString("discNo");
				String year = rs.getString("songYear");
				String comment = rs.getString("comment");
				
				song = new Song(id, path, artist, album, title, albumArtist, 
						composer, genre, trackNo, discNo, year, comment);
				
			}
		}catch(SQLException se){
			se.printStackTrace();
		}
		
		return song;
    }
	
    public void insertSong(Song song) throws SQLException{
    	
    	// Will count the number of rows inserted into DB.
    	int numRows = 0;
    	
    	// uses java.sql.Connection (con) to create a PreparedStatement.
    	PreparedStatement stmt = con.prepareStatement(insertSongSQL);
    	
    	//                                stmt.setXXX(1, dataToBeInserted);
    	//                   set certain data type ^  |          ^-------------<
    	//                                            ^------------------<      |
    	// This number represents which "?" is in the PreparedStatement --^     |
    	// This is a reference to a variable that is to be inserted-------------^
    	
    	stmt.setInt(1, getMaxId());
    	stmt.setString(2, escapeApos(song.getPath()));
    	stmt.setString(3, escapeApos(song.getArtist()));
    	stmt.setString(4, escapeApos(song.getAlbum()));
    	stmt.setString(5, escapeApos(song.getTitle()));
    	stmt.setString(6, escapeApos(song.getAlbumArtist()));
    	stmt.setString(7, escapeApos(song.getComposer()));
    	stmt.setString(8, escapeApos(song.getGenre()));
    	stmt.setString(9, escapeApos(song.getTrackNo()));
    	stmt.setString(10, escapeApos(song.getDiscNo()));
    	stmt.setString(11, escapeApos(song.getYear()));
    	stmt.setString(12, escapeApos(song.getComment()));
    	
    	numRows += stmt.executeUpdate();
    	
    	
    	System.out.println("Entry added to songs table.\n");
    	
    	// ALWAYS REMEMBER TO CLOSE
    	stmt.close();
    }

    public void updateSong(Song song) throws SQLException{
    	
    	// Will count the number of rows inserted into DB.
    	int numRows = 0;
    	
    	// uses java.sql.Connection (con) to create a PreparedStatement.
    	PreparedStatement stmt = con.prepareStatement(updateSongSQL);
    	
    	//                                stmt.setXXX(1, dataToBeInserted);
    	//                   set certain data type ^  |          ^-------------<
    	//                                            ^------------------<      |
    	// This number represents which "?" is in the PreparedStatement --^     |
    	// This is a reference to a variable that is to be inserted-------------^
    	
    	stmt.setString(1, escapeApos(song.getPath()));
    	stmt.setString(2, escapeApos(song.getArtist()));
    	stmt.setString(3, escapeApos(song.getAlbum()));
    	stmt.setString(4, escapeApos(song.getTitle()));
    	stmt.setString(5, escapeApos(song.getAlbumArtist()));
    	stmt.setString(6, escapeApos(song.getComposer()));
    	stmt.setString(7, escapeApos(song.getGenre()));
    	stmt.setString(8, escapeApos(song.getTrackNo()));
    	stmt.setString(9, escapeApos(song.getDiscNo()));
    	stmt.setString(10, escapeApos(song.getYear()));
    	stmt.setString(11, escapeApos(song.getComment()));
    	
    	stmt.setInt(12, song.getId());
    	
    	numRows += stmt.executeUpdate();
    	
    	
    	System.out.println("\n" + numRows + " row/s updated in songs table.\n");
    	
    	// ALWAYS REMEMBER TO CLOSE
    	stmt.close();
    }
    
    public void deleteSong(String filePath) throws SQLException{
    	int numRows = 0;
    	PreparedStatement stmt = con.prepareStatement(deleteSongSQL);
    	stmt.setString(1, escapeApos(filePath));
    	numRows += stmt.executeUpdate();
    	System.out.println("\n" + numRows + " row/s deleted from songs table.\n");
    	stmt.close();
    }
    
    public boolean checkSongExistsByPath(String path){
    	boolean exists = false;
		try{
			
			Statement stmt = con.createStatement();
			
			ResultSet rs = 
					stmt.executeQuery("SELECT id FROM songs WHERE path = '" + escapeApos(path) + "'");
			
			if(rs.next()){
				exists = true;
			}
			
			rs.close();
			stmt.close();
		}catch(SQLException se){
			se.printStackTrace();
		}
		
		return exists;
    }
    
    private String escapeApos(String string){
    	return string.replace("'", "''");
    }

}
