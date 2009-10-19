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

public class DemoResultsBase extends ResultsBase {

    /** All the data that are meant to be persistent between requests
     * in the same session */
    public DemoSessionData sd;

    public DemoResultsBase(HttpServletRequest _request) {
	super(_request);
	try {
	    sd = DemoSessionData.getDemoSessionData(request);	    
	}  catch (Exception _e) {
	    setEx(_e);
	}
	
    }
}
