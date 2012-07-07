package com.dotrow.mail.server;
/*
 * POP3Server.java
 *
 * Created on 18 de junio de 2006, 12:41
 */

import java.sql.*;
import java.util.*;
import java.net.*;

/**
 * POP3 Server
 * @author Sergio Ceron Figueroa
 */
public class POP3Server extends Thread implements DatabaseInterface {
    
    private Logger log = Logger.getInstance();
    static private Statement ivStatement;
    static private int port;
    static private Vector Pop3CThreads = new Vector();
    
    /** Creates a new instance of POP3Server
     * @param _port port
     */
    public POP3Server( int _port ) {
        POP3Server.port = _port;
    }
    
    @Override
    public void run(){
         try{
            ServerSocket pop3server = new ServerSocket( port );
            while(true){
                Socket client = pop3server.accept();
                POP3CThread pop3CThread = new POP3CThread( this, client );
                pop3CThread.start();
                pop3CThread.dbStatement( ivStatement );
                Pop3CThreads.add( pop3CThread );
                log.debugClientThread(pop3CThread  , "Starting Pop 3 Connection [ " + client.getInetAddress() + " ]" ,2);
            }
        }catch(Exception e){
            log.debugServerThread(e, 0);
        }
    }
    
    /**
     * Delete Client Thread
     * @param t The Thread
     */
    public void deleteClient( Thread t ){
        for( int i=0; i<Pop3CThreads.size(); i++ ){
            if(Pop3CThreads.get(i).equals( t ))
                Pop3CThreads.remove(i);
        }
        //System.out.println(CThreads.size());
    }
    
    public void dbStatement(Statement st) {
        ivStatement = st;
    }
    
}
