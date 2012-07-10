/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server;
/*
 * Sender.java
 *
 * Created on 16 de junio de 2006, 14:28
 */

import com.dotrow.mail.server.util.NSLookup;

import java.io.DataInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;
/**
 * Sender Service Manager
 * @author Sergio Ceron Figueroa
 */
public class Sender extends Thread {
    
    private Logger log = Logger.getInstance();
    private Email email = null;
    private static int  QUEUE_TIMEOUT = 0;
    private static int DEF_PORT = 25;
    private int DEF_DNSHOST = 0;
    private Socket connection = null;
    private DataInputStream in = null;
    private PrintWriter out = null;
    private boolean running = true;
    
    /** Creates a new instance of Sender */
    public Sender() {
    }
    
    
    public void setMessage( Email msg ){
        this.email = msg;
    }
    
    /**
     * Set the timeout to send mail in the queue
     * @param milis Time to wait
     */
    @SuppressWarnings("static-access")
    public void setQueueTimeout( int milis ) {
        this.QUEUE_TIMEOUT = milis;
    }
    
    @Override
    @SuppressWarnings("static-access")
    public void run(){
        try{
            // sleep thread on first time if is queue
            this.sleep( QUEUE_TIMEOUT );
            if( email.getType() == 1 ){ // Mail error
                EmailError me = null;
                me = (EmailError) email;
                //System.out.println("this message error is " + email.getError() + " in thread "+ this);
                MailerDaemon.getInstance().persistMail(me.getFrom(), me.getUndeliveredMessage(), me.getFolder());
            }else{
                SendMail();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Establishes communication SMTP to send emails
     * @return 0 if mail is correctly shipping
      */
    private int SendMail(){
        retry :
            try{
                if( MXHosts() != 0 ){
                    // start while for control of request
                    while( running ){
                        connection = new Socket( getMXHost( DEF_DNSHOST ), DEF_PORT );
                        in = new DataInputStream( connection.getInputStream() );
                        out = new PrintWriter( connection.getOutputStream(), true);
                        
                        log.debug(this, in.readLine(), Logger.Level.INFO);
                        
                        out.println( "HELO 189.144.4.75" );
                        if (! ProcessResponse( in.readLine(), 250 ))
                            break;

                        out.println( "MAIL FROM:<" + email.getFrom() + ">" );
                        if (! ProcessResponse( in.readLine(), 250 ))
                            break;
                        
                        out.println( "RCPT TO:<" + email.getTo() + ">" );
                        if (! ProcessResponse( in.readLine(), 250 ))
                            break;

                        out.println( "DATA" );
                        if (! ProcessResponse( in.readLine(), 354 ))
                            break;
                        
                        out.print(email.getData());
                        out.println( "." );
                        if (! ProcessResponse( in.readLine(), 250 ))
                            break;
                        
                        out.println( "QUIT" );
                        log.debug(this, in.readLine(), Logger.Level.INFO);
                        //ViewTraffic.getInstance().getData( email.getData().length() );
                        break;
                    }
                    // stop connection
                    Destroy();
                }else{
                    // error host doesnt exist
                    System.out.println("Error dns mx for host" + email.getTo());
                }
            }catch(Exception e){
                log.debug(this, e, Logger.Level.WARNING);
                //e.printStackTrace();
                if( MXHosts() > DEF_DNSHOST ){
                    // return to try connect with another mx server
                    DEF_DNSHOST ++;
                    break retry;
                    
                }else{
                    // if cant connect with mx server save as queue
                    log.debug(this, "sending mail to queue", Logger.Level.INFO);
                    MailerDaemon.getInstance().addMailOnQueue(email);
                }
            }
            return 0;
    }
    
    /**
     * Close server connection
     */
    private void Destroy(){
        //finalize connection and in/out stream
        try{
            this.running = false;
            log.debug(this, "Closing connection", Logger.Level.INFO);
            out.close();
            in.close();
            connection.close();
        }catch( Exception e ){
            log.debug(this, e, Logger.Level.WARNING);
        }
        
    }
    
    /**
     * Process the response code of mail server
     * @param response Response of server
     * @param hope Expected value
     * @return True if the two values match
     */
    private boolean ProcessResponse( String response, int hope ){
        String resCode = null;
        boolean _return = false;
        try{
            resCode = response.substring( 0, 3 );
            
            log.debug(this, response, Logger.Level.INFO);
            
            if( resCode.equals( ( String.valueOf( hope ) ) )){
                //debug.TDebug( this, "ok continue", 2 );
                _return = true;
                
            }else{
                // error level 1 only send to queue
                if( resCode.startsWith( "4" ) ){
                    _return = false;
                    log.debug(this, "Error : sending mail to queue", Logger.Level.INFO);
                    MailerDaemon.getInstance().addMailOnQueue(email);
                    
                    // error level 0
                }else if( resCode.startsWith( "5" ) ){
                    log.debug(this, "Fatal error : Sending mail error to sender", Logger.Level.INFO);
                    EmailError me = new EmailError();
                    me.setFrom(email.getFrom());
                    me.setTo( email.getTo());
                    me.setType(Email.MSGTYPE_ERROR);
                    me.setFolder( Email.MSGFOLDER_INBOX );
                    me.setError(response);
                    me.setData(email.getData());
                    // send mail error
                    MailerDaemon.getInstance().addMailToSend( me );
                    
                    _return = false;
                    // if send another response code or bad sequence
                }else{
                    log.debug(this, resCode + " --> " + hope, Logger.Level.INFO);
                    _return = false;
                }
                
            }
        }catch(Exception e){
            log.debug(this, "e --> " + e, Logger.Level.WARNING);
            e.printStackTrace();
            Destroy();
            _return = false;
        }
        return _return;
    }
    
    /**
     * Get Number of DNS MX Registers 
     * @return Count IP`s address
     */
    private int MXHosts(){
        NSLookup nl = NSLookup.getInstance();
        Vector mx = nl.getMXHosts(MessageHandlerFactory.getDomain(email.getTo()) );
        if ( mx.size() > 0 )
            return mx.size();
        else
            return 0;
    }
    
    
    /**
     * Lookup and find IP address information of the SMTP server name
     * @param n ID of Host
     * @return IP address
     */
    private String getMXHost( int  n ){
        NSLookup nl = NSLookup.getInstance();
        Vector mx = nl.getMXHosts( MessageHandlerFactory.getDomain(email.getTo()) );
        return mx.get( n ).toString();
    }
}
