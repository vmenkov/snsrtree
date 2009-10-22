package dd.web;

//import java.net.*;
//import java.sql.*;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

//import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.*;
//import org.apache.commons.fileupload.disk.*;


//import dd.engine.*;
//import dd.gui.*;

/** The base class for the classes that are behind the Frontier Finder
 * Lite JSP pages.
 */
public class FFMain extends FFResultsBase {

    static final String INIT = "init";

    public FFMain(HttpServletRequest _request) {
	super(_request);
	try {

	    if (ServletFileUpload.isMultipartContent(request)) {
		// The only place this is done is in file/textarea upload
		UploadingResults u = readUploadedFile();
		if (!error) {
		    id = u.id;
		    if (id < 0 || id >= sd.maxNS || id > sd.nSensors()) {
			error = true;
			errmsg = "Invalid or inappropriate sensor id=" + id + ". Maybe your sesson has expired?";
		    }
		    sd.sensors[id] = u.q;
		    //sd.sensorFromTextarea = u.sensorFromTextarea;
		    //sd.sensorFileName = u.sensorFileName;
		}

	    } else if  (request.getParameter(INIT)!= null) {		
		/** Acting upon various button-supplied commands */
		String init = request.getParameter(INIT);
		if (init.equals("pk")) 	  sd.initSensorsAF();	
		else if (init.equals("ss"))  sd.initSensorsSS();	
		else  sd.initSensorsBlank();	
		sd.presentedFrontier = null; 
		infomsg += "Re-initialized sensor set from scratch<br>";
	    } else if (request.getParameter("compute")!= null) {
		// Computing the frontier
		sd.setMulti(request);
		sd.computeFrontier();
		infomsg += "Computed frontier<br>";
	    } else if  (request.getParameter("cancel")!= null) {	       
		infomsg += "Cancel: did nothing<br>";
	    } else if  (request.getParameter("deletesensor")!= null) {		
		sd.deleteSensor(id);
		infomsg += "Deleted sensor "+id+"<br>";
	    }


	    sd.synchronizeSensorPresentation();
	}  catch (Exception _e) {
	    setEx(_e);
	}
    }
}
