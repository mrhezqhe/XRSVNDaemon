package com.xrizq.xrsvndaemon.util;

import com.xrizq.xrsvndaemon.common.XSVNConstants;
import org.apache.commons.io.IOUtils;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author mrhezqhe@gmail.com
 */


public class CommitMediator implements XSVNConstants {
    
    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CommitMediator.class);
    
    public static void doCommit(SVNRepository repository, HashMap<String, String> fileNameMap, String destRepoUrl, String nameRepo, String passwordRepo){
        ISVNEditor editor = null;
        SVNCommitInfo commitInfo = null;
        
        if (fileNameMap !=null && fileNameMap.size() > 0){
            repository = AccessUtil.setupRepoAccess(destRepoUrl, nameRepo, passwordRepo);
            for (Map.Entry<String, String> entry : fileNameMap.entrySet()) {
                FileInputStream fis = null;
                File file = null;
                byte[] contents = null;
                byte[] modifiedContents = null;
                String value = entry.getValue();
                long fileRevision = -1;
                try { 
                    //commit files
                    file = new File(entry.getKey());
                    
                    //local vs remote bytes
                    fis = new FileInputStream(file);
                    
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
                                fileRevision = sVNDirEntry.getRevision();
                                contents = checkRemoteDelta(sVNDirEntry.getName(), repository);
                                break;
                            } else if (sVNDirEntry.getKind() == SVNNodeKind.FILE){
                                System.out.println( "The entry is a file." );
                                System.out.println( "Got a file = ["+sVNDirEntry.getName()+"]");
                                isFileExist = true;
                                fileRevision = sVNDirEntry.getRevision();
                                modifiedContents = IOUtils.toByteArray(fis);
                                contents = checkRemoteDelta(sVNDirEntry.getName(), repository);
                                break;
                            }
                        }
                    }

                    if (isFileExist){
                        editor = repository.getCommitEditor( "file contents changed" , null );
                        commitInfo = SVNDelegate.modifyFile(editor, null, entry.getValue(), contents , modifiedContents);
                        log.info( "The file was changed: " + commitInfo );
                    } else {
                        contents = IOUtils.toByteArray(fis);
                        editor = repository.getCommitEditor("file added " , null);
                        commitInfo = SVNDelegate.addFile(editor, entry.getValue(), contents);
                        log.info("The file was added in destination repository: " + commitInfo );
                    }
                    
                }catch (IOException e) {
                    e.printStackTrace();
                }catch (SVNException ex) {
                    System.out.println(" error code " +ex.getErrorMessage().getErrorCode().toString());
                    String errCode = ex.getErrorMessage().getErrorCode().toString().replaceAll("[^\\d]", "");
                    if(errCode.equalsIgnoreCase(SVN_ERROR_CHECKSUM_MISSMATCH)){
                        try {
                            repository = AccessUtil.setupRepoAccess(destRepoUrl, nameRepo, passwordRepo);
                            editor = repository.getCommitEditor("file delete because checksum mismatch " , null);
                            commitInfo = SVNDelegate.delFile(editor, value, fileRevision);
                            log.info( "The file was delete due to checksum mismatch: " + commitInfo );
                            
                            editor = repository.getCommitEditor("file added again " , null);
                            commitInfo = SVNDelegate.addFile(editor, value, contents);
                            log.info( "The file was added again: " + commitInfo );
                        }
                        catch (SVNException ex1) {
                            log.error(ex1);
                        }
                    }
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
    
    public static byte[] checkRemoteDelta(String filename, SVNRepository repository)throws SVNException {
        SVNProperties fileProperties = new SVNProperties();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        repository.getFile(filename, -1, fileProperties, baos);
    return baos.toByteArray();
}
    
}
