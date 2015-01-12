package com.example.MucisServerCLI.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JsonFileDAO {

	Connection con = null;
	PreparedStatement pstmt = null;
	
	private int maxId = 0;
	
	private static final String selectJsonFileSQL = 
			"SELECT songId, songPath, filePath, action, jsonData " +
			"FROM jsonFiles WHERE id = ?";
	
	private static final String insertJsonFileSQL = 
			"INSERT INTO jsonFiles " +
			"(id, songId, songPath, filePath, action, jsonData) " +
			"VALUES (?, ?, ?, ?, ?, ?)";
	
	private static final String updateJsonFileSQL = 
			"UPDATE jsonFiles " +
			"SET songId = ?, songPath = ?, filePath = ?, " +
			"action = ?, jsonData = ?" +
			"WHERE id = ?";
	
	public JsonFileDAO(Connection con){
		this.con = con;

		updateMaxId();
	}
	
	public void updateMaxId(){
		try{
			
			Statement stmt = con.createStatement();
			
			ResultSet rs = 
					stmt.executeQuery("SELECT MAX(id) FROM jsonFiles");
			
			if(rs.next()){
				maxId = rs.getInt(1);
			}
			
			rs.close();
			stmt.close();
		}catch(SQLException se){
			printSQLException(se);
		}
	}
	
	public int getMaxId(){
		updateMaxId();
		return this.maxId + 1;
	}
	
	public JsonFile getJsonFile(int targetId){
		JsonFile jsonFile = null;
		
		try{
			pstmt = con.prepareStatement(selectJsonFileSQL);
			pstmt.clearParameters();
			pstmt.setInt(1, targetId);
			
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next()){
				int songId = rs.getInt("songId");
				String songPath = rs.getString("songPath");
				String filePath = rs.getString("filePath");
				String action = rs.getString("action");
				String jsonData = rs.getString("jsonData");
				
				jsonFile = new JsonFile(targetId, songId, songPath, filePath, action, jsonData);
			}
		}catch(SQLException se){
			printSQLException(se);
		}
		
		return jsonFile;
	}
	
    public void insertJsonFile(JsonFile jsonFile) throws SQLException{
    	
    	// Will count the number of rows inserted into DB.
    	int numRows = 0;
    	
    	// uses java.sql.Connection (con) to create a PreparedStatement.
    	PreparedStatement stmt = con.prepareStatement(insertJsonFileSQL);
    	
    	//                                stmt.setXXX(1, dataToBeInserted);
    	//                   set certain data type ^  |          ^-------------<
    	//                                            ^------------------<      |
    	// This number represents which "?" is in the PreparedStatement --^     |
    	// This is a reference to a variable that is to be inserted-------------^
    	int jsonFileId = jsonFile.getId();
    	stmt.setInt(1, jsonFileId);
    	stmt.setInt(2, jsonFile.getSongId());
    	stmt.setString(3, fixApostrophes(jsonFile.getSongPath()));
    	stmt.setString(4, fixApostrophes(jsonFile.getFilePath()));
    	stmt.setString(5, fixApostrophes(jsonFile.getAction()));
    	stmt.setString(6, fixApostrophes(jsonFile.getJsonData()));
    	
    	numRows += stmt.executeUpdate();
    	
    	/*
    	 * 				"INSERT INTO jsonFiles " +
			"(id, songId, songPath, filePath, action, jsonData) " +
			"VALUES (?, ?, ?, ?, ?, ?)";
    	 */
    	
    	System.out.println("\n" + numRows + " row/s inserted into jsonFiles table.\n");
    	
    	// ALWAYS REMEMBER TO CLOSE
    	stmt.close();
    }

    public void updateJsonFile(JsonFile jsonFile) throws SQLException{
    	
    	// Will count the number of rows inserted into DB.
    	int numRows = 0;
    	
    	// uses java.sql.Connection (con) to create a PreparedStatement.
    	PreparedStatement stmt = con.prepareStatement(updateJsonFileSQL);
    	
    	//                                stmt.setXXX(1, dataToBeInserted);
    	//                   set certain data type ^  |          ^-------------<
    	//                                            ^------------------<      |
    	// This number represents which "?" is in the PreparedStatement --^     |
    	// This is a reference to a variable that is to be inserted-------------^
    	
    	stmt.setInt(1, jsonFile.getSongId());
    	stmt.setString(2, jsonFile.getSongPath());
    	stmt.setString(3, jsonFile.getFilePath());
    	stmt.setString(4, jsonFile.getAction());
    	stmt.setString(5, jsonFile.getJsonData());
    	
    	numRows += stmt.executeUpdate();
    	
    	System.out.println("\n" + numRows + " row/s updated in jsonFiles table.\n");
    	
    	// ALWAYS REMEMBER TO CLOSE
    	stmt.close();
    }
    
    static void printSQLException(SQLException se) {
        while(se != null) {

            System.out.print("SQLException: State:   " + se.getSQLState());
            System.out.println("Severity: " + se.getErrorCode());
            System.out.println(se.getMessage());   
            
            se = se.getNextException();
        }
    }
    
    public String fixApostrophes(String string){
    	return string.replace("'", "''");
    }
}
