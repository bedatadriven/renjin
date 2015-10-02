package org.renjin.gcc.jimple;

import com.google.common.collect.Lists;
import org.objectweb.asm.ClassVisitor;

import java.util.List;

public abstract class AbstractClassBuilder {
  private String packageName;
  private String className;
  
  private ClassVisitor classVisitor;

  private final List<JimpleFieldBuilder> fields = Lists.newArrayList();
  private final List<JimpleMethodBuilder> methods = Lists.newArrayList();

  

  public String getFqcn() {
    if (packageName == null) {
      return className;
    } else {
      return packageName + "." + className;
    }
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public JimpleMethodBuilder newMethod() {
    JimpleMethodBuilder method = new JimpleMethodBuilder(this);
    methods.add(method);
    return method;
  }

  public JimpleFieldBuilder newField() {
    JimpleFieldBuilder field = new JimpleFieldBuilder();
    fields.add(field);
    return field;
  }

  public List<JimpleFieldBuilder> getFields() {
    return fields;
  }

  public List<JimpleMethodBuilder> getMethods() {
    return methods;
  }

  public abstract void write(JimpleWriter w);
}
