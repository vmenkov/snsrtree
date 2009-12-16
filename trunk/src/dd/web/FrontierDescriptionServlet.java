package dd.web;

import java.io.*;
import java.util.*;
import java.text.*;

import dd.engine.*;
import dd.gui.*;

import javax.servlet.*;
import javax.servlet.http.*;

//------------------------------------------------

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

import javax.swing.*;
import javax.swing.event.*;

//import org.w3c.dom.Document;
//import org.w3c.dom.DOMImplementation;
//import org.w3c.dom.Element;


/** This servlet prints frontier description 
 */
public class FrontierDescriptionServlet extends HttpServlet {

    private long getSerial(HttpServletRequest request) {
	try {
	    String s = request.getParameter("serial");
	    if (s!=null) {
		return Long.parseLong(s);
	    } 
	} catch(Exception e) {}
	return -1;
    }

    /** Finds the stored graph that we are called to plot now */
    private PresentedData getPresented(HttpServletRequest request) throws WebException, IOException {
	String caller = request.getParameter("caller");
	if (caller==null) caller = "demo"; // default

	long serial = getSerial(request);

	if (caller.equals("ff")) {
	    FFSessionData r = FFSessionData.getFFSessionData(request);

	    if (r.presentedFrontier != null &&
		r.presentedFrontier.serial == serial) {
		return r.presentedFrontier;
	    }

	    for(int i=0; i<r.presentedSensors.length && r.presentedSensors[i]!=null; i++) {
		if (r.presentedSensors[i].serial == serial) 
		    return r.presentedSensors[i];
	    }
	    return new PresentedFailure(new String[] {
		    "Graph data expired from cache?",
		    "serial=" + serial});
	    
	} else {
	    // demo
	    DemoSessionData r = DemoSessionData.getDemoSessionData(request);
	    return r.presented;
	}
    }

    public void	doGet(HttpServletRequest request,HttpServletResponse response) {


	try {
	    response.setContentType("text/plain");
	    OutputStream ostream = response.getOutputStream();
	    PrintWriter w = new PrintWriter(ostream);

	    PresentedData presented = getPresented(request);
	    if (presented==null || presented instanceof PresentedFailure) {
		w.println("Session expired? serial=" + getSerial(request));
	    } else if (!(presented instanceof PresentedFrontier)) {
		w.println("Unexpected data stored. serial=" + 
			  getSerial(request));
	    } else {
		((PresentedFrontier)presented).saveFrontier(w);
	    }
	    w.flush();
	    
	    ostream.flush();
	    ostream.close();
	} catch (Exception e) {
	    try {
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
	    } catch(IOException ex) {};
	    return;
	}
    }
}
