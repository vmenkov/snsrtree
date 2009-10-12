package dd.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

import javax.swing.*;
import javax.swing.event.*;
//import javax.swing.filechooser.*;
import java.io.*;
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


/** A window to display the decision tree of a policy
 */
public class TreeFrame extends MyJFrame implements ActionListener {

    TreePanel treePanel = null;
 
    //JButton closeButton = null;

   // GUI components - menu items
    //private JMenuItem saveItem;
    private JMenuItem writeImgItem;
    private JMenuItem closeItem;
    private JMenuItem saveBGItem;
 

    PolicySignature policy;
    int policyI;

    static private NumberFormat fmt = new DecimalFormat("0.0#####");

    final DDPanel parent;

    public TreeFrame(DDPanel _parent, int _i, PolicySignature _p) {
	super("Decision tree for policy ["+_i+"]. C=" +
	      fmt.format(_p.getPolicyCost()) +  " D=" +
	      fmt.format(_p.getDetectionRate()));
	parent = _parent;
	policy  = _p;
	policyI = _i;

	int width = 600;
	int height = 600;
	setSize(width, height);
	setLocation(300, 200);

	//-- menu
	JMenuBar menuBar = new JMenuBar();
	setJMenuBar(menuBar);
	MenuHandler menuHandler = new MenuHandler();
	
	// file menu
	JMenu fileMenu = new JMenu("File");
	menuBar.add(fileMenu);

	writeImgItem = new JMenuItem("Save Tree to image file");//, 'S' );
	writeImgItem.addActionListener(menuHandler);
	fileMenu.add(writeImgItem);

	saveBGItem = new JMenuItem("Describe policy as a device");
	saveBGItem.addActionListener(menuHandler);
	fileMenu.add(saveBGItem);
	
	fileMenu.addSeparator(); // separate line
	
	closeItem = new JMenuItem("Close window"); //, 'x' );
	closeItem.addActionListener(menuHandler);
	fileMenu.add(closeItem);

	//-- main panel
	treePanel = new TreePanel();

	Container c = getContentPane();
	c.setLayout(new BorderLayout());

	//String s = " Policy["+_i+"], C=" + fmt.format(policy.getPolicyCost()) + 
	//    " D=" + fmt.format(policy.getDetectionRate());
	//c.add(new JLabel(s),  BorderLayout.NORTH);

	String s = policy.toTreeString(0, Options.fold);
	if (!Options.fold) s = s.replaceAll("I ", "I").replaceAll("R ", "R");
	//final int L = 100;
	//if (s.length() > L) s = s.substring(0,L-3) +  "...";
	//c.add(new JLabel(s),  BorderLayout.NORTH);

	int nrows = 2 + s.length()/100;
	if (nrows > 6) nrows = 6;
	JTextArea ta = new JTextArea(s, nrows, 0);
	ta.setLineWrap(true);
	ta.setEditable(false);
	c.add(new JScrollPane(ta),  BorderLayout.NORTH);

	//closeButton = new JButton("Close");
	//closeButton.addActionListener(this);
	//c.add(closeButton, BorderLayout.SOUTH);

	c.add(new JScrollPane(treePanel, 
			      ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));
	setVisible(true);
    }


    /** FIXME: would be nice to add a GUI label */
    void setLabel(String msg) {
	System.out.println(msg); 
    }

    /**
     * Action listener for text fields and buttons.
     */
    public void actionPerformed(ActionEvent e) {
	/*
	if (e.getSource() == closeButton) {
	    setVisible(false);
	    dispose();
	}
	*/
    }


    /** Class MenuHandler: handling all menu events. */
    private class MenuHandler implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    // file menu
	    //System.out.println("...TF action");
	    if (e.getSource() == closeItem) {
		setVisible(false);
		dispose();
	    }else if (e.getSource() == writeImgItem) {
		SVGWriter.writeImage(TreeFrame.this, treePanel);
	    } else  if (e.getSource() == saveBGItem) {
		//System.out.println("...Describing policy as a device");
		DDGUI.debugln("Describing policy as a device");
		parent.saveAsADevice(TreeFrame.this, policy);
	    }
	}
    }




    static int radius = 10, sq = 8;
    static int dh = 45, dw = 30;

    class TreePanel extends JPanel implements SVGAwareComponent {
 
	TreePanel() {
	}

	String mkTitle() {
	    return  "The tree of policy["+policyI+"], C=" +
		fmt.format(policy.getPolicyCost()) + 
		" D=" + fmt.format(policy.getDetectionRate());
	}

	int textHt;

	/** Paints the content of this element (i.e, the policy tree) to a
	    Graphics2d that comes from the SVG rendering process. The actions
	    are similar to paintComponent(), except that here we need to
	    explicitly decide how big our SVG canvas ought to be.
	*/
	public void paintSVG( SVGGraphics2D g2d) 	{
	    paintPolicy(g2d, policy, mkTitle(), false);
	}

	public void paintComponent(Graphics g) {
	    Graphics2D g2d = (Graphics2D) g;
	    super.paintComponent(g2d);
	    paintPolicy(g2d,  policy, mkTitle(), true);
	}
	

	void paintPolicy(Graphics2D g2d, PolicySignature policy,
				String title, boolean fromGUI) {
	
	    Policy.Sizes sizes = policy.getSizes();

	    FontMetrics fm = g2d.getFontMetrics();
	    textHt  = fm.getMaxAscent();

	    int margin = 20;
	    int marginTop = margin;// + radius + 2 * (textHt + 4);
	    int rooty = marginTop;
	    int rootx = margin + sizes.width * dw /2;

	    Dimension dim= new Dimension( sizes.width*dw + 2*margin,
					  sizes.height*dh + 2*margin);

	    if (fromGUI) {
		this.setPreferredSize(dim);
	    } else {
		(( SVGGraphics2D)g2d).setSVGCanvasSize(dim);
		
	    }

	    plotSubtree(g2d,  policy, sizes,  rootx, rooty, fromGUI);
	}	    


	void plotSubtree(Graphics2D g2d, PolicySignature policy, Policy.Sizes sizes, int rootx, int rooty, boolean fromGUI) {


	    if (policy.isTrivial()) {
		g2d.setPaint( policy.isInspect()? Color.red : Color.green);
		g2d.fillRect( rootx-sq, rooty-sq, 2*sq, 2*sq);
		
		g2d.setPaint( Color.white);
		g2d.drawString( policy.getRootName(), 
			       (int)(rootx-0.5*sq),
			       (int)(rooty + 0.5*textHt));
	    } else {
		g2d.setPaint(Color.black);
		g2d.drawOval( rootx-radius, rooty-radius, 2*radius, 2*radius);
		g2d.drawString(policy.getRootName(),
			       (int)(rootx-0.5*radius),
			       (int)(rooty + 0.5*textHt));

		// children
		if (policy instanceof Policy) {
		    PolicySignature[] outputs = ((Policy)policy).getOutputs();
		    int offset = 0;
		    int cy = rooty + dh;
		    int k =0;
		    for(int i=0; i<sizes.children.length; i++) {
			int cx = rootx + 
			    (2*offset- sizes.width + sizes.children[i].width)*dw/2;
			
			g2d.setPaint(Color.black);
			
			int x1 =  (cx < rootx ? rootx-radius :
				   cx > rootx ? rootx+radius : rootx),
			    y1 =  (cx == rootx ? rooty+radius: rooty),
			    y2 =  cy- (outputs[k].isTrivial()? sq : radius);
				     
			
			g2d.drawLine( x1, y1, cx, y2);

			if ( sizes.children[i].count > 1) {
			    g2d.drawString("" + sizes.children[i].count,
					   cx+2, y2-2);
			}
			
			plotSubtree( g2d, outputs[k], sizes.children[i], cx,cy, 
				     fromGUI);
			offset += sizes.children[i].width + 1;
			k += sizes.children[i].count;
		    }
		}		    
	    }
	    
	}


    }


 
}