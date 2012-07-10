/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server;
/*
 * Email.java
 *
 * Created on 16 de junio de 2006, 14:10
 */

/**
 * Mensaje de correo
 * @author Sergio Ceron Figueroa
 */
public class Email {
    
    /**
     * Tipo de mensaje: Normal
     */    
    final static int MSGTYPE_NORMAL = 0;
    /**
     * Tipo de mensaje: Error
     */    
    public final static int MSGTYPE_ERROR = 1;
    /**
     * Folder de mensaje: Bandeja de entrada
     */    
    public final static int MSGFOLDER_INBOX = 0;
    /**
     * Folder de mensaje: Correo no deseado
     */    
    final static int MSGFOLDER_SPAM = 1;
    
    /**
     * Mail to
     */
    private String to;
    
    /**
     * Usuario que envia el correo
     */
    private String from;
    
    /**
     * Cuerpo del mensaje
     */
    private String data;
    /**
     * Tipo de mensaje
     */
    private int type;
    /**
     * Folder del mensaje
     */
    private int folder;
    /**
     * Id del mensaje
     */
    private int id;    
    
    /** Crea una nueva instancia de la clase Email */
    public Email() {
    }
    
    /**
     * Regresa a quien va el correo
     * @return el usuario que se enviara el correo
     */
    public String getTo() {
        return this.to;
    }    
    
    /**
     * Establece para quien es el correo
     * @param to a quien va el correo
     */
    public void setTo(String to) {
        this.to = to;
    }    
    
    /**
     * Regresa el usuario que envia el correo
     * @return el usuario que envia el correo
     */
    public String getFrom() {
        return this.from;
    }
    
    /**
     * Establece quien envia el correo
     * @param from quien envia el correo
     */
    public void setFrom(String from) {
        this.from = from;
    }
    
    /**
     * Regresa los datos del correo
     * @return una cadena de caracteres, que es el cuerpo del correo
     */
    public String getData() {
        return this.data;
    }
    
    /**
     * Establece el cuerpo del mensaje
     * @param data el contenido del correo
     */
    public void setData(String data) {
        this.data = data.replaceAll("'", "`");
    }
    
    /**
     * Regresa el tipo de correo
     * @return tipo de correo     */
    public int getType() {
        return this.type;
    }
    
    /**
     * Establece el tipo de correo
     * @param type tipo de correo
     */
    public void setType(int type) {
        this.type = type;
    }
    
    /**
     * Regresa el folder del correo
     * @return el folder del correo
     */
    public int getFolder() {
        return this.folder;
    }
    
    /**
     * Establece el folder del correo
     * @param folder el folder del correo
     */
    public void setFolder(int folder) {
        this.folder = folder;
    }
    
    /**
     * Regresa el tamño del correo
     * @return tamaño en bytes del correo
     */
    public int getSize() {
        return this.data.length();
    }
    
    /**
     * Regresa el identificador del correo
     * @return el identificador del correo
     */
    public int getId() {
        return this.id;
    }
    
    /**
     * Establece un identificador para el correo
     * @param id el identificador para el correo
     */
    public void setId(int id) {
        this.id = id;
    }
    
}
