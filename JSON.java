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

  /**
   * Parse JSON from a reader, keeping track of the current position
   */
  static JSONValue parseKernel(Reader source) throws ParseException, IOException {
    int ch;
    ch = skipWhitespace(source);
    if (-1 == ch) {
      throw new ParseException("Unexpected end of file", pos);
    }


    int num0InASCII = 48;
    int num9InASCII = 57;
    
    // int decimalPointASCII = 46;
    // int eInASCII = 101;
    // int EInASCII = 69;
    // int negativeSignASCII = 45;
    // boolean isReal = false;

    int input = source.read();
    pos++;
    int nextInput;

    if (num0InASCII <= input || num9InASCII >= input) {
      String numStr = "";
      nextInput = source.read();
      while (num0InASCII <= nextInput || num9InASCII >= nextInput) {
        numStr += (char) nextInput;
        nextInput = source.read();
        pos++;
      } // while

      nextInput = source.read();
      pos++;
      if (nextInput == (int) 'e' || nextInput == (int) 'E' ) {
        nextInput = source.read();
        pos++;
        numStr += (char) nextInput;
        if (nextInput == (int) '+' || nextInput == (int) '-') {
          nextInput = source.read();
          pos++;
          if (num0InASCII <= nextInput || num9InASCII >= nextInput) {
            do {
              numStr += (char) nextInput;
              nextInput = source.read();
              pos++;
            } while (num0InASCII <= nextInput || num9InASCII >= nextInput); 
          } else {
            throw new ParseException("Invalid format for JSONReal", pos);
          } // if...else
        } else if (num0InASCII <= nextInput || num9InASCII >= nextInput) {
          do {
            numStr += (char) nextInput;
            nextInput = source.read();
            pos++;
          } while (num0InASCII <= nextInput || num9InASCII >= nextInput);
        } else {
          throw new ParseException("Invalid format for JSONReal", pos);
        } // if
        return new JSONReal(numStr);
      } else if (nextInput == (int) '.') {
          nextInput = source.read();
          pos++;
          numStr += (char) nextInput;
          if (num0InASCII <= nextInput || num9InASCII >= nextInput) {
            do {
              numStr += (char) nextInput;
              nextInput = source.read();
              pos++;
            } while (num0InASCII <= nextInput || num9InASCII >= nextInput); 
          } else {
            throw new ParseException("Invalid format for JSONReal", pos);
          } // if...else
          return new JSONReal(numStr);
      } else {
        return new JSONInteger(numStr);
      } // if...else      
    } // if

    if (input == '-') {
      String numStr = "";
      nextInput = source.read();

      if (num0InASCII <= nextInput || num9InASCII >= nextInput) {
        numStr = "";
        nextInput = source.read();
        while (num0InASCII <= nextInput || num9InASCII >= nextInput) {
          numStr += (char) nextInput;
          nextInput = source.read();
          pos++;
        } // while
  
        nextInput = source.read();
        pos++;
        if (nextInput == (int) 'e' || nextInput == (int) 'E' ) {
          nextInput = source.read();
          pos++;
          numStr += (char) nextInput;
          if (nextInput == (int) '+' || nextInput == (int) '-') {
            nextInput = source.read();
            pos++;
            if (num0InASCII <= nextInput || num9InASCII >= nextInput) {
              do {
                numStr += (char) nextInput;
                nextInput = source.read();
                pos++;
              } while (num0InASCII <= nextInput || num9InASCII >= nextInput); 
            } else {
              throw new ParseException("Invalid format for JSONReal", pos);
            } // if...else
          } else if (num0InASCII <= nextInput || num9InASCII >= nextInput) {
            do {
              numStr += (char) nextInput;
              nextInput = source.read();
              pos++;
            } while (num0InASCII <= nextInput || num9InASCII >= nextInput);
          } else {
            throw new ParseException("Invalid format for JSONReal", pos);
          } // if
          return new JSONReal(numStr);
        } else if (nextInput == (int) '.') {
            nextInput = source.read();
            pos++;
            numStr += (char) nextInput;
            if (num0InASCII <= nextInput || num9InASCII >= nextInput) {
              do {
                numStr += (char) nextInput;
                nextInput = source.read();
                pos++;
              } while (num0InASCII <= nextInput || num9InASCII >= nextInput); 
            } else {
              throw new ParseException("Invalid format for JSONReal", pos);
            } // if...else
            return new JSONReal(numStr);
        } else {
          return new JSONInteger(numStr);
        } // if...else      
      } // if
    } // if

    switch (input) {
      // String
      case 147:
        String strForJSONString = "";
        nextInput = source.read();
        pos++;
        
        while (nextInput != 148) {
          strForJSONString += nextInput;
          nextInput = source.read();
          pos++;
        }
        return new JSONString(strForJSONString);

      // Object (Hash)
      case (int) '{':
        JSONHash hashtable = new JSONHash();

        nextInput = source.read();
        pos++;
        while (nextInput != (int) '}') {
          JSONValue key = parseKernel(source);
          if (!(key instanceof JSONString)) {
            throw new ParseException("Invalid key for JSONHash", pos);
          } // if

          nextInput = source.read();
          pos++;
          if (nextInput != ':') {
            throw new ParseException("Invalid format for JSONHash", pos);
          } // if
          
          JSONValue value = parseKernel(source);

          hashtable.set((JSONString) key, value);

          nextInput = source.read();
          pos++;
          if (nextInput != (int) ',') {
            throw new ParseException("Invalid format for JSONHash", pos);
          } // if

          return hashtable;
        } // while

      // Array 
      case (int) '[':
        
        JSONArray arr = new JSONArray();
        nextInput = source.read();
        pos++;
        while (nextInput != (int) ']') {
          arr.add(parseKernel(source));
          nextInput = source.read();
          pos++;
          if (nextInput != (int) ',') {
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
