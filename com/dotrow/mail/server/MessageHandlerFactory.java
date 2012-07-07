package com.dotrow.mail.server;
/*
 * Message.java
 *
 * Created on 14 de junio de 2006, 11:32
 */

import java.util.*;
import java.sql.*;
import java.text.*;
/**
 * Message smtp to send or save
 * @author Sergio Ceron Figueroa
 */
public class MessageHandlerFactory {
    
    /** Instace of debugger */
    private Logger log = Logger.getInstance();
    
    /** Database statement */
    private Statement st = null;
    
    /** Mail Server Filter instance */
    private MailServerFilter msf = MailServerFilter.getInstance();
    
    /**
     * Mail from
     */
    private String from;
    
    /**
     * Vector of mail to
     */
    private Vector to = new Vector();
    
     /**
     * Thread client
     */
    private Thread tClient;
    
    /**
     * Mail data
     */
    private String data;
    
    /**
     * Creates a new instance of Message
     * @param _tClient Thread Client
     * Can be any thread
     * @param st Database statement
     */
    public MessageHandlerFactory( Thread _tClient, Statement st) {
        this.tClient = _tClient;
        this.st = st;
    }
    
    /**
     * Mail From.
     * @return Value of Mail from.
     */
    public String getFrom() {
        return this.from;
    }
    
    /**
     * Set Mail From of the command smtp.
     * @param from line of smtp.
     */
    public void setFrom( String from ) {
        try{
            int i = from.indexOf(':') + 2;
            this.from = from.trim().substring( i, from.length()-1 ).
            replaceAll("<", "");
        }catch(Exception e){
            log.debugClientThread(tClient, e, 1);
        }
    }
    
    /**
     * Remove one reciver of vector <i>to</i>
     * @param index Element id to delete
     */
    public void removeTo(int index){
        this.to.remove( index );
    }
    
    /**
     * Clear vector <i>to</i> of recivers
     */
    public void clearTo(){
        this.to.clear();
    }
    
    /**
     * Mail vector of recivers.
     * @return Vector Mail of to.
     */
    public Vector getTo() {
        return this.to;
    }
    
    /**
     * Get on reciver of line smtp.
     * @return Value Mail to of command smtp.
     * @param to command smtp.
     */
    public String getTo( String to ) {
        String _to = null;
        try{
            int i = to.indexOf(':') + 2;
            _to = to.trim().substring( i, to.length()-1 ).replaceAll("<", "");
        }catch(Exception e){
            log.debugClientThread(tClient, e, 1);
        }
        return _to;
    }
    
    /**
     * Add element to vector recivers.
     * @param to command smtp.
     */
    public void setTo( String to ) {
        try{
            int i = to.indexOf(':') + 2;
            this.to.add( to.trim().substring( i, to.length()-1 ).
            replaceAll("<", "") );
        }catch(Exception e){
            log.debugClientThread(tClient, e, 1);
        }
    }
    
    /**
     * Getter for property data.
     * @return Value of property data.
     */
    public String getData() {
        return this.data;
    }
    
    /**
     * Data of message.
     * @param data Message data.
     */
    public void setData(String data) {
        this.data = data.replaceAll("'", "`");
    }
    
    /**
     * Save mail to local recivers or send to foreight users
     * @return If mail has been sent
     * <I>true</I> if mail sent
     * <I>false</I> if mail cant send
     */
    public boolean send(){
        boolean _return = false, fromEqTo = false;
        try{
            LocalDomains ld = LocalDomains.getInstance();
            // For each user reciver
            for(int iv = 0; iv < getTo().size(); iv++){
                String sendTo = getTo().get( iv ).toString();
                if( sendTo.equals( getFrom() ) )
                    fromEqTo = true;
                // if user is local
                if( ld.isLocalDomain( getDomain( sendTo ) ) ){
                    
                    // filter mail recived, if this has enabled for reciver
                    if( msf.filterMailToSpam( getFrom(), sendTo ) ){
                        _return = MailerDaemon.getInstance().SaveMail( sendTo, getData(), Message.MSGFOLDER_INBOX );
                    } else {
                        log.debugClientThread( tClient, "Mail from: " + getFrom() + " to :" + sendTo + " is spam", 2 );
                        _return = MailerDaemon.getInstance().SaveMail( sendTo, getData(), Message.MSGFOLDER_SPAM );
                    }
                }else{
                    // Add 'Mail to send' to Mailer Daemon and set parameters of message
                    Message msg = new Message();
                    msg.setTo( getTo().get( iv ).toString() );
                    msg.setFrom( getFrom() );
                    msg.setData( getData() );
                    msg.setFolder( Message.MSGFOLDER_INBOX );
                    msg.setType( Message.MSGTYPE_NORMAL );
                    MailerDaemon.getInstance().addMailToSend( msg );
                    log.debugClientThread( tClient, "Sending Mail", 2 );
                    _return = true;
                }
            }
            /*if user was sent is local, save in folder sents
              if( ld.isLocalDomain( getDomain( getFrom() ) ) &&  !fromEqTo){
                _return = MailerDaemon.getInstance().SaveMail( getFrom(), getData(), Message.MSGFOLDER_SENTS );
            }*/
        }catch(Exception e){
            log.debugClientThread(tClient, e, 1);
            _return = false;
        }
        return _return;
    }
    
    /**
     * Get a domain from mail address
     * @param sparse mail of user
     * @return Domain of user
     */
    final protected static String getDomain( String sparse ){
        int i = sparse.indexOf('@') + 1;
        return sparse.substring(i);
    }
}
