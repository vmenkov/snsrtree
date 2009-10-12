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

		if (canMix && c1 < budget && p2.getDetectionRate()>p1.getDetectionRate()) {
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

}

    