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
    AnnotatedFrontier(PolicySignature[] p, FrontierContext _context, int _maxDepth,
		      Calendar _startTime, Calendar _endTime) {
	super(p, _context);
	//eps = context.eps;
	maxDepth = _maxDepth;
	startTime = _startTime;
	endTime = _endTime;
	
    }
    AnnotatedFrontier(PolicySignature[] p, FrontierContext _context, int _maxDepth,
		      Calendar _startTime) {
	this(p, _context, _maxDepth, _startTime, Calendar.getInstance());
    }

    public AnnotatedFrontier(Frontier f, int _maxDepth,  Calendar _startTime) {
	this(f.policies, f.context, _maxDepth, _startTime, Calendar.getInstance());
    }


    public Calendar getStartTime() { return startTime; }
    public Calendar getEndTime() { return endTime; }
    public double getMaxDepth() { return maxDepth; }
    public double runtimeMsec() {
	return endTime.getTimeInMillis() - 	startTime.getTimeInMillis(); 
    }
    
}

