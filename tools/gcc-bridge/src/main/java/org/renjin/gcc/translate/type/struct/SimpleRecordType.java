package org.renjin.gcc.translate.type.struct;

import com.google.common.collect.Maps;
import org.renjin.gcc.jimple.*;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.TranslationContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.type.ImPrimitiveType;
import org.renjin.gcc.translate.type.ImType;
import org.renjin.gcc.translate.var.SimpleRecordVar;
import org.renjin.gcc.translate.var.Variable;

import java.util.Map;

/**
 * Represents a simple implementation of a record type, where
 * each gimple field is mapped to a public JVM field.
 */
public class SimpleRecordType extends ImRecordType {
  private JimpleType jimpleType;

  private Map<String, ImType> types = Maps.newHashMap();

  public SimpleRecordType(JimpleType jimpleType) {
    this.jimpleType = jimpleType;
  }
  @Override
  public JimpleType getJimpleType() {
    return jimpleType;
  }

  @Override
  public ImType pointerType() {
    return new SimpleRecordPtrType(this);
  }

  @Override
  public ImType arrayType(Integer lowerBound, Integer upperBound) {
    return new SimpleRecordArrayType(this, lowerBound, upperBound);
  }

  @Override
  public JimpleType paramType() {
    return getJimpleType();
  }

  @Override
  public JimpleType returnType() {
    return getJimpleType();
  }

  @Override
  public void defineField(JimpleClassBuilder classBuilder, String memberName, boolean isStatic) {
    JimpleFieldBuilder field = classBuilder.newField();
    field.setName(memberName);
    field.setType(getJimpleType());
    if(isStatic) {
      field.setModifiers(JimpleModifiers.PUBLIC, JimpleModifiers.STATIC);
    } else {
      field.setModifiers(JimpleModifiers.PUBLIC);
    }
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage varUsage) {
    return new SimpleRecordVar(functionContext, gimpleName, this);
  }

  @Override
  public ImExpr createFieldExpr(String instanceExpr, JimpleType classType, String memberName) {
    throw new UnsupportedOperationException();
  }

  public void addMember(String name, ImType type) {
    types.put(name, type);
  }

  public int getMemberCount() {
    return types.size();
  }

  @Override
  public String toString() {
    return "<struct: "  + jimpleType + ">";
  }

  public ImPrimitiveType getMemberType(String member) {
    ImType type = types.get(member);
    if(type == null) {
      throw new IllegalArgumentException("no member named '" + member + "'");
    }
    return (ImPrimitiveType) type;
  }

}
