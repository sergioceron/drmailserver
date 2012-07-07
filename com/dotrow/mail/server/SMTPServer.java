package com.dotrow.mail.server;
/*
 * SMTPServer.java
 *
 * Created on 13 de junio de 2006, 15:51
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
/**
 * SMTP Server
 * @author Sergio Ceron Figueroa
 */
public class SMTPServer extends Thread implements DatabaseInterface{
    
    private Logger log = Logger.getInstance();
    private static Statement ivStatement;
    private static int port;
    private static Vector CThreads = new Vector();
    private MailServerFilter msf = MailServerFilter.getInstance();
    
    /** Creates a new instance of SMTPServer
     * @param _port port
     */
    @SuppressWarnings("static-access")
    public SMTPServer(int _port) {
        this.port = _port;
    }
    
    @Override
    public void run(){
        try{
            ServerSocket server = new ServerSocket( port );
            while(true){
                Socket client = server.accept();
                // if filter is enabled and disallow recive mail
                //if ( !msf.allowReciveMailbyIp( client.getInetAddress().getHostAddress() ) && msf.enabled() ){
                /*if ( !msf.allowReciveMailbyIp( client.getInetAddress().getHostAddress() ) ){
                    debug.SDebug( "Cant start connection : ip banned", 2 );
                    client.close();
                }else{*/
                    SMTPCThread CThread = new SMTPCThread( this, client );
                    CThread.start();
                    CThread.dbStatement( ivStatement );
                    CThreads.add( CThread );
                    log.debugClientThread(CThread  , "Starting Smtp Connection [ " + client.getInetAddress() + " ]" ,2);
                    
                //}
            }
        }catch(Exception e){
            log.debugServerThread(e, 0);
        }
    }
    
    /**
     * Delete Client Thread
     * @param t Client Thread
     */
    public void deleteClient( Thread t ){
        for(int i=0;i<CThreads.size();i++){
            if(CThreads.get(i).equals( t ))
                CThreads.remove(i);
        }
    }
    
    public void dbStatement(java.sql.Statement st) {
        ivStatement = st;
    }
    
}
