package org.renjin.gcc.jimple;

public class JimpleInterfaceBuilder extends AbstractClassBuilder {

  @Override
  public JimpleMethodBuilder newMethod() {
    JimpleMethodBuilder builder = super.newMethod();
    builder.setModifiers(JimpleModifiers.PUBLIC, JimpleModifiers.ABSTRACT);
    return builder;
  }

  @Override
  public void write(JimpleWriter w) {
    w.println("public interface " + getFqcn() + " extends java.lang.Object");
    w.startBlock();

    for (JimpleMethodBuilder method : getMethods()) {
      w.println();
      method.write(w);
    }

    w.closeBlock();
  }
}
