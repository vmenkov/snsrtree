package dd.web;

//import java.net.*;
//import java.sql.*;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.disk.*;

import dd.engine.*;
//import dd.gui.*;

public class ResultsBase {

    HttpServletRequest request;

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
	    
	    infomsg+= "<br>Plain params:<br>";
	    for(Enumeration en=request.getParameterNames(); en.hasMoreElements();){
		String name = (String)en.nextElement();
		infomsg += name + "=" + request.getParameter(name) + "<br>";
	    }	    
	}  catch (Exception _e) {
	    setEx(_e);
	}	
    }

    void setEx(Exception _e) {
	error = true;
	if (_e instanceof WebException) {
	    // this is our own exception - we known where it came from,
	    // so no need to print stack ect
	} else {
	    e = _e;
	}
	errmsg = "Error: " + _e.getMessage();
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

    /** An auxiliary class, used in parsing requests sent in "multipart" format
	(data uploads)
    */
    static class UploadingResults {
	Test q=null;
	boolean sensorFromTextarea = false;
	String sensorFileName = "?";
	int id = 0;
    }

    /** Reads and parses a sensor description uploaded from the web
     * form (as an uploaded file, or via a TEXTAREA element 
     */
    UploadingResults readUploadedFile() {
	UploadingResults u = new UploadingResults();
	try {

	    final String SENSORDATA = "sensordata";

	    // Create a factory for disk-based file items
	    FileItemFactory factory = new DiskFileItemFactory();
	    
	    // Create a new file upload handler
	    ServletFileUpload upload = new ServletFileUpload(factory);
	    
	    // Parse the request
	    List /* FileItem */ items = upload.parseRequest(request);
	    
	    // Process the uploaded items
	    Iterator iter = items.iterator();
	    FileItem sensorFile = null;

	    // if supplied by param...
	    String sensorname = null;

	    while (iter.hasNext()) {
		FileItem item = (FileItem) iter.next();
		
		if (item.isFormField()) {
		    String name = item.getFieldName();
		    String value = item.getString();	

		    if (name.equals(SENSORDATA)) {
			// TEXTAREA upload
			u.sensorFileName = "Sample_Sensor";
			u.q = new Test(new BufferedReader(new StringReader(value)) , 
				       u.sensorFileName, 1);
			u.sensorFromTextarea = true;
			infomsg +="<p>Successfully read sensor description from uploaded data; using dummy name='"+ u.sensorFileName+"'</p>";
		    } else if (name.equals("sensorname")) {
			sensorname = value;
		    } else if (name.equals("id")) {

			try {
			    u.id = Integer.valueOf(value);
			} catch(Exception ex) {}
  
		    } else {
			infomsg += "<p>Ignoring parameter "+name+"=<pre>"+value+"</pre></p>\n";
		    }
		} else {
		    
		    String fieldName = item.getFieldName();
		    String fileName = item.getName();
		    String contentType = item.getContentType();
		    boolean isInMemory = item.isInMemory();
		    long sizeInBytes = item.getSize();
		    

		    if (!fieldName.equals( "sensor"))  {
			infomsg += "<p>Ignoring file field parameter named "+fieldName+", with file Name "+ fileName+"</p>\n";
		    } else if (fileName.equals("")) {
			error = true;
			errmsg = "It appears that you have not uploaded a file! Please go back to the file upload form, and make sure to pick an existing sensor description file!";
			return u;
		    } else {
			u.sensorFileName= fileName;
			sensorFile = item;

			// we have data file; now read it in
			
			InputStream uploadedStream = sensorFile.getInputStream();
			u.q = new Test(new BufferedReader(new InputStreamReader(uploadedStream)) , 
				       u.sensorFileName, 1);
			u.sensorFromTextarea = false;
			// uploadedStream.close(); 
			infomsg +="<p>Successfully read sensor description from uploaded file '"+ u.sensorFileName+"'</p>";
		    }
		    
		    infomsg += "<p>File field name="+fieldName+", file name="+fileName+", in mem=" + isInMemory +", len=" + sizeInBytes+"</p>"; 
		}
	    }

	    if (u.q==null) {
		error=true;
		errmsg = "No file data seems to have been uploaded!";
	    } else if (sensorname != null && !sensorname.trim().equals("")) {
		sensorname = sensorname.trim();
		u.q.setName(sensorname);
	    }

	}  catch (Exception _e) {
	    e = _e;
	    error = true;
	    errmsg = "Failed to receive uploaded file, or to parse the file data. Please make sure that you are uploading file in the correct format! Error: " + e.getMessage();
	}
	return u;
	    
    }


}
