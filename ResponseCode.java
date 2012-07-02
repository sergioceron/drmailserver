/*
 * ResponseCode.java
 *
 * Created on 14 de junio de 2006, 10:47
 */

/**
 * Response SMTP codes
 * @author Sergio Ceron Figueroa
 */
public class ResponseCode{
    
    /** Creates a new instance of ResponseCode */
    private ResponseCode() {}
    final static protected String RC200 = "200 ( nonstandard success response, see rfc876)";
    final static protected String RC211 = "211 System status, or system help reply";
    final static protected String RC214 = "214 Help message";
    final static protected String RC220 = "220 DotRow Service ready";
    final static protected String RC221 = "221 DotRow Service closing transmission channel";
    final static protected String RC250 = "250 Requested mail action okay, completed";
    final static protected String RC251 = "251 User not local; will forward to <forward-path>";
    final static protected String RC354 = "354 Start mail input; end with .";
    final static protected String RC421 = "421 DotRow Service not available, closing transmission channel";
    final static protected String RC450 = "450 Requested mail action not taken: mailbox unavailable";
    final static protected String RC451 = "451 Requested action aborted: local error in processing";
    final static protected String RC452 = "452 Requested action not taken: insufficient system storage";
    final static protected String RC500 = "500 Syntax error, command unrecognised";
    final static protected String RC501 = "501 Syntax error in parameters or arguments";
    final static protected String RC502 = "502 Command not implemented";
    final static protected String RC503 = "503 Bad sequence of commands";
    final static protected String RC505 = "505 Unknow command";
    final static protected String RC504 = "504 Command parameter not implemented";
    final static protected String RC521 = "521 DotRow does not accept mail (see rfc1846)";
    final static protected String RC530 = "530 Access denied (???a Sendmailism)";
    final static protected String RC550 = "550 Requested action not taken: No such user. ";
    final static protected String RC551 = "551 User not local; please try <forward-path>";
    final static protected String RC552 = "552 Requested mail action aborted: exceeded storage allocation";
    final static protected String RC553 = "553 Requested action not taken: mailbox name not allowed";
    final static protected String RC554 = "554 Transaction failed";
    
}
