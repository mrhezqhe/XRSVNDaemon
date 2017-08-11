/*
 *  .
 *  Copyright (C) 2016 mrhezqhe@gmail.com
 *  All rights reserved.
 */
package com.xrizq.xrsvndaemon;

import com.xrizq.xrsvndaemon.util.AccessUtil;
import com.xrizq.xrsvndaemon.util.CommitMediator;
import com.xrizq.xrsvndaemon.util.InitUtil;
import com.xrizq.xrsvndaemon.util.SVNDelegate;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.SvnLog;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnRevisionRange;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.xrizq.xrsvndaemon.util.WriterUtil.writeFiles;

/**
 *
 * @author mrhezqhe@gmail.com
 */
public class MainLog 
{

    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MainLog.class);
    
    private static final InitUtil prop = InitUtil.getInstance();
    private static final DateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
    
    public static void main(String[] args) {

        log.info("XSVNDaemon start main log...");
        
        //date range parameters
        String strDateStart = args[0];
        String strDateEnd = args[1];
        Date startDate = null;
        Date endDate = null;
        
        try {
            
            //start minus 1 day, end plus 1 day
            if (strDateStart !=null && strDateStart.length() > 0){
                startDate = format.parse(strDateStart);
                Calendar calStart = Calendar.getInstance();
                calStart.setTime(startDate);
                calStart.add(Calendar.DATE, -1); //minus number would decrement the days
                startDate = calStart.getTime();
            }
            if (strDateEnd !=null && strDateEnd.length() > 0){
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
        String tempLocalDir = prop.load().getProperty("temp_local_dir");

        HashMap<String, String> fileNameMap = new HashMap<>();
        SVNRepository repository = null;
        
        for (String urlRepo : repoPaths){
            
            String url = urlRepo.split("#")[0];
            String fileName = urlRepo.split("#")[1];
            long startRevision = 0;
            long endRevision = -1; //HEAD (the latest) revision
            repository = AccessUtil.setupRepoAccess(url, nameRepo, passwordRepo);
            
            SvnOperationFactory operationFactory = new SvnOperationFactory();
            operationFactory.setAuthenticationManager(repository.getAuthenticationManager());
            
            String currDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String outputFileName = tempLocalDir+fileName+currDate+".txt";
            
            try {

                System.out.println( "Repository Root: " + repository.getRepositoryRoot( true ) );
                System.out.println(  "Repository UUID: " + repository.getRepositoryUUID( true ) );
                
                //print list entries
//                listEntries(repository, "");
//
                long latestRev = repository.getLatestRevision();
                System.out.println( "Repository latest revision: " + latestRev );

                //Fetching log from SVNServer
                SvnLog svnLog = operationFactory.createLog();

                //check date range
                if (startDate !=null && endDate !=null){
                    //get revision date from startDate to endDate
                    svnLog.addRange(SvnRevisionRange.create(SVNRevision.create(startDate), SVNRevision.create(endDate)));
                } else if (startDate != null && endDate ==null){
                    long startRev = repository.getDatedRevision(startDate) + 1;
                    if (SVNRevision.isValidRevisionNumber(startRev)){
                        svnLog.addRange(SvnRevisionRange.create(SVNRevision.create(startRev), SVNRevision.create(latestRev)));
                    }
                } else if (startDate == null && endDate !=null){
                    //get revision date before endDate until early revision
                    long endRev = repository.getDatedRevision(endDate);
                    if (SVNRevision.isValidRevisionNumber(endRev)){
                        svnLog.addRange(SvnRevisionRange.create(SVNRevision.create(0), SVNRevision.create(endRev)));
                    }
                } else {
                    svnLog.addRange(SvnRevisionRange.create(SVNRevision.create(0), SVNRevision.create(latestRev)));
                }
                svnLog.setDiscoverChangedPaths(true);
                svnLog.setSingleTarget(SvnTarget.fromURL(SVNURL.parseURIEncoded(url)));
                List<SVNLogEntry> logEntries = SVNDelegate.getEntryLogs(svnLog);

                for (SVNLogEntry logEntry : logEntries) {
                    //write files to local temporary dir
                    writeFiles(logEntry.getRevision(), logEntry.getAuthor(), new SimpleDateFormat("YYYY-MM-dd hh:mm:ss").format(logEntry.getDate()), logEntry.getMessage(), outputFileName);
                    //assign localname and commit file name
                    fileNameMap.put(outputFileName, fileName+currDate+".txt");
                    //print out changed path
                    SVNDelegate.getChangedPath(logEntry);
                }
            } catch ( SVNException e ) {
               e.printStackTrace();
               log.error(e);
            }
        }
        
        //commit
        CommitMediator.doCommit(repository, fileNameMap, destRepoUrl, nameRepo, passwordRepo);
        
//        
//        ISVNEditor editor = null;
//        SVNCommitInfo commitInfo = null;
//        
//        if (fileNameMap !=null && fileNameMap.size() > 0){
//            repository = AccessUtil.setupRepoAccess(destRepoUrl, nameRepo, passwordRepo);
//            for (Map.Entry<String, String> entry : fileNameMap.entrySet()) {
//                FileInputStream fis = null;
//                File file = null;
//                try { 
//                    //commit files
//                    file = new File(entry.getKey());
//                    byte[] contents = null;
//                    byte[] modifiedContents = null;
//                    fis = new FileInputStream(file);
//                    contents = IOUtils.toByteArray(fis);
//                    
//                    //check wether file is exist or not
//                    boolean isFileExist = false;
//                    boolean isDirExist = false;
//                    Collection entries = repository.getDir("", -1 , null , (Collection) null );
//                    Iterator iterator = entries.iterator( );
//                    while (iterator.hasNext() ) {
//                        SVNDirEntry sVNDirEntry = (SVNDirEntry) iterator.next( );
//                        //check for the same name
//                        if(sVNDirEntry.getName().equalsIgnoreCase(entry.getValue())){
//                            if(sVNDirEntry.getKind() == SVNNodeKind.NONE){
//                                System.out.println( "repository is empty");
//                                break;
//                            } else if (sVNDirEntry.getKind() == SVNNodeKind.DIR) {
//                                System.out.println( "The entry is a directory." );
//                                isDirExist = true;
//                                break;
//                            } else if (sVNDirEntry.getKind() == SVNNodeKind.FILE){
//                                System.out.println( "The entry is a file." );
//                                System.out.println( "Got a file." +sVNDirEntry.getName());
//                                isFileExist = true;
//                                modifiedContents = contents;
//                                break;
//                            }
//                        }
//                    }
//
//                    if (isFileExist){
//                        editor = repository.getCommitEditor( "file contents changed" , null );
//                        commitInfo = SVNDelegate.modifyFile(editor, null, entry.getValue(), contents , modifiedContents);
//                        log.info( "The file was changed: " + commitInfo );
//                    } else {
//                        editor = repository.getCommitEditor("file added " , null);
//                        commitInfo = SVNDelegate.addFile(editor, entry.getValue(), contents);
//                        log.info("The file was added in destination repository: " + commitInfo );
//                    }
//                    
//                    
//                }catch (IOException e) {
//                    e.printStackTrace();
//                }catch (SVNException ex) {
//                    log.error(ex);
//                }finally {
//                    try {
//                        if (fis != null){
//                            fis.close();
//                            if(file.delete()){
//                                log.info("Local file ("+ entry.getKey() +") was deleted");
//                            }
//                        }
//                    }catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        }
        
    }
    
    
    
    
}