package org.renjin.gcc.translate.type;

import org.renjin.gcc.jimple.JimpleClassBuilder;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.jimple.SyntheticJimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.TranslationContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.var.Variable;

/**
 * An intermediate representation of a type used during translation
 */
public interface ImType {

  JimpleType paramType();

  JimpleType returnType();

  /**
   * Define of a field of this type within the given classBuilder.
   *  @param context
   * @param classBuilder builder for the class to which we need to add the field
   * @param memberName the name of the field member
   * @param isStatic true if the field should be static
   */
  void defineField(JimpleClassBuilder classBuilder, String memberName, boolean isStatic);

  Variable createLocalVariable(
      FunctionContext functionContext,
      String gimpleName,
      VarUsage varUsage);

  ImExpr createFieldExpr(String instanceExpr, JimpleType classType, String memberName);

  ImType pointerType();

  ImType arrayType(Integer lowerBound, Integer upperBound);

}
