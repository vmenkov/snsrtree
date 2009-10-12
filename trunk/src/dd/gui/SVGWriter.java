package dd.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
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


/** Auxiliary class to write image files - SVG, JPG, PNG 
 */
public class SVGWriter extends JFrame {

    static File filedir = new File(".");

    private static final boolean useCSS=true; // we want to use CSS style attributes


    static public void writeImage(MyJFrame frame, SVGAwareComponent abs) {

	JFileChooser fileChooser = new JFileChooser(filedir);
	fileChooser.setDialogTitle("Specify an existing or new SVG, JPG, or PNG image file to (over)write");

	//-- this class is only available in JDK 1.6
	/*
	FileNameExtensionFilter filter = new FileNameExtensionFilter(
	    "SVG, JPG, PNG images", "svg", "jpg", "jpeg", "png");
	fileChooser.setFileFilter(filter);
	*/

	int returnVal = fileChooser.showOpenDialog(frame);
	String filepath = "";
	
	if (returnVal != JFileChooser.APPROVE_OPTION) {
	    return;  // user chose no file - writing cancelled
	}

	File file = fileChooser.getSelectedFile();
	filepath = file.getPath();
	filedir = file.getParentFile();

	String filepathLwr = filepath.toLowerCase();
	boolean isSVG = filepathLwr.endsWith(".svg"),
	    isJPG =filepathLwr.endsWith(".jpg")||filepathLwr.endsWith(".jpeg"),
	    isPNG = filepathLwr.endsWith(".png");

	if (!isSVG && !isPNG && !isJPG) {
	    String msg= "Invalid file name '"+filepath+"'. File extension must be one of the following: .svg .jpg .png";
	    System.out.println(msg);
	    JOptionPane.showMessageDialog(frame,msg);
	    return;
	}

	frame.setLabel("Writing image file "+ filepath);
	    
	try {

	    // Get a DOMImplementation.
	    DOMImplementation domImpl =
		GenericDOMImplementation.getDOMImplementation();
		
	    // Create an instance of org.w3c.dom.Document.
	    String svgNS = "http://www.w3.org/2000/svg";
	    Document document = domImpl.createDocument(svgNS, "svg", null);

	    // Create an instance of the SVG Generator.
	    SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
	    
	    //  Ask our DDPanel to set the canvas size and to render into the
	    //  SVG Graphics2D implementation.
	    abs.paintSVG( svgGenerator);

	    String dataAsString = null;

	    // Writing data to an  SVG file or a String (for later conversion)
	    

	    if (isSVG) {	
		//PrintWriter out = isSVG ? new PrintWriter(file, "UTF-8") :
		// Writer out = new OutputStreamWriter(System.out, "UTF-8");
		//svgGenerator.stream(out, useCSS);
		//out.close();
	
		svgGenerator.stream(filepath, useCSS);
		frame.setLabel("Successfully wrote SVG image to file "+ filepath);
	    } else if (isPNG || isJPG) {

		StringWriter sw = new StringWriter();
		svgGenerator.stream(sw, useCSS);
		sw.close();

		// Try JPEG or PNG writing...
		// Create a Transcoder and set its quality hint.
		ImageTranscoder t= isJPG? new JPEGTranscoder():
		    new PNGTranscoder();
		t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,new Float(.8));

		// Set the transcoder input and output.

		StringReader sr = new StringReader(sw.toString());

		TranscoderInput input = new TranscoderInput(sr);
		OutputStream ostream = new FileOutputStream(file);
		TranscoderOutput output = new TranscoderOutput(ostream);

		//t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH,new Float(width));
		//t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT,new Float(height));
   		
		// Perform the transcoding.
		t.transcode(input, output);
		ostream.flush();
		ostream.close();
		frame.setLabel("Successfully wrote image to file "+ filepath);
	    } else {
		System.out.println("Don't know what to write to file named '" + file + "'. Please use a file name with an .svg or .jpg extension instead.");
	    }
		
	} catch (Exception e) {
	    frame.setLabel("Failed to write image file " + filepath);
	    System.out.println( e.getMessage());
	    e.printStackTrace(System.err);
	}
	    
    }

    /** Stream out SVG to the specified stream, in UTF-8 encoding.

	usage:
	<pre>
	PipedWriter pw = new PipedWriter();
	PipedReader sr = new PipedReader(pw);
	SvgGeneratorThread st = new SvgGeneratorThread(frame,svgGenerator,pw);
	st.start();
	</pre>

     */
    /*
    static class SvgGeneratorThread extends Thread {
	MyJFrame frame;
	SVGGraphics2D svgGenerator;
	PipedWriter pw;
	SvgGeneratorThread( MyJFrame _frame, SVGGraphics2D _svgGenerator, PipedWriter _pw) {
	    frame = _frame;
	    svgGenerator = _svgGenerator;
	    pw = _pw;
	}
	public void run()  {
	    try {
		svgGenerator.stream(pw, useCSS);
		pw.close();
	    }  catch (Exception e) {
		frame.setLabel("Failed to stream SVG data to pipe");
		System.out.println( e.getMessage());
		e.printStackTrace(System.err);
	    }
	}
    }
    */
}