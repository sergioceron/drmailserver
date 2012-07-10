/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server.smtp;
/*
 * SMTPResponse.java
 *
 * Created on 14 de junio de 2006, 10:47
 */

/**
 * Response SMTP codes
 * @author Sergio Ceron Figueroa
 */
public enum SMTPResponse {

      RC200("200 ( nonstandard success response, see rfc876)"),
      RC211("211 System status, or system help reply"),
      RC214("214 Help message"),
      RC220("220 DotRow Service ready"),
      RC221("221 DotRow Service closing transmission channel"),
      RC250("250 Requested mail action okay, completed"),
      RC251("251 User not local), will forward to <forward-path>"),
      RC354("354 Start mail input), end with ."),
      RC421("421 DotRow Service not available, closing transmission channel"),
      RC450("450 Requested mail action not taken: mailbox unavailable"),
      RC451("451 Requested action aborted: local error in processing"),
      RC452("452 Requested action not taken: insufficient system storage"),
      RC500("500 Syntax error, command unrecognised"),
      RC501("501 Syntax error in parameters or arguments"),
      RC502("502 Command not implemented"),
      RC503("503 Bad sequence of commands"),
      RC505("505 Unknow command"),
      RC504("504 Command parameter not implemented"),
      RC521("521 DotRow does not accept mail (see rfc1846)"),
      RC530("530 Access denied (???a Sendmailism)"),
      RC550("550 Requested action not taken: No such user. "),
      RC551("551 User not local), please try <forward-path>"),
      RC552("552 Requested mail action aborted: exceeded storage allocation"),
      RC553("553 Requested action not taken: mailbox name not allowed"),
      RC554("554 Transaction failed");

    private String message;

    private SMTPResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
