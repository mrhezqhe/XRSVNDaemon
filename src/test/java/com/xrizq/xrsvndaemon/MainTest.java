package com.xrizq.xrsvndaemon;

import org.junit.*;

/**
 *
 * @author mrhezqhe@gmail.com
 */


public class MainTest
{
    
    public MainTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    @Test
    public void testMain()
    {
        System.out.println("main");
        String[] args = new String[2];
        
        //case 1
        //no parameter defined
        
        //case 2 start date and end date
//        args[0] = "20160729";
//        args[1] = "20160803";
        
        //case 3 start date only
        args[0] = "20160803";
        args[1] = "";
        
        //case 4 end date only
//        args[0] = "";
//        args[1] = "20160729";
        
        
//        Main.main(args);
        MainLog.main(args);
    }
    
}
