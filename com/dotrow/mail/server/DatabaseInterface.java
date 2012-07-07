package com.dotrow.mail.server;
/*
 * DatabaseConnection.java
 *
 * Created on 14 de junio de 2006, 15:38
 */

import java.sql.*;

/**
 * Interface para compartir la conexion a la base de datos
 * @author Sergio Ceron Figueroa
 */
public interface DatabaseInterface {
    /**
     * Asigna el asentamiento de la conexion compartida
     * @param st el asentamiento de la conexion a la base de datos
     */    
    public void dbStatement( Statement st );
}
