package dd.engine;

import java.io.*;


/** Some methods used in building the multi-pi surface (represented as
 * a set of Frontier cross-sections)
 */

class MultiPiFusion {
    
    /** Builds an efficient frontier of a set that includes all
	policies constructed by using Test q as the first stage, and
	any policies from the Frontier f[i] attached to q's i-th output
	channels.
    */
    static Frontier testFusion(Test q, FrontierInfo f[], FrontierContext context) {
	// How many channels in Q? 
	int m = q.getM();

	if (m != f.length) throw new AssertionError();

	int mv = 0; 
	for(int k = 0; k<m; k++) {
	    // number of segments in the Frontier f - that is, the number of
	    // non-trivial policies plus 1.
	    final int fSegCount = f[k].length() + 1;
	    mv += fSegCount;
	}

	double qCost = q.getCost();

	int used[] = new int[m];

	// we start with the implicit lowest-cost (0,0) policy, and keep
	// adding "Inspect" to the channels in the order of the decreasing 


	// .... ratio
	PolicySignature lastPolicy = Policy.RELEASE;

	//----------------------------------------------------
	// Order the mv segments in the order of increasing 
	// (b[i]/g[i]) * (delta(DetectionRate,j)/delta(Cost(pi),j)).
	//
	// {iOrderd[k]=i, jOrdered[k]=j} means: the k-th segment of the
	// compound-device efficient frontier uses the j-th segment of the
	// EF of the device attached to the i-th channel of the front-end
	// device q. Both i and j are 0-based.
	int iOrdered[] = new int[mv]; 
	int jOrdered[] = new int[mv]; 
	boolean skippable[] = new boolean[mv]; 

	//double h[] = new double[m];
	//for(int i=0; i<m; i++) {
	//    h[i] = q.getB(i)/q.getG(i);
	//}

	// precompute delta(DetectionRate)/delta(Cost) ratios for all policies
 	// in f
	double [][] r = new double[m][], invr = new double[m][];
	for(int k=0; k<m; k++) {
	    final int fSegCount = f[k].length() + 1;
	    r[k] = new double[fSegCount];
	    invr[k] = new double[fSegCount];
	    for(int j=0; j<fSegCount; j++) {

		double a = q.getB(k) * f[k].getIncrementInDetectionRate(j);
		double b = 
		    (context.pi*q.getB(k)*f[k].getIncrementInCostOnBad(j) + 
		     (1-context.pi)*q.getG(k)*f[k].getIncrementInCost0(j));
		r[k][j]= a/b;
		invr[k][j]= b/a;

	    }
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
		final int fSegCount = f[i].length() + 1;
		if (used[i] == fSegCount) {
		    continue;
		} 
		double ratio = r[i][used[i]];
		if (iFound < 0 || ratio > maxRatio) {
		    iFound = i;
		    maxRatio = ratio;
		} 
	    }

	    if (iFound < 0) throw new AssertionError("max not found?!");
	    double invMaxRatio = invr[iFound][used[iFound]];
	    
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
		int i = iOrdered[pos];
		System.out.println("Ordered["+pos+"]=("+i + " " + 
				   jOrdered[pos] + " " + 
				   r[i][ jOrdered[pos]] + " " +
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

	    double c0 = combiCost(q, f, used, 0.0);
	    double c = combiCost(q, f, used, context.pi);
	    double e = combiCost(q, f, used, 1.0);

	    double delta = combiDetectionRate(q, f, used);

	    if (c >= context.getInspectCostPi()) break; // reject this and the rest

	    // FIXME: inelegant, but should work: if signaturesOnly==false,
	    // we store regular Frontiers
	    PolicySignature z = Options.signaturesOnly? 
		new PolicySignature(c0, e, delta) : 	
		makePolicy(q, f, used, c0, e, delta);

	    if (pCnt > 0) {

		double back = pp[pCnt-1].getPolicyCost(context.pi) - c;

		// cost equality may result from a vertical (first) segment
		// of a ROC curve; (small) backwardation from a rounding error
		if ( back > 0) {

		    if ( back > 1e-8) {
			String msg = "Backwardation in fusion from " +  pp[pCnt-1].toShortString2(context.pi) + " to " + z.toShortString2(context.pi);
			//if ( pp[pCnt-1].c - z.c > 1e-6) 
			    throw new AssertionError(msg);
			    //else System.out.println(msg);
		    }

		    if ( z.d >  pp[pCnt-1].d) { 
			// replace last element
			pp[pCnt-1] = z;
			continue;
		    } else {
			// skip
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
	    System.out.println("TestFusion: downstream frontiers: " + f.length + " of them");
	    System.out.println("TestFusion: resulting  frontier=" + ff);
	    System.out.println("TestFusion: ======================");
	}
	return ff;
    }

    /** Computes the total cost of the policy that has device q in front, and
	the policy f[i].policies[ used[i]-1 ] attached to the i-th channel of
	q. (So, used[i]=0 means: use the trivial zero-cost R policy on the
	i-th channel)
    */
    static double combiCost(Test q, FrontierInfo f[], int used[], double pi) {
	double sum = q.getCost(); // The cost of running test q
	for(int i=0; i< used.length; i++) {
	    if (used[i] > 0) {
		sum += (1-pi)*q.getG(i) * f[i].getPolicyCost0(used[i]-1) +
		    pi*q.getB(i) * f[i].getPolicyCostOnBad(used[i]-1);
	    }
	}
	return sum;
    }

    static double combiDetectionRate(Test q, FrontierInfo f[], int used[]) {
	double sum = 0;
	for(int i=0; i< used.length; i++) {
	    if (used[i] == 0) {
	    } else if (used[i] == f[i].length()+1) {
		sum += q.getB(i); // detect all
	    } else {
		sum += q.getB(i) * f[i].getDetectionRate(used[i]-1);
	    }
	}
	return sum;
    }

    /** Generates a policy that has test q as its root, with certain
	policies from f[] attached to q's output channels. This method
	is only used in multi-Pi frontier construction, and is only
	invoked when Options.signaturesOnly==false, i.e. we actually
	need to generate a full tree.
	@param q The root test of the new policy
	@param f List of frontiers. A policy from f[i] will be attached to q's 
	i-th output channel
	@param used An array whose i-th element specifies which policy
	from f[i] is to be attached to the i'th output
	channel of q. Value used[i]=0 refers to RELEASE, used[i]=1 to
	policy[0], and so on, with used[i]=f[i].policy.length meaning that
	INSPECT is to be attached to the i'th channel of q.
     */
    static Policy makePolicy(Test q, FrontierInfo[] f, int used[], double c, double e, double d) {
	if (q.getM() != used.length) throw new AssertionError("Length mismatch:q.getM()=" + q.getM() +", used.length=" + used.length );
	if (used.length != f.length) throw new AssertionError("Length mismatch:f.length=" + f.length +", used.length=" + used.length );
	PolicySignature [] outputs= new PolicySignature[used.length];
	for(int i=0; i< used.length; i++) {
	    // An exception will be thrown if we have screwed up with types
	    // (which we should not)
	    outputs[i] = ((Frontier)f[i]).getPolicy(used[i]-1);
	}
	return new Policy(q, outputs, c, e, d);
    }


}
