package org.renjin.gcc.jimple;

/**
 * 
 * Reference to a JVM class that will be constructed for a struct, but does not
 * yet exist.
 */
public class SyntheticJimpleType extends JimpleType {

  private final String fullyQualifiedClassName;

  /**
   * @param fullyQualifiedClassName  the fully-qualified class name (including
   *              package and class name, with dots)
   */
  public SyntheticJimpleType(String fullyQualifiedClassName) {
    super();
    this.fullyQualifiedClassName = fullyQualifiedClassName;
  }

  @Override
  public String toString() {
    return fullyQualifiedClassName;
  }

  @Override
  public boolean is(Class clazz) {
    return clazz.getName().equals(fullyQualifiedClassName);
  }

  @Override
  public JimpleType arrayType() {
    return new SyntheticJimpleType(fullyQualifiedClassName + "[]");
  }
}
