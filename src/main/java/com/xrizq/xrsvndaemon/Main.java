/*
 *  .
 *  Copyright (C) 2016 mrhezqhe@gmail.com
 *  All rights reserved.
 */
package com.xrizq.xrsvndaemon;

import com.xrizq.xrsvndaemon.util.AccessUtil;
import com.xrizq.xrsvndaemon.util.InitUtil;
import com.xrizq.xrsvndaemon.util.SVNDelegate;
import org.apache.commons.io.IOUtils;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.xrizq.xrsvndaemon.util.WriterUtil.listEntries;
import static com.xrizq.xrsvndaemon.util.WriterUtil.writeFiles;

/**
 *
 * @author mrhezqhe@gmail.com
 */
public class Main
{

    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Main.class);
    
    private static final InitUtil prop = InitUtil.getInstance();
    
    public static void main(String[] args) {

        log.info("XSVNDaemon start...");
        
        //date range parameters
        String strDateStart = args[0];
        String strDateEnd = args[1];
        Date startDate = null;
        Date endDate = null;
        
        try {
            DateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
            //start minus 1 day, end plus 1 day
            if (strDateStart !=null){
                startDate = format.parse(strDateStart);
                Calendar calStart = Calendar.getInstance();
                calStart.setTime(startDate);
                calStart.add(Calendar.DATE, -1); //minus number would decrement the days
                startDate = calStart.getTime();
            }
            if (strDateEnd !=null){
                endDate = format.parse(strDateEnd);
                Calendar calEnd = Calendar.getInstance();
                calEnd.setTime(endDate);
                calEnd.add(Calendar.DATE, 1); //add number would increment the days
                endDate = calEnd.getTime();
            }
        } catch (ParseException ex){
            log.error(ex);
        }
        
        
        //admin credential to collect from repositories
        String nameRepo = prop.load().getProperty("userrepo");
        String passwordRepo = prop.load().getProperty("passrepo");
        
        //available repositories
        List<String> repoPaths = new ArrayList<>();
        String[] availRepos = prop.load().getProperty("available_repositories").split(";");
        repoPaths = new ArrayList<>(Arrays.asList(availRepos));
        //destination repository
        String destRepoUrl = prop.load().getProperty("destination_repository");

        HashMap<String, String> fileNameMap = new HashMap<>();
        SVNRepository repository = null;
        
        for (String urlRepo : repoPaths){
            
            String url = urlRepo.split("#")[0];
            String fileName = urlRepo.split("#")[1];
            long startRevision = 0;
            long endRevision = -1; //HEAD (the latest) revision
            repository = AccessUtil.setupRepoAccess(url, nameRepo, passwordRepo);
            Collection logEntries = null;
            
            String currDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String outputFileName = "D:\\outputsvnbulk\\"+fileName+currDate+".txt";
            
            try {

                System.out.println( "Repository Root: " + repository.getRepositoryRoot( true ) );
                System.out.println(  "Repository UUID: " + repository.getRepositoryUUID( true ) );
                listEntries(repository, "");

                long latestRevision = repository.getLatestRevision( );
                System.out.println( "Repository latest revision: " + latestRevision );

                logEntries = repository.log( new String[] { "" } , null , startRevision , endRevision , true , true );
                
  
                for ( Iterator entries = logEntries.iterator( ); entries.hasNext( ); ) {
                    SVNLogEntry logEntry = ( SVNLogEntry ) entries.next( );
                    
                    System.out.println( "---------------------------------------------" );
                    System.out.println ("revision: " + logEntry.getRevision( ) );
                    System.out.println( "author: " + logEntry.getAuthor( ) );
                    System.out.println( "date: " + logEntry.getDate() );
                    System.out.println( "commit log message: " + logEntry.getMessage( ) );
                    
                    //check date range
                    if (startDate !=null && endDate !=null){
                        //get revision date from startDate to endDate
                        if(startDate.compareTo(logEntry.getDate())>0 && endDate.compareTo(logEntry.getDate())<0){
                            //create output file
                            writeFiles(logEntry.getRevision(), logEntry.getAuthor(), new SimpleDateFormat("YYYY-MM-DD hh:mm:ss").format(logEntry.getDate()), logEntry.getMessage(), outputFileName);
                        }
                    } else if (startDate != null && endDate ==null){
                        //get revision date from startDate to latest rev
                        if(startDate.compareTo(logEntry.getDate())>0){
                            //create output file
                            writeFiles(logEntry.getRevision(), logEntry.getAuthor(), new SimpleDateFormat("YYYY-MM-DD hh:mm:ss").format(logEntry.getDate()), logEntry.getMessage(), outputFileName);
                        }
                    } 
                    else if (startDate == null && endDate !=null){
                        //get revision date before endDate until early revision
                        if(endDate.compareTo(logEntry.getDate())<0){
                            //create output file
                            writeFiles(logEntry.getRevision(), logEntry.getAuthor(), new SimpleDateFormat("YYYY-MM-DD hh:mm:ss").format(logEntry.getDate()), logEntry.getMessage(), outputFileName);
                        }
                    } else {
                        //get all revision
                        //create output file
                        writeFiles(logEntry.getRevision(), logEntry.getAuthor(), new SimpleDateFormat("YYYY-MM-DD hh:mm:ss").format(logEntry.getDate()), logEntry.getMessage(), outputFileName);
                    }
 
                    fileNameMap.put(outputFileName, fileName+currDate+".txt");

                    if (logEntry.getChangedPaths( ).size( ) > 0 ) {
                       System.out.println( );
                       System.out.println( "changed paths:" );
                       Set changedPathsSet = logEntry.getChangedPaths( ).keySet( );

                       for ( Iterator changedPaths = changedPathsSet.iterator( ); changedPaths.hasNext( ); ) {
                           SVNLogEntryPath entryPath = ( SVNLogEntryPath ) logEntry.getChangedPaths( ).get( changedPaths.next( ) );
                           System.out.println( " "
                                   + entryPath.getType( )
                                   + " "
                                   + entryPath.getPath( )
                                   + ( ( entryPath.getCopyPath( ) != null ) ? " (from "
                                           + entryPath.getCopyPath( ) + " revision "
                                           + entryPath.getCopyRevision( ) + ")" : "" ) );
                       }
                   }

       
                }
               
            } catch ( SVNException e ) {
               e.printStackTrace();
               log.error(e);
            }
        }
        
        
        ISVNEditor editor = null;
        SVNCommitInfo commitInfo = null;
        
        if (fileNameMap !=null && fileNameMap.size() > 0){
            repository = AccessUtil.setupRepoAccess(destRepoUrl, nameRepo, passwordRepo);
            for (Map.Entry<String, String> entry : fileNameMap.entrySet()) {
                FileInputStream fis = null;
                File file = null;
                try { 
                    //commit files
                    file = new File(entry.getKey());
                    byte[] contents = null;
                    byte[] modifiedContents = null;
                    fis = new FileInputStream(file);
                    contents = IOUtils.toByteArray(fis);
                    
                    //check wether file is exist or not
                    boolean isFileExist = false;
                    boolean isDirExist = false;
                    Collection entries = repository.getDir("", -1 , null , (Collection) null );
                    Iterator iterator = entries.iterator( );
                    while (iterator.hasNext() ) {
                        SVNDirEntry sVNDirEntry = (SVNDirEntry) iterator.next( );
                        //check for the same name
                        if(sVNDirEntry.getName().equalsIgnoreCase(entry.getValue())){
                            if(sVNDirEntry.getKind() == SVNNodeKind.NONE){
                                System.out.println( "repository is empty");
                                break;
                            } else if (sVNDirEntry.getKind() == SVNNodeKind.DIR) {
                                System.out.println( "The entry is a directory." );
                                isDirExist = true;
                                break;
                            } else if (sVNDirEntry.getKind() == SVNNodeKind.FILE){
                                System.out.println( "The entry is a file." );
                                System.out.println( "Got a file." +sVNDirEntry.getName());
                                isFileExist = true;
                                modifiedContents = contents;
                                break;
                            }
                        }
                    }

                    if (isFileExist){
                        editor = repository.getCommitEditor( "file contents changed" , null );
                        commitInfo = SVNDelegate.modifyFile(editor, null, entry.getValue(), contents , modifiedContents);
                        log.info( "The file was changed: " + commitInfo );
                    } else {
                        editor = repository.getCommitEditor("file added " , null);
                        commitInfo = SVNDelegate.addFile(editor, entry.getValue(), contents);
                        log.info("The file was added in destination repository: " + commitInfo );
                    }
                    
                    
                }catch (IOException e) {
                    e.printStackTrace();
                }catch (SVNException ex) {
                    log.error(ex);
                }finally {
                    try {
                        if (fis != null){
                            fis.close();
                            if(file.delete()){
                                log.info("Local file ("+ entry.getKey() +") was deleted");
                            }
                        }
                    }catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        
    }
    
    
}