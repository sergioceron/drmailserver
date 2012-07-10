/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server;
/*
 * UserHandler.java
 *
 * Created on 15 de junio de 2006, 8:52
 */

import java.sql.ResultSet;
import java.sql.Statement;
/**
 * User Handler
 * @author Sergio Ceron Figueroa
 */
public class UserHandler implements DatabaseConnection {
    private static Statement statement = null;
    private static Thread client = null;
    private static String userEmail = null;
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
        client = _tClient;
        userEmail = _ivUser;
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
            if(ld.isLocalDomain( MessageHandlerFactory.getDomain(userEmail) )){
                ResultSet rs = statement.executeQuery( "SELECT quota FROM users WHERE name = '" + userEmail + "'" );
                rs.next();
                if( rs.getInt( "user_quota" ) >  Data.length()){
                    _return = true;
                }else{
                    _return = false;
                    log.debug(client, userEmail + " have not quota", Logger.Level.INFO);
                }
            }else{
                _return = true;
            }
            
        }catch(Exception e){
            log.debug(client, e, Logger.Level.WARNING);
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
            ResultSet rs = statement.executeQuery( "SELECT alias FROM users WHERE name = '" + ivMail + "'" );
            if( rs.next() )
                alias = rs.getString( "user_alias" );
        }catch( Exception e ){
            log.debug(client, e, Logger.Level.WARNING);
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
            ResultSet rs = statement.executeQuery( "SELECT name FROM users WHERE alias = '" + ivAlias + "'" );
            if( rs.next() )
                name = rs.getString( "user_name" );
        }catch( Exception e ){
            log.debug(client, e, Logger.Level.WARNING);
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
            ResultSet rs = statement.executeQuery( "SELECT filter_enabled FROM users WHERE name = '" + ivMail + "'" );
            if( rs.next() )
                enabled = rs.getBoolean( "user_filter_enabled" );

        }catch( Exception e ){
            log.debug(client, e, Logger.Level.WARNING);
        }
        return enabled;
    }
    
    /**
     * Return the password of user from her alias
     * @param ivAlias User alias
     * @return The password
     */
    public final static String getPwdByAlias(String ivAlias){
        String pwd = null;
        try{
            ResultSet rs = statement.executeQuery( "SELECT password FROM users WHERE alias = '" + ivAlias + "'" );
            if( rs.next() )
                pwd = rs.getString( "user_pwd" );
        }catch( Exception e ){
            log.debug(client, e, Logger.Level.WARNING);
        }
        return pwd;
    }
    
    /**
     * Verify if specified user exist
     * @param ivAlias User alias
     * @return true if user exist
     */
    public final static boolean UserExist(String ivAlias){
        boolean isUser = false;
        try{
            ResultSet rs = statement.executeQuery( "SELECT name FROM users WHERE alias = '" + ivAlias + "'" );
            if( rs.next() )
                isUser = true;
        }catch( Exception e ){
            log.debug(client, e, Logger.Level.WARNING);
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
            if(ld.isLocalDomain( MessageHandlerFactory.getDomain(userEmail) )){
                ResultSet rs = statement.executeQuery( "SELECT * FROM users WHERE name = '" + userEmail + "'" );
                if(rs.next())
                    _return = true;
                else
                    log.debug(client, userEmail + " does not exist", Logger.Level.INFO);
            }else{
                _return = true;
            }
        }catch(Exception e){
            log.debug(client, e, Logger.Level.WARNING);
            _return = false;
        }
        return _return;
    }
    
    @SuppressWarnings("static-access")
    public void setStatement(Statement st) {
        this.statement = st;
    }
    
}
