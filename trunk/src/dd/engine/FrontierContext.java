package dd.engine;

import java.io.*;
import java.util.*;


/** A FrontierContext instance contains some information used in
 * computing a frpntier, which is shared by a number of frontiers. In
 * a most common API application, it is used simply to specify the
 * vertex-skipping method and the value of it vertex-skipping
 * parameter (eps). To do that, use the constructor {@link
 * #FrontierContext( VSMethod _vs, double _eps)}.

 */

final public class FrontierContext {
    /** The vertex-skipping method. Used to simplify the genrated frontier,
	removing some vertexes that don't change its shape much. */
    public final VSMethod vs;
    /** The "epsilon" parameter for vertex-skipping */
    public final double eps;
    /** The frontier, as computed, was meant to be an extreme frontier for 
	this value of pi. */
    public final double pi;

    /** If true, the frontiers we generate may be used in the future for
	other, non-zero values of pi - which mean that we must compute and
	store cost-on-bad for all policies throughout        
    */
    public final boolean multiPi;

    /** This is a link to Policy.INSPECT, as it existed during the creation
	of the context. It is saved here because Policy.INSPECT may change
	in the future if the user changes E via the GUI. */
    PolicySignature INSPECT;

    FrontierContext( boolean _multiPi, double _pi, VSMethod _vs, double _eps) {
	multiPi = _multiPi;
	pi = _pi;
	vs = _vs;
	eps = _eps;
	INSPECT = Policy.INSPECT;
    }

    /** Creates a context with the specified vertex-skipping method and eps,
	for zero-pi model (i.e., very low percentage of "bad" objects in the 
	input stream).

	@param _vs Vertex-skipping method. Recommended value is VSMethod.VM1
	@param _eps Epsilon for vertex-skipping. Recommended value is
	1e-6; in combination with vs=VSMethod.VM1, it will mean that a
	vertex with both cost and detection rate with 1e-6 from an
	already recorded vertex will be ignored ("skipped"). Using a
	zero value is not recommended, because of potential
	computational problems.
    */
    public FrontierContext( VSMethod _vs, double _eps) {
	this(false, 0, _vs, _eps);
    }


    public double getInspectCostPi() {
	return INSPECT.getPolicyCost(pi);
    }

    /** Creates a new context, based on the current one, but with a
     * different pi */
    public FrontierContext changePi(double _pi) {
	return new  FrontierContext(multiPi, _pi, vs, eps);
    }

    FrontierContext changePiMulti(double _pi) {
	return new  FrontierContext(true, _pi, vs, eps);
    }


}


  