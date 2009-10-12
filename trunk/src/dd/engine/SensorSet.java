package dd.engine;

import java.util.*;

public class SensorSet {
    static int maxCnt[];
    /** Array representation */
    int cnt[];

    static void init( int _maxCnt[]) {
	maxCnt = _maxCnt;

	StringBuffer x = new StringBuffer("maxCnt={");
	for(int i=maxCnt.length-1; i>=0; i--) {
	    x.append(" " + maxCnt[i]);
	}	
	x.append("}");
	System.out.println(x.toString());

    }

    SensorSet(SensorSet x) {
	cnt = Arrays.copyOf(x.cnt, x.cnt.length);
    }

    /** How many distinct policies can be formed with so many tests of
     * each kind?
     */
    static int getMaxPolicyCnt() throws DDException {
	int pow = 1;
	for(int i=0; i<maxCnt.length; i++) {
	    if ((maxCnt[i]+1) >=  Integer.MAX_VALUE / pow) {
		throw new DDException("Too many sesnsors to combine. It appears we may end up with close to, or over, " + Integer.MAX_VALUE + " distinct policies");
	    }
	    pow *= (maxCnt[i]+1);
	}
	return pow;
    }

    public static int maxSetSize(Test[] tests) {
	int sum = 0;
	for(Test t: tests) sum += t.nCopies;
	return sum;
    }

    static int maxSetSize() {
	int sum = 0;
	for(int c: maxCnt) sum += c;
	return sum;
    }

    int getSize() {
	int sum=0;
	for(int c: cnt) sum+= c;
	return sum;
    }

    SensorSet() {
	cnt = new int[maxCnt.length];
    }

    SensorSet(int ptr) {
	cnt = new int[maxCnt.length];
	for(int i=0; ptr>0 && i<maxCnt.length; i++) {
	    cnt[i] = (ptr % (maxCnt[i]+1));
	    ptr /= (maxCnt[i]+1);
	}	
	if (ptr>0) throw new IllegalArgumentException("Cannot convert " + ptr+ " to a sensor set, as the value is out of range");
    }

    /** A set with consisting of only one (k-th) sensor */
    static SensorSet oneSensorSet(int k) {
	SensorSet x = new SensorSet();
	x.cnt[k] = 1;
	return x;
    }


    /** Converts to integer representation */
    int intValue() {
	int pow = 1;
	int sum = 0;
	for(int i=0; i<maxCnt.length; i++) {
	    sum += cnt[i] * pow;
	    pow *= (maxCnt[i]+1);	    
	}	
	return sum;
    }

    /** Printable representation, with the 0th test on the right 
     */
    public String toString() {
	StringBuffer x = new StringBuffer("{");
	for(int i=maxCnt.length-1; i>=0; i--) {
	    x.append(" " + cnt[i]);
	}	
	x.append("}");
	return x.toString();
    }

    static SensorSet firstSetOfSize(int n) {
	SensorSet x = new SensorSet();
	for(int i=0; i<maxCnt.length && n>0; i++) {
	    x.cnt[i] = Math.min(maxCnt[i] , n);
	    n -= x.cnt[i];
	}
	return x;
    }

    /** Modifies this set to be the next set in the lexicographic
      sequence; returns increment/decrement in set size.

      @return Integer.MAX_VALUE,  if this is the last set already

     */
    private int toNext() {
	int diff = 0;
	for(int i=0; i<maxCnt.length; i++) {
	    if (cnt[i] < maxCnt[i]) {
		cnt[i]++;
		diff ++;
		return diff;
	    } else {
		diff -= cnt[i];
		cnt[i] = 0;
	    }
	}
	// there was no "next" ...
	return Integer.MAX_VALUE;
    }

    /* Modifies this set to be the next set of the same size
       @return True on success, or false if this is the last set
     */
    boolean transformToNextSetOfSameSize() {
	int sizeDiff = 0;
	do {
	    int d = toNext();
	    if (d == Integer.MAX_VALUE) return false;
	    sizeDiff += d;
	} while(sizeDiff != 0);
	return true;
    }

    /** Returns a new sensor with out the j-th sensor (or with one
      j-th sensor fewer)
      @return A new sensor, or null (if the j-th sensor can't be
      removed, becuase it's not there in this set)
     */
    SensorSet minusJ(int j) {
	if (cnt[j] == 0) return null;
	SensorSet x = new SensorSet(this);
	x.cnt[j]--;
	return x;
    }
    

}