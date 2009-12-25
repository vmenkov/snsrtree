package dd.engine;

import java.io.*;

/** This class stores a few static members, which are values of
 * certain options to the frontier finder algorithm. They can be set
 * and accessed via the set and get access methods, which are also
 * used by the {@link dd.gui.DDGUI Frontier Finder GUI}. Default
 * values for most variables can be from the command line via the
 * -Dname=value Java system properties. Read the source code of this
 * class for the names of these options.
 */
public class Options {

    /** Just the option names, to avoid using quoted strings throughout the
     * program */
    static final String EPS = "eps", SVG_EPS = "svgEps", MAX_DEPTH="maxDepth",
	SIGNATURES_ONLY = "signaturesOnly", FOLD="fold", PARANOID="paranoid",
	COMMERCE="E";


    /** The option table. Presently, no config file is used - just get options 
     from the command line using Java system properties (-Dname=value) */
    static ParseConfig options = new ParseConfig(); 

    /** Vertex skipping method, for skipping some vertices without affecting
	the overall shape of the frontier. It controls how eps is used.
     */
    static VSMethod vs = VSMethod.VM1;
    {
	String q =  options.getOption( "vs", VSMethod.VM1.toString());
	vs = VSMethod.valueOf(q);
    }

    public static VSMethod getVSMethod() {
	return vs;
    }
    public static void  setVSMethod(VSMethod _vs) {
	vs = _vs;
    }

    final static double defaultEps = 1e-6; 
    public static double getEps() {
	return options.getOptionDouble( EPS, defaultEps);
    }
    public static void setEps(double val) {
	options.setOption(EPS, val);
    }

    /** To make generating image files easier ... */
    final static double defaultSvgEps = 5e-4; 

    /** Retrieves the "plotting epsilon" parameter, set by {@link #
     * setSvgEps(double) }
     */
    public static double getSvgEps() {
	return options.getOptionDouble( SVG_EPS, defaultSvgEps);
    }

    /** Sets the <em>epsilon</em> parameter used in generating image
      files (and <strong>not</strong> in the actual frontier
      construction).  Vertices that are closer than <em>epsilon</em>
      to an already-plotted vertex (with respect to both cost and
      detection rate) may be skipped <strong>during
      plotting</strong>. This results in the generation of an SVG file
      of a reasonable size. E.g., setting this epsilon to 1e-4 will
      ensure that the graph has no more than 20,000 vertices.
     */
    public static void setSvgEps(double val) {
	options.setOption(SVG_EPS, val);
    }

    public static int getMaxDepth(int n) {
	return (int)options.getOptionDouble( MAX_DEPTH, n);
    }

    /** Sets the default value of maximum allowed policy tree depth
     * used in frontier generation
     */
    public static void setMaxDepth(int n) {
	options.setOption(MAX_DEPTH, n);
    }
    
    static public boolean paranoid = options.getOption(PARANOID, false);

    /** If true, apply {@link vertex-skipping VSMethod} "early",
     * i.e. to the sensors' ROC curves, and not just to policies built
     * from multiple sensors. */
    static public boolean epsAppliesToSensors = true;

    /** If true, the Frontier Finder only stores (cost_good, cost_bad,
	detection_rate) triplets (i.e., {@link PolicySignature}s)
	rather than complete {@link Policy} trees. This allows one to
	save a lot of memory and to produce frontiers for more complex
	combinations of sensors - but without seeing the structure of
	actual policies that go into the frontier.
	
	<p>
	This flag can be modified by calls from SNSRTREE GUI.
     */
    static public boolean signaturesOnly = options.getOption(SIGNATURES_ONLY, false);

    /** If true, Frontier Finder uses compact representation (text and
	tree-plot) for trees, whenever there are identical subtrees */
    static public boolean fold =  options.getOption(FOLD, false);

    /** Controls debug level */
    static public int verbosity = options.getOption("verbosity", 1);

    static double[] piList = {0};    

    /** Returns tree if the currently set list of <em>pi</em> values
     * is the minimum legal list, i.e. {0, 1}
     */
    public static boolean piListIsTrivial() { 
	return piList.length == 1 && piList[0]==0; 
    }

    /** Retrieves the list of <em>pi</em> values (used for extremal
     * surfaces calculations for non-zero pi
     */
    public static double[] getPiList() { return piList; }

    /** Produces a string describing the currently stored list of pi values 
     */
    public static String formatPiList() {
	String s="";
	for(double pi: piList) s += " " + pi;
	return s;
    }

    /*  Sets the list of pi values from a space-separated string
     */
    public static void parsePiList(String s) {
	s = s.trim();
	String z[] = s.split("\\s+");
	if (z==null || z.length==0) throw new IllegalArgumentException("Not a single token in the piList string: " + s);
	double q[] = new double[z.length];
	for(int i=0; i<z.length; i++) {
	    q[i] = Double.parseDouble(z[i]);
	}

	if (q[0] != 0) 	    throw new IllegalArgumentException("The piList must start with 0. Now is: " + s);

	if (q.length==1) { piList=q; return;} // just 0
	for(int i=1; i<q.length; i++) {
	    if (q[i] <= q[i-1]) throw new IllegalArgumentException("The piList must be ordered, and it is not ("+q[i-1]+" followed by "+q[i]+"). Now is: " + s);
	}
	if (q[q.length-1] != 1.0 )throw new IllegalArgumentException("The piList must end with 1, not "+q[q.length-1]+ ". Now is: " + s);
	piList = q;
    }

    /** Reads the pi list from file
	@return false if failed to read or to parse
	@see  #parsePiList(String s) 
     */
    public static boolean readPiList(File f) throws IOException {
	FileReader _in = new FileReader(f);
	LineNumberReader in = new LineNumberReader(_in);
	StringBuffer b=new StringBuffer();
	String s;
	while((s=in.readLine())!=null) b.append(s + " ");
	parsePiList(b.toString());	
	in.close();
	return true;
    }

    static private double E=  options.getOptionDouble( COMMERCE, 0);

    /** Sets <em>E</em>, a.k.a the "cost of interruption of commerce"
     * - the extra cost involved in false positives (i.e., applying
     * the INSPECT action to objects that in fact are "good".
     */
    public static void setE(double _E) {
	E = _E;
	Policy.INSPECT = mkInspect();
    } 
    
    /** Retrieves <em>E</em>, a.k.a the "cost of interruption of commerce"
     * - the extra cost involved in false positives (i.e., applying
     * the INSPECT action to objects that in fact are "good".
     */
    public static double getE() { return E; }

    /** This should only be used when E changes */
    static PolicySignature mkInspect() {
	return  new PolicySignature(1+E, 1, 1);
    }

    public static FrontierContext getZeroPiContext() {
	return new FrontierContext(false, 0.0, getVSMethod(), getEps());
    }

}
