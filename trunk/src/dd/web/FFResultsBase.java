package dd.web;

//import java.net.*;
//import java.sql.*;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

//import org.apache.commons.fileupload.*;
//import org.apache.commons.fileupload.servlet.*;
//import org.apache.commons.fileupload.disk.*;


//import dd.engine.*;
//import dd.gui.*;

/** The base class for the classes that are behind the Frontier Finder
 * Lite JSP pages.
 */
public class FFResultsBase extends ResultsBase {

    /** All the data that are meant to be persistent between requests
     * in the same session */
    public FFSessionData sd;

    /** The value of param "id", if supplied */
    public int id = -1;

    public FFResultsBase(HttpServletRequest _request) {
	super(_request);
	try {
	    sd = FFSessionData.getFFSessionData(request);	    

	    String s = request.getParameter("id");
	    if (s!=null) {
		try {
		    id = Integer.valueOf(s);
		} catch(Exception ex) {}
	    }

	}  catch (Exception _e) {
	    setEx(_e);
	}
	
    }
}
