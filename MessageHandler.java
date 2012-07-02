/*
 * MessageHandler.java
 *
 * Created on 18 de junio de 2006, 16:36
 */


import java.sql.*;
import java.util.*;
/**
 *
 * @author Sergio Ceron Figueroa
 */
public class MessageHandler implements DatabaseInterface {
    
    private static MessageHandler _instance = null;
    
    /** Instace of debugger */
    private Logger log = Logger.getInstance();
    
    /** Thread client */
    private static Thread tClient = null;
    
    /** Database statement */
    private static Statement st = null;
    
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
        tClient = t;
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
            ResultSet rs = st.executeQuery( "SELECT * FROM dr_mails WHERE mail_user = '" + UserMail + "' and mail_read = 0 and mail_folder =" + folder );
            while( rs.next() ){
                Message msg = new Message();
                msg.setId( rs.getInt( "mail_id" ) );
                msg.setData( rs.getString( "mail_data" ) );
                Messages.add( msg );
            }
        }catch( Exception e ){
            log.TDebug( tClient, e, 1 );
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
            Message msg = ( Message )Messages.get(i);
            st.execute( "UPDATE dr_mails SET mail_read = 1 WHERE mail_id = " + msg.getId() );
            _return = true;
        }catch(Exception e){
            log.TDebug( tClient, e, 1 );
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
            st.execute( "UPDATE dr_mails SET mail_read = 0 WHERE mail_user = '" + UserMail  + "'" );
            _return = true;
        }catch(Exception e){
            log.TDebug( tClient, e, 1 );
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
                Message msg = ( Message )Messages.get( i );
                MessagesOctet += msg.getSize();
            }
        }catch( Exception e ){
            log.TDebug( tClient, e, 1 );
            e.printStackTrace();
        }
        return MessagesOctet;
    }
    
    /**
     * Get a database statement
     * @param st Database statement
     */
    public void dbStatement(java.sql.Statement st) {
        MessageHandler.st = st;
    }
    
}
