/*
 * MailerDaemon.java
 *
 * Created on 16 de junio de 2006, 14:15
 */

import java.util.*;
import java.sql.*;
import java.text.*;
/**
 * Tarea que se encarga de distribuir los correos para enviar, lo que son errores o en espera
 * @author Sergio Ceron Figueroa
 */
public class MailerDaemon extends Thread implements DatabaseInterface{
    
    /** Instancia del logger */
    private Logger log = Logger.getInstance();
    
    /** Instancia activa de la clase  */
    private static MailerDaemon _instance = null;
    
    /** Vector que almacena los correos a enviar */
    private static Vector MailsToSend = new Vector();
    
    /** Vector que almancena los correos en espera */
    private static Vector MailsOnQueue = new Vector();
    
    /** Asentamiento de la base de datos */
    private static Statement st = null;
    
    /** Configuraciones del servidor */
    private static Settings set = Settings.getInstance();
    
    /** Bandera que manipula el estado de la tarea */
    private boolean running = true;
            
    /** Crea una nueva instancia de MailerDaemon */
    private MailerDaemon() { }
    
    /**
     * Regresa la instancia activa de MailerDaemon, si no existe crea una nueva
     * @return una instancia de MailerDaemon
     */
    final static public MailerDaemon getInstance(){
        return _instance == null ? new MailerDaemon() : _instance;
    }
    
    /**
     * Inicia la ejecucion de la tarea
     */
    @Override
    public void run(){
        try{
            log.SDebug("Mailer Daemon started", 2);
            while( running ){
                
                if( MailsToSend.size() > 0 )
                    for( int mts = 0; mts < MailsToSend.size(); mts ++ ){
                        Sender MailTS = new Sender();
                        MailTS.start();
                        MailTS.setMessage( ( Message )MailsToSend.get( mts ) );
                        MailsToSend.remove( mts );
                    }
                
                if( MailsOnQueue.size() > 0 )
                    for( int moq = 0; moq < MailsOnQueue.size(); moq ++ ){
                        Sender MailTS = new Sender();
                        MailTS.setQueueTimeout( set.getIntKey( "Queue.timeout" ) );
                        MailTS.setMessage( ( Message )MailsOnQueue.get( moq ) );
                        MailTS.start();
                        MailsOnQueue.remove( moq );
                    }
                sleep( 100 );
            }
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
    
    
    @Override
    public void destroy(){
        this.running = false;
    }
    
    /**
     * Guarda un correo en la base de datos,esto sucede cuando el correo es recivido o enviado a un usuario local
     * @return si el correo ha sido guardado con exito
     *
     * <I>true</I> si el correo fue guardado correctamente
     * <I>false</I> si ocurrio un error al intentar guardar el archivo
     * @param user el usuario al que pertenece el correo
     * @param data el cuerpo del mensaje
     * @param folder el foder donde se almancenara el mensaje
     *
     * <I>0</I> --> Bandeja de entrada
     * <I>1</I> --> Correo basura
     */
    public boolean SaveMail(String user, String data, int folder){
        boolean _return = false;
        try{
            st.execute( "INSERT INTO dr_mails(mail_user, mail_data, mail_date, mail_folder) VALUES('" + user + "', '" + data + "', '" + getDate("yyyy-MM-dd") + "', '" + folder + "')" );
            log.TDebug( this, "Mail Saved", 2 );
            _return = true;
        }catch(Exception e){
            log.TDebug( this, e, 1);
            _return = false;
        }
        return _return;
    }
    
    /**
     * Regresa la fecha actual con cierto formato
     * @return la fecha formateada
     * @param format el formato de la fecha
     * Ejemplo
     * <CODE>25/01/2005 20:30:20</CODE>
     */
    final private String getDate( String format ){
        java.util.Date date = new java.util.Date();
        Format formatter = new SimpleDateFormat( format );
        String fDate = formatter.format( date );
        return fDate;
    }
    
    /**
     * Agrega un mensaje a la lista de correos a enviar
     * @param msg el mensage a enviar
     * @see Message
     */
    public void addMailToSend( Message msg ) {
        MailsToSend.add( msg );
    }
    
    /**
     * Agrega un mensaje a la lista de correos en espera
     * @param msg el mensage a enviar
     * @see Message
     */
    public void addMailOnQueue( Message msg ) {
        MailsOnQueue.add( msg );
    }
    
    /**
     * Actualiza el asentamiento de la base de datos por medio de la interface
     * @param st el asentamiento de la base de datos
     */
    @SuppressWarnings("static-access")
    public void dbStatement(java.sql.Statement st) {
        this.st = st;
    }
    
}
