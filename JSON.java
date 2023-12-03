import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import javax.imageio.plugins.tiff.TIFFField;

/**
 * Utilities for our simple implementation of JSON.
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

  private static Boolean isNumber(int input) {
    int num0InASCII = 48;
    int num9InASCII = 57;
    return num0InASCII <= input && num9InASCII >= input;
  } // isNUmber(int)


  private static void nextNumbers(String numStr, int nextInput, Reader source) throws IOException {
    do {
      addAndIncrement(numStr, nextInput, source);
    } while (isNumber(nextInput)); 
  } // nextNumbers(String, int, Reader)

  private static int addAndIncrement(String numStr, int nextInput, Reader source) throws IOException {
    numStr += (char) nextInput;
    nextInput = skipWhitespace(source);
    return nextInput;
  } // addAndIncrement(String, int, Reader)


  private static JSONValue readNum(boolean isPositive, int input, Reader source) throws IOException, ParseException{
      
    String numStr = "";
    if (isPositive) {
        numStr += (char) input;
      } else {
        numStr = "-" + (char) input;
      }
      
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
              } // if
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
  } // readNum(Boolean, int, Reader)


  private static void checkLeadingZero(Boolean isPostive, String numStr) throws ParseException {
    if (!isPostive) {
      if ((numStr.length() > 2) && (numStr.substring(1,3).equals("00"))) {
        throw new ParseException("Invalid format for JSONReal at postion " + (pos - (numStr.length() - 3)), pos - (numStr.length() -2));
      } //if
    } // if
  } // checkLeadingZero(Boolean, String)

    
  /**
   * Parse JSON from a reader, keeping track of the current position
   */
  static JSONValue parseKernel(Reader source) throws ParseException, IOException {
    int ch;
    ch = skipWhitespace(source);
    if (-1 == ch) {
      throw new ParseException("Unexpected end of file", pos);
    }

    // int num0InASCII = 48;
    // int num9InASCII = 57;
 
    int input = ch;
    int nextInput;


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

    /* 
    if (isNumber(input)) {
      String numStr = "" + (char) input;
      
      nextInput = skipWhitespace(source);
      while (isNumber(nextInput)) {
        addAndIncrement(numStr, nextInput, source);
      } // while

      if (nextInput == (int) 'e' || nextInput == (int) 'E' ) {
        addAndIncrement(numStr, nextInput, source);
        if (nextInput == (int) '+' || nextInput == (int) '-') {
          addAndIncrement(numStr, nextInput, source);
          if (isNumber(nextInput)) {
            nextNumbers(numStr, nextInput, source);
          } else {
            throw new ParseException("Invalid format for JSONReal", pos);
          } // if...else
        } else {
          throw new ParseException("Invalid format for JSONReal", pos);
        } // if
        return new JSONReal(numStr);
      } else if (nextInput == (int) '.') {
        addAndIncrement(numStr, nextInput, source);
          if (isNumber(nextInput)) {
            nextNumbers(numStr, nextInput, source);
          
            if (nextInput == (int) 'e' || nextInput == (int) 'E' ) {
              addAndIncrement(numStr, nextInput, source);
              if (nextInput == (int) '+' || nextInput == (int) '-') {
                addAndIncrement(numStr, nextInput, source);
                if (isNumber(nextInput)) {
                  nextNumbers(numStr, nextInput, source);
                } else {
                  throw new ParseException("Invalid format for JSONReal", pos);
                } // if...else
              } else {
                throw new ParseException("Invalid format for JSONReal", pos);
              } // if
            } // if
          } else {
            throw new ParseException("Invalid format for JSONReal", pos);
          } // if...else
          return new JSONReal(numStr);
      } else {
        return new JSONInteger(numStr);
      } // if...else  
    } //if
*/
/*  
      String numStr = "-";
      input = skipWhitespace(source);

      if (num0InASCII <= input && num9InASCII >= input) {
        numStr += (char) input;
        
        nextInput = skipWhitespace(source);
        while (num0InASCII <= nextInput && num9InASCII >= nextInput) {
          numStr += (char) nextInput;
          nextInput = skipWhitespace(source);
        } // while
  
        if (nextInput == (int) 'e' || nextInput == (int) 'E' ) {
          numStr += (char) nextInput;
          nextInput = skipWhitespace(source);
          if (nextInput == (int) '+' || nextInput == (int) '-') {
            numStr += (char) nextInput;
            nextInput = skipWhitespace(source);
            if (num0InASCII <= nextInput && num9InASCII >= nextInput) {
              do {
                numStr += (char) nextInput;
                nextInput = skipWhitespace(source);
              } while (num0InASCII <= nextInput && num9InASCII >= nextInput); 
            } else {
              throw new ParseException("Invalid format for JSONReal", pos);
            } // if...else
          } else {
            throw new ParseException("Invalid format for JSONReal", pos);
          } // if

          if (numStr.substring(1,3).equals("00")) {
            throw new ParseException("Invalid format for JSONReal at postion " + (pos - (numStr.length() - 3)), pos - (numStr.length() -2));
          } //if
          return new JSONReal(numStr);
        } else if (nextInput == (int) '.') {
          numStr += ".";
          nextInput = skipWhitespace(source);
            if (num0InASCII <= nextInput && num9InASCII >= nextInput) {
              do {
                numStr += (char) nextInput;
                nextInput = skipWhitespace(source);
              } while (num0InASCII <= nextInput && num9InASCII >= nextInput); 
              
  
              if (nextInput == (int) 'e' || nextInput == (int) 'E' ) {
                numStr += (char) nextInput;
                nextInput = skipWhitespace(source);
                if (nextInput == (int) '+' || nextInput == (int) '-') {
                  numStr += (char) nextInput;
                  nextInput = skipWhitespace(source);
                  if (num0InASCII <= nextInput && num9InASCII >= nextInput) {
                    do {
                      numStr += (char) nextInput;
                      nextInput = skipWhitespace(source);
                    } while (num0InASCII <= nextInput && num9InASCII >= nextInput); 
                  } else {
                    throw new ParseException("Invalid format for JSONReal %s", pos);
                  } // if...else
                } else {
                  throw new ParseException("Invalid format for JSONReal", pos);
                } // if
              } // if
            } else {
              throw new ParseException("Invalid format for JSONReal", pos);
            } // if...else

            if (numStr.substring(1,3).equals("00")) {
              throw new ParseException("Invalid format for JSONReal at postion " + (pos - (numStr.length() - 3)), pos - (numStr.length() -2));
            } //if
            return new JSONReal(numStr);
        } else {
          if (numStr.substring(1,3).equals("00")) {
              throw new ParseException("Invalid format for JSONInteger at postion " + (pos - (numStr.length() - 3)), pos - (numStr.length() -2));
            } //if
          return new JSONInteger(numStr);
        } // if...else   
      } // if
*/


    switch (input) {
      // String
      case (int) '"':
        String strForJSONString = "";

        // nextInput = source.read();
        // pos++;
        nextInput = skipWhitespace(source);
        
        while (nextInput != (int) '"') {
          strForJSONString += (char) nextInput;
          nextInput = skipWhitespace(source);
        }
        return new JSONString(strForJSONString);

      // Object (Hash)
      case (int) '{': // doesn't work for empty hashes
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
            }
            throw new ParseException("Invalid format for JSONHash", pos);
          } // if
        } // while
        return hashtable;

      // Array 
      case (int) '[': // doesn't work for empty arrs
        
        JSONArray arr = new JSONArray();
        // nextInput = source.read();
        // pos++;
        while (input != (int) ']') {
          arr.add(parseKernel(source));
          input = skipWhitespace(source);
          // nextInput = source.read();
          // pos++;
          if (input != (int) ',') {
            if (input == (int) ']') {
              return arr;
            }
            throw new ParseException("Invalid format for JSONArray", pos);
          } // if
        } // while
    
        return arr;

      case ((int) 't'):
        boolean isTrueConstant = false;
        nextInput = source.read();
        
        if (nextInput == (int)'r') {
          nextInput = source.read();
          pos++;

          if (nextInput == (int)'u') {
            nextInput = source.read();
            pos++;

            if (nextInput == (int)'e') {
              isTrueConstant = true;
            }
          } else {
            throw new ParseException("Incorrectly written JSONConstant", pos);
          } // if/else
        } else {
          throw new ParseException("Incorrectly written JSONConstant", pos);
        } // if/else

        if (isTrueConstant) {
          return new JSONConstant("true");
        } // if
        
      case ((int) 'f'):
        boolean isFalseConstant = false;
        nextInput = source.read();
        
        if (nextInput == (int)'a') {
          nextInput = source.read();
          pos++;

          if (nextInput == (int)'l') {
            nextInput = source.read();
            pos++;

            if (nextInput == (int)'s') {
              nextInput = source.read();
              pos++;

              if (nextInput == (int)'e') {
                isFalseConstant = true;
              }
            } else {
              throw new ParseException("Incorrectly written JSONConstant", pos);
            } // if/else
          } else {
            throw new ParseException("Incorrectly written JSONConstant", pos);
          } // if/else
        } else {
          throw new ParseException("Incorrectly written JSONConstant", pos);
        } // if/else

        if (isFalseConstant) {
          return new JSONConstant("false");
        } // if

      case ((int) 'n'):
        boolean isNullConstant = false;
        nextInput = source.read();
        
        if (nextInput == (int)'u') {
          nextInput = source.read();
          pos++;

          if (nextInput == (int)'l') {
            nextInput = source.read();
            pos++;

            if (nextInput == (int)'l') {
              isNullConstant = true;
            }
          } else {
            throw new ParseException("Incorrectly written JSONConstant", pos);
          } // if/else
        } else {
          throw new ParseException("Incorrectly written JSONConstant", pos);
        } // if/else

        if (isNullConstant) {
          return new JSONConstant("null");
        } // if
      default:

        throw new ParseException("Invalid initial character", pos);
        /* 
        if (input == negativeSignASCII || (input >= num0InASCII && input <= num9InASCII)) {
        //   while (source.read() == ) {
        //     str += (char) input;
        //     if (input == decimalPointASCII || input == EInASCII || input == eInASCII) {
        //       isReal = true;
        //     }
        // }
        }
        // real number contains decimal point or exponent

// String
    // case 147:
    //   break;
    // // Object (Hash)
    // case 123:
    //   break;
    // // Array 
    // case 91:
    //   break;
    // default:
    //   // 45 (negative sign)
    //   // 48 -> 57 (numbers 0-9)
    //   while (input == 45 || || (input >= 48 && input <= 57)) {
    //     str += (char) input;
    //     if ()
    //   }
      // real number contains decimal point or exponent*/
    } // switch
  } // parseKernel(Reader)
    


  

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
