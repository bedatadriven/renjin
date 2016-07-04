package org.renjin.invoke.reflection;

public class CallJavaFromRTest {

  private MyCakeTest cakeConverted = null;
  // field with get set should be see in R
  private int intConverted = 0;// For public
  private boolean boolConverted = false;
  private double doubleConverted = 0;
  private String stringConverted = "";

  public MyCakeTest getFileConverted() {
    return cakeConverted;
  }

  public void setFileConverted(MyCakeTest cakeConverted) {
    this.cakeConverted = cakeConverted;
  }

  public int getIntConverted() {
    return this.intConverted;
  }

  public void setIntConverted(int intConverted) {
    this.intConverted = intConverted;
  }

  public double getDoubleConverted() {
    return this.doubleConverted;
  }

  public void setDoubleConverted(double doubleConverted) {
    this.doubleConverted = doubleConverted;
  }

  public String getStringConverted() {
    return this.stringConverted;
  }

  public void setStringConverted(String stringConverted) {
    this.stringConverted = stringConverted;
  }

  public CallJavaFromRTest(MyCakeTest cake) {
    String result = "Java Object Constructed in R,Type of argument is MyCakeTest:"
        + cake.getJam();
    System.out.println(result);
    this.cakeConverted = cake;
  }

  public CallJavaFromRTest(int intNumber) {
    String result = "Java Object Constructed in R,Type of argument is int:"
        + intNumber;
    System.out.println(result);
    this.intConverted = intNumber;
  }

  public CallJavaFromRTest(boolean logical) {
    String result = "Java Object Constructed in R,Type of argument is boolean:"
        + logical;
    this.boolConverted = logical;
    System.out.println(result);
  }

  public CallJavaFromRTest(double doubleNumber) {
    String result = "Java Object Constructed in R,Type of argument is double:"
        + doubleNumber;
    System.out.println(result);
    this.doubleConverted = doubleNumber;
  }

  public CallJavaFromRTest(String testString) {
    String result = "Java Object Constructed in R,Type of argument is String:"
        + testString;
    System.out.println(result);
    this.stringConverted = testString;
  }

  public static String StaticMethod(String str) {
    String result = "You called StaticMethod in R,Type of argument is String = "
        + str;
    System.out.println(result);
    return result;
  }

  // inStringArray<-c("test","StringArray","ok")
  public static String[] StringArrayConvert(String[] str) {
    System.out
        .println("You called StaticMethod in R,Type of argument is String[]");
    for (int i = 0; i < str.length; i++) {
      System.out.print(str[i] + " ");
    }
    System.out.print("\n");
    return str;
  }

  // inDoubleArray<-c(1.2,4.5,7.3)
  public static Double[] DoubleArrayConvert(Double[] values) {
    System.out
        .println("You called StaticMethod in R,Type of argument is Double[]");
    for (int i = 0; i < values.length; i++) {
      values[i] += 1;
      System.out.print(values[i] + " ");
    }
    System.out.print("\n");
    return values;
  }

  // inIntArray<-as.integer(c(1,3,5))
  public static Integer[] IntArrayConvert(Integer[] values) {
    System.out
        .println("You called StaticMethod in R,Type of argument is Integer[]");
    for (int i = 0; i < values.length; i++) {
      values[i] += 1;
      System.out.print(values[i] + " ");
    }
    System.out.print("\n");
    return values;
  }

  // inBooleanArray<-c(TRUE,FALSE,FALSE)
  public static Boolean[] BooleanArrayConvert(Boolean[] values) {
    System.out
        .println("You called StaticMethod in R,Type of argument is Boolean[]");
    for (int i = 0; i < values.length; i++) {
      if (values[i]) {
        values[i] = Boolean.FALSE;
      } else {
        values[i] = Boolean.TRUE;
      }
      System.out.print(values[i] + " ");
    }
    System.out.print("\n");
    return values;
  }

  public double getDoubleNumber() {
    return doubleConverted;
  }

  public void setDoubleNumber(double doubleNumber) {
    this.doubleConverted = doubleNumber;
  }

  public void setBoolConverted(boolean boolConverted) {
    this.boolConverted = boolConverted;
  }

  public boolean isBoolConverted() {
    return boolConverted;
  }

  public MyCakeTest getCakeConverted() {
    return cakeConverted;
  }

  public void setCakeConverted(MyCakeTest cakeConverted) {
    this.cakeConverted = cakeConverted;
  }
  
  public static void throwException() {
    throw new RuntimeException("FOO!");
  }
}