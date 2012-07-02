/*
 * POP3.java
 *
 * Created on 18 de junio de 2006, 12:37
 */

import java.sql.*;
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
        
        UserHandler.newInstance().dbStatement( st );
        MessageHandler.getMessageHandler().dbStatement( st );
        
        POP3Server server = new POP3Server( set.getIntKey( "POP3.port" ) );
        server.start();
        server.dbStatement( st );
        log.SDebug("POP3 server started", 2);
        log.SDebug("Waiting for clients", 2);
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
            log.SDebug( e, 0 );
        }
        return select;
    }
    
}
