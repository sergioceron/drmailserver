/*
 * MessageError.java
 *
 * Created on 17 de junio de 2006, 12:33
 */

import java.text.*;
import java.util.*;

/**
 * Structure of mail error message inheritance Message
 * @author Sergio Ceron Figueroa
 */
public class MessageError extends Message {
    /**
     * Formatter date
     */    
    private SimpleDateFormat Formatter = new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss (z)", Locale.US );
    
    /**
     * Mail error or cause.
     */
    private String error;
    
    /** Creates a new instance of MessageError */
    public MessageError() {
    }
    
    /**
     * Create a new Message error for deliver
     * @return A formated data message error
     */
    public String getUndeliveredMessage(){
        String ErrMsg = "Sended by DotRow Mail Server ( SMTP )\r\n";
        ErrMsg += "From: Mail Delivery Subsystem <Mailer-Daemon@dotrow.com>\r\n";
        ErrMsg += "Reply-To: " + UserHandler.newInstance().getAliasByMail( getFrom() ) + " <" + getFrom() + ">\r\n";
        ErrMsg += "To: " + UserHandler.newInstance().getAliasByMail( getFrom() ) + " <" + getFrom() + ">\r\n";
        ErrMsg += "Subject: Delivery Status Notification (Failure)\r\n";
        ErrMsg += "Date: " + Formatter.format( new Date() ) + "\r\n";
        ErrMsg += "MIME-Version: 1.0\r\n";
        ErrMsg += "Content-Type: multipart/mixed;\r\n";
        ErrMsg += "     boundary=\"----=_NextPart_000_0001.DR\"\r\n";
        ErrMsg += "Importance: High\r\n\r\n";
        ErrMsg += "----=_NextPart_000_0001.DR\r\n";
        ErrMsg += "Content-Type: text/html;\r\n";
        ErrMsg += "     charset=\"iso-8859-1\"\r\n";
        ErrMsg += "Content-Transfer-Encoding: 7bit\r\n\r\n";
        ErrMsg += "This is an automatically generated Delivery Status Notification\r\n\r\n";
        ErrMsg += "Delivery to the following recipient failed permanently:\r\n\r\n";
        ErrMsg += getTo() +"\r\n\r\n";
        ErrMsg += "Technical details of permanent failure:\r\n";
        ErrMsg += "PERM_FAILURE: SMTP Error : " + getError() + "\r\n\r\n";
        ErrMsg += "---- Original Message ----\r\n\r\n";
        ErrMsg += getData();
        ErrMsg += "\n---- Message truncated ----\n";
        ErrMsg += "------=_NextPart_000_0001.DR\r\n";
        return ErrMsg;
    }
    
    /**
     * Getter for mail error.
     * @return Mail error.
     */
    public String getError() {
        return this.error;
    }
    
    /**
     * Setter for Mail error.
     * @param error New value of Mail error.
     */
    public void setError(String error) {
        this.error = error;
    }
    
}
