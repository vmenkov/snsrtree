package dd.engine;

/** An instance of a Channel represents a particular output channel of a
 * Device. (A device is a single test, or a tree of tests). 
 */

public class Channel {
    /** Percentages of good and bad cases entering the device that end up 
	exiting via this channel; the cost of all tests required to get to the channel */
    private double g, b, c;
    /** Percentages of good and bad cases entering the device that end up 
	exiting thru this channel or lower-numbered channels. 
    */
    private double sumGC, sumDet;
    //public double getG() { return g; }    
    //public double getB() { return b; }

    /** If this is a channel of a simple Test, then underlying==null;
     * otheriwse, underlying is a pointer to the output channel of the
     * underlying device which corresponds to this output channel of the
     * compound device */

    Channel underlying;
}