package dd.engine;

import java.io.*;

public class Options {

    /** Just the option names, to avoid using quoted strings throughout the
     * program */
    static final String EPS = "eps", SVG_EPS = "svgEps", MAX_DEPTH="maxDepth",
	SIGNATURES_ONLY = "signaturesOnly", FOLD="fold", PARANOID="paranoid",
	COMMERCE="E";


    /** The option table. Presently, no config file is used - just get options 
     with -D */
    static ParseConfig options = new ParseConfig(); 

    /** Vertex skipping method, for skipping some vertices without affecting
	the overall shape of the frontier. It controls how eps is used.
     */
    //enum VSMethod { VM1, EB1 };
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
    public static double getSvgEps() {
	return options.getOptionDouble( SVG_EPS, defaultSvgEps);
    }
    public static void setSvgEps(double val) {
	options.setOption(SVG_EPS, val);
    }

    public static int getMaxDepth(int n) {
	return (int)options.getOptionDouble( MAX_DEPTH, n);
    }

    public static void setMaxDepth(int n) {
	options.setOption(MAX_DEPTH, n);
    }
    
    static public boolean paranoid = options.getOption(PARANOID, false);

    /** If true, apply eps early */
    static public boolean epsAppliesToSensors = true;

    // can be modified later, by calls from GUI
    static public boolean signaturesOnly = options.getOption(SIGNATURES_ONLY, false);

    /** Compact representation (text and tree-plot) of trees, whenever there are
	identical subtrees */
    static public boolean fold =  options.getOption(FOLD, false);

 
    static public int verbosity = options.getOption("verbosity", 1);

    static double[] piList = {0};    

    public static boolean piListIsTrivial() { 
	return piList.length == 1 && piList[0]==0; 
    }

    public static double[] getPiList() { return piList; }

    public static String formatPiList() {
	String s="";
	for(double pi: piList) s += " " + pi;
	return s;
    }

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
    public static void setE(double _E) {
	E = _E;
	Policy.INSPECT = mkInspect();
    } 
    
    public static double getE() { return E; }

    /** This should only be used when E changes */
    static PolicySignature mkInspect() {
	return  new PolicySignature(1+E, 1, 1);
    }

    public static FrontierContext getZeroPiContext() {
	return new FrontierContext(false, 0.0, getVSMethod(), getEps());
    }

}
