package dd.engine;

import java.io.*;
import java.util.*;


/** An instance of the Test class represents a particular single multi-channel
    test (sensor).  This is the simplest type of a Device; more
    complicated Devices are combined from Tests.

    A sensor is described by its <a
href="http://en.wikipedia.org/wiki/Receiver_operating_characteristic">Receiver
operating characteristic curve</a> (ROC curve), as well as its cost.

<p> We represent the ROC curve as a curve in the (1-specificity,
   sensitivity) space. A sensor is assumed to have n+1 discrete
   "channels" - that, is the sensor is a mathematical model of a
   measuring device of process which, after measuring some properties
   of the object being tested, returns its "answer" as an integer
   number in the range 0 thru n. Out of all "good" input examples, the
   fraction g[i] goes into the i-th channel; and out of all "bad"
   examples, the fraction b[i] goes into the i-th channel. By
   definition, the sum of all g[i] from i=0 thru n,  must be equal to
   1.0, and so is the  sum of all b[i] from i=0 thru n.

   <p>(We start numbering above from 0 and not from 1 because that's
   how data are represented in the argument to the constructor).

   <p> Channels are always expected to be arranged in the order of
   decreasing ration b[i]/g[i]; in other words, the percentage of
   "bad" objects among the cases sent by the device into the 0-th
   channel is higher than the corresponding proportion in the 1-st
   channel, and so on. 

   <p>Cosnider a "policy" that says: "We must inspect all objects that
   are sent by the sensor into channels 0 thru m". The
   <em>sensitivity</em> of this policy is
   <center>
   sensitivity =  SumBad =    sum<sub> i=0,...,m </sub>b[i],
   </center>
   and its specificity is 1-SumGood[m] where 
   <center>
    SumGood[m] =    sum<sub> i=0,...,m </sub>g[i]
   </center>

   <P>The policy with m=n (test all objects and then inspect them all)
   has, of course, SumBad[n]=SumGood[n]=1, i.e. sensitivity of 1 and
   specificity of 0. The policy with m=-1 (test no objects, and
   inspect none) has SumBad[-1]=SumGood[-1]=0, i.e. sensitivity of 0 and
   specificity of 1.</p>

   <P>To create a Test object, you need to describe its ROC curve in
   terms of SumBad[i] and SumGood[i] numbers (i.e., sensitivity and
   1-specificity) for each i=0 thru n-1. The values from i=n don't need to
   be supplied, because they are known to be (1,1) by definition.

   <p>The channels are required to be ordered in the order of
   non-decreasing b/g ratio; thus, for every <em>i</em>, the data must
   satisfy the condition 
   <center>
   (sumBad[i+1]-sumBad[i])/(sumGood[i+1]-sumGood[i]) &le;
   (sumBad[i]-sumBad[i-1])/(sumGood[i]-sumGood[i-1]) </center> 

   <p>
   Since vertical sections <em>are</em> allowed in (the beginning of)
   the ROC curve, the above condition actually has a more general form,
   <center>
   (sumBad[i+1]-sumBad[i])*(sumGood[i]-sumGood[i-1]) &le;
   (sumBad[i]-sumBad[i-1])*(sumGood[i+1]-sumGood[i])
   </center> 
   </p>

   <p> Typically, the only method of this class that you would use as
   an application programmer is its constructor which takes SumGood[]
   and SumBad[] arrays. ({@link #Test(String _name, int _nCopies,
   double _cost, double[], double[], int M)})

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
     * for several sensors with identical characteristics (cost and
     * ROC curve). This allows more efficient Frontier Finder
     * implementation than having several Test instances.
     */
    int nCopies=1;
    /** Gets the sensor's "multiplicity", that is how many sensors
	with identical costs and ROC curves we have. Typically 1, but
	may be more.
     */
    public int getNCopies() { return nCopies; }
    /** Sets the sensor's "multiplicity", that is how many sensors
	with identical costs and ROC curves we have. Typically 1, but
	may be more. */
    public void setNCopies(int n) throws IllegalArgumentException { 
	if (n < 0) throw new IllegalArgumentException("Number of copies must be non-negative");
	nCopies = n;
    }

    private double cost;
    /** Gets the cost of performing the test */
    public double getCost() { return cost; }

    /** The value in sumBad[k] is the fraction of "bad" cases that ends up in
	the output channels 0 through k. (In other words, it's the fraction of
	bad cases that end up in the k+1 highest-quality channels). Thus,
	sumBad[0] &gt; 0, and sumBad[M-1]=1, where M is the number of output
	channels.
    */
    private double[] sumBad;
    private double[] sumGood;

    /** Gets the percentage of bad cases that end up in output
     * channels no. 0 thru i 
     @return A value between 0.0 and 1.1
     */
    double sumBad(int i) {
	return (i<0) ? 0 : (i==sumBad.length)? 1 : sumBad[i];
    }

    /** Percentage of good cases that end up in output channels no. 0 thru i 
     @return A value between 0.0 and 1.1
    */
    double sumGood(int i) {
	return  (i<0) ? 0 : (i==sumGood.length)? 1 : sumGood[i];
    }


    /** Creates a Test object completely describing a sensor in terms
	of its cost and its ROC curve. Read the class description for
	the details about the ROC curve.

	@param _name The name of the sensor. An arbitrary string which
	will be used in all printouts, charts, etc. to identify the
	sensor. There is no limitations on what this string may
	contain, but for the better legibility of plots, it had better
	be printable and short.

	@param _nCopies Can be used to describe multiple sensors with
	identical costs and identical ROC curves. Typically, _nCopies
	should be set to 1; but if you have several (<em>n</em>)
	sensors with the same cost and with identical ROC curve, a
	significant performance saving can be obtained by creating a
	single Test object with _nCopies=<em>n</em> rather than
	<em>n</em> identical Test objects.

	@param _sumBad The arrays _sumBad and _sumGood describe the
	ROC curve in terms of (sensitivity, 1-specificity). If your
	sensor has n+1 channels, these arrays must contain n elements
	each. sumBad[m] must contain athe specificity of the policy
	that requires the inspection of objects that have been sent by
	the sensor into the first m output channels. sumGood[m] must
	contain 1-sensitivity of the sensor. Note that sumGood[n]=1 and
	sumBad[n]=1 are not supplied (the arrays only have elements [0...n-1].
	       
	@param _sumGood See _sumBad, above.

     */
    public Test(String _name, int _nCopies, double _cost, double _sumBad[], double _sumGood[], int M) {
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

    /** Fraction of good cases going out thru channel i (0 &le; i &lt; M) */
    double getG(int i) {
	return (i==0) ? sumGood[i] : sumGood[i]-sumGood[i-1];
    }

    /** Fraction of bad cases going out thru channels (i1,...,i2)
	(0 &le; i1 &le; i2 &lt; M) */
    double getBRange(int i1, int i2) {
	return (i1==0) ? sumBad[i2] : sumBad[i2]-sumBad[i1-1];
    }

    /** Fraction of good cases going out thru channels (i1,...,i2)
	(0 &le; i1 &le; i2 &lt; M) */
    double getGRange(int i1, int i2) {
	return (i1==0) ? sumGood[i2] : sumGood[i2]-sumGood[i1-1];
    }


    /** Gets the number of channels */
    public int getM() { return sumBad.length; } 

    /** A short sensor name for output */
    private String name = "no_name";
    /** Gets the sensor's name */
    public String getName() { return name; }
    /** Sets the sensor's name */
    public void setName(String _name) { name = _name; }

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

    /** Creates a new Test from a description in text format.

	@param br A reader throgh which the description will be read
	by the constructor. This is a BufferedReader because line
	breaks matter. Typically, you'd be passing here a
	BufferedReader constructed from a file reader, but you may
	read data from any other source (pipe, StringReader, etc) as
	well.

	<p>
	Sample input file:
<pre>

cost: .1
0 0
.1 .6
.2 .8
.4 .9
1 1

</pre> 

         The first line contains the cost; other lines contain
	 (sumGood[i], sumBad[i]) pairs; however, unlike the {@link
	 #Test(String _name, int _nCopies, double _cost, double[],
	 double[], int M) multi-argument constructor}, the "trivial"
	 lines "0 0" and "1 1" are explicitly provided.

	@param supposedFileName A string that will be used by the
	constructor as if it were the name of the file from which the
	description is read. (E.g., in error messages, or when setting
	the sensor name). You can use the actual name of the file that
	you're reading via the stream, or anything else.

	@param _nCopies How many sensors with identical costs and ROC curves
	(as described in the stream) do you want to create? Typically
	one, but may be more; see {@link #Test(String _name, int
	_nCopies, double _cost, double[], double[],
	int M)} for details.
	

     */
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


    /** Prints the description of the sensor in the same format as
	used in input (i.e., in the constructor {@link
	#Test(BufferedReader br, String supposedFileName, int _nCopies)
     */
    public void print(PrintWriter out)	{
	//out.println("there are " + getM() + " channels");
	out.println("cost " + cost);
	//  System.out.println("The (B,G) curve:");
	out.println("0 0");	
	for(int i = 0; i< getM(); i++)	{
	    out.println(sumGood[i] + "\t" + sumBad[i]);
	}
	out.flush();
    }

    
    /** Prints the description of the test in the same format as used
	in input (i.e., in the constructor {@link #Test(BufferedReader
	br, String supposedFileName, int _nCopies)}
     */
      public void print(OutputStream out)	{
	print( new PrintWriter(out));
    }
    
    /** Returns the description of the sensor in the same format as
	used in input (i.e., in the constructor {@link
	#Test(BufferedReader br, String supposedFileName, int _nCopies)}
     */
     public String toString1() {
	 StringWriter sw= new StringWriter();
	 print(new PrintWriter(sw));
	 sw.flush();
	 return sw.toString();
     }

    /** Prints the approximation map, if available. This is only
	applicable if this Test object is an approximation of another
	Test object, describing a sensor with a greater number of
	"thinner" channels.
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

    /** A human readable description of the sensor */
    public String toString() {
	StringBuffer b = new StringBuffer("{" + name+": c="+getCost()+" (");
	for(int i = 0; i< getM(); i++)	{
	    b.append("(" + sumGood[i] + " " + sumBad[i] + ") ");
	}
	b.append("}");
	return b.toString();
    }


}



