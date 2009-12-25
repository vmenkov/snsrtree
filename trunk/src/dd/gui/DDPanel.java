package dd.gui;

import java.io.*;
import java.text.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

import dd.engine.*;

//--------------- for SVG output
import org.apache.batik.svggen.SVGGraphics2D;

/**
 * The main panel in the DNDO Frontier Finder interactive GUI window
 * ({@link DDGUI}).
 */
public class DDPanel extends JPanel implements ActionListener, 
				    MouseInputListener,  SVGAwareComponent {
 

    DDGUI parent;
    JPopupMenu popup;
    JMenuItem drawTreeMenuItem, saveBGItem;
    JLabel popupLabel[] = new JLabel[3];

     /** 
      * Constructor for DDPanel. 
      */
    public DDPanel(int width, int height, DDGUI _parent) {
	parent = _parent;
	setPreferredSize(new Dimension(width, height));
	setLayout(new BorderLayout());
	this.addMouseListener(this); // to monitor mouse clicks
	//this.addMouseMotionListener(this); // ??

	//------------- popup menu for information about a policy
	//Create the popup menu.
	popup = new JPopupMenu("Policy details");

	String names[] = {"Policy info", "dD/dC Ratio", "Policy tree"};
	for(int i=0; i<names.length; i++) {
	    popupLabel[i] = new JLabel(names[i]);
	    popup.add(popupLabel[i]);
	}

	drawTreeMenuItem = new JMenuItem("Draw policy tree in a new window");
	drawTreeMenuItem.addActionListener(this);
	popup.add(drawTreeMenuItem);

	saveBGItem = new JMenuItem("Describe policy as a device");
	saveBGItem.addActionListener(this);
	popup.add(saveBGItem);

	//Add listener to components that can bring up popup menus.
	//MouseListener popupListener = new PopupListener();
	//output.addMouseListener(popupListener);
	//menuBar.addMouseListener(popupListener);
    }

    /** 
     * Paints the component (to the screen).
     */
    public void paintComponent(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	super.paintComponent(g2d);

	if (parent==null || parent.presented  == null) {
	    return;
	} 
	
	Rectangle bounds = this.getBounds();
	DDGUI.debugln("paintCompo: panel' bounds(x="+bounds.x+",y="+bounds.y+
		      "; w=" + bounds.width +", h=" + bounds.height+")");

	parent.presented.paintFrontier(g2d, new Dimension( bounds.width,bounds.height),
				       true);

    }

    /** Paints the content of this element (i.e, the frontier curve) to a 
     Graphics2d that comes from the SVG rendering process. The actions are
     similar to paintComponent(), except that here we need to explicitly decide
     how big our SVG canvas ought to be.
     */
    public void paintSVG( SVGGraphics2D g2d) 	{
	Dimension dim =  new Dimension( 800, 800);  // (w h)
	g2d.setSVGCanvasSize(dim);
	parent.presented.paintFrontier(g2d, dim, false);
    }

    /**
     * Action listener for text fields and buttons.
     */
 

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
	//DDGUI.debugln("mouse entered at " + e.getX() + " " + e.getY());

    }

    public void mouseExited(MouseEvent e) {
    }

    int selectedI;
    PolicySignature selectedPolicy;

    
    public void mousePressed(MouseEvent e) {
	if (parent==null || parent.presented==null) return; //oops

	//DDGUI.debugln("mouse pressed at " + e.getX() + " " + e.getY());
	// are we near a circle that labels a policy?
	PresentedData.PolicyLink found = null;
	for(int i = -2*parent.presented.radius; i<=0 && found==null; i++) {
	    for(int j = -2*parent.presented.radius; j<=0 && found ==null; j++) {
		Point pt = new Point(e.getX() + i, e.getY() + j);
		found = parent.presented.pos2pol.get(pt);
	    }
	}

	if (found == null) return; // no policy on the screen here

	if (!(parent.presented  instanceof PresentedFrontier)) {
	    // FIXME
	    throw new AssertionError("Not supported");
	}


	int i = selectedI = found.pol;
	PolicySignature pol = selectedPolicy = 	found.f.getPolicy(i);
	NumberFormat fmt = new DecimalFormat("0.0#####");

	DDGUI.debugln("Mouse pressed at " + e.getX() + " " + e.getY() +
		      " Policy["+i+"]" + pol.toTreeString(300, false) );

	String s = "Policy";
	if (found.f.getPi() != 0) s += "(pi="+ found.f.getPi() + ")"; 
	s += "["+i+"], C=" + fmt.format(pol.getPolicyCost());
	//if (found.f.pi != 0) 
s += " C(pi="+ found.f.getPi() + ")=" + fmt.format(pol.getPolicyCost(found.f.getPi())); 
	s +=  " D=" + fmt.format(pol.getDetectionRate());
	popupLabel[0].setText(s);

	PolicySignature prevPol =  	found.f.getPolicy(i-1);
	double dDet = pol.getDetectionRate() - prevPol.getDetectionRate();
	double dCost = pol.getPolicyCost() - prevPol.getPolicyCost();
	double r = dDet / dCost;
	popupLabel[1].setText("dD/dC=" + fmt.format(r));

	final int L = 100;
	s = pol.toTreeString(L*2, Options.fold);
	s = s.replaceAll("I ", "I").replaceAll("R ", "R");
	// if (s.length() > L) s = s.substring(0,L-3) +  "...";
	popupLabel[2].setText(s);
	popup.show(e.getComponent(),  e.getX(), e.getY());

    }
    
    public void mouseReleased(MouseEvent e) {
    }
    
    public void mouseDragged(MouseEvent e) {
    }
    
    public void mouseMoved(MouseEvent e) {
	//DDGUI.debugln("mouse moved at " + e.getX() + " " + e.getY());
    }

    /** implementing ActionListener... */
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == drawTreeMenuItem) {
	    DDGUI.debugln("Trying to draw the tree,,,");
	    new TreeFrame(this, selectedI, selectedPolicy);
	} else	if (e.getSource() == saveBGItem) {
	    DDGUI.debugln("Describing policy as a device");
	    saveAsADevice( this, selectedPolicy);
	}
    }

    public void saveAsADevice( Component compo, PolicySignature _pol ) {

	if (! (_pol instanceof Policy)) {
	    String msg=
		"Detailed policy information is not available, because we only store (C,D) and not the policy tree. Please uncheck the \"Save (C,D) only\" box in the Options menu, and re-run";
	    System.out.println(msg);
	    JOptionPane.showMessageDialog(compo,msg);
	    return;
	}

	JFileChooser fileChooser = new JFileChooser(parent.filedir);
	fileChooser.setDialogTitle("Specify an existing or new text file to (over)write");

	int returnVal = fileChooser.showOpenDialog(compo);
	String filepath = "";
	
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    File file = fileChooser.getSelectedFile();
	    filepath = file.getPath();
	    parent.filedir = file.getParentFile();

	    parent.setLabel("Saving policy info as a device to file "+ filepath);
	    Policy pol = (Policy) _pol;

	    try {
		
		PrintWriter w = new PrintWriter(file);
		pol.saveAsADevice(w);
		w.close();
		parent.setLabel("Policy info has been written to file "+ filepath);

	    } catch (Exception e) {
		parent.setLabel("Failed to save policy info to file " + filepath);
		System.out.println( e.getMessage());
		e.printStackTrace(System.err);
	    }
	    
        }
    }



}