package org.renjin.gcc.jimple;

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

  public static JimpleExpr doubleConstant(Number number) {
    return doubleConstant(number.doubleValue());
  }

  public static JimpleExpr integerConstant(Number number) {
    return integerConstant(number.intValue());
  }

  public static JimpleExpr staticFieldReference(Field field) {
    return new JimpleExpr("<" + field.getDeclaringClass().getName() + ": " +
            Jimple.type(field.getType()) + " " + field.getName() + ">");
  }

  public static JimpleExpr cast(JimpleExpr expr, JimpleType type) {
    return new JimpleExpr( "(" + type + ")" + expr);
  }
}