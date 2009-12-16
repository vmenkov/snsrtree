package dd.gui;

import java.io.*;
import java.util.*;
import java.text.*;

import java.awt.*;
import java.awt.geom.*;
//import javax.swing.*;

import dd.engine.*;

/** Information about the data that needs to be plotted (the main
 * curve, and some subsidiary ones), and methods for doing so.

 This class is public because it's used by the web GUI, too 
 */
public class PresentedFrontier extends PresentedData {
    final public AnnotatedFrontier frontier;

    /** (Optionally) additional frontiers to display on the same graph */
    Vector<AnnotatedFrontier> otherFrontiers;//=new Vector<AnnotatedFrontier>();

    public PresentedFrontier(Test sensors[], AnnotatedFrontier f,
		      Vector<AnnotatedFrontier> _otherFrontiers) {
	super(sensors);
	frontier = f;
	otherFrontiers = _otherFrontiers;


    }

    /** Produces a brief description of the graph, to be displayed above it */
    public String makeGraphTitle() {
	String text = "Extremal frontier for sensor set {";
	int cnt=0;
	for(Test sensor: lastSensorsUsed) {
	    int n = sensor.getNCopies();
	    if (n> 0) {
		if (cnt++ >0) 	    text += " ";
		if (sensor.getNCopies()>1) text = text +sensor.getNCopies()+ "*";
		text += sensor.getName();
	    }
	}
	text += "}";
	text += " eps=" + frontier.getEps();
	if (frontier.getMaxDepth() < lastSensorsUsed.length) {
	    text += "; maxDepth=" + (int)frontier.getMaxDepth();
	}
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

	double realWidth = frontier.getMaxCost();
	AffineTransform at =  drawGrid(g2d, bounds, realWidth, title, false);
	
	if (otherFrontiers!=null) {
	    g2d.setPaint(Color.green);
	    for(AnnotatedFrontier f: otherFrontiers) {
		drawFrontierCurve(g2d, f, eps, fromGUI, at);
	    }
	}

	g2d.setPaint(Color.red);
	drawFrontierCurve(g2d, frontier, eps, fromGUI, at);

    }

    protected void drawFrontierCurve(Graphics2D g2d, 
				   Frontier frontier, 
				   double eps,
				   boolean doAddToTable,
				   AffineTransform at)  {
	PolicySignature[] policies = frontier.getPolicies();
	Point2D.Double lastPoint = null;
	PolicySignature lastP = null;
	for(int i=0; i<policies.length+1; i++) {
	    if (lastPoint==null) lastPoint = policy2point(Policy.RELEASE, frontier.getPi());

	    PolicySignature thisP = frontier.getPolicy(i);

	    if (i != policies.length && lastP != null &&
		Math.abs(thisP.getPolicyCost(frontier.getPi()) - lastP.getPolicyCost(frontier.getPi())) < eps &&
		Math.abs(thisP.getDetectionRate()-lastP.getDetectionRate()) < eps) {
		continue;
	    }

	    Point2D.Double thisPoint = policy2point(thisP, frontier.getPi());

	    g2d.draw( at.createTransformedShape(new Line2D.Double(lastPoint, thisPoint)));
	    Point2D tp = at.transform(thisPoint, null);
	    int x = (int)tp.getX()-radius, y=(int)tp.getY()-radius;
	    g2d.drawOval( x, y, 2*radius, 2*radius);
	    lastP = thisP;
	    lastPoint = thisPoint;
	    if (doAddToTable) pos2pol.put(new Point(x,y), 
					  new PolicyLink(frontier, i));	    
	}	    

    }
   
    /*
    static Point2D.Double policy2point(PolicySignature pol, double pi) {
	return new  Point2D.Double(pol.getPolicyCost(pi), 
				   pol.getDetectionRate());
    }
    */

    public Calendar getEndTime() { return frontier.getEndTime(); }

    private static final DateFormat timeFmt = 
	new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    public void saveFrontier( PrintWriter w) {

	w.println("----------- INPUTS: ----------------------");
	w.println("A set of " +  lastSensorsUsed.length + " sensors.");
	for(int i=0; i< lastSensorsUsed.length; i++) {
	    w.println("Sensor["+(i+1)+"], name="+
		      lastSensorsUsed[i].getName()+", multiplicity="+lastSensorsUsed[i].getNCopies()+":");
	    w.println(lastSensorsUsed[i]);
	}
	w.flush();
	w.println("------------ OPTIONS: ---------------------");
	w.println("eps=" + frontier.getEps());		
	w.println("maxDepth=" + frontier.getMaxDepth());		
	w.println("------------ RUNTIME: ---------------------");
	w.println("Frontier computation started at  " + 
		  timeFmt.format(frontier.getStartTime().getTime()));
	w.println("Frontier computation finished at " + 
		  timeFmt.format(frontier.getEndTime().getTime()));
	double msec = frontier.runtimeMsec();
	w.println("Wall-clock runtime = " + 
		  (msec < 1000 ?
		   ""+  msec + " msec":
		   ""+ (0.001 * msec) + " sec"));
			   

	w.println("-------------- OUTPUT: ---------------------");
	w.flush();
	//w.println( frontier );
	frontier.print(w);
	w.println();
    }
	
	

}
