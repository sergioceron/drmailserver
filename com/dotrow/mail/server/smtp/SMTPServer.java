/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server.smtp;
/*
 * SMTPServer.java
 *
 * Created on 13 de junio de 2006, 15:51
 */

import com.dotrow.mail.server.DatabaseConnection;
import com.dotrow.mail.server.Logger;
import com.dotrow.mail.server.MailServerFilter;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Statement;
import java.util.Vector;
/**
 * SMTP Server
 * @author Sergio Ceron Figueroa
 */
public class SMTPServer extends Thread implements DatabaseConnection {
    
    private Logger log = Logger.getInstance();
    private static Statement statement;
    private static int port;
    private static Vector<SMTPCThread> clients = new Vector<SMTPCThread>();
    private MailServerFilter msf = MailServerFilter.getInstance();
    
    /** Creates a new instance of SMTPServer
     * @param port port
     */
    @SuppressWarnings("static-access")
    public SMTPServer(int port) {
        this.port = port;
    }
    
    @Override
    public void run(){
        try{
            ServerSocket server = new ServerSocket( port );
            while(true){
                Socket clientSocket = server.accept();
                SMTPCThread client = new SMTPCThread( this, clientSocket );
                client.start();
                client.setStatement(statement);
                clients.add(client);
                log.debug(client, "Starting Smtp Connection [ " + clientSocket.getInetAddress() + " ]", Logger.Level.INFO);
            }
        }catch(Exception e){
            log.debug(e, Logger.Level.ERROR);
        }
    }
    
    /**
     * Delete Client Thread
     * @param t Client Thread
     */
    public void deleteClient( SMTPCThread t ){
        for(SMTPCThread client : clients){
            if( client.equals(t) ){
                clients.remove(client);
            }
        }
    }
    
    public void setStatement(java.sql.Statement st) {
        statement = st;
    }
    
}
