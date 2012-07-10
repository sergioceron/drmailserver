/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server.smtp;
/*
 * SMTPCThread.java
 *
 * Created on 13 de junio de 2006, 15:55
 */

import com.dotrow.mail.server.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Statement;
import java.util.Vector;

import static com.dotrow.mail.server.smtp.SMTPResponse.*;

/**
 * SMTP Client Thread
 * @author Sergio Ceron Figueroa
 */
public class SMTPCThread extends Thread implements DatabaseConnection {

    private Logger log = Logger.getInstance();
    private static Statement statement;
    private Socket client = null;
    private SMTPServer smtpServer = null;
    private DataInputStream in = null;
    private BufferedReader inReader = null;
    private PrintWriter out = null;
    private Settings settings = Settings.getInstance();
    
    /** Constructor de la clase SMTPCThread
     * @param _s SMTPServer class
     * @param _client Socket client
     */
    public SMTPCThread(SMTPServer _s, Socket _client) {
        client = _client;
        /**
         * Set Time out connection with client
         */
        try{
            client.setSoTimeout(settings.getIntKey("SMTP.Connection_timeout"));
        }catch(Exception e){
            log.debug(this, e, Logger.Level.WARNING);
        }
        smtpServer = _s;
    }
    
    @Override
    public void run(){
        try{
            boolean Hello = false, ExistUser = false;
            
            in = new DataInputStream( client.getInputStream() );
            inReader = new BufferedReader( new InputStreamReader( client.getInputStream() ) );
            out = new PrintWriter( client.getOutputStream(), true );
            MessageHandlerFactory msg = new MessageHandlerFactory( this, statement);
            
            sendLine("220 DotRow Mail Server v1.1");
                        
            Vector<Email> MessagesError = new Vector<Email>();
            String line;
            while( (line = inReader.readLine().toUpperCase()) != null ){
                log.debug(this, line, Logger.Level.INFO);
                if(line.startsWith("HELO") || line.startsWith("EHLO")){
                    sendLine( RC250.getMessage() );
                    Hello = true;
                    continue;
                }
                
                if( Hello ){
                    if( line.startsWith("MAIL FROM") ){
                        msg.setFrom( line );
                        UserHandler user= UserHandler.getUser( this, msg.getFrom() );
                        if( !user.userExist() ){
                            sendLine( RC550.getMessage() );
                            break;
                        }else{
                            sendLine( RC250.getMessage() );
                            continue;
                        }
                    } else
                    
                    if( line.startsWith("RCPT TO") ){
                        UserHandler user= UserHandler.getUser( this, msg.getTo( line ) );
                        LocalDomains ld = LocalDomains.getInstance();
                        if( user.userExist() ){
                            sendLine( RC250.getMessage() );
                            msg.setTo( line );
                            continue;
                        }else{
                            if( ld.isLocalDomain( MessageHandlerFactory.getDomain( msg.getFrom() ) ) ){
                                // prepare to send mail error********
                                EmailError mee = new EmailError();
                                mee.setFrom( msg.getFrom() );
                                mee.setTo( msg.getTo( line ) );
                                mee.setType( Email.MSGTYPE_ERROR );
                                mee.setFolder( Email.MSGFOLDER_INBOX );
                                mee.setError( RC550.getMessage() );
                                MessagesError.add( mee ) ;
                                
                                log.debug(this, "Sending mail error ( user no exist ) for local user", Logger.Level.INFO);
                                ExistUser = false;
                                sendLine( RC250.getMessage() );
                                continue;
                            }else{
                                sendLine( RC550.getMessage() );
                                break;
                            }
                        }
                    } else
                    
                    if( line.startsWith("DATA") ){
                        UserHandler user= UserHandler.getUser( this, msg.getFrom() );
                        String Data = getData( in );
                        msg.setData( Data );
                        // check if sender is local and have quota
                        if ( !user.haveQuota( msg.getData() )){
                            sendLine( RC552.getMessage() );
                            break;
                        }
                        /* After recived data check if recivers is local and have quota */
                        if( !Storange( msg )){
                            sendLine( RC552.getMessage() );
                            break;
                        }else{
                            // send response code
                            sendLine( RC250.getMessage() );
                        }
                        // add data to error messages for non user exist and send mail error
                        for (Email aMessagesError : MessagesError) {
                            EmailError mee = (EmailError) aMessagesError;
                            mee.setData(Data);
                            MailerDaemon.getInstance().addMailToSend(mee);
                        }
                        continue;
                    } else
                    
                    if( line.startsWith("RSET") ){
                        msg.clearTo();
                        sendLine( RC250.getMessage() );
                        continue;
                    } else
                    
                    if( line.startsWith("QUIT") ){
                        msg.send();
                        sendLine( RC221.getMessage() );
                        break;
                    } else {
                        sendLine( RC505.getMessage() );
                        continue;
                    }
                    
                }else{
                    sendLine( RC503.getMessage() );
                }
            }
            dissconnect();
        }catch(Exception e){
            log.debug(this, e, Logger.Level.ERROR);
            dissconnect();
        }
    }
    
    
    /**
     * Verify that you have disk space to store mail
     * @param msg The Email Mail
     * @return true if space
     */
    private boolean Storange(MessageHandlerFactory msg){
        LocalDomains ld = LocalDomains.getInstance();
        UserHandler user;
        boolean _return = false;
        int w = 0;
        for( ; w < msg.getTo().size() ; w++ ) {
            user = UserHandler.getUser( this, msg.getTo().get(w).toString() );
            if( user.haveQuota( msg.getData() ) ){
                _return = true;
            }else{
                if( ld.isLocalDomain( MessageHandlerFactory.getDomain( msg.getFrom()) ) ){
                    // prepare to send mail error********
                    EmailError meq = new EmailError();
                    meq.setFrom( msg.getFrom() );
                    meq.setTo( msg.getTo().get(w).toString() );
                    meq.setType( Email.MSGTYPE_ERROR );
                    meq.setFolder( Email.MSGFOLDER_INBOX );
                    meq.setError( RC552.getMessage() );
                    meq.setData( msg.getData() );
                    // send mail error
                    MailerDaemon.getInstance().addMailToSend( meq );
                    
                    log.debug(this, "Sending mail error ( quota ) for local user", Logger.Level.INFO);
                    _return = true;
                }else{
                    _return = false;
                }
                msg.removeTo( w );
            }
        }
        
        if( ld.isLocalDomain( MessageHandlerFactory.getDomain( msg.getFrom()) ) && w==0)
            _return = true;
        
        return _return;
    }
    
    
    /**
     * Close client connections
     */
    private void dissconnect(){
        try{
            out.close();
            in.close();
            client.close();
            log.debug(this, "Client socket closed", Logger.Level.INFO);
            log.debug(this, "Destroying Thread", Logger.Level.INFO);
            smtpServer.deleteClient(this);
        }catch(Exception e1){
            log.debug(this, e1, Logger.Level.ERROR);
            e1.printStackTrace();
        }
    }
    
    /**
     * Receive data sent by the client in bytes
     * @param in Input Stream
     * @return String formatted data
     */
    private String getData(DataInputStream in){
        sendLine( RC354.getMessage() );
        String line, data = "";
        try{
            while( (line = in.readLine().trim()) != null){
                if( !line.equals(".") ){
                    data += line + "\r\n";
                }else{
                    break;
                }
            }
        }catch(Exception e){
            log.debug(this, e, Logger.Level.ERROR);
        }
        return data;
    }
    
    /**
     * Send a text line to client
     * @param data Text line
     */
    public void sendLine(String data){
        try{
            out.println(data);
        }catch(Exception e){
            log.debug(this, e, Logger.Level.WARNING);
        }
    }
    
    /**
     * Send a block text to client
     * @param data The block text
     */
    public void Write(String data){
        try{
            PrintWriter out = new PrintWriter( client.getOutputStream(), true);
            out.write(data);
        }catch(Exception e){
            log.debug(this, e, Logger.Level.WARNING);
        }
    }
    
    public void setStatement(java.sql.Statement st) {
        statement = st;
    }
    
}
