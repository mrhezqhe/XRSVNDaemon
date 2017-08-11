/*
 *  .
 *  Copyright (C) 2016 mrhezqhe@gmail.com
 *  All rights reserved.
 */
package com.xrizq.xrsvndaemon.util;

import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author mrhezqhe@gmail.com
 */
public class InitUtil
{
    
    private static InitUtil initUtil; 
    public static Properties prop = new Properties();
    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InitUtil.class);
    
    public InitUtil() {}
    
    /*
    * As singleton to access instance 
    */
    public static InitUtil getInstance() {    
        if (initUtil == null){  
           initUtil = new InitUtil();  
        }  
        setProperties();    
        return initUtil;  
    }  
    
    /*
    * set properties value xsettlement.properties
    */
    private static void setProperties() {
        try {
            prop.load(InitUtil.class.getResourceAsStream("/svnconfig.properties"));
        } catch (IOException ex) {
            log.error(ex);
        }
    }    
    
    /*
    * load properties file and ready to read one by one
    */
    public Properties load(){
        return prop;
    }
}