package dd.engine;

import java.text.*;
import java.io.*;
import java.util.Arrays;


/** A Policy instances represents a policy in a recursive, tree-like
    way, as a combination of a top-level test and a list of
    lower-level policies associated with that test's channels.
 */

public class Policy extends PolicySignature {
    /** The root of the tree of this Policy's device. Formerly, we
     could store null here, if it was a trivial policy (INSPECT or
     RELEASE); but now it's not done, since I/R are implemented as
     PolicySignature instances  */
    Test q;
    /** Policies attached to the output channels of q. The length of this
	array must be equal to the number of q's channels (i.e., q.getM())
    */
    PolicySignature outputs [];

    Policy(Test _q, PolicySignature[] _outputs, double _c, double _e, double _d) {
	super(_c, _e, _d);
	q = _q;
	outputs = _outputs;
	if (q.getM() != outputs.length) throw new AssertionError("Mismatch in Policy(q,outputs): q.m="+q.getM()+", outputs.length=" + outputs.length);
    }

    /** Full tree equality. No attempt to identify equivalent trees (as in,
	(A? I : (B? I : R))     and
	(B? I : (A? I : R))     
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

    /** An elementary efficient non-trivial policy 

	@param level To how many highest-quality channels we apply
	INSPECT. Thus, argument values of 0 and M are not legal, because the
	resulting policies would not be efficient: with level=0, the policy
	would be equivalent to a trivial RELEASE policy, and with level=M, to
	the trivial INSPECT policy; but unlike RELEASE and INSPECT, these
	policies would also contain an unnecessary test q.
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

  
    /** The two trivial policies, which form the ends of every efficient 
	frontier, but aren't usually stored */
    public static final PolicySignature
	RELEASE = new PolicySignature(0,0,0);

    /* This can be changed from Options.setE() */
    public static PolicySignature INSPECT = Options.mkInspect();

    public PolicySignature[] getOutputs() {
        return outputs;
    }


    private int eqCnt(int i) {
	int j=i+1;
	while( j<outputs.length && 
	       outputs[j].equals(outputs[i])) { j++; }
	return j-i;
    }

    /** Returns a string fully describing the operations of this policy
	@param L max string length hint: don't dig too deep... L=0 means "don't restrict"
     */
    public String toTreeString(int L, boolean fold) {
	if (q==null) {
	    // 0 and 1 are the only 2 legal values in trivial policies
	    return super.toTreeString(L,fold);
	} else {
	    StringBuffer b=new StringBuffer( "(" + q.getName() + ":");
	    for(int i=0; i<outputs.length; i++) {
		if (L>0 && b.length()>=L) {
		    b.append(".....");
		    break;
		}
		int remains = (L==0) ? 0 : L-b.length();

		b.append(" ");
		if (fold) {
		    // compact format applies to identical subtrees
		    int cnt = eqCnt(i);
		    if (cnt>1) {
			b.append("" + cnt+"*");
			i += cnt-1;
		    }
		} 
		b.append(outputs[i].toTreeString(remains, fold));
	    }
	    b.append(")");
	    return b.toString();
	}
    }


    public int printTreeString(PrintWriter w, int L, boolean fold) {
	int len=0;
	if (q==null) {
	    // 0 and 1 are the only 2 legal values in trivial policies
	    len += super.printTree(w, L,fold);
	} else {
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
		if (fold) {
		    // compact format applies to identical subtrees
		    int cnt = eqCnt(i);
		    if (cnt>1) {
			s= "" + cnt+"*";
			w.print(s); 
			len += s.length();

			i += cnt-1;
		    }
		}
		len += outputs[i].printTree(w, remains, fold);
	    }
	    w.print(")"); len++;	    
	}
	return len;
    }


    public int printTree(PrintWriter w) {
	return printTree(w, 0, true);
    }


    /** Dimensions of the tree, for the benefit of plotting tools.
     The sizes of a trivial policy are all zeros. */
    static public class Sizes {
	public int height, width;
	/** This structure is to be repeated so many times on several
	    consecutive outpput channels
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

     totalCost = 
     allSensorCosts().g + sum_{i: channel i has INSPECT on it} g[i].
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


    /** Prints the cumulative (G,B) curve for the properly ordered channels 
	of the policy. The i-th line (starting from 0-th) contains the 
	cumulative sensor costs 
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

 