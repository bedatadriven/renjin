package org.renjin.gcc.translate;

import com.google.common.collect.Maps;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleExternal;
import org.renjin.gcc.gimple.expr.GimpleVar;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleMethodBuilder;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.types.TypeTranslator;
import org.renjin.gcc.translate.var.Variable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class FunctionContext {

  private TranslationContext translationContext;
  private GimpleFunction gimpleFunction;
  private JimpleMethodBuilder builder;
  private Map<String, Variable> variables = Maps.newHashMap();

  private int nextTempId = 0;
  private int nextLabelId = 1000;

  public FunctionContext(TranslationContext translationContext,
                         GimpleFunction gimpleFunction, JimpleMethodBuilder builder) {
    this.gimpleFunction = gimpleFunction;
    this.translationContext = translationContext;
    this.builder = builder;

    for(GimpleVarDecl decl : gimpleFunction.getVariableDeclarations()) {
      Variable localVariable = translationContext.resolveType(decl.getType()).createLocalVariable(this, decl.getName());
      variables.put(decl.getName(), localVariable);
    }

    for(GimpleParameter param : gimpleFunction.getParameters()) {
      TypeTranslator type = translationContext.resolveType(param.getType());
      builder.addParameter(type.paramType(), param.getName());
      Variable variable = type.createLocalVariable(this, param.getName());
      variable.initFromParameter();
      variables.put(param.getName(), variable);
    }
  }

  public String declareTemp(JimpleType type) {
    String name = "_tmp" + (nextTempId++);
    builder.addVarDecl(type, name);
    return name;
  }

  public JimpleMethodBuilder getBuilder() {
    return builder;
  }

  public TranslationContext getTranslationContext() {
    return translationContext;
  }

  public String newLabel() {
    return "trlabel" + (nextLabelId++) + "__";
  }

  public Variable lookupVar(GimpleVar var) {
    return lookupVar(var.getName());
  }

  private Variable lookupVar(String name) {
    Variable variable = variables.get(name);
    if(variable == null) {
      throw new IllegalArgumentException("No such variable " + name);
    }
    return variable;
  }


  public Variable lookupVar(GimpleExpr gimpleExpr) {
    if(gimpleExpr instanceof GimpleVar) {
      return lookupVar((GimpleVar)gimpleExpr);
    } else {
      throw new UnsupportedOperationException("Expected GimpleVar, got: " + gimpleExpr + " [" + gimpleExpr.getClass().getSimpleName() + "]");
    }
  }

  public Collection<Variable> getVariables() {
    return variables.values();
  }

  public JimpleExpr asNumericExpr(GimpleExpr gimpleExpr) {
    if(gimpleExpr instanceof GimpleVar) {
      Variable variable = lookupVar((GimpleVar)gimpleExpr);
      return variable.asNumericExpr();
    } else if (gimpleExpr instanceof GimpleConstant) {
      return asConstant(((GimpleConstant) gimpleExpr).getValue());
    } else if (gimpleExpr instanceof GimpleExternal) {
      return fromField((GimpleExternal) gimpleExpr);
    } else {
      throw new UnsupportedOperationException(gimpleExpr.toString());
    }
  }

  private JimpleExpr fromField(GimpleExternal external) {
    Field field = translationContext.findField(external);
    return JimpleExpr.staticFieldReference(field);
  }

  private JimpleExpr asConstant(Object value) {
    if(value instanceof Double) {
      return JimpleExpr.doubleConstant(((Double) value).doubleValue());
    } else if(value instanceof Integer) {
      return JimpleExpr.integerConstant(((Integer) value).intValue());
    } else {
      throw new UnsupportedOperationException(value.toString());
    }
  }

}
