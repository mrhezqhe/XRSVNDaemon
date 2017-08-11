package com.xrizq.xrsvndaemon.util;

import com.xrizq.xrsvndaemon.common.XSVNConstants;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc2.ISvnObjectReceiver;
import org.tmatesoft.svn.core.wc2.SvnLog;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.*;

/**
 *
 * @author mrhezqhe@gmail.com
 */


public class SVNDelegate implements XSVNConstants
{
    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SVNDelegate.class);
   
    public static SVNCommitInfo addFile(ISVNEditor editor, String filePath , byte[] data) throws SVNException {
        editor.openRoot( -1);
        editor.addFile(filePath , null , -1);
        editor.applyTextDelta(filePath , null);

        SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator( );
        String checksum = deltaGenerator.sendDelta(filePath , new ByteArrayInputStream(data), editor , true );
        editor.closeFile(filePath, checksum);
        //Closes dirPath.
        editor.closeDir();
        return editor.closeEdit();
    }
    
    public static SVNCommitInfo delFile(ISVNEditor editor, String filePath, long revision) throws SVNException {
        editor.openRoot( -1);
        editor.deleteEntry(filePath, revision);
        //Closes dirPath.
        editor.closeDir();
        return editor.closeEdit();
    }
     
    public static SVNCommitInfo addDir(ISVNEditor editor , String dirPath) throws SVNException {
        editor.openRoot( -1);
        editor.addDir(dirPath , null , -1);
        //Closes the root directory.
        editor.closeDir();
        return editor.closeEdit();
    }
    
    public static SVNCommitInfo addDirAndFile(ISVNEditor editor , String dirPath , String filePath , byte[] data) throws SVNException {
        editor.openRoot( -1);
        editor.addDir(dirPath , null , -1);
        editor.addFile(filePath , null , -1);
        editor.applyTextDelta(filePath , null);

        SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator( );
        String checksum = deltaGenerator.sendDelta(filePath , new ByteArrayInputStream(data), editor , true);
        editor.closeFile(filePath, checksum);
        //Closes dirPath.
        editor.closeDir();
        //Closes the root directory.
        editor.closeDir();
        return editor.closeEdit();
    }

    public static SVNCommitInfo modifyFile(ISVNEditor editor , String dirPath , String filePath , byte[] oldData , byte[] newData) throws SVNException {
        editor.openRoot(-1);
        if(dirPath !=null){
            editor.openDir(dirPath , -1);
        }        
        editor.openFile(filePath , -1);
        editor.applyTextDelta(filePath , null);

        SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
        String checksum = deltaGenerator.sendDelta(filePath , new ByteArrayInputStream(oldData) , 0 , new ByteArrayInputStream(newData) , editor , true);
        //Closes filePath.
        editor.closeFile(filePath , checksum);
        if(dirPath !=null){
            // Closes dirPath.
            editor.closeDir();
        }
        //Closes the root directory.
        editor.closeDir();
        return editor.closeEdit();
    }

    public static SVNCommitInfo copyDir(ISVNEditor editor , String srcDirPath , String dstDirPath , long revision) throws SVNException {
        editor.openRoot(-1);
        editor.addDir(dstDirPath , srcDirPath , revision);
        //Closes dstDirPath.
        editor.closeDir();
        //Closes the root directory.
        editor.closeDir();
        return editor.closeEdit();
   }

    public static SVNCommitInfo deleteDir(ISVNEditor editor , String dirPath) throws SVNException {
        editor.openRoot(-1);
        editor.deleteEntry( dirPath , -1);
        //Closes the root directory.
        editor.closeDir();
        return editor.closeEdit();
    }
    
    public static List<SVNLogEntry> getEntryLogs(SvnLog svnLog){
        final List<SVNLogEntry> sVNLogEntryList = new ArrayList<>();
        svnLog.setReceiver(new ISvnObjectReceiver<SVNLogEntry>() {
        @Override
            public void receive(SvnTarget target, SVNLogEntry logEntry) throws SVNException {
                sVNLogEntryList.add(logEntry);
            }
        });
        try {
            svnLog.run();
        } catch (SVNException ex) {
            log.error(ex);
        }
        return sVNLogEntryList;
    }
    
    public static void getChangedPath(SVNLogEntry logEntry){
         if (logEntry.getChangedPaths( ).size( ) > 0 ) {
            System.out.println( );
            System.out.println( "changed paths:" );
            Set changedPathsSet = logEntry.getChangedPaths( ).keySet( );

            for ( Iterator changedPaths = changedPathsSet.iterator(); changedPaths.hasNext();) {
                SVNLogEntryPath entryPath = ( SVNLogEntryPath ) logEntry.getChangedPaths().get(changedPaths.next());
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
    
    public static Date getDateRevisionByRevisionId(long revisionId, SVNRepository repository){
        Date dateRevision = null;
        try {
            SVNProperties svnProp =  repository.getRevisionProperties(revisionId, null);
            SVNPropertyValue propValue = svnProp.getSVNPropertyValue(SVN_DATE);
            String dateStr = propValue.getString().substring(0, 10);
            dateRevision = FORMAT_DATE.parse(dateStr.replace("-", ""));
            long testRev = repository.getDatedRevision(dateRevision);
            System.out.println("");
        }catch (ParseException | SVNException ex){
            log.error(ex);
        }
        return dateRevision;
    }
    
}
