package dd.engine;

import java.io.*;
import java.util.*;


/** A FrontierContext instance contains some information used in
 * computing a frpntier, which is shared by a number of frontiers.

 */

final public class FrontierContext {
    final VSMethod vs;
    final double eps;
    /** The frontier, as computed, was meant to be an extreme frontier for 
     this value of pi. (Should not be mutable beyond the constructor) */
    public final double pi;

    /** If true, the frontiers we generate may be used in the future for
	other, non-zero values of pi - which mean that we must compute and
	store cost-on-bad for all policies throughout        
    */
    final boolean multiPi;

    /** This is a link to Policy.INSPECT, as it existed during the creation
	of the context. It is saved here because Policy.INSPECT may change
	in the future if the user changes E via the GUI. */
    PolicySignature INSPECT;

    public FrontierContext( boolean _multiPi, double _pi, VSMethod _vs, double _eps) {
	multiPi = _multiPi;
	pi = _pi;
	vs = _vs;
	eps = _eps;
	INSPECT = Policy.INSPECT;
    }

    public double getInspectCostPi() {
	return INSPECT.getPolicyCost(pi);
    }

    /** Crteates a new context, with a different pi */
    public FrontierContext changePi(double _pi) {
	return new  FrontierContext(multiPi, _pi, vs, eps);
    }

}


  