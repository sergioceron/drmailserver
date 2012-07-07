package com.dotrow.mail.server;
/*
 * Logger.java
 * @author Sergio Ceron Figueroa
 * Created on 13 de junio de 2006, 15:58
 */

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class Logger {
	
	public static enum LEVEL {ERROR, WARNING, INFO};
    /**
     * Tipos de error segun su prioridad
     */
    final static private String MSG_ERROR   = "Error : ";
    final static private String MSG_WARNING = "Warning : ";
    final static private String MSG_INFO    = "Info : ";
    /**
     * La instacia que sera creada y guardada en memoria
     */
    static private Logger _instance = null;
    /**
     * Las configuraciones del servidor
     */
    private final Settings settings = Settings.getInstance();
    /**
     * El buffer de entrada-salida que escribira los datos en el archivo
     */
    static private DataOutputStream dos = null;
    
    /** Crea una nueva instacia y abre el archivo donde se guardara el log */
    private Logger() {
        try{
            openLog( settings.getStrKey( "Logger.File" ) );
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
    
    /**
     * Regresa la instancia activa
     * @return una nueva instancia de la clase Logger si no existe, en caso contrario devuelve la activa
     */
    final static public Logger getInstance(){
         return _instance != null ? _instance : new Logger();
    }
    
    /**
     * Escribe los mensajes generados por el cliente y el servidor en un archivo de log
     * Si el archivo no existe se crea, de lo contrario se concatenan los datos al archivo
     * @param message una cadena de caracteres, la cual se escribira en el archivo
     */
    final private void writeLog( String message) throws Exception{
        Logger.dos.writeUTF( message );
    }
    /**
     * Crea un buffer de entrada-salida de datos
     * @param file una cadena de caracteres, la cual se escribira en el archivo
     */
    final private void openLog( String file ) throws Exception{
        File _file = new File( file );
        Logger.dos = new DataOutputStream( new FileOutputStream( _file, true ) );
    }
    /**
     * Cierra el buffer de entrada-salida de datos
     */
    final private void closeLog() throws Exception{
        Logger.dos.close();
    }
    
    /**
     * Almacena los mensajes producidos por un cliente y los imprime en la consola
     * @param ct Thread la tarea que produce el mensaje
     * @param trace el mensaje que se imprimira, puede ser un string o una excepcion
     * @param priority la prioridad del mensaje
     * <Ul>
     *    <li>
     *        0 --> Error
     *        1 --> Warning
     *        2 --> Info
     *    </li>
     *
     * </Ul>
     *
     * Ejemplo:
     * <CODE>
     *    Logger.getInstace().TDebug(this, "show info", 2 );
     * </CODE>
     */
    public void debugClientThread(Thread ct, Object trace, int priority){
        Date date = new Date();
        try{
            switch( priority ){
                case 0:
                    System.out.println( ct + "["+ date +"] --> " + MSG_ERROR + trace);
                    writeLog( ct + "["+ date +"] --> " + MSG_ERROR + trace );
                    break;
                case 1:
                    System.out.println( ct + "["+ date +"] --> " + MSG_WARNING + trace);
                    writeLog( ct + "["+ date +"] --> " + MSG_WARNING + trace );
                    break;
                case 2:
                    System.out.println( ct + "["+ date +"] --> " + MSG_INFO + trace);
                    writeLog( ct + "["+ date +"] --> " + MSG_INFO + trace );
                    break;
            }
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
    
    /**
     * Almacena los mensajes producidos por el servidor y los imprime en la consola
     * @param trace el mensaje que se imprimira, puede ser un string o una excepcion
     * @param priority la prioridad del mensaje
     * <Ul>
     *    <li>
     *        0 --> Error
     *        1 --> Warning
     *        2 --> Info
     *    </li>
     *
     * </Ul>
     *
     * Ejemplo:
     * <CODE>
     *    Logger.getInstace().SDebug( "show info", 2 );
     * </CODE>
     */
    public void debugServerThread( Object trace, int priority ){
        Date date = new Date();
        try{
            switch( priority ){
                case 0:
                    System.out.println( "Server ["+ date  +"] --> " + MSG_ERROR + trace );
                    writeLog(  "Server ["+ date  +"] --> " + MSG_ERROR + trace );
                    break;
                case 1:
                    System.out.println( "Server ["+ date  +"] --> " + MSG_WARNING + trace );
                    writeLog( "Server ["+ date  +"] --> " + MSG_WARNING + trace );
                    break;
                case 2:
                    System.out.println( "Server ["+ date  +"] --> " + MSG_INFO + trace );
                    writeLog( "Server ["+ date  +"] --> " + MSG_INFO + trace );
                    break;
            }
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
    
    /**
     * Destruye la instancia
     */
    public void destroy(){
        try{
            closeLog();
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
}
