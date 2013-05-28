package org.renjin.gcc.jimple;

import com.google.common.collect.Lists;

import java.util.List;

public class JimpleClassBuilder extends AbstractClassBuilder {

  private final JimpleOutput output;
  private final List<String> interfaces = Lists.newArrayList();
  
  private final JimpleMethodBuilder staticInitializer;

  JimpleClassBuilder(JimpleOutput output) {
    this.output = output;
  
    staticInitializer = new JimpleMethodBuilder(this);
    staticInitializer.setName("<clinit>");
    staticInitializer.setModifiers(JimpleModifiers.STATIC);
    staticInitializer.setReturnType(JimpleType.VOID);
  }

  public void addInterface(String interfaceName) {
    interfaces.add(interfaceName);

  }

  public JimpleMethodBuilder getStaticInitializer() {
    return staticInitializer;
  }

  @Override
  public void write(JimpleWriter w) {
    w.println("public class " + getFqcn() + " extends java.lang.Object" + implementsText());
    w.startBlock();

    for (JimpleFieldBuilder field : getFields()) {
      field.write(w);
    }

    writeConstructor(w);
    writeStaticInitializer(w);

    for (JimpleMethodBuilder method : getMethods()) {
      w.println();
      method.write(w);
    }

    w.closeBlock();
  }

  private void writeStaticInitializer(JimpleWriter w) {
    if(staticInitializer.hasBody()) {
      staticInitializer.addStatement("return");
      staticInitializer.write(w);
    }
    
  }

  private void writeConstructor(JimpleWriter w) {
    w.println("public void <init>()");
    w.startBlock();
    w.println(getFqcn() + " r0;");
    w.println("r0 := @this: " + getFqcn() + ";");
    w.println("specialinvoke r0.<java.lang.Object: void <init>()>();");
    w.println("return;");
    w.closeBlock();
  }

  private String implementsText() {
    if (interfaces.isEmpty()) {
      return "";
    } else {
      StringBuilder sb = new StringBuilder(" implements ");
      boolean needsComma = false;
      for (String interfaceName : interfaces) {
        if (needsComma) {
          sb.append(", ");
        }
        sb.append(interfaceName);
      }
      return sb.toString();
    }
  }

  public JimpleOutput getOutput() {
    return output;
  }

}
