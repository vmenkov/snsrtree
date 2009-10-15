package dd.web;

//import java.net.*;
//import java.sql.*;

import java.io.*;
import java.util.*;
import dd.engine.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.disk.*;

import dd.engine.*;
import dd.gui.*;

public class ResultsBase {

    HttpServletRequest request;
    //HttpSession session;

    /** All the data that are meant to be persistent between requests
     * in the same session */
    public DemoSessionData sd;

    /** Will be set to true if an error happened */
    public boolean error = false;
    /** The JSP page should print this out if error==true */
    public String errmsg="[No message]";
    /** The JSP page may print this out if it is not null. */
    public Exception e=null;

    /** The JSP page should always print this message. Most often
        it is just an empty string, anyway; but it may be used
        for debugging and status messages. */
    public String infomsg = "";

    public ResultsBase(HttpServletRequest _request) {
	try {
	request = _request;

	sd = DemoSessionData.getDemoSessionData(request);

	infomsg+= "<br>Plain params:";
	for(Enumeration en=request.getParameterNames(); en.hasMoreElements();){
	    String name = (String)en.nextElement();
	    infomsg += "<br>"+name + "=" + request.getParameter(name);	    
	}
	    
	}  catch (Exception _e) {
	    e = _e;
	    error = true;
	    errmsg = "Error: " + e.getMessage();
	}
	
    }

    /** Returns the exception's stack trace, as a plain-text string 
     */
    public String exceptionTrace() {	
	StringWriter sw = new StringWriter();
	try {
	    if (e==null) return "No exception was caught";
	    e.printStackTrace(new PrintWriter(sw));
	    sw.close();
	} catch (IOException ex){}
	return sw.toString();
    }

}
