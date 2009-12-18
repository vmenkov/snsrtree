package dd.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import java.io.*;
import java.util.Vector;
import java.text.*;

import dd.engine.*;


//--------------- for SVG output
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;

//--------- and for JPEG output...
import org.apache.batik.transcoder.image.*;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.dom.svg.SVGDOMImplementation;


/** The main Deceptive Detection GUI driver
 */
public class DDGUI extends MyJFrame {

    static boolean debugMode = false; //true;
    DDPanel abs = null;
    JLabel msgLabel = null;
 
    // GUI components - menu items
    private JMenuItem openConfigItem;          // file menu
    private JMenuItem readPiListItem;          // file menu

    private JMenuItem saveFrontierItem;
    private JMenuItem writeFrontierImgItem;
    private JMenuItem saveSensorsItem;
    private JMenuItem exitItem;

    private JMenuItem computeFrontierItem;          // run menu


    private JCheckBoxMenuItem debugItem; // options menu
    private JCheckBoxMenuItem paranoidItem; 
    private JCheckBoxMenuItem signaturesItem; 
    private JCheckBoxMenuItem otherFrontItem; 
    private JCheckBoxMenuItem foldItem; 
    private JCheckBoxMenuItem applyEpsItem;
    private JMenuItem optionsItem;

    public DDGUI() {
	super("Deceptive Detection Policy Frontier Finder, version "+ 
	      Main.version);
	
	JMenuBar menuBar = new JMenuBar();
	setJMenuBar(menuBar);
	MenuHandler menuHandler = new MenuHandler();
	
	// file menu
	JMenu fileMenu = new JMenu("File");
	menuBar.add(fileMenu);
	
	openConfigItem = new JMenuItem("Read Config file");//, 'O' );
	openConfigItem.addActionListener(menuHandler);
	fileMenu.add(openConfigItem);
	
	readPiListItem= new JMenuItem("Read Pi list file");//, 'O' );
	readPiListItem.addActionListener(menuHandler);
	fileMenu.add(readPiListItem);

	fileMenu.addSeparator(); // separate line

	saveFrontierItem = new JMenuItem("Save Frontier (text file)");//, 'S' );
	saveFrontierItem.addActionListener(menuHandler);
	fileMenu.add(saveFrontierItem);

	writeFrontierImgItem = new JMenuItem("Write Frontier (image)");//, 'W' );
	writeFrontierImgItem.addActionListener(menuHandler);
	fileMenu.add(writeFrontierImgItem);

	saveSensorsItem = new JMenuItem("Save (approximated) sensors");//, '' );
	saveSensorsItem.addActionListener(menuHandler);
	fileMenu.add(saveSensorsItem);
	


	fileMenu.addSeparator(); // separate line
	
	exitItem = new JMenuItem("Exit"); //, 'x' );
	exitItem.addActionListener(menuHandler);
	fileMenu.add(exitItem);

	// run menu
	JMenu runMenu = new JMenu("Run");
	menuBar.add(runMenu);

	computeFrontierItem = new JMenuItem("Compute Frontier"); //, 'x' );
	computeFrontierItem.addActionListener(menuHandler);
	runMenu.add(computeFrontierItem);

	// options menu
	JMenu optionsMenu = new JMenu("Options");
	menuBar.add(optionsMenu);

	debugItem = new JCheckBoxMenuItem("Debug", Frontier.debug);
	debugItem.addActionListener(menuHandler);
	optionsMenu.add(debugItem);

	paranoidItem = new JCheckBoxMenuItem("Paranoid checking", Options.paranoid);
	paranoidItem.addActionListener(menuHandler);
	optionsMenu.add(paranoidItem);
	optionsMenu.addSeparator(); // separate line

	foldItem = new JCheckBoxMenuItem("Compact tree print & plot", Options.fold);
	foldItem.addActionListener(menuHandler);
	optionsMenu.add(foldItem);

	signaturesItem = new JCheckBoxMenuItem("Save (C,D) only", Options.signaturesOnly);
	signaturesItem.addActionListener(menuHandler);
	optionsMenu.add(signaturesItem);

	otherFrontItem = new JCheckBoxMenuItem("Show subset frontiers", showSubsetFrontiers);
	otherFrontItem.addActionListener(menuHandler);
	optionsMenu.add(otherFrontItem);

	applyEpsItem = new JCheckBoxMenuItem("Apply eps to sensors", Options.epsAppliesToSensors);
	applyEpsItem.addActionListener(menuHandler);
	optionsMenu.add( applyEpsItem);

	optionsItem = new JMenuItem("More algo options..."); //, 'o' );
	optionsItem.addActionListener(menuHandler);
	optionsMenu.add(optionsItem);



	int width = 1000;
	int height = 900;
	setSize(width, height);
	setLocation(100, 0);

	abs = new DDPanel(width, height, this);
	msgLabel = new JLabel("Welcome to Deceptive Detection Frontier Finder! Please use the File menu to read a config file");

	Container c = getContentPane();
	c.setLayout(new BorderLayout());
	c.add(msgLabel, BorderLayout.SOUTH);
	c.add(abs);
	setVisible(true);
    }

    public static void debug(String s) {
	if (debugMode)	    System.out.print(s);
    }

    public static void debugln(String s) {
	if (debugMode)	    System.out.println(s);
    }

    public static void main(String argv[]) {
	DDGUI app = new DDGUI();
	app.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		}
	    });
	if (argv.length > 0) {	    
	    app.readConfig(new File(argv[0]));
	}
    }

    /** Class MenuHandler: handling all menu events. */
    private class MenuHandler implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    // file menu
	    if (e.getSource() == openConfigItem)
		openConfig();
	    else if (e.getSource() ==readPiListItem)
		readPiList();
	    else if (e.getSource() == saveFrontierItem)
		saveFrontier();
	    else if (e.getSource() == writeFrontierImgItem)
		writeFrontierImage();
	    else if (e.getSource() == saveSensorsItem)
		saveSensors();
	    else if (e.getSource() == exitItem)
		System.exit(0);
	    else if (e.getSource() == computeFrontierItem)
		computeFrontier();
	    else if (e.getSource() == debugItem) {
		Frontier.debug = debugItem.isSelected();
		System.out.println("Setting debug " + (Frontier.debug? "on": "off"));
	    } else if (e.getSource() == paranoidItem) {
		Options.paranoid = paranoidItem.isSelected();
		System.out.println("Setting Options.paranoid = "+Options.paranoid);
	    } else if (e.getSource() == foldItem) {
		Options.fold = foldItem.isSelected();
		System.out.println("Setting Options.fold=" + Options.fold);
	    } else if (e.getSource() == signaturesItem) {
		Options.signaturesOnly = signaturesItem.isSelected();
		System.out.println("Setting Options.signaturesOnly=" + Options.signaturesOnly);
	    } else if (e.getSource() ==  otherFrontItem) {
		showSubsetFrontiers = otherFrontItem.isSelected();
		System.out.println("Setting showSubsetFrontiers=" + showSubsetFrontiers);
	    } else if (e.getSource() ==  applyEpsItem) {		    
		Options.epsAppliesToSensors= applyEpsItem.isSelected();
		System.out.println("Setting Options.applyEpsItem=" + Options.epsAppliesToSensors);
	    } else if (e.getSource() == optionsItem) {
		optionDialog();
	    }
	}
    }

    void setLabel(String msg ) {
	setLabel(msg, msg);
    }

    void setLabel(String msg, String msgLong) {
	debugln(msgLong);
	msgLabel.setText(msg);
	msgLabel.repaint(2); // trying to to make sure the label shows right away
    }

    //------------------
    File filedir = new File(".");

    Test sensors[] = null;
    //private 
    PresentedData presented = null;

    boolean showSubsetFrontiers = false;

    // for multi-pi
    //AnnotatedFrontier[] surface = null;
    

    //----------------------------


    /** 
     * Reads sensor info for the sensors listed in the config file.
     */
    public void openConfig() {
	JFileChooser fileChooser = new JFileChooser(filedir);
	fileChooser.setDialogTitle("Open a config file");

	//-- this class is only available in JDK 1.6
	/*
	FileNameExtensionFilter filter = new FileNameExtensionFilter(
	    "Text files", "txt", "conf", "cnf");
	fileChooser.setFileFilter(filter);
	*/

	int returnVal = fileChooser.showOpenDialog(this);
	
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    File file = fileChooser.getSelectedFile();
	    readConfig(file);
	}
    }

    private void readConfig(File file) {

	String filepath = file.getPath();
	filedir = file.getParentFile();

	setLabel("Reading config file "+ filepath);
	
	try {
	    
	    sensors = Main.readSensorData(filepath);
	    
	    setLabel("Successfully read data for "+ sensors.length + " sensors from '"+filepath+"'");
	    
	} catch (Exception e) {
	    String msg0 = "Error when reading config file " + filepath;
	    String msg = msg0 + ":\n" + e.getMessage();
	    setLabel(msg0, msg);
	    e.printStackTrace(System.err);
	    msg += "\nPlease see the standard output for the stack trace";
	    JOptionPane.showMessageDialog(this,msg);
	}

    }


    public void readPiList() {
	JFileChooser fileChooser = new JFileChooser(filedir);
	fileChooser.setDialogTitle("Open a Pi list file");

	//-- this class is only available in JDK 1.6
	/*
	FileNameExtensionFilter filter = new FileNameExtensionFilter(
	    "Text files", "txt", "conf", "cnf");
	fileChooser.setFileFilter(filter);
	*/

	int returnVal = fileChooser.showOpenDialog(this);
	String filepath = "";
	
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    File file = fileChooser.getSelectedFile();
	    filepath = file.getPath();
	    filedir = file.getParentFile();

	    setLabel("Reading pi list file "+ filepath);
	    
	    try {

		Options.readPiList(file);

		setLabel("Successfully read Pi list ("+Options.formatPiList()+") from "+filepath);

	    } catch (Exception e) {
		String msg0 = "Error when reading Pi list file " + filepath;
		String msg = msg0 + ":\n" + e.getMessage();
		setLabel(msg0, msg);
		e.printStackTrace(System.err);
		msg += "\nPlease see the standard output for the stack trace";
		JOptionPane.showMessageDialog(this,msg);
	    }
	    
        }
    }


    public void optionDialog() {

	OptionDialog.showDialog(this,
				optionsItem,
				"Current option values:",
				"Frontier Finder options");

	return;
    }

    public void computeFrontier() {
	if (sensors == null) {
	    String msg="No sensors have been read yet. Please read some in first!";	    
	    System.out.println(msg);
	    JOptionPane.showMessageDialog(this,msg);
	} else {
	    NumberFormat secFmt = new DecimalFormat("#.000");
	    try {

		Test[] actualSensors =  Main.approximateSensors(sensors);

		if (Options.piListIsTrivial()) {

		    setLabel("Computing frontier...");
		    System.out.println("Computing frontier...");

		    Vector<AnnotatedFrontier> otherFrontiers=
			showSubsetFrontiers? new Vector<AnnotatedFrontier>() : null;

		    AnnotatedFrontier frontier = Frontier.buildFrontier(actualSensors, otherFrontiers);
		    System.out.println("Computed frontier");
		    if (otherFrontiers!=null) {
			System.out.println("|others|=" +otherFrontiers.size()); 
		    }
		    double msec = frontier.runtimeMsec();
		    String ts = (msec < 1000) ?  ""+  msec + " msec":
			secFmt.format(0.001 * msec) + " sec";
			   
		    setLabel("Computed frontier, with  "+
			     frontier.length() +
			     " non-trivial policies, S="+ frontier.areaUnderCurve() +", in "+ts+
			     ". To save frontier to file, use the File menu. To view details of a policy, click the mouse on its circle.");
		    //debugln(frontier.toString());
		    if (debugMode) {
			frontier.print(new PrintWriter(System.out));
		    }
		    presented = new PresentedFrontier(actualSensors, frontier, otherFrontiers);
		} else { // Build a surface!
		    double[] piList = Options.getPiList();
		    AnnotatedFrontier[] 
		    surface =
			Frontier.buildFrontiersMultiPi
			(piList, actualSensors,
			 Options.getZeroPiContext(),
			 Options.getMaxDepth(SensorSet.maxSetSize(actualSensors)) );
		    //(showSubsetFrontiers? otherFrontiers:null) );

		    // FIXME: so far showing just 1. And the rest?
		    AnnotatedFrontier frontier = surface[0];

		    Vector<AnnotatedFrontier> highPiFrontiers = 
			new Vector<AnnotatedFrontier>();
		    for(int i=1; i<surface.length; i++) {
			highPiFrontiers.add(surface[i]);
		    }

		    presented = new PresentedFrontier(actualSensors, frontier, 
						      highPiFrontiers);
		    //null);
		    double msec = frontier.runtimeMsec();
		    String ts = (msec < 1000) ?  ""+  msec + " msec":
			secFmt.format(0.001 * msec) + " sec";

		    String msg =
			"Computed surface, so many non-trivial policies: (";
		    for(AnnotatedFrontier f: surface) {
			msg += " " + f.getPolicies().length;
		    }
		    msg += "), top S="+ frontier.areaUnderCurve() +", in "+ts+
			". To save frontier to file, use the File menu. To view details of a policy, click the mouse on its circle.";
		    setLabel(msg);

		}
		debugln("Requesting repaint...");
		abs.repaint();
	    } catch (DDException e) {
		setLabel("RUN: error when computing frontier: "
				   + e.getMessage());
		e.printStackTrace(System.err);
	    }
	    
	}
    }


    /** Saves the frontier description as a text file */
    void saveFrontier() {

	if (presented == null) {

	    String msg=(sensors == null) ?
		"No sensors have been read yet. Please read some in first, then compute the frontier, and then you can save it." :
		"No frontier has been computed yet. To save the frontier, you need to compute the frontier first (using the Run menu).";
	    System.out.println(msg);
	    JOptionPane.showMessageDialog(this,msg);

	    return;
	}

	JFileChooser fileChooser = new JFileChooser(filedir);
	fileChooser.setDialogTitle("Specify an existing or new text file to (over)write");

	int returnVal = fileChooser.showOpenDialog(this);
	String filepath = "";
	
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    File file = fileChooser.getSelectedFile();
	    filepath = file.getPath();
	    filedir = file.getParentFile();

	    setLabel("Saving frontier to file "+ filepath);
	    
	    try {		
		if (presented instanceof PresentedFrontier) {
		    PrintWriter w = new PrintWriter(file);
		    ((PresentedFrontier)presented).saveFrontier(w);
		    w.close();
		    setLabel("Frontier has been written to file "+ filepath);
		} else {
		    // FIXME
		    setLabel("Saving surfaces not supported");
		    throw new AssertionError("Saving surfaces not supported");
		} 
	    } catch (Exception e) {
		setLabel("Failed to write frontier to file " + filepath);
		System.out.println( e.getMessage());
		e.printStackTrace(System.err);
	    }
	    
        }
    }

    /*
    void doSaveFrontier(    PresentedData presented, PrintWriter w) {
	AnnotatedFrontier frontier = null;
	if (presented instanceof PresentedFrontier) {
	    frontier = ((PresentedFrontier)presented).frontier;
	} else {
	    // FIXME
	    throw new AssertionError("Saving surface not supported");
	} 
	w.println("----------- INPUTS: ----------------------");
	w.println("A set of " +  presented.lastSensorsUsed.length + " sensors.");
	for(int i=0; i< presented.lastSensorsUsed.length; i++) {
	    w.println("Sensor["+(i+1)+"], name="+
		      presented.lastSensorsUsed[i].getName()+":");
	    w.println(presented.lastSensorsUsed[i]);
	}
	w.println("------------ OPTIONS: ---------------------");
	w.println("eps=" + frontier.getEps());		
	w.println("maxDepth=" + frontier.getMaxDepth());		
	w.println("------------ RUNTIME: ---------------------");
	w.println("Frontier computation started at  " + 
		  timeFmt.format(frontier.getStartTime().getTime()));
	w.println("Frontier computation finished at " + 
		  timeFmt.format(frontier.getEndTime().getTime()));
	double msec = frontier.runtimeMsec();
	w.println("Wall-clock runtime = " + 
		  (msec < 1000 ?
		   ""+  msec + " msec":
		   ""+ (0.001 * msec) + " sec"));
			   

	w.println("-------------- OUTPUT: ---------------------");
	//w.println( frontier );
	frontier.print(w);
    }
    */	
	
    /** Saves the frontier as an SVG image file 
     */
    void writeFrontierImage() {
	if (presented == null) {

	    String msg=(sensors == null) ?
		"No sensors have been read yet. Please read some in first, then compute the frontier, and then you can save it." :
		"No frontier has been computed yet. To save the frontier, you need to compute the frontier first (using the Run menu).";
	    System.out.println(msg);
	    JOptionPane.showMessageDialog(this,msg);

	    return;
	}

	SVGWriter.writeImage(this, abs);
    }

    /** Saves the sensors actually used in the last frontier finding
     */
    void saveSensors() {
	if (presented == null) {

	    String msg=(sensors == null) ?
		"No sensors have been read yet. Please read some in first, then compute the frontier, and then you can save it." :
		"No frontier-finding (which involves sensor approximation) has been carried out yet. You need to compute the frontier first (using the Run menu).";
	    System.out.println(msg);
	    JOptionPane.showMessageDialog(this,msg);

	    return;
	}

	JFileChooser fileChooser = new JFileChooser(filedir);
	fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);


	fileChooser.setDialogTitle("Specify an existing or new directory to save approximated-sensor files into");

	int returnVal = fileChooser.showOpenDialog(this);
	String filepath = "";
	
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    File file = fileChooser.getSelectedFile();
	    filepath = file.getPath();
	    filedir = file.getParentFile();

	    setLabel("Saving sensors to dir "+ filepath);
	    
	    try {

		if (!file.exists()) {
		    if (!file.mkdirs()) 	{
			String msg="Failed to create directory " + filepath;
			System.out.println(msg);
			JOptionPane.showMessageDialog(this,msg);
			return;
		    }
		}

		if (!file.exists() || !file.isDirectory() || !file.canWrite()) {
		    String msg= filepath + " is not a (writeable) directory";
		    System.out.println(msg);
		    JOptionPane.showMessageDialog(this,msg);
		    return;	  
		} 

		for(int i=0; i<presented.lastSensorsUsed.length;  i++) {
		    Test q=presented.lastSensorsUsed[i];
		    String name = "sensor-" + i+ "-" +q.getName() + ".txt";
		    File g = new File(file, name);
		    FileOutputStream w = new FileOutputStream(g); 
		    q.print(w);
		    w.close();
		    setLabel("Description of (approximated) sensor " + 
			     q.getName() + "  has been written to file "+ 
			     g.getPath());
		    if (q.getOrig() != null) {
			name = "map-" + i+ "-" +q.getName() + ".txt";
			g = new File(file, name);
			w = new FileOutputStream(g); 
			q.printApproxMap(new PrintStream( w ));
			w.close();
			setLabel("Approximation map for sensor " + 
				 q.getName() + "  has been written to file "+
				 g.getPath());
		    }
		}

		setLabel("Description of all "+presented.lastSensorsUsed.length+" (approximated) sensors have been written to files in dir " +  filepath);

	    } catch (Exception e) {
		setLabel("Failed to write some or all sensor descriptions to files in directory " + filepath);
		System.out.println( e.getMessage());
		e.printStackTrace(System.err);
	    }
	    
        }


    }

}
