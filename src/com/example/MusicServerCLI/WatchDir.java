package com.example.MusicServerCLI;
/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;

import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class WatchDir {

    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final boolean recursive;
    private boolean trace = false;
    private String musicFolder = null;
    LibraryManager libraryManager = null;
    private boolean publicServer = false;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    WatchDir(Path dir, boolean recursive, boolean publicServer) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        this.recursive = recursive;
        this.publicServer = publicServer;

        if (recursive) {
            System.out.format("\nWatching folders under %s ...\n", dir);
            registerAll(dir);
        } else {
            register(dir);
        }

        // enable trace after initial registration
        this.trace = true;
        
        libraryManager = new LibraryManager(dir.toString(), publicServer);
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {
        for (;;) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

        	// sleep 250 milliseconds
            // give the file a chance to even "touch"
        	try {
				Thread.sleep(250);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                // print out event
               // System.out.format("%s: %s\n", event.kind().name(), child);
                
                if(kind == ENTRY_CREATE){

                    if(Files.isDirectory(child)){
                        try {
                        	
							Files.walkFileTree(child, new SimpleFileVisitor<Path>() {
							    @Override
							    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
							        throws IOException {
							    	
							        // call addSongFromPath for each file in directory
							    	File[] files = new File(dir.toString()).listFiles();
							    	
							    	for(int i = 0; i < files.length; i++){
							    		libraryManager.addSongFromPath(files[i].getPath());
							    	}
							    	
							        return FileVisitResult.CONTINUE;
							    }
							});
						}catch(IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    }else{
                    	libraryManager.addSongFromPath(child.toString());
                    }
                }else if(kind == ENTRY_MODIFY){
                	
                	 if(Files.isDirectory(child)){
                         try {
                         	
 							Files.walkFileTree(child, new SimpleFileVisitor<Path>() {
 							    @Override
 							    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
 							        throws IOException {
 							    	
 							        // call addSongFromPath for each file in directory
 							    	File[] files = new File(dir.toString()).listFiles();
 							    	
 							    	for(int i = 0; i < files.length; i++){
 							    		libraryManager.modifySongFromPath(files[i].getPath());
 							    	}
 							    	
 							        return FileVisitResult.CONTINUE;
 							    }
 							});
 						}catch(IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
                     }else{
                     	libraryManager.modifySongFromPath(child.toString());
                     }
                	
                }else if(kind == ENTRY_DELETE){
                	
                	// need to delete everything in database LIKE the folder that was deleted
                	// can't iterate file path because it's been deleted
                	
                	// if a single file, can delete from db as is
                	
                	// Files.isDirectory() is always false because the file can never be a directory
                	// ... it's been deleted
                	
                	// YOU HAVE CHANGED GETPUBLICHOSTNAME TO GETLOCALHOSTNAME
                	// MAKE THIS WORK BOTH WAYS
                	
                	String path = child.toString();
                	
                    if(isDirectoryString(child.toString())){
                    	libraryManager.deleteSongsLikePath(child.toString());
                    }else{
			    		libraryManager.deleteSongFromPath(child.toString());
                    }
                }

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    static void usage() {
        System.err.println("usage: java WatchDir [-r] dir");
        System.exit(-1);
    }
    
    public static boolean isDirectoryString(String dir){
    	
    	//String[] dirSplit = dir.split("\\.");
    	
    	String[] dirSplitNix = dir.split("\\/");
    	String[] dirSplitNixDos = dirSplitNix[dirSplitNix.length - 1].split("\\\\");
    	String last = dirSplitNixDos[dirSplitNixDos.length - 1];
    	
    	if(last.split("\\.").length > 1){
    		return false;
    	}else{
    		return true;
    	}
    }
}
