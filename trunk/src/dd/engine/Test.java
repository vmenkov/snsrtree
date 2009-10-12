package dd.engine;

import java.io.*;
import java.util.*;


/** An instance of a Test represents a particular single multi-channel test.
    This is the simplest type of a Device; more complicated Devices are
    combined from Tests.
 */

public class Test implements Cloneable {

    /** The "orginal" sensor by the approximation of which this sensor
     * has been created. This field contains null if this sensor has
     * not been created by approximation.
     */
    private Test orig=null;
    /** approxMap[i]=k means that the i'th channel of this
     * (approximated) test corresponds to the k'th channel, and
     * possibly a number of preceding channels, of the original test.
     */
    private int[] approxMap = null;

    /** This value may be more than one if this Test instance stands
     * for several sensors with identical characteristics
     */
    int nCopies=1;
    public int getNCopies() { return nCopies; }

    private double cost;
    public double getCost() { return cost; }

    /** The value in sumBad[k] is the fraction of "bad" cases that ends up in
	the output channels 0 through k. (In other words, it's the fraction of
	bad cases that end up in the k+1 highest-quality channels). Thus,
	sumBad[0]>0, and sumBad[M-1]=1, where M is the number of output
	channels.
    */
    private double[] sumBad;
    private double[] sumGood;

    /** Percentage of bad cases that end up in output channels no. 0 thru i */
    double sumBad(int i) {
	return (i<0) ? 0 : (i==sumBad.length)? 1 : sumBad[i];
    }

    /** Percentage of good cases that end up in output channels no. 0 thru i */
    double sumGood(int i) {
	return  (i<0) ? 0 : (i==sumGood.length)? 1 : sumGood[i];
    }


    private Test(String _name, int _nCopies, double _cost, double _sumBad[], double _sumGood[], int M) {
	name = _name;
	nCopies = _nCopies;
	cost = _cost;
	sumBad = new double[M];
	sumGood = new double[M];
	for(int i=0; i<M; i++) {
	    sumBad[i] = _sumBad[i];
	    sumGood[i] = _sumGood[i];
	}
    }

    /** Creates a simplified version of this test, merging some "thin" channels. 
	As per Endre Boros' request, 2009-08-22.
	@return The simplified test
     */
    public Test approximate(VSMethod vs, double eps) {
	if (vs== VSMethod.VM1) {
	    int m = sumBad.length;
	    double[] aBad = new double[m],
		aGood = new double[m];
	    int map[] = new int[m];
	    int nKept = 0;
	    if (sumBad[m-1] != 1 || sumGood[m-1] != 1) throw new AssertionError();
	    double lastBad =0, lastGood = 0;
	    for(int i=0; i<m; i++) {	    
		
		if (i < m-1 && 
		    sumBad[i] <= lastBad  + eps &&
		    sumGood[i] <= lastGood  + eps) {
		    // skip 
		} else {
		    aBad[nKept] = lastBad = sumBad[i];
		    aGood[nKept] = lastGood = sumGood[i];
		    map[nKept] = i;
		    nKept ++;
		}
	    }
	    Test approx = new Test(name, nCopies, cost, aBad, aGood, nKept);
	    approx.orig = this;
	// approx.approxMap = Arrays.copyOf(map); // JDK 1.6 only
	    approx.approxMap = new int[ nKept];
	    for(int i=0; i<nKept; i++ ) approx.approxMap[i] = map[i];
	    return approx;
	} else {
	    // Create a frontier that looks exactly as the sensor's ROC curve...
	    Test zeroCost;
	    try {
		zeroCost = (Test)clone();
	    } catch(java.lang.CloneNotSupportedException ex) {
		throw new AssertionError("How come?");
	    }
	    zeroCost.cost = 0;
	    FrontierContext context = new FrontierContext(false, 0, vs, eps);
	    Frontier f = new Frontier(zeroCost, context);
	    // Apply approximation to that frontier...
	    if (vs == VSMethod.VM2) {
		f.approximateVM2();
	    } else if (vs == VSMethod.EB1) {
		f.approximateEB1();
	    } else {
		throw new AssertionError("Vertex-skipping method " + vs + " no supported");
	    }
	    // And now build a sensor with the ROC curve that looks like
	    // the approximated frontier!
	    int nKept = f.length()+1;
	    double [] aGood = new double[nKept], aBad = new double[nKept];
	    int [] aMap = new int[nKept];
	    int j=0;
	    for(int i=0; i<nKept; i++) {
		aGood[i] = f.getPolicyCost0(i);
		aBad[i] = f.getDetectionRate(i);
		while( sumGood[j] < aGood[i] || sumBad[j] < aBad[i]) {
		    if (sumGood[j] > aGood[i] || sumBad[j] > aBad[i]) {
			throw new AssertionError("Can't build approx map due to inconsistent data (1)");
		    }
		    j++;
		}
		if (aGood[i] != sumGood[j] || aBad[i]!=sumBad[j]) {
		    throw new AssertionError("Can't build approx map due to inconsistent data (2)");
		}
		aMap[i] = j++;

	    }
	    Test approx = new Test(name, nCopies, cost, aBad, aGood, nKept);
	    approx.orig = this;
	    approx.approxMap = aMap;
	    return approx;
	}
    }


    /** Fraction of bad cases going out thru channel i (0 &le; i &lt; M) */
    double getB(int i) {
	return (i==0) ? sumBad[i] : sumBad[i]-sumBad[i-1];
    }

    double getG(int i) {
	return (i==0) ? sumGood[i] : sumGood[i]-sumGood[i-1];
    }

    /** Fraction of bad cases going out thru channels (i1,...,i2)
	(0 &le; i1 &le; i2 &lt; M) */
    double getBRange(int i1, int i2) {
	return (i1==0) ? sumBad[i2] : sumBad[i2]-sumBad[i1-1];
    }

    double getGRange(int i1, int i2) {
	return (i1==0) ? sumGood[i2] : sumGood[i2]-sumGood[i1-1];
    }


    /** An array of output channels, ordered in the order of decreasing b/(g+c)
        ratios */
    //Channel channels[];

    /** Number of channels */
    public int getM() { return sumBad.length; } 



    /** A short sensor name for output */
    private String name = "no_name";
    public String getName() { return name; }

    private static String mkName(String fileName) {
	final String prefix = "sensor", suffix = ".txt";
	String s = fileName;
	if (s.endsWith(suffix)) s = s.substring(0, s.length()-suffix.length());
	if (s.startsWith(prefix)) s = s.substring(prefix.length());
	return s;
    }

    Test(File f)  throws IOException {
	this(f,1);
    }

    /** Initializing a Test from an input file of its own 

	Mainly, reads in the points from a text file but first counts the
	lines and channels (countPoints) and checks the form of the file
	(properPoints)
    */
    Test(File f, int _nCopies)  throws IOException {

	String fileName =f.getName();

	if (!f.canRead()) throw new IOException("Sensor description file named '"+fileName+"' does not exist or is not readable.\nPlease check sensor file names listed in your config file!");
	
	init( new BufferedReader( new FileReader(f)),f.getName(), _nCopies); 
    }

    public Test(BufferedReader br, String supposedFileName, int _nCopies)  throws IOException {
	init( br,  supposedFileName,  _nCopies);
    }

    void init(BufferedReader br, String fileName, int _nCopies)  throws IOException {

	nCopies = _nCopies;
	name= mkName(fileName); // brief name to label this test

	Vector<String> lines=new Vector<String>();

	for( String line=null; (line=br.readLine()) != null; ) {
	    if (line.trim().length()!=0) 	  {
		lines.addElement(line);
	    }
	}	    
	br.close();
	if (lines.size() < 3) {
	    throw new IOException("Only " + lines.size() + " lines found in the input file " + fileName);
	}
	//-------------------------------
	int channels = lines.size() - 2;
	sumGood = new double[ channels ];
	sumBad = new double[ channels ];
	
	
	double nextB = 0.0;
	double nextG = 0.0;
	    
	StringTokenizer stFirst = new StringTokenizer(lines.elementAt(0));
	stFirst.nextToken();
	cost = Double.parseDouble( stFirst.nextToken() );
	
	//ASSUMES THERE IS (0,0) AND (1,1) because of method properPoints

	StringTokenizer stO = new StringTokenizer(lines.elementAt(1));
	
	nextG = Double.parseDouble(stO.nextToken() ); //assumed to be 0
	nextB = Double.parseDouble(stO.nextToken() ); //assumed to be 0
	if (nextG != 0 || nextB != 0) throw new IOException("Invalid data in sensor file "+fileName+ ": Starting point("+ nextG + ", " + nextB + ") is not (0,0)");
		
	for(int i = 0; i < channels; i++) 		{
	    String line = lines.elementAt(i+2);
	    
	    StringTokenizer st = new StringTokenizer(line);
	    
	    double firstG = nextG;
	    double firstB = nextB;
	    
	    nextG = Double.parseDouble(st.nextToken() );
	    nextB = Double.parseDouble(st.nextToken() );
	    
	    sumGood[i] = nextG;
	    sumBad[i] = nextB;
	    
	    double g = nextG-firstG, b = nextB - firstB;
	    
	    if ( g<0 || b<0) throw new IOException("Invalid data in sensor file "+fileName+ ": point["+i+"]("+ nextG + ", " + nextB + ") is not an increment over the previous point");

	    if (i==0) {
		if (b < g) {
		    throw new IOException("Invalid data in sensor file "+fileName+ ": point["+i+"]("+ nextG + ", " + nextB + ") is not an efficient channel assignment; should be flipped!");
		}
	    } else {
		double backG = (i==1)? 0: sumGood[i-2];
		double backB = (i==1)? 0: sumBad[i-2];
		
		if ( b*(firstG-backG) > g*(firstB-backB) ) {
		    throw new IOException("Invalid data in sesnsor file "+fileName+ ": point["+i+"]("+ nextG + ", " + nextB + ") is not an efficient channel assignment: b/g ratio on (b="+b+", g="+g+") is increasing compared to the prev channel  (b="+(firstB-backB)+", g="+(firstG-backG)+")!");
		}
	    }


	    if (i==channels-1) {
		if (nextG != 1.0 || nextB != 1.0) throw new IOException("Invalid data in sesnort file "+fileName+ ": End point("+ nextG + ", " + nextB + ")  is not (1,1)");		    
	    }
	    

	}
	br.close();
		
    }


    /** Saves the test in the same format as used in input 
     */
    public void print(PrintStream out)	{
	//out.println("there are " + getM() + " channels");
	out.println("cost " + cost);
	//  System.out.println("The (B,G) curve:");
	out.println("0 0");	
	for(int i = 0; i< getM(); i++)	{
	    out.println(sumGood[i] + "\t" + sumBad[i]);
	}
	out.flush();
    }

    /** Prints the approximation map, if available.
     */
    public void printApproxMap(PrintStream out)	{
	for(int i = 0; i< getM(); i++)	{
	    out.println(i + "\t" + approxMap[i]);
	}
	out.flush();
    }

    /** Returns the "original" sensor if this one is an approximation,
	or null otherwise
     */
    public Test getOrig() { return orig; }

    public String toString() {
	StringBuffer b = new StringBuffer("{" + name+": c="+getCost()+" (");
	for(int i = 0; i< getM(); i++)	{
	    b.append("(" + sumGood[i] + " " + sumBad[i] + ") ");
	}
	b.append("}");
	return b.toString();
    }


}



