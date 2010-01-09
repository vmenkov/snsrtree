package dd.gui;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.text.*;

import java.awt.*;
import java.awt.geom.*;

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
   
    public Calendar getEndTime() { return frontier.getEndTime(); }

    private static final DateFormat timeFmt = 
	new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    /** Saves all pertinent information - the sensors and the frontier
       of the policy set based on them - in a human-readable
       format. The output goes to a PrintWriter (typically, associated
       with a text file).

       The sensors will be described in their "original" or actually
       stored (approximated) form, based on the flag {@link
       Options#useSimplifiedSensors}; policies are described
       accordingly.

       @param w The writer to which the data will be written

       @param L max line length hint. (0 means "print everything")
     */
    public void saveFrontier( PrintWriter w, int L) {

	w.println("----------- INPUTS: ----------------------");
	w.println("A set of " +  lastSensorsUsed.length + " sensors.");
	for(int i=0; i< lastSensorsUsed.length; i++) {
	    Test sensor = lastSensorsUsed[i];
	    if (!Options.useSimplifiedSensors && sensor.getOrig()!=null) {
		sensor = sensor.getOrig();
	    }

	    w.println("Sensor["+(i+1)+"], name="+ sensor.getName()+
		      ", multiplicity="+sensor.getNCopies()+":");
	    w.println(sensor);
	}
	w.flush();
	w.println("------------ OPTIONS: ---------------------");
	w.println("eps=" + frontier.getEps());		
	w.println("vs=" + frontier.context.vs);		
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
	frontier.print(w, L);
	w.println();
    }
	
    /** Reads in a human-readable frontier description as produced by 
	{@link #saveFrontier( PrintWriter, int L)}. Of course, the 
	frontier must have been saved without truncating policy descriptions, 
	i.e. with L=0.
     */
    public static PresentedFrontier readFrontier(BufferedReader br)  throws IOException, DDParseException{
	String line;
	Pattern optionsPattern =  Pattern.compile("\\-.*\\s*OPTIONS:.*");
	Pattern outputPattern =  Pattern.compile("\\-.*\\s*OUTPUT:.*");
	// Sensor[1], name=SS1g, multiplicity=1:
	final String intPar = "([(0-9]+)";
	Pattern sensorPattern = 
	    Pattern.compile("Sensor\\["+intPar+"\\],\\s*name="+   Test.idPar + 
			    ",\\s*multiplicity=" + intPar + ":\\s*");

	// Parse tests, in INPUTS section
	Vector<Test> v= new 	Vector<Test>();
	while( (line = br.readLine())!=null
	       && !optionsPattern.matcher(line).matches() ) {
	    Matcher m = sensorPattern.matcher(line);
	    if (!m.matches()) {
		//System.out.println("Not a Sensor line: " + line);
		continue;
	    }
	    int j= Integer.parseInt(m.group(1));
	    if (j!=v.size()+1) {
		throw new   DDParseException(line, "Expected sensor number mismatch: expected " + (v.size()+1) + ", found " + j);
	    }
	    j--;
	    String name = m.group(2);
	    int mult= Integer.parseInt(m.group(3));
	    line = br.readLine();
	    Test t =Test.parseTest2(line, name, mult);
	    v.addElement(t);
	}
	
	Test[] sensors = v.toArray(new Test[0]);

	// Parse OPTIONS section
	int maxDepth = SensorSet.maxSetSize(sensors);
	double eps = 0;
	VSMethod vs = VSMethod.VM1;
	Pattern maxDepthPattern = 
	    Pattern.compile("\\s*maxDepth\\s*=\\s*"+ intPar + "\\s*");

	Pattern epsPattern = 
	    Pattern.compile("\\s*eps\\s*=\\s*"+ Test.numPar + "\\s*");

	Pattern vsPattern = 
	    Pattern.compile("\\s*vs\\s*=\\s*(\\S+)\\s*");

	while( (line = br.readLine())!=null
	       && !outputPattern.matcher(line).matches() ) {
	    Matcher m =  maxDepthPattern.matcher(line);
	    if (m.matches()) {
		maxDepth = Integer.parseInt(m.group(1));
		continue;
	    }
	    m =  epsPattern.matcher(line);
	    if (m.matches()) {
		eps = Double.parseDouble(m.group(1));
		continue;
	    }
	    m =  vsPattern.matcher(line);
	    if (m.matches()) {
		vs = VSMethod.valueOf(m.group(1));
		continue;
	    }
	}


	// Parse policies, in OUTPUTS section
	PolicyParser parser = new PolicyParser(sensors);

	//[POLICY 0] (0.11 0.36) (B: (A: I 2*R) R)
	Pattern polStartPattern = 
	    Pattern.compile("\\[POLICY\\s+"+intPar+"\\]\\s*\\(\\s*"+ 
			    Test.numPar+ "\\s+" + Test.numPar + "\\s*\\)\\s*");

	Vector<PolicySignature> vp = new Vector<PolicySignature>();
	while( (line = br.readLine())!=null ) {
	    Matcher m = polStartPattern.matcher(line);
	    if (m.lookingAt()) {
		System.out.println("Found policy line: " + line);
		int j =  Integer.parseInt(m.group(1));
		if (j != vp.size()) {
		     new   DDParseException(line, "Expected pol number mismatch: expected " + (vp.size()+1) + ", found " + j);
		}
		String s = line.substring(m.end(0), line.length());
		System.out.println("parsing policy: " + s);
		PolicySignature pol = parser.parseTree(s);		
		System.out.println("parsed into policy: " + pol + " " + pol.toTreeString());
		vp.addElement(pol);
	    } else {
		//System.out.println("Not a policy line: " + line);
	    }
	}	    

	Calendar cal = Calendar.getInstance();
	AnnotatedFrontier af = new 
	    AnnotatedFrontier(vp.toArray( new PolicySignature[0]), 
			      new FrontierContext(false, 0.0, vs, eps), maxDepth,
			      cal, cal);

	return new PresentedFrontier(sensors, af, null);

	
    }

    /** Just for testing */
    static public void main(String [] argv) throws IOException, DDParseException {
	if (argv.length != 1) throw new AssertionError("no file name");
	FileReader in = new FileReader(argv[0]);	    
	BufferedReader br = new BufferedReader( in );
	System.out.println("Reading " + argv[0]);	    
	PresentedFrontier f =  readFrontier(br);
	in.close();
	int i=0;
	for( Test q: f.lastSensorsUsed ) {
	    System.out.println("Read sensor["+i+"]: " + q);
	    i++;
	}
	
    }

}
