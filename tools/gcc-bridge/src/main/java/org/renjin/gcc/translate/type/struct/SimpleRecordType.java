package org.renjin.gcc.translate.type.struct;

import com.google.common.collect.Maps;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
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
  private TranslationContext context;
  private GimpleRecordTypeDef recordTypeDef;
  private String name;
  private JimpleClassBuilder recordClass;

  private Map<String, ImType> types = Maps.newHashMap();

  public SimpleRecordType(TranslationContext context, GimpleRecordTypeDef recordType) {
    this.context = context;
    this.recordTypeDef = recordType;
    this.name = recordType.getName();
    this.recordClass = context.getJimpleOutput().newClass();
    this.recordClass.setPackageName(context.getMainClass().getPackageName());
    this.recordClass.setClassName(context.getMainClass().getClassName() + "$" + name);
  }

  public void resolveFields() {
    for (GimpleField member : recordTypeDef.getFields()) {
      ImType type = context.resolveType(member.getType());
      types.put(member.getName(), type);


      JimpleFieldBuilder field = recordClass.newField();
      field.setName(member.getName());
      field.setType(type.returnType()); // TODO: probably need a fieldType()
      field.setModifiers(JimpleModifiers.PUBLIC);
    }
  }

  @Override
  public JimpleType getJimpleType() {
    return new SyntheticJimpleType(recordClass.getFqcn());
  }

  @Override
  public ImType pointerType() {
    return new SimpleRecordPtrType(context, this);
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
    throw new UnsupportedOperationException();
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage varUsage) {
    return new SimpleRecordVar(functionContext, gimpleName, this);
  }

  @Override
  public ImExpr createFieldExpr(String instanceExpr, JimpleType classType, String memberName) {
    throw new UnsupportedOperationException();
  }

  public int getMemberCount() {
    return types.size();
  }

  @Override
  public String toString() {
    return "<struct: "  + name + ">";
  }

  public ImPrimitiveType getMemberType(String member) {
    ImType type = types.get(member);
    if(type == null) {
      throw new IllegalArgumentException("no member named '" + member + "'");
    }
    return (ImPrimitiveType) type;
  }
}
