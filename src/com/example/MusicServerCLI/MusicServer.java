package com.example.MusicServerCLI;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class MusicServer {

	static Scanner scanner;
	static boolean publicServer;
	
	public static void main(String[] args) throws Exception{
		
		System.out.println();
		
		Logger.getLogger("org.eclipse.jetty.*").setLevel(Level.OFF);
		scanner = new Scanner(System.in);
		
		if(args != null && args.length == 1){
			
			if(args[0].equals("-p")){
				
				String musicFolder = getServerFolder();
				publicServer = blnGetPublic();
				
				createMusicLibraryFolder(musicFolder);
				
				LibraryManager libManager = new LibraryManager(musicFolder, publicServer);
				libManager.makeLibrary();
				
			}else if(args[0].equals("-s")){
				
				System.out.println("\nStart Server");
				System.out.println("------------\n");
				System.out.println("Be sure that you have indexed the music library first, \notherwise your URL will 404.\n");
				
				String musicFolder = getServerFolder();
				
				boolean publicServer = blnGetPublic();
				
				createMusicLibraryFolder(musicFolder);
				
				/*LibraryManager libManager = new LibraryManager(musicFolder, publicServer);
				libManager.makeLibrary();*/

				System.out.println("\nStarting server at " + musicFolder + "\n");
				startServer(musicFolder, publicServer);
				
			}else{
				showHelp();
			}
		}else{
			showHelp();
		}
	}
	
	public static void showHelp(){
		System.out.println("Usage\n");
		System.out.println("-p: Create a JSON music library.");
		System.out.println("-s: Start the server.");
	}
	
	public static void createJson(){
		
		System.out.println("Create JSON Library");
		System.out.println("-------------------\n");
		String musicFolder = getServerFolder();
		
		createMusicLibraryFolder(musicFolder);
		
		LibraryManager libManager = new LibraryManager(musicFolder, publicServer);
		libManager.makeLibrary();
	}
	
	public static String getServerFolder(){
		System.out.println("Please enter the path to your music folder:");
		String musicFolder = scanner.nextLine();
		//String musicFolder = "/home/kyle/Music/TestMusic/";
		return musicFolder;
	}
	
	public static boolean blnGetPublic(){
		
		System.out.println("Use public or local hostname? (P/L)");
		String publicServerString = scanner.nextLine();
		
		if(publicServerString.equals("P") || publicServerString.equals("p")){
			return true;
		}else if(publicServerString.equals("L") || publicServerString.equals("l")){
			return false;
		}else{
			System.out.println("invalid entry, quitting...");
			System.exit(1);
		}
		
		return false;
	}
	
	public static void startServer(String musicFolder, boolean publicServer) throws Exception{
		
		String hostName = "";
		
		if(publicServer){
			hostName = LibraryManager.getPublicHostname();
		}else{
			hostName = LibraryManager.getLocalHostname();
		}
		
		System.out.println("Server address: http://" + hostName + ":4231/" );
		System.out.println("\nLibrary link: http://" + hostName + ":4231/.LibraryIndex/MusicIndex\n");
		
	    Server server = new Server(4231);
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setWelcomeFiles(new String[]{"index.html"});
		resourceHandler.setResourceBase(musicFolder);
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[]{resourceHandler, new DefaultHandler()});
		server.setHandler(handlers);
		server.start();
		//server.join();
		
        // register directory and process its events
        Path dir = Paths.get(musicFolder);
        new WatchDir(dir, true, publicServer).processEvents();
		
	}
	
	public static void createMusicLibraryFolder(String musicFolder){
		// if ./MusicLibrary doesn't exist
		Path path = Paths.get(musicFolder + "/.LibraryIndex/");
		if(!Files.exists(path)){
			boolean success = new File(musicFolder + "/.LibraryIndex/").mkdirs();
		}
	}
	
}
