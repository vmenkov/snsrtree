package dd.engine;

import java.text.*;
import java.io.*;
import java.util.Arrays;
import java.util.regex.*;


/** A Policy instance represents a policy, that is a decision tree whose
    nodes are sensors, and whose leaves are "INSPECT" or "RELEASE"
    actions. The data are represented in a recursive, tree-like way:
    that is, a policy instance stores the top-level test and a list of
    lower-level policies associated with that test's channels. The two
    simplest policies are {@link #INSPECT} and {@link #RELEASE}, which
    have no sensors, just a single non-conditional action.

    <P> A Policy object also stores information about the policy's
    detection rate and average total cost (separately on "good" and on
    "bad" objects). This total policy cost includes the cost of
    testing (i.e., the test cost of all sensors in the tree nodes),
    the inspections (in the {@link Policy#INSPECT INSPECT} nodes), and
    the extra "interruption of commerce" cost resulting from
    inspecting "good" objects; all of these are appropriately weighted
    based on the percentage of good and bad objects that end up going
    through every part of the decision tree.

    <p>Policy is an extension of the "simplified" class, called {@link
    PolicySignature}, whose instances only store the cost and
    detection rate numbers for the policiesq, but not the actual
    decision trees.

    <p>The Policy class has methods for saving a policy description
    (serializing). For reading in a policy description
    (deserializing), you can use {@link
    PolicyParser#parseTree(CharSequence)}
 */

public class Policy extends PolicySignature {
    /** The root of the tree of this Policy's device. Formerly, we
     could store null here, if it was a trivial policy (INSPECT or
     RELEASE); but now it's not done, since I/R are implemented as
     PolicySignature instances  */
    final Test q;

    /** Policies attached to the output channels of q. The length of this
	array must be equal to the number of q's channels (i.e., q.getM())
    */
    PolicySignature outputs [];

    /** Creates a Policy object from the root sensor and the list of
	child policies asscociated with its output channels. For
	efficiency's sake, correct pre-computed costs and detection
	rates must be supplied as well.

	@param _q Root sensor
	@param Child policies asscociated with the sensor's output
	channels. The length of this array must be equal to the number
	of the sensor's channels.

	@param _c cost on good objects
	@param _e cost on bad objects
	@param _d detection rate
 
     */
    Policy(Test _q, PolicySignature[] _outputs, double _c, double _e, double _d) {
	super(_c, _e, _d);
	q = _q;
	outputs = _outputs;
	if (q.getM() != outputs.length) throw new AssertionError("Mismatch in Policy(q,outputs): q.m="+q.getM()+", outputs.length=" + outputs.length);
    }

    /** Creates a Policy object from the root sensor and the list of
	child policies asscociated with its output channels. This
	method computes the costs and detection rates of the new policy
	based on those of the child policies.

	@param q Root sensor
	@param outputs Child policies asscociated with the sensor's output
	channels. The length of this array must be equal to the number
	of the sensor's channels.

	@see #Policy(Test _q, PolicySignature[] _outputs, double _c, double _e, double _d)
     */
    public static Policy constructPolicy(Test q,  PolicySignature[] outputs) {
	double c = q.getCost(), d=0, e=q.getCost();
	if (q.getM() != outputs.length) throw new IllegalArgumentException("Channel count mismatch: " +  outputs.length + " children policies given for " + q.getM() + " channels of the sensor");

	for(int i=0; i< outputs.length; i++) {
	    d += q.getB(i) * outputs[i].d;
	    c += q.getG(i) * outputs[i].c;
	    e += q.getB(i) * outputs[i].e;
	}

	return new Policy(q, outputs, c,e,d);
    }



    /** Checks for full tree equality. No attempt is made to identify
	equivalent trees (e.g., this method won't know that the policies
	<pre>(A? I : (B? I : R))</pre>
	and 
	<pre>(B? I : (A? I : R)),</pre>

	where the costs and ROC curves are A and B are identical, are
	equivalent)

	@param o Must be a Policy object
	@return True if o describes the same exactly decision tree as
	this policy.
    */
    public boolean equals(Object o) {
	if (o == this) return true; // e.g. INSPECT or RELEASE
	if (!(o instanceof Policy)) return false;
	Policy x = (Policy)o;
	if (!q.equals(x.q) && outputs.length != x.outputs.length) return false;
	for(int i=0; i<outputs.length; i++) {
	    if (!(outputs[i] instanceof Policy) || 
		!outputs[i].equals(x.outputs[i])) return false;
	}
	return true;
    }

    /** Produces an elementary efficient non-trivial policy

	@param _q The root sensor of the new Policy

	@param level To how many highest-quality channels we apply
	INSPECT. Thus, argument values of 0 and M are not legal, because the
	resulting policies would not be efficient: with level=0, the policy
	would be equivalent to a trivial RELEASE policy, and with level=M, to
	the trivial INSPECT policy; but unlike RELEASE and INSPECT, these
	policies would also contain an unnecessary test q.

	The constructor creates a policy that consists of the
	specified sensor, and a number of INSPECT and RELEASE
	leaves. The INSPECT leaves are attached to the first ''level''
	output channels of the sensor, and the RELEASE ones, to the
	remaining channels.
     */
    public Policy(Test _q, int level) {
	super(_q, level);
	q = _q;
	if (q==null) throw new AssertionError("Though shalt not create a policy with q=null");
	int m =q.getM();
	outputs = new PolicySignature[m];
	if (level < 0 || level > m) throw new AssertionError("Mismatch in Policy(q,level): m="+m+", level=" + level);
	int i=0;
	while( i<level ) outputs[i++] = INSPECT;
	while( i<m ) outputs[i++] = RELEASE;
    }

  
    /** The trivial "release-all-objects" policy. Its cost (on any
     * object) and detection rate are both equal to 0. */
    public static final PolicySignature
	RELEASE = new PolicySignature(0,0,0);

    /** The trivial "inspect-all-objects" policy. Its detection rate is
      1. Its cost on "bad" objects is 1 (by definition, as the
      inspection cost is our unit of cost), while its cost on "good" objects 
      is 1+<em>E</em>. 

      <p>The value of <em>E</em>, and the policy stored at
      Policy.INSPECT, can be by a call to {@link Options#setE(double)}
    */
    public static PolicySignature INSPECT = Options.mkInspect();

    /** Returns the array of "child" policies associated with output
     * channels of the sensor.
     */
    public PolicySignature[] getOutputs() {
        return outputs;
    }


    /** Returns a human-readable string fully describing the
	operations of this policy
	@param L max string length hint: don't dig too deep... L=0 means "don't restrict"
     */
    public String toTreeString(int L, boolean fold) {
	StringWriter w = new StringWriter();
	printTreeString(new PrintWriter(w), L,fold); 
	return w.toString();
    }


    /** Prints a human-readable string fully describing the
	operations of this policy.

	The flag {@link Options#useSimplifiedSensors} controls whether
	the policy is described with respect to the original sensors
	or simplified ones.

	@param L max string length hint. If it's non-zero, it's
	interpreted as max-length-hint for the output line, and can be
	used to create manageable, even if incomplete, human-readable
	description. The value L=0 means "don't abbreviate", and
	should be used when a complete - even if potentially large -
	description is desired (e.g., in order for you to be able to
	"deserialize" the policy later on).

	@param fold If true, a more compact format is used whenever
	adjacent subtrees are identical
     */
    public int printTreeString(PrintWriter w, int L, boolean fold) {
	int len=0;
	if (q==null) {
	    // 0 and 1 are the only 2 legal values in trivial policies
	    len += super.printTree(w, L,fold);
	} else {
	    // do we need to express results in terms of a different,
	    // "original", sensor?
	    final boolean useOrig= !Options.useSimplifiedSensors &&
		q.getOrig()!=null;

	    // space-saving expedient
	    if (useOrig) fold = true;

	    String s= "(" + q.getName() + ":";
	    w.print(s); 
	    len += s.length();

	    for(int i=0; i<outputs.length; i++) {
		if (L>0 && len>=L) {
		    s=".....";
		    w.print(s);
		    len +=  s.length();
		    break;
		}
		int remains = (L==0) ? 0 : L - len;

		w.print(" "); len++;

		// Compact format applies to identical subtrees
		int cnt = fold?  eqCnt(i) : 1;

		// Channel multiplicity, in terms of the actual
		// (approximated) sensor, or of the original sensor (if
		// available and requested)

		int mult = useOrig?  q.getOrigChannelCount(i, i+cnt) : cnt;

		if (mult  > 1) {
		    s= "" + mult+"*";
		    w.print(s); 
		    len += s.length();		    
		}

		if (cnt>1) {
		    i += cnt-1;
		}


		len += outputs[i].printTree(w, remains, fold);
	    }
	    w.print(")"); len++;	    
	}
	return len;
    }

    /** Same as printTree(w), but without truncating the text (i.e.,
      L=0), and with "folding" identical subtrees.

       @see  #printTree(PrintWriter w, int L, boolean fold)
     */
    public int printTree(PrintWriter w) {
	return printTree(w, 0, true);
    }

    private int eqCnt(int i) {
	int j=i+1;
	while( j<outputs.length && 
	       outputs[j].equals(outputs[i])) { j++; }
	return j-i;
    }

  

    /** Dimensions of the tree, for the benefit of plotting tools.
     The sizes of a trivial policy (INSPECT or RELEASE) are all
     zeros. */
    static public class Sizes {
	final public int height, width;
	/** This structure is to be repeated so many times on several
	    consecutive output channels
	*/
	public int count=1;
	public Sizes children[];
	Sizes() { 
	    height=width=0; 
	    children = new Sizes[0];	
	}
	Sizes(int h, int w, Sizes[] s) { 
	    height=h; width=w; 
	    children=s;
	}
    }

    /** Builds a tree of Sizes structures, for easy plotting later
     */
    public Sizes getSizes() {
	if (q==null) return new Sizes();
	int h = 0, w=0;
	Sizes a[] = new Sizes[outputs.length];
	int nKept = 0;
	for(int i=0; i<outputs.length; i++) {
	    if (Options.fold && i>0 && outputs[i].equals(outputs[i-1])){
		a[nKept-1].count ++;
	    } else {
		a[nKept] = outputs[i].getSizes();
		if (a[nKept].height > h) h = a[nKept].height;
		w += a[nKept].width;
		nKept++;
	    }
	}
	if (nKept < a.length) {
	    //a = Arrays.copyOf(a, nKept, a[0].getClass());
	    Sizes b[] = a;
	    a = new Sizes[nKept];
	    for(int i=0; i<nKept; i++) a[i] = b[i];
	}
	return new Sizes( h+1, w+a.length-1, a);
    }

    /** Short name for the root test */
    public String getRootName() {
	if (q==null) {
	    return (c==0)? "R" : "I";
	} else return q.getName();
    }
	    
    public boolean isTrivial() {
	return (q==null);
    }

    PolicySignature getSignature() {
	return new PolicySignature(c,d);
    }

    
    static class GBPair implements Comparable<GBPair>  {
	double g, b;
	GBPair(double _g, double _b) { g=_g; b=_b; }
	/** Sorts in descending order of b/g, i.e. ascending of g/b */
	public int compareTo(GBPair x)  {
	    double diff  = g*x.b - b*x.g;
	    return diff<0? -1 : diff==0? 0: 1;
	}
	GBPair plus(GBPair x) {
	    return new GBPair( g + x.g, b+ x.b);
	}
	GBPair minus(GBPair x) {
	    return new GBPair( g - x.g, b- x.b);
	}
	GBPair times(GBPair x) {
	    return new GBPair( g * x.g, b* x.b);
	}
    }


    /** Computes expected total costs of applying all sensors for G and B
     * specimens. This does not include the cost of INSPECT operations.
     * Thus one would expect that 
     <center>
     totalCost = 
     allSensorCosts().g + sum_{i: channel i has INSPECT on it} g[i].
     </center>
     */
    private GBPair allSensorCost() {
	double tc = q.getCost();
	GBPair c = new GBPair( tc, tc);
	for(int i=0; i<outputs.length; i++) {
	    if ( outputs[i] instanceof Policy) {		
		double g = q.getG(i), b = q.getB(i);
		GBPair childCost =  ((Policy)(outputs[i])).allSensorCost();
		c.g += g * childCost.g;
		c.b += b * childCost.b;
	    }
	}	
	return c;
    }

    /** Builds a (not necessarily sorted) vector of (G,B) pairs, describing
	percentages of for all input cases ending up in each output channel
	of this policy.
     */
    private GBPair[] toGBVector() {
	GBPair[][] ch = new GBPair[outputs.length][];
	int len = 0;	
	for(int i=0; i<outputs.length; i++) {
	    if (outputs[i] instanceof Policy) {
		ch[i] = ((Policy)(outputs[i])).toGBVector();
	    } else {
		// INSPECT or RELEASE - it only has one channel
		ch[i] = new GBPair[] { new GBPair(1,1) };
	    }
	    len += ch[i].length;
	}
	GBPair[] w = new GBPair[len];
	int pos=0;
	for(int i=0; i<outputs.length; i++) {
	    GBPair a  = new GBPair(q.getG(i), q.getB(i));
	    //if (a.g==0) System.out.println("** Got g=0 on channel " +i + " of test " + q);
	    for(GBPair p: ch[i]) {
		w[pos++] = a.times(p);
	    } 
	}
	return w;
    }


    /** Prints the cumulative (G,B) curve for the properly ordered
	channels of the policy. The i-th line (starting from 0-th) contains the 
	cumulative sensor costs.

	This is a rather peculiar representation, described by Endre
	Boros in the document "DNDO.C.D.curves.MenuPart2.doc"
	(2009-05-12) as follows:
	
	<p>
	 More precisely, delete the terminal actions from the leaves,
	 compute C(good) and C(bad) which are the expected cost of
	 inspection (only sensor costs) for good and bad containers
	 respectively.

	 <p> Also, compute g(j) and b(j) as the probability that a
	 good (resp., bad) container gets to the leaf node j, sort the
	 leaves by decreasing b(j)/g(j) (left-to-right if generation
	 went by the usual process), and output a file similar to the
	 sensor files, containing C(good), C(bad) in the first line,
	 and then the pairs
	 <center>
	 \sum_{j=1}^i g(j), \sum_{j=1}^i b(j)
	 </center>
	 
	 for i=0,...,L, where L is the number of leaves. Note that
	 these pairs, as coordinates of points must form an ROC curve
	 from (0,0) to (1,1).
	 
     */
    public void saveAsADevice(PrintWriter w) {
	GBPair[] h = toGBVector();
	Arrays.sort(h);
	// Addition carries some floating-point error, and we would not
	// want to have a gap between sum_{0...n} and 1. So let's sum
	// from both ends, and average
	GBPair[] sumLeft=new GBPair[h.length+1], 
	    sumRight=new GBPair[h.length+1];
	sumLeft[0] = new  GBPair(0,0);
	sumRight[h.length] = new  GBPair(0,0);
	for(int i=0;i<h.length; i++) {
	    sumLeft[i+1] =  sumLeft[i].plus(h[i]);
	}
	for(int i=h.length-1; i>=0; i--) {
	    sumRight[i] = sumRight[i+1].plus(h[i]);
	}
	GBPair[] sum = new GBPair[h.length+1];
	sum[0] = new GBPair(0,0);
	sum[h.length] = new GBPair(1,1);
	
	//System.out.println("i  G(L+R)   B(L+R)" );



	for(int i=1;i<h.length; i++) {
	    //System.out.println("" +i+ " "+ sumLeft[i].g  +"+"+  sumRight[i].g +
	    //		       " "+ sumLeft[i].b  +"+"+  sumRight[i].b );
	    sum[i] = new GBPair( sumLeft[i].g / (sumLeft[i].g + sumRight[i].g),
				 sumLeft[i].b / (sumLeft[i].b + sumRight[i].b));
	}

	GBPair asc =  allSensorCost();
	w.println("cost(good): "  + asc.g);
	w.println("cost(bad): "  + asc.b);
	for(int i=0; i<h.length+1; i++) {
	    
	    w.println( (sum[i].g==0? "0" : "" + sum[i].g) + " " + sum[i].b);
	}
	

	w.flush();
    }


}

 