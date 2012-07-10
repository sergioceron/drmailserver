/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server;
/*
 * Settings.java
 *
 * Created on 19 de junio de 2006, 17:55
 */

import java.util.ResourceBundle;
/**
 * Settings Handler
 * @author Sergio Ceron Figueroa
 */
public class Settings {
    
    private static Settings _instance = null;
    /** Creates a new instance of Settings */
    private Settings() {}
    
    final public static Settings getInstance(){
        return _instance != null ? _instance : new Settings();
    }
    
    /**
     * Return a Integer Key value of Resource Bundle
     * @param key String key
     * @return Integer value
     */
    public int getIntKey(String key){
        String Key = ResourceBundle.getBundle("MailConfig").getString( key );
        return Integer.parseInt( Key );
    }
    
    /**
     * Return a String Key value of Resource Bundle
     * @param key String key
     * @return String value
     */
    public String getStrKey(String key){
        return ResourceBundle.getBundle("MailConfig").getString( key );
    }
    
}
