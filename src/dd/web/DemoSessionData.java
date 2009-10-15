package dd.web;

//import java.net.*;
//import java.sql.*;

import java.io.*;
import java.util.*;
import java.text.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.disk.*;

import dd.engine.*;
import dd.gui.*;

/** A single instance of this class is associated with a particular
 * session of the Demo Application (via HttpSession.setAttribute), and
 * stores all the information that may be set via one http request and
 * needs to be accessible during the processing of other requests.
 */
public class DemoSessionData {

    /** Back-pointer */
    final private HttpSession session;

    /* The sensor which we display in this way and that... */
    public Test q=null;
    public String sensorFileName = null;
    /** Includes pi */
    public FrontierContext context = new FrontierContext(true, 0, VSMethod.VM1, 1e-6);

   /** Null means that it's not set */
    public Double budget=null;
    public int stage = 1;


    /** Can be set in mainweb.jsp  */
    public PresentedData presented = null;

    DemoSessionData( HttpSession _session) {
	session = _session;
    }

    void update(HttpServletRequest request) {
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
            try {
                int x = Integer.valueOf(s);
                if (x>=1 && x<=3) {
		    stage = x;
		}
            } catch(Exception ex) {}
	}


    }


    /** Generates the description of the data that we may need to plot */
    public void makePresentedData() {
	if (stage==1 ||stage==2 ) {		
	    presented = new PresentedSensor(q, context, stage, budget);
	} else if (stage==3) {
	    
	    Calendar  startTime=Calendar.getInstance();
	    Test[] actualSensors  = {q};
	    
	    Frontier f = new Frontier(q,  context);
	    AnnotatedFrontier frontier=new AnnotatedFrontier(f,1,startTime);
	    
	    //AnnotatedFrontier frontier =  Frontier.buildFrontier(actualSensors, otherFrontiers);

	    //presented = new PresentedFrontier(actualSensors, frontier,null);
	    presented = new 
		PresentedFullMixing(actualSensors, frontier, budget);

	} else {
	    // can't do anything
	}
    }	     

    /** Looks up the DemoSessionData already associated with the
     * current session, or creates a new one. This is done atomically,
     * synchronized on the session object.
     */
    static DemoSessionData getDemoSessionData(HttpServletRequest request) {
	HttpSession session = request.getSession();

	String name = "demo";
	DemoSessionData sd  = null;
	synchronized(session) {
	    sd  = ( DemoSessionData) session.getAttribute(name);
	    if (sd == null) {
		sd = new DemoSessionData(session);
		session.setAttribute(name, sd);
	    }
	    sd.update(request);	
	}
	return sd;
    }

    final static NumberFormat pcfmt = new DecimalFormat("#0.##");
    final static NumberFormat ratefmt = new DecimalFormat("0.###");


    public String budgetMessage() {

	DetectionRateForBudget db =    presented.db;
	String s="";
	if (db == null) return "";
	
	s = "<p>The best available policy within budget="+db.givenBudget
	    +" (shown with a black circle) is ";
	s += db.w==1 ?
	    "a non-mixed policy "+db.p1.toTreeString()+". Its total cost is " :
	    "a mixed policy: applying the policy "+ db.p1.toTreeString() +
	    " in " + pcfmt.format(100*db.w) + "% of cases, and the policy "+
	    db.p2.toTreeString() + " the other "+
	    pcfmt.format(100*(1-db.w)) + "% of cases. " +
	    " Its total cost, on average, is ";
	s += pcfmt.format(db.actualBudget) +
	    " This policy detects " + pcfmt.format(100*db.detectionRate) +
	    "% of the \"bad\" objects." +
	    "</p>";
	
	// (stage==3) : compare full to partial
	if (presented instanceof  PresentedFullMixing) {		
	    PresentedFullMixing pres = ( PresentedFullMixing)presented;
	    
	    DetectionRateForBudget db0 = pres.presentedPartialMixing.db;
	    if (Math.abs(db.detectionRate - db0.detectionRate)<1e-3) {
		// about the same - not bother describing
	    } else if (db0.detectionRate==0) {
		s+= "<p>This compares to 0% detection rate given by the simple mixing policy.</p>";
	    } else {
		double inc = (db.detectionRate - db0.detectionRate)/db0.detectionRate;
		s+= "<p>This is an increase of "+pcfmt.format(inc*100)+
		    "% when compared to the base ("+ 
		    pcfmt.format(100*db0.detectionRate)+
		    "%) given by the simple mixing policy (shown by black dashed circle).";

		double eqB = pres.bForD.actualBudget;
		
		if (eqB < db.actualBudget) {
		    double saving = (db.actualBudget-eqB)/ db.actualBudget;

		    s+=" Alternatively, with the full mixing strategy, the detection level ("+ 
			pcfmt.format(100*db0.detectionRate)+
			"%) could be achieved at a budget of "+
			ratefmt.format(eqB)+", saving "+pcfmt.format(100*saving)+"% of the original budget (shown by black dashed square)";
		}
		s += " </p>";
	    }
	}
	return s;
    }

}
