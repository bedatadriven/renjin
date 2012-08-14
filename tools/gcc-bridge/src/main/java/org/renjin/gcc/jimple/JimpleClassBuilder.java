package org.renjin.gcc.jimple;

import com.google.common.collect.Lists;

import java.util.List;

public class JimpleClassBuilder extends AbstractClassBuilder {

  private final JimpleOutput output;
  private final List<String> interfaces = Lists.newArrayList();

  JimpleClassBuilder(JimpleOutput output) {
    this.output = output;
  }

  public void addInterface(String interfaceName) {
    interfaces.add(interfaceName);
  }

  @Override
  public void write(JimpleWriter w) {
    w.println("public class " + getFqcn() + " extends java.lang.Object" + implementsText());
    w.startBlock();

    w.println("public void <init>()");
    w.startBlock();
    w.println(getFqcn() + " r0;");
    w.println("r0 := @this: " + getFqcn() + ";");
    w.println("specialinvoke r0.<java.lang.Object: void <init>()>();");
    w.println("return;");
    w.closeBlock();

    for(JimpleMethodBuilder method : getMethods()) {
      w.println();
      method.write(w);
    }

    w.closeBlock();
  }

  private String implementsText() {
    if(interfaces.isEmpty()) {
      return "";
    } else {
      StringBuilder sb = new StringBuilder(" implements ");
      boolean needsComma = false;
      for(String interfaceName : interfaces) {
        if(needsComma) {
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
