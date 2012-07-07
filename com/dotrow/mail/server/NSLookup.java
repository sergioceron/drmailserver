package com.dotrow.mail.server;
/*
 * NSLookup.java
 *
 * Created on 13 de junio de 2006, 14:01
 */

import java.util.*;
import java.io.*;

/**
 * Excute a cmd command nslookup with server client to get a vector of mail server smtp
 * nslookup -type=MX smtp.server.com
 * @author Sergio Ceron Figueroa
 */
public class NSLookup {
    static private NSLookup _instance = null;
    
    /** Creates a new instance of NSLookup */
    private NSLookup() { }
    
    /**
     * Get a active instanse of NSLookup
     * @return instance of NSLookup if exist, if not create new
     */
    final static public NSLookup getInstance(){
        return _instance==null ? new NSLookup() : _instance;
    }
    
    /**
     * Mx host of client smtp
     * @return Vector of mx host
     * @param host Server smtp to get mx hosts
     */
    public Vector getMXHosts( String host){
        Vector MXHosts = new Vector();
        try{
            // Create a new process of cmd
            Process p = Runtime.getRuntime().exec("nslookup -type=MX " + host);
            String line;
            BufferedReader input = new BufferedReader
            (new InputStreamReader(p.getInputStream()));
            // read line and parse to get each mx host
            while ((line = input.readLine()) != null) {
                if(line.startsWith( host )){
                    MXHosts.add( line.substring(line.indexOf("mail exchanger = ") + 17  ).trim() );
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return MXHosts;
    }
    
    public String reverseLookup( String ip ){
        String line = null;
        try{
            // Create a new process of cmd
            String os = System.getProperty( "os.name" );
            String command = "";
            if( os.indexOf( "Windows" ) != 0 ){
                command = "ping -a ";
            }else
            if( os.indexOf( "Linux" ) != 0 ){
                command = "dnsname ";
            }else{
                
            }
            Process p = Runtime.getRuntime().exec( command + ip);
            BufferedReader input = new BufferedReader
            (new InputStreamReader(p.getInputStream()));
            // read line and parse to get each mx host
            line = input.readLine();
        }catch(Exception e){
            System.out.println(e);
        }
        return line;
    }
}
