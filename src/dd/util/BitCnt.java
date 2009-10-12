package dd.util;

public class BitCnt {

  /** Counting set bits in an integer - that is, the size of a set that it
	represents */
    static private int bitCnt(int x, int n) {
	int sum = 0;
	int z=x;
	for(int i=0; i<n; i++) {
	    sum += (z & 1);
	    z = (z>>1);
	}
	return sum;
    }

    static public void main(String argv[]) {
	for(String a: argv) {
	    System.out.println(a + " : " + bitCnt(Integer.parseInt(a),32) + " bits");
	}
    }

}