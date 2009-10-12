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
	request = _request;
        //session = request.getSession();

	sd = DemoSessionData.getDemoSessionData(request);

	if (ServletFileUpload.isMultipartContent(request)) {
	    // The only place this is done is in file upload

	    try {
		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();
		
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		
		// Parse the request
		List /* FileItem */ items = upload.parseRequest(request);
		
		// Process the uploaded items
		Iterator iter = items.iterator();
		FileItem sensorFile = null;
		infomsg += "<p>Budget="+sd.budget + "</p>\n";
		while (iter.hasNext()) {
		    FileItem item = (FileItem) iter.next();
		
		    if (item.isFormField()) {
			String name = item.getFieldName();
			String value = item.getString();	
			infomsg += "<p>Ignoring parameter "+name+"="+value+"</p>\n";

		    } else {
			
			String fieldName = item.getFieldName();
			String fileName = item.getName();
			String contentType = item.getContentType();
			boolean isInMemory = item.isInMemory();
			long sizeInBytes = item.getSize();
			

			if (!fieldName.equals( "sensor"))  {
			    infomsg += "<p>Ignoring file field parameter named "+fieldName+", with file Name "+ fileName+"</p>\n";
			} else {
			    sd.sensorFileName= fileName;
			    sensorFile = item;
			}

			infomsg += "<p>File field name="+fieldName+", file name="+fileName+", in mem=" + isInMemory +", len=" + sizeInBytes+"</p>"; 
		    }
		}
		if (sensorFile==null) {
		    error=true;
		    errmsg = "No file data seems to have been uploaded!";
		    return;
		}

		// we have data file; now read it in

		InputStream uploadedStream = sensorFile.getInputStream();
		sd.q = new Test(new BufferedReader(new InputStreamReader(uploadedStream)) , 
				sd.sensorFileName, 1);

		// uploadedStream.close(); 

		infomsg +="<p>Successfully read sensor description from uploaded file '"+ sd.sensorFileName+"'</p>";


	    }  catch (Exception _e) {
		e = _e;
		error = true;
		errmsg = "Failed to receive uploaded file, or to parse the file data. Error: " + e.getMessage();
	    }
	} 

	infomsg+= "<br>Plain params:";
	for(Enumeration en=request.getParameterNames(); en.hasMoreElements();){
	    String name = (String)en.nextElement();
	    infomsg += "<br>"+name + "=" + request.getParameter(name);	    
	}
	    

    }

    /** Returns the exception's stack trace, as a plain-text string 
     */
    public String exceptionTrace() {	
	StringWriter sw = new StringWriter();
	try {
	    e.printStackTrace(new PrintWriter(sw));
	    sw.close();
	} catch (IOException ex){}
	return sw.toString();
    }

}
