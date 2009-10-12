package dd.gui;

//--------------- for SVG output
import org.apache.batik.svggen.SVGGraphics2D;

interface SVGAwareComponent {
    
   /** Paints the content of this Swing component (e.g. the frontier curve or
     a tree) to a Graphics2d that comes from the SVG rendering process. The
     actions are similar to paintComponent(), except that here we need to
     explicitly decide how big our SVG canvas ought to be.
     */
    void paintSVG( SVGGraphics2D g2d);
}
