package com.dotrow.mail.server;
/*
 * UserHandler.java
 *
 * Created on 15 de junio de 2006, 8:52
 */

import java.sql.*;
/**
 * User Handler
 * @author Sergio Ceron Figueroa
 */
public class UserHandler implements DatabaseInterface{
    private static Statement st = null;
    private static Thread tClient = null;
    private static String ivUser = null;
    private static Logger log = Logger.getInstance();
    private static UserHandler _instance = null;
    /** Creates a new instance of UserHandler */
    private UserHandler() {}
    
    /**
     * Create a new instance of UserHandler
     * @return new instance of UserHandler
     */
    final static public UserHandler newInstance(){
        return new UserHandler();
    }
    
    /**
     * Create a new instance of UserHandler from Thread client and Username
     * @param _tClient CLient Thread
     * @param _ivUser User Alias
     * @return New UserHandler
     */
    final static public UserHandler getUser(Thread _tClient, String _ivUser){
        tClient = _tClient;
        ivUser = _ivUser;
        return _instance!=null? _instance : new UserHandler();
    }
    
    /**
     * Verify that you have disk space to store mail
     * @param Data Body of message
     * @return true if have space
     */
    public boolean haveQuota( String Data ){
        LocalDomains ld = LocalDomains.getInstance();
        boolean _return = false;
        try{
            if(ld.isLocalDomain( MessageHandlerFactory.getDomain( ivUser ) )){
                ResultSet rs = st.executeQuery( "SELECT user_quota FROM dr_users WHERE user_name = '" + ivUser + "'" );
                rs.next();
                if( rs.getInt( "user_quota" ) >  Data.length()){
                    _return = true;
                }else{
                    _return = false;
                    log.debugClientThread( tClient, ivUser + " have not quota", 2 );
                }
            }else{
                _return = true;
            }
            
        }catch(Exception e){
            log.debugClientThread(tClient, e, 1);
            e.printStackTrace();
            _return = false;
        }
        return _return;
    }
    
    /**
     * Return the alias user from her mail address
     * @param ivMail Mail address
     * @return User alias
     */
    public String getAliasByMail( String ivMail ){
        String alias = null;
        try{
            ResultSet rs = st.executeQuery( "SELECT user_alias FROM dr_users WHERE user_name = '" + ivMail + "'" );
            if( rs.next() )
                alias = rs.getString( "user_alias" );
        }catch( Exception e ){
            log.debugClientThread( tClient, e, 1 );
        }
        return alias;
    }
    
    /**
     * Return mail address from user alias
     * @param ivAlias User alias
     * @return Mail addrsss
     */
    final static protected String getMailByAlias( String ivAlias ){
        String name = null;
        try{
            ResultSet rs = st.executeQuery( "SELECT user_name FROM dr_users WHERE user_alias = '" + ivAlias + "'" );
            if( rs.next() )
                name = rs.getString( "user_name" );
        }catch( Exception e ){
            log.debugClientThread( tClient, e, 1 );
        }
        return name;
    }
    
    /**
     * Verify if mail filter of user is enabled
     * @param ivMail User mail address
     * @return true if filter is active
     */
    final protected boolean isFilterEnabled( String ivMail ){
        boolean enabled = false;
        try{
            ResultSet rs = st.executeQuery( "SELECT user_filter_enabled FROM dr_users WHERE user_name = '" + ivMail + "'" );
            if( rs.next() )
                enabled = rs.getBoolean( "user_filter_enabled" );

        }catch( Exception e ){
            log.debugClientThread( tClient, e, 1 );
        }
        return enabled;
    }
    
    /**
     * Return the password of user from her alias
     * @param ivAlias User alias
     * @return The password
     */
    final static protected String getPwdByAlias( String ivAlias ){
        String pwd = null;
        try{
            ResultSet rs = st.executeQuery( "SELECT user_pwd FROM dr_users WHERE user_alias = '" + ivAlias + "'" );
            if( rs.next() )
                pwd = rs.getString( "user_pwd" );
        }catch( Exception e ){
            log.debugClientThread( tClient, e, 1 );
        }
        return pwd;
    }
    
    /**
     * Verify if specified user exist
     * @param ivAlias User alias
     * @return true if user exist
     */
    final static protected boolean UserExist( String ivAlias ){
        boolean isUser = false;
        try{
            ResultSet rs = st.executeQuery( "SELECT user_name FROM dr_users WHERE user_alias = '" + ivAlias + "'" );
            if( rs.next() )
                isUser = true;
        }catch( Exception e ){
            log.debugClientThread( tClient, e, 1 );
        }
        return isUser;
    }
    
    /**
     * Verify if user exist
     * @return true if user exist
     */
    public boolean userExist(){
        LocalDomains ld = LocalDomains.getInstance();
        boolean _return = false;
        try{
            if(ld.isLocalDomain( MessageHandlerFactory.getDomain( ivUser ) )){
                ResultSet rs = st.executeQuery( "SELECT * FROM dr_users WHERE user_name = '" + ivUser + "'" );
                if(rs.next())
                    _return = true;
                else
                    log.debugClientThread( tClient, ivUser + " does not exist", 2 );
            }else{
                _return = true;
            }
        }catch(Exception e){
            log.debugClientThread(tClient, e, 1);
            _return = false;
        }
        return _return;
    }
    
    @SuppressWarnings("static-access")
    public void dbStatement(Statement st) {
        this.st = st;
    }
    
}
