package org.renjin.gcc.jimple;

import com.google.common.collect.Lists;

import java.util.List;

public class JimpleInterfaceBuilder extends AbstractClassBuilder {

  private List<String> superInterfaces = Lists.newArrayList();
  
  @Override
  public JimpleMethodBuilder newMethod() {
    JimpleMethodBuilder builder = super.newMethod();
    builder.setModifiers(JimpleModifiers.PUBLIC, JimpleModifiers.ABSTRACT);
    return builder;
  }


  public JimpleInterfaceBuilder extendsInterface(String name) {
    superInterfaces.add(name);
    return this;
  }

  @Override
  public void write(JimpleWriter w) {
    w.println("public interface " + getFqcn() + " extends java.lang.Object " + implementClause());
    w.startBlock();

    for (JimpleMethodBuilder method : getMethods()) {
      w.println();
      method.write(w);
    }

    w.closeBlock();
  }

  private String implementClause() {
    if(superInterfaces.isEmpty()) {
      return "";
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append(" implements ");
      for(int i=0;i!=superInterfaces.size();++i) {
        if(i > 0) {
          sb.append(", ");
        }
        sb.append(superInterfaces.get(i));
      }
      return sb.toString();
    }
  }

}
