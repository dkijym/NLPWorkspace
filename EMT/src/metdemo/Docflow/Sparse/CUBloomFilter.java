package metdemo.Docflow.Sparse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/*
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
*/

/**
 * Bloom filter for Worminator project.  Adapted from code by
 * Shlomo Hershkop (sh553@cs.columbia.edu) and 
 * Spencer Whitman (joecat28@cs.columbia.edu).  Uses a SHA-1 hash function.
 * 
 * <!--
 * TODO: Support multiple hash functions?
 * TODO: Build XML parser-wrapper (sourceIP list / sourceIP+destport list)
 * TODO: Robust bit count checking
 * -->
 * 
 * <p>Copyright (c) 2005 The Trustees of Columbia University in the
 * City of New York.  All Rights Reserved.</p>
 * 
 * @author Janak J Parekh (janak@cs.columbia.edu)
 * @version $Revision: 1.1 $
 **/
public class CUBloomFilter implements Serializable {
  private static final long serialVersionUID = 3257283617406334521L;
  /** Logger */
  //private static Logger log = Logger.getLogger(CUBloomFilter.class.getName());
  /** Hash method */
  public static final String HASH_METHOD = "SHA-1";
  /** Maximum number of bits in our hash */
  public static final int HASH_SIZE = 160;
  /** Maximum size of our index.  31 for now to avoid sign issues with ints. */
  public static final int MAX_BITS_INDEX = 31;
  /** Name of this bloom filter (descriptive) */
  private String name = null;
  /** Bit hash, contains actual content. */
  private BitSet hash = null;
  /** Size of index into Bloom filter.  bfHash is therefore 2^(#index bits). */
  private int indexSize = -1;
  /** 
   * Number of "phases" to be used, i.e., the number of times we index a given
   * datum, taking bfHash bits at a time, into a Bloom filter.
   */
  private int numPhases = -1;
  /**
   * Our hasher.
   */
  private transient MessageDigest hasher = null;
  /** Opaque metadata to be used by the owner as they see fit. */
  private Serializable metadata = null;


  /**
   * Default constructor.  By default, we create a 1MB (20-bit index size)
   * Bloom filter, and ask the algorithm to create as many phases as it can.
   */
  public CUBloomFilter() throws InstantiationException {
    this("BloomFilter", 20, null);
  }

  /**
   * Create a BloomFilter with a specific name, and use a specific number of
   * bits for the index to the bit array.  Ask the algorithm to create as
   * many phases as it can.
   * 
   * @param name Name of the bloom filter
   * @param indexSize Number of bits for the index to the bit array; cannot
   * exceed the total number of bits of our hash.
   * @param metadata Some opaque metadata for the creator's purpose.
   */
  public CUBloomFilter(String name, int indexSize, Serializable metadata)
    throws InstantiationException {
    this(name, indexSize, (int) (HASH_SIZE / indexSize), metadata);
  }

  /**
   * Full constructor.
   * 
   * @param name Name of the bloom filter
   * @param indexSize Number of bits for the index to the bit array; cannot
   * exceed the total number of bits of our hash.
   * @param numPhases Number of times to insert entries in the array indexed by
   * (presumably different) parts of the hash.
   * @param metadata Some opaque metadata for the creator's purpose.
   */
  public CUBloomFilter(String name, int indexSize, int numPhases,
  Serializable metadata)
    throws InstantiationException {
    this.name = name;
    this.metadata = metadata;
    if (indexSize > MAX_BITS_INDEX)
      throw new InstantiationException("Index size too large");
    else
      this.indexSize = indexSize;

    // Create our bit array
    hash = new BitSet(1 << indexSize); // e.g., 2^indexSize
    this.numPhases = numPhases;

    // Finally, create the hasher.  Use the built-in Java MessageDigest class.
    try {
      hasher = MessageDigest.getInstance(HASH_METHOD);
    } catch (Exception e) {
      throw new InstantiationException(e.toString());
    }
  }

  /**
   * Return the number of bits used to hold the Bloom filter.
   * 
   * @return The number of bits in the BitSet.
   */
  public int getSize() {
    return hash.size();
  }

  /**
   * Return the number of bits ACTUALLY USED in the Bloom filter.
   *
   * @return The number of bits used.
   */
  public int getUsedSize() {
    return hash.cardinality();
  }
  
  /**
   * Compute the false positive rate -- this is computed by exponentiating
   * the "saturation" of the BitSet by the phase.
   * 
   * @return The false positive rate, between 0 and 1.
   */
  public double falsePositiveRate() {
    return Math.pow((double)hash.cardinality() / hash.size(), numPhases);
  }

  /**
   * Insert a new String into the Bloom filter.
   * 
   * @param n The string to insert.
   */
  public void insert(String n) {
    insert(n.getBytes());
  }

  /**
   * Insert a new set of bits into the Bloom filter.
   * 
   * @param ba The array of bytes.
   */
  public void insert(byte[] ba) {
    bf_process(ba, false);
  }

  /**
   * Check that a String exists in the bloom filter. 
   * 
   * @param n The string to check.
   * @return A boolean; if true, we assert the string is in the Bloom filter.
   */
  public boolean check(String n) {
    return check(n.getBytes());
  }

  /**
   * Check that a set of bits exists in the Bloom filter.
   * 
   * @param ba The array of bytes to check.
   * @return A boolean; if true, we assert the bytes are in the Bloom filter.
   */
  public boolean check(byte[] ba) {
      return bf_process(ba, true);
  }

  /**
   * The actual processing of the Bloom filter.
   *
   * @param ba The data to insert or check
   * @param check Is this a check?
   * @return If a check, true if match, false if not.  If not a check, true
   * is always returned.
   */
  private boolean bf_process(byte[] ba, boolean check) {

    // Create a hash of the incoming bytes
    byte[] uniqueHash = hasher.digest(ba);
    // For checks: has it been found?  We assume so until we find an exception
    boolean found = true;

    // For each phase, compute the index and set the appropriate bit
    for (int i = 0; i < numPhases; i++) {
      // Index computation is accomplished by copying the appropriate
      // bits into an integer field.  XXX - We enumerate bit-by-bit, which 
      // might be optimizable.
      int index = 0, offset = i * indexSize;
      for (int j = offset; j < offset + indexSize; j++) {
        // For each iteration, move the existing bits over and extract
        // the next bit and stick it at the end, i.e., a MSB implementation.
        index = (index << 1) + ((uniqueHash[j / 8] >>> (7 - (j % 8))) & 1);
      }

      // Now check or insert.
      if (!check)
        hash.set(index);
      else if (!hash.get(index)) { // Nope, not here, break out
        found = false;
        break;
      }
    }
    return found;
  }

  /**
   * Return the user-supplied name of the bloom filter.
   * 
   * @return The name.
   */
  public String getName() {
    return name;
  }

  /**
   * Return user-supplied metadata associated with this Bloom filter.
   * 
   * @return The metadata.
   */
  public Serializable getMetadata() {
    return this.metadata;
  }


  /**
   * toString method: for now, it returns the name of the bloom filter and
   * a dump of the bitset.
   *
   * @return A string representation of the bloom filter
   */
  public String toString() {
    return getName() + "[" + hash.toString() + "]";
  }

  /**
   * Clear out the bloom filter.
   */
  public void reset() {
    hash.clear();
  }

  /**
   * Trap a deserialization operation, and make sure to create a new 
   * MessageDigest instance.  We have to do this here, as opposed to at
   * declaration time, as we need to trap an exception while we're at it.
   * 
   * @param in The ObjectInputStream
   * @throws IOException 
   * @throws ClassNotFoundException
   */
  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    try {
      hasher = MessageDigest.getInstance(HASH_METHOD);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  /**
   * Compress this Bloom filter, via gzip, to a byte array.
   * 
   * @return A byte array, or null if a failure occurs.
   */
  public byte[] compressFilter() {
    byte[] bloomFilter = null;

    try { // Set up the streams, and output
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos =
        new ObjectOutputStream(new GZIPOutputStream(baos));
      oos.writeObject(this);
      oos.close();
      baos.close();
      bloomFilter = baos.toByteArray();
    } catch (Exception e) {
	//log.error("Could not compress Bloom filter", e);
      return null;
    }

    return bloomFilter;
  }

  /**
   * Uncompress and return a new Bloom filter.
   * 
   * @param bf The Bloom filter, in gzip'ed byte array form.
   * @return A new Bloom filter instance, or null if failure
   */
  public static CUBloomFilter decompressFilter(byte[] bf) {
    CUBloomFilter newFilter = null;

    try {
      // Read using ByteArrayInputStream
      ObjectInputStream ois =
        new ObjectInputStream(
          new GZIPInputStream(new ByteArrayInputStream(bf)));

      newFilter = (CUBloomFilter)ois.readObject();
      ois.close(); // We don't really need to do this, but why not?
    } catch (Exception e) {
	//log.error("Could not read Bloom filter back in", e);
      return null;
    }

    return newFilter;
  }

  /**
   * Test driver.
   */
    /*
  public static void main(String args[]) {
    // Log4j initialization
    BasicConfigurator.configure();
    log.setLevel(Level.INFO);
    BloomTester.main(args);
  }
    */
}

/**
 * Simple Bloom filter testing code.  Take a list of words, one per line, 
 * from a file, randomly select N of them, and then conduct M tests inside 
 * and outside of that dictionary.  /usr/dict/words is recommended.
 * 
 * @author Janak J Parekh (janak@cs.columbia.edu)
 * @version $Revision: 1.1 $
 */
class BloomTester {
  /** Number of words to add into the Bloom filter */
  public static final int NUMWORDS = 20000;
  /** Number of words to pick randomly from dictionary and check */
  public static final int CHECKWORDS = 40000;
  private int falsePositives = 0, trueNegatives = 0;

  public static void main(String[] args) {
    try {
      new BloomTester();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public BloomTester() throws InstantiationException {
    ArrayList<String> dict = new ArrayList<String>();
    HashSet<String> dictwords = new HashSet<String>();
    CUBloomFilter bf = new CUBloomFilter("test", 16, 5, null);
    Random r = new Random();

    try {
      BufferedReader br = new BufferedReader(new FileReader("words"));
      while (br.ready()) {
        String word = br.readLine();
        if (word != null && word.length() > 0)
          dict.add(word);
        else
          break;
      }

      System.out.println(dict.size() + " words read, about to insert");
      // Insert words
      for (int i = 0; i < NUMWORDS; i++) {
        String word = dict.get(r.nextInt(dict.size()));
        dictwords.add(word);
        bf.insert(word);
      }

      // Now test CHECKWORDS words in the Bloom Filter, and a random set of 
      for (int i = 0; i < CHECKWORDS; i++) {
        String tempWord = dict.get(r.nextInt(dict.size()));
        if (!dictwords.contains(tempWord)) {
          trueNegatives++;
          if (bf.check(tempWord)) {
            falsePositives++;
          }
        }
      }
      
      // Stats?
      System.out.println("Bloom filter size: " + bf.getSize());
      System.out.println("False positive rate is " + bf.falsePositiveRate());
      System.out.println("Actual false positives: " + falsePositives);
      System.out.println("Number true negatives: " + trueNegatives);
      System.out.println(
        "Actual false positive rate: "
          + (double)falsePositives / trueNegatives);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
