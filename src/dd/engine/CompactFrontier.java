package dd.engine;

import java.io.*;

/** This is a compact representation of the (c,d) curve - same data
    that are stored in a {@link Frontier} when signaturesOnly=true, but in a
    more compact format. As in Frontier, since every convex hull
    includes the two trivial policies, (0,0) and (1,1), we don't
    explicitly store them in the representation.
 */

public class CompactFrontier extends FrontierInfo {

    /** 2 or 3, depending on whether we need to store e beside c and d */
    private final int Z;

    /** Compared to {@link dd.engine.Frontier}, 
	data[Z*i] = polices[i].c,
	data[Z*i+1] = polices[i].d.
	(And, if Z==3,  data[Z*i+2] = polices[i].e)
     */
    double data[];
    
    /** Creates the compact representation of a given frontier */
    CompactFrontier(Frontier f) {
	this(f,2);
    }

    /** @param _Z  If 3 (rather than 2), we also store cost-on-bad */
    CompactFrontier(Frontier f, int _Z) {
	Z = _Z;
	if (Z<2 || Z>3) throw new IllegalArgumentException();
	context = f.context;
	data = new double[f.length() * Z];
	for(int i=0; i<f.length(); i++) {
	    data[Z*i] = f.getPolicyCost0(i);
	    data[Z*i+1] = f.getDetectionRate(i);   	    
	}
	if (Z==3) {
	    for(int i=0; i<f.length(); i++) {
		data[Z*i+2] = f.getPolicyCostOnBad(i);    
	    }
	}
    }

    Frontier getFrontier() { 
	return new Frontier(getPolicies(), context);
    }

    public PolicySignature[] getPolicies() { 
	PolicySignature pp[] = new PolicySignature[length()];
	for(int i=0; i<length(); i++) {
	    pp[i] = getPolicy(i);
	}
	return pp;
    }

   public PolicySignature[] getPoliciesCopy() { 
       return getPolicies();
   }

    public PolicySignature getPolicy(int i) { 
	return (i==-1)? Policy.RELEASE :  
	    i==length()? context.INSPECT: Z==2? 
	    new PolicySignature( data[Z*i], data[Z*i+1]):
	    new PolicySignature( data[Z*i], data[Z*i+2], data[Z*i+1]);
    }

    final public double getDetectionRate(int i) {
	return (i== -1) ? 0 : i==length()? context.INSPECT.d : data[Z*i+1];
    }

    final public double getPolicyCost0(int i) {
	return (i== -1) ? 0 : i==length()? context.INSPECT.c : data[Z*i];
    }

    final public double getPolicyCostOnBad(int i) {
	if (Z!=3) throw new AssertionError("Compact rep does not store cost on bad");
	return (i== -1) ? 0 : i==length()? context.INSPECT.e : data[Z*i + 2];
    }

    final public double getPolicyCostPi(int i) {
	if (Z!=3) throw new AssertionError("Compact rep does not store cost on bad");
	if (i==-1) return 0;
	else if ( i==length()) return context.getInspectCostPi() ;
	double c = data[Z*i];
	return  c + context.pi * (data[Z*i + 2] - c);
    }
    
    /** How many non-trivial policies are stored */
    public int length() {
	return data.length/Z;
    }
    

}