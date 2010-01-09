package dd.test;

import java.io.*;
import dd.engine.*;

/** This is a sample application to demonstrate reading a sensor
    description file and a policy file. The two command-line arguments
    should be a config file of the same format used by the Frontier
    Finder (i.e., a file that contains list of sensor file names,
    possibly with multiplicities), and a policy file, which contains
    description of policies in a format similar to what can be saved
    by the frontier finder.

    <p>The config file should describe all sensors that will occur in
    the policies.

    <p>Sample config file:
    <pre>
 3*sensorPBK.txt
    </pre>

    (It says that there are 3 sensors with identical costs and ROC
    curves, whose description is to be found in the file sensorPBK.txt
    in the same directory as the config file.

    <P> More config files and sensor files can be found in the
    subdirectories of the "sensors" directory in the distribution.
   
    <p>Sample policy file:
    <pre>
 (PBK: (PBK: I R) R)
 (PBK: (PBK: I (PBK: I R)) R)
 (PBK: (PBK: I (PBK: I R)) (PBK: (PBK: I R) R))
 ...
    </pre>

 */
public class ParserTest {

    public static void main(String [] argv) throws Exception {
	if (argv.length != 2) {
	    System.out.println("Usage: java dd.test.ParseTest config_file policy_file");
	    System.exit(1);
	}
	String config = argv[0];
	
	Test[] sensors = Main.readSensorData(config);
	//System.out.println("Read " + sensors.length + " sensors");
	
	PolicyParser pp = new PolicyParser(sensors);
	
	
	String fname = argv[1];
	FileReader in = new FileReader(fname);
	
	BufferedReader br = new BufferedReader( in );
	String line;
	
    
	while((line = br.readLine())!=null ) {
	    //System.out.println("Input = " + line);
	    PolicySignature pol = pp.parseTree(line);
	    //System.out.print("Output= ");
	    System.out.println( line);
	}
    }
}