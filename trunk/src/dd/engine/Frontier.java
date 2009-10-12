package dd.engine;

import java.util.Calendar;
import java.io.*;
import java.util.Vector;


/** A Frontier describes a convex hull over a set of Policies. It always
 * includes the two trivial policies, (0,0) and (1,1+E), which we don't
 * explicitly store in the representation.
 */

public class Frontier extends FrontierInfo {

    /** A Frontier is represented as simply an array of Policies,
	arranged in the order of increasing cost, forming the frontier
	of the convex hull. The points (0,0) and (1,1+E), present in any
	frontier, are omitted, to save space. Thus, a trivial (no
	devices) frontier can be represented by a 0-length vector. The
	array may store merely PolicySignature objects, or actual
	{@link dd.engine.Policy Policies}, depending on the parameters
	to buildFrontier */
    PolicySignature policies[];
    
    Frontier getFrontier() { return this; }

    public PolicySignature[] getPolicies() { return policies; }
    public PolicySignature[] getPoliciesCopy() { 
	PolicySignature[] q = new PolicySignature[policies.length];
	for(int i=0; i<policies.length; i++) q[i] = policies[i];
	return q;
    }
    /** Returns the i-th policy of this frontier. The "non-trivial"
      policies are numbered with base 0, meaning that the argument
      i=-1 referes to the (implied) RELEASE policy, and
      i=policies.length refers to the (implied) INSPECT policy.     
     */
    public PolicySignature getPolicy(int i) { 
	return (i==-1)? Policy.RELEASE :  
	    i==policies.length? context.INSPECT: policies[i]; 
    }

    public double getDetectionRate(int i) {
	return (i==-1) ? 0: (i==length())? 1 : policies[i].getDetectionRate();
    }

    public double getPolicyCost0(int i) {
	return getPolicy(i).getPolicyCost();
    }

    public double getPolicyCostOnBad(int i) {
	return getPolicy(i).getPolicyCostOnBad();
    }

    public double getPolicyCostPi(int i) {
	 return getPolicy(i).getPolicyCost(context.pi);
    }
    
    /** How many non-trivial policies are stored */
    public int length() {
	return policies.length;
    }
    
    /** Can be modified via GUI */
    static public boolean debug = false;

    /** Creates the frontier for an empty (zero-test) device. Having
	no tests, we only have two actions (polices): I and R, which
	are not stored explicitly.
    */
    public Frontier(FrontierContext _context) {
	// nothing needs to be done here!
	policies = new PolicySignature[0];
	context = _context;
    }
    
    Frontier(PolicySignature pp[],FrontierContext _context) {
	policies = pp;
	context = _context;
    }

    Frontier(PolicySignature pp[], FrontierContext _context, int pCnt) {
	policies = trim(pp,pCnt);
	context = _context;
    }

    /* This constructor creates a "one-test frontier", i.e. the
     * efficient frontier based on policies that use one or more
     * channels of the test. This frontier may contain as many as n-1
     * non-trivial policies, where n is the number of the test's
     * channels; however, it may contain fewer, since, due to the
     * test's cost, some policies may be less efficient than simply
     * the linear combination of another policy and "INSPECT" (i.e.,
     * sometimes it's cheaper to select some cases for inspection
     * randomly) 

     @param eps Ignored, unless mandatory approximation is in effect
     
     @param pi

    */
    public Frontier(Test t, FrontierContext _context) {
	context = _context;
	int m = t.getM();
	double tc = t.getCost(); // cost of the test itself

	PolicySignature p[] = new PolicySignature[m-1]; // we may not need that many, though
	int pCnt = 0;

	for(int i=1;i < m;i++) {
	    
	    PolicySignature w = Options.signaturesOnly? 
		new PolicySignature(t,i): new Policy(t,i);

	    if (pCnt==0) {
		// The first channel is usually added - unless it's 
		// on or below the (0-1) line 
		if (w.isBelowRay(Policy.RELEASE, context.INSPECT, context.pi)) continue;
	    } else if ( w.isBelowRay( p[pCnt-1], context.INSPECT, context.pi)) {
		// this policy is not on the hull - and neither is
		// any other higher-cost policy for this test (because
		// the test's channels are canonically ordered)
		break;
	    } else if (pCnt==1 && !w.isBelowRay(Policy.RELEASE, p[0], context.pi)) {
		// we will be replacing the existing first element, because it
		// was not on the frontier - it was below the line from (0,0)
		// to the new policy
		pCnt=0;
	    }

	    // add the new policy to the frontier
	    p[pCnt++] = w;
	}

	setPolicies(p, pCnt);

	if (Options.verbosity>1) System.out.println("For test " + t + ", generated frontier: " + this);
    }

    /** Sets this frontier from the first pCnt polices in the given array -
	or just reuses the array */
    private void setPolicies(PolicySignature p[], int pCnt) {
	if (pCnt == p.length ) {
	    policies = p;
	} else {
	    policies = new PolicySignature[pCnt];
	    for(int i=0;i<pCnt; i++) policies[i] = p[i];
	} 
    }

    /** Counting set bits in an integer - that is, the size of a set that it
	represents */
    static private int bitCnt(int x, int n) {
	int sum = 0;
	int z=x;
	for(int i=0; i<n; i++) {
	    sum += (z & 1);
	    z = (z>>1);
	}
	return sum;
    }

    static private Frontier combineFrontiers( Vector<Frontier> v) {
	if (v.size() == 0) throw new IllegalArgumentException("combineFrontier on an empty vector!");
	Frontier newHull = new Frontier(v.elementAt(0).context);
	Frontier[] a = v.toArray(new Frontier[0]);
	for(int i=0; i<a.length; i++) {
	    if (Options.paranoid && !a[i].validate())  throw new AssertionError("frontier a[i] no good!");
	}
	newHull.combineFrontiers(a);
	return newHull;
    }

    private void combineFrontiers( Frontier fy) {
	if (Options.paranoid && !validate())  throw new AssertionError("this frontier no good!");
	if (Options.paranoid && !fy.validate())  throw new AssertionError("frontier fy no good!");
	combineFrontiers(new Frontier[] {fy});
    }

    /** Taking this Frontier and another one, each describing a convex
	hull, replaces this frontier with one that describes a convex
	hull above both of them.

	All policies ought to have the same pi
    */
    private void combineFrontiers( Frontier fArray[]) {
	for(Frontier fy: fArray) {
	    if (context.pi != fy.context.pi) {
		throw new IllegalArgumentException("pi mismatch");
	    }
	    
	    if (debug) {
		System.out.println("CombiF: ======================");
		System.out.println("CombiF: this  frontier=" + this);
		System.out.println("CombiF: other frontier=" + fy);
		System.out.println("CombiF: ======================");
	    }
	    PolicySignature x[] = policies;
	    PolicySignature y[] = fy.policies;

	    int px=0;
	    int py=0;
	    // skip the R policy, in case it was stored explicitly
	    while(px < x.length && x[px].d == 0) px++;
	    while(py < y.length && y[py].d == 0) py++;
	    PolicySignature[] z = new PolicySignature[x.length + y.length ];
	    int pz = 0;
	    
	    while(px < x.length || py < y.length) {
		
		PolicySignature nextP;
		if (px == x.length) {
		    nextP = y[py++];
		} else if (py == y.length) {
		    nextP = x[px++];
		} else if (x[px].getPolicyCost(context.pi) < y[py].getPolicyCost(context.pi)) {
		    nextP = x[px++];
		} else if (x[px].getPolicyCost(context.pi) == y[py].getPolicyCost(context.pi)) {
		    if (x[px].d > y[py].d) {
			nextP = x[px++];
			py++;
		    } else {
			nextP = y[py++];
			px++;
		    }
		} else {
		    nextP = y[py++];
		}
		
		if (pz>0 && nextP.getPolicyCost(context.pi) < z[pz-1].getPolicyCost(context.pi)) {
		    throw new AssertionError("" +  nextP.getPolicyCost(context.pi) + "<" + z[pz-1].getPolicyCost(context.pi));
		}
		
		z[pz++] = nextP;
	    }

	    if (pz != z.length) z = trim(z, pz);
	
	    policies = z;
	}

	// Selection and approximation are applied only once, after all
	// vertices have been put into a single list
	selectNecessaryVerticesFromSortedList(); //triangleEps);

	// Additional vertex-skipping, if requested
	if (context.vs==VSMethod.EB1) {
	    approximateEB1();
	} else if  (context.vs==VSMethod.VM2) {
	    approximateVM2();
	}

	if (debug) {
	    System.out.println("CombiF: result frontier=" + this);
	    System.out.println("CombiF: ======================");
	}
    }

    //private int minusJ(int ptr) { return  = ptr & (~ (1<<j)); }

     /** Runs buildFrontier with the eps value from the options file/cmd line
      * option (or the default value)
      */
    public static AnnotatedFrontier buildFrontier(Test t[]) throws DDException {
	return  buildFrontier( t, null);
    }

    public static AnnotatedFrontier buildFrontier(Test t[], Vector<AnnotatedFrontier> others) throws DDException {
	int maxSetSizeOrig = SensorSet.maxSetSize(t);
	return buildFrontier(t, Options.getEps(), Options.getVSMethod(),
			     Options.getMaxDepth(maxSetSizeOrig), others);
    }

   /** This is the main method here: it constructs the efficient frontier of
	all policies that can be created using Tests from t[] 

	@param t An array of tests. The frontier will be constructed
	from policies based on tests from this array. In each array
	element, t[i].nCopies, if set to a value N greater than 1,
	specifies that this array element represents not a single
	sensor, but N sensors with identical detection curves; thus, a
	policy can include up to N sensors identical to t[i]. This is
	simply an optimization over multiple sensors with identical
	detection curves.

	@param eps "Merge" frontier points located within eps from each in
	both Cost and DetectionRate direction. It is desirable to always use a
	non-zero - even if very small - eps, in order to prevent
	near-duplicate points appearing on the hull due to floating-point
	computational errors.

	@param maxDepth Maximum size of decision trees included into
	the frontier. If this value is equal or greater than t.length
	(the number of tests), then it has no effect, as no efficient
	tree constructed from t[] will be higher than t.length anyway.

	@param others If not null, we'll add some intermediate
	frontiers to this vector, for a post-mortem
    */
    public static AnnotatedFrontier 
	buildFrontier(Test t[], double eps, VSMethod vs, int maxDepth, 
		      Vector<AnnotatedFrontier> others) throws DDException {

	final boolean fastPurge = true; //delete old frontiers fast to save mem

	int n = t.length;
	int maxCnt[]  = new int[n];
	for(int i=0; i<n; i++) maxCnt[i] = t[i].nCopies;

	SensorSet.init( maxCnt);
	// max possible size
	int maxSetSizeOrig = SensorSet.maxSetSize();
	if (maxDepth < 0) maxDepth =maxSetSizeOrig;
	// max allowed size
	int maxSetSize = Math.min( maxSetSizeOrig, maxDepth);

	System.out.println("Build frontiers: "+n + " sensors, maxDepth=" + 
			   (maxDepth<0? "ALL" : ""+maxDepth)+
			   ", VS method="+vs+" with eps=" + eps);
	Calendar startTime = Calendar.getInstance();

	if (n >= Integer.SIZE-1) {
	    throw new DDException( "Too many ("+n+") tests to combine. The limit is " + (Integer.SIZE-2));
	}
	// 2^n (or, generally, Product_i(maxCnt_i+1)), i.e. the number
	// of different subsets of the set of all tests in t[]
	int pow = SensorSet.getMaxPolicyCnt();

	// Storing the frontier for each of the 2^n subsets
	FrontierInfo frontiers[] = new FrontierInfo[pow];
	
	FrontierContext context = new FrontierContext(false, 0, vs, eps); 
	// start filling frontiers[]...
	// Empty set:
	frontiers[0] = new Frontier(context); // an empty (R-I) frontier

	if (maxDepth <= 0) return new AnnotatedFrontier(frontiers[0].getPolicies(), 
							frontiers[0].context,
							maxDepth,
							startTime);

	System.out.println("Max possible set size= "+maxSetSizeOrig+", max allowed set size = " +maxSetSize); 

	// Sets of one test
	int totalSavedCnt=0;
	
	for(int i=0; i<n; i++) {
	    SensorSet ss = SensorSet.oneSensorSet(i);
	    frontiers[ss.intValue()] = new Frontier( t[i], context);
	    totalSavedCnt ++;
	    if (Options.verbosity>0) System.out.println("Saved frontier["+ss+"]");
	}
	System.out.println("Generated and saved "+n+" 1-sensor frontiers");


	for(int setSize = 2; setSize <= maxSetSize; setSize++) {
	    int savedCnt = 0;

	    SensorSet ss = SensorSet.firstSetOfSize(setSize);
	    System.out.println("Set size=" + setSize + "; first frontier="+ss);
	    do {
		// ss represents a new set to fill
		if (frontiers[ss.intValue()] != null) {
		    throw new AssertionError("Frontier["+ss+"] is already filled?!");
		}

		Vector<Frontier> v= new Vector<Frontier>();
		for(int j=0;j<n;j++) {
		    // ss\t[j]
		    SensorSet ssMinusTj = ss.minusJ(j);
		    if (ssMinusTj != null) {
			// j indeed was in the set ptr, so let's
			// combine t[j] with ptr\t[j]
			if (frontiers[ssMinusTj.intValue()] == null) {
			    throw new AssertionError("Frontier["+ssMinusTj+"] has not been filled, as expected!");
			}
			v.addElement( frontiers[ssMinusTj.intValue()].testFusion(t[j]));
		    }
		}

		Frontier newHull = combineFrontiers( v);
		if (Options.paranoid && !newHull.validate()) throw new AssertionError("newHull no good");
	    
		// Compress data for storage, if requested
		frontiers[ss.intValue()] = Options.signaturesOnly?
		    new CompactFrontier(newHull, 2) : newHull;

		if (others != null && setSize== maxSetSize-1) {
		    // save the subset's frontier for a post-mortem
		    others.addElement(new AnnotatedFrontier(newHull.policies,
							    newHull.context,
							    maxDepth, startTime));
		}

		savedCnt++;
		totalSavedCnt ++;
		if (Options.verbosity>0) System.out.println("Saved frontier["+ss+"]");
	    } while ( ss.transformToNextSetOfSameSize() ); 

	    System.out.println("Generated and saved "+savedCnt+" " + setSize + "-sensor frontiers");
	    if (fastPurge) { // delete frontiers we don't need anymore	      
		ss = SensorSet.firstSetOfSize(setSize-1);
		do {
		    frontiers[ss.intValue()]=null;
		} while ( ss.transformToNextSetOfSameSize() ); 

		if (n>=10) {
		    // Now is a good time to do garbage collection, since
		    // this is the "locally lowest point" in memory usage
		    System.out.println("GG:");
		    Runtime.getRuntime().gc();
		}

	    }
	}
	System.out.println("Generated and saved "+totalSavedCnt+" frontiers of all sizes");
	// done all sets...
	if (maxDepth >= maxSetSizeOrig) {
	    int ptr = pow-1;
	    if (frontiers[ptr]==null) throw new  AssertionError("The final result, Frontier["+new SensorSet(ptr)+"] has not been filled, as expected!");
	    return new AnnotatedFrontier(frontiers[ptr].getPolicies(), 
					 frontiers[ptr].context,
					 maxDepth, startTime);
	} else {
	    // Since we have not finished building the frontier
	    // including all possible efficient policies, we have to 
	    // create a convex hull of "most complex allowed policies" now
	    Vector<Frontier> v= new Vector<Frontier>();

	    SensorSet ss = SensorSet.firstSetOfSize(maxDepth);
	    do {
		int ptr = ss.intValue();
		if (frontiers[ptr] == null) {
		    throw new AssertionError("Frontier["+ss+"] has not been filled, as expected!");
		}
		v.addElement(frontiers[ptr].getFrontier());
	    } while ( ss.transformToNextSetOfSameSize() ); 

	    Frontier newHull = combineFrontiers(v);

	    return new AnnotatedFrontier(newHull.policies, newHull.context, maxDepth, startTime);
	}
    }

    /** Builds a set of frontiers for a variety of pi values, from 0
	to 1, with some intermediate values.
     */
    public static AnnotatedFrontier[] 
	buildFrontiersMultiPi(double piList[], Test t[], double eps, 
			      VSMethod vs, int maxDepth, 
			      Vector<AnnotatedFrontier> others) throws DDException {

	final boolean fastPurge = true; //delete old frontiers fast to save mem

	// validate piList
	if (piList==null || piList.length < 2 || piList[0]!=0 || piList[piList.length-1] != 1) 
	    throw new AssertionError("Empty or short piList");
	for(int i=1; i<piList.length; i++) {
	    if (piList[i] <= piList[i-1])
		throw new AssertionError("piList values not in increasing order");
	}

	int n = t.length;
	int maxCnt[]  = new int[n];
	for(int i=0; i<n; i++) maxCnt[i] = t[i].nCopies;

	SensorSet.init( maxCnt);
	// max possible size
	int maxSetSizeOrig = SensorSet.maxSetSize();
	if (maxDepth < 0) maxDepth =maxSetSizeOrig;
	// max allowed size
	int maxSetSize = Math.min( maxSetSizeOrig, maxDepth);

	System.out.println("Build frontiers multiPi: "+n + " sensors, maxDepth=" + 
			   (maxDepth<0? "ALL" : ""+maxDepth)+
			   ", VS method="+vs+" with eps=" + eps);
	Calendar startTime = Calendar.getInstance();

	if (n >= Integer.SIZE-1) {
	    throw new DDException( "Too many ("+n+") tests to combine. The limit is " + (Integer.SIZE-2));
	}
	// 2^n (or, generally, Product_i(maxCnt_i+1)), i.e. the number
	// of different subsets of the set of all tests in t[]
	int pow = SensorSet.getMaxPolicyCnt();

	// Storing the frontier for each of the pi values, and for each of 2^n subsets
	FrontierInfo xf[][] = new FrontierInfo[piList.length][];
	for(int i=0; i<xf.length;i++) xf[i]= new FrontierInfo[pow];

	FrontierContext contexts[] = new  FrontierContext[ piList.length];
	for(int j=0; j<piList.length; j++) {
	    contexts[j] = new FrontierContext(true, piList[j], vs, eps);
	    // start filling frontiers[]...
	    // Empty set:
	    xf[j][0] = new Frontier(contexts[j]); // an empty (R-I) frontier
	}

	if (maxDepth <= 0) return annotate(xf, 0, eps, maxDepth, startTime);

	System.out.println("Max possible set size= "+maxSetSizeOrig+", max allowed set size = " +maxSetSize); 

	// Sets of one test
	int totalSavedCnt=0;

	for(int j=0; j<piList.length; j++) {
	    double pi = piList[j];

	    for(int i=0; i<n; i++) {
		SensorSet ss = SensorSet.oneSensorSet(i);
		xf[j][ss.intValue()] = new Frontier( t[i], contexts[j]);
		totalSavedCnt ++;
		if (Options.verbosity>0) System.out.println("Saved frontier(pi="+pi+")["+ss+"]");
	    }
	    System.out.println("[pi="+piList[j]+"] Generated and saved "+n+" 1-sensor frontiers");
	}

	for(int setSize = 2; setSize <= maxSetSize; setSize++) {
	    int savedCnt = 0;

	    SensorSet ss = SensorSet.firstSetOfSize(setSize);
	    System.out.println("Set size=" + setSize + "; first frontier="+ss);
	    do {
		for(int jp=0; jp<piList.length; jp++) {		    
		    // ss represents a new set to fill
		    if (xf[jp][ss.intValue()] != null) {
			throw new AssertionError("Frontier["+jp+"]["+ss+"] is already filled?!");
		    }
		    double basePi = piList[jp];
		    FrontierContext baseContext = contexts[jp];

		    Vector<Frontier> v = new Vector<Frontier>();
		    for(int j=0;j<n;j++) {
			// ss\t[j]
			SensorSet ssMinusTj = ss.minusJ(j);

			// only proceed if j indeed was in the set ptr
			if (ssMinusTj == null) continue;

			// let's combine t[j] with ptr\t[j]
			int subsetJ = ssMinusTj.intValue();
			Test q = t[j];
			// different frontiers (for different,
			// adjusted, pi) need to be attached to each
			// output
			FrontierInfo[] subFrontiers = new FrontierInfo[q.getM()];
			
			for(int k=0; k<subFrontiers.length; k++) {
			    double g = q.getG(k), b = q.getB(k);
			    double adjPi = basePi*b /( basePi*b + g*(1-basePi));
			    if (adjPi < 0 || adjPi> 1) throw new AssertionError("");

			    FrontierContext adjContext = 
				new FrontierContext(true, adjPi, vs, eps);

			    // what's the closest value in piList?
			    int jpAdj = 0;
			    while(jpAdj+1< piList.length && piList[jpAdj+1]<=adjPi){
				jpAdj ++;
			    }			    

			    if (xf[jpAdj][subsetJ] == null) {
				throw new AssertionError("Frontier["+jpAdj+"]["+ssMinusTj+"] has not been filled, as expected!");
			    }

			    Frontier f = xf[jpAdj][subsetJ].realign(adjContext);
			    if (Options.paranoid && !f.validate())  throw new AssertionError("frontier f no good!");

			    if (jpAdj+1 < xf.length && adjPi > piList[jpAdj]) {
				Frontier w = xf[jpAdj+1][subsetJ].realign(adjContext);
				f.combineFrontiers(w);
			    }
			    subFrontiers[k] = f;

			}

			v.addElement(MultiPiFusion.testFusion(t[j], subFrontiers, baseContext));

		    }


		    Frontier newHull = combineFrontiers(v);

		    // Compress data for storage, if requested
		    xf[jp][ss.intValue()] = Options.signaturesOnly?
			new CompactFrontier(newHull, 3) : newHull;
		    
		    savedCnt++;
		    totalSavedCnt ++;
		    if (xf[jp][ss.intValue()].context != baseContext) throw new AssertionError();
		    if (Options.verbosity>0) System.out.println("Saved frontier(pi["+jp+"]="+basePi+")["+ss+"]");
		} //-- loop over pi list
	    } while ( ss.transformToNextSetOfSameSize() ); 

	    System.out.println("[All pi] Generated and saved "+savedCnt+" " + setSize + "-sensor frontiers");
	    if (fastPurge) { // delete frontiers we don't need anymore	      
		ss = SensorSet.firstSetOfSize(setSize-1);
		do {
		    for(FrontierInfo[] frontiers: xf) frontiers[ss.intValue()]=null;
		} while ( ss.transformToNextSetOfSameSize() ); 

		if (n>=10) {
		    // Now is a good time to do garbage collection, since
		    // this is the "locally lowest point" in memory usage
		    System.out.println("GG:");
		    Runtime.getRuntime().gc();
		}

	    }
	}
	System.out.println("Generated and saved "+totalSavedCnt+" frontiers of all sizes");
	
	int destPtr = pow-1;
	// done all sets...
	if (maxDepth >= maxSetSizeOrig) {
	    for(int jp=0; jp<piList.length; jp++) {
		if (xf[jp][destPtr]==null) throw new  AssertionError("The final result, Frontier["+jp+"]["+new SensorSet(destPtr)+"] has not been filled, as expected!");
	    }
	} else {
	    // Since we have not finished building the frontier
	    // including all possible efficient policies, we have to 
	    // create a convex hull of "most complex allowed policies" now (for each pi)
	    for(int jp=0; jp<piList.length; jp++) {

		Vector<Frontier> v = new Vector<Frontier>();
		SensorSet ss = SensorSet.firstSetOfSize(maxDepth);
		
		do {
		    int ptr = ss.intValue();
		    if (xf[jp][ptr] == null) {
			throw new AssertionError("Frontier["+jp+"]["+ss+"] has not been filled, as expected!");
		    }
		    v.addElement(xf[jp][ptr].getFrontier());
		} while ( ss.transformToNextSetOfSameSize() ); 
		
		Frontier newHull = combineFrontiers(v);


		// store it at destPtr, as if it were the true final result
		if (xf[jp][destPtr]!=null) throw new  AssertionError("The final result, Frontier["+jp+"]["+new SensorSet(destPtr)+"] has been filled already, unexpectedly!");
		xf[jp][destPtr] = newHull;
	    }
	}
	return annotate(xf, destPtr, eps, maxDepth, startTime);
    }
    

    /** For multi-pi */
    static private AnnotatedFrontier[] annotate(FrontierInfo[][] xf, int ptr, double eps, int maxDepth, Calendar startTime) {
	AnnotatedFrontier af[] = new AnnotatedFrontier[xf.length];
	Calendar endTime = Calendar.getInstance();
	for(int i=0; i<af.length; i++) {
	    af[i] = new AnnotatedFrontier(xf[i][ptr].getPolicies(), 
					  xf[i][ptr].context,
					  maxDepth,
					  startTime, endTime);
	}
	return af;
    }


    /** Generates a policy that has test q as its root, with certain
	policies contained in this Frontier attached to q's output
	channels. This method is only invoked when
	Options.signaturesOnly==false, i.e. we actually need to
	generate a full tree.
	@param q The root test of the new policy
	@param used An array whose i'th element specifies which policy
	from this Frontier is to be attached to the i'th output
	channel of q. Value used[i]=0 refers to RELEASE, used[i]=1 to
	policy[0], and so on, with used[i]=policy.length meaning that
	INSPECT is to be attached to the i'th channel of q.
     */

    Policy makePolicy(Test q, int used[], double c, double e, double d) {
	if (q.getM() != used.length) throw new AssertionError("Length mismatch:q.getM()=" + q.getM() +", used.length=" + used.length );
	PolicySignature [] outputs= new PolicySignature[used.length];
	for(int i=0; i< used.length; i++) {
	    outputs[i] = getPolicy(used[i]-1);
	}
	return new Policy(q, outputs, c, e, d);
    }


    public String toString() {
	StringBuffer b=new StringBuffer();
	b.append("Fold="+Options.fold+"\n");
	b.append("Frontier contains " + policies.length + " non-trivial policies\n");
		
	b.append("[POLICY ] (policyCost, detectionRate) policy_tree\n");
	b.append("-------------------------\n");
	b.append("[RELEASE] " + Policy.RELEASE.toShortString() + " " + 
		 Policy.RELEASE.toTreeString() + "\n"); 
	for(int i=0; i<policies.length; i++) {
	    b.append("[POLICY "+i+"] "+policies[i].toShortString() + " " + 
		     policies[i].toTreeString() + 		     "\n");
	}	    
	b.append("[INSPECT] " + context.INSPECT.toShortString() + " " + 
		 context.INSPECT.toTreeString() + "\n"); 
	b.append("-------------------------\n");
	return b.toString();
    }

    public void print(PrintStream out) {
	print(new PrintWriter(out));
	out.flush();
    }

    /** Much same thing as toString(), but with immediate printing out the data
	- to save memory */
    public void print(PrintWriter out) {
	out.println("Fold="+Options.fold);
	out.println("Frontier contains " + policies.length + " non-trivial policies");
		
	out.println("[POLICY ] (policyCost, detectionRate) policy_tree");
	out.println("-------------------------");
	out.println("[RELEASE] " + Policy.RELEASE.toShortString() + " " + 
		    Policy.RELEASE.toTreeString() ); 
	for(int i=0; i<policies.length; i++) {
	    out.print("[POLICY "+i+"] "+policies[i].toShortString() + " ");
	    out.println(    policies[i].toTreeString());
	}	    
	out.println("[INSPECT] " + context.INSPECT.toShortString() + " " + 
		 context.INSPECT.toTreeString() ); 
	out.println("-------------------------");
	out.flush();
    }

    /** Assuming that the list of this Frontier's policies is already
	sorted by cost(PI), this methiod replaces it with a shorter
	list that contains only those policies that are truly
	necessary to form a frontier.

	The following simplifications are carried out:
	<ul>

	<li>Policies with cost at or above he cost of INSPECT 
	
	<li> Skipping vertices within vmEps, in both directions, from
	 the last recorded vertex. The threshold vmEps is fixed (eps)
	 if the vertex-skipping mode is VM1; or, if the mode is EB1,
	 it is adjusted to satisfy EB1's vertex-skipping criterion.

	<li> Skipping vertices that are redundant because they are below the
	line running from the previous vertex to the next one.

	<li> Skipping vertices that are redundant because they are
	below the line runing from RELEASE to the next vertex, or from
	the previous vertex to INSPECT

	</ul>

	@param maxDefect If a positive number, interpreted as an
	"area epsilon" within which redundant vertices can be thrown out;
	if the defect exceeds this value, a warning will be printed
     */
    void selectNecessaryVerticesFromSortedList() {

	PolicySignature[] kept = new PolicySignature[ policies.length ];
	int nkept = 0;


	for(int i=0; i<policies.length; i++) {

	    PolicySignature prev = (nkept==0) ? Policy.RELEASE: kept[nkept-1];

	    if (i>0) {
	        if (getPolicyCostPi(i) < getPolicyCostPi(i-1)) {
		    throw new AssertionError("Vertices " + (i-1) + ", " + i +", not in order: " + getPolicyCostPi(i-1) +">" +  getPolicyCostPi(i));
		}
	    }
	   

	    if (getPolicyCostPi(i) >= context.getInspectCostPi()) break; // this and the rest...

	    // If EB1 is in effect, one can still skip vertices
	    // satisfying VM1 with an adjusted eps
	    double vmEps = (context.vs==VSMethod.EB1) ? context.eps * prev.d : 
		(context.vs==VSMethod.VM2) ? context.eps / policies.length :
		context.eps;
	    
	    if (policies[i].isWithinEps( prev, vmEps, context.pi)) {
		// skip as nearly redundant
		continue;
	    }

	    if (Options.paranoid && getPolicyCostPi(i) < prev.getPolicyCost( context.pi)) throw new AssertionError("Vertex("+i+")=" + policies[i] + " is back of the stored vertex kept[" +(nkept-1)+ "]=" + prev);


	    if (Options.paranoid && context.pi == 0 && policies[i].c < prev.c) {
		throw new AssertionError("Vertex("+i+")=" + policies[i].c + ".c < kept[" +(nkept-1)+ "].c=" + prev.c + "; pi="+ context.pi +"; p("+i+")="+ policies[i] +", cost=" + getPolicyCostPi(i)+"; prev="+ prev+", cost=" +  prev.getPolicyCost( context.pi) );
	    }
	

	    if (policies[i].isBelowRay( prev, context.INSPECT, context.pi)) {
		// skip as redundant
		continue;
	    }
	    
	    while( nkept > 0 ) {
		PolicySignature prev2= (nkept==1? Policy.RELEASE : kept[nkept-2]);
		double d = kept[nkept-1].compareToRay( prev2, policies[i], context.pi);

		// This test would be redundant in precise
		// arithmetic... but in floating-point, sometimes it isn't
		double d0 = kept[nkept-1].compareToRay( Policy.RELEASE, policies[i], context.pi);

		if (d > 0 && d0 > 0) break;
		nkept --;
	    }
	    kept[nkept++] = policies[i];
	}
	policies = trim(kept, nkept);

    }


    /** Removes some vertices, replacing the frontier with an approximation
 
	ALGORITHM (EB1):
	<pre>
  For a given input s:

  initialize M := 0;  
  initialize i := s+1;

  while(  i &le; N   AND   (D(i)-D(s))/(C(i)-C(s))  &ge;  M/(1+eps) ) {
     Set  M := max{ M,  ((D(i)-(1+eps)*D(s)) / (C(i)-C(s)) };
     Set  i := i+1;    
  }

  return i(s) := i-1;
  </pre>

    */
    synchronized void approximateEB1() {
	if (policies.length ==0) return;
	PolicySignature z[] = new PolicySignature[policies.length];
	int pz = 0;
	int s = -1;
	while(true) {
	    PolicySignature ps = getPolicy(s);
	    int i=s+1;
	    for(double M=0; i<=policies.length; i++) {
		PolicySignature pj= getPolicy(i);
		if ((pj.d - ps.d)*(1+context.eps) < M*(pj.c - ps.c)) break;
		M = Math.max(M, (pj.d - (1+context.eps)*ps.d)/(pj.c - ps.c));
	    }
	    s = (i-1);
	    if (s < policies.length) {
		z[pz++] = policies[s];
	    } else {
		break; // we don't store INSPECT
	    }
	}
	if (Frontier.debug) System.out.println("EB1: reduced vertex count from " + policies.length + " to "  + pz);
	z = trim(z, pz);
	policies=z;
    }

    /** Removes some vertices */
    synchronized void approximateVM1() {
	if (policies.length ==0) return;
	PolicySignature z[] = new PolicySignature[policies.length];
	int pz = 0;
	for(int i=0; i<policies.length; i++) {
	    if (pz>0 && getPolicy(i).isWithinEps( z[pz-1], context.eps, context.pi)) {
		continue;
	    } else {
		z[pz++] = getPolicy(i);
	    }
	}
	z = trim(z, pz);
	policies=z;
    }

    /** The idea is to limit the area of each excluded triangle to no
	more than eps*c/2, where c is the length of the preserved
	segment of the frontier. This means that the total area of
	excluded triangles, along the entire frontierm, will be no
	more than eps.
     */
    synchronized void approximateVM2() {
	if (policies.length ==0) return;
	PolicySignature kept[] = new PolicySignature[policies.length];
	int nKept = 0;
	double totalS = 0;
	for(int i=0; i<policies.length; ) {
	    PolicySignature base = (nKept==0) ? Policy.RELEASE: kept[nKept-1];
	    double s=0;
	    
	    PolicySignature head = policies[i++];
	    while( i<= policies.length ) {
		PolicySignature next = getPolicy(i);
		double sNew = s + head.compareToRay( base, next, context.pi);
		if (sNew*sNew > context.eps*context.eps * base.hyp2( next, context.pi)) break;
		s = sNew;
		head = next;
		i++;
	    }
		
	    if (head != context.INSPECT) {
		kept[nKept++] = head;
	    }
	    totalS += s;

	}
	policies=trim(kept, nKept);
	System.out.println("VM2: total excluded area=" + totalS);
    }




}