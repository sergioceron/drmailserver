/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server.pop3;
/*
 * POP3CThread.java
 *
 * Created on 18 de junio de 2006, 12:44
 */

import com.dotrow.mail.server.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Statement;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * POP3 Client Thread
 * @author Sergio Ceron Figueroa
 */
public class POP3CThread extends Thread implements DatabaseConnection {
    
    /** Instace of debugger */
    private Logger log = Logger.getInstance();
    
     /** Database statement */
    private static Statement ivStatement;
    private Socket client = null;
    private POP3Server pop3s = null;
    private MessageHandler Mh = MessageHandler.getMessageHandler( this );
    private String POP3User = null, POP3Pwd = null;
    private Vector msgs = null;
    private DataInputStream in = null;
    private BufferedReader inLine = null;
    private PrintWriter out = null;
    private Settings set = Settings.getInstance();
    
    
    /** Creates a new instance of POP3CThread
     * @param _pop3s POP3Server class
     * @param _client Socket client
     */
    public POP3CThread( POP3Server _pop3s, Socket _client ) {
        client = _client;
        /**
         * Set Time out connection with client
         */
        try{
            client.setSoTimeout( set.getIntKey( "POP3.Connection_timeout" ) );
        }catch(Exception e){
            log.debug(this, e, Logger.Level.WARNING);
        }
        pop3s = _pop3s;
    }
    
    @Override
    public void run(){
        boolean isLogged = false;
        try{
            
            in = new DataInputStream( client.getInputStream() );
            inLine = new BufferedReader( new InputStreamReader( client.getInputStream() ) );
            out = new PrintWriter( client.getOutputStream(), true );
            
            //MessageHandlerFactory  msg = new MessageHandlerFactory( this, ivStatement );
            
            // send server ready
            sendLine("+OK DotRow POP3 Server ready. (" +  new java.util.Date()  + ")");
        
            //Vector MessagesError = new Vector();
            String line;
            
            while( (line = inLine.readLine()) != null ){
                log.debug(this, line, Logger.Level.INFO);
                StringTokenizer cmd = new StringTokenizer( line );
                // get command and args
                String command = cmd.nextToken().toUpperCase();
                
                if( command.equals("AUTH") ){
                    //sendLine( "-ERR unknown command <" + command + ">" );
                    sendLine( "-ERR no authentication mechanism support" );
                } else
                    
                if( command.equals("CAPA") ){
                    //sendLine( "-ERR unknown command <" + command + ">" );
                    sendLine( "+OK Capability list follows" );
                    sendLine( "USER" );
                    sendLine( "TOP" );
                    sendLine( "." );
                    continue;
                } else
                
                if( command.equals("USER") ){
                    String args = cmd.nextToken();
                    if( UserHandler.UserExist(args) ){
  
                        sendLine( "+OK Password required for " + args );
                        POP3User = args;
                    }else{
                        sendLine( "-ERR User " + args + " doesnt exist" );
                        log.debug(this, "User " + args + " doesnt exist", Logger.Level.INFO);
                    }
                    continue;
                } else
                    
                if( command.equals("PASS") ){
                    String args = cmd.nextToken();
                    if( POP3User != null ){
                        
                        if( UserHandler.getPwdByAlias( POP3User ).equals( args ) ){
                            isLogged = true;
                            sendLine( "+OK Grant Access" );
                            //Stat();
                            this.msgs = Mh.getMessages( POP3User, Email.MSGFOLDER_INBOX );
                            log.debug(this, "User " + POP3User + " logged", Logger.Level.INFO);
                            
                        }else{
                            sendLine( "-ERR password is wrong" );
                            log.debug(this, "Password of user " + POP3User + " is wrong", Logger.Level.INFO);
                        }
                        
                    }else{
                        sendLine( "-ERR first user" );
                    }
                    continue;
                }
                
                // Close connection
                if( command.equals( "QUIT" ) ){
                    sendLine( "+OK Closing connection" );
                    break;
                }
                
                // if user has been logged can be continue, else do nothing
                if( isLogged ){
                    
                    // Return the statics for messages unreaded
                    if( command.equals( "STAT" ) ){
                        Stat();
                        continue;
                    } else
                    
                    // List all messages unreaded
                    if( command.equals( "LIST" ) ){
                        try{
                            // case list have arguments
                            String args = cmd.nextToken();
                            List( Integer.parseInt( args ) - 1 );
                        }catch(Exception e){
                            // case list haven`t arguments
                            List();
                        }
                        continue;
                    } else

                    // For get Email
                    if( command.equals( "RETR" ) ){
                        String args = cmd.nextToken();
                        Retr( Integer.parseInt( args ) );
                        continue;
                    } else
                    
                    // Mark as deleted one message
                    if( command.equals( "DELE" ) ){
                        String args = cmd.nextToken();
                        Dele( Integer.parseInt( args ) );
                        continue;
                    } else
                    
                    // Top in this case is the same that retr
                    if( command.equals( "TOP" ) ){
                        String args = cmd.nextToken();
                        Retr( Integer.parseInt( args ) );
                        continue;
                    } else
                    
                    // Only return true
                    if( command.equals( "NOOP" ) ){
                        sendLine( "+OK" );
                        continue;
                    } else
                        
                    if( command.equals( "RSET" ) ){
                        Rset();
                        continue;
                    } else {
                        sendLine( "-ERR unknow command" );
                        continue;
                    }
                    
                } else {
                    sendLine( "-ERR unknow command " );
                    continue;
                }

            }
            CloseConnection();
        }catch(Exception e){
            log.debug(this, e, Logger.Level.INFO);
            CloseConnection();
            e.printStackTrace();
        }
    }
    
    /**
     * Close Connection to all clients
     */
    private void CloseConnection(){
        try{
            out.close();
            in.close();
            client.close();
            log.debug(this, "Client socket closed", Logger.Level.INFO);
            log.debug(this, "Destroying Thread", Logger.Level.INFO);
            pop3s.deleteClient(this);
        }catch(Exception e1){
            log.debug(this, e1, Logger.Level.ERROR);
            e1.printStackTrace();
        }
    }
    
    /**
     *  Return state of inbox
     */
    private void Stat(){
        sendLine( "+OK " + msgs.size() + " " + Mh.getMessagesOctets( msgs ) );
    }
    
    /**
     * Reset Transactions
     */
    private void Rset(){
        // Unmark deleted messages
        Mh.undeleteAllMessage( POP3User );
        // recall all messages and fill vector
        this.msgs = Mh.getMessages( POP3User, Email.MSGFOLDER_INBOX );
        
        sendLine( "+OK " + msgs.size() + " (" + Mh.getMessagesOctets( msgs ) + ")" );
    }
    
    /**
     * Send the content of messaage
     * @param i Email ID
     */
    private void Retr( int i ){
        if( msgExist( i - 1 ) ){
            Email msg = (Email)msgs.get( i - 1 );
            sendLine( "+OK " + msg.getSize() + " octets" );
            sendData( msg.getData() );
            sendLine( "." );
            
        }else{
            sendLine( "-ERR no such message" );
        }
    }
    
    /**
     *  Delete mensasge i
     * @param i Email ID
     */
    @SuppressWarnings("unchecked")
    private void Dele( int i ){
        if( msgExist( i - 1 ) ){
            // mark as deleted message id
            if(  Mh.deleteMessage( i-1 , msgs ) ){
                sendLine( "+OK message " + i + " deleted" );
                // remove from vector the message
                msgs.setElementAt(null, i - 1);
            }else{
                sendLine( "-ERR cant delete message " + i );
            }
        }else{
            sendLine( "-ERR no such message" );
        }
    }
    
    /**
     * List messages in inbox
     */
    private void List(){
       sendLine( "+OK " + msgs.size() + " messages" );
       
       for ( int i = 0; i < msgs.size(); i++ )
        {
            if( msgExist( i ) ){
                Email msg = (Email)msgs.get( i );
                sendLine( ( i + 1 ) + " " + msg.getSize() );
            }
        }
       
       sendLine( "." );
    }
    
    /**
     * Return size of specified message
     * @param i Email ID
     */
    private void List( int i ){
       if(  msgExist( i )){
            sendLine( "+OK " + i + " " + ((Email)msgs.get( i )).getSize() + " messages" );
       }else{
           sendLine( "-ERR no such message" );
       }
       
    }
    
    /**
     * Return if the message exist
     * @param i Email ID
     * @return true if message exist
     */
    private boolean msgExist( int i ){
        boolean _return = false;
        try{
            if( msgs.get( i ) != null )
                _return = true;
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return _return;
    }
    
    public void setStatement(Statement st) {
        ivStatement = st;
    }
    
    /**
     * Send a text line to client
     * @param line The text to transfer
     */
    public void sendLine(String line){
        try{
            out.println( line );
        }catch(Exception e){
            log.debug(this, e, Logger.Level.WARNING);
            e.printStackTrace();
        }
    }
    
    /**
     * Send a block of text to client
     * @param data The block of text
     */
    public void sendData(String data){
        try{
            out.print( data );
        }catch(Exception e){
            log.debug(this, e, Logger.Level.WARNING);
        }
    }
    
}
