/**
//  Copyright (C) 2001  Georgia Institute of Technology
    
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
    
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
    
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
    
//  Copyright by: Peter Dillinger & Pete Manolios who can be reached as follows.
    
//  Email: peterd@cc.gatech.edu
//         manolios@cc.gatech.edu

//  Postal Mail:
//  College of Computing
//  CERCS Lab
//  Georgia Institute of Technology
//  801 Atlantic Drive
//  Atlanta, Georgia 30332-0280 U.S.A.

*/

/**
 * A fast, accurate, and compact Bloom filter.
 *
 * Please use the factory method BloomFilter.create() to get BloomFilter
 * instances.  The factory method will choose the appropriate implementation
 * for the information given.  (Right now, all that matters is whether the
 * size is a power of two or not, with the power of two implementation being
 * faster.)
 *
 * Note that there are various add and query operations for getting information
 * to the Bloom filter.  The hash functions are different for each data format,
 * so even if one datum is passed in in different formats, the Bloom filter
 * treats it as different data.  Consequently, query() with the same format
 * as you add()ed the data with.
 */

package metdemo.Tools;

public abstract class BloomFilter implements java.io.Serializable {
    /** Add a datum of up to 32 bits to the Bloom filter. */
    public final void add(int val) { addImpl(val); added++; }
    protected abstract void addImpl(int val);

    /** Add a datum of up to 32 bits to the Bloom filter. */
    public final void add(long val) { addImpl(val); added++; }
    protected abstract void addImpl(long val);

    /** Add a datum in the form of an int[] to the Bloom filter. */
    public void add(int[] val) { fingerprint(val); addfp(); added++; }

    /** Add a datum in the form of a char[] to the Bloom filter. */
    public void add(char[] val) { fingerprint(val); addfp(); added++; }

    /** Add a datum in the form of a byte[] to the Bloom filter. */
    public void add(byte[] val) { fingerprint(val); addfp(); added++; }

    /** Add a datum in the form of a String to the Bloom filter. */
    public void add(String val) { fingerprint(val); addfp(); added++; }

    /** Queries a datum of up to 32 bits against the Bloom filter. */
    public abstract boolean query(int val);

    /** Queries a datum of up to 64 bits against the Bloom filter. */
    public abstract boolean query(long val);

    /** Queries a datum in the form of a String against the Bloom filter. */
    public boolean query(int[] val) { fingerprint(val); return queryfp(); }
    /** Queries a datum in the form of a String against the Bloom filter. */
    public boolean query(char[] val) { fingerprint(val); return queryfp(); }
    /** Queries a datum in the form of a String against the Bloom filter. */
    public boolean query(byte[] val) { fingerprint(val); return queryfp(); }
    /** Queries a datum in the form of a String against the Bloom filter. */
    public boolean query(String val) { fingerprint(val); return queryfp(); }

    /** Returns the theoretical false positive rate. */
    public double fpRate() {
	double p = Math.pow(Math.E, -(double)hfns * (double)added / (double)size);
	return Math.pow(1-p,(double)hfns);
    }

    /** Returns the number of elements added to the Bloom filter. */
    public int added() {
    	return added;
    }

    /**
     * Creates a new Bloom filter seeding the hash functions with the current
     * time.
     */
    public static BloomFilter create(int size, int hfns) {
	return create(size,hfns,System.currentTimeMillis());
    }

    /**
     * Creates a new Bloom filter with the specified size, number of index
     * functions, and hash function seed.
     */
    public static BloomFilter create(int size, int hfns, long hash_init) {
	if (size < 64 || hfns < 1 || hfns > 32)
	    throw new IllegalArgumentException();
	int pow = pow2(size);
	if (pow < 0) { // not a power of two
	    /*while (!isprime(size)) size++;*/
	    return new BloomFilterNonPow2Impl(size, (byte) hfns, hash_init);
	} else { // power of two size
	    return new BloomFilterPow2Impl(size, (byte) hfns, hash_init);
	}
    }

    static int pow2(int x) {
	int ret = 0;
	while (((x & 1) == 0) && x > 1) {
	    ret++;
	    x >>= 1;
	}
	if (x > 1) {
	    return -1;
	} else {
	    return ret;
	}
    }

    /** Add the stored 96-bit fingerprint to the Bloom filter. */
    protected abstract void addfp();

    /** Queries the stored 96-bit fingerprint against the Bloom filter. */
    protected abstract boolean queryfp();

    protected static final int[] baseEntropy = {
	0x20bbc6cc, 0x73725091, 0x090cd062, 0x60d18892,
	0xf3086c68, 0x40e09102, 0x5642be24, 0x4a12477a,
	0x5b6e50fb, 0x79763d7d, 0xfccd4ee6, 0x4ba8f095,
	0x19e3df70, 0xeb8fb96a, 0xe076f3fe, 0xddc3cce1,
	0x68048071, 0x05b5a566, 0x3fcb8f82, 0x2e835c35,
	0xa92520ab, 0x40fd2405, 0x3229d846, 0x9f85135d,
	0x1730dc09, 0x900ad7fa, 0x3f54257c, 0x1897dcbb,
	0xf0c50266, 0x38f0991e, 0x21ea380c, 0x469246b1,
	0x5221b158, 0x87b2f0f2, 0x42d830b9, 0xd184ce5e};
    
    protected final int[] entropy;
    protected final int size;
    protected final byte hfns;
    protected final long hash;
    protected final int[] vect;
    protected int added;

    /** 
     * Constructor should only be used by subclasses.  To create a Bloom
     * filter, use the factory method, BloomFilter.create().
     */
    protected BloomFilter(int size, byte hfns, long hash_init) {
	this.size = (size + 31) & 0x7fffe0;
	this.hash = hash_init;
	this.hfns = hfns;
	vect = new int[this.size >> 5];
	entropy = new int[baseEntropy.length];
	for (int i = 0; i < entropy.length; i++) {
	    entropy[i] = (baseEntropy[i] ^ (int)(hash_init >> (i & 31))) | 1;
	}
	added = 0;
    }

    protected transient int fp1;
    protected transient int fp2;
    protected transient int fp3;

    /** Jenkins LOOKUP2-based fingerprinting. */
    protected void fingerprint(int[] val) {
	int a = (int) hash;
	int b = (int) (hash >> 32);
	int c = 0x9e3779b9;
	int i;
	for (i = 0; i < val.length - 2; i += 3) {
	    a += val[i];
	    b += val[i+1];
	    c += val[i+2];
	    a -= b; a -= c; a ^= (c>>>13);
	    b -= c; b -= a; b ^= (a<<8);
	    c -= a; c -= b; c ^= (b>>>13);
	    a -= b; a -= c; a ^= (c>>>12);
	    b -= c; b -= a; b ^= (a<<16);
	    c -= a; c -= b; c ^= (b>>>5);
	    a -= b; a -= c; a ^= (c>>>3);
	    b -= c; b -= a; b ^= (a<<10);
	    c -= a; c -= b; c ^= (b>>>15);
	}
	switch (val.length - i) {
	case 2: c += val[val.length - 2]; 
	case 1: b += val[val.length - 1]; 
	}
	a += val.length;
	a -= b; a -= c; a ^= (c>>>13);
	b -= c; b -= a; b ^= (a<<8);
	c -= a; c -= b; c ^= (b>>>13);
	a -= b; a -= c; a ^= (c>>>12);
	b -= c; b -= a; b ^= (a<<16);
	c -= a; c -= b; c ^= (b>>>5);
	a -= b; a -= c; a ^= (c>>>3);
	b -= c; b -= a; b ^= (a<<10);
	c -= a; c -= b; c ^= (b>>>15);

	fp1 = a;
	fp2 = b;
	fp3 = c;
    }

    /** Jenkins LOOKUP2-based fingerprinting. */
    protected void fingerprint(char[] val) {
	int a = (int) hash;
	int b = (int) (hash >> 32);
	int c = 0xdeadbeef;
	int i;
	for (i = 0; i < val.length - 5; i += 6) {
	    a += ((int)val[i] << 16) + (int)val[i+1];
	    b += ((int)val[i+2] << 16) + (int)val[i+3];
	    c += ((int)val[i+4] << 16) + (int)val[i+5];
	    a -= b; a -= c; a ^= (c>>>13);
	    b -= c; b -= a; b ^= (a<<8);
	    c -= a; c -= b; c ^= (b>>>13);
	    a -= b; a -= c; a ^= (c>>>12);
	    b -= c; b -= a; b ^= (a<<16);
	    c -= a; c -= b; c ^= (b>>>5);
	    a -= b; a -= c; a ^= (c>>>3);
	    b -= c; b -= a; b ^= (a<<10);
	    c -= a; c -= b; c ^= (b>>>15);
	}
	switch (val.length - i) {
	case 5: a += (int)val[val.length - 5] << 16; 
	case 4: b += (int)val[val.length - 4]; 
	case 3: b += (int)val[val.length - 3] << 16; 
	case 2: c += (int)val[val.length - 2]; 
	case 1: c += (int)val[val.length - 1] << 16; 
	}
	a += val.length;
	a -= b; a -= c; a ^= (c>>>13);
	b -= c; b -= a; b ^= (a<<8);
	c -= a; c -= b; c ^= (b>>>13);
	a -= b; a -= c; a ^= (c>>>12);
	b -= c; b -= a; b ^= (a<<16);
	c -= a; c -= b; c ^= (b>>>5);
	a -= b; a -= c; a ^= (c>>>3);
	b -= c; b -= a; b ^= (a<<10);
	c -= a; c -= b; c ^= (b>>>15);

	fp1 = a;
	fp2 = b;
	fp3 = c;
    }

    /** Jenkins LOOKUP2-based fingerprinting. */
    protected void fingerprint(byte[] val) {
	int a = (int) hash;
	int b = (int) (hash >> 32);
	int c = 0xcafebabe;
	int i;
	for (i = 0; i < val.length - 11; i += 12) {
	    a += ((int)val[i+0]<<24)+((int)val[i+1]<<16)+((int)val[i+2]<<8)+(int)val[i+3];
	    b += ((int)val[i+4]<<24)+((int)val[i+5]<<16)+((int)val[i+6]<<8)+(int)val[i+7];
	    c += ((int)val[i+8]<<24)+((int)val[i+9]<<16)+((int)val[i+10]<<8)+(int)val[i+11];
	    a -= b; a -= c; a ^= (c>>>13);
	    b -= c; b -= a; b ^= (a<<8);
	    c -= a; c -= b; c ^= (b>>>13);
	    a -= b; a -= c; a ^= (c>>>12);
	    b -= c; b -= a; b ^= (a<<16);
	    c -= a; c -= b; c ^= (b>>>5);
	    a -= b; a -= c; a ^= (c>>>3);
	    b -= c; b -= a; b ^= (a<<10);
	    c -= a; c -= b; c ^= (b>>>15);
	}
	switch (val.length - i) {
	case 11: a += val[val.length - 11] << 24; 
	case 10: b += val[val.length - 10] << 16; 
	case 9: c += val[val.length - 9] << 8; 
	case 8: b += val[val.length - 8] << 24; 
	case 7: c += val[val.length - 7] << 16; 
	case 6: a += val[val.length - 6] << 8; 
	case 5: b += val[val.length - 5]; 
	case 4: c += val[val.length - 4] << 24; 
	case 3: a += val[val.length - 3] << 16; 
	case 2: b += val[val.length - 2] << 8; 
	case 1: c += val[val.length - 1]; 
	}
	a += val.length;
	a -= b; a -= c; a ^= (c>>>13);
	b -= c; b -= a; b ^= (a<<8);
	c -= a; c -= b; c ^= (b>>>13);
	a -= b; a -= c; a ^= (c>>>12);
	b -= c; b -= a; b ^= (a<<16);
	c -= a; c -= b; c ^= (b>>>5);
	a -= b; a -= c; a ^= (c>>>3);
	b -= c; b -= a; b ^= (a<<10);
	c -= a; c -= b; c ^= (b>>>15);

	fp1 = a;
	fp2 = b;
	fp3 = c;
    }

    /** Jenkins LOOKUP2-based fingerprinting. */
    protected void fingerprint(String val) {
	int a = (int) hash;
	int b = (int) (hash >> 32);
	int c = 0xfeed1234;
	int len = val.length();
	int i;
	for (i = 0; i < len - 5; i += 6) {
	    a += ((int)val.charAt(i) << 16) + (int)val.charAt(i+1);
	    b += ((int)val.charAt(i+2) << 16) + (int)val.charAt(i+3);
	    c += ((int)val.charAt(i+4) << 16) + (int)val.charAt(i+5);
	    a -= b; a -= c; a ^= (c>>>13);
	    b -= c; b -= a; b ^= (a<<8);
	    c -= a; c -= b; c ^= (b>>>13);
	    a -= b; a -= c; a ^= (c>>>12);
	    b -= c; b -= a; b ^= (a<<16);
	    c -= a; c -= b; c ^= (b>>>5);
	    a -= b; a -= c; a ^= (c>>>3);
	    b -= c; b -= a; b ^= (a<<10);
	    c -= a; c -= b; c ^= (b>>>15);
	}
	switch (len - i) {
	case 5: a += (int)val.charAt(len - 5) << 16; 
	case 4: b += (int)val.charAt(len - 4); 
	case 3: b += (int)val.charAt(len - 3) << 16; 
	case 2: c += (int)val.charAt(len - 2); 
	case 1: c += (int)val.charAt(len - 1) << 16; 
	}
	a += len;
	a -= b; a -= c; a ^= (c>>>13);
	b -= c; b -= a; b ^= (a<<8);
	c -= a; c -= b; c ^= (b>>>13);
	a -= b; a -= c; a ^= (c>>>12);
	b -= c; b -= a; b ^= (a<<16);
	c -= a; c -= b; c ^= (b>>>5);
	a -= b; a -= c; a ^= (c>>>3);
	b -= c; b -= a; b ^= (a<<10);
	c -= a; c -= b; c ^= (b>>>15);

	fp1 = a;
	fp2 = b;
	fp3 = c;
    }

    static class BloomFilterPow2Impl extends BloomFilter {
	private int mask;
	public BloomFilterPow2Impl(int size, byte hfns, long hash_init) {
	    super(size, hfns, hash_init);
	    mask = (size-1) >> 5;
	}
	public void addImpl(int val) {
	    for (int i = 0; i < hfns; i++) {
		int x = ((val + entropy[i]) ^ entropy[i+1]) * entropy[i+2];
		vect[x & mask] |= (1 << (x >>> 27));
	    }
	}
	public void addImpl(long val) {
	    for (int i = 0; i < hfns; i++) {
		int x = (((int)(val>>32) + entropy[i]) ^ entropy[i+1]) * entropy[i+2]
		    + (((int)val + entropy[i+2]) ^ entropy[i]) * entropy[i+1];
		vect[x & mask] |= (1 << (x >>> 27));
	    }
	}
	public void addfp() {
	    int x = fp1;
	    int y = fp2 | 1;
	    int z = fp3;
	    for (int i = 0; i < hfns; i++) {
		vect[x & mask] |= (1 << (x >>> 27));
		x += y; y += z;
	    }
	}
        public boolean query(int val) {
	    for (int i = 0; i < hfns; i++) {
		int x = ((val + entropy[i]) ^ entropy[i+1]) * entropy[i+2];
		if ((vect[x & mask] & (1 << (x >>> 27))) == 0) {
		    return false;
		}
	    }
	    return true;
	}
	public boolean query(long val) {
	    for (int i = 0; i < hfns; i++) {
		int x = (((int)(val>>32) + entropy[i]) ^ entropy[i+1]) * entropy[i+2]
		    + (((int)val + entropy[i+2]) ^ entropy[i]) * entropy[i+1];
		if ((vect[x & mask] & (1 << (x >>> 27))) == 0) {
		    return false;
		}
	    }
	    return true;
	}
	public boolean queryfp() {
	    int x = fp1;
	    int y = fp2 | 1;
	    int z = fp3;
	    for (int i = 0; i < hfns; i++) {
		if ((vect[x & mask] & (1 << (x >>> 27))) == 0) {
		    return false;
		}
		x += y; y += z;
	    }
	    return true;
	}
    }

    static class BloomFilterNonPow2Impl extends BloomFilter {
	public BloomFilterNonPow2Impl(int size, byte hfns, long hash_init) {
	    super(size, hfns, hash_init);
	}
	public void addImpl(int val) {
	    for (int i = 0; i < hfns; i++) {
		int t = ((val + entropy[i]) ^ entropy[i+1]) * entropy[i+2];
		t >>>= 1; // make it positive for modulus
		vect[t % vect.length] |= (1 << (t >>> 26));
	    }
	}
	public void addImpl(long val) {
	    for (int i = 0; i < hfns; i++) {
		int t = (((int)(val>>32) + entropy[i]) ^ entropy[i+1]) * entropy[i+2]
		    + (((int)val + entropy[i+2]) ^ entropy[i]) * entropy[i+1];
		t >>>= 1; // make it positive for modulus
		vect[t % vect.length] |= (1 << (t >>> 26));
	    }
	}
	public void addfp() {
	    int x = fp1;
	    int y = fp2 | 1;
	    int z = fp3;
	    for (int i = 0; i < hfns; i++) {
		int t = x >>> 1; // make it positive for modulus
		vect[t % vect.length] |= (1 << (t >>> 26));
		x += y; y += z;
	    }
	}
        public boolean query(int val) {
	    for (int i = 0; i < hfns; i++) {
		int t = ((val + entropy[i]) ^ entropy[i+1]) * entropy[i+2];
		t >>>= 1; // make it positive for modulus
		if ((vect[t % vect.length] & (1 << (t >>> 26))) == 0) {
		    return false;
		}
	    }
	    return true;
	}
	public boolean query(long val) {
	    for (int i = 0; i < hfns; i++) {
		int t = (((int)(val>>32) + entropy[i]) ^ entropy[i+1]) * entropy[i+2]
		    + (((int)val + entropy[i+2]) ^ entropy[i]) * entropy[i+1];
		t >>>= 1; // make it positive for modulus
		if ((vect[t % vect.length] & (1 << (t >>> 26))) == 0) {
		    return false;
		}
	    }
	    return true;
	}
	public boolean queryfp() {
	    int x = fp1;
	    int y = fp2 | 1;
	    int z = fp3;
	    for (int i = 0; i < hfns; i++) {
		int t = x >>> 1; // make it positive for modulus
		if ((vect[t % vect.length] & (1 << (t >>> 26))) == 0) {
		    return false;
		}
		x += y; y += z;
	    }
	    return true;
	}
    }

  /*  public static void main(String args[]) throws Exception {
	BloomFilter bf = BloomFilter.create(Integer.parseInt(args[0]),
					    Integer.parseInt(args[1]));
	for (int i = 100; i < 1000000; i += 100) {
	    bf.add((long)i);
	}
	for (int i = 100; i < 1000000; i += 100) {
	    if (!bf.query((long)i)) throw new Exception("error in Bloom filter!");
	}
	int fp = 0;
	for (int i = 106; i < 1000006; i += 100) {
	    if (bf.query((long)i)) fp++;
	}
	System.out.println("False positives: " + fp + " / 10,000");
	System.out.println("Theoretical: " + bf.fpRate());
    }*/
}
