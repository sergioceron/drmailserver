/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server;
/*
 * Email.java
 *
 * Created on 14 de junio de 2006, 11:32
 */

import java.sql.Statement;
import java.util.Vector;

/**
 * Email smtp to send or save
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
     * Creates a new instance of Email
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
            log.debug(tClient, e, Logger.Level.WARNING);
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
            log.debug(tClient, e, Logger.Level.WARNING);
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
            log.debug(tClient, e, Logger.Level.WARNING);
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
     * @param data Email data.
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
                        _return = MailerDaemon.getInstance().persistMail(sendTo, getData(), Email.MSGFOLDER_INBOX);
                    } else {
                        log.debug(tClient, "Mail from: " + getFrom() + " to :" + sendTo + " is spam", Logger.Level.INFO);
                        _return = MailerDaemon.getInstance().persistMail(sendTo, getData(), Email.MSGFOLDER_SPAM);
                    }
                }else{
                    // Add 'Mail to send' to Mailer Daemon and set parameters of message
                    Email msg = new Email();
                    msg.setTo( getTo().get( iv ).toString() );
                    msg.setFrom( getFrom() );
                    msg.setData( getData() );
                    msg.setFolder( Email.MSGFOLDER_INBOX );
                    msg.setType( Email.MSGTYPE_NORMAL );
                    MailerDaemon.getInstance().addMailToSend( msg );
                    log.debug(tClient, "Sending Mail", Logger.Level.INFO);
                    _return = true;
                }
            }
            /*if user was sent is local, save in folder sents
              if( ld.isLocalDomain( getDomain( getFrom() ) ) &&  !fromEqTo){
                _return = MailerDaemon.getInstance().persistMail( getFrom(), getData(), Email.MSGFOLDER_SENTS );
            }*/
        }catch(Exception e){
            log.debug(tClient, e, Logger.Level.WARNING);
            _return = false;
        }
        return _return;
    }
    
    /**
     * Get a domain from mail address
     * @param sparse mail of user
     * @return Domain of user
     */
    public final static String getDomain(String sparse){
        int i = sparse.indexOf('@') + 1;
        return sparse.substring(i);
    }
}
