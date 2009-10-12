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

/** Used by mainweb.jsp */
public class Mainweb extends ResultsBase {
 
    public Mainweb(HttpServletRequest request) {
	super(request);
	// prepare "presented" both for a text report now and to plot it 
	// later in GraphServlet
	if (sd.q != null) sd.makePresentedData();
    }

}
