/*
 * SMTPCThread.java
 *
 * Created on 13 de junio de 2006, 15:55
 */

import java.net.*;
import java.io.*;
import java.util.*;
import java.sql.*;

/**
 * SMTP Client Thread
 * @author Sergio Ceron Figueroa
 */
public class SMTPCThread extends Thread implements DatabaseInterface {

    private Logger log = Logger.getInstance();
    private static Statement ivStatement;
    private Socket client = null;
    private SMTPServer s = null;
    private DataInputStream in = null;
    private BufferedReader inReader = null;
    private PrintWriter out = null;
    private Settings set = Settings.getInstance();
    
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
            client.setSoTimeout( set.getIntKey( "SMTP.Connection_timeout" ) );
        }catch(Exception e){
            log.TDebug(this, e, 1);
        }
        s = _s;
    }
    
    @Override
    public void run(){
        try{
            boolean Hello = false, ExistUser = false;
            
            in = new DataInputStream( client.getInputStream() );
            inReader = new BufferedReader( new InputStreamReader( client.getInputStream() ) );
            out = new PrintWriter( client.getOutputStream(), true );
            MessageHandlerFactory  msg = new MessageHandlerFactory( this, ivStatement );
            
            sendLine("220 DotRow Mail Server v1.1");
                        
            Vector MessagesError = new Vector();
            String line;
            while( (line = inReader.readLine().toUpperCase()) != null ){
                log.TDebug(this , line, 2);
                if(line.startsWith("HELO") || line.startsWith("EHLO")){
                    sendLine( ResponseCode.RC250 );
                    Hello = true;
                    continue;
                }
                
                if( Hello ){
                    if( line.startsWith("MAIL FROM") ){
                        msg.setFrom( line );
                        UserHandler user= UserHandler.getUser( this, msg.getFrom() );
                        if( !user.userExist() ){
                            sendLine( ResponseCode.RC550 );
                            break;
                        }else{
                            sendLine( ResponseCode.RC250 );
                            continue;
                        }
                    } else
                    
                    if( line.startsWith("RCPT TO") ){
                        UserHandler user= UserHandler.getUser( this, msg.getTo( line ) );
                        LocalDomains ld = LocalDomains.getInstance();
                        if( user.userExist() ){
                            sendLine( ResponseCode.RC250 );
                            msg.setTo( line );
                            continue;
                        }else{
                            if( ld.isLocalDomain( MessageHandlerFactory.getDomain( msg.getFrom() ) ) ){
                                // prepare to send mail error********
                                MessageError mee = new MessageError();
                                mee.setFrom( msg.getFrom() );
                                mee.setTo( msg.getTo( line ) );
                                mee.setType( Message.MSGTYPE_ERROR );
                                mee.setFolder( Message.MSGFOLDER_INBOX );
                                mee.setError( ResponseCode.RC550 );
                                MessagesError.add( mee ) ;
                                
                                log.TDebug(this, "Sending mail error ( user no exist ) for local user", 2);
                                ExistUser = false;
                                sendLine( ResponseCode.RC250 );
                                continue;
                            }else{
                                sendLine( ResponseCode.RC550 );
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
                            sendLine( ResponseCode.RC552 );
                            break;
                        }
                        /* After recived data check if recivers is local and have quota */
                        if( !Storange( msg )){
                            sendLine( ResponseCode.RC552 );
                            break;
                        }else{
                            // send response code
                            sendLine( ResponseCode.RC250 );
                        }
                        // add data to error messages for non user exist and send mail error
                        for( int e = 0; e < MessagesError.size(); e++ ){
                            MessageError mee = ( MessageError )MessagesError.get( e );
                            mee.setData( Data );
                            MailerDaemon.getInstance().addMailToSend( mee );
                        }
                        continue;
                    } else
                    
                    if( line.startsWith("RSET") ){
                        msg.clearTo();
                        sendLine( ResponseCode.RC250 );
                        continue;
                    } else
                    
                    if( line.startsWith("QUIT") ){
                        msg.send();
                        sendLine( ResponseCode.RC221 );
                        break;
                    } else {
                        sendLine( ResponseCode.RC505 );
                        continue;
                    }
                    
                }else{
                    sendLine( ResponseCode.RC503 );
                }
            }
            end();
        }catch(Exception e){
            log.TDebug(this ,e, 0);
            e.printStackTrace();
            end();
        }
    }
    
    
    /**
     * Verify that you have disk space to store mail
     * @param msg The Message Mail
     * @return true if space
     */
    private boolean Storange(MessageHandlerFactory msg){
        LocalDomains ld = LocalDomains.getInstance();
        UserHandler user;
        boolean _return = false;
        int w=0;
        for( w=0; w < msg.getTo().size() ; w++ ) {
            user = UserHandler.getUser( this, msg.getTo().get(w).toString() );
            if( user.haveQuota( msg.getData() ) ){
                _return = true;
            }else{
                if( ld.isLocalDomain( MessageHandlerFactory.getDomain( msg.getFrom()) ) ){
                    // prepare to send mail error********
                    MessageError meq = new MessageError();
                    meq.setFrom( msg.getFrom() );
                    meq.setTo( msg.getTo().get(w).toString() );
                    meq.setType( Message.MSGTYPE_ERROR );
                    meq.setFolder( Message.MSGFOLDER_INBOX );
                    meq.setError( ResponseCode.RC552 );
                    meq.setData( msg.getData() );
                    // send mail error
                    MailerDaemon.getInstance().addMailToSend( meq );
                    
                    log.TDebug(this, "Sending mail error ( quota ) for local user", 2);
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
    private void end(){
        try{
            out.close();
            in.close();
            client.close();
            log.TDebug(this ,"Client socket closed", 2);
            log.TDebug(this ,"Destroying Thread", 2);
            s.deleteClient(this);
            
        }catch(Exception e1){
            log.TDebug(this ,e1, 0);
            e1.printStackTrace();
        }
    }
    
    /**
     * Receive data sent by the client in bytes
     * @param in Input Stream
     * @return String formatted data
     */
    private String getData(DataInputStream in){
        sendLine( ResponseCode.RC354 );
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
            log.TDebug(this, e, 0);
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
            log.TDebug(this ,e, 1);
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
            log.TDebug(this ,e, 1);
        }
    }
    
    public void dbStatement(java.sql.Statement st) {
        ivStatement = st;
    }
    
}
