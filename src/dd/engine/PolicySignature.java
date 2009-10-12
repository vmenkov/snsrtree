package dd.engine;

import java.text.*;
import java.io.*;


/** A "PolicySignature" just stores the cost and detection rate of a Policy. 
 */

public class PolicySignature {
    /** precomputed total cost (on good items, and on bad items) */
    double c, e;
    /** precomputed detection rate  */
    double d;

    /** Builds an instance that only has c and d, not e. For e, it
     * store something that would cause an error later...
     * This method must not be used in multi-pi runs.
     */
    PolicySignature( double _c, double _d) {
	c = _c;
	e = Double.NaN;  
	d = _d;
    }

    PolicySignature( double _c, double _e, double _d) {
	c = _c;
	e = _e;
	d = _d;
    }

    /** Signature equality (same cost and detection rate) */
    public boolean equals(Object o) {
	if (!(o instanceof PolicySignature)) return false;
	PolicySignature x = (PolicySignature)o;
	return c==x.c && d==x.d && e==x.e;	
    }

    /** Creates a simple policy that consists of one test and the
     INSPECT attached to its first n channels. In other words, the
     policy says, "inspect the object if it has been sent into one of
     the first N channels of the test _q".

     @param level N (0 through M). The two extremes are: 0, meanining
     "test, but inspect never"; 1, meaning "test, but inspect always";
     both are bad policies (since the test is done, but its results
     are ignored.
     */
    PolicySignature(Test _q, int level) {
	c=_q.getCost() + _q.sumGood(level-1) * Policy.INSPECT.c;
	e=_q.getCost() + _q.sumBad(level-1) * Policy.INSPECT.e;
	d=_q.sumBad(level-1);
    }

    public double getDetectionRate() {
        return d;
    }

    /** Returns the total cost (on good items): the (average) cost of
        testing, plus g (which is the cost of inspections */
    public double getPolicyCost() {
        return c;
    }

    public double getPolicyCostOnBad() {
        return e;
    }

    // FIXME: should give error if pi!=0 and e=NaN
    public double getPolicyCost(double pi) {
	return (pi==0) ? c : c + pi*(e-c);
    }


    /** Returns true is the point for this policy is below the ray
	from x to y. (In the case of [x,y] being vertical, it is sufficient
	for this policy to be to the right of [x,y).
    */
    boolean isBelowRay(PolicySignature x, PolicySignature y, double pi) {
	return compareToRay(x,y,pi) < 0;
    }

    /** Returns a positive value if, when mapped on the (cost,
	detection_rate) plane, this Policy is above the ray [x,y); 0
	if it is on the ray (in the plane), and a negative value if it
	is below the ray. The value is equal to twice the area of the
	triangle "above the ray" with the vertices at x, y, and this policy.

	@param pi Cost is estimated as   (1-pi)*c + pi*e
    */
    double compareToRay(PolicySignature x, PolicySignature y, double pi) {
	if (pi==0) {
	    if (x.c > y.c) throw new AssertionError("(x.c="+x.c+") > (y.c="+y.c+")");
	    if (x.c > c) throw new AssertionError("(x.c="+x.c+") > (this.c="+c+")");
	    return (d - x.d) * (y.c -x.c) - (c - x.c) * (y.d - x.d);
	} else {
	    double xc = x.getPolicyCost(pi),
		yc = y.getPolicyCost(pi),
		tc = getPolicyCost(pi);
	    if (xc > yc) throw new AssertionError("x.c(pi) > y.c(pi)");
	    if (xc > tc) throw new AssertionError("(x.c(pi)="+xc+") > (this.c(pi)="+tc+")");
	    return (d - x.d) * (yc -xc) - (tc - xc) * (y.d - x.d);
	}
    }

    static private NumberFormat fmt = new DecimalFormat("0.######");
    
    /** Just print cost and detection rate, with a few decimal digits */
    public String toShortString() {
	return "("+fmt.format(c)+" "+fmt.format(d)+")";
    }

    public String toShortString(double pi) {
	return "(c="+fmt.format(c)+", e="+fmt.format(e)+" C(pi)="+
	    fmt.format(c*(1-pi)+ e*pi)+ "; d="+fmt.format(d)+")";
    }

    
    /** Just print cost and detection rate, with all decimal digits */
    public String toShortString2() {
	return "("+c+" "+d+")";
    }

    public String toShortString2(double pi) {
	return "(c="+c+", e="+e+" C(pi)="+  (c*(1-pi)+ e*pi)+ "; d="+d+")";
    }

    public String toString() {
	return  toShortString2();
    }

    /** Returns a string fully describing the operations of this policy */
    public String toTreeString() { return toTreeString(0, Options.fold); }

    /** Returns a string fully describing the operations of this policy
	@param L max string length hint: don't dig too deep... L=0 means "don't restrict"
     */
    public String toTreeString(int L, boolean fold) {
	if (isTrivial()) {
	    // 0 and 1 are the only 2 legal values in trivial policies
	    return (c == 0) ? "R" : "I";
	} else {
	    return "TREE_NOT_STORED";
	}
    }

    public void printTree(PrintWriter w) {
	w.print( toTreeString());
    }

    public boolean isWithinEps( PolicySignature x, double eps) {
	return x.c-eps <= c && c <= x.c+eps &&
	    x.d-eps <= d && d <= x.d+eps;
    }

    public boolean isWithinEps( PolicySignature x, double eps, double pi) {
	double xc = x.getPolicyCost(pi),
		tc = getPolicyCost(pi);
	return xc-eps <= tc && tc <= xc+eps &&
	    x.d-eps <= d && d <= x.d+eps;
    }

    /** Short name for the root test */
    public String getRootName() {
	if (isTrivial()) {
	    return (c==0)? "R" : "I";
	} else return "?";
    }
	    
    /** Returns true if this is the RELEASE or INSPECT policy */
    public boolean isTrivial() {
	return (d==0 || d==1);
    }

    
    public boolean isInspect() {
	return (d==1);
    }

    /** Builds a tree of Sizes structures, for easy plotting later
     */
    public Policy.Sizes getSizes() {
	return new Policy.Sizes();
    }

    double slopeTo(PolicySignature x) {
	return (x.d - d) / (x.c - c);
    }

    double hyp2( PolicySignature q, double pi) {
	double dc = q.getPolicyCost(pi) -getPolicyCost(pi), 
	    dd = q.d - d;
	return dc*dc + dd*dd;
    }


}