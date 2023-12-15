import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * JSON hashes/objects.
 * 
 * @author SamR (starter code)
 * @author Lydia Ye
 * @author Wenfei Lin
 */
public class JSONHash implements JSONValue {

  // +-----------+-------------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * The load factor for expanding the table.
   */
  static final double LOAD_FACTOR = 0.5;
  static final int INITIAL_CAPACITY = 41;


  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The number of values currently stored in the hash table. We use this to determine when to
   * expand the hash table.
   */
  int size = 0;

  /**
   * The array that we use to store the ArrayList of key/value pairs. (We use an array, rather than
   * an ArrayList, because we want to control expansion and ArrayLists of ArrayLists are just
   * weird.)
   */
  Object[] buckets;

  /**
   * Our helpful random number generator, used primarily when expanding the size of the table.
   */
  Random rand;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new hash table.
   */
  public JSONHash() {
    this.rand = new Random();
    this.buckets = new Object[INITIAL_CAPACITY];
    this.size = 0;
  } // JSONHash

  // +-------------------------+-------------------------------------
  // | Standard object methods |
  // +-------------------------+

  /**
   * Convert to a string (e.g., for printing).
   */
  public String toString() {

    String str = "{";
    Iterator<KVPair<JSONString, JSONValue>> it = getValue();

    while (it.hasNext()) {
      KVPair<JSONString, JSONValue> next = it.next();
      str += next.key() + ": " + next.value() + ", ";
    } // while

    if (str.length() > 1) {
      str = str.substring(0, str.length() - 2);
    } // if

    return str + "}";
  } // toString()

  /**
   * Compare to another object.
   */
  public boolean equals(Object other) {
    if (other instanceof JSONHash) {
      return Arrays.equals(this.buckets, ((JSONHash) other).buckets);
    } else {
      return false;
    } // if/else
  } // equals(Object)

  /**
   * Compute the hash code.
   */
  public int hashCode() {
    return this.buckets.hashCode();
  } // hashCode()

  // +--------------------+------------------------------------------
  // | Additional methods |
  // +--------------------+

  /**
   * Write the value as JSON.
   */
  public void writeJSON(PrintWriter pen) {
    pen.print(this.toString());
    pen.flush();
  } // writeJSON(PrintWriter)

  /**
   * Get the underlying value.
   */
  public Iterator<KVPair<JSONString, JSONValue>> getValue() {
    return this.iterator();
  } // getValue()

  // +-------------------+-------------------------------------------
  // | Hashtable methods |
  // +-------------------+

  /**
   * Get the value for a particular key.
   */
  @SuppressWarnings("unchecked")
  public JSONValue get(JSONString key) {
    int index = find(key);
    ArrayList<KVPair<JSONString, JSONValue>> alist =
        (ArrayList<KVPair<JSONString, JSONValue>>) buckets[index];

    // Don't even search for it in the bucket b/c bucket is null
    if (alist == null) {
      throw new IndexOutOfBoundsException("Invalid key: " + key);
    } else {
      // loop through all elements in the bucket
      for (int i = 0; i < alist.size(); i++) {
        if (alist.get(i).key().equals(key)) {
          KVPair<JSONString, JSONValue> pair = alist.get(i);
          return pair.value();
        } // if we found the target key in the bucket
      } // for
      // If key does not exist in bucket after searching for it (invalid key)
      throw new IndexOutOfBoundsException("Invalid key: " + key);
    } // if...else
  } // get(JSONString)

  /**
   * Find the index of the entry with a given key. If there is no such entry, return the index of an
   * entry we can use to store that key.
   */
  int find(JSONString key) {
    return Math.abs(key.hashCode()) % this.buckets.length;
  } // find(JSONString)

  /**
   * Get all of the key/value pairs.
   */
  @SuppressWarnings("unchecked")
  public Iterator<KVPair<JSONString, JSONValue>> iterator() {
    return new Iterator<KVPair<JSONString, JSONValue>>() {
      // The position in the array of buckets
      int outerIndex = 0; 
      // The position in the arraylist of current bucket
      int innerIndex = 0; 

      /**
       * Determine if the hash (hash table) has a next element (KVPair).
       */
      public boolean hasNext() {
        if (outerIndex >= buckets.length) {
          // If the position is out of bound of the array of buckets
          return false;
        } // if

        // If the current bucket is null
        if (buckets[outerIndex] == null) {
          for (int i = outerIndex + 1; i < buckets.length; i++) {
            if (buckets[i] != null) {
              // If we found next nonempty bucket
              return true; 
            } // if
          } // for
          // If all subsequent buckets are null too
          return false; 
        } // if

        return true;
      } // hasNext()

      /**
       * Return the next element (KVPair) in the hash (hash table) and 
       * prepare the iterator for the next next call by making it point to 
       * the next, next element.
       */
      public KVPair<JSONString, JSONValue> next() throws NoSuchElementException {
        if (!this.hasNext()) {
          throw new NoSuchElementException();
        } // if

        // If the current bucket is null (can only happen when calling next() for the first time)
        if (buckets[outerIndex] == null) {
          // Find the index of next nonempty bucket
          do {
            outerIndex++;
          } while (buckets[outerIndex] == null);
          innerIndex = 0;
        } // if

        ArrayList<KVPair<JSONString, JSONValue>> alist =
            (ArrayList<KVPair<JSONString, JSONValue>>) buckets[outerIndex];
        KVPair<JSONString, JSONValue> next = alist.get(innerIndex);

        if (innerIndex < alist.size() - 1) {
          // if the current KVPair is not the last one in the bucket
          innerIndex++;
        } else {
          // If the current KVPair is the last one in the current bucket, we advance
          // the outerIndex to the next nonempty bucket
          do {
            outerIndex++;
          } while (outerIndex < buckets.length && buckets[outerIndex] == null);
          innerIndex = 0;
        } // if/else
        return next;
      } // next()
    }; // new Iterator for buckets
  } // iterator()

  /**
   * Set a value associated with a key.
   */
  @SuppressWarnings("unchecked")
  public void set(JSONString key, JSONValue value) {
    // If there are too many entries, expand the table.
    if (this.size > (this.buckets.length * LOAD_FACTOR)) {
      expand();
    } // if there are too many entries

    // Find out where the key belongs and put the pair there.
    int index = find(key);
    ArrayList<KVPair<JSONString, JSONValue>> alist =
        (ArrayList<KVPair<JSONString, JSONValue>>) this.buckets[index];

    // Special case: Nothing there yet
    if (alist == null) {
      alist = new ArrayList<KVPair<JSONString, JSONValue>>();
      this.buckets[index] = alist;
    } // if

    for (int i = 0; i < alist.size(); i++) {
      if (alist.get(i).key().equals(key)) {
        alist.set(i, new KVPair<JSONString, JSONValue>(key, value));
      } // if
    } // for
    alist.add(new KVPair<JSONString, JSONValue>(key, value));
    ++this.size;

    return; 
  } // set(JSONString, JSONValue)

  /**
   * Expand the size of the table.
   */
  @SuppressWarnings("unchecked")
  void expand() {
    // Figure out the size of the new table
    int newSize = 2 * this.buckets.length + rand.nextInt(10);

    // Remember the old table
    Object[] oldBuckets = this.buckets;
    // Create a new table of that size.
    this.buckets = new Object[newSize]; // new buckets
    this.size = 0;

    // Move each pair in each bucket from the old table to their appropriate
    // location in the new table.
    // Cycle through each bucket
    for (int i = 0; i < oldBuckets.length; i++) { 
      ArrayList<KVPair<JSONString, JSONValue>> oldBucket =
          (ArrayList<KVPair<JSONString, JSONValue>>) oldBuckets[i];
      if (oldBucket != null) {
        // Cycle through each KVPair in a bucket
        for (int j = 0; j < oldBucket.size(); j++) { 
          set(oldBucket.get(j).key(), oldBucket.get(j).value());
        } // for
      } // if
    } // for
  } // expand()

  /**
   * Find out how many key/value pairs are in the hash table.
   */
  public int size() {
    return this.size();
  } // size()
} // class JSONHash