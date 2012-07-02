/*
 * LocalDomains.java
 *
 * Created on 14 de junio de 2006, 18:23
 */

import java.util.*;
/**
 * Local domains
 * @author Sergio Ceron Figueroa
 */
public class LocalDomains {
    static private LocalDomains _instance = null;
    
    /** instancia del logger */
    private Logger log = Logger.getInstance();
    
    /** crea una nueva instancia */
    private LocalDomains(){}
    
    /**
     * Regresa la instancia activa de LocalDomains, si no existe crea una nueva,
     * @return la instancia activa de LocalDOmains
     */    
    final static public LocalDomains getInstance() {
        return _instance == null ? new LocalDomains() : _instance;
    }
    
    /**
     * Lista de domonios locales
     * @return un vector con todos los dominios locales
     */    
    public Vector getLocalDomains(){
        Vector locald = new Vector();
        try{
            String lDomains = ResourceBundle.getBundle("DRMailServer/MailConfig").getString("Domains.Local");
            StringTokenizer ld = new StringTokenizer( lDomains, ";" );
            while( ld.hasMoreTokens() ){
                locald.add( ld.nextToken().toUpperCase() );
            }
        }catch(Exception e){
            log.SDebug(e, 1);
        }
        return locald;
    }
    
    /**
     * Verifica si el dominio especificado es local
     * @param domain dominio a verificar
     * @return true si el dominio es local, false si no lo es
     *
     * <I>true</I> si el dominio es local
     * <I>false</I> si el dominio no es local
     */    
    public boolean isLocalDomain( String domain ){
        boolean _return = false;
        for( int i = 0; i < getLocalDomains().size(); i++ ){
            if(getLocalDomains().get( i ).equals( domain ))
                _return = true;
        }
        return _return;
    }
}