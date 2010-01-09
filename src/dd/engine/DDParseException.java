package dd.engine;

/** Just something to throw
 */

public class DDParseException extends DDException {
    public  DDParseException(CharSequence s, String msg) {
	super(msg + "; String being parsed = '" + s + "'");
    }
    public  DDParseException(String msg) {
	super(msg);
    }
}
