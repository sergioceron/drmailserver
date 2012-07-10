/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server.smtp;
/*
 * SMTP.java
 *
 * Created on 13 de junio de 2006, 15:47
 */

import com.dotrow.mail.server.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SMTP Main Service
 * 
 * @author Sergio Ceron Figueroa
 */
public class SMTP {
	/** Instancia activa de la clase Logger */
	private static Logger log = Logger.getInstance();

	/** Instancia activa de la clase Settings */
	private static Settings settings = Settings.getInstance();

	/** Constructor de la clase SMTP */
	public SMTP() {
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
        try{
		// Create Statemet of database
		Statement st = connect(settings.getStrKey("Database.host"),
                settings.getStrKey("Database.name"), settings.getStrKey("Database.user"),
                settings.getStrKey("Database.password"));

		// Start SMTP Server
		SMTPServer smtpServer = new SMTPServer(settings.getIntKey("SMTP.port"));
        smtpServer.start();
		log.debug("SMTP Server started", Logger.Level.INFO);

		smtpServer.setStatement(st);

		// Add statement to MailServerFilter
		MailServerFilter.getInstance().setStatement(st);

		// Add statement to user handler
		UserHandler.newInstance().setStatement(st);

		// Start Mailer Daemin Service
		MailerDaemon mailerDaemon = MailerDaemon.getInstance();
		mailerDaemon.start();

		mailerDaemon.setStatement(st);

		log.debug("Waiting for clients", Logger.Level.INFO);
        } catch (Exception e) {
            log.debug(e, Logger.Level.ERROR);
        }
	}

	/**
	 * Genera una conexion a la base de datos especificada (Solo MYSQL)
	 * 
	 * @param server
	 *            Nombre/IP del servidor
	 * @param db
	 *            Nombre de la base de datos
	 * @param user
	 *            Usuario
	 * @param pass
	 *            Contraseña
	 * @return El asentamiento de la conexion
	 */
	final static protected Statement connect(String server, String db, String user, String pass)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
		String url = "jdbc:mysql://" + server + "/" + db;
		String driver = "com.mysql.jdbc.Driver";
		Class.forName(driver).newInstance();
        Connection con = java.sql.DriverManager.getConnection(url, user, pass);
		return con.createStatement();
	}
}
