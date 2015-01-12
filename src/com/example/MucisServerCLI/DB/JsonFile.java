package com.example.MucisServerCLI.DB;

public class JsonFile {

	private int id = -1;
	private int songId = -1;
	private String songPath = "";
	private String filePath = "";
	private String action = "";
	private String jsonData = "";
	
	public JsonFile(int id,int songId, String songPath, String filePath, String action, String jsonData){
		
		this.id = id;
		this.songId = songId;
		this.songPath = songPath;
		this.filePath = filePath;
		this.action = action;
		this.jsonData = jsonData;
		
	}
	
	public void printJsonFile(){
		System.out.println("----------------------------------------------");
		System.out.println("      id: " + id);
		System.out.println("  songId: " + songId);
		System.out.println("songPath: " + songPath);
		System.out.println("filePath: " + filePath);
		System.out.println("  action: " + action);
		System.out.println("jsonData: " + jsonData);
		System.out.println("----------------------------------------------\n");
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSongId() {
		return songId;
	}

	public void setSongId(int songId) {
		this.songId = songId;
	}

	public String getSongPath() {
		return songPath;
	}

	public void setSongPath(String songPath) {
		this.songPath = songPath;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getJsonData() {
		return jsonData;
	}

	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}
	
	
	
}
