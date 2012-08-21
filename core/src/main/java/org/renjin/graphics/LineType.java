package org.renjin.graphics;

import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;

public enum LineType {
  BLANK(0),
  SOLID(1),
  DASHED(2),
  DOTTED(3),
  DOTDASH(4),
  LONGDASH(5),
  TWODASH(6);

  private final int code;

  LineType(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public static LineType valueOf(SEXP exp) {
    return valueOf(exp);
  }

  public static LineType valueOf(SEXP exp, int elementIndex) {
    if(exp instanceof StringVector) {
      return valueOf(((StringVector) exp).getElementAsString(elementIndex).toUpperCase());
    } else if(exp instanceof IntVector) {
      return valueOf(((IntVector) exp).getElementAsInt(elementIndex));
    } else {
      throw new IllegalArgumentException("" + exp);
    }
  }

  private static LineType valueOf(int code) {
    for(LineType lineType : values()) {
      if(lineType.getCode() == code) {
        return lineType;
      }
    }
    throw new IllegalArgumentException("Unknown LineType code: " + code);
  }

  public StringVector toExpression() {
    return new StringArrayVector(name().toLowerCase());
  }
}
