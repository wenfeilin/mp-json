import java.io.PrintWriter;

/**
 * JSON strings.
 */
public class JSONString implements JSONValue {

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The underlying string.
   */
  String value;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Build a new JSON string for a particular string.
   */
  public JSONString(String value) {
    this.value = value;
  } // JSONString(String)

  // +-------------------------+-------------------------------------
  // | Standard object methods |
  // +-------------------------+

  /**
   * Convert to a string (e.g., for printing).
   */
  public String toString() {
    return this.value;
  } // toString()

  /**
   * Compare to another object.
   */
  public boolean equals(Object other) {
    if (other instanceof JSONString) {
      return this.value.equals(((JSONString) other).getValue());
    } else {
      return false;
    }
  } // equals(Object)

  /**
   * Compute the hash code.
   */
  public int hashCode() {
    return this.value.hashCode();
  } // hashCode()

  // +--------------------+------------------------------------------
  // | Additional methods |
  // +--------------------+

  /**
   * Write the value as JSON.
   */
  public void writeJSON(PrintWriter pen) {
    String JSONstr = "\"";

    for (int i = 0; i < this.value.length(); i++) {
      char currentChar = this.value.charAt(i);

      if (Character.isLetter(currentChar) || Character.isDigit(currentChar)
          || Character.isSpaceChar(currentChar)) {
        JSONstr += currentChar;
      } else {
        switch (currentChar) {
          case '\n':
            JSONstr += "\\n";
            break;
          case '\t':
            JSONstr += "\\t";
            break;
          case '\r':
            JSONstr += "\\r";
            break;
          case '\"':
            JSONstr += "\"";
            break;
          case '\'':
            JSONstr += "\'";
            break;
          case '\b':
            JSONstr += "\b'";
            break;
          case '\\':
            JSONstr += "\\";
            break;
          case '\f':
            JSONstr += "\\f";
            break;
        } // switch
      } // if/else
    } // for

    pen.print(JSONstr + "\"");
    pen.flush();
  } // writeJSON(PrintWriter)

  /**
   * Get the underlying value.
   */
  public String getValue() {
    return this.value;
  } // getValue()
} // class JSONString
