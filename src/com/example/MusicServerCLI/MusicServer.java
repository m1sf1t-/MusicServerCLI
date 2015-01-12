package com.example.MusicServerCLI;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class MusicServer {

	static Scanner scanner;
	
	public static void main(String[] args) throws Exception{
		
		scanner = new Scanner(System.in);
		
		if(args != null && args.length == 1){
			if(args[0].equals("-p")){
				createJson();
			}else if(args[0].equals("-s")){
				startServer();
			}else{
				showHelp();
			}
		}else{
			showHelp();
		}
	}
	
	public static void showHelp(){
		System.out.println("usage:");
		System.out.println("-p: create a JSON music library");
		System.out.println("-s: start the server");
	}
	
	public static void createJson(){
		
		System.out.println("Create JSON Library");
		System.out.println("-------------------\n");
		System.out.println("Please enter the path to your music folder:");
		String musicFolder = scanner.nextLine();
		
		createMusicLibraryFolder(musicFolder);
		
		LibraryManager libManager = new LibraryManager(musicFolder);
		libManager.makeLibrary();
	}
	
	public static void startServer() throws Exception{
		
		System.out.println("\nStart Server");
		System.out.println("------------\n");
		System.out.println("Be sure that you have indexed the music library first, \notherwise your URL will 404.\n");
		System.out.println("Please enter the path to your music folder:");
		String musicFolder = scanner.nextLine();
		//String musicFolder = "/home/kyle/Music/TestMusic/";
		System.out.println("\nStarting server at '" + musicFolder + "'");
		
		createMusicLibraryFolder(musicFolder);
		
		//LibraryManager libManager = new LibraryManager(musicFolder);
		//libManager.makeLibrary();
		
		System.out.println("This folder will now be available at http://" + InetAddress.getLocalHost().getHostName() + ":4231/" );
		System.out.println("\nURL to use: http://" + InetAddress.getLocalHost().getHostName() + ":4231/.LibraryIndex/MusicIndex.json\n");
		
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
        new WatchDir(dir, true).processEvents();
		
	}
	
	public static void createMusicLibraryFolder(String musicFolder){
		// if ./MusicLibrary doesn't exist
		Path path = Paths.get(musicFolder + "/.LibraryIndex/");
		if(!Files.exists(path)){
			boolean success = new File(musicFolder + "/.LibraryIndex/").mkdirs();
		}
	}
	
}
