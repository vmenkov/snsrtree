package dd.gui;

import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.geom.AffineTransform;

import dd.engine.*;

/** It is public because it's used by the web GUI, too */
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

