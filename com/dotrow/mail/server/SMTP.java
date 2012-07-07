package com.dotrow.mail.server;
/*
 * SMTP.java
 *
 * Created on 13 de junio de 2006, 15:47
 */

import java.sql.Connection;
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
	private static Settings set = Settings.getInstance();

	/** Constructor de la clase SMTP */
	public SMTP() {
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		// Create Statemet of database
		Statement st = connect(set.getStrKey("Database.host"),
				set.getStrKey("Database.name"), set.getStrKey("Database.user"),
				set.getStrKey("Database.password"));

		// Start SMTP Server
		SMTPServer server = new SMTPServer(set.getIntKey("SMTP.port"));
		server.start();
		log.debugServerThread("SMTP Server started", 2);

		server.dbStatement(st);

		// Add statement to MailServerFilter
		MailServerFilter.getInstance().dbStatement(st);

		// Add statement to user handler
		UserHandler.newInstance().dbStatement(st);

		// Start Mailer Daemin Service
		MailerDaemon Md = MailerDaemon.getInstance();
		Md.start();

		Md.dbStatement(st);

		log.debugServerThread("Waiting for clients", 2);
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
	final static protected Statement connect(String server, String db,
			String user, String pass) {
		java.sql.Statement select = null;
		try {
			String url = "jdbc:mysql://" + server + "/" + db;
			String driver = "com.mysql.jdbc.Driver";
			Connection con = null;
			Class.forName(driver).newInstance();
			con = java.sql.DriverManager.getConnection(url, user, pass);
			select = con.createStatement();
		} catch (Exception e) {
			select = null;
			log.debugServerThread(e, 0);
		}
		return select;
	}
}
