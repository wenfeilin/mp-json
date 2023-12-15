import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import javax.imageio.plugins.tiff.TIFFField;

/**
 * Utilities for our simple implementation of JSON.
 * 
 * @author SamR (starter code)
 * @author Lydia Ye
 * @author Wenfei Lin
 */
public class JSON {
  // +---------------+-----------------------------------------------
  // | Static fields |
  // +---------------+

  /**
   * The current position in the input.
   */
  static int pos;

  // +----------------+----------------------------------------------
  // | Static methods |
  // +----------------+

  /**
   * Parse a string into JSON.
   */
  public static JSONValue parse(String source) throws ParseException, IOException {
    return parse(new StringReader(source));
  } // parse(String)

  /**
   * Parse a file into JSON.
   */
  public static JSONValue parseFile(String filename) throws ParseException, IOException {
    FileReader reader = new FileReader(filename);
    JSONValue result = parse(reader);
    reader.close();
    return result;
  } // parseFile(String)

  /**
   * Parse JSON from a reader.
   */
  public static JSONValue parse(Reader source) throws ParseException, IOException {
    pos = 0;
    JSONValue result = parseKernel(source);
    if (-1 != skipWhitespace(source)) {
      throw new ParseException("Characters remain at end", pos);
    }
    return result;
  } // parse(Reader)

  // +---------------+-----------------------------------------------
  // | Local helpers |
  // +---------------+

  /**
   * Determines if the inputted character is a number from 0-9.
   * 
   * @param input current char being read
   * @return true if input is a number from 0-9
   */
  private static boolean isNumber(int input) {
    int num0InASCII = 48;
    int num9InASCII = 57;
    return num0InASCII <= input && num9InASCII >= input;
  } // isNumber(int)

  /**
   * Adds the next characters read on the end of numStr as long 
   * as they are digit characters.
   * 
   * @param numStr string with the value of the JSONReal or 
   *               JSONInteger so far
   * @param nextInput current char being read
   * @param source reader
   * @throws IOException I/O error (reading)
   */
  private static void nextNumbers(String numStr, int nextInput, Reader source) 
      throws IOException {
    do {
      addAndIncrement(numStr, nextInput, source);
    } while (isNumber(nextInput)); 
  } // nextNumbers(String, int, Reader)

  /**
   * Adds the next character read on the end of numStr if 
   * the former is a digit character.
   * 
   * @param numStr string with the value of the JSONReal or 
   *               JSONInteger so far
   * @param nextInput current char being read
   * @param source reader
   * @return the next input
   * @throws IOException I/O error (reading)
   */
  private static int addAndIncrement(String numStr, int nextInput, Reader source) 
      throws IOException {
    numStr += (char) nextInput;
    nextInput = skipWhitespace(source);
    return nextInput;
  } // addAndIncrement(String, int, Reader)

  /**
   * Reads the input and determines if what is being read is a valid JSONReal 
   * or JSONInteger and creates the corresponding JSONValue.
   * 
   * @param isPositive true if the number value is positive and false if not
   * @param input current char being read
   * @param source reader
   * @return JSONValue parsed from reader
   * @throws IOException I/O error (reading)
   * @throws ParseException when end of file reached unexpectedly or 
   *                        incorrect JSON value written in file
   */
  private static JSONValue readNum(boolean isPositive, int input, Reader source) 
      throws IOException, ParseException {
      
    String numStr = "";

    if (isPositive) {
      numStr += (char) input;
    } else {
      numStr = "-" + (char) input;
    } // if/else
      
    int nextInput = skipWhitespace(source);

    while (isNumber(nextInput)) {
      nextInput = addAndIncrement(numStr, nextInput, source);
    } // while

    if (nextInput == (int) 'e' || nextInput == (int) 'E' ) {
      nextInput = addAndIncrement(numStr, nextInput, source);

      if (nextInput == (int) '+' || nextInput == (int) '-') {
        nextInput = addAndIncrement(numStr, nextInput, source);
        
        if (isNumber(nextInput)) {
          nextNumbers(numStr, nextInput, source);
        } else {
          throw new ParseException("Invalid format for JSONReal", pos);
        } // if...else
      } else {
        throw new ParseException("Invalid format for JSONReal", pos);
      } // if

      checkLeadingZero(isPositive, numStr);
      return new JSONReal(numStr);
    } else if (nextInput == (int) '.') {
      nextInput = addAndIncrement(numStr, nextInput, source);
      if (isNumber(nextInput)) {
        nextNumbers(numStr, nextInput, source);

        if (nextInput == (int) 'e' || nextInput == (int) 'E' ) {
          nextInput = addAndIncrement(numStr, nextInput, source);

          if (nextInput == (int) '+' || nextInput == (int) '-') {
            nextInput = addAndIncrement(numStr, nextInput, source);

            if (isNumber(nextInput)) {
              nextNumbers(numStr, nextInput, source);
            } else {
              throw new ParseException("Invalid format for JSONReal", pos);
            } // if...else
          } else {
            throw new ParseException("Invalid format for JSONReal", pos);
          } // if/else
        } // if
      } else {
        throw new ParseException("Invalid format for JSONReal", pos);
      } // if...else

      checkLeadingZero(isPositive, numStr);
      return new JSONReal(numStr);
    } else {
      checkLeadingZero(isPositive, numStr);
      return new JSONInteger(numStr);
    } // if...else      
  } // readNum(boolean, int, Reader)

  /**
   * Checks that the input for JSONReal or JSONInteger does not having 
   * leading zeroes when negative.
   * 
   * @param isPostive true if the number value is positive and false if not
   * @param numStr the value of the JSONReal or JSONInteger as a string
   * @throws ParseException when end of file reached unexpectedly or 
   *                        incorrect JSON value written in file
   */
  private static void checkLeadingZero(boolean isPostive, String numStr) 
      throws ParseException {
    if (!isPostive) {
      if ((numStr.length() > 2) && 
          (numStr.substring(1,3).equals("00"))) { 
        throw new ParseException("Invalid format for JSONReal at postion " + 
            (pos - (numStr.length() - 3)), pos - (numStr.length() -2));
      } // if
    } // if
  } // checkLeadingZero(boolean, String)

  /**
   * Reads the input and determines if what is being read is a 
   * valid JSONConstant and creates it.
   * 
   * @param nextInput current char being read
   * @param booleanStr "true", "false", or "null"
   * @param source reader
   * @return the JSONValue (JSONConstant) written in the file
   * @throws ParseException when end of file reached unexpectedly or 
   *                        incorrect JSON value written in file
   * @throws IOException I/O problem when reading
   */
  private static JSONConstant matchJSONConstant(
        int nextInput, 
        String booleanStr, 
        Reader source) throws IOException, ParseException {
    for (int i = 1; i < booleanStr.length(); i++) {
      nextInput = skipWhitespace(source);
      if (nextInput != (int) booleanStr.charAt(i)) {
        throw new ParseException("Incorrectly written JSONConstant", pos);
      } // if
    } // for

    return new JSONConstant(booleanStr);
  } // matchJSONConstant(int, String, Reader)

  /**
   * Reads the input and determines if what is being read is a 
   * valid JSONString and creates it.
   * 
   * @param nextInput current char being read
   * @param source reader
   * @return the JSONValue (JSONString) written in the file
   * @throws ParseException when end of file reached unexpectedly or 
   *                        incorrect JSON value written in file
   * @throws IOException I/O problem when reading
   */
  private static JSONString matchJSONString(int nextInput, Reader source) 
      throws IOException, ParseException {
    String strForJSONString = "";
    nextInput = skipWhitespace(source);
    
    while (nextInput != (int) '"') {
      checkEOF(nextInput);
      strForJSONString += (char) nextInput;
      nextInput = skipWhitespace(source);
    } // while
    return new JSONString(strForJSONString);
  } // matchJSONString(int, Reader)

  /**
   * Reads the input and determines if what is being read is a 
   * valid JSONArray and creates it.
   * 
   * @param input current char being read
   * @param source reader
   * @return the JSONValue (JSONArray) written in the file
   * @throws ParseException when end of file reached unexpectedly 
   *                        or incorrect JSON value written in file
   * @throws IOException I/O problem when reading
   */
  private static JSONArray matchJSONArray(int input, Reader source) 
      throws ParseException, IOException {
    JSONArray arr = new JSONArray();
       
    while (input != (int) ']') {
      checkEOF(input);

      arr.add(parseKernel(source));
      input = skipWhitespace(source);
      
      if (input != (int) ',') {
        if (input == (int) ']') {
          return arr;
        } // if
        throw new ParseException("Invalid format for JSONArray", pos);
      } // if
    } // while

    return arr;
  } // matchJSONArray(int, Reader)

  /**
   * Reads the input and determines if what is being read is a 
   * valid JSONHash and creates it.
   * 
   * @param input current char being read
   * @param source reader
   * @return the JSONValue (JSONHash) written in the file
   * @throws ParseException when end of file reached unexpectedly or 
   *                        incorrect JSON value written in file
   * @throws IOException I/O problem when reading
   */
  private static JSONHash matchJSONHash(int input, Reader source) 
      throws ParseException, IOException {
    JSONHash hashtable = new JSONHash();

    while (input != (int) '}') {
      JSONValue key = parseKernel(source);
      if (!(key instanceof JSONString)) {
        throw new ParseException("Invalid key for JSONHash", pos);
      } // if

      input = skipWhitespace(source);
      if (input != ':') {
        throw new ParseException("Invalid format for JSONHash", pos);
      } // if
      
      JSONValue value = parseKernel(source);
      hashtable.set((JSONString) key, value);
      input = skipWhitespace(source);

      if (input != (int) ',') {
        if (input == (int) '}') {
          return hashtable;
        } // if 
        throw new ParseException("Invalid format for JSONHash", pos);
      } // if
    } // while
    return hashtable;
  } // matchJSONHash(int, Reader)

  /**
   * Parse JSON from a reader, keeping track of the current position.
   */
  static JSONValue parseKernel(Reader source) throws ParseException, IOException {
    int ch;
    ch = skipWhitespace(source);
    checkEOF(ch);
 
    int input = ch;
    int nextInput = -1;

    // Read postive integer/real
    if (isNumber(input)) {
      return readNum(true, input, source);
    } // if

    // Read negative integer/real
    if (input == '-') {
      input = skipWhitespace(source);
      if (isNumber(input)) {
        return readNum(false, input, source);
      } // if
    } // if

    switch (input) {
      // JSON String
      case (int) '"':
        return matchJSONString(nextInput, source);

      // JSON Hash (Object)
      case (int) '{': 
        // Doesn't work for empty hashes
        return matchJSONHash(input, source);

      // JSON Array 
      case (int) '[': 
        // Doesn't work for empty arrs
        return matchJSONArray(input, source);

      // JSON Constants: (true, false, null)
      case ((int) 't'):
        return matchJSONConstant(nextInput, "true", source);
        
      case ((int) 'f'):
        return matchJSONConstant(nextInput, "false", source);

      case ((int) 'n'):
        return matchJSONConstant(nextInput, "null", source);

      default:
        throw new ParseException("Invalid initial character", pos);
    } // switch
  } // parseKernel(Reader)

  /**
   * Checks to make sure the end of the file hasn't been reached
   * unexpectedly and if it did, throws an exception.
   * 
   * @param ch current input being read
   * @throws ParseException when end of file is reached unexpectedly
   */
  private static void checkEOF(int ch) throws ParseException {
    if (-1 == ch) {
      throw new ParseException("Unexpected end of file", pos);
    } // if
  } // checkEOF(int)

  /**
   * Get the next character from source, skipping over whitespace.
   */
  static int skipWhitespace(Reader source) throws IOException {
    int ch;
    do {
      ch = source.read();
      ++pos;
    } while (isWhitespace(ch));
    return ch;
  } // skipWhitespace(Reader)

  /**
   * Determine if a character is JSON whitespace (newline, carriage return,
   * space, or tab).
   */
  static boolean isWhitespace(int ch) {
    return (' ' == ch) || ('\n' == ch) || ('\r' == ch) || ('\t' == ch);
  } // isWhiteSpace(int)

} // class JSON