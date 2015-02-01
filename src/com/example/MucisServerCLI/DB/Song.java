package com.example.MucisServerCLI.DB;

public class Song {

	private int id = -1;
	private String path = "";
	private String artist = "";
	private String album = "";
	private String title = "";
	private String albumArtist = "";
	private String composer = "";
	private String genre = "";
	private String trackNo = "";
	private String discNo = "";
	private String year = "";
	private String comment = "";
	
	public Song(int id, String path, String artist, String album, String title, String albumArtist,
			String composer, String genre, String trackNo, String discNo, String year, String comment){
		
		this.id = id;
		this.path = path;
		this.artist = artist;
		this.album = album;
		this.title = title;
		this.albumArtist = albumArtist;
		this.composer = composer;
		this.genre = genre;
		this.trackNo = trackNo;
		this.discNo = discNo;
		this.year = year;
		this.comment = comment;
		
	}
	
	public void printSong(){
		System.out.println("----------------------------------------------");
		System.out.println("         id: " + id);
		System.out.println("       path: " + path);
		System.out.println("     artist: " + artist);
		System.out.println("      album: " + album);
		System.out.println("      title: " + title);
		System.out.println("albumArtist: " + albumArtist);
		System.out.println("   composer: " + composer);
		System.out.println("      genre: " + genre);
		System.out.println("    trackNo: " + trackNo);
		System.out.println("     discNo: " + discNo);
		System.out.println("       year: " + year);
		System.out.println("    comment: " + comment);
		System.out.println("----------------------------------------------");
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAlbumArtist() {
		return albumArtist;
	}

	public void setAlbumArtist(String albumArtist) {
		this.albumArtist = albumArtist;
	}

	public String getComposer() {
		return composer;
	}

	public void setComposer(String composer) {
		this.composer = composer;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getTrackNo() {
		return trackNo;
	}

	public void setTrackNo(String trackNo) {
		this.trackNo = trackNo;
	}

	public String getDiscNo() {
		return discNo;
	}

	public void setDiscNo(String discNo) {
		this.discNo = discNo;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	
	
}
