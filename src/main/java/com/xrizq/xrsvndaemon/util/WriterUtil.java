/*
 *  .
 *  Copyright (C) 2016 mrhezqhe@gmail.com
 *  All rights reserved.
 */
package com.xrizq.xrsvndaemon.util;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.io.SVNRepository;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author mrhezqhe@gmail.com
 */
public class WriterUtil
{
    
    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WriterUtil.class);
    
    public static boolean writeFiles(long revision, String author, String date, String message, String outputFilePath){
        boolean writeNow = false;
        try {
            FileWriter writer = new FileWriter(outputFilePath, true);
            try (BufferedWriter bufferedWriter = new BufferedWriter(writer))
            {
                if (message != null){
                    StringBuilder spaces = new StringBuilder();
                    spaces.append(revision).append(" > ").append(author).append(" > ").append(date).append(" > ").append(message);
                    bufferedWriter.write(spaces.toString());
                    writeNow = true;
                }
                if(writeNow){
                    bufferedWriter.newLine();
                }   
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writeNow;
    }
    
    public static void listEntries( SVNRepository repository, String path ) throws SVNException {
        Collection entries = repository.getDir( path, -1 , null , (Collection) null );
            Iterator iterator = entries.iterator( );
            while (iterator.hasNext()) {
                SVNDirEntry entry = (SVNDirEntry ) iterator.next( );
                System.out.println( "/" + (path.equals( "" ) ? "" : path + "/" ) + entry.getName( ) + 
                                   " ( author: '" + entry.getAuthor( ) + "'; revision: " + entry.getRevision( ) + 
                                   "; date: " + entry.getDate( ) + ")" );
                if ( entry.getKind() == SVNNodeKind.DIR ) {
                   listEntries( repository, ( path.equals( "" ) ) ? entry.getName( ) : path + "/" + entry.getName( ) );
                }
            }
    }
    
}