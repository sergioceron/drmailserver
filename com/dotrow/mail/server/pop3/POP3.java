/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server.pop3;
/*
 * POP3.java
 *
 * Created on 18 de junio de 2006, 12:37
 */

import com.dotrow.mail.server.Logger;
import com.dotrow.mail.server.MessageHandler;
import com.dotrow.mail.server.Settings;
import com.dotrow.mail.server.UserHandler;

import java.sql.Connection;
import java.sql.Statement;
/**
 * POP3 Main Service
 * @author Sergio Ceron Figueroa
 */
public class POP3 {

    /** Instancia actual de la clase Logger */
    private static Logger log = Logger.getInstance();
    
    /** Instancia actual de la clase Settings */
    private static Settings set = Settings.getInstance();
    
    /** Constructor de la clase POP3 */
    public POP3() {
    }
    
    /**
     * Main POP3 server
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Statement st = connect( set.getStrKey( "Database.host" ) , set.getStrKey( "Database.name" ), set.getStrKey( "Database.user" ), set.getStrKey( "Database.password" ) );
        
        UserHandler.newInstance().setStatement(st);
        MessageHandler.getMessageHandler().setStatement(st);
        
        POP3Server server = new POP3Server( set.getIntKey( "POP3.port" ) );
        server.start();
        server.setStatement(st);
        log.debug("POP3 server started", Logger.Level.INFO);
        log.debug("Waiting for clients", Logger.Level.INFO);
    }
    
    /**
     * Genera una conexion a la base de datos especificada (Solo MYSQL)
     * @param server Nombre/IP del servidor
     * @param db Nombre de la base de datos
     * @param user Usuario
     * @param pass Contraseña
     * @return El asentamiento de la conexion
     */
    final static protected Statement connect(String server, String db, String user, String pass){
        java.sql.Statement select = null;
        try{
            String url = "jdbc:mysql://" + server + "/" + db;
            String driver = "com.mysql.jdbc.Driver";
            Connection con = null;
            Class.forName( driver ).newInstance();
            con=java.sql.DriverManager.getConnection( url, user, pass );
            select= con.createStatement();
        }catch(Exception e){
            select = null;
            log.debug(e, Logger.Level.ERROR);
        }
        return select;
    }
    
}
