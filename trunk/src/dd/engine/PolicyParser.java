package dd.engine;

import java.text.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;


public class PolicyParser {
    
    /** All sensors */
    Test[] sensors;
    /** All sensors, accessible by name */
    HashMap<String, Test> h=new HashMap<String, Test>();

    public PolicyParser(Test[] _sensors) throws DDParseException {
	sensors = _sensors;
	for(Test q: sensors) {
	    String name = q.getName();	    
	    if (h.get(name) != null) {
		throw new  DDParseException("Can't construct a PolicyParser, because the sensor list contains two sensors with identical name `"+name+"': " + h.get(name) + " and " + q);
	    }
	    h.put(name, q);
	}
    }

    Pattern pTest = Pattern.compile("\\s*\\(\\s*([A-Za-z][A-Za-z0-9_]*)\\s*:\\s*(.*)\\)\\s*");    	
    Pattern pIR = Pattern.compile("\\s*([IR])\\b\\s*");
    Pattern pMult = Pattern.compile("\\s*([0-9]+)\\s*\\*");

    private PolicySignature ir2pol(String x) {
	if (x.equals("I")) return Policy.INSPECT;
	else if (x.equals("R")) return Policy.RELEASE;
	else throw new IllegalArgumentException();
    }

    /** Parses a description of the tree, of the kind produced by
	{@link #printTreeString(PrintWriter)}

	<p>
	Syntax:
	<pre>
	<em>TREE</em> := I | R |  ( <em>TEST</em> : {[<em>multiplier</em> *] <em>TREE</em>}<sup>*</sup> )
	 <em>TEST</em> := <em>an alphanumeric identifier other than I or R</em>
	 <em>multiplier</em> := <em>an integer</em>
	</pre>
	E.g.
	<pre>
	I
	R
	(A: I R)
	(A: 2*I R)
	(B: I 3*(C: I R) 2*R)
	</pre>
    */
    public PolicySignature parseTree(CharSequence s) throws DDParseException {
	int[] allowed = new int[sensors.length];
	for(int i=0; i<sensors.length; i++) allowed[i]= sensors[i].getNCopies();
	return parseSubtree(s, allowed);
    }

    /**
       @param allowed[] Max allowed multiplicities for sensors. We
	must make sure that we never use the same i-th sensor more
	than its allowed multiplicity, allowed[i], in any path in the
	decision tree.
     */
    private PolicySignature parseSubtree(CharSequence s, int[] allowed) throws DDParseException {
	Matcher m = pIR.matcher(s);
	if (m.matches()) return ir2pol( m.group(1));

	m = pTest.matcher(s);
	if (!m.matches()) throw new DDParseException(s, "Can't parse the string as either I, R, or (TEST: ....)");

	String sensorName  = m.group(1);
	CharSequence w = s.subSequence(m.start(2), m.end(2)); 

	Test q = h.get(sensorName);
	if (q==null) {
	    throw new DDParseException("Can't identify sensor name `"+sensorName+"' with any of the known sensor names");
	}

	
	int j= sensorIndex(q);
	allowed[j] --;
	if (allowed[j] <0) throw new DDParseException("Policy has too many occurences of sensor " + q.getName() +"; only " + q.getNCopies() + " is allowed");

	Vector<PolicySignature> ch = new Vector<PolicySignature>();

	// count parentheses
	int start = 0;
	while(start < w.length()) {
	    CharSequence look = w.subSequence( start, w.length());
	    m = pMult.matcher(look);

	    int mult = 1;
	    if (m.lookingAt()) {
		start += m.end();
		mult = Integer.parseInt(m.group(1));
	    }

	    while(start<w.length()&& Character.isWhitespace(w.charAt(start))) {
		start++;
	    }
	    
	    look = w.subSequence( start, w.length());
	    m = pIR.matcher(look);
	    PolicySignature z=null;
	    if (m.lookingAt()) {
		z = ir2pol(m.group(1));
		start += m.end();
	    } else if (w.charAt(start) == '(') {
		int depth = 1;
		start++;
		int end = start;
		while( depth > 0 ) {
		    if (end >=  w.length())  throw new DDParseException(s, "mismatched parentheses in subexpression '"+ w.subSequence(start-1,w.length()) +"'?");
		    char c = w.charAt(end++);
		    if (c=='(') depth++;
		    else if (c==')') depth--;
		}
		z =  parseSubtree(w.subSequence(start-1, end), allowed);
		start = end;
	    } else {
		throw new DDParseException(s, "Can't parse subexpression as a sequence of sub-policies ("+w+")");
	    } 

	    for(int k=0; k<mult; k++) ch.addElement( z);

	}
	allowed[j] ++;	
	return Policy.constructPolicy(q, ch.toArray(new PolicySignature[0]));
    }

    // FIXME: ought to use a hashtable instead of linear search...
    private int sensorIndex(Test q) {
	for(int i=0; i<sensors.length; i++) {
	    if (q == sensors[i]) return i;
	}
	throw new IllegalArgumentException("Test " + q + " is not in the sensor array");
    }

    /** Makes sure that we never use the same sensor more than once
	(or, more precisely, more than its allowed multiplicity) in any path
	in the decision tree.
     */
    /*
    private void depthChecker(PolicySignature p) throws DDParseException {{
	int[] allowed = new int[sensors.length];
	for(int i=0; i<sensors.length; i++) allowed[i]= sensors[i].getNCopies();
	depthChecker(p, allowed);
    }

    private void depthChecker(PolicySignature _p, int[] allowed) throws DDParseException {
	if (_p.isTrivial()) return;
	else if (_p instanceof Policy) {
	    Policy p = (Policy)_p;
	    int j= sensorIndex(p.q);
	    allowed[j] --;
	    if (allowed[j] <=0) throw new DDParseException("Policy has too many occurences of sensor " + p.q +"; only " + p.q.getNCopies() + " is allowed");
	    depthChecker(p, allowed);
	    allowed[j] ++;	
	} else throw new IllegalArgumentException("What kind of policy is that? " + _p);
    }
    */

}
