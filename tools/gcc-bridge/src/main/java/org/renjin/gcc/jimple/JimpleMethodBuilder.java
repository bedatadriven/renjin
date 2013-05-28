package org.renjin.gcc.jimple;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

public class JimpleMethodBuilder {

  private AbstractClassBuilder classBuilder;

  private String name;
  private JimpleType returnType;

  private List<JimpleVarDecl> varDecls = Lists.newArrayList();
  private List<JimpleParam> params = Lists.newArrayList();
  private List<JimpleBodyElement> body = Lists.newArrayList();
  private Set<JimpleModifiers> modifiers = Sets.newHashSet(JimpleModifiers.PUBLIC);

  private int nextTempIndex = 1;

  JimpleMethodBuilder(AbstractClassBuilder classBuilder) {
    this.classBuilder = classBuilder;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public JimpleType getReturnType() {
    return returnType;
  }

  public void addVarDecl(JimpleType type, String name) {
    varDecls.add(new JimpleVarDecl(type, name));
  }

  public String addTempVarDecl(JimpleType type) {
    String name = "__temp" + (nextTempIndex++);
    varDecls.add(new JimpleVarDecl(type, name));
    return name;
  }
  
  public boolean hasBody() {
    return !this.body.isEmpty();
  }

  public void addParameter(JimpleType type, String name) {
    JimpleParam jimpleParam = new JimpleParam(type, name);
    params.add(jimpleParam);
    varDecls.add(jimpleParam);
  }

  public void addLabel(String labelName) {
    body.add(new JimpleLabel(labelName));
  }

  public void setReturnType(JimpleType returnType) {
    this.returnType = returnType;
  }

  public void setModifiers(JimpleModifiers... modifiers) {
    this.modifiers = Sets.newHashSet(modifiers);
  }

  public void addStatement(String text) {
    body.add(new JimpleStatement(text));
  }

  public void addStatement(JimpleStatement statement) {
    body.add(statement);
  }

  public void addAssignment(String tempVar, JimpleExpr value) {
    addStatement(tempVar + " = " + value);
  }

  public void add(JimpleBodyElement bodyElement) {
    this.body.add(bodyElement);
  }

  public void write(JimpleWriter w) {
    if (modifiers.contains(JimpleModifiers.ABSTRACT)) {
      w.println(modifierList() + " " + returnType + " " + name + "(" + paramList() + ");");
    } else {
      w.println(modifierList() + " " + returnType + " " + name + "(" + paramList() + ")");
      w.startBlock();

      for (JimpleVarDecl decl : varDecls) {
        w.println(decl.toString() + ";");
      }

      for (int i = 0; i != params.size(); ++i) {
        w.println(params.get(i).getName() + " := @parameter" + i + ": " + params.get(i).getType() + ";");
      }

      for (JimpleBodyElement bodyElement : body) {
        bodyElement.write(w);
      }

      w.closeBlock();
    }
  }

  private String paramList() {
    StringBuilder sb = new StringBuilder();
    for (JimpleParam param : params) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(param.getType());
    }
    return sb.toString();
  }

  private String modifierList() {
    StringBuilder sb = new StringBuilder();
    JimpleModifiers order[] = new JimpleModifiers[] { JimpleModifiers.PUBLIC, JimpleModifiers.STATIC,
        JimpleModifiers.ABSTRACT };
    for (JimpleModifiers modifier : order) {
      if (this.modifiers.contains(modifier)) {
        sb.append(" ").append(modifier.name().toLowerCase());
      }
    }
    return sb.toString().trim();
  }

  public void addVarDecl(Class clazz, String name) {
    addVarDecl(new RealJimpleType(clazz), name);
  }

}
