/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server;
/*
 * Logger.java
 * @author Sergio Ceron Figueroa
 * Created on 13 de junio de 2006, 15:58
 */

import com.dotrow.mail.server.util.VT100;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class Logger {

    /**
     * Tipos de error segun su prioridad
     */
	public static enum Level { 
        ERROR   ("ERROR"),
        WARNING ("WARN "),
        INFO    ("INFO ");

        private String key;

        private Level(String key){
            this.key = key;
        }
        
        public String getKey(){
            return key;
        }
    };

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
    
    private VT100 consola;
    
    /** Crea una nueva instacia y abre el archivo donde se guardara el log */
    private Logger() {
        try{
            openLog( settings.getStrKey( "Logger.File" ) );
        }catch( Exception e ){
            e.printStackTrace();
        }
        consola = new VT100(System.out);
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
     * @param level la prioridad del mensaje
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
    public void debug(Thread ct, Object trace, Level level){
        Date date = new Date();
        try{
            consola.setColor(VT100.Color.WHITE);
            switch( level ){
                case ERROR:
                    consola.setBackground(VT100.Color.RED);
                    break;
                case WARNING:
                    consola.setBackground(VT100.Color.YELLOW);
                    break;
                case INFO:
                    consola.setColor(VT100.Color.BLACK);
                    consola.setBackground(VT100.Color.GREEN);
                    break;
            }
            consola.print(String.format("<%s>:", level.getKey()));
            consola.reset();
            consola.print( " ["+ ct +"] --> " + trace + "\n");
            consola.flush();
            writeLog( ct + "["+ date +"] --> " + level.getKey() + trace );
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
    
    /**
     * Almacena los mensajes producidos por el servidor y los imprime en la consola
     * @param trace el mensaje que se imprimira, puede ser un string o una excepcion
     * @param level la prioridad del mensaje
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
    public void debug(Object trace, Level level){
        Date date = new Date();
        try{
            consola.setColor(VT100.Color.WHITE);
            switch( level ){
                case ERROR:
                    consola.setBackground(VT100.Color.RED);
                    break;
                case WARNING:
                    consola.setBackground(VT100.Color.YELLOW);
                    break;
                case INFO:
                    consola.setColor(VT100.Color.BLACK);
                    consola.setBackground(VT100.Color.GREEN);
                    break;
            }
            consola.print(String.format("<%s>:", level.getKey()));
            consola.reset();
            consola.print( " [Server] --> " + trace + "\n" );
            consola.flush();
            writeLog(  "Server ["+ date  +"] --> " + level.getKey() + trace );
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
