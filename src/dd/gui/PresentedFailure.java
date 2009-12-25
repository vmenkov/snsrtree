package dd.gui;

import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.geom.AffineTransform;

import dd.engine.*;

/** An instance of this subclass is produced instead of more useful
    types of {@link PresentedData} when, due to some error, there is
    nothing to display. This is somewhat useful when, e.g., we have a
    servlet which is supposed to return a PNG image - but has nothing
    useful to return other than an error message.

    <p> It is public because it's used by the web GUI, too */
public class PresentedFailure extends PresentedData {

    String msg[]={"eh?"};

    public PresentedFailure(String[] _msg) {
	super(new Test[0]);
	msg = _msg;
    }
    public PresentedFailure(String m) {
	super(new Test[0]);
	msg = new String[] {m};
    }

    /** Just "paints" an error message 
     */
    public void paintFrontier(Graphics2D g2d, Dimension bounds, boolean fromGUI) {
	FontMetrics fm = g2d.getFontMetrics();
	int textHt  = fm.getMaxAscent();

	int margin = 2 * textHt + 8;

	int y=4;
	for(String m: msg) {
	    y += 2*textHt;
	    g2d.drawString(m, 2, y);
	//		       (int)p1.getX(), (int)p1.getY()+2*textHt+4);
	}
    }


}

