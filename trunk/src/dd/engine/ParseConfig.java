package dd.engine;

import java.io.*;
import java.util.*;


/**
 * 
 * This class is used to obtain configuration paramters, from a configuration
 * file or from Java System Properties.  If this is an applet,
 * SecurityException is caught safely.
 * 
 * @author Qin Shi
 * @author Vladimir Menkov
 *
 <P>
 * 1999-2004 
 */

public final class ParseConfig extends Hashtable<String,Object> {
	final static String prefix = ""; // "Ant."

	/**
	 * Creates an empty hashtable. That can be used simply as a convenient interface for accessing Java system options.
	 */
	public ParseConfig() {
	}

	/**
	 * Creates a hashtable that contains the parsed contents of the specified configuration file.
	 * @param aFname Configuration file name.	 
	 */

	public ParseConfig(String aFname) throws FileNotFoundException, IOException {
		this(new FileReader(aFname));
	}

	/**
	 * Creates a hashtable that contains the parsed data obtained from an open reader (which may, for example, be associated with an open file), 
	 * and then closes the reader.
	 * <p>
	 * The configuration file syntax:
	 * <ul>
	 * <li> Lines (or "tails" of lines) beginning with a '#' are comments, and
	 * are ignored
	 * <li> Blank lines are ignored
	 * <li> A line of the form
	 * <pre>
	 * 	name  value
	 * </pre>
	 * or
	 * <pre>
	 * 	name = value
	 * </pre>
	 * assigns a value to the named variable. The equal sign is optional. There can be a semicolon at the end of the line, but it's optional.
	 * The value may be a number (with no quotes), or a string surrounded by double quotes. If the string consists only of alphanumeric characters,
	 * with possible '/' and ':' chars, then quotes are optional too.
	 * </ul>
	 * <p>
	 * A ParseConfig structure is created by reading a specified configuration file. 
	 * The values of parameters stored in the table can be accessed by using accessor methods, such as getOption or getOptionDouble.
	 * <p>
	 * This method throws various exceptions, so that the caller method could produce a meaningful error report.
	 * @param in A Reader (a file reader, etc.)
	 */
	public ParseConfig(Reader in) throws IOException {
		// create an underlying hash table
		super(20);
		String param = "";
		String lastName = "N/A";

		try {
			StreamTokenizer token = new StreamTokenizer(in);

			// Semicolns are completely and utterly ignored
			token.whitespaceChars((int) ';', (int) ';');

			// These characters often appear in URLs. 
			// They should be treated as word chars, so that URLs would be "words" and wouldn't need to be quoted
			token.wordChars((int) '/', (int) '/');
			token.wordChars((int) ':', (int) ':');
			token.wordChars((int) '.', (int) '.');
			token.wordChars((int) '_', (int) '_');

			// Comments begin with a '#', not '//'
			token.slashSlashComments(false);
			token.commentChar('#');
			token.eolIsSignificant(false);

			// read the name
			while (token.nextToken() != token.TT_EOF) {
				String name = "";
				if (token.ttype == token.TT_WORD) {
					name = token.sval;
					lastName = name;
				} else {
					throw new IOException("Syntax error in config file: A WORD token expected for a parameter name. The last parmeter read was `" + lastName + "'");
				}

				// read the value 
				if (token.nextToken() == token.TT_EOF) {
					throw new IOException("Syntax error in config file: No value for" + name);
				}

				if (token.ttype == (int) '=') {
					// This just was an optional equal sign. The value must be * in the next token.
					if (token.nextToken() == token.TT_EOF) {
						throw new IOException("Syntax error in config file: No value found for"	+ name);
					}
				}

				Object value = null;
				if (token.ttype == token.TT_WORD || token.ttype == '"') {
					// a String 
					value = token.sval;
				} else if (token.ttype == token.TT_NUMBER) {
					// A number
					value = new Double(token.nval);
				} else {
					System.err.println("Syntax error in config file: unexpected value token type " + token.ttype);
					continue;
				}
				
				// store in the hashtable
				put(name, value);
			}
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Looks up the system property. 
	 * Returns the value, or null if the property is not found or if look up fails with a security exception because we're in an applet.
	 */
	private String getPropertySafe(String name) {
		String property = null;
		try {
			property = System.getProperty(name);
		} catch (SecurityException e) {
			// We must be in an applet, and system properties are not available. Ignore the problem.
		}
		return property;
	}

	/**
	 * Gets the requested value from the hash table or from the Java system property aName.
	 * The Java system property, if given, overrides the value from the hash table.
	 */
	public String getOption(String aName, String aDefault) {
		String value = aDefault;
		Object obj = get(aName);
		
		if (obj != null) {
			if (obj instanceof String)
				value = (String) obj;
			else if (obj instanceof Number) {
				String msg = "Property `" + aName + "' read from the config file " + "should be a string, not a number! Ignoring.";
				System.err.println(msg);
			} else {
				String msg = "Property `" + aName + "' read from the config file " + "is not a String";
				System.err.println(msg);
			}
		} else {
		
		    String property = getPropertySafe(prefix + aName);
		    if (property != null)
			value = property;
		}
		return value;
	}

	/**
	 * Gets the requested double value from the hash table or from the Java system property aName.
	 */
	public double getOptionDouble(String aName, double aDefault) {
		double value = aDefault;
		Object obj = get(aName);
		if (obj != null) {
			if (obj instanceof Number)
				value = ((Number) obj).doubleValue();
			else {
				String msg = "Property `" + aName + "' read from the config file " + "is not a number! Ignored.";
				System.err.println(msg);
			}
		} else {
		    String property = getPropertySafe(prefix + aName);
		    if (property != null)
			value = Double.parseDouble(property);
		}
		return value;
	}

	public void setOption(String aName, double val) {
	    put(aName, new Double(val));
	    System.out.println("Recorded option "+aName+ "=" + val);
	    System.out.println("get("+aName+ ")=" + get(aName));
	    
	}
	
	/**
	 * Gets the requested integer value from the hash table or from the Java system property aName.
	 */
	public int getOption(String aName, int aDefault) {
		int value = aDefault;
		Object obj = get(aName);
		if (obj != null) {
			if (obj instanceof Number)
				value = ((Number) obj).intValue();
			else {
				String msg = "Property `" + aName + "' read from the config file " + "is not a number! Ignored.";
				System.err.println(msg);
			}
		} else {
		    String property = getPropertySafe(prefix + aName);
		    if (property != null)
			value = Integer.parseInt(property);
		}
		return value;
	}

	/**
	 * Gets the requested integer value from the hash table or from the Java system property aName.
	 */
	public boolean getOption(String aName, boolean aDefault) {
		boolean value = aDefault;
		Object obj = get(aName);
		if (obj != null) {
			if (obj instanceof String) {
				String v = (String) obj;
				value = (new Boolean(v)).booleanValue();
			} else {
				String msg = "Property `" + aName + "' read from the config file " + "is not a boolean! Ignored.";
				System.err.println(msg);
			}
		} else {
		    String property = getPropertySafe(prefix + aName);
		    if (property != null)
			value = (new Boolean(property)).booleanValue();
		}
		return value;
	}

	/**
	 * Gets the requested value from the hash table. If the value is not found, IOException is thrown.
	 */
	public String getParameter(String aName) throws IOException {
		String value = null;
		Object obj = get(aName);
		if (obj != null) {
			if (obj instanceof String)
				return (String) obj;
			else if (obj instanceof Number)
				return "" + ((Number) obj).intValue();
			else {
				throw new IOException("Invalid type for parameter " + aName);
			}
		} else {
			throw new IOException("Missing parameter " + aName);
		}
	}

	/** 
	 * Purely for testing.
	 */
	static public void main(String argv[]) throws FileNotFoundException, IOException {
		for (int i = 0; i < argv.length; i++) {
			System.out.print("Reading " + argv[i]);
			ParseConfig ht = new ParseConfig(argv[i]);
			for (Enumeration keys = ht.keys(); keys.hasMoreElements();) {
				String name = (String) keys.nextElement();
				Object value = ht.get(name);
				System.out.print("h[" + name + "] = ");
				if (value instanceof Number) {
					System.out.println(" number(" + ((Number) value).doubleValue() + ")");
				} else if (value instanceof String) {
					System.out.println(" string(" + (String) value + ")");
				}
			}
		}
	}
}
