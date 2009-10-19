package dd.web;

import java.io.*;
import java.util.*;
import java.text.*;

import java.awt.*;


import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.disk.*;

import dd.engine.*;
import dd.gui.*;

/** A single instance of this class is associated with a particular
 * session of the Frontier Finder Lite Application (via
 * HttpSession.setAttribute), and stores all the information that may
 * be set via one http request and needs to be accessible during the
 * processing of other requests.

 * There are two purposes for storing data like this: 
 
 <ol> <li> Not having to pass information about sensor, budget,
 etc. from call to call via e.g. hidden parameters.

 <li> Logically integrating the production of an image and its text
 description - the two things that are logically connected, but have
 toi be served by two seprate servlets.

 </ol>
 */
public class FFSessionData {

    /** Back-pointer */
    final private HttpSession session;

    /** Max number of sensors allowed */
    public final static int maxNS = 6;

    /** Widths of columns, in percents */
    public static int w[] = new int[maxNS];
    static {
	int w0 = 100/maxNS;
	for(int i=0; i<w.length-1; i++) w[i]= w0;
	w[w.length-1] = 100 - (maxNS-1)*w0;
    }


    /* The set of sensors that we display and analyse. Elements beyond
     * a certain point may be nulls. */
    final public Test[] sensors=new Test[maxNS];
    public int nSensors() {
	int n=0;
	while(n<sensors.length && sensors[n]!=null) n++;
	return n;
    }
    //public String sensorFileName = null;
    //public boolean sensorFromTextarea = false;
    /** elements at and beyond sensors.length will be null */
    final public PresentedSensor[] presentedSensors =new PresentedSensor[maxNS];


    /** Includes pi, eps etc. */
    public FrontierContext context = new FrontierContext(true, 0, VSMethod.VM1, 1e-6);

    /** Can be set in mainweb.jsp  */
    public PresentedData presented = null;

    FFSessionData( HttpSession _session) throws WebException,IOException {
	session = _session;
	initSensorsAF();

    }

    synchronized void initSensorsAF()   throws WebException, IOException {
	ServletContext context = session.getServletContext(); 
	String prefix = "/WEB-INF/sensors/a/";
	String names[] = {"sensorA.txt", "sensorB.txt", "sensorC.txt", 
			  "sensorD.txt", "sensorE.txt", "sensorF.txt"};
	int n = (names.length < maxNS) ? names.length : maxNS;

	for(int i=0; i<n; i++) {
	    String path = prefix + names[i];
	    InputStream is = context.getResourceAsStream(path);
	    if (is==null) throw new WebException("Cannot find file '"+path+"' at the server's application context.");
	    sensors[i] = new Test(new BufferedReader(new InputStreamReader(is)),
				  names[i], 1);
	    
	}



    }

    void update(HttpServletRequest request) {
	/*
	String s = request.getParameter("pi");
        if (s!=null) {
            try {
                double pi = Double.valueOf(s);
                if (pi>=0 && pi<=1) {
		    context = context.changePi(pi);
		}
            } catch(Exception ex) {}
	}

	s = request.getParameter("budget");
        if (s!=null) {
            try {
                Double b = new Double(s);
                if (b.doubleValue()>=0 &&b.doubleValue() <=1) {
		    budget = b;
		}
            } catch(Exception ex) {}
	}

	s = request.getParameter("stage");
        if (s!=null) { 
	    // only use the first char of the string, for IE compatibility
	    s = s.trim();
	    if (s.length()>0) s = s.substring(0,1);
            try {
                int x = Integer.valueOf(s);
                if (x>=1 && x<=3) {
		    stage = x;
		}
            } catch(Exception ex) {}
	}
	*/
    }


    /** Generates the description of the data that we may need to plot */
    /*
    public void makePresentedData() {
	if (stage==1 ||stage==2 ) {		
	    presented = new PresentedSensor(q, context, stage, budget);
	} else if (stage==3) {
	    
	    Calendar  startTime=Calendar.getInstance();
	    Test[] actualSensors  = {q};
	    
	    Frontier f = new Frontier(q,  context);
	    AnnotatedFrontier frontier=new AnnotatedFrontier(f,1,startTime);
	    
	    //AnnotatedFrontier frontier =  Frontier.buildFrontier(actualSensors, otherFrontiers);

	    presented = new 
		PresentedFullMixing(actualSensors, frontier, budget);

	} else {
	    // can't do anything
	}
    }	
    */     

    /** Looks up the DemoSessionData already associated with the
     * current session, or creates a new one. This is done atomically,
     * synchronized on the session object.
     */
    static FFSessionData getFFSessionData(HttpServletRequest request) throws WebException,IOException{
	HttpSession session = request.getSession();

	String name = "ff";
	FFSessionData sd  = null;
	synchronized(session) {
	    sd  = ( FFSessionData) session.getAttribute(name);
	    if (sd == null) {
		sd = new FFSessionData(session);
		session.setAttribute(name, sd);
	    }
	    sd.update(request);

	}
	return sd;
    }

    final static NumberFormat pcfmt = new DecimalFormat("#0.##");
    final static NumberFormat ratefmt = new DecimalFormat("0.###");

    /** synchronize presentation with stored sensors */
    synchronized void synchronizeSensorPresentation() {
	for(int i=0; i<maxNS; i++) {
	    if (sensors[i]==null) {
		presentedSensors[i]=null;
	    } else if (presentedSensors[i]!=null && presentedSensors[i].sensor== sensors[i]) {
	    } else {
		presentedSensors[i] = new PresentedSensor(sensors[i], context, 2, null);
		// w h 
		presentedSensors[i].setRecommendedDim(new Dimension(200, 200));
	    }
	}
    }


}
