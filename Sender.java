/*
 * Sender.java
 *
 * Created on 16 de junio de 2006, 14:28
 */

import java.util.*;
import java.net.*;
import java.io.*;
/**
 * Sender Service Manager
 * @author Sergio Ceron Figueroa
 */
public class Sender extends Thread {
    
    private Logger log = Logger.getInstance();
    private Message msg = null;
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
    
    
    public void setMessage( Message msg ){
        this.msg = msg;
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
            if( msg.getType() == 1 ){ // Mail error
                MessageError me = null;
                me = (MessageError) msg;
                //System.out.println("this message error is " + msg.getError() + " in thread "+ this);
                MailerDaemon.getInstance().SaveMail( me.getFrom(), me.getUndeliveredMessage() , me.getFolder() );
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
                        
                        log.TDebug( this, in.readLine(), 2 );
                        
                        out.println( "HELO 189.144.4.75" );
                        if (! ProcessResponse( in.readLine(), 250 ))
                            break;

                        out.println( "MAIL FROM:<" + msg.getFrom() + ">" );
                        if (! ProcessResponse( in.readLine(), 250 ))
                            break;
                        
                        out.println( "RCPT TO:<" + msg.getTo() + ">" );
                        if (! ProcessResponse( in.readLine(), 250 ))
                            break;
                        
                        out.println( "DATA" );
                        if (! ProcessResponse( in.readLine(), 354 ))
                            break;
                        
                        out.print( msg.getData() );
                        out.println( "." );
                        if (! ProcessResponse( in.readLine(), 250 ))
                            break;
                        
                        out.println( "QUIT" );
                        log.TDebug( this, in.readLine(), 2 );
                        //ViewTraffic.getInstance().getData( msg.getData().length() );
                        break;
                    }
                    // stop connection
                    Destroy();
                }else{
                    // error host doesnt exist
                    System.out.println("Error dns mx for host" + msg.getTo());
                }
            }catch(Exception e){
                log.TDebug( this, e, 1);
                //e.printStackTrace();
                if( MXHosts() > DEF_DNSHOST ){
                    // return to try connect with another mx server
                    DEF_DNSHOST ++;
                    break retry;
                    
                }else{
                    // if cant connect with mx server save as queue
                    log.TDebug( this, "sending mail to queue", 2 );
                    MailerDaemon.getInstance().addMailOnQueue( msg );
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
            log.TDebug( this, "Closing connection", 2);
            out.close();
            in.close();
            connection.close();
        }catch( Exception e ){
            log.TDebug( this, e, 1);
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
            
            log.TDebug( this, response, 2 );
            
            if( resCode.equals( ( String.valueOf( hope ) ) )){
                //debug.TDebug( this, "ok continue", 2 );
                _return = true;
                
            }else{
                // error level 1 only send to queue
                if( resCode.startsWith( "4" ) ){
                    _return = false;
                    log.TDebug( this, "Error : sending mail to queue", 2 );
                    MailerDaemon.getInstance().addMailOnQueue( msg );
                    
                    // error level 0
                }else if( resCode.startsWith( "5" ) ){
                    log.TDebug( this, "Fatal error : Sending mail error to sender", 2);
                    MessageError me = new MessageError();
                    me.setFrom( msg.getFrom() );
                    me.setTo( msg.getTo() );
                    me.setType( Message.MSGTYPE_ERROR );
                    me.setFolder( Message.MSGFOLDER_INBOX );
                    me.setError( response );
                    me.setData( msg.getData() );
                    // send mail error
                    MailerDaemon.getInstance().addMailToSend( me );
                    
                    _return = false;
                    // if send another response code or bad sequence
                }else{
                    log.TDebug( this, resCode + " --> " + hope, 2);
                    _return = false;
                }
                
            }
        }catch(Exception e){
            log.TDebug( this, "e --> " + e, 1 );
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
        Vector mx = nl.getMXHosts( MessageHandlerFactory.getDomain( msg.getTo() ) );
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
        Vector mx = nl.getMXHosts( MessageHandlerFactory.getDomain( msg.getTo() ) );
        return mx.get( n ).toString();
    }
}
