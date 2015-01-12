package com.example.MucisServerCLI.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
	
	private static Connection con = null;
	
	private static final String driver = 
			"org.apache.derby.jdbc.EmbeddedDriver";
	
	private static final String url = 
			"jdbc:derby:";
	
	private static final String dbName = "MusicLibrary";
	
	private static final String createSongsSQL = 
			"CREATE TABLE songs (" +
			"id INT PRIMARY KEY, " +
			"path VARCHAR(32672) NOT NULL, " +
			"artist VARCHAR(32672), " +
			"album VARCHAR(32672), " +
			"title VARCHAR(32672), " +
			"albumArtist VARCHAR(32672), " +
			"composer VARCHAR(32672), " +
			"genre VARCHAR(32672), " +
			"trackNo VARCHAR(32672), " +
			"discNo VARCHAR(32672), " +
			"songYear VARCHAR(32672), " +
			"comment VARCHAR(32672))";
	
	private static final String createJsonFilesSQL = 
			"CREATE TABLE jsonFiles (" +
			"id INT PRIMARY KEY, " +
			"songId INT NOT NULL, " +
			"songPath VARCHAR(32672) NOT NULL," +
			"filePath VARCHAR(32672) NOT NULL, " +
			"action VARCHAR(1) NOT NULL, " +
			"jsonData VARCHAR(32672) NOT NULL)";
	
	private SongDAO sdao = null;
	private JsonFileDAO jdao = null;
	
	public DBManager(){
		if(!dbExists()){
			try{
				Class.forName(driver);
				con = DriverManager.getConnection(url + dbName + ";create=true");
				
				System.out.println("No database exists. Creating database...");
				int rows = processStatement(createSongsSQL);
				System.out.println(rows + " rows inserted into customers.");
				
				rows = processStatement(createJsonFilesSQL);
				System.out.println(rows + " rows inserted into jsonFiles.");
				System.out.println("Database created sucessfully!");

			}catch(ClassNotFoundException ce){
				System.out.println("Class not found! Is derby in the CLASSPATH?");
			}catch(SQLException se){
				printSQLException(se);
			}
		}
		sdao = new SongDAO(con);
		jdao = new JsonFileDAO(con);
	}
	
	public void close(){
		try{
			con = DriverManager.getConnection(url + ";shutdown=true");
		}catch(SQLException se){
			// Do nothing, system has now shut down.
		}
	}
	
	public SongDAO getSongDAO(){
		return sdao;
	}
	
	public JsonFileDAO getJsonFileDAO(){
		return jdao;
	}
	
	private boolean dbExists(){
		Boolean exists = false;
		
		try{
			Class.forName(driver);
			con = DriverManager.getConnection(url + dbName);
			exists = true;
		}catch(Exception e){
			// 
		}
		
		return exists;
	}
	
	public void wipeDatabase(){
		try {
			System.out.println("Deleting tables...");
			processStatement(" DROP TABLE songs");
			processStatement(" DROP TABLE jsonFiles");
			
			int rows = processStatement(createSongsSQL);
			System.out.println(rows + " rows inserted into customers.");
			
			rows = processStatement(createJsonFilesSQL);
			System.out.println(rows + " rows inserted into jsonFiles.");
			System.out.println("Database created sucessfully!");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			printSQLException(e);
		}
	}
	
    static int processStatement(String sql) throws SQLException{
    	
    	System.out.println("SQL statement: \n'" + sql + "' executing...");
    	
    	Statement stmt = con.createStatement();
    	int count = stmt.executeUpdate(sql);
    	
    	stmt.close();
    	
    	System.out.println("Successful.");
    	return(count);
    }
    
    static void printSQLException(SQLException se) {
        while(se != null) {

            System.out.print("SQLException: State:   " + se.getSQLState());
            System.out.println("Severity: " + se.getErrorCode());
            System.out.println(se.getMessage());            
            
            se = se.getNextException();
        }
    }

}
