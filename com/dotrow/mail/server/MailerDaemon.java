/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server;
/*
 * MailerDaemon.java
 *
 * Created on 16 de junio de 2006, 14:15
 */

import java.sql.Statement;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;
/**
 * Tarea que se encarga de distribuir los correos para enviar, lo que son errores o en espera
 * @author Sergio Ceron Figueroa
 */
public class MailerDaemon extends Thread implements DatabaseConnection {
    
    /** Instancia del logger */
    private Logger log = Logger.getInstance();
    
    /** Instancia activa de la clase  */
    private static MailerDaemon _instance = null;
    
    /** Vector que almacena los correos a enviar */
    private static Vector<Email> mailsToSend = new Vector<Email>();
    
    /** Vector que almancena los correos en espera */
    private static Vector<Email> mailsOnQueue = new Vector<Email>();
    
    /** Asentamiento de la base de datos */
    private static Statement statement = null;
    
    /** Configuraciones del servidor */
    private static Settings settings = Settings.getInstance();
    
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
            log.debug("Mailer Daemon started", Logger.Level.INFO);
            while( running ){
                if( mailsToSend.size() > 0 )
                    for( int mts = 0; mts < mailsToSend.size(); mts ++ ){
                        Sender sender = new Sender();
                        sender.start();
                        sender.setMessage(mailsToSend.get(mts));
                        mailsToSend.remove( mts );
                    }
                
                if( mailsOnQueue.size() > 0 )
                    for( int moq = 0; moq < mailsOnQueue.size(); moq ++ ){
                        Sender sender = new Sender();
                        sender.setQueueTimeout(settings.getIntKey("Queue.timeout"));
                        sender.setMessage(mailsOnQueue.get(moq));
                        sender.start();
                        mailsOnQueue.remove( moq );
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
    public boolean persistMail(String user, String data, int folder){
        try{
            statement.execute(MessageFormat.format("INSERT INTO emails(user_id, message, date, recipient_id) VALUES(''{0}'', ''{1}'', ''{2}'', ''{3}'')", user, data, getDate("yyyy-MM-dd"), folder));
            log.debug(this, "Mail Saved", Logger.Level.INFO);
            return true;
        }catch(Exception e){
            log.debug(this, e, Logger.Level.WARNING);
        }
        return false;
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
     * @see Email
     */
    public void addMailToSend( Email msg ) {
        mailsToSend.add(msg);
    }
    
    /**
     * Agrega un mensaje a la lista de correos en espera
     * @param msg el mensage a enviar
     * @see Email
     */
    public void addMailOnQueue( Email msg ) {
        mailsOnQueue.add(msg);
    }
    
    /**
     * Actualiza el asentamiento de la base de datos por medio de la interface
     * @param st el asentamiento de la base de datos
     */
    @SuppressWarnings("static-access")
    public void setStatement(java.sql.Statement st) {
        this.statement = st;
    }
    
}
