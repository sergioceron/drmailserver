/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server;
/*
 * MessageHandler.java
 *
 * Created on 18 de junio de 2006, 16:36
 */


import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Vector;
/**
 *
 * @author Sergio Ceron Figueroa
 */
public class MessageHandler implements DatabaseConnection {
    
    private static MessageHandler _instance = null;
    
    /** Instace of debugger */
    private Logger log = Logger.getInstance();
    
    /** Thread client */
    private static Thread client = null;
    
    /** Database statement */
    private static Statement statement = null;
    
    /** Creates a new instance of MessageHandler */
    private MessageHandler() {}
    
    /**
     * Get a active instanse of MessageHandler
     * @return instance of MessageHandler if exist, if not create new
     */
    final static public MessageHandler getMessageHandler(){
        return _instance != null ? _instance : new MessageHandler();
    }
    
    /**
     * Get a active instanse of MessageHandler
     * @return instance of MessageHandler if exist, if not create new
     * @param t Thread for child message handler
     */
    final static public MessageHandler getMessageHandler( Thread t ){
        client = t;
        return _instance != null ? _instance : new MessageHandler();
    }
    
    /**
     * Get vector list of messages for user and folder specified
     * @return Vector of messages
     * @param UserAlias Alias of user to list messages
     * @param folder Alias of user to list messages
     */
    public Vector getMessages( String UserAlias, int folder ){
        Vector Messages = new Vector();
        try{
            String UserMail = UserHandler.getMailByAlias( UserAlias  );
            // Count all messages no readed
            ResultSet rs = statement.executeQuery(MessageFormat.format("SELECT * FROM emails WHERE user_id = ''{0}'' and read = 0 and recipient_id ={1}", UserMail, folder));
            while( rs.next() ){
                Email msg = new Email();
                msg.setId( rs.getInt("id") );
                msg.setData( rs.getString("message") );
                Messages.add( msg );
            }
        }catch( Exception e) {
            log.debug(client, e, Logger.Level.WARNING);
            e.printStackTrace();
        }
        return Messages;
    }
    
    /**
     * mark as deleted mail spacified from list of messages
     * @return if mail has been deleted
     * <I>true</I> if mail has been deleted sucefully
     * <I>false</I> if mail cant be deleted
     * @param i id of message
     * @param Messages vector of messages
     */
    public boolean deleteMessage( int i , Vector Messages ){
        boolean _return = false;
        try{
            Email msg = (Email)Messages.get(i);
            statement.execute(MessageFormat.format("UPDATE emails SET read = 1 WHERE id = {0}", msg.getId()));
            _return = true;
        }catch(Exception e){
            log.debug(client, e, Logger.Level.WARNING);
        }
        return _return;
    }
    
    /**
     * mark as undeleted mail spacified from list of messages
     * @return if mail has been undeleted
     * <I>true</I> if mail has been undeleted sucefully
     * <I>false</I> if mail cant be undeleted
     * @param UserAlias alias of user to undelete messages
     */
    public boolean undeleteAllMessage( String UserAlias ){
        boolean _return = false;
        try{
            String UserMail = UserHandler.getMailByAlias( UserAlias  );
            statement.execute(MessageFormat.format("UPDATE emails SET read = 0 WHERE user_id = ''{0}''", UserMail));
            _return = true;
        }catch(Exception e){
            log.debug(client, e, Logger.Level.WARNING);
        }
        return _return;
    }
    
    /**
     * Get octets of messages, size of message in 8 bits
     * @return size of messages in 8 bits
     * @param Messages vector of messages
     */
    public long getMessagesOctets( Vector Messages ){
        long MessagesOctet = 0;
        try{
            for( int i = 0; i< Messages.size(); i++ ){
                Email msg = (Email)Messages.get( i );
                MessagesOctet += msg.getSize();
            }
        }catch( Exception e ){
            log.debug(client, e, Logger.Level.WARNING);
            e.printStackTrace();
        }
        return MessagesOctet;
    }
    
    /**
     * Get a database statement
     * @param st Database statement
     */
    public void setStatement(java.sql.Statement st) {
        MessageHandler.statement = st;
    }
    
}
