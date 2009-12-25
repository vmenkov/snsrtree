package dd.engine;

import java.io.*;
import java.util.*;
import java.util.regex.*;


/** 
    The main class for the DNDO Frontier Finder batch
    application. (For the GUI application, see {@link dd.gui.DDGUI}

    <p>
    Usage:
    <pre>
    java [-Dvs=VM1|VM2|EB1][-Deps=0.0001] [-DmaxDepth=n][-DsignaturesOnly=false] [-Dverbsity=0|1|...] dd.engine.Main config.txt
    </pre>
    
    <p> For the explanation of vertex skipping methods (-Dvs=...), see
    {@link VSMethod}.

    <p>Recent versions:
    <ul>
    <li>1.7.8 -  (2009-10-22)
    <li>1.7.9 -  (2009-12-15) - more features for Web UI
    </ul>


 */

public class Main  {

    public static final String version = "1.7.9"; // (2009-12-15)
    
    public static void main(String[] argv) throws IOException, DDException {

	String config = "config.txt";
	if (argv.length >  0) config = argv[0];

	Test[] test = readSensorData(config);

	System.out.println("config=" + config + "; eps=" + Options.getEps() + ", signaturesOnly=" + Options.signaturesOnly);
	Frontier f = Frontier.buildFrontier(approximateSensors(test));

	if (Options.verbosity>0) f.print(System.out);
	else System.out.println("Computed frontier with " + f.policies.length + " non-trivial policies");
    }


    /** The top-level input routine. Reads the config file first, and then
        create a sensor for each sensor file mentioned there.
     */
    public static Test[] readSensorData(String config) throws IOException {

	int numberFiles = countTitles(config); //number of sensors being used
	File configFile = new File(config);
	File parentDir = configFile.getParentFile();
	
	int cnt[] = new int[numberFiles];
	String[] sensorTitle = getTitles(configFile, numberFiles, cnt);

	Test[] test = new Test[numberFiles];
        
	for(int i = 0; i<numberFiles; i++)    {
	    test[i] = new Test(new File(parentDir, sensorTitle[i]), cnt[i]); 
	}
	return test;
    }


    public static String[] getTitles(File inputFile, int n, int[] count) throws IOException {
	
	Pattern p = Pattern.compile("([0-9]+)\\s*\\*\\s*(\\S.*)");

	String[] titles= new String[n];

	FileReader in = new FileReader(inputFile);
	    
	BufferedReader br = new BufferedReader( in );
	String line;
	
	for(int i = 0; (i<n) && (line = br.readLine())!=null; ) {
	    line = line.trim();
	    if (line.equals(""))  continue;

	    Matcher m = p.matcher(line);
	    if (m.matches()) {
		count[i] = Integer.parseInt(m.group(1));
		titles[i] = m.group(2);
		System.out.println("Sensor file " + titles[i] + ", "  + count[i]+" copies");
	    } else {
		count[i] = 1;
		titles[i] = line;
		System.out.println("Sensor file " + titles[i] + ", once");
	    }
	    i++;
	}

	return titles;
    }


    public static int countTitles(String config) throws IOException{

	int a = 0;

	File inputFile = new File(config);
	if (!inputFile.canRead()) 
	    throw new IOException("File '" + config +"' does not exist or is not readable");
	BufferedReader br = new BufferedReader( new FileReader(inputFile));
	String line;
	while ((line=br.readLine()) != null) {
	    if (!line.trim().equals("")) 	a++;
	}
	br.close();

	return a;
    }
    
    /** Approximate the sensors, if required
	@return The original sensor array, or an array of simplified ones, as appropriate
     */
    public static Test[] approximateSensors(Test[] sensors) {
	Test[] actualSensors =  sensors;
	if (Options.epsAppliesToSensors) {
	    System.out.println("Approximating sensors, using eps=" + Options.getEps());
	    actualSensors =  new Test[ sensors.length];
	    for(int i=0; i<sensors.length; i++) {
		actualSensors[i] = sensors[i].approximate(Options.getVSMethod(),
							  Options.getEps());

		if (actualSensors[i].getM() == sensors[i].getM()) {
		    System.out.println("Sensor["+i+"], " +  sensors[i].getM() + " channels, no change");
		} else {
		    System.out.println("Sensor["+i+"], " +  sensors[i].getM() + " channels, simplified to " +actualSensors[i].getM() + " channels" );
		}
	    }
	}
	return  actualSensors;
    }

 
}