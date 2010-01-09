package dd.test;

import java.io.*;
import dd.engine.*;
import dd.gui.PresentedFrontier;

/** This is a sample application to demonstrate reading a complete
    {@link dd.engine.Frontier frontier} description. It can parse the
    complete description of a frontier saved by Frontier Finder,
    including the description of the sensors based on which the
    frontier is composed, and the policies that make the frontier.

    <p>Sample input file (saved from the Frontier Finder):
    <pre>
----------- INPUTS: ----------------------
A set of 2 sensors.
Sensor[1], name=A, multiplicity=1:
{A: c=0.05 ((0.2 0.6) (0.4 0.8) (1.0 1.0) )}
Sensor[2], name=B, multiplicity=1:
{B: c=0.01 ((0.4 0.6) (1.0 1.0) )}
------------ OPTIONS: ---------------------
eps=1.0E-6
maxDepth=2.0
------------ RUNTIME: ---------------------
Frontier computation started at  2010-01-06 23:11:18.880
Frontier computation finished at 2010-01-06 23:11:18.896
Wall-clock runtime = 16.0 msec
-------------- OUTPUT: ---------------------
Fold=true
Frontier contains 5 non-trivial policies
[POLICY ] (policyCost, detectionRate) policy_tree
-------------------------
[RELEASE] (0 0) R
[POLICY 0] (0.11 0.36) (B: (A: I 2*R) R)
[POLICY 1] (0.25 0.6) (A: I 2*R)
[POLICY 2] (0.332 0.72) (A: I (B: I R) R)
[POLICY 3] (0.45 0.8) (A: 2*I R)
[POLICY 4] (0.68 0.92) (B: I (A: 2*I R))
[INSPECT] (1 1) I
-------------------------
    </pre>

 */
public class ParseFrontierTest {


    /** Just for testing */
    static public void main(String [] argv) throws IOException, DDParseException {
	if (argv.length != 1) throw new AssertionError("no file name");
	FileReader in = new FileReader(argv[0]);	    
	BufferedReader br = new BufferedReader( in );
	System.out.println("Reading " + argv[0]);	    
	PresentedFrontier f =  PresentedFrontier.readFrontier(br);
	in.close();

	/*
	int i=0;
	for( Test q: f.lastSensorsUsed ) {
	    System.out.println("Read sensor["+i+"]: " + q);
	    i++;
	}
	*/

	PrintWriter w = new PrintWriter(System.out);
	((PresentedFrontier)f).saveFrontier(w, 0);


    }

}