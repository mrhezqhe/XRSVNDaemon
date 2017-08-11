/*
 *  .
 *  Copyright (C) 2016 mrhezqhe@gmail.com
 *  All rights reserved.
 */
package com.xrizq.xrsvndaemon;

import com.xrizq.xrsvndaemon.util.SVNDelegate;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mrhezqhe@gmail.com
 */
public class MainCommit
{

    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MainCommit.class);

    public static void main(String[] args) {

        try {
            
            log.info("XSVNDaemon MainCommit start...");
            
            FSRepositoryFactory.setup( );
            SVNURL url = SVNURL.parseURIDecoded("https://domain.com/svn/testbrancha");
            String userName = "repoadmin";
            String userPassword = "repopass";
            
            byte[] contents = "This is a new file".getBytes( );
            byte[] modifiedContents = "This is the same file but modified a little.".getBytes( );
            
            SVNRepository repository = SVNRepositoryFactory.create( url );
            
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( userName, userPassword );
            repository.setAuthenticationManager( authManager );
            
            SVNNodeKind nodeKind = repository.checkPath("", -1);
            
            if (nodeKind == SVNNodeKind.NONE) {
                System.out.println( "No entry at URL " + url );
                System.exit(1);
            } else if (nodeKind == SVNNodeKind.FILE ) {
                System.out.println( "Entry at URL " + url + " is a file while directory was expected" );
                System.exit(1);
            }
            
            //Get exact value of the latest (HEAD) revision.
            long latestRevision = repository.getLatestRevision();
            System.out.println("Repository latest revision (before committing): " + latestRevision);

            ISVNEditor editor = null;
            SVNCommitInfo commitInfo = null;
            
//            editor = repository.getCommitEditor( "directory and file added" , null );
//            try {
//                commitInfo = SVNDelegate.addDir( editor , "test" , "test/file.txt" , contents );
//                System.out.println( "The directory was added: " + commitInfo );
//            } catch ( SVNException svne ) {
//                editor.abortEdit();
//                throw svne;
//            }
            
//            editor = repository.getCommitEditor( "file contents changed" , null );
//            try {
//                commitInfo = SVNDelegate.modifyFile( editor , "test" , "test/file.txt" , contents , modifiedContents );
//                System.out.println( "The file was changed: " + commitInfo );
//            } catch ( SVNException svne ) {
//                editor.abortEdit( );
//                throw svne;
//            }
            
            //converts a relative path to an absolute one
//            String absoluteSrcPath = repository.getRepositoryPath( "test" );
//            long srcRevision = repository.getLatestRevision( );
            
//            editor = repository.getCommitEditor( "directory copied" , null );
//            try {
//                commitInfo = SVNDelegate.copyDir( editor , absoluteSrcPath , "test2" , srcRevision );
//                System.out.println( "The directory was copied: " + commitInfo );
//            } catch ( SVNException svne ) {
//                editor.abortEdit( );
//                throw svne;
//            }
            
           
            //Delete directory "test".
            editor = repository.getCommitEditor( "directory deleted" , null );
            try {
                commitInfo = SVNDelegate.deleteDir( editor , "test" );
                System.out.println( "The directory was deleted: " + commitInfo );
            } catch ( SVNException svne ) {
                editor.abortEdit( );
                throw svne;
            }
            
            //Delete directory "test2".
            editor = repository.getCommitEditor( "copied directory deleted" , null );
            try {
                commitInfo = SVNDelegate.deleteDir( editor , "test2" );
                System.out.println( "The copied directory was deleted: " + commitInfo );
            } catch ( SVNException svne ) {
                editor.abortEdit( );
                throw svne;
            }
            
            
            latestRevision = repository.getLatestRevision( );
            System.out.println( "Repository latest revision (after committing): " + latestRevision );
            
            
        } catch ( SVNException ex ) {
            Logger.getLogger(MainCommit.class.getName()).log(Level.SEVERE, null, ex);
        }
           
       
 
        
    }
    
    
}