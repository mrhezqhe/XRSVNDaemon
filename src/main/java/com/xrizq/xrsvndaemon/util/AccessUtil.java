/*
 *  .
 *  Copyright (C) 2016 mrhezqhe@gmail.com
 *  All rights reserved.
 */
package com.xrizq.xrsvndaemon.util;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 *
 * @author mrhezqhe@gmail.com
 */
public class AccessUtil
{
    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AccessUtil.class);
    
    public static SVNRepository setupRepoAccess(String url, String name, String password){
        SVNRepository repository = null;
        try {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url) );
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( name , password );
            repository.setAuthenticationManager(authManager );
        } catch (SVNException ex) {
            log.error(ex);
        }
        return repository;
    }

}