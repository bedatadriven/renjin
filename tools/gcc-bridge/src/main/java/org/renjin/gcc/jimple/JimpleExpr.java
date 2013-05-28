package org.renjin.gcc.jimple;

import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;

import java.lang.reflect.Field;

public class JimpleExpr {
  private String text;

  public JimpleExpr(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }

  public static JimpleExpr binaryInfix(String operator, JimpleExpr a, JimpleExpr b) {
    return new JimpleExpr(a + " " + operator + " " + b);
  }

  public static JimpleExpr doubleConstant(double x) {
    return new JimpleExpr(Double.toString(x));
  }

  public static JimpleExpr integerConstant(int x) {
    return new JimpleExpr(Integer.toString(x));
  }

  public static JimpleExpr longConstant(long x) {
    return new JimpleExpr(Long.toString(x) + "L");
  }

  public static JimpleExpr doubleConstant(Number number) {
    return doubleConstant(number.doubleValue());
  }

  public static JimpleExpr integerConstant(Number number) {
    return integerConstant(number.intValue());
  }

  public static JimpleExpr staticFieldReference(Field field) {
    return new JimpleExpr("<" + field.getDeclaringClass().getName() + ": " +
        JimpleType.valueOf(field.getType()) + " "
        + field.getName() + ">");
  }

  public static JimpleExpr cast(JimpleExpr expr, JimpleType type) {
    return new JimpleExpr("(" + type + ")" + expr);
  }

  public static JimpleExpr doubleConstant(GimpleExpr gimpleExpr) {
    Object value = ((GimpleConstant) gimpleExpr).getValue();
    return doubleConstant((Number) value);
  }

  public static JimpleExpr integerConstant(GimpleExpr gimpleExpr) {
    Object value = ((GimpleConstant) gimpleExpr).getValue();
    return integerConstant((Number) value);
  }

  public static JimpleExpr floatConstant(float floatValue) {
    return new JimpleExpr(Float.toString(floatValue) + "F");
  }

  public static JimpleExpr stringLiteral(String value) {
    StringBuilder literal = new StringBuilder();
    literal.append("\"");
    for(int i=0;i!=value.length();++i) {
      int cp = value.codePointAt(i);
      if(cp == '"') {
        literal.append("\\\"");
      } else if(cp == '\\') {
        literal.append("\\\\");
      } else if(cp >= 32 && cp <= 126) {
        literal.appendCodePoint(cp);
      } else {
        literal.append(String.format("\\u%04x", cp));
      }
    }
    literal.append("\"");
    return new JimpleExpr(literal.toString());
  }
}