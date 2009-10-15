package dd.web;

import java.io.*;
import java.util.*;
import java.text.*;

import dd.engine.*;
import dd.gui.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.disk.*;

//------------------------------------------------

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

import javax.swing.*;
import javax.swing.event.*;

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


/** This servlet produces PNG or JPEG images for the DNDO Demo application
 */
public class GraphServlet extends HttpServlet {
    public void	doGet(HttpServletRequest request,HttpServletResponse response) {
	DemoSessionData r = DemoSessionData.getDemoSessionData(request);


	try {
	    //  r.presented must have been set in main.jsp
	    if (r.presented != null) {
		
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
		Dimension dim =  new Dimension( 800, 800);  // (w h)
		svgGenerator.setSVGCanvasSize(dim);
		
		r.presented.paintFrontier(svgGenerator, dim, false);
		
		// Writing data in SVG format to an in-memory String
		// (for later conversion)
		boolean useCSS=true; // we want to use CSS style attributes
		
		StringWriter sw = new StringWriter();
		svgGenerator.stream(sw, useCSS);
		sw.close();
		
		final boolean isJPG = false;
		
		// Try JPEG or PNG writing...
		// Create a Transcoder and set its quality hint.
		ImageTranscoder t= isJPG? new JPEGTranscoder(): new PNGTranscoder();
		if (isJPG) t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,new Float(.8));
		
		response.setStatus(HttpServletResponse.SC_OK );
		response.setContentType(isJPG? "image/jpeg" : "image/png");
		
		// Set the transcoder input and output.
		StringReader sr = new StringReader(sw.toString());
		TranscoderInput input = new TranscoderInput(sr);
		OutputStream ostream = response.getOutputStream();
		TranscoderOutput output = new TranscoderOutput(ostream);
		
		// Perform the transcoding.
		t.transcode(input, output);
		ostream.flush();
		ostream.close();
	    }
	} catch (Exception e) {
	    try {
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
	    } catch(IOException ex) {};
	    return;
	}
    }
}
