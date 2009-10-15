package dd.web;

import java.util.*;
import java.text.*;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

import dd.engine.*;
import dd.gui.*;

/** "Full mixing" Frontier + "partial mixing curve

 */
public class PresentedFullMixing extends PresentedFrontier {

    //final private int stage;
    PresentedSensor presentedPartialMixing;

    DetectionRateForBudget bForD = null;

    public PresentedFullMixing(Test[] _actualSensors, AnnotatedFrontier  f, 
			       Double _budget) {
	super( _actualSensors, f,null);

	presentedPartialMixing = 
	    new PresentedSensor( _actualSensors[0], f.context, 2, _budget);

	if (_budget!=null) {
	    db = f.detectionRateForBudget(_budget.doubleValue(), true);

	    bForD=   DetectionRateForBudget.budgetForDetectionRate
		(f.getPolicies(), f.context,  
		 presentedPartialMixing.db.detectionRate);
	}
    }


    /** Produces a brief description of the graph, to be displayed above it */
    String makeGraphTitle() {
	String text = "Fully randomized deceptive strategy";
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
	double realWidth =  frontier.getMaxCost();

	// 1. grid
	AffineTransform at =  drawGrid(g2d, bounds, realWidth, title, true);

	// 2. low-quality curve
	g2d.setPaint(Color.green);
	presentedPartialMixing.plotSensor(g2d, eps, fromGUI, at); 

	// 3. our (better) curve
	g2d.setPaint(Color.red);
	super.drawFrontierCurve(g2d, frontier, eps, false, at);
	
	// 4. budget
	if (db != null) {
	    plotBudget(g2d, at, frontier.getPi());
	}
    }

    /**  Creates a dashed BasicStroke instance, similar to the original stroke*/
    private BasicStroke mkDashedStroke(Stroke origStroke) {
	float[] dashArray = {3,3};
	if (origStroke instanceof BasicStroke) {
	    BasicStroke orig = ( BasicStroke)origStroke;
	    return new  BasicStroke(orig.getLineWidth(), orig.getEndCap(),
				    orig.getLineJoin(), orig.getMiterLimit(),
				    dashArray, 0);
	} else {
	    return new  BasicStroke(1, BasicStroke.CAP_ROUND,
				    BasicStroke.JOIN_BEVEL, 1,
				    dashArray, 0);
	}
    }
	
	    
    protected void plotBudget(Graphics2D g2d, AffineTransform at, double pi) {
	super.plotBudget( g2d, at, pi);
	
	DetectionRateForBudget db0 = presentedPartialMixing.db;
	if (Math.abs(db.detectionRate - db0.detectionRate)<1e-3) {
	    // about the same - not bother describing
	    return;
	} 

	Stroke origStroke = g2d.getStroke();
	Stroke dashedStroke = mkDashedStroke(origStroke);
	
	try {
	    g2d.setStroke(dashedStroke);

	    Point2D.Double thisPoint = mixedPolicy2point(db0, pi);
	    circle(g2d, at.transform(thisPoint, null), 7);
	    
	    double d0 = db0.detectionRate;

	    if (d0 > 0) {
		Point2D pt1 = at.transform(new  Point2D.Double(0,d0), null);
		Point2D pt2 = at.transform(new  Point2D.Double(1,d0), null);
		g2d.draw( new Line2D.Double(pt1, pt2));

		double eqB = bForD.actualBudget;
		Point2D.Double eqPoint = new Point2D.Double(eqB, d0);
		square(g2d, at.transform(eqPoint, null), 7);	
	    }
	} finally {
	    g2d.setStroke(origStroke);
	}

    }

}
