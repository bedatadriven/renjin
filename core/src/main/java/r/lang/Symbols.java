package r.lang;

public class Symbols {
  
  private Symbols() {}

  public static final Symbol NAMES = Symbol.get("names");
  public static final Symbol DIM = Symbol.get("dim");
  public static final Symbol CLASS = Symbol.get("class");
  public static final Symbol LEVELS = Symbol.get("levels");
  public static final Symbol STDOUT = Symbol.get("stdout");
  public static final Symbol SRC_REF = Symbol.get("srcref");
  public static final Symbol ELLIPSES = Symbol.get("...");
  public static final Symbol SRC_FILE = Symbol.get("srcfile");
  public static final Symbol TEMP_VAL = Symbol.get("*tmp*");
  public static final Symbol DIMNAMES = Symbol.get("dimnames");
  public static final Symbol NAME = Symbol.get("name");
  public static final Symbol DOT_ENVIRONMENT = Symbol.get(".Environment");

  public static final Symbol TZONE = Symbol.get("tzone");
  
  /**
   * Identifies the {@code row.names} attribute, which contains an {@code AtomicVector} with the
   * names of the rows of a {@code data.frame} object. Note: This attribute is different than the 
   * names of matrix rows: those are stored as an element in the {@code dimnames} attribute.
   */
  public static final Symbol ROW_NAMES = Symbol.get("row.names");
  public static final Symbol AS_CHARACTER = Symbol.get("as.character");
  public static final Symbol TEMP = Symbol.get("*tmp*");

  
}
