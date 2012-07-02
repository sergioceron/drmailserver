/*
 * LoggerInterface.java
 * @author sergio ceron fiigueroa
 * Created on 12 de diciembre de 2006, 09:45 AM
 */

/**
 * Interface para compartir el logger
 */
public interface LoggerInterface {
    void smtpText( String text );
    void pop3Text( String text );
}
