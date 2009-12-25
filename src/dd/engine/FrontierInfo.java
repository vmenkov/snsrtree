package dd.engine;

import java.io.*;
import java.util.*;


/** A FrontierInfo is a generic parent class for the classes that are
 * used to describe an "efficient frontier" - a convex hull over a set
 * of Policies. It always includes the two trivial policies, (0,0) and
 * (1,1), which we don't explicitly store in the representation.
 */

abstract public class FrontierInfo {
    /** Returns the detection rate of the i-th policy
      
      @param i The policy position, ranging from -1 to policies.length
      @return Detection rate, in the range 0 through 1.0 
    */
    abstract public double getDetectionRate(int i);

    /** Returns the total average policy cost of the i-th policy
      on a "good" object.
      
      @param i The policy position, ranging from -1 to policies.length
      @return Total average policy cost on a "good" object, in the range
      0 through 1.0 + <em>E</em>
     */
    abstract public double getPolicyCost0(int i);

    /** Returns the total average policy cost of the i-th policy
      on a "bad" object.
      
      @param i The policy position, ranging from -1 to policies.length
      @return Total average policy cost on a "bad" object, in the
      range 0 through 1.0 
     */
    abstract public double getPolicyCostOnBad(int i);

    /** Returns the total average policy cost of the i-th policy
      on a set of objects in which the fraction of "bad" objects is 
      equal to this frontier's {@link #context}.pi.
      
      @param i The policy position, ranging from -1 to policies.length
      @return Total average policy cost on a large set objects in
      which pi*|set| are "bad" and (1-pi)*|set| are "good". The value
      is in the range 0 through 1.0 + (1-pi)*E
     */
    abstract public double getPolicyCostPi(int i);

    /** How many non-trivial policies make this frontier */
    abstract public int length();
  
    abstract Frontier getFrontier();
    /** An array of policies - may be "live" or copy */
    abstract public PolicySignature[] getPolicies();
    /** A copy of the array of policies - safe to modify */
    abstract PolicySignature[] getPoliciesCopy();

    /** The frontier context stores certain parameters of the
	algorithm used to consttruct this frontier, as well as the
	value of pi (expected proportion of "bad" objects) for which the
	frontier was constructed.
    */
    public FrontierContext context;
    
    /** Accesses the value of pi from the {@link FrontierContext} */
    public double getPi() { return context.pi; }
    /** Accesses {@link FrontierContext#getInspectCostPi()} */
    public double getMaxCost() { return context.getInspectCostPi(); }
    /** Accesses the value of eps from the {@link FrontierContext} */
    public double getEps() { return context.eps; }

    //static final double triangleEps = 1e-9;

    /** By how much is policy No. i more expensive than policy
     * No. (i-1), on "good" items?
     */
    double getIncrementInCost0(int i) {
	return getPolicyCost0(i) - getPolicyCost0(i-1);
    }

    /** By how much is policy No. i more expensive than policy
     * No. (i-1), on "bad" items?
     */
    double getIncrementInCostOnBad(int i) {
	return getPolicyCostOnBad(i) - getPolicyCostOnBad(i-1);
    }

    /** By how much is policy No. i's detection rate higher than that of policy No. (i-1)?
     */
    double getIncrementInDetectionRate(int i) {
	return getDetectionRate(i) - getDetectionRate(i-1);
    }
    
    /** Computes the total cost of the policy that has device q in front, and
	the policy This.policies[ used[i]-1 ] attached to the i-th channel of
	q. (So, used[i]=0 means: use the trivial zero-cost R policy on the
	i-th channel)

    */
    PolicySignature combiCost(Test q, int used[]) {
	double c= q.getCost(),  //cost of running test q
	    e = context.multiPi? Double.NaN: q.getCost(),
	    d=	 0;
	int k=0;

	while( k <  used.length) {
	    if (k>0 && used[k] > used[k-1]) throw new AssertionError("used["+k+"] > used["+(k-1)+"]");
	    int i=k;
	    while( i<used.length && used[i]== used[k]) i++;

	    if (used[k] > 0) {
		double gr = q.getGRange(k, i-1),  br =  q.getBRange(k, i-1);
		c +=gr* getPolicyCost0(used[k]-1);
		if (context.multiPi) e += br*getPolicyCostOnBad(used[k]-1);
		d += br * getDetectionRate(used[k]-1);
	    }
	    k = i;
	}
	return new PolicySignature(c,e,d);
    }

    double combiDetectionRate(Test q, int used[]) {
	double sum = 0;
	int k=0;

	while( k <  used.length) {
	    if (k>0 && used[k] > used[k-1]) throw new AssertionError("used["+k+"] > used["+(k-1)+"]");
	    int i=k;
	    while( i<used.length && used[i]== used[k]) i++;
	    if (used[k] == 0) {
	    } else if (used[k] == length()+1) {
		sum += q.getBRange(k, i-1); // detect all
		if (getDetectionRate(used[k]-1) != 1) throw new AssertionError();
	    } else { 
		sum += q.getBRange(k, i-1) * getDetectionRate(used[k]-1);
	    }
	    k = i;
	}
	return sum;
    }


    /** Builds an efficient frontier of a set that includes all
	policies constructed by using Test q as the first stage, and
	any policies from "this" Frontier attached to q's output
	channels.

	This method is only used at pi=0. (There is a different one in
	MultiPi).
    */
    Frontier testFusion(Test q) {
	// How many channels in Q? 
	int m = q.getM();

	// number of segments in the Frontier f - that is, the number of
	// non-trivial policies plus 1.
	final int fSegCount = length() + 1;

	double qCost = q.getCost();

	final int mv = m*fSegCount; 
	int used[] = new int[m];

	// we start with the implicit lowest-cost (0,0) policy, and keep
	// adding "Inspect" to the channels in the order of the decreasing 
	// .... ratio
	PolicySignature lastPolicy = Policy.RELEASE;

	//----------------------------------------------------
	// Order the m*fSegCount segments in the order of increasing 
	// (b[i]/g[i]) * (delta(DetectionRate,j)/delta(Cost,j)).
	//
	// {iOrderd[k]=i, jOrdered[k]=j} means: the k-th segment of the
	// compound-device efficient frontier uses the j-th segment of the
	// EF of the device attached to the i-th channel of the front-end
	// device q. Both i and j are 0-based.
	int iOrdered[] = new int[mv]; 
	int jOrdered[] = new int[mv]; 
	boolean skippable[] = new boolean[mv]; 

	double h[] = new double[m];
	double invh[] = new double[m];
	for(int i=0; i<m; i++) {
	    h[i] = q.getB(i)/q.getG(i);
	    invh[i] = q.getG(i)/q.getB(i);
	}

	// Precompute delta(DetectionRate)/delta(Cost) ratios, and
 	// their inverse, for all policies in f. One of them may be
	// infinity (on vertical/horizontal sections), but at least the
	// other one will be a regular number
	double r[] = new double[fSegCount];
	double invr[] = new double[fSegCount];
	for(int j=0; j<fSegCount; j++) {
	    double dd=getIncrementInDetectionRate(j), dc=getIncrementInCost0(j);
	    r[j]=dd/dc;
	    invr[j]=dc/dd;
	}
	
	// used to skip some vertices that sit on virtually straight-line
	// sections of the frontier
	final double ratioEps = 1e-14;
	double lastRatio = 0, lastInvRatio=0;

	for(int pos=0; pos< mv; pos++) {
	    // find the not-yet-"used" segment with the largest g/b ratio
	    double maxRatio = 0;
	    int iFound = -1;
	    for(int i=0; i<m; i++) {
		if (used[i] == fSegCount) {
		    continue;
		} 
		double ratio = h[i]*r[used[i]];
		if (iFound < 0 || ratio > maxRatio) {
		    iFound = i;
		    maxRatio = ratio;
		} 
	    }
	    
	    if (iFound < 0) throw new AssertionError("max not found?!");
	    double invMaxRatio = invh[iFound] * invr[ used[iFound]];

	    // Was the current max (computationally) same as the
	    // previous one?  We assume equality if either
	    // |r1-r2|<eps, or |(1/r1)-(1/r2)|<eps, where eps=ratioEps
	    // is a special very tight tolerance value
	    
	    boolean skipPrev = (pos > 0) &&  
		((lastRatio <= 0.5)?
		 (Math.abs(maxRatio-lastRatio) <ratioEps) :
		 (Math.abs( invMaxRatio - lastInvRatio) < ratioEps));

	    if (skipPrev) {
		skippable[pos-1] = true;
	    } else {
		lastRatio = maxRatio;
		lastInvRatio = invMaxRatio;
	    }

	    // record pair (i, used[i])
	    iOrdered[pos] = iFound;
	    jOrdered[pos] = used[iFound]++;
	    skippable[pos] = false;
	}

	if (Frontier.debug) for(int pos=0; pos< mv; pos++) {
	    System.out.println("Ordered["+pos+"]=("+iOrdered[pos] + " " + 
			       jOrdered[pos] + " " + 
			       h[iOrdered[pos]] * r[ jOrdered[pos]] + " " +
			       skippable[pos] + ")");
	}


	//----------------------------------------------------
	for(int i=0; i<m; i++) used[i] = 0;

	PolicySignature pp[] = new PolicySignature[mv];
	int pCnt =0;

	for(int k=0; k < mv;k++) {
	    int i = iOrdered[k];
	    int j = jOrdered[k];
	    used[i] = j+1;

            // this policy sits on the straight line between the previous and
	    // the next one, so we don't need to use it in the hull
	    // construction
	    if (skippable[k]) continue; 

	     PolicySignature z = combiCost(q, used);
	    if (z.c >= context.getInspectCostPi()) break; // reject this and the rest
	    //double delta = combiDetectionRate(q, used);
	    //if (delta!=ced[2]) throw new AssertionError("delta="+delta+", ced[2]=" + ced[2]);

	    // FIXME: inelegant, but should work: if signaturesOnly==false,
	    // we store regular Frontiers
	    if (!Options.signaturesOnly) {
		z = ((Frontier)this).makePolicy(q, used, z.c, z.e, z.d);
	    }

	    if (pCnt > 0) {
		// cost equality may result from a vertical (first) segment
		// of a ROC curve; (small) backwardation from a rounding error
		if ( z.c <= pp[pCnt-1].c ) {

		    if ( pp[pCnt-1].c - z.c > 1e-8) {
			throw new AssertionError("Backwardation in fusion from " +  pp[pCnt-1] + " to " + z);
		    }

		    if ( z.d >  pp[pCnt-1].d) { // replace last element
			pp[pCnt-1] = z;
			continue;
		    } else {  // skip
			continue;
		    }
		} 
	    }
	    pp[pCnt++] = z;
	}

	Frontier ff = new Frontier(pp, context, pCnt);
	ff.selectNecessaryVerticesFromSortedList(); //triangleEps);
	
	if (Frontier.debug) {
	    System.out.println("TestFusion: ======================");
	    System.out.println("TestFusion: test=" + q);
	    System.out.println("TestFusion: downstream frontier=" + this);
	    System.out.println("TestFusion: resulting  frontier=" + ff);
	    System.out.println("TestFusion: ======================");
	}
	return ff;
    }

    /** Checks that this sequence of policies does indeed describe an
	extreme frontier in the (Cost(Pi), DetectionRate) plane
	@return true if it is indeed an extreme frontier
    */
    boolean validate() {
	PolicySignature[] po = getPolicies();

	double oldD=0, oldCpi=0;
	PolicySignature prev = Policy.RELEASE, prev2=null;

	for(int i=0; i<length(); i++) {
	    double d = getDetectionRate(i);
	    double cpi = getPolicyCostPi(i);	    

	    if (i>0) {

		double def1=0, def2=0;

		if (cpi < oldCpi) {
		    System.out.println("Frontier validation failed: ("+oldCpi+","+oldD+")  is followed by ("+cpi+","+d+")");
		    return false;
		} else if (cpi > context.getInspectCostPi()) {
		    System.out.println("Frontier validation failed: cost for ("+cpi+","+d+")>inspectCost=" + context.getInspectCostPi());
		    return false;

		} else if ((def1=prev.compareToRay(prev2, po[i], context.pi))<0) {
		    System.out.println("Frontier validation failed: policy["+
				       (i-1)+"] (" + prev.toShortString(context.pi)+ ") is below the ray from policy["+(i-2)+"] "+prev2.toShortString(context.pi)+
				       " to policy["+(i)+"] (" + po[i].toShortString(context.pi)+ "); defect="+ (-def1));
		    return false;
		} else if ((def2=prev.compareToRay(Policy.RELEASE,po[i],context.pi))<0){
		    System.out.println("Frontier validation failed: policy["+
				       (i-1)+"] (" + prev.toShortString(context.pi)+ ") is below the ray from RELEASE to policy["+(i)+"] (" + po[i].toShortString(context.pi)+ "; defect="+ (-def2) +"; def1=" + (-def1));
		    return false;
		}
	    } 


	    oldD = d;
	    oldCpi = cpi;
	    prev2 = prev;
	    prev = po[i];
	}
	return true;
    }

    /** Used for sorting Policies with respect to their cost at a
     * certain value of Pi */
    static class PSCostComparator implements Comparator<PolicySignature>  {
	private double pi;
	PSCostComparator(double _pi) { pi = _pi; }

	public int compare(PolicySignature o1, PolicySignature o2) {
	    double x = o1.getPolicyCost(pi) - o2.getPolicyCost(pi);
	    return (x<0) ? -1 : (x>0) ? 1 : 
		(int)Math.signum(o1.getDetectionRate() - o2.getDetectionRate());
	}
 
    }

    /** Reorders vertices, and maybe drops some, for them to form a
	frontier under a given pi value. This is used in multi-pi
	experiments, to convert a frontier obtained for one value of
	pi to a proper frontier for another value.
     */
    Frontier realign(FrontierContext newContext) {
	PolicySignature[] po = getPoliciesCopy();

	// sort with respect to cost for this pi
	Arrays.sort(po, new PSCostComparator(newContext.pi));

	Frontier f = new Frontier(po, newContext); // has redundant ones
	f.selectNecessaryVerticesFromSortedList();
	return f;
    }

    public static PolicySignature[] trim( PolicySignature[] pp, int pCnt) {
	if (pCnt == pp.length) return pp;
	PolicySignature w[] = new 	PolicySignature[pCnt];
	for(int i=0; i<pCnt; i++) w[i] = pp[i];
	return w;
    }

    /** Ranging between 0.5 and 1 (if E=0), or between 0.5*(1+E) and (1+E) 
	in the general case
     */
    public double areaUnderCurve() {
	double s=0;
	for(int i=0; i<length(); i++) {
	    s += getDetectionRate(i) * (getPolicyCostPi(i+1)-getPolicyCostPi(i-1));
	}
	s += (getPolicyCostPi( length()) - getPolicyCostPi( length()-1));
	return s/2;
    }

    /** Given a budget, what's the detection rate? And a combination
	of what policies it is based on? 

	This method is used in the web demo only. */
    public DetectionRateForBudget detectionRateForBudget( double budget, 
							  boolean canMix) {
	return new  DetectionRateForBudget(getPolicies(), context, budget, canMix);
    }

}
