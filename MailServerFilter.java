/*
 * MailFilter.java
 *
 * Created on 19 de junio de 2006, 18:11
 */

import java.sql.*;
import java.net.*;
import java.io.*;
/**
 * Filtra los correos spam o por ip
 * @author Sergio Ceron Figueroa
 */
public class MailServerFilter implements DatabaseInterface {
    
    private static MailServerFilter _instance = null;
    
    /** Configuraciones del servidor */
    private static Settings set = Settings.getInstance();
    
    /** Instancia activa del logger */
    private Logger log = Logger.getInstance();
    
    /** Asentamiento de la base de datos */
    private static Statement st = null;
    
    /** Crea una nueva instancia */
    private MailServerFilter() {}
    
    /**
     * Regresa la instancia activa de MailServerFilter o crea una nueva si no existe
     * @return una intancia de la clase MailServerFilter
     */
    final static public MailServerFilter getInstance(){
        return _instance != null ? _instance : new MailServerFilter();
    }
    
    /**
     * Verifica si el filtro por ip esta activo
     * @return si el filtro esta activo
     * <I>true</I> si esta activo
     * <I>alse</I> si esta deshabilitado
     */
    protected boolean enabled(){
        Boolean en = Boolean.valueOf( set.getStrKey( "MailServerFilter.enabled" ) );
        return en.booleanValue();
    }
    
    /**
     * Verifica si la ip tiene un nombre de resolucion inversa, asi se determina si la ip pertenece a algun dominio
     * @return si se permite que la ip envie correo
     * <I>true</I> si si coincide la ip con el dominio
     * <I>true</I> si no coincide la ip con el dominio
     * @param ip  la ip para verificar con el dominio
     * @param domain el dominio que debe coincidir con la ip en la resolucion inversa
     */
    protected boolean allowReciveMail( String ip, String domain ){
        NSLookup nslu = NSLookup.getInstance();        
        if( nslu.reverseLookup( ip ).indexOf( domain ) != 0 )
            return true;
        return false;
    }
    
    /**
     * Filtra el correo que ha sido enviado con la base de datos de spam
     * @return si el correo es aceptado y no esta en la base de datos
     * <I>true</I> si el correo es aceptado
     * <I>false</I> si el correo es spam
     * @param from el dominio de donde proviene el correo
     * @param to el usuario a quien va dirigido
     */
    protected boolean filterMailToSpam( String from, String to ){
        boolean allow = true;
        if( UserHandler.newInstance().isFilterEnabled( to ) ){
            try{
                ResultSet rs = st.executeQuery( "SELECT * FROM dr_deny_mails WHERE deny_mails_user = '" + to + "' and deny_mails_mail = '" + from + "'" );
                if( rs.next() )
                    allow = false;
            }catch(Exception e){
                log.SDebug( e, 0 );
            }
        }
        return allow;
    }
    
  
    /**
     * Actualiza el asentamiento de la base de datos por medio de la interface
     * @param st el asentamiento de la base de datos
     */
    public void dbStatement(java.sql.Statement st) {
        MailServerFilter.st = st;
    }
    
    
}
