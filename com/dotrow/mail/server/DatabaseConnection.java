/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server;
/*
 * DatabaseConnection.java
 *
 * Created on 14 de junio de 2006, 15:38
 */

import java.sql.Statement;

/**
 * Interface para compartir la conexion a la base de datos
 * @author Sergio Ceron Figueroa
 */
public interface DatabaseConnection {
    /**
     * Asigna el asentamiento de la conexion compartida
     * @param st el asentamiento de la conexion a la base de datos
     */    
    public void setStatement(Statement st);
}
