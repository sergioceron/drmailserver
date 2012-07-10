/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server.pop3;
/*
 * POP3Server.java
 *
 * Created on 18 de junio de 2006, 12:41
 */

import com.dotrow.mail.server.DatabaseConnection;
import com.dotrow.mail.server.Logger;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Statement;
import java.util.Vector;

/**
 * POP3 Server
 * @author Sergio Ceron Figueroa
 */
public class POP3Server extends Thread implements DatabaseConnection {
    
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
                pop3CThread.setStatement(ivStatement);
                Pop3CThreads.add( pop3CThread );
                log.debug(pop3CThread, "Starting Pop 3 Connection [ " + client.getInetAddress() + " ]", Logger.Level.INFO);
            }
        }catch(Exception e){
            log.debug(e, Logger.Level.ERROR);
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
    
    public void setStatement(Statement st) {
        ivStatement = st;
    }
    
}
