package com.xrizq.xrsvndaemon.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 *
 * @author mrhezqhe@gmail.com
 */


public interface XSVNConstants
{
    public static DateFormat FORMAT_DATE = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
    public static final String SVN_LOG = "svn:log";
    public static final String SVN_AUTHOR = "svn:author";
    public static final String SVN_DATE = "svn:date";
    
    public static final String SVN_ERROR_CHECKSUM_MISSMATCH = "200014";
    
    
}
