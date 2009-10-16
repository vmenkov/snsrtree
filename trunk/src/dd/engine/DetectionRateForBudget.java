package dd.engine;

import java.io.*;
import java.util.*;

/** Given a budget, what's the detection rate? And a combination
    of what policies it is based on? */
public class  DetectionRateForBudget {
    public double givenBudget, actualBudget, detectionRate;
    /** The policy in question is a mix, p1*w + p2 * (1-w) */
    public PolicySignature p1=null, p2=null;
    public double w=1;

    public DetectionRateForBudget(PolicySignature _p1, 
				  PolicySignature _p2, 
				  FrontierContext context,
				  double _w) {
	p1 = _p1;
	p2 = _p2;
	w = _w;
	actualBudget = p1.getPolicyCost(context.pi);
	detectionRate =  p1.getDetectionRate();
	if (w<1) {
	    actualBudget= actualBudget*w + p2.getPolicyCost(context.pi) * (1-w);
	    detectionRate = detectionRate*w + p2.getDetectionRate() * (1-w);
 	} 
	givenBudget = actualBudget;	   
    }

    public DetectionRateForBudget(PolicySignature p[],
				  FrontierContext context,
				  double budget, 
				  boolean canMix) {
	if (budget < 0 || budget > context.getInspectCostPi() ) throw new IllegalArgumentException("budget outside of the legal range [0,"+context.getInspectCostPi()+"]");

	givenBudget = budget;

	p1 =  Policy.RELEASE;
	for(int i=0; i<=p.length; i++) {
	    p2 = (i<p.length) ? p[i] : context.INSPECT;
	    double c1 = p1.getPolicyCost(context.pi),
		c2 = p2.getPolicyCost(context.pi);
	    if (c2 > budget) {
		// p2 is over the budget, which means that p1 is
		// the best policy within budget

		//&& p2.getDetectionRate()>p1.getDetectionRate()) {

		if (canMix && c1 < budget) {
		    w = (c2-budget)/(c2-c1);
		    actualBudget = budget;
		    detectionRate = p1.getDetectionRate()  * w  + 
			p2.getDetectionRate()  * (1-w);
		    return;
		} else {
		    break;
		}
	    }
	    p1 = p2;
	}
	// Either we got here with "break", or at the end of the loop
	// (i.e.,every policy is within budget, so let's pick the last
	// one)
	w = 1;
	p2=null;
	actualBudget = p1.getPolicyCost(context.pi);
	detectionRate =  p1.getDetectionRate();
    }

    /** Tries to find the mixed policy that provides a given detection
     * rate. Assumes that full mixing is allowed, from I to R. */
    static public DetectionRateForBudget 
	budgetForDetectionRate(PolicySignature p[],
			       FrontierContext context,
			       double d) {
	if (d < 0 || d > 1 ) throw new IllegalArgumentException("Requested detection rate  outside of the legal range [0,1]");


	PolicySignature p1 =  Policy.RELEASE;
	for(int i=0; i<=p.length; i++) {
	    PolicySignature  p2 = (i<p.length) ? p[i] : context.INSPECT;
	    double d1 = p1.getDetectionRate(),d2 = p2.getDetectionRate();

	    if (d2==d) {
		return new DetectionRateForBudget(p2, null, context, 1);
	    } else if (d2>d) {
		return new DetectionRateForBudget(p1,p2,context,(d2-d)/(d2-d1));
	    }
	    p1 = p2;
	}
	// Ought not ever happen, as we include I and R, and mixes with them
	throw new IllegalArgumentException("Policy set does not cover desired detection rate "+ d);
    }

}

    