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

	if (ServletFileUpload.isMultipartContent(request)) {
	    // The only place this is done is in file upload
	    readUploadedFile();
	    sd.stage = 1; // reset stage on a new file
	} 

	// prepare "presented" both for a text report now and to plot it 
	// later in GraphServlet
	if (sd.q != null) sd.makePresentedData();
	else sd.presented = null;
    }

    /** Reads and parses a sensor description uploaded from the web
     * form (as an uploaded file, or via a TEXTAREA element 
     */
    void readUploadedFile() {
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
	    infomsg += "<p>Budget="+sd.budget + "</p>\n";
	    while (iter.hasNext()) {
		FileItem item = (FileItem) iter.next();
		
		if (item.isFormField()) {
		    String name = item.getFieldName();
		    String value = item.getString();	

		    if (name.equals(SENSORDATA)) {
			// TEXTAREA upload
			sd.sensorFileName = "Sample_Data";
			sd.q = new Test(new BufferedReader(new StringReader(value)) , 
					sd.sensorFileName, 1);
			sd.sensorFromTextarea = true;
			infomsg +="<p>Successfully read sensor description from uploaded data; using dummy name='"+ sd.sensorFileName+"'</p>";
			return;
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
			return;
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
	    sd.sensorFromTextarea = true;
	    // uploadedStream.close(); 
	    
	    infomsg +="<p>Successfully read sensor description from uploaded file '"+ sd.sensorFileName+"'</p>";
	}  catch (Exception _e) {
	    e = _e;
	    error = true;
	    errmsg = "Failed to receive uploaded file, or to parse the file data. Please make sure that you are uploading file in the correct format! Error: " + e.getMessage();
	}
    }


    /*
    void readUploadedTextareaData() {
	try {
	    String name = "sensordata";
	    String text =(request.getParameter(name));
	    infomsg += name + "\n" + text;
	} catch (Exception _e) {
		e = _e;
		error = true;
		errmsg = "Failed to receive uploaded sensor description, or to parse it. Error: " + e.getMessage();
	    }
    }
    */



}
