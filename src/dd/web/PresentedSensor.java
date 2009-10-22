package dd.web;

import java.util.*;
import java.text.*;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

import dd.engine.*;
import dd.gui.*;

/** Information about the data that needs to be plotted (sensor's B/G
 * graph, in various flavors) and
 * methods for doing so.

 */
public class PresentedSensor extends PresentedData {

    final private int stage;
    final private FrontierContext context;
    final Test sensor;
    final private Double budget;


    PolicySignature[] policies;

    public PresentedSensor(Test _sensor, FrontierContext _context, int _stage, Double _budget) {
	super(new Test[] {_sensor});
	sensor = _sensor;
	stage = _stage;
	context = _context;
	budget = _budget;

	int m = sensor.getM();
	double tc = sensor.getCost(); // cost of the test itself

	policies = new PolicySignature[m+1];

	int cnt=0;
	for(int i=0;i <= m;i++) {
	    Policy w = new Policy(sensor,i);
	    //if (w.getPolicyCost()<1) {
		policies[cnt++]=w;
		//}
	}

	policies = FrontierInfo.trim(policies, cnt);

	if (budget != null) {
	    db = new DetectionRateForBudget(policies, context, budget.doubleValue(),  (stage!=1));
	}

    }

    /** Produces a brief description of the graph, to be displayed above it */
    public String makeGraphTitle() {
	String text = 
	    (stage==1) ? "Non-mixed policies for sensor ":
	    (stage==2) ? "Naive policy mixing for sensor ": "??? ";
	text += " {";
	int cnt=0;
	for(Test sensor: lastSensorsUsed) {
	    if (cnt++ >0) 	    text += " ";
	    if (sensor.getNCopies()>1) text = text +sensor.getNCopies()+ "*";
	    text += sensor.getName();
	}
	text += "}";
	//text += " eps=" + frontier.getEps();
	return text;
    }

    /** Paints the Frontier curve on a specified Graphics2d (which may be
	associated with a Swing GUI element on the screen, or with the SVG
	rendering process)
	@param bounds  Draw within this space; scale as necessary.

	When writing to file, we may keep the file size within manageable
	bounds by skipping frontier points which are within svgEps from each
	other in both C and D coordinates.

	Rendering from the "natural" (c,d) coordinates to the pixel
	coordinates is done using an AffineTransform:

	[ x']   [  m00  m01  m02  ] [ x ]   [ m00x + m01y + m02 ]
        [ y'] = [  m10  m11  m12  ] [ y ] = [ m10x + m11y + m12 ]
        [ 1 ]   [   0    0    1   ] [ 1 ]   [         1         ]
 
	AffineTransform( m00, m10, m01,  m11, m02,  m12)

    */

    public void paintFrontier(Graphics2D g2d, Dimension bounds, 
			      boolean fromGUI) {
	
	String title = 	makeGraphTitle();

	if (fromGUI) pos2pol.clear();
	// when writing to file, we may want to drop some hardly-visible points
	// to save time and file space
	double eps = fromGUI ? 0 : Options.getSvgEps();

	//frontier.getMaxCost();
	double realWidth =  context.getInspectCostPi();

	// 1. grid...
	AffineTransform at =  drawGrid(g2d, bounds, realWidth, title, true);
	
	// 2. curve
	g2d.setPaint(Color.red);
	plotSensor(g2d, eps, fromGUI, at); // FIXME

	// 3. budget
	if (budget != null) plotBudget(g2d, at, context.pi);

	
    }

    void plotSensor(Graphics2D g2d,  double eps,
			    boolean doAddToTable,   AffineTransform at)  {
	

	Point2D.Double point;

	//point = policy2point(Policy.RELEASE, context.pi);
	//circle( g2d, at.transform(point, null));
	
	for(int i= -1; i<policies.length+1; i++) {

	    PolicySignature pol = (i<0) ? Policy.RELEASE : policies[i];
	    if (pol.getPolicyCost(context.pi) > 1) break;

	    point = policy2point(pol, context.pi);
	    circle( g2d, at.transform(point, null));
	    
	    if (i+1 >=policies.length) break;
	    
	    if (stage>1) {
		PolicySignature nextPol = policies[i+1];
	    	Point2D.Double nextPoint = policy2point(nextPol, context.pi);
		if (nextPoint.x > 1) {
		    // truncate
		    double dy = nextPoint.y- point.y;
		    if (dy != 0) dy *= (1-point.x) / (nextPoint.x - point.x);
		    nextPoint = new Point2D.Double(1, point.y + dy);
		}
		Line2D.Double line = new Line2D.Double(point, nextPoint);
		g2d.draw( at.createTransformedShape(line));
	    }
	}

	point  = policy2point(Policy.INSPECT, context.pi);
	circle( g2d, at.transform(point, null));


    }
   

}
