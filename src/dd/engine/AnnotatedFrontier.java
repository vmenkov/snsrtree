package dd.engine;

import java.util.Calendar;

/** An extension of the Frontier class, it stores some additional
    information about the computation process, for use in
    reporting. */
public class AnnotatedFrontier extends Frontier {
    /* Computation parameters */
    int maxDepth;
    /* Computation timing */
    Calendar startTime, endTime; 
    public AnnotatedFrontier(PolicySignature[] p, FrontierContext _context, int _maxDepth,
		      Calendar _startTime, Calendar _endTime) {
	super(p, _context);
	maxDepth = _maxDepth;
	startTime = _startTime;
	endTime = _endTime;	
    }

    AnnotatedFrontier(PolicySignature[] p, FrontierContext _context, int _maxDepth,
		      Calendar _startTime) {
	this(p, _context, _maxDepth, _startTime, Calendar.getInstance());
    }

    public AnnotatedFrontier(FrontierInfo f, int _maxDepth,  Calendar _startTime) {
	this(f.getPolicies(), f.context, _maxDepth, _startTime, Calendar.getInstance());
    }

    /** Returns the recorded time when the frontier computation started */
    public Calendar getStartTime() { return startTime; }
    /** Returns the recorded time when the frontier computation ended */
    public Calendar getEndTime() { return endTime; }
    /** Returns the recorded value of maxDepth used in the
     * construction of this frontier */
    public double getMaxDepth() { return maxDepth; }
    /** Returns the time taken by the computer to construct the
     * frontier, in milliseconds. This is based on the wall clock
     * time, (not CPU time), so should be taken with a grain of salt
     * in a multitasking environment.
     */
    public double runtimeMsec() {
	return endTime.getTimeInMillis() - startTime.getTimeInMillis(); 
    }
    
}

